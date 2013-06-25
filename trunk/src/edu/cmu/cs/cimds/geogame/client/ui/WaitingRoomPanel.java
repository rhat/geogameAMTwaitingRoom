package edu.cmu.cs.cimds.geogame.client.ui;//edu.psu.ist.acs.geogame.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;

//import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import edu.cmu.cs.cimds.geogame.client.services.*;

import edu.cmu.cs.cimds.geogame.client.exception.DBException;


public class WaitingRoomPanel extends Composite implements HasName, HasText{
	protected final DockPanel mainPanel = new DockPanel();
	protected final DeckPanel contentPanel = new DeckPanel();
	protected Panel gamePanel;
	protected Panel promptPanel;
	protected final Panel statusPanel = new VerticalPanel();
	protected final Label header = new Label();
	protected final Label status = new Label();
	protected final Label heartbeatStatus = new Label();
	protected String myID =  "ME::"+ (new Random()).nextInt()+ ":" + System.currentTimeMillis();
	protected String name = "waitingroom";
	protected String targetUrl = "http://www.google.com";
	protected String promptText = "You will now be redirected...";
	protected RoomCountPoller poller = null;
	protected int maxRoomCount = 5;
	protected boolean adminMode = false;
	protected Timer heartbeat = null;
	protected WaitingRoomServiceAsync waitingRoomService = null;
	private static final int periodMillis = 15*1000; //polling and heartbeat rate
	private static final int promptVisibiltyMillis = 60*1000;
	
	
	
	/**
	 * This will begin polling as soon as its constuctor is called, so always 
	 * connect before calling this. Also, gamePanel may be null, in which case
	 * the defaultGamePanel() result is used.
	 * 
	 */
	
	
	public WaitingRoomPanel(String userID, String prompt, final String targetUrl, 
			Panel gamePanel, WaitingRoomServiceAsync serviceProxy,
			boolean isAdmin){
		this.myID = userID;
		this.promptText = prompt;
		this.targetUrl = targetUrl;
		this.adminMode = isAdmin;
		this.waitingRoomService = serviceProxy;
		this.gamePanel = gamePanel != null? gamePanel : WaitingRoomPanel.defaultGamePanel();
		
		
		
		//construct the Prompt Panel
		promptPanel = new VerticalPanel();
		TextArea promptText = new TextArea();
		promptText.setText(this.promptText);
		promptText.setReadOnly(true);
		promptPanel.add(promptText);
		promptPanel.setWidth("75%");		
		
		
		//Add the decks
		contentPanel.add(this.gamePanel);
		contentPanel.add(promptPanel);
		contentPanel.showWidget(contentPanel.getWidgetIndex(this.gamePanel));
		
		//Add the status fields
		statusPanel.add(status);
		statusPanel.add(heartbeatStatus);
		
		
		
		if(adminMode){
			heartbeat = null; //do not count me as being in the room
			poller = new RoomCountPoller(waitingRoomService, null, null);
			poller.setUpdate(new Command(){
				@Override
				public void execute() {
					status.setText("There are " + poller.roomCount + " people in this room, with " + poller.maxRoomCount + " required.");
							
				}
			});
			poller.setComplete(new Command(){
				boolean done = false;
				@Override
				public void execute() { //never do redirection
					if(!done){
						status.setText(status.getText() + "... Admin Mode [Redirection Ignored]");
						done = true;
					}
				}
			});
		}else{
			heartbeatStatus.setVisible(false);
			status.setVisible(false);
			
			//make it the client version
			waitingRoomService.enter(myID, new AsyncCallback(){
			@Override
			public void onFailure(Throwable caught) {
				//GWT.log("failed to enter room",caught);
				heartbeatStatus.setText("ERROR ENTERING ROOM:" + caught.getMessage());
			}
			@Override
			public void onSuccess(Object result) {
				heartbeatStatus.setText("ENTERED ROOM");
			}
			
			});
			
			heartbeat = new Timer(){
				@Override
				public void run() {
					waitingRoomService.enter(myID, new AsyncCallback(){
						@Override
						public void onFailure(Throwable caught) {
							//GWT.log("Error on heartbeat", caught);
							heartbeatStatus.setText("ERROR ON HEARTBEAT:" + caught.getMessage());
						}
						@Override
						public void onSuccess(Object result) {
							heartbeatStatus.setText("Hearbeat Refreshed..." + System.currentTimeMillis());
						}
					});
				}
			};
			
			poller = new RoomCountPoller(waitingRoomService, null, null);
			poller.setUpdate(new Command(){
				@Override
				public void execute() {
					status.setText("There are " + poller.roomCount + " people in this room, with " + poller.maxRoomCount + " required.");
							
				}
			});
			poller.setComplete(new Command(){
				@Override
				public void execute() {
					if(!isPrompt()){
						togglePrompt();
						RedirectTimer t = new RedirectTimer(targetUrl);
						t.schedule(promptVisibiltyMillis);
						//RedirectTimer.redirect(targetUrl); //use a Prompt to do this
					}
				}
			});
			
		}
		
		mainPanel.add(header, DockPanel.NORTH);
		mainPanel.add(contentPanel, DockPanel.CENTER);
		mainPanel.add(statusPanel, DockPanel.SOUTH);
		
		if(heartbeat !=null) heartbeat.scheduleRepeating(periodMillis);
		if(poller != null) poller.scheduleRepeating(periodMillis);
		
		initWidget(mainPanel);
	}
	
	/**
	 * Use this when you don't know the current Max for the WaitingRoomService
	 * Works in adminMode too.
	 */
	
	public void toggleFlushWaitingRoom(){
		//Calling toggleFlushWaitingRoom without adminMode being enabled may make untoggling impossible
		waitingRoomService.roomMaxCount(new AsyncCallback(){
			@Override
			public void onFailure(Throwable caught) {
				//GWT.log("ERRROR: Could not poll max room count", caught);
			}

			@Override
			public void onSuccess(Object result) {
				toggleFlushWaitingRoom((Integer)result);
			}
			
		});
	}
	
	/**
	 * Use this when you know the current Max for the WaitingRoomService
	 * Works in adminMode too.
	 * @param currentMax
	 */
	
	public void toggleFlushWaitingRoom(Integer currentMax){
		int i;
		if(currentMax <= 0){
			i = this.maxRoomCount;
		}else{
			i = 0;
		}
		waitingRoomService.setRoomMax(i, new AsyncCallback(){

			@Override
			public void onFailure(Throwable caught) {
				//GWT.log("ERRROR: Could not set max room count", caught);
			}

			@Override
			public void onSuccess(Object result) {
				//Do nothing, for now				
			}
			
		});
	}
	
	public boolean isPrompt(){
		int promptindex = contentPanel.getWidgetIndex(promptPanel);
		int current = this.contentPanel.getVisibleWidget();
		return current == promptindex;
	}
	
	public void togglePrompt(){
		int current = this.contentPanel.getVisibleWidget();
		int promptindex = contentPanel.getWidgetIndex(promptPanel);
		int gameindex = contentPanel.getWidgetIndex(gamePanel);
		if(current != promptindex){
			contentPanel.showWidget(promptindex);
		}else{
			contentPanel.showWidget(gameindex);
		}
	}
	
	@Override
	public String getText() {
		return promptText;
	}

	@Override
	public void setText(String text) {
		promptText = text;
	}

	@Override
	public void setName(String name) {
		this.name = name;
		mainPanel.setStylePrimaryName(name);
	}

	@Override
	public String getName() {
		return name;
	}
	
	
	/**
	 * This is a simple implementation of the Number-guessing game.
	 * Use it if you just want something for the users to play with,
	 * but don't care about the results.
	 * @return A panel containing the game.
	 */
	public static Panel defaultGamePanel(){
		final int target = (new Random()).nextInt(100);
		final Panel res = new VerticalPanel();
		final Label prompt = new Label("Guess a number between 0 and 100");
		final Label feedback = new Label();
		final TextBox textBox = new TextBox();
		final Button guess = new Button("Guess");
		
		res.add(prompt);
		res.add(textBox);
		res.add(guess);
		res.add(feedback);
		
		guess.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				try{
					int userGuess = Integer.parseInt(textBox.getText());
					if(userGuess > target){
						feedback.setText("Too High...");
					}else if(userGuess < target){
						feedback.setText("Too Low...");
					}else{
						feedback.setText("Good Job, you win!");
					}
				}catch(NumberFormatException e){
					textBox.setText("0");
					feedback.setText("Enter a number next time");
				}
			}
			
		});
		
		return res;
	}
	
	protected static String printDigitList(List<Integer> x){
		StringBuilder s = new StringBuilder();
		for(Integer i: x){
			s.append(i);
			s.append(" ");
		}
		return s.toString();
	}
	
	protected static Double scoreDigitSpan(List<Integer> actual, List<Integer> submitted){
		int alen = actual.size(),
			slen = submitted.size();
		if(alen != slen) return 0d;
		int correctCounter = 0;
		for(int i = 0; i < alen; i++){
			if(actual.get(i).equals(submitted.get(i))){
				correctCounter++;
			}
		}
		return ((double)correctCounter) / ((double)alen);
	}
	
	protected static Double averageScore(List<Double> scores){
		Double sum = 0d;
		for(Double score: scores) sum += score;
		return sum/((double)scores.size());
	}
	
	
	
	/**
	 * This is an implementation of the Wechsler Adult Intelligence Scale
	 * Digit Span subtask. It aims to implement WAIS-IV compatibility. 
	 * 
	 * @param reverse				Do the reverse digit span task, if this is true.
	 * @param waitingRoomService	Nullable, use this service proxy to report results. If null, no reporting will occur.
	 * @return 	A panel implementing the whole game.
	 */
	public static Panel digitSpanGamePanel(final boolean reverse, final WaitingRoomServiceAsync waitingRoomService){
		final boolean doReporting = waitingRoomService != null;
		//this is dirty, but scoping rules require it.
		final HashMap<String, Object> localstate = new HashMap<String, Object>();
		localstate.put("currentTrial", 0);
		
		final int trials = 15;
		final int presentationMillis = 1*1000;
		final int[] acceptableSpanLengths = {4,5,6,7,8,9,10};
		final Random r = new Random();
		final ArrayList<ArrayList<Integer>> spans = new ArrayList<ArrayList<Integer>>(trials);
		for(int i = 0; i < trials; i++){
			int len = acceptableSpanLengths[r.nextInt(acceptableSpanLengths.length)];
			ArrayList<Integer> span = new ArrayList<Integer>(len);
			for(int j = 0; j < len; j++){
				span.add(r.nextInt(10));
			}
			spans.add(span);
		}
		
		
		final ArrayList<Double> scores = new ArrayList<Double>(trials);
		
		final DeckPanel deck = new DeckPanel();
		
		final DockPanel startPanel = new DockPanel();
		final Button startButton = new Button("Start");
		final Panel gameShow = new HorizontalPanel();
		final Panel gameEnter = new HorizontalPanel();
		deck.add(startPanel);
		deck.add(gameShow);
		deck.add(gameEnter);
		
		//these are lookup indicies
		final int startIndex = deck.getWidgetIndex(startPanel),
			       showIndex  = deck.getWidgetIndex(gameShow),
			       enterIndex = deck.getWidgetIndex(gameEnter);
		deck.showWidget(startIndex); //start here as a default
		
		//populate the ShowPanel
		final Label prompt = new Label("Memorize this number:");
		final Label numberLabel = new Label("");
		//make the label bold text
		numberLabel.getElement().getStyle().setFontStyle(com.google.gwt.dom.client.Style.FontStyle.OBLIQUE);
		gameShow.add(prompt);
		gameShow.add(numberLabel);
		gameShow.setWidth("75%");
		prompt.setWidth("150px");
		numberLabel.setWidth("200px");
			
		final Timer showTimer = new Timer(){
			@Override
			public void run() {
				((Command)localstate.get("fireenter")).execute();
			}					
		};
		
		Command fireshow = new Command(){
			@Override
			public void execute() {
				deck.showWidget(showIndex);
				if((Integer)localstate.get("currentTrial") >= trials){ 
					prompt.setText("DONE");
					double result = Math.round(averageScore(scores) * 100.0);
					numberLabel.setText("Your test is complete, you scored " + result + "%");
					return;
				}
				List<Integer> span = spans.get((Integer)localstate.get("currentTrial"));
				//if(reverse) Collections.reverse(span); //only do this for the checking phase
				numberLabel.setText(printDigitList(span));
				showTimer.schedule(presentationMillis);
			}			
		};
		localstate.put("fireshow", fireshow);
		
		Command fireenter = new Command(){
			@Override
			public void execute() {
				deck.showWidget(enterIndex);
				
			}
		};
		localstate.put("fireenter", fireenter);
		
		
		//Populate the startpanel
		startPanel.add(startButton, DockPanel.CENTER);
		startButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent arg0) {
				((Command)localstate.get("fireshow")).execute();
			}
		});
		
		
		
		
		
		final Label promptEnter = new Label("Enter the number:");
		final TextBox textBox = new TextBox();
		final Button guess = new Button("Submit");
		
		gameEnter.add(promptEnter);
		gameEnter.add(textBox);
		gameEnter.add(guess);
		textBox.setText("");
		textBox.setWidth("200px");
		gameEnter.setWidth("75%");
		
		guess.addClickHandler(new ClickHandler(){
			boolean completed = false;
						
			public void doChecking(){
				Window.alert("DoChecking");
				if(completed){
					double result = averageScore(scores);
					textBox.setText("Your test is complete, you scored " + result);
					guess.setEnabled(false);
					//in the future, report this to a service
					if(doReporting){
						//this is a STUB, replace it with real reporting.
					}
				}
			}
			
			@Override
			public void onClick(ClickEvent event) {
				int current = (Integer)localstate.get("currentTrial");
				if(current>=trials){
					completed = true;
					doChecking();
					return;
				}
				localstate.put("currentTrial", current + 1);
				
				promptEnter.setText("Trial " + current + " of " + trials );
				
				String raw =  textBox.getText();
				char[] chars = raw.toCharArray();
				List<Integer> digits = new ArrayList<Integer>(chars.length);
				for(int i=0; i<chars.length;i++){
					try{
						Integer n = Integer.parseInt(((Character)chars[i]).toString());
						digits.add(n);
					}catch(NumberFormatException e){
						continue;
					}
				}
				List<Integer> span = spans.get(current); 
				if(reverse) Collections.reverse(span);
				Double score = scoreDigitSpan(span, digits);
				scores.add(score);
				textBox.setText("");
				
				if(current < trials){
					((Command)localstate.get("fireshow")).execute();
				}else{
					completed = true;
					doChecking();
				}
				
			}				
		
			
		});
		
		
		return deck;
	}
	
	
	//Utility function
	public static String printableParameters(){ //http://127.0.0.1:8888/GeoGameAMT.html?gwt.codesvr=127.0.0.1:9997&thing=blah
		StringBuilder res = new StringBuilder();
		res.append("");
		Map<String, List<String>> paramMap = Window.Location.getParameterMap();
		for(String param: paramMap.keySet()){
			res.append(param + "=");
			List<String> list = paramMap.get(param);
			boolean multi = list.size() != 1;
			String vals = !multi ? "\"" + list.get(0) + "\"": "[";
			res.append(vals);
			if(multi){
				for(String val: list) res.append("\"" + val + "\" " );
				res.append("]");
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	public static void makeAMTUserAccount(final String hitID, final String assignmentID, 
			final String geogameKey, final WaitingRoomServiceAsync waitingRoomService){
		//TODO: this is a stub, flesh out to do the following
		//1) check to see if the AMTKey is in the DB and not expired
		//2) remove the key
		//3) make new login with user ID stuff
	
		try{
		    waitingRoomService.isKeyValid(geogameKey, true, new AsyncCallback(){
		    	@Override
			public void onFailure(Throwable caught) {
		    		//GWT.log("cannot get room occupancy count", caught);
		    		Window.alert("Could not check key!");
		    	}

		    	@Override
		    	public void onSuccess(Object result) {
		    		Window.alert("Checked key, now to make that account...");
		    		Boolean res = (Boolean)result;
		    		if(res){
		    			String username = geogameKey;
		    			String email = "noemail@example.com";
		    			String password = "password";
		    			String firstName = assignmentID;
		    			String lastName = hitID;
		    			NewUserWindow.createUser(username,email,
		    					password,firstName,lastName,
		    					true,  //isAMT?
		    					true,  //show popups
		    					true); //get acceptance form signed
		    			
		    		}
		    	}
		    });
		
		}catch(DBException e){
			Window.alert("DB Error: " + e.getMessage());
		}
		
		//final String username, final String email,
		//final String password, final String firstName, final String lastName, 
		//boolean isAMT,
		//boolean verbose){
		
		
	}
	
}
