package gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class NumericFieldListener implements ChangeListener<String>  {
	
	TextField field;
	boolean allowDoubles;
	
	public NumericFieldListener(TextField field, boolean allowDoubles) {
		this.field = field;
		this.allowDoubles = allowDoubles;
	}
	
	@Override
	public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		if(allowDoubles ? !newValue.matches("\\d+(.\\d*)?") : !newValue.matches("\\d+"))
			field.setText(oldValue);
			
	}

}
