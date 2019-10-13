package gui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Dialogs {
	public static void warning(String msg, boolean wait) {
		if(Platform.isFxApplicationThread()) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning");
			alert.setHeaderText("Something did not go as planned.");
			alert.setContentText(msg);
			if(wait)
				alert.showAndWait();
			else
				alert.show();
		} else {
			Platform.runLater(new Runnable() {
				public void run() {
					Alert alert = new Alert(AlertType.WARNING);
					alert.setTitle("Warning");
					alert.setHeaderText("Something did not go as planned.");
					alert.setContentText(msg);
					if(wait)
						alert.showAndWait();
					else
						alert.show();
				}
			});
		}
	}
	
	public static void info(String msg, boolean wait) {
		if(Platform.isFxApplicationThread()) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText(msg);
			if(wait)
				alert.showAndWait();
			else
				alert.show();
		} else {
			Platform.runLater(new Runnable() {
				public void run() {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Information");
					alert.setHeaderText(msg);
					if(wait)
						alert.showAndWait();
					else
						alert.show();
				}
			});
		}
	}
}
