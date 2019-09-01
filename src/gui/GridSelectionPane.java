package gui;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;

public class GridSelectionPane extends GridPane {

	int colCount;
	int rowCount = 1;
	int currentRow=0, currentCol=0;
	public GridSelectionPane(int cols) {
		colCount = cols;
	}
	
	public void add(Node n) {
		if(currentCol>=colCount) {
			currentCol = 0;
			currentRow++;
		}
		this.add(n, currentCol++, currentRow);
	}
	
}
