package edu.cmu.cs.cimds.geogame.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.Command;
import edu.cmu.cs.cimds.geogame.client.exception.DuplicateUsernameException;
import edu.cmu.cs.cimds.geogame.client.GameInfo;
import edu.cmu.cs.cimds.geogame.client.services.GameServices;

/**
 * Client entry point for the LocationInformationWindow.
 *
 * @author Antonio Juarez <ajuarez@andrew.cmu.edu>
 */
public class NewUserWindow extends DialogBox {

	Grid widgetGrid = new Grid(5,2);
	
	Label usernameLabel = new Label("Username:");
	TextBox usernameTextBox = new TextBox();
	
	Label emailLabel = new Label("Email:");
	TextBox emailTextBox = new TextBox();
	
	Label passwordLabel = new Label("Password:");
	PasswordTextBox passwordTextBox = new PasswordTextBox();
	
	Label firstNameLabel = new Label("First Name:");
	TextBox firstNameTextBox = new TextBox();
	
	Label lastNameLabel = new Label("Last Name:");
	TextBox lastNameTextBox = new TextBox();
	
	public NewUserWindow() {
		super(false,true);
		this.init();
	}

	/**
	 * The entry point method, called automatically by loading a module
	 * that declares an implementing class as an entry-point
	 */
	
	public void init() {
		// Define widget elements
		VerticalPanel containerPanel = new VerticalPanel();
		
		widgetGrid.setWidget(0,0,usernameLabel);
		widgetGrid.setWidget(0,1,usernameTextBox);
		widgetGrid.setWidget(1,0,firstNameLabel);
		widgetGrid.setWidget(1,1,firstNameTextBox);
		widgetGrid.setWidget(2,0,lastNameLabel);
		widgetGrid.setWidget(2,1,lastNameTextBox);
		widgetGrid.setWidget(3,0,emailLabel);
		widgetGrid.setWidget(3,1,emailTextBox);
		widgetGrid.setWidget(4,0,passwordLabel);
		widgetGrid.setWidget(4,1,passwordTextBox);
		
		final Button submitButton = new Button("Create");
		
		passwordTextBox.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				// TODO Auto-generated method stub
				if(event.getCharCode()==KeyCodes.KEY_ENTER) {
					submitButton.click();
				}
			}
		});
		
		containerPanel.add(widgetGrid);
		
		
		submitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final String username = usernameTextBox.getText().trim();
				final String email = emailTextBox.getText().trim();
				final String password = passwordTextBox.getText().trim();
				final String firstName = firstNameTextBox.getText().trim();
				final String lastName = lastNameTextBox.getText().trim();
				
				if(username.isEmpty() || password.isEmpty()) {
					return;
				}
				
				GameServices.gameService.createUser(username, firstName, lastName, email, password, WindowInformation.AMTVisitor, new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						// check to see if we're catching a duplicate exception; if so, tell the user
						if (caught instanceof DuplicateUsernameException) {
							Window.alert("Failed to create user " + username + "! " + caught.getMessage());
						}
						//caught.printStackTrace();
						else {
							Window.alert("Failed to create user " + username + "! " + caught.getMessage());
						}
					}
					@Override
					public void onSuccess(Void result) {
						InfoWindow alert = new InfoWindow(200, 100, "Success!");
						alert.ensureDebugId("alert");
						alert.center();
						NewUserWindow.this.hide();
						WindowInformation.loginPanel.sendLogin(username, password);
					}
				});
			}
		});
		containerPanel.add(submitButton);
		
		Button cancelButton = new Button("Cancel");
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				NewUserWindow.this.hide();
			}
		});
		containerPanel.add(cancelButton);
		
		this.setWidget(containerPanel);
	}
	
	public void clearContent() {
		this.usernameTextBox.setText("");
		this.emailTextBox.setText("");
		this.passwordTextBox.setText("");
		this.firstNameTextBox.setText("");
		this.lastNameTextBox.setText("");
	}
	
	public static void createUser(final String username, final String email,
			final String password, final String firstName, final String lastName, 
			final boolean isAMT,
			final boolean verbose,
			final boolean promptForAcceptance){
		
		if(username.isEmpty() || password.isEmpty()) {
			return;
		}
		
		final long CONSENT_FORM = 1;
		final long GENERAL_INSTRUCTIONS = 2;
		final AcceptPanelWindow acceptPrompt = new AcceptPanelWindow();
		
		GameServices.gameService.createUser(username, firstName, lastName, email, password, isAMT, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				// check to see if we're catching a duplicate exception; if so, tell the user
				if (caught instanceof DuplicateUsernameException) {
					Window.alert("Failed to create user " + username + "! " + caught.getMessage());
				}
				//caught.printStackTrace();
				else {
					Window.alert("Failed to create user " + username + "! " + caught.getMessage());
				}
			}
			@Override
			public void onSuccess(Void result) {
				if(verbose){
					//InfoWindow alert = new InfoWindow(200, 100, "Success1!");
				    //alert.ensureDebugId("alert1");
				    //alert.center();
					Window.alert("User \"" + username + "\" Created!");
				}
				
				Command cmd;
				if(promptForAcceptance){
					//InfoWindow alert3 = new InfoWindow(200, 100, "Success3!");
				    //alert3.ensureDebugId("alert3");
				    //alert3.center();
					cmd = new Command(){
						@Override
						public void execute(){
							//reporting
							if(verbose)Window.alert("Login completed!");
							
							acceptPrompt.setFormId(CONSENT_FORM);
							acceptPrompt.getAcceptPanel().setHeight(512);
							acceptPrompt.getAcceptPanel().setWidth(724);
						
							acceptPrompt.setSuccessCommand(new Command(){
								@Override
								public void execute(){
									GameServices.gameService.sendFormAcceptance(GameInfo.getInstance().getPlayer(), 
											CONSENT_FORM, 
											true, 
											new AsyncCallback(){
												@Override
												public void onFailure(Throwable caught) {
													Window.alert("Form Acceptance Failure caught");
												}
												
												@Override 
												public void onSuccess(Object o){
													Window.alert("Form Acceptance Success caught");
												}
									});
								}
							});
							
							acceptPrompt.init();
							//instructionsDialog.showHideButton();
							acceptPrompt.center();
							acceptPrompt.show();
						}
					};
					
					
				}else{
					cmd = new Command(){
						@Override
						public void execute(){
							//noop
							return;
						}
					};
				}
				
				LoginPanel.sendLogin(username, password, cmd);
				//TODO: finish this callback to show the acceptance form, and put that in the db
				//acceptPrompt.getAcceptPanel.setCallback()
				//InfoWindow alert = new InfoWindow(200, 100, "Success2!");
			    //alert.ensureDebugId("alert2");
			    //alert.center();
				
			    
				
			}
		});
	}
	
}