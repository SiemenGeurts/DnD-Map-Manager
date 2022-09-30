package controller;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

import javax.imageio.ImageIO;

import app.MapManagerApp;
import data.mapdata.Entity;
import data.mapdata.Map;
import data.mapdata.PresetTile;
import data.mapdata.Tile;
import gui.ErrorHandler;
import gui.NumericFieldListener;
import helpers.AssetManager;
import helpers.Utils;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

	private static Map.EditingKey key = null;
	
	@Override
	public void initialize() {
		super.initialize();
		try {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Library");
			alert.setHeaderText("Do you want to create a new library or open one?");
			alert.setContentText("You can also choose to open a map which already has a library associated with it.");
			ButtonType btnNew = new ButtonType("New library");
			ButtonType btnOpenLib = new ButtonType("Open library");
			ButtonType btnOpenMap = new ButtonType("Open map");
			alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			alert.getButtonTypes().setAll(btnNew, btnOpenLib, btnOpenMap);
			Optional<ButtonType> result = alert.showAndWait();
			if(result.get() == btnNew) {
				AssetManager.initializeManager(false);
				setMap(Map.emptyMap(20, 20));
			} else if(result.get() == btnOpenLib) {
				File libFile = AssetManager.initializeManager(true);
				setMap(Map.emptyMap(20, 20));
				if(libFile != null)
					getMap().setLibraryFile(libFile);
			} else if(result.get() == btnOpenMap) {
				try {
					onOpen();
					if(getMap() == null) {
						//treat this as a new map
						AssetManager.initializeManager(false);
						setMap(Map.emptyMap(20, 20));
					}
				} catch (IOException e) {
					ErrorHandler.handle("Couldn't open map.", e);
					AssetManager.initializeManager(false);
					setMap(Map.emptyMap(20, 20));
				}
			}

			PresetTile.setupPresetTiles();
			//JSONManager.initialize();
			 
			FXMLLoader loader = new FXMLLoader(MainMenuController.class.getResource("/assets/fxml/Toolkit.fxml"));
			Node root = new Scene(loader.load()).getRoot();
			toolkitPane.getChildren().add(root);
			AnchorPane.setBottomAnchor(root, 10d);
			AnchorPane.setTopAnchor(root, 10d);
			AnchorPane.setLeftAnchor(root, 10d);
			AnchorPane.setRightAnchor(root, 10d);
			
			tkController = loader.getController();
			
			loader = new FXMLLoader(MainMenuController.class.getResource("/assets/fxml/PaintPane.fxml"));
			root = loader.load();
			paintController = loader.getController();
			vbox.getChildren().add(root);
			VBox.setVgrow(root, Priority.NEVER);

			loader = new FXMLLoader(MainMenuController.class.getResource("/assets/fxml/ObjectSelector.fxml"));
			root = loader.load();
			osController = loader.getController();
			osController.setController(this);
			vbox.getChildren().add(root);
			VBox.setVgrow(root, Priority.SOMETIMES);
			
			
			loader = new FXMLLoader(MainMenuController.class.getResource("/assets/fxml/PropertyEditor.fxml"));
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
			tfWidth.setText(""+getCurrentLevel().getWidth());
			tfHeight.setText(""+getCurrentLevel().getHeight());
			tfWidth.textProperty().addListener(new NumericFieldListener(tfWidth, false));
			tfHeight.textProperty().addListener(new NumericFieldListener(tfHeight, true));
			
			//Background image stuff
			tgScaling.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
				if(oldVal==newVal) return;
				if(newVal==rbFit)
					getCurrentLevel().setScaling(ScaleMode.FIT);
				else if(newVal==rbExtend)
					getCurrentLevel().setScaling(ScaleMode.EXTEND);
				else
					getCurrentLevel().setScaling(ScaleMode.STRETCH);
				calculateBackgroundBounds();
				redraw();
			});
			
			chkboxViewGrid.selectedProperty().addListener((obs, oldVal, newVal) -> setViewGrid(newVal));
			
			MapManagerApp.stage.setResizable(true);
			MapManagerApp.stage.setMaximized(true);
			
		} catch (IOException e) {
			ErrorHandler.handle("Something went wrong...", e);
		}
	}
	
	public void endInit() {
		MapManagerApp.stage.setResizable(true);
		MapManagerApp.stage.setMaximized(true);
		redraw();
	}
	
	private Stage sendTextStage;
	@FXML
	void onNewText() {
        try {
        	sendTextStage = new Stage();
        	FXMLLoader loader = new FXMLLoader(MapManagerApp.class.getResource("/assets/fxml/TextCreatePane.fxml"));
			Scene scene = new Scene(loader.load());
			CreateTextPaneController cont = loader.getController();
			cont.setMode(CreateTextPaneController.CREATE);
			sendTextStage.setScene(scene);
			sendTextStage.showAndWait();
		} catch (IOException e) {
			ErrorHandler.handle("Could not start stage", e);
		}
	}
	
    @FXML
    void hoverTile(MouseEvent event) {
 
    }
	
	@FXML
	void onBtnChooseImageClicked(ActionEvent e) {
		File f = Utils.openImageDialog();
		if(f == null) return;
		try {
			Image img = SwingFXUtils.toFXImage(ImageIO.read(f), null);
			getCurrentLevel().setBackground(img);
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
		int width = getCurrentLevel().getWidth();
		int height = getCurrentLevel().getHeight();
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
		byte[][] mask = new byte[newHeight][newWidth];
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
		Rectangle rect = new Rectangle(xStart, yStart, newWidth, newHeight);
		
		for(Iterator<Entity> iter = getCurrentLevel().getAllEntities(key).iterator(); iter.hasNext();) {
			Entity entity = iter.next();
			if(rect.contains(entity.getLocation())) {
				entity.setX(entity.getX()-xStart);
				entity.setY(entity.getY()-yStart);
			} else
				iter.remove();
		}
		
		//copy the tiles onto the new map
		for(int i = y1; i<y2; i++) {
			for(int j = x1; j<x2; j++) {
				tiles[i-yStart][j-xStart] = getCurrentLevel().getTile(j, i);
				mask[i-yStart][j-xStart] = getCurrentLevel().getMask(j, i);
			}
		}
		
		for(int i = 0; i < newHeight; i++) {
			for(int j = 0; j < newWidth; j++)
				if(tiles[i][j]==null) {
					tiles[i][j] = new Tile(PresetTile.EMPTY);
					mask[i][j] = 0;
				}
		}
		getCurrentLevel().setTiles(tiles, key);
		getCurrentLevel().setMask(mask, key);
		calculateBackgroundBounds();
		redraw();
	}
	
	public static void setEditingKey(Map.EditingKey _key) {
		key = _key;
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