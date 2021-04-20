package controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import gui.ErrorHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class TextPaneController {

	 @FXML
    private Button btnClose;
    @FXML
    private Button btnNextPage;
    @FXML
    private Button btnPrevPage;
    @FXML
    private TextArea txtArea;
    @FXML
    private Label lblPageNumber;
    
    private String[] pages;
    private int currentPage;
    
    @FXML
    void initialize() {
    	btnNextPage.setDisable(true);
    	btnPrevPage.setDisable(true);
    	InputStream fontStream = TextPaneController.class.getResourceAsStream("/assets/fonts/BlackSamsGold.ttf");
    	if(fontStream != null) {
    		Font font = Font.loadFont(fontStream, 46);
    		try {
				fontStream.close();
			} catch (IOException e) {
				ErrorHandler.handle("An error occured while loading font.",e);
			}
    		txtArea.setFont(font);
    	}
    	txtArea.setEditable(false);
    }
    
    @FXML
    void onBtnCloseClicked(ActionEvent event) {
    	Alert alert = new Alert(AlertType.CONFIRMATION);
    	alert.setTitle("Send text pane");
    	alert.setHeaderText("Are you sure you want to close this window?");
    	Optional<ButtonType> result = alert.showAndWait();
    	if(result.get()==ButtonType.OK) {
    		((Stage) btnClose.getScene().getWindow()).close();
    	}
    }

    @FXML
    void onBtnNextClicked(ActionEvent event) {
    	showPage(currentPage+1);
    }

    @FXML
    void onBtnPrevClicked(ActionEvent event) {
    	showPage(currentPage-1);
    }
	
	public void showText(String text) {
    	showText(new String[] {text});
	}
	
	public void showText(String[] pages) {
		if(pages.length==0)
			showText(pages = new String[] {""});
		this.pages = pages;
		currentPage = 0;
		showPage(currentPage);
	}
	
	private void showPage(int page) {
		currentPage = page;
		btnPrevPage.setDisable(currentPage == 0);
		btnNextPage.setDisable(currentPage == pages.length-1);
		lblPageNumber.setText("Pages " + (page+1)+ "/" + pages.length);
		txtArea.setText(pages[page]);
	}
}
