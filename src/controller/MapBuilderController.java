package controller;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import app.MapManagerApp;
import data.mapdata.Entity;
import data.mapdata.Map;
import data.mapdata.PresetTile;
import data.mapdata.Tile;
import gui.NumericFieldListener;
import helpers.JSONManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class MapBuilderController extends MapEditorController {

	private File currentFile = null;
	private FileChooser mapChooser;

	@FXML
	MenuBar menuBar;
	
	@FXML
	private AnchorPane toolkitPane;
	@FXML
	private VBox vbox;
	
	//canvas settings
	@FXML
	private GridPane expandAnchorPane;
	private Button[][] anchorButtons;
	private ImageView[][] radialArrows;
	@FXML
	private TextField tfWidth, tfHeight;
	private Point anchor;
	
	ToolkitController tkController;
	ObjectSelectorController osController;

	@Override
	public void initialize() {
		super.initialize();
		try {
			JSONManager.initialize();
			currentMap = Map.emptyMap(20, 20);
			currentMap.setTile(0, 0, new Tile(PresetTile.WALL));
			currentMap.setTile(0, currentMap.getHeight()-1, new Tile(PresetTile.WALL));
			currentMap.setTile(currentMap.getWidth()-1, 0, new Tile(PresetTile.WALL));
			currentMap.setTile(currentMap.getWidth()-1, currentMap.getHeight()-1, new Tile(PresetTile.WALL));
			
			FXMLLoader loader = new FXMLLoader(MainMenuController.class.getResource("/assets/fxml/Toolkit.fxml"));
			Node root = new Scene(loader.load()).getRoot();
			toolkitPane.getChildren().add(root);
			AnchorPane.setBottomAnchor(root, 10d);
			AnchorPane.setTopAnchor(root, 10d);
			AnchorPane.setLeftAnchor(root, 10d);
			AnchorPane.setRightAnchor(root, 10d);
			
			tkController = loader.getController();
			
			loader = new FXMLLoader(MainMenuController.class.getResource("/assets/fxml/ObjectSelector.fxml"));
			root = loader.load();
			osController = loader.getController();
			osController.setController(this);
			vbox.getChildren().add(root);
			VBox.setVgrow(root, Priority.SOMETIMES);
			
			loader = new FXMLLoader(ServerController.class.getResource("/assets/fxml/PropertyEditor.fxml"));
			root = loader.load();
			setPropertyEditor(loader.getController());
			vbox.getChildren().add(root);
			VBox.setVgrow(root, Priority.SOMETIMES);
			
			tkController.setSelector(osController);
			
			mapChooser = new FileChooser();

			List<FileChooser.ExtensionFilter> extensionFilters = mapChooser.getExtensionFilters();
			extensionFilters.add(new FileChooser.ExtensionFilter("Map files (*.map)", "*.map"));
			anchorButtons = new Button[3][3];
			radialArrows = new ImageView[3][3];
			for(Node n : expandAnchorPane.getChildren()) {
				Button b = (Button) n;
				int row = GridPane.getRowIndex(n);
				int column = GridPane.getColumnIndex(n);
				b.setOnAction(new AnchorButtonClicked(row, column));
				anchorButtons[row][column] = b;
				radialArrows[row][column] = new ImageView(new Image("/assets/images/icons/arrow"+row+"-"+column+".png", 45, 45, true, true));
				radialArrows[row][column].setFitHeight(45);
				radialArrows[row][column].setFitWidth(45);
			}	
			anchor = new Point(1, 1);
			setSelectedAnchor(1, 1);
			tfWidth.setText(""+currentMap.getWidth());
			tfHeight.setText(""+currentMap.getHeight());
			tfWidth.textProperty().addListener(new NumericFieldListener(tfWidth, false));
			tfHeight.textProperty().addListener(new NumericFieldListener(tfHeight, true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void endInit() {
		MapManagerApp.stage.setResizable(true);
		MapManagerApp.stage.setMaximized(true);
		redraw();
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
		redraw();
	}
	
	public void setSelectedAnchor(int row, int col) {
		anchor.setLocation(col, row);
		row -= 1;
		col -= 1;
		for(int i = 0; i <= 2; i++) {
			for(int j = 0; j <= 2; j++) {
				if(i-row>=0 && i-row<=2 && j-col>= 0 && j-col<=2)
					anchorButtons[i][j].setGraphic(radialArrows[i-row][j-col]);
				else
					anchorButtons[i][j].setGraphic(null);
			}
		}
	}
	
	@FXML
	public void onRevertExpansion(ActionEvent e) {
		
	}
	
	@FXML
	public void onApplyExpansion(ActionEvent e) {
		int width = currentMap.getWidth();
		int height = currentMap.getHeight();
		int newWidth = Integer.parseInt(tfWidth.getText());
		int newHeight = Integer.parseInt(tfHeight.getText());
		if(newWidth<width || newHeight<height) {
			Alert alert = new Alert(AlertType.WARNING, "Are you sure you want to reduce the map side? This operation is not reversable.", ButtonType.YES, ButtonType.CANCEL);
			alert.setTitle("Confirm resize");
			Optional<ButtonType> result = alert.showAndWait();
			if(result.orElse(ButtonType.CANCEL) == ButtonType.CANCEL)
				return;
		}
		Tile[][] tiles = new Tile[newHeight][newWidth];
		int xdiff = newWidth-width;
		int ydiff = newHeight-height;
		int xStart=0, xEnd=width, yStart=0, yEnd=height;
		switch(anchor.x) {
		case 0:
			xStart = 0;
			xEnd = newWidth;
			break;
		case 1:
			xStart = -(int)Math.floorDiv(xdiff,2);
			xEnd = width+(int)Math.ceil(xdiff/2);
			break;
		case 2:
			xStart = -xdiff;
			xEnd = width;
			break;
		}
		switch(anchor.y) {
		case 0:
			yStart = 0;
			yEnd = newHeight;
			break;
		case 1:
			yStart = -(int)Math.floorDiv(ydiff,2);
			yEnd = height+(int)Math.ceil(ydiff/2);
			break;
		case 2:
			yStart = -ydiff;
			yEnd = height;
			break;
		}
		int x1 = xStart>0 ? xStart : 0;
		int x2 = xEnd > width ? width: xEnd;
		int y1 = yStart > 0 ? yStart : 0;
		int y2 = yEnd > height ? height : yEnd;
		
		for(int i = y1; i<y2; i++)
			for(int j = x1; j<x2; j++)
				tiles[i-yStart][j-xStart] = currentMap.getTile(j, i);

		if(newWidth>width) {
			xStart = -xStart;
			for(int x = 0; x<xStart; x++)
				for(int y = 0; y<newHeight; y++)
					tiles[y][x] = new Tile(PresetTile.EMPTY);
			for(int x = xStart+width; x<newWidth; x++)
				for(int y = 0; y<newHeight; y++)
					tiles[y][x] = new Tile(PresetTile.EMPTY);
		}
		if(newHeight>height) {
			yStart = -yStart;
			for(int y = 0; y < yStart; y++)
				for(int x = xStart; x < width+xStart; x++)
					tiles[y][x] = new Tile(PresetTile.EMPTY);
			for(int y = yStart+height; y < newHeight; y++)
				for(int x = xStart; x < width+xStart; x++)
					tiles[y][x] = new Tile(PresetTile.EMPTY);
		}
		
		Map map = new Map(tiles);
		for(Entity entity : currentMap.getEntities())
			map.addEntity(entity);
		currentMap = map;
		redraw();
	}
	
	class AnchorButtonClicked implements EventHandler<ActionEvent> {
		int row, col;
		public AnchorButtonClicked(int row, int col) {
			this.row = row;
			this.col = col;
		}
		
		@Override
		public void handle(ActionEvent event) {
			setSelectedAnchor(row, col);
		}
	}
}