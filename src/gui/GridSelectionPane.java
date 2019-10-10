package gui;

import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.TextAlignment;

public class GridSelectionPane extends GridPane {

	int colCount;
	int rowCount = 1;
	int currentRow=0, currentCol=0;
	boolean names = true;
	
	public GridSelectionPane(int cols) {
		colCount = cols;
	}
	
	public void setNames(boolean _names) {
		names = _names;
	}
	
	public void add(Node n) {
		add(n, "");
	}
	
	public void add(Node n, String name) {
		if(currentCol>=colCount) {
			currentCol = 0;
			currentRow++;
		}
		this.add(n, currentCol, names ? 2*currentRow : currentRow);
		if(names) {
			Label lbl = new Label(name);
			lbl.setTextAlignment(TextAlignment.CENTER);
			setHalignment(lbl, HPos.CENTER);
			this.add(lbl, currentCol, 2*currentRow+1);
		}
		currentCol++;
	}
	
}
