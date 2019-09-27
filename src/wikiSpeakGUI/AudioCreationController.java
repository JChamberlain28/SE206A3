package wikiSpeakGUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Text;

public class AudioCreationController {


	private String _numberedText;
	private static String _tempDir;
	private String _lineNo;
	private String _wikitTerm;
	private static List<String> audioGenResult;
	private static ArrayList<String> audioSentences=new ArrayList<String>();
	private static ArrayList<String> savedAudio=new ArrayList<String>();
	private static String savedText;
	private SceneSwitcher ss = new SceneSwitcher();
	private int count=savedAudio.size();




	@FXML
	private Button cancelButton;

	@FXML
	private Button previewButton;

	@FXML
	private ComboBox<String> voiceSelect;

	@FXML
	private Button submitCreationButton;

	@FXML
	private TextArea numberedTextArea;

	//@FXML
	//private TextField noLines;


	//@FXML
	//private Text lineNoMessage;

	@FXML
	private Button speakButton;

	@FXML
	private Button addButton;

	@FXML
	private Button delButton;

	@FXML
	private Button upButton;

	@FXML
	private Button downButton;

	@FXML
	private ListView<String> selectedAudio;

	@FXML
	private void initialize() {
		numberedTextArea.setWrapText(true);
		numberedTextArea.setEditable(true);
		numberedTextArea.setStyle("-fx-control-inner-background: rgb(049,055,060); "
				+ "-fx-text-fill: rgb(255,255,255); -fx-focus-color: rgb(255,255,255);");
		//noLines.setStyle("-fx-control-inner-background: rgb(049,055,060); "
			//	+ "-fx-text-fill: rgb(255,255,255); -fx-focus-color: rgb(255,255,255);");

		voiceSelect.getItems().add("Default Voice");
		voiceSelect.getItems().add("Akl_NZ(male) Voice");
		voiceSelect.getItems().add("Akl_NZ(female) Voice");
		voiceSelect.getSelectionModel().selectFirst();
		numberedTextArea.setText(savedText);
		
		selectedAudio.getItems().clear();
		for(int i=0; i<savedAudio.size();i++) {
			selectedAudio.getItems().add(savedAudio.get(i));
		}
		
		savedAudio.clear();
		// disallow non-numeric characters
		/*
		 * noLines.textProperty().addListener(new ChangeListener<String>() {
		 * 
		 * @Override public void changed(ObservableValue<? extends String> observable,
		 * String oldValue, String newValue) { if (!newValue.matches("\\d*")) {
		 * noLines.setText(newValue.replaceAll("[^\\d]", "")); } } });
		 */


	}




	// Allows other AppGUIController to pass info to this controller
	// and display the text in the create scene.
	public void passInfo(String numberedText, String tempDir, String wikitTerm) {
		_numberedText = numberedText;
		_tempDir = tempDir;
		_wikitTerm = wikitTerm;
		numberedTextArea.setText(_numberedText);
		selectedAudio.getItems().clear();
		savedAudio.clear();
	}


	// updates text that describes line number field with correct line limit
	public void updateCount() throws IOException, InterruptedException {
		CommandFactory command = new CommandFactory();
		List<String> output = command.sendCommand("wc -l < " + _tempDir + "/description.txt", false);
		_lineNo = output.get(0);
		//lineNoMessage.setText("Number of lines to include (1 to " + _lineNo + ")");

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
		//boolean abort = false;

		//String lineNoSelect = noLines.getText();

		CommandFactory command = new CommandFactory();

		// error checking

		// checks user didn't leave line number selection field empty (prompts user if they have)
		/*
		 * if (lineNoSelect.isEmpty()) { Alert popup = new Alert(AlertType.INFORMATION);
		 * popup.setTitle("Empty Line Selection");
		 * popup.setHeaderText("Please enter a line selection (1 to " + _lineNo + ")");
		 * popup.show(); abort = true; } else { // checks line number selection is not
		 * outside the valid line range (prompts user if so) int lineNoSelectInt =
		 * Integer.parseInt(lineNoSelect); int lineNoLimitInt =
		 * Integer.parseInt(_lineNo);
		 * 
		 * if ((lineNoSelectInt > lineNoLimitInt) || (lineNoSelectInt < 1)) { Alert
		 * popup = new Alert(AlertType.INFORMATION);
		 * popup.setTitle("Invalid Line Selection");
		 * popup.setHeaderText("Please enter a line selection (1 to " + _lineNo + ")");
		 * popup.show(); abort = true; } }
		 */





		// start audio generation and switch to video customisation view
		//if (!abort) {

		//GenerateAudioTask task = new GenerateAudioTask(lineNoSelect, _tempDir);
		//Thread generateAudio = new Thread(task); 
		//generateAudio.start();
		if (selectedAudio.getItems().size()==0) {
			Alert popup = new Alert(AlertType.INFORMATION);
			popup.setTitle("No audio selected");
			popup.setHeaderText("Please add some audio to create an creation");
			popup.show();
		}else {
			String order = "";
			for(int i=0; i<selectedAudio.getItems().size();i++) {
				if (i==(selectedAudio.getItems().size() - 1)) {
					order = order +_tempDir +"/audio" + audioSentences.indexOf(selectedAudio.getItems().get(i)) + ".mp3";
					savedAudio.add(selectedAudio.getItems().get(i));
					break;
				}
				order = order + _tempDir + "/audio" + audioSentences.indexOf(selectedAudio.getItems().get(i)) + ".mp3 ";
				savedAudio.add(selectedAudio.getItems().get(i));
			}
			order = order.replace(" ", "|");
			String cmd = "ffmpeg -i \"concat:"+order+"\" -acodec copy "+ _tempDir +"/output.mp3";
			savedText=numberedTextArea.getText();

			new Thread(() -> {
				submitCreationButton.setDisable(true);
				try {
					command.sendCommand("rm " +_tempDir + "/output.mp3" , false);
					command.sendCommand(cmd , false);
					command.sendCommand("echo \""+savedText+"\" > "+_tempDir+"/description.txt" , false);
					audioGenResult = command.sendCommand("echo $(soxi -D "+_tempDir+"/output.mp3)" , false);
					submitCreationButton.setDisable(false);
					Platform.runLater(() -> {
						VideoCreationController videoCreationController = (VideoCreationController)ss.newScene("VideoCreationGUI.fxml", event);
						videoCreationController.passInfo(_wikitTerm, _tempDir, audioGenResult);
					});
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}).start();
		}



		// proceed with video generation upon audio generation completion ####
		// (temporary code until video generation moved to another controller) ####
		// gsin387 <- please make gui wait till audio generation complete before switching ####
		// can change this near end if need be ####
		/*
		 * task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
		 * 
		 * @Override public void handle(WorkerStateEvent t) { audioGenResult =
		 * task.getValue();
		 * 
		 * videoCreationController.passInfo(_wikitTerm, _tempDir, audioGenResult); } });
		 */


	}





	//}


	@FXML
	private void handleSpeakPress(ActionEvent event){ 
		String lol = numberedTextArea.getSelectedText();
		String[] words = lol.split(" ");
		if (numberedTextArea.getSelectedText().isEmpty()) {
			Alert popup = new Alert(AlertType.INFORMATION);
			popup.setTitle("Select a chuck to add");
			popup.setHeaderText("No selected text detected. please select text to add");
			popup.show();
		}else if (words.length > 40) {
			Alert popup = new Alert(AlertType.INFORMATION);
			popup.setTitle("Too many words selected");
			popup.setHeaderText("Please selected between 1-40 words");
			popup.show();
		}else {
			lol = lol.replace("\"", " ");
			String cmd = "echo \"" + lol + "\" > selectedText.txt";
			String voice = getVoice();
			CommandFactory command = new CommandFactory();
			new Thread(() -> {
				speakButton.setDisable(true);
				try {
					command.sendCommand(cmd , false);
					command.sendCommand("text2wave -o "+ _tempDir +"/speakAudio.wav selectedText.txt " + voice , false);
					command.sendCommand("aplay "+ _tempDir +"/speakAudio.wav" , false);
					command.sendCommand("rm " + _tempDir +"/speakAudio.wav" , false);
					command.sendCommand("rm selectedText.txt" , false);
					speakButton.setDisable(false);

				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}).start();
		}
	}

	@FXML
	private void handleAddPress(ActionEvent event){ 
		String lol = numberedTextArea.getSelectedText();
		String[] words = lol.split(" ");
		if (numberedTextArea.getSelectedText().isEmpty()) {
			Alert popup = new Alert(AlertType.INFORMATION);
			popup.setTitle("Select a chuck to add");
			popup.setHeaderText("No selected text detected. please select text to add");
			popup.show();
		} else if (words.length > 40) {
			Alert popup = new Alert(AlertType.INFORMATION);
			popup.setTitle("Too many words selected");
			popup.setHeaderText("Please selected between 1-40 words");
			popup.show();
		}else {
			while (selectedAudio.getItems().contains(lol)) {
				lol = lol + " ";
			}
			selectedAudio.getItems().add(lol);
			audioSentences.add(lol);
			String name = "audio" + count;
			count++;
			lol = lol.replace("\"", " ");
			String cmd = "echo \"" + lol + "\" > selectedText.txt";
			String voice = getVoice();
			CommandFactory command = new CommandFactory();
			new Thread(() -> {
				addButton.setDisable(true);
				try {
					command.sendCommand(cmd , false);
					command.sendCommand("text2wave -o "+ _tempDir +"/"+ name +".wav selectedText.txt " + voice , false);
					command.sendCommand("lame " + _tempDir +"/"+ name +".wav " + _tempDir +"/"+ name +".mp3" , false);
					command.sendCommand("rm selectedText.txt" , false);
					//command.sendCommand("rm "+ _tempDir +"/*.wav" , false);
					addButton.setDisable(false);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}).start();
		}
	}

	@FXML
	private void handleDelPress(ActionEvent event){ 
		String lol=selectedAudio.getSelectionModel().getSelectedItem();
		selectedAudio.getItems().remove(lol);
	}

	@FXML
	private void upPress(ActionEvent event){ 
		int order = selectedAudio.getSelectionModel().getSelectedIndex();
		if (order <= 0) {

		}else {
			String temp = selectedAudio.getItems().get(order - 1);
			selectedAudio.getItems().set(order - 1, selectedAudio.getSelectionModel().getSelectedItem());
			selectedAudio.getItems().set(order, temp);
		}

	}

	@FXML
	private void downPress(ActionEvent event){ 
		int order = selectedAudio.getSelectionModel().getSelectedIndex();
		if (order + 1 >= selectedAudio.getItems().size()) {

		}else {
			String temp = selectedAudio.getItems().get(order + 1);
			selectedAudio.getItems().set(order + 1, selectedAudio.getSelectionModel().getSelectedItem());
			selectedAudio.getItems().set(order, temp);
		}
	}

	@FXML
	private void previewPress(ActionEvent event){ 
		CommandFactory command = new CommandFactory();
		new Thread(() -> {
			previewButton.setDisable(true);
			speakButton.setDisable(true);
			addButton.setDisable(true);
			for(int i=0; i<selectedAudio.getItems().size();i++) {
				String cmd = "aplay "+ _tempDir + "/audio" + audioSentences.indexOf(selectedAudio.getItems().get(i)) + ".wav";
				try {
					command.sendCommand(cmd , false);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
			previewButton.setDisable(false);
			speakButton.setDisable(false);
			addButton.setDisable(false);
		}).start();;
	}

	private String getVoice() {
		String selected = voiceSelect.getSelectionModel().getSelectedItem();
		if (selected.equals("Default Voice")) {
			return "-eval \"(voice_kal_diphone)\"";
		}else if (selected.equals("Akl_NZ(male) Voice")) {
			return "-eval \"(voice_akl_nz_jdt_diphone)\"";
		}else {
			return "-eval \"(voice_akl_nz_cw_cg_cg)\"";
		}
	}
}
