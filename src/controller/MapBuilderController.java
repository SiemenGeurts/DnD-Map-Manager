package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import data.mapdata.Map;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class MapBuilderController extends MapController {
	
	private File currentFile = null;
	private FileChooser mapChooser;
	
	@FXML
	MenuBar menuBar;
	
    @Override
	public void initialize() {
    	super.initialize();
		currentMap = Map.emptyMap(20, 20);
		
		FXMLLoader loader = new FXMLLoader(MainMenuController.class.getResource("../assets/fxml/Toolkit.fxml"));
        Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
        Scene scene = new Scene(root);
        Stage toolkit = new Stage();
        toolkit.setScene(scene);
        toolkit.initStyle(StageStyle.UNDECORATED);
        toolkit.show();
        menuBar.sceneProperty().addListener((observableScene, oldScene, newScene) -> { if (oldScene == null && newScene != null) {
        	newScene.windowProperty().addListener((observableWindow, oldWindow, newWindow) -> { if (oldWindow == null && newWindow != null) {
        		toolkit.setHeight(newWindow.getHeight());
        		newWindow.xProperty().addListener((obs, oldVal, newVal) -> toolkit.setX(newVal.doubleValue() + newWindow.getWidth()));
            	newWindow.yProperty().addListener((obs, oldVal, newVal) -> toolkit.setY(newVal.doubleValue()));
            	newWindow.focusedProperty().addListener((obs, oldVal, newVal) -> {
            		toolkit.setAlwaysOnTop(true);
                	toolkit.setAlwaysOnTop(false);
            	});
            	newWindow.setOnCloseRequest(new EventHandler<WindowEvent>() {
					@Override
					public void handle(WindowEvent event) {
						toolkit.close();
					}
            	});
        	}});
        }});
		mapChooser = new FileChooser();

		List<FileChooser.ExtensionFilter> extensionFilters = mapChooser.getExtensionFilters();
		extensionFilters.add(new FileChooser.ExtensionFilter("Map files (*.map)", "*.map"));
		drawBackground();
		drawMap(0, 0, 20, 20);
    }
    
    @FXML
	void saveMap() throws IOException {
		File file = currentFile;
		if (file == null) {
			mapChooser.setTitle("Save map");
			currentFile = file = mapChooser.showSaveDialog(SceneManager.getPrimaryStage());
		}
		if (file != null) {
			FileWriter writer = new FileWriter(file, false);
			writer.write(currentMap.encode());
			writer.close();
		}
	}
	
	@FXML
	void saveAsMap() {
		mapChooser.setTitle("Save map");
		File file = mapChooser.showSaveDialog(SceneManager.getPrimaryStage());
		try {
			if (file != null) {
				FileWriter writer = new FileWriter(file, false);
				writer.write(currentMap.encode());
				writer.close();
			}			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void loadMap() throws IOException {
		mapChooser.setTitle("Load map");
		File file = mapChooser.showOpenDialog(SceneManager.getPrimaryStage());
		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuilder sb = new StringBuilder();
		String line = br.readLine();
		while (line != null) {
			sb.append(line);
			line = br.readLine();
		}
		br.close();
		Map m = Map.decode(new String(sb));
		if (m != null)
			currentMap = m;
		drawBackground();
		drawMap(0, 0, currentMap.getWidth(), currentMap.getHeight());
	}
	
}
