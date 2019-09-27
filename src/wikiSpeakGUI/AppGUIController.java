package wikiSpeakGUI;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class AppGUIController {


	SceneSwitcher ss = new SceneSwitcher();

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
		updateCreationList();

		// block characters that are not accepted by wikit command
		wikitInput.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("[^\\\\&;\'\"]*")) {
					wikitInput.setText(newValue.replaceAll("[\\\\&;\'\"]", ""));
				}
			}
		});



		// Custom cell factory so placeholder creations (where they are still generating)
		// are unable to be selected to play / delete
		creationList.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
			@Override 
			public ListCell<String> call(ListView<String> param) {
				return new ListCell<String>() {
					@Override 
					protected void updateItem(String string, boolean empty) {
						super.updateItem(string, empty);
						
						if ((string != null) && (string.contains(" (Unavailable)"))) {
							setDisable(true);
						} else {
							setDisable(false);
						}
						setText(string);
					}

				};
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



		// switch scene to create view (casting to create controller as type of object known)
		AudioCreationController createController = (AudioCreationController)ss.newScene("AudioCreationGUI.fxml", event);


		// pass numbered description to be displayed in create view
		String searchTerm = wikitInput.getText();
		createController.passInfo(numberedDescriptionOutput.get(0), tempFolder, searchTerm);
		createController.updateCount();




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

		// selection field is static in PlayController
		PlayController pc = new PlayController();
		pc.passInfo(selection);


		// switch to play scene
		ss.newScene("PlayGUI.fxml",event);

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
			updateCreationList();
		} else {
			deleteButton.setDisable(false);
			playButton.setDisable(false);
		}



	}










	// helper function to update all lists
	public void updateCreationList() {
		Thread updateCreationList = new Thread(new UpdateCreationListTask(creationList, deleteButton, playButton, creationNoText, VideoCreationController.getCurrentlyGenerating()));
		updateCreationList.start();
	}








}
