package controller;

import helpers.Utils;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class IPDialogController {
	
	public static final int OK = 1, CANCEL=2, SERVER=3, CLIENT=4;
	public static final PseudoClass INVALID = PseudoClass.getPseudoClass("invalid");
	@FXML
	private Button btnOk;
	@FXML
	private Button btnCancel;
	@FXML
	private RadioButton rbServer;
	@FXML
	private RadioButton rbClient;
	@FXML
	private TextField ipField;
	@FXML
	private TextField portField;
	
	@FXML
	private ToggleGroup toggleGroup;
	
	private int option;
	private boolean isServer = false;
	private String ip;
	private int port = 5000;
	
	@FXML
	void initialize() {
		rbClient.selectedProperty().addListener((observable, oldVal, newVal) -> {
			ipField.setDisable(!newVal);
		});
		rbServer.selectedProperty().addListener((observable, oldVal, newVal) -> {
			portField.setDisable(!newVal);
		});
	}
	
	@FXML
	public void handleClick(ActionEvent e) {
		if(e.getSource() == btnOk) {
			if(toggleGroup.getSelectedToggle() == rbServer) {
				portField.setText(portField.getText().replaceAll("\\s+", ""));
				if(Utils.isValidPort(portField.getText())) {
					port = Integer.parseInt(portField.getText());
					option = OK;
					isServer = true;
					closeStage(e);
				} else
					portField.pseudoClassStateChanged(INVALID, true);
			} else {
				ipField.setText(ipField.getText().trim().replaceAll("\\s+", ""));
				if(Utils.isValidIP(ipField.getText())) {
					int index = ipField.getText().indexOf(':');
					if(index!=-1) {
						ip = ipField.getText().substring(0, index);
						port = Integer.parseInt(ipField.getText().substring(index+1));
					} else
						ip = ipField.getText();						
					option = OK;
					isServer = false;
					closeStage(e);
				} else {
					ipField.pseudoClassStateChanged(INVALID, true);
				}
			}
		} else {
			option = CANCEL;
			closeStage(e);
		}
	}
	
	private void closeStage(ActionEvent e) {
		((Stage) ((Button) e.getSource()).getScene().getWindow()).close();
	}
	
	public int getResult() {
		return option;
	}
	
	public String getIP() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}
	
	public boolean isServer() {
		return isServer;
	}
	
	public boolean isClient() { 
		return !isServer;
	}
}
