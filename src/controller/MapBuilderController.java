package controller;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.imageio.ImageIO;

import app.MapManagerApp;
import data.mapdata.Map;
import data.mapdata.PresetTile;
import data.mapdata.Tile;
import gui.ErrorHandler;
import gui.NumericFieldListener;
import helpers.AssetManager;
import helpers.JSONManager;
import helpers.Logger;
import helpers.ScalingBounds.ScaleMode;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
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
	@FXML
	private Button btnChooseImage;
	@FXML
	private RadioButton rbFit;
	@FXML
	private RadioButton rbExtend;
	@FXML
	private RadioButton rbStretch;
	@FXML
	private ToggleGroup tgScaling;
	@FXML
	private CheckMenuItem chkboxViewGrid;
	
	ToolkitController tkController;
	ObjectSelectorController osController;

	private FileChooser imgChooser;
	
	@Override
	public void initialize() {
		super.initialize();
		try {
			try {
				AssetManager.initializeManager();
			} catch (Exception e) {
				ErrorHandler.handle("Asset manager could not be created.", e);
			}

			PresetTile.setupPresetTiles();
			JSONManager.initialize();
			setMap(Map.emptyMap(20, 20));
			
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
			tfWidth.setText(""+getMap().getWidth());
			tfHeight.setText(""+getMap().getHeight());
			tfWidth.textProperty().addListener(new NumericFieldListener(tfWidth, false));
			tfHeight.textProperty().addListener(new NumericFieldListener(tfHeight, true));
			
			//Background image stuff
			imgChooser = new FileChooser();
			//imgChooser.setTitle("Choose background image");
			//imgChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files (*.png, *.jpg)", "*.png", "*.jpg"));
			tgScaling.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
				if(oldVal==newVal) return;
				if(newVal==rbFit)
					getMap().setScaling(ScaleMode.FIT);
				else if(newVal==rbExtend)
					getMap().setScaling(ScaleMode.EXTEND);
				else
					getMap().setScaling(ScaleMode.STRETCH);
				calculateBackgroundBounds();
				redraw();
			});
			
			chkboxViewGrid.selectedProperty().addListener((obs, oldVal, newVal) -> setViewGrid(newVal));
			
		} catch (IOException e) {
			ErrorHandler.handle("Something went wrong...", e);
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
	void onBtnChooseImageClicked(ActionEvent e) {
		Logger.println("Opening file dialog");
		File f = imgChooser.showOpenDialog(SceneManager.getPrimaryStage());
		Logger.println("Selected file " + f);
		if(f == null) return;
		imgChooser.setInitialDirectory(new File(f.getParent()));
		try {
			Image img = SwingFXUtils.toFXImage(ImageIO.read(f), null);
			getMap().setBackground(img);
			calculateBackgroundBounds();
		} catch (IOException e1) {
			ErrorHandler.handle("Could not read image.", e1);
			e1.printStackTrace();
		}
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
		int width = getMap().getWidth();
		int height = getMap().getHeight();
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
				tiles[i-yStart][j-xStart] = getMap().getTile(j, i);

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
		getMap().setTiles(tiles);
		calculateBackgroundBounds();
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