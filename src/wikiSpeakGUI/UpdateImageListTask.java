package wikiSpeakGUI;

import javafx.concurrent.Task;
import javafx.scene.control.ListView;

public class UpdateImageListTask extends Task<Void> {
	


	private ListView _imageList;

	public UpdateImageListTask(ListView imageList) {
		_imageList = imageList;
	}

	@Override
	protected Void call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
