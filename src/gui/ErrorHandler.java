package gui;

import java.io.PrintWriter;
import java.io.StringWriter;

import helpers.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class ErrorHandler {

	public static void handle(String msg, Exception ex) {
		if(ex != null)
			Logger.error(ex);
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Something went wrong");
		alert.setHeaderText("An exception occured");
		if(ex == null)
			alert.setContentText(msg);
		else {
			alert.setContentText(msg + "\n" + ex.getMessage());

			Label lbl = new Label("The exception stacktrace was:");
			StringWriter sw;
			ex.printStackTrace(new PrintWriter(sw = new StringWriter()));
			TextArea text = new TextArea(sw.toString()) {{
				setWrapText(true);
				setEditable(false);
				setMaxWidth(Double.MAX_VALUE);
				setMaxHeight(Double.MAX_VALUE);
			}};
			
			GridPane.setVgrow(text, Priority.ALWAYS);
			GridPane.setHgrow(text, Priority.ALWAYS);
			GridPane content = new GridPane();
			content.setMaxWidth(Double.MAX_VALUE);
			content.add(lbl, 0, 0);
			content.add(text, 0, 1);
			alert.getDialogPane().setExpandableContent(content);
		}
		alert.showAndWait();
	}
}
