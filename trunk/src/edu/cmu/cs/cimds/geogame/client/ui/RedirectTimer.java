package edu.cmu.cs.cimds.geogame.client.ui;//edu.psu.ist.acs.geogame.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

public class RedirectTimer extends Timer {

	protected String target;
		
	public RedirectTimer(String url){
		super();
		target = url;
	}
	
	@Override
	public void run() {
		RedirectTimer.redirect(target);
	}

	
	public static void redirect(String url){
		Window.Location.assign(url);
	}
	
	/**
	 * Try not to call this, since it will only ever 
	 * execute once. Really, you should just call schedule(int).
	 */
	@Deprecated
	@Override
	public void scheduleRepeating(int periodMillis) {
		// TODO Auto-generated method stub
		super.scheduleRepeating(periodMillis);
	}
	
	

}
