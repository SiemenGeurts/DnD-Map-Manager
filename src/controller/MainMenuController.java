package controller;

import java.io.IOException;

import app.ClientGameHandler;
import app.MapManagerApp;
import app.ServerGameHandler;
import comms.Client;
import gui.ErrorHandler;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainMenuController {

	public static SceneManager sceneManager;
	
    @FXML
    void exit(ActionEvent event) {
    	Platform.exit();
    }
    
    @FXML
    void startEditText(ActionEvent event) {
    	try {
        	FXMLLoader loader = new FXMLLoader(MapManagerApp.class.getResource("/assets/fxml/TextCreatePane.fxml"));
			Scene scene = new Scene(loader.load());
			CreateTextPaneController cont = loader.getController();
			cont.setMode(CreateTextPaneController.CREATE);
			scene.getRoot().requestFocus();
			sceneManager.pushView(scene, loader);
		} catch (IOException e) {
			ErrorHandler.handle("Could not start stage", e);
		}
    }

    @FXML
    void startMapBuilder(ActionEvent event) {
    	try {
            FXMLLoader loader = new FXMLLoader(MainMenuController.class.getResource("/assets/fxml/MapBuilder.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getRoot().requestFocus();
            sceneManager.pushView(scene,loader);
            ((MapBuilderController) loader.getController()).endInit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void startPlay(ActionEvent event) {
    	try {
	    	Stage stage = new Stage();
			stage.setTitle("Connection dialog");
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainMenuController.class.getResource("/assets/fxml/IPDialog.fxml"));
			stage.setScene(new Scene(loader.load()));
			IPDialogController controller = loader.getController();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.showAndWait();
			if(controller.getResult() == IPDialogController.OK) {
				if(controller.isServer()) {
					new ServerGameHandler(controller.getPort());
				} else {
					try {
						new ClientGameHandler(new Client(controller.getIP(), controller.getPort()));
					} catch(IOException e) {
						ErrorHandler.handle("A client could not be created.", e);
					}
				}
			}
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }

}
