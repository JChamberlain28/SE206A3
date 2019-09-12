package wikiSpeakGUI;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.text.Text;

public class CreateController {


	private String _numberedText;
	private String _tempDir;
	private String _lineNo;
	private String _wikitTerm;
	private static List<String> audioGenResult;
	private SceneSwitcher ss = new SceneSwitcher();




	@FXML
	private Button cancelButton;

	@FXML
	private Button submitCreationButton;

	@FXML
	private TextArea numberedTextArea;

	@FXML
	private TextField noLines;

	@FXML
	private TextField nameInput;

	@FXML
	private Text lineNoMessage;



	@FXML
	private void initialize() {
		numberedTextArea.setWrapText(true);
		numberedTextArea.setEditable(false);
		numberedTextArea.setStyle("-fx-control-inner-background: rgb(049,055,060); "
				+ "-fx-text-fill: rgb(255,255,255); -fx-focus-color: rgb(255,255,255);");
		noLines.setStyle("-fx-control-inner-background: rgb(049,055,060); "
				+ "-fx-text-fill: rgb(255,255,255); -fx-focus-color: rgb(255,255,255);");
		nameInput.setStyle("-fx-control-inner-background: rgb(049,055,060); "
				+ "-fx-text-fill: rgb(255,255,255); -fx-focus-color: rgb(255,255,255);");
		
		
		// removes characters that cause hidden file creation 
		nameInput.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("[^\\\\./$&:;]*")) {
					nameInput.setText(newValue.replaceAll("[\\\\./$&:;]", ""));
				}
			}
		});
		
		
		// disallow non-numeric characters
		noLines.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					noLines.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});


	}




	// Allows other AppGUIController to pass info to this controller
	// and display the text in the create scene.
	public void passInfo(String numberedText, String tempDir, String wikitTerm) {
		_numberedText = numberedText;
		_tempDir = tempDir;
		_wikitTerm = wikitTerm;
		numberedTextArea.setText(_numberedText);


	}

	
	// updates text that describes line number field with correct line limit
	public void updateCount() throws IOException, InterruptedException {
		CommandFactory command = new CommandFactory();
		List<String> output = command.sendCommand("wc -l < " + _tempDir + "/description.txt", false);
		_lineNo = output.get(0);
		lineNoMessage.setText("Number of lines to include (1 to " + _lineNo + ")");

	}



	@FXML
	// Changes scene to main scene
	private void handleBackToMainView(ActionEvent event) throws IOException { // handle io exception?
		
		// removes temp directory to prevent left over files
		Thread delDir = new Thread(new RemoveDirTask(_tempDir));
		delDir.start();
		
		
		ss.newScene("AppGUI.fxml", event);
	}

	
	
	@FXML
	private void handleSubmitCreation(ActionEvent event) throws IOException, InterruptedException {

		
		// abort flag cancels creation generation when set to true
		boolean abort = false;

		String lineNoSelect = noLines.getText();
		String name = nameInput.getText();
		
		

		


		CommandFactory command = new CommandFactory();
		
		List<String> nameCheckResult = command.sendCommand("./nameCheck.sh \"" + name + "\"", false);
		

		// error checking
		
		// checks user didn't leave line number selection field empty (prompts user if they have)
		if (lineNoSelect.isEmpty()) {
			Alert popup = new Alert(AlertType.INFORMATION);
			popup.setTitle("Empty Line Selection");
			popup.setHeaderText("Please enter a line selection (1 to " + _lineNo + ")");
			popup.show();
			abort = true;
		}
		else {
			// checks line number selection is not outside the valid line range (prompts user if so)
			int lineNoSelectInt = Integer.parseInt(lineNoSelect);
			int lineNoLimitInt = Integer.parseInt(_lineNo);
			
			if ((lineNoSelectInt > lineNoLimitInt) || (lineNoSelectInt < 1)) {
				Alert popup = new Alert(AlertType.INFORMATION);
				popup.setTitle("Invalid Line Selection");
				popup.setHeaderText("Please enter a line selection (1 to " + _lineNo + ")");
				popup.show();
				abort = true;
			}	
		}
		
		
		// checks supplied creation name has valid file name (informs user if invalid)
		// (as some characters are blocked, only case this triggers is no name or white space only)
		if (nameCheckResult.get(0).equals("Invalid Name") || (name == null)) {
			abort = true;
			Alert popup = new Alert(AlertType.INFORMATION);
			popup.setTitle("Invalid Name");
			popup.setHeaderText("The name \"" + name + "\" is invalid");
			popup.showAndWait();
			abort = true;
		}	
		
		// Informs user if creation with same name exists
		else if (nameCheckResult.get(0).equals("Exists")) {
			Alert popup = new Alert(AlertType.CONFIRMATION);
			popup.setTitle("Creation Exists");
			popup.setHeaderText("A creation with the name \"" + name + "\" aleardy exists");
			
			ButtonType buttonTypeYes = new ButtonType("Overwrite");
			ButtonType buttonTypeNo = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

			popup.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
					
			Optional<ButtonType> result = popup.showAndWait();
			
			// deletes file so the new creation can be made
			if (result.get() == buttonTypeYes){
				command.sendCommand("rm \"creations/" + name + ".mp4\"", false);
			} 
			else {
				abort = true;
			}
		}
		
		
		
		// start creation generation in the background and return to main app GUI
		if (!abort) {
			
			GenerateAudioTask task = new GenerateAudioTask(lineNoSelect, _tempDir);
			Thread generateAudio = new Thread(task);
			generateAudio.start();
			
			AppGUIController appGUIcontroller = (AppGUIController)ss.newScene("AppGUI.fxml", event);
			
			// proceed with video generation upon audio generation completion ####
			// (temporary code until video generation moved to another controller) ####
			task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			    @Override
			    public void handle(WorkerStateEvent t) {
			        audioGenResult = task.getValue();
			        
					Thread generateCreation= new Thread(new GenerateVideoTask(audioGenResult, name, _tempDir, _wikitTerm, appGUIcontroller));
					generateCreation.start();
			    }
			});
			

		}





	}






}
