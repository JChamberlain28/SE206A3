package wikiSpeakGUI;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;

public class VideoCreationController {


	private String _wikitTerm;
	private String _tempDir;
	private List<String> _audioGenResult;
	private SceneSwitcher ss = new SceneSwitcher();



	@FXML
	private TextField nameInput;



	@FXML
	private void initialize() {

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


	}

	public void passInfo(String wikitTerm, String tempDir, List<String> audioGenResult) {
		_wikitTerm = wikitTerm;
		_tempDir = tempDir;
		_audioGenResult = audioGenResult;
	}






	@FXML
	private void handleSubmitCreation(ActionEvent event) throws IOException, InterruptedException {


		// abort flag cancels creation generation when set to true
		boolean abort = false;

		String name = nameInput.getText();






		CommandFactory command = new CommandFactory();

		List<String> nameCheckResult = command.sendCommand("./nameCheck.sh \"" + name + "\"", false);


		// error checking

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


			AppGUIController appGUIController = (AppGUIController)ss.newScene("AppGUI.fxml", event);

			Thread generateCreation= new Thread(new GenerateVideoTask(_audioGenResult, name, _tempDir, _wikitTerm, appGUIController));
			generateCreation.start();



		}
	}
}
