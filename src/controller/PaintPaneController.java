package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;

public class PaintPaneController {
	
	@FXML
	protected ToggleButton tglBrush;
	@FXML
	protected Slider sldrBrushSize,sldrBrushOpacity;
	@FXML
	protected Label lblBrushSize, lblBrushOpacity;

	@FXML
	void initialize() {
		sldrBrushSize.valueProperty().addListener((obs, oldVal, newVal) ->
			lblBrushSize.setText(String.format("%.1f tiles", newVal.floatValue()))
		);
		sldrBrushOpacity.valueProperty().addListener((obs, oldVal, newVal) ->
			lblBrushOpacity.setText(String.format("%d%%", newVal.intValue()))
		);
	}
	
	@FXML
	void onFogBrushToggle(ActionEvent event) {
		if(tglBrush.isSelected()) {
			tglBrush.setText("Disable fog brush");
		} else
			tglBrush.setText("Enable fog brush");
	}
	
	public int getSize() {
		return (int) Math.round(sldrBrushSize.getValue());
	}
	
	public double getOpacity() {
		return sldrBrushOpacity.getValue()/100;
	}
	
	public boolean isEditingMask() {
		return tglBrush.isSelected();
	}
}