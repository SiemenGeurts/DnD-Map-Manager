package controller;

import java.awt.Point;

import app.ClientGameHandler;
import app.MapManagerApp;
import data.mapdata.Entity;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class ClientController extends MapController {
	ClientGameHandler gameHandler;
	Entity selected = null;
	
	@FXML
	private CheckBox chkboxBuffer;
	@FXML
	private Button btnPush;
	
	public void setGameHandler(ClientGameHandler _gameHandler) {
		gameHandler = _gameHandler;
	}
	
	@FXML
	@Override
	public void initialize() {
		super.initialize();
		MapManagerApp.stage.setResizable(true);
		MapManagerApp.stage.setMaximized(true);
		chkboxBuffer.selectedProperty().addListener((obs, oldVal, newVal) -> {
			if(oldVal) {
				if(!gameHandler.requestDisableUpdateBuffer()) {
					chkboxBuffer.setSelected(true);
					btnPush.setDisable(true);
				}
			} else {
				btnPush.setDisable(false);
				gameHandler.enableUpdateBuffer();
			}
		});
	}
	
	@FXML
	void onBtnPushClicked(ActionEvent event) {
		gameHandler.pushUpdates();
	}
	
	@Override
	protected void handleClick(Point p, MouseEvent event) {
		System.out.println("Mouse clicked: " + p + " : " + event.isPrimaryButtonDown());
    	if(event.getButton() == MouseButton.PRIMARY || event.isSynthesized()) {
    		Entity e = currentMap.getEntity(p);
    		if(e == null && selected != null) {
    			gameHandler.move(selected, p);
    			selected = null;
    		} else if(selected == null) {
    			selected = e;
    		}
    	}
	}
}