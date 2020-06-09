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
	
	public void remove(Node n) {
		if(!getChildren().contains(n)) return;
		if(!names) {
			remove(n);
			return;
		}
		int row = getRowIndex(n);
		int col = getColumnIndex(n);
		Node toRemove = null;
		for(Node child : getChildren()) {
			if(getColumnIndex(child) == col && getRowIndex(child) == row + 1) {
				toRemove = child;
				break;
			}
		}
		if(toRemove != null)
			getChildren().remove(toRemove);
		getChildren().remove(n);
	}
	
	public void clear() {
		getChildren().clear();
		rowCount = 1;
		currentRow = 0; currentCol = 0;
	}
	
	public void updateName(Node n, String name) {
		if(!getChildren().contains(n)) return;
		if(!names) return;
		int row = getRowIndex(n);
		int col = getColumnIndex(n);
		for(Node child : getChildren()) {
			if(getColumnIndex(child) == col && getRowIndex(child) == row + 1) {
				((Label) child).setText(name);
				return;
			}
		}
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
			lbl.setWrapText(true);
			this.add(lbl, currentCol, 2*currentRow+1);
		}
		currentCol++;
	}
	
}
