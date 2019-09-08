package wikiSpeakGUI;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.FadeTransition;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.media.Media;
import javafx.scene.media.MediaMarkerEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;

public class MediaPlayerController{
	private String filePath ="big_buck_bunny_1_minute.mp4";
	File fileUrl = new File(filePath).getAbsoluteFile(); 
	@FXML private Button playPauseB;
	@FXML private Button forwardB;
	@FXML private Button backwardsB;
	@FXML private MediaView mv;
	private MediaPlayer mp;
	private Media media;
	
	public void initialize(){
		start();
	}
	
	public void start(){
		media= new Media(fileUrl.toURI().toString());
		mp= new MediaPlayer(media);
		mv.setMediaPlayer(mp);
		mp.setAutoPlay(true);
		
		mp.setOnReady(new Runnable() {
			public void run() {
				ObservableMap<String, Duration> markers = media.getMarkers();
				markers.put("Start", Duration.ZERO);
				markers.put("25%", media.getDuration().multiply(0.25)); 
				markers.put("Half", media.getDuration().multiply(0.5)); 
				}
			});
		
		mp.setOnMarker(new EventHandler<MediaMarkerEvent>() {
			@Override
			public void handle(MediaMarkerEvent event) {
				System.out.println(event.getMarker().getKey());
			}
		});
	}
	
	@FXML
	private void pausePress(ActionEvent event) {
		if (mp.getStatus() == Status.PLAYING) {
			mp.pause();
		} else {
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
	
}
