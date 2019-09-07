package wikiSpeakGUI;

import java.util.List;

import javafx.application.Platform;
import javafx.concurrent.Task;

public class GenerateCreationTask extends Task<Void>  {
	
	
	private String _creationName;
	private String _noOfLines;
	private String _tempDir;
	private String _wikitTerm;
	private AppGUIController _controllerForUpdate;

	public GenerateCreationTask(String noOfLines, String creationName, String tempDir, String wikitTerm, AppGUIController controllerForUpdate) {
		_noOfLines = noOfLines;
		_creationName = creationName;
		_tempDir = tempDir;
		_wikitTerm = wikitTerm;
		_controllerForUpdate = controllerForUpdate;
	}

	@Override
	protected Void call() throws Exception {
		CommandFactory generationScript = new CommandFactory();
			List<String> result = generationScript.sendCommand("./generateAudio.sh " + _noOfLines + " " + _tempDir, false);
			
			// audio generation script returns audio duration
			double audioTime = Double.parseDouble(result.get(0));
			audioTime = audioTime + 1; // make video 1 second longer than audio
			
			generationScript.sendCommand("./generateVid.sh \"" + _creationName + "\" " + _tempDir + " \"" + _wikitTerm + "\" " + audioTime, false);
		
		return null;
	}
	
	
	@Override
	protected void done() {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				_controllerForUpdate.updateAllLists();
				
			}
			
		});
	}

	
	
}
