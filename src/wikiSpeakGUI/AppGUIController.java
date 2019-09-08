package wikiSpeakGUI;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class AppGUIController {





	// create section widgets
	@FXML
	private Button continueButton;

	@FXML
	private Button wikitButton;

	@FXML
	private TextField wikitInput;

	@FXML
	private TextArea wikitResult;


	@FXML
	private ListView<String> creationList;
	
	
	@FXML
	private Button playButton;
	
	
	@FXML
	private Button deleteButton;


	
	
	@FXML
	private Text creationNoText;
	
	


	@FXML
	private void initialize() {
		wikitButton.setDisable(false);
		continueButton.setDisable(true);
		wikitResult.setWrapText(true);
		wikitResult.setEditable(false);
		wikitResult.setStyle("-fx-control-inner-background: rgb(049,055,060); "
				+ "-fx-text-fill: rgb(255,255,255); -fx-focus-color: rgb(255,255,255);");
		wikitInput.setStyle("-fx-control-inner-background: rgb(049,055,060);"
				+ " -fx-text-fill: rgb(255,255,255); -fx-focus-color: rgb(255,255,255);");
		creationList.setStyle("-fx-control-inner-background: rgb(049,055,060); "
				+ "-fx-text-fill: rgb(255,255,255); -fx-focus-color: rgb(255,255,255);");
		updateAllLists();

		// block characters that are not accepted by wikit command
		wikitInput.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("[^\\\\&;\'\"]*")) {
					wikitInput.setText(newValue.replaceAll("[\\\\&;\'\"]", ""));
				}
			}
		});

	}
	
	

	
	
	// event handling
	
	
	
	// sets ListViews to default selection when triggered (first item)
	@FXML
	private void creationListDefaultSelect(Event event) {
		creationList.getSelectionModel().select(0);

	}


	

	
	// Changes scene to create scene
	@FXML
	private void handleContinueButton(ActionEvent event) throws IOException, InterruptedException { // handle io exception ####

		// get command object for sending bash commands
		CommandFactory command = new CommandFactory();

		
		
		
		// make temp directory
		List<String> mktempResult = null;
		List<String> numberedDescriptionOutput = null;

		try {
			mktempResult = command.sendCommand("mktemp -d TempCreation-XXXXX", false);


		}
		catch (Exception e){  // check exception later e.g. read only directory
			e.printStackTrace();
		}

		String tempFolder = mktempResult.get(0);

		
		// process description so each sentence is on a new line.
		try {
			command.sendCommand("cat .description.txt " + " | sed 's/\\([.!?]\\) \\([[:upper:]]\\)/\\1\\n\\2/g' > " + String.format("%s/description.txt ", tempFolder), false);
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		// retrieve description with line numbers.
		// send command 2nd parameter determines if each array item (sentence) should be separated by a new line
		try {
			numberedDescriptionOutput = command.sendCommand("cat -n " +  String.format("%s/description.txt ", tempFolder), true);
			
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		
		// generate and switch scene to create scene
		FXMLLoader loaderCreate = new FXMLLoader();
		loaderCreate.setLocation(getClass().getResource("CreateGUI.fxml"));
		Parent layoutCreate = loaderCreate.load();
		CreateController createController = loaderCreate.getController();
		
		// pass numbered description to be displayed in create view
		String searchTerm = wikitInput.getText();
		createController.passInfo(numberedDescriptionOutput.get(0), tempFolder, searchTerm);
		createController.updateCount();
		
		

		Scene sceneCreate = new Scene(layoutCreate);

		// get the stage
		Stage primaryStage = (Stage) (((Node) event.getSource()).getScene().getWindow());
		primaryStage.setScene(sceneCreate);
		


	}
	
	
	
	
	
	
	
	
	@FXML
	private void handleWikiSearch(ActionEvent event) { 


		String searchTerm = wikitInput.getText();

		if( searchTerm.trim().length() == 0) {
			Alert popup = new Alert(AlertType.INFORMATION);
			popup.setTitle("Input Error");
			popup.setHeaderText("Please enter a search term");
			popup.show();
		}
		else {
			wikitButton.setDisable(true);
			Thread wikiSearchThread = new Thread(new WikitSearchTask(wikitButton, continueButton, searchTerm, wikitResult));
			wikiSearchThread.start();
		}

	}
	
	
	@FXML
	private void handlePlayButton(Event event) {
		
		// get selected creation name to play
		String selection = creationList.getSelectionModel().getSelectedItem(); 
		PlayController pc = new PlayController();
		pc.passInfo(selection);
		
		try {
			Parent ViewParent = FXMLLoader.load(getClass().getResource("PlayGUI.fxml"));
			Scene ViewScene = new Scene(ViewParent,310,270);
			
			Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
			window.setScene(ViewScene);
			window.show();
		} catch (IOException e) {
		}
		
		//Thread playCreationThread= new Thread(new PlayCreationTask(selection));
		//playCreationThread.start();

	}
	
	
	

	
	@FXML
	private void handleDeleteButton(Event event) throws IOException, InterruptedException{ 
		
		
		deleteButton.setDisable(true);
		playButton.setDisable(true);
		
		// get selected creation name to delete
		String selection = creationList.getSelectionModel().getSelectedItem();
		
		
		// deletion confirmation dialog
		Alert popup = new Alert(AlertType.CONFIRMATION);
		popup.setTitle("Delete Confirmation");
		popup.setHeaderText("Are you sure you want to delete " + selection);
		
		ButtonType buttonTypeYes = new ButtonType("Yes");
		ButtonType buttonTypeNo = new ButtonType("No", ButtonData.CANCEL_CLOSE);

		popup.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
				
		Optional<ButtonType> result = popup.showAndWait();
		if (result.get() == buttonTypeYes){
			CommandFactory deleteCommand = new CommandFactory();
			deleteCommand.sendCommand("rm \"creations/" + selection + ".mp4\"", false);
			updateAllLists();
		} else {
			deleteButton.setDisable(false);
			playButton.setDisable(false);
		}
		


	}
	
	
	
	

	
	
	

	
	// helper function to update all lists
	public void updateAllLists() {
		Thread updateCreationList = new Thread(new UpdateCreationListTask(creationList, deleteButton, playButton, creationNoText));
		updateCreationList.start();
	}

	






}
