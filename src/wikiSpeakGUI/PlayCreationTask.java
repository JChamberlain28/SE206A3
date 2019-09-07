package wikiSpeakGUI;



import javafx.concurrent.Task;

public class PlayCreationTask extends Task<Void> {
	
	

	private String _name;

	public PlayCreationTask(String creationName) {
		_name = creationName;
	}

	@Override
	protected Void call() throws Exception { // runs ffplay command for video
		CommandFactory playCreationCommand = new CommandFactory();
		playCreationCommand.sendCommand("ffplay -loglevel fatal -autoexit \"creations/" + _name + ".mp4\"", false);
		return null;
	}

}
