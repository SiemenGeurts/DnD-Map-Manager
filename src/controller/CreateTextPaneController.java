package controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Optional;

import org.json.JSONException;
import org.json.JSONObject;

import app.Constants;
import app.MapManagerApp;
import gui.Dialogs;
import gui.ErrorHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class CreateTextPaneController {

	public static final short CREATE = 1;
	public static final short SEND = 2;
	
	@FXML
	private Button btnSend;
    @FXML
    private TabPane tabPane;
	@FXML
	private Button btnCancel;
	
	private Font font;
	
	private FileChooser fileChooser;
	private static final FileChooser.ExtensionFilter jsonFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
	
    private ArrayList<TextArea> pages;
    
    private boolean canceled = false;
	@FXML
	void initialize() {
		pages = new ArrayList<>();
		pages.add((TextArea)((AnchorPane) tabPane.getTabs().get(0).getContent()).getChildrenUnmodifiable().get(0));

		InputStream fontStream = TextPaneController.class.getResourceAsStream("/assets/fonts/BlackSamsGold.ttf");
		if(fontStream != null) {
			font = Font.loadFont(fontStream, 46);
			pages.get(0).setFont(font);
			try {
				fontStream.close();
			} catch (IOException e) {
				ErrorHandler.handle("An error occured while loading font.",e);
			}
		}
	}
	
	public void setMode(short val) {
		switch(val) {
			case CREATE:
				btnSend.setVisible(false);
				break;
			case SEND:
				btnSend.setVisible(true);
				break;
		}
	}
	
	public int getPageCount() {
		return pages.size();
	}
	
	public JSONObject getJSON() {
		JSONObject obj = new JSONObject();
		obj.put("type", Constants.JSON_TYPE_SHOW_TEXT);
		int count = 0;
		for(TextArea ta : pages) {
			String str = ta.getText();
			if(str.length()==0)
				continue;
			obj.put("page"+ ++count, str);
		}
		obj.put("num_pages", count);
		return obj;
	}
	
	private void setJSON(JSONObject json) throws JSONException {
		int count = json.getInt("num_pages");
		tabPane.getTabs().clear();
		pages.clear();
		for(int i = 1; i <= count; i++)
			addPage(json.getString("page"+i));
	}
	
	public boolean isCanceled() {
		return canceled;
	}
	
    @FXML
    void btnCancelClicked(ActionEvent event) {
    	Alert alert = new Alert(AlertType.CONFIRMATION);
    	alert.setTitle("Send text pane");
    	alert.setHeaderText("Are you sure you want to close this window?");
    	alert.setContentText("Any entered text will be lost");
    	Optional<ButtonType> result = alert.showAndWait();
    	if(result.get()==ButtonType.OK) {
    		canceled = true;
    		((Stage) btnCancel.getScene().getWindow()).close();
    	}
    }

    @FXML
    void onBtnSaveClicked(ActionEvent event) {
    	if(fileChooser == null) {
    		fileChooser = new FileChooser();
    		fileChooser.getExtensionFilters().add(jsonFilter);
    	}
    	fileChooser.setTitle("Save file");
		fileChooser.setInitialDirectory(MapManagerApp.getLastResourceDirectory());
		File file = fileChooser.showSaveDialog(SceneManager.getPrimaryStage());
		if(file == null) return;
		MapManagerApp.updateLastResourceDirectory(file);
		
		try (PrintWriter writer = new PrintWriter(file)) {
			writer.println(getJSON().toString());
			Dialogs.info("Text was successfully saved!", false);
		} catch (IOException e) {
			ErrorHandler.handle("Could not save text.", e);
		}
    }
    
    @FXML
    void onBtnOpenClicked(ActionEvent event) {
    	if(fileChooser == null) {
    		fileChooser = new FileChooser();
    		fileChooser.getExtensionFilters().add(jsonFilter);
    	}
    	fileChooser.setTitle("Open file");
		fileChooser.setInitialDirectory(MapManagerApp.getLastResourceDirectory());
		File file = fileChooser.showOpenDialog(SceneManager.getPrimaryStage());
		if(file == null) return;
		MapManagerApp.updateLastResourceDirectory(file);
		
		try {
			setJSON(new JSONObject(Files.readString(file.toPath())));
		} catch (JSONException | IOException e) {
			ErrorHandler.handle("Could not load text.", e);
		}
    }
    
    @FXML
    void btnSendClicked(ActionEvent event) {
    	canceled = false;
    	((Stage)btnCancel.getScene().getWindow()).close();
    }

    @FXML
    void newPageClicked(ActionEvent event) {
    	addPage("");
    }
    
    private void addPage(String s) {
    	Tab tab = new Tab("Page " + (pages.size()+1));
    	
    	TextArea area = new TextArea(s);
    	area.setWrapText(true);
    	area.getStyleClass().add("text-area");
    	area.getStylesheets().add("/assets/css/textpane.css");
    	area.getStylesheets().add("/assets/css/scrollbars.css");
    	if(font != null)
    		area.setFont(font);
    	AnchorPane pane = new AnchorPane();
    	pane.getStyleClass().add("anchor-pane");
    	pane.getStylesheets().add("/assets/css/textpane.css");
    	AnchorPane.setTopAnchor(area, 50d);
    	AnchorPane.setLeftAnchor(area, 50d);
    	AnchorPane.setRightAnchor(area, 150d);
    	AnchorPane.setBottomAnchor(area, 50d);
    	pane.getChildren().add(area);
    	tab.setContent(pane);
    	tab.setClosable(true);
    	tab.setOnClosed(e -> {
    		pages.remove(area);
    	});
    	pages.add(area);
    	
    	tabPane.getTabs().add(tab);
    }
}
