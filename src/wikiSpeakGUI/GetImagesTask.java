package wikiSpeakGUI;

import java.util.List;

import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

public class GetImagesTask extends Task<Void>{
	
	private String _wikitTerm;
	private Button _submitImageSelect;
	private String _noOfImages;
	private ListView _imageView;
	private String _tempDir;

	
	
	public GetImagesTask(String wikitTerm, Button submitImageSelect, String noOfImages, ListView imageView, String tempDir) {
		_wikitTerm = wikitTerm;
		_submitImageSelect = submitImageSelect;
		_noOfImages = noOfImages;
		_imageView = imageView;
		_tempDir = tempDir;
		
	}

	@Override
	protected Void call() throws Exception {
		System.out.println("here");
		CommandFactory command = new CommandFactory();
		command.sendCommand("rm ./" + _tempDir + "/*.jpg", false);
		System.out.println(_wikitTerm);
		
		// will need loading animation for this ####
		List<String> list = command.sendCommand("./downloadImages.sh " + _wikitTerm + " " + _noOfImages + " " + _tempDir, false);
		System.out.println(list.get(0));
		return null;
	}

}
