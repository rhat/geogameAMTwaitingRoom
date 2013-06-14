package edu.cmu.cs.cimds.geogame.client.services;//edu.psu.ist.acs.geogame.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import edu.cmu.cs.cimds.geogame.client.exception.DBException;

@RemoteServiceRelativePath("WaitingRoomService")
public interface WaitingRoomService extends RemoteService {
	int roomCount();
	int roomMaxCount();
	void enter(String id) throws IllegalArgumentException;
	void leave(String id) throws IllegalArgumentException;
	void setRoomMax(int i);
	boolean isKeyValid(String key, boolean purge) throws DBException;
}
