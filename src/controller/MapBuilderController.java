package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import app.MapManagerApp;
import data.mapdata.Entity;
import data.mapdata.Map;
import data.mapdata.PresetTile;
import data.mapdata.Tile;
import data.mapdata.prefabs.EntityPrefab;
import data.mapdata.prefabs.TilePrefab;
import gui.BuilderButton;
import gui.GridSelectionPane;
import helpers.AssetManager;
import helpers.JSONManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

public class MapBuilderController extends MapController {

	private File currentFile = null;
	private FileChooser mapChooser;

	@FXML
	MenuBar menuBar;
	
	@FXML
	private AnchorPane toolkitPane;
	@FXML
	ScrollPane entityScrollPane;
	@FXML
	ScrollPane tileScrollPane;
	@FXML
	ScrollPane playerScrollPane;
	@FXML
	GridSelectionPane tilePane, entityPane, playerPane;
	
	ToolkitController tkController;

	@Override
	public void initialize() {
		super.initialize();
		try {
			currentMap = Map.emptyMap(20, 20);
			FXMLLoader loader = new FXMLLoader(MainMenuController.class.getResource("../assets/fxml/Toolkit.fxml"));
			Node root = new Scene(loader.load()).getRoot();
			toolkitPane.getChildren().add(root);
			tkController = loader.getController();
			AnchorPane.setTopAnchor(root, 0d);
			AnchorPane.setBottomAnchor(root, 0d);
			AnchorPane.setLeftAnchor(root, 0d);
			AnchorPane.setRightAnchor(root, 0d);
			
			canvas.widthProperty().bind(((AnchorPane) canvas.getParent()).widthProperty());
			canvas.heightProperty().bind(((AnchorPane) canvas.getParent()).heightProperty());
			canvas.widthProperty().addListener(event -> drawMap());
			canvas.heightProperty().addListener(event -> drawMap());
			/*menuBar.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
				if (oldScene == null && newScene != null) {
					newScene.windowProperty().addListener((observableWindow, oldWindow, newWindow) -> {
						if (oldWindow == null && newWindow != null) {
							newWindow.xProperty().addListener(
									(obs, oldVal, newVal) -> toolkit.setX(newVal.doubleValue() + newWindow.getWidth()));
							newWindow.yProperty()
									.addListener((obs, oldVal, newVal) -> toolkit.setY(newVal.doubleValue()));
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
						}
					});
				}
			});*/
			tilePane = new GridSelectionPane(5);
			tilePane.add(new BuilderButton<Tile>(new TilePrefab(PresetTile.EMPTY), AssetManager.textures.get(PresetTile.EMPTY)));
			tilePane.add(new BuilderButton<Tile>(new TilePrefab(PresetTile.FLOOR), AssetManager.textures.get(PresetTile.FLOOR)));
			tilePane.add(new BuilderButton<Tile>(new TilePrefab(PresetTile.WALL), AssetManager.textures.get(PresetTile.WALL)));
			tilePane.add(new BuilderButton<Tile>(new TilePrefab(PresetTile.BUSHES), AssetManager.textures.get(PresetTile.BUSHES)));
			tileScrollPane.setContent(tilePane);
			entityPane = new GridSelectionPane(5);
			entityScrollPane.setContent(entityPane);
			playerPane = new GridSelectionPane(5);
			playerScrollPane.setContent(playerPane);
			ArrayList<TilePrefab> tiles = JSONManager.getTiles();
			if(tiles != null)
				for(TilePrefab tp : tiles)
					tilePane.add(new BuilderButton<Tile>(tp, AssetManager.textures.get(tp.getID())));
			ArrayList<EntityPrefab> entities = JSONManager.getEntities();
			if(entities != null)
				for(EntityPrefab ep : entities)
					entityPane.add(new BuilderButton<Entity>(ep, AssetManager.textures.get(ep.getID())));
			entities = JSONManager.getPlayers();
			if(entities != null)
				for(EntityPrefab ep : entities)
					playerPane.add(new BuilderButton<Entity>(ep, AssetManager.textures.get(ep.getID())));
			
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