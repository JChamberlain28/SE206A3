package wikiSpeakGUI;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class GetImagesTask extends Task<Void>{
	
	private String _wikitTerm;
	private Button _submitImageSelect;
	private String _noOfImages;
	private TableView<CellImage> _imageView;
	private String _tempDir;
	private TableColumn<CellImage, ImageView> _colForUpdate;
	private List<String> _imageNameList = null;

	
	
	public GetImagesTask(String wikitTerm, Button submitImageSelect, String noOfImages, TableView<CellImage> imageView, TableColumn<CellImage, ImageView> colForUpdate, String tempDir) {
		_wikitTerm = wikitTerm;
		_submitImageSelect = submitImageSelect;
		_noOfImages = noOfImages;
		_imageView = imageView;
		_tempDir = tempDir;
		_colForUpdate = colForUpdate;
		
	}

	@Override
	protected Void call() throws Exception {
		CommandFactory command = new CommandFactory();
		command.sendCommand("rm ./" + _tempDir + "/*.jpg", false);
		
		// will need loading animation for this ####
		List<String> list = command.sendCommand("./downloadImages.sh \"" + _wikitTerm + "\" " + _noOfImages + " " + _tempDir, false);
		System.out.println(list.get(1));
		
		
		// create list of images to update TableView with
		List<String> imageNames = command.sendCommand("ls " + _tempDir + " | grep .*\\.jpg", true);
		_imageNameList = new ArrayList<String>(Arrays.asList(imageNames.get(0).split("\n")));

		return null;
	}
	
	// instruct JavaFX GUI thread to populate TableView with images
	@Override
	protected void done() {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				ObservableList<CellImage> imageItems = FXCollections.observableArrayList();
				
				for(String imageName : _imageNameList) {
					
					// empty file to get directory of javaFX application
					File file = new File("");
					
					Image ImageObject = new Image((file.toURI().toString()) + _tempDir + "/" + imageName);
					ImageView imageView = new ImageView(ImageObject);
					imageView.setFitHeight(180);
					imageView.setFitWidth(180);
					
					// instantiates custom image class used for setting TableView cell value type
					CellImage cell = new CellImage(imageView);
					imageItems.addAll(cell);

					
				}
				
				_colForUpdate.setCellValueFactory(new PropertyValueFactory<CellImage, ImageView>("image"));
				_imageView.setItems(imageItems);

				
			}
			
		});
	}

}
