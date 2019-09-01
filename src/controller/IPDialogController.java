package controller;

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
	private ToggleGroup toggleGroup;
	
	private int option;
	private boolean isServer = false;
	private String ip;
	
	@FXML
	public void handleClick(ActionEvent e) {
		if(e.getSource() == btnOk) {
			if(toggleGroup.getSelectedToggle() == rbServer) {
				option = OK;
				isServer = true;
				closeStage(e);
			} else {
				ipField.setText(ipField.getText().trim().replaceAll("\\s+", ""));
				if(isValidIP(ipField.getText())) {
					option = OK;
					isServer = false;
					ip = ipField.getText();
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
	
	private boolean isValidIP(String ip) {
		if(ip.equals("localhost"))
			return true;
		if(ip.indexOf('.')==-1) return false;
		String[] sections = ip.split("\\.");
		for(String s : sections) {
			try {
				if(s.length()>3 || s.length()==0) return false;
				int i = Integer.parseInt(s);
				if(i > 255 || i<0)
					return false;
			} catch(NumberFormatException e) {
				return false;
			}
		}
		return true;
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
	
	public boolean isServer() {
		return isServer;
	}
	
	public boolean isClient() { 
		return !isServer;
	}
}
