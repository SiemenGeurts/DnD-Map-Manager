package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import app.MapManagerApp;
import data.mapdata.Map;
import helpers.JSONManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

public class MapBuilderController extends MapEditorController {

	private File currentFile = null;
	private FileChooser mapChooser;

	@FXML
	MenuBar menuBar;
	
	@FXML
	private AnchorPane toolkitPane;
	@FXML
	private AnchorPane selectorPane;
	
	ToolkitController tkController;
	ObjectSelectorController osController;

	@Override
	public void initialize() {
		super.initialize();
		try {
			JSONManager.initialize();
			currentMap = Map.emptyMap(20, 20);
			FXMLLoader loader = new FXMLLoader(MainMenuController.class.getResource("../assets/fxml/Toolkit.fxml"));
			Node root = new Scene(loader.load()).getRoot();
			toolkitPane.getChildren().add(root);
			tkController = loader.getController();
			AnchorPane.setTopAnchor(root, 0d);
			AnchorPane.setBottomAnchor(root, 0d);
			AnchorPane.setLeftAnchor(root, 0d);
			AnchorPane.setRightAnchor(root, 0d);
			
			loader = new FXMLLoader(MainMenuController.class.getResource("../assets/fxml/ObjectSelector.fxml"));
			root = loader.load();
			osController = loader.getController();
			osController.setController(this);
			selectorPane.getChildren().add(root);
			AnchorPane.setTopAnchor(root, 0d);
			AnchorPane.setBottomAnchor(root, 0d);
			AnchorPane.setLeftAnchor(root, 0d);
			AnchorPane.setRightAnchor(root, 0d);
			tkController.setSelector(osController);
			
			mapChooser = new FileChooser();

			List<FileChooser.ExtensionFilter> extensionFilters = mapChooser.getExtensionFilters();
			extensionFilters.add(new FileChooser.ExtensionFilter("Map files (*.map)", "*.map"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void endInit() {
		MapManagerApp.stage.setResizable(true);
		MapManagerApp.stage.setMaximized(true);
		drawBackground();
		drawMap();
	}
	
    @FXML
    void hoverTile(MouseEvent event) {
 
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
		} catch (IOException e) {
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