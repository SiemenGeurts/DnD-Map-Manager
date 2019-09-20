package controller;

import java.io.IOException;

import app.ClientGameHandler;
import app.ServerGameHandler;
import comms.Client;
import comms.Server;
import gui.ErrorHandler;
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
    	System.exit(0);
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
					try {
						new ServerGameHandler(Server.create(controller.getPort()));
					} catch(IOException e) {
						ErrorHandler.handle("A server could not be created.", e);
					}
				} else {
					try {
						new ClientGameHandler(Client.create(controller.getIP(), controller.getPort()));
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
