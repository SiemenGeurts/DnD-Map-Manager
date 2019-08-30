package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import data.mapdata.Map;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;

public class MapBuilderController extends MapController {
	
	private File currentFile = null;
	private FileChooser mapChooser;
	
    @Override
	public void initialize() {
    	super.initialize();
		currentMap = Map.emptyMap(20, 20);
		
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
