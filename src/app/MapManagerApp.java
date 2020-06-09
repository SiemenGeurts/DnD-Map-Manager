package app;

import java.io.IOException;
import java.util.Map;

import javax.swing.filechooser.FileSystemView;

import comms.Client;
import comms.Server;
import controller.MainMenuController;
import controller.MapBuilderController;
import controller.SceneManager;
import gui.Dialogs;
import gui.ErrorHandler;
import helpers.Logger;
import helpers.Utils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MapManagerApp extends Application{
	
	public static final long VERSION_ID=1L;
	
	private SceneManager sceneManager;
	
	public static Stage stage;
	public final static String defaultDirectory = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
	
	@Override
	public void start(Stage stage) throws Exception {
		MapManagerApp.stage = stage;
		stage.setResizable(false);
        FXMLLoader loader = new FXMLLoader(MapManagerApp.class.getResource("/assets/fxml/MainMenu.fxml"));
        Scene scene = new Scene(loader.load());
        sceneManager = new SceneManager(stage,scene,loader);
        MainMenuController.sceneManager = this.sceneManager;
        stage.setTitle("DnD Map Manager");
       
        stage.setOnCloseRequest(event -> {
        	if(!sceneManager.requestClose())
        		event.consume();
        });
        if(getParameters()!=null) {
        	Utils.setParameters(getParameters());
        	processArguments(getParameters());
        }
	}
	
	private void processArguments(Parameters params) throws IOException {
		Map<String, String> namedArgs = params.getNamed();
		String playMode = namedArgs.get("playmode");		
		if(playMode == null) return;
        if(playMode.equals("builder")) {
        	FXMLLoader loader = new FXMLLoader(MainMenuController.class.getResource("/assets/fxml/MapBuilder.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getRoot().requestFocus();
            sceneManager.pushView(scene,loader);
            ((MapBuilderController) loader.getController()).endInit();
        } else if(playMode.equals("dm")) {
        	String portString = namedArgs.get("port");
        	int port = 5000;
        	if(portString != null && Utils.isValidPort(portString))
        		port = Integer.parseInt(portString);
        	else
        		Logger.println("No valid port given, using 5000");
        	new ServerGameHandler(Server.create(port));
        } else if(playMode.equals("party")) {
        	String ip = namedArgs.get("ip");
        	String portString = namedArgs.get("port");
        	int port = 5000;
        	if(Utils.isValidIP(ip)) {
				int index = ip.indexOf(':');
				if(index!=-1) {
					port = Integer.parseInt(ip.substring(index+1));
					ip = ip.substring(0, index);
				} else if(portString!=null) {
					if(Utils.isValidPort(portString))
						port = Integer.parseInt(portString);
					else
						Dialogs.warning("Port " + portString + " is not valid, using port 5000 instead.", true);
				}
				new ClientGameHandler(Client.create(ip, port));
        	} else {
        		Dialogs.warning("No valid ip given, application will exit.", true);
        		Platform.exit();
        	}
        }
	}
	
	private static void setupLogFile() {
		new Logger(defaultDirectory +"/log.txt");
	}

    public static void main(String[] args) {
    	try {
    	setupLogFile();
    	//AssetManager.initializeManager();
		//PresetTile.setupPresetTiles();
        launch(args);
    	} catch(Exception e) {
    		ErrorHandler.handle("Something went wrong...", e);
    	}
    }


}
