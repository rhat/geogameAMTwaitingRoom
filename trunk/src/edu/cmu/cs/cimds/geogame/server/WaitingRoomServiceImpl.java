package edu.cmu.cs.cimds.geogame.server;//edu.psu.ist.acs.geogame.server;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Logger;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jasypt.util.password.BasicPasswordEncryptor;
import edu.cmu.cs.cimds.geogame.client.exception.AuthorizationException;
import edu.cmu.cs.cimds.geogame.client.exception.DBException;
import edu.cmu.cs.cimds.geogame.client.exception.GeoGameException;


//import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
//import edu.psu.ist.acs.geogame.client.WaitingRoomService;
import edu.cmu.cs.cimds.geogame.client.services.*;
import edu.cmu.cs.cimds.geogame.client.model.db.AMTKey;
import edu.cmu.cs.cimds.geogame.client.model.dto.AMTKeyDTO;


public class WaitingRoomServiceImpl extends RemoteServiceServlet implements
		WaitingRoomService {
	
	
	private static final long serialVersionUID = 5567689990085678L;

	private static Logger logger = Logger.getLogger(LoginServiceImpl.class);
	
	public static final int DEFAULT_ROOM_SIZE = 5;
	protected static final AtomicInteger ROOM_MAX_COUNT = new AtomicInteger(DEFAULT_ROOM_SIZE); //change this to add more or less participants
	protected static final AtomicInteger ROOM_COUNT = new AtomicInteger(0);
	protected static final AtomicLong LAST_PURGE = new AtomicLong(System.currentTimeMillis());
	protected static final Map<String,Long> CURRENT_ENTRANTS = Collections.synchronizedMap(new HashMap<String,Long>());
	protected static final long USER_TIMEOUT = 45*1000;//users time out after 9 minutes, since the normal delay should be 5min
	protected static final long SLEEP = 15*1000;//number of millis for cleaner to sleep 
	
	
	@Override
	public int roomCount() {
		// TODO Auto-generated method stub
		return ROOM_COUNT.get();
	}

	@Override
	public int roomMaxCount() {
		// TODO Auto-generated method stub
		return ROOM_MAX_COUNT.get();
	}

	@Override 
	
	public void enter(String id) throws IllegalArgumentException {
		long now = System.currentTimeMillis();
		if(now > LAST_PURGE.get() + SLEEP){
			//GWT.log("Cleaning now!");
			LAST_PURGE.set(now);
			expireOldMembers();
		}
		
		if(!CURRENT_ENTRANTS.containsKey(id)){
			ROOM_COUNT.getAndIncrement();
		}
		CURRENT_ENTRANTS.put(id, (Long)System.currentTimeMillis());
			
	}

	@Override
	public void leave(String id) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		if(CURRENT_ENTRANTS.containsKey(id)){
			ROOM_COUNT.getAndDecrement();
			CURRENT_ENTRANTS.remove(id);
		}
	}
	
	protected static synchronized void expireOldMembers(){
		System.out.println("Collecting Garbage");
		if(ROOM_COUNT.get() > 0){
			Long now = (Long)System.currentTimeMillis();
			Long cutoff = now - USER_TIMEOUT;
			Set<String> badkeys = Collections.synchronizedSet(new HashSet<String>());
			System.out.println("Marking bad entries");
			Iterator<String> iter = CURRENT_ENTRANTS.keySet().iterator();
			while(iter.hasNext()){
				String id = iter.next();
				Long timestamp = CURRENT_ENTRANTS.get(id);
				//GWT.log("timestamp(" + timestamp + ")<cuttoff(" + cutoff + ")? " + (timestamp < cutoff? "YES" : "NO"));
				if(timestamp < cutoff){
					badkeys.add(id);
				}
			}
			
			int c=0;System.out.println("Expecting " + badkeys.size()  + "bad entries.");
			
			Iterator<String> badkeyIter = badkeys.iterator();
			while(badkeyIter.hasNext()){
				String key = badkeyIter.next();
				Object o = CURRENT_ENTRANTS.remove(key);
				ROOM_COUNT.decrementAndGet();
				if(o != null) c++;
			}
			System.out.println("Actually removed..." + c);
		}
	}

	@Override
	public void setRoomMax(int i) {
		ROOM_MAX_COUNT.set(i);
	}
	
	@Override
	public boolean isKeyValid(String key, boolean purge) throws DBException {
		logger.log(org.apache.log4j.Level.INFO,"Checking key:" + key + " and purge = " + purge);
		boolean res = false;
		if(key == null) return false;
		Session session = PersistenceManager.getSession();
		Transaction tx = session.beginTransaction();
		try {
			//Look in database for username
			AMTKey amtkey = (AMTKey)session.createCriteria(AMTKey.class)
					.add(Restrictions.eq("key", key)).uniqueResult();
			
			if(amtkey == null){
				res =  false;
			}else{
			
				boolean expired = AMTKey.expired(amtkey.getExpiration());
				res = !expired;
			
			}
			
			if(purge && res){//remove the key from the unused list, because its now a uid
				session.delete(amtkey);
			}
			logger.log(org.apache.log4j.Level.INFO,"Found key:" + key + " which is " + (!res? "expired " : "not expired ") + " and purge = " + purge);
			
			tx.commit();
			return res;
		} catch(HibernateException ex) {
			tx.rollback();
			//logger.error(ex.getMessage());
			throw new DBException(ex);
		} catch(NullPointerException ex) {
			tx.rollback();
			throw new DBException(ex);
		}
	}

}
