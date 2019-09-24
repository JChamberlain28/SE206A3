package wikiSpeakGUI;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.CodeSource;

import javafx.application.Application;
import javafx.event.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.*;


public class AppGUI extends Application{


	static String _jarDir;


	public static void main(String[] args) {

		// get directory of jar
		CodeSource codeSource = AppGUI.class.getProtectionDomain().getCodeSource();
		File runnableJar = null;

		try {
			runnableJar = new File(codeSource.getLocation().toURI().getPath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		_jarDir = runnableJar.getParentFile().getPath();
		
		CommandFactory command = new CommandFactory();
		
		// makes bash scripts executable (in case their permissions are incorrect)
		try {
			command.sendCommand("chmod +x *.sh", false);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}




		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		CommandFactory command = new CommandFactory();
		command.sendCommand("mkdir -p creations" , false);

		primaryStage.setTitle("Wiki Speak");

		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("AppGUI.fxml"));
		Parent layout = loader.load();
		Scene scene = new Scene(layout);
		primaryStage.setScene(scene);
		primaryStage.show();

		// remove .description.txt upon closing application to prevent left over files
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent e) {
				try {
					command.sendCommand("rm -f .description.txt", false);
				} catch (IOException | InterruptedException e1) {

					e1.printStackTrace();
				}
			}
		});


	}




}






























