package edu.cmu.cs.cimds.geogame.client.ui;//edu.psu.ist.acs.geogame.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import edu.cmu.cs.cimds.geogame.client.services.*;

/**
 * 
 * This is a recurring poller for the room occupancy status.
 * 
 * Do not start this timer until the URL for the proxy
 * has been set elsewhere!
 * 
 * Will poll before scheduling anything, so don't worry
 * about setting your stuff up with too small of a scheduling
 * increment just to get early poll data.
 * 
 * @author rmk216@ist.psu.edu
 *
 */

public class RoomCountPoller extends Timer {

	protected WaitingRoomServiceAsync proxy;
	protected Command update;
	protected Command complete;
	protected int roomCount;
	protected int maxRoomCount;
	protected final Command NOOP = new Command(){ //used trivially
		@Override
		public void execute() {
			//do nothing instead of checking for nulls
		}
	};
	
	/**
	 * Will accept null Command inputs without complaint or error 
	 * @param proxy
	 * @param update
	 * @param complete
	 */
	public RoomCountPoller(WaitingRoomServiceAsync proxy, 
			Command update,
			Command complete){
		this.proxy = proxy;
		this.update = update == null? NOOP : update;
		this.complete = complete == null? NOOP : complete;
	}
	
	public int getRoomCount(){
		return roomCount;
	}
	
	/**
	 * This shouldn't really be called, since you want continuous 
	 * polling, so it just calls Timer.scheduleRepeating();
	 * It's not wrong to call this, but you really shouldn't.
	 */
	@Deprecated
	@Override
	public void schedule(int delayMillis) {
		poll();
		super.scheduleRepeating(delayMillis);
	}
	
	
	@Override
	public void scheduleRepeating(int periodMillis) {
		poll();
		super.scheduleRepeating(periodMillis);
	}
	

	public void poll(){
		proxy.roomMaxCount(new AsyncCallback(){
			@Override
			public void onFailure(Throwable caught) {
				GWT.log("cannot get room max", caught);
			}

			@Override
			public void onSuccess(Object result) {
				Integer res = (Integer)result;
				maxRoomCount = res;
			}
			
		});
		proxy.roomCount(new AsyncCallback(){
			@Override
			public void onFailure(Throwable caught) {
				GWT.log("cannot get room occupancy count", caught);
			}

			@Override
			public void onSuccess(Object result) {
				Integer res = (Integer)result;
				roomCount = res;
			}
			
		});

	}
	
	@Override
	public void run() {
		poll();
		if(roomCount >= maxRoomCount){
			complete.execute();
		}else{
			
			update.execute();
		}				
	}

	public void setUpdate(Command update) {
		this.update = update;
	}

	public void setComplete(Command complete) {
		this.complete = complete;
	}

	public int getMaxRoomCount() {
		return maxRoomCount;
	}

	
}
