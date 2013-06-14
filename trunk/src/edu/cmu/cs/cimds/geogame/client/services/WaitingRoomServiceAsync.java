package edu.cmu.cs.cimds.geogame.client.services;//edu.psu.ist.acs.geogame.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import edu.cmu.cs.cimds.geogame.client.exception.DBException;

public interface WaitingRoomServiceAsync {
	void roomCount(AsyncCallback callback);
	void roomMaxCount(AsyncCallback callback);
	void enter(String id, AsyncCallback callback);
	void leave(String id, AsyncCallback callback);
	void setRoomMax(int i, AsyncCallback callback);
	void isKeyValid(String key, boolean purge, AsyncCallback callback) throws DBException ;
}
