package controller.InitClassStructure;

import controller.SceneManager;

public class SceneController{
    protected SceneManager sceneManager;


    public void initialize() {

    }


    public void initialize(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }
}
