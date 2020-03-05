package controller;

import java.util.Hashtable;
import java.util.Stack;

import controller.InitClassStructure.SceneController;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class SceneManager {
	
	private static Stage primaryStage;
	private Stack<Scene> scenes;
	private Hashtable<Scene, FXMLLoader> SceneLoader;

	public SceneManager(Stage primaryStage, Scene mainScene, FXMLLoader loader) {
		SceneManager.primaryStage = primaryStage;
        SceneLoader = new Hashtable<>();
        mainScene.getRoot().requestFocus();
        scenes = new Stack<>();
        scenes.push(mainScene);
        SceneLoader.put(mainScene,loader);
        updateScene();
	}
	
	public void pushView(Scene scene, FXMLLoader loader) {
		this.scenes.push(scene);
        scene.getRoot().requestFocus();
        SceneLoader.put(scene,loader);
        if(loader.getController() instanceof SceneController) {
            SceneController controller = (SceneController) loader.getController();
            controller.initialize(this);
        }
		updateScene();

	}
	
	private void updateScene() {
		SceneManager.primaryStage.setScene(scenes.peek());
		primaryStage.show();
		scenes.peek().setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode() == KeyCode.ESCAPE){
					if(scenes.size()>1){
						popScene();
					}
				}
			}
		});
		setCenterOnScreen();

	}
	private void setCenterOnScreen(){
		Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		primaryStage.setX((primScreenBounds.getWidth() - primaryStage.getWidth()) / 2);
		primaryStage.setY((primScreenBounds.getHeight() - primaryStage.getHeight()) / 2);
	}
	
	public void popScene() {
		if(scenes.size()>1) {
			scenes.pop();
			updateScene();
		}
	}

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public boolean requestClose() {
    	while(scenes.size()>1) {
    		Scene scene = scenes.peek();
    		Object loader = SceneLoader.get(scene).getController();
    		if(loader instanceof MapEditorController) {
    			if(!((MapEditorController) loader).checkSaved())
    				return false;
    			else
    				popScene();
    		} else
    			popScene();
    	}
    	return true;
    }
}
