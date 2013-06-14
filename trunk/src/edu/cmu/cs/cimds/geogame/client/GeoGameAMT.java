package edu.cmu.cs.cimds.geogame.client;//edu.psu.ist.acs.geogame.client;


import java.util.List;
import java.util.Map;

import edu.cmu.cs.cimds.geogame.client.services.WaitingRoomServiceAsync;
import edu.cmu.cs.cimds.geogame.client.services.GameServices;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import edu.cmu.cs.cimds.geogame.client.ui.*;

/**
 * This Class is the entry point for the Waiting Room. It will
 * be used to forward users on to whatever URL you like, probably
 * the Geogame host. The only catch is that the current version
 * still needs to have security elements added, as well as proper
 * styling.
 * 
 * 
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GeoGameAMT implements EntryPoint {
	/**
	 * Create a remote service proxy to talk to the server-side WaitingRoom service.
	 * Checking the RemoteSeriveRelativePath Annotation on WaitingRoomSerice to
	 * see what parts of the normal GWT-RPC setup are automated for you.
	 */
	private WaitingRoomServiceAsync waitingRoomService = GameServices.waitingroomService;
			
	/**
	 * This is the entry point method.
	 * 
	 * It puts all of the stuff in the window, but the waiting room functionality
	 * is not actually implemented here, just presentation stuff. See 
	 * WaitingRoomPanel for the real stuff.
	 */
	public void onModuleLoad() {
		final Button flushButton = new Button("Toggle Flush");
		final Label roomLabel = new Label();
		final Label statusLabel = new Label();
		//final String myID = "ME::"+ (Random.nextInt())+ ":" + System.currentTimeMillis();
		final TextArea params = new TextArea();
		params.setText(WaitingRoomPanel.printableParameters());
		Map<String, List<String>> paramMap = Window.Location.getParameterMap();
		final boolean  adminMode = paramMap.containsKey("adminMode") ? Boolean.parseBoolean(paramMap.get("adminMode").get(0)) : false;
		final String hitID = paramMap.containsKey("hitID") ? paramMap.get("hitID").get(0).trim() : null;
		final String assignmentID = paramMap.containsKey("assignmentID") ? paramMap.get("assignmentID").get(0).trim() : null;
		final String geogameKey = paramMap.containsKey("geogameKey") ? paramMap.get("geogameKey").get(0).trim() : null;
		final boolean validAMTLoginAttempt = (hitID != null) && (assignmentID != null) && (geogameKey != null); 
		params.setText(params.getText() + "... and valid AMT login attempt says:" + validAMTLoginAttempt);
		
		final String myID = validAMTLoginAttempt? geogameKey : "ME::"+ (Random.nextInt())+ ":" + System.currentTimeMillis();
		
		final WaitingRoomPanel room = new WaitingRoomPanel(myID, 
					"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer nec odio. Praesent libero. Sed cursus ante dapibus diam. Sed nisi. Nulla quis sem at nibh elementum imperdiet. Duis sagittis ipsum. Praesent mauris. Fusce nec tellus sed augue semper porta. Mauris massa. Vestibulum lacinia arcu eget nulla.\n Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Curabitur sodales ligula in libero. Sed dignissim lacinia nunc. Curabitur tortor. Pellentesque nibh. Aenean quam. In scelerisque sem at dolor. Maecenas mattis. Sed convallis tristique sem. Proin ut ligula vel nunc egestas porttitor. Morbi lectus risus, iaculis vel, suscipit quis, luctus non, massa. \nFusce ac turpis quis ligula lacinia aliquet. Mauris ipsum. Nulla metus metus, ullamcorper vel, tincidunt sed, euismod in, nibh. Quisque volutpat condimentum velit. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Nam nec ante. Sed lacinia, urna non tincidunt mattis, tortor neque adipiscing diam, a cursus ipsum ante quis turpis. Nulla facilisi. Ut fringilla. Suspendisse potenti. Nunc feugiat mi a tellus consequat imperdiet. Vestibulum sapien. Proin quam. Etiam ultrices. \nSuspendisse in justo eu magna luctus suscipit. Sed lectus. Integer euismod lacus luctus magna. Quisque cursus, metus vitae pharetra auctor, sem massa mattis sem, at interdum magna augue eget diam. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Morbi lacinia molestie dui. Praesent blandit dolor. Sed non quam. In vel mi sit amet augue congue elementum. Morbi in ipsum sit amet pede facilisis laoreet. Donec lacus nunc, viverra nec, blandit vel, egestas et, augue. Vestibulum tincidunt malesuada tellus. Ut ultrices ultrices enim. Curabitur sit amet mauris. Morbi in dui quis est pulvinar ullamcorper. \nNulla facilisi. Integer lacinia sollicitudin massa. Cras metus. Sed aliquet risus a tortor. Integer id quam. Morbi mi. Quisque nisl felis, venenatis tristique, dignissim in, ultrices sit amet, augue. Proin sodales libero eget ante. Nulla quam. Aenean laoreet. Vestibulum nisi lectus, commodo ac, facilisis ac, ultricies eu, pede.", 
					GWT.getHostPageBaseURL(),//"http://acs.ist.psu.edu",
					WaitingRoomPanel.digitSpanGamePanel(false, null),   
					waitingRoomService,
					adminMode);
		
		
		
		room.setWidth("75%");
		RootPanel.get("errorLabelContainer").add(roomLabel);
		RootPanel.get("errorLabelContainer").add(statusLabel);
		if(adminMode){
			RootPanel.get("sendButtonContainer").add(flushButton);
			RootPanel.get("sendButtonContainer").add(params);
		}
		
		
		
		flushButton.addClickHandler(new ClickHandler(){
			Integer roomsize = null;
			@Override
			public void onClick(ClickEvent event) {
				if(roomsize == null){
					waitingRoomService.roomMaxCount(new AsyncCallback(){
						@Override
						public void onFailure(Throwable caught) {
							GWT.log("error, could not get roomMax", caught);
							flushButton.setText("ERROR");
						}
						@Override
						public void onSuccess(Object result) {
							Integer i = (Integer)result;
							roomsize = i;
							waitingRoomService.setRoomMax(0, new AsyncCallback(){

								@Override
								public void onFailure(Throwable caught) {
									GWT.log("error, could not set roomMax to 0", caught);
									flushButton.setText("ERROR");
								}

								@Override
								public void onSuccess(Object result) {
									GWT.log("Admin toggled FLUSH to on.");
								}
							});
								
						
				}
										
				});
			}else{
				waitingRoomService.setRoomMax(roomsize, new AsyncCallback(){
					@Override
					public void onFailure(Throwable caught) {
						GWT.log("error, could not set roomMax to 0", caught);
						flushButton.setText("ERROR");
					}
					@Override
					public void onSuccess(Object result) {
						GWT.log("Admin toggled FLUSH to off.");
						roomsize = null;
					}
				});
			}
		}
	});
			
		
		
		
		RootPanel.get("nameFieldContainer").add(room);
		
		if(validAMTLoginAttempt){
			WaitingRoomPanel.makeAMTUserAccount(hitID, assignmentID, geogameKey, waitingRoomService);
			//if(Cookies.getCookie("username"))
		}
		
	}
}
