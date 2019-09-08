package wikiSpeakGUI;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.FadeTransition;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.media.Media;
import javafx.scene.media.MediaMarkerEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.media.MediaPlayer.Status;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PlayController {
	private static String _filePath ="";
	File fileUrl = new File(_filePath).getAbsoluteFile(); 
	@FXML private Button playPauseB;
	@FXML private Button forwardB;
	@FXML private Button backwardsB;
	@FXML private MediaView mv;
	@FXML private Button backToMenu;
	private MediaPlayer mp;
	private Media media;
	
	public void initialize(){
		playPauseB.setMinWidth(40);
		start();
	}
	
	public void start(){
		media= new Media(fileUrl.toURI().toString());
		mp= new MediaPlayer(media);
		mv.setMediaPlayer(mp);
		mp.setAutoPlay(true);
		
		
		
		// return media to start once played
		mp.setOnEndOfMedia(new Runnable() {
		    @Override
		    public void run() {
		    	mp.seek(Duration.ZERO);
				playPauseB.setText("▶");
				mp.pause();
		    }
		});
	}
	
	@FXML
	private void pausePress(ActionEvent event) {
		if (mp.getStatus() == Status.PLAYING) {
			playPauseB.setText("▶");
			mp.pause();
		} else {
			playPauseB.setText("❚❚");
			mp.play();
		}
	}
	
	@FXML
	private void forwardPress(ActionEvent event) {
		mp.seek( mp.getCurrentTime().add( Duration.seconds(3)) );
	}
	
	@FXML
	private void backwardsPress(ActionEvent event) {
		mp.seek( mp.getCurrentTime().add( Duration.seconds(-3)) );
	}
	
	// pass the selected creation from the AppGUIController to the PlayController
	public void passInfo(String nameOfCreation) {
		_filePath = "creations/"+ nameOfCreation + ".mp4"; 

	}
	
	
	@FXML
	// Changes scene to main scene
	private void handleBackToMainView(ActionEvent event) throws IOException { // handle io exception?
		

		mp.stop();
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("AppGUI.fxml"));
		Parent layout = loader.load();
		
		Scene scene = new Scene(layout);

		// get the stage
		Stage primaryStage = (Stage) (((Node) event.getSource()).getScene().getWindow());
		primaryStage.setScene(scene);
	}
	
}
