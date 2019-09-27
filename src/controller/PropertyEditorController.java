package controller;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import data.mapdata.Property;
import gui.NumericFieldListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

public class PropertyEditorController {

    @FXML
    private ListView<Property> listView;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnRemove;
    @FXML
    private TextField tfWidth;
    @FXML
    private TextField tfHeight;
    @FXML
    private CheckBox chkboxBloodied;
    
    public ArrayList<Property> startProperties;
    private boolean editing = false;
    
    ObservableList<Property> properties;
    
    Runnable onSaveClicked = null;
    
    public void setProperties(ArrayList<Property> props) {
    	startProperties = props;
    	listView.setCellFactory(new Callback<ListView<Property>, ListCell<Property>>() {
    		@Override
    		public PropertyCell call(ListView<Property> listView) {
    			return new PropertyCell();
    		}
    	});
    	properties = FXCollections.observableArrayList();
    	properties.addAll(props.stream().map(prop -> prop.copy()).collect(Collectors.toList()));
    	listView.setItems(properties);
    	tfWidth.textProperty().addListener(new NumericFieldListener(tfWidth, false));
    	tfHeight.textProperty().addListener(new NumericFieldListener(tfHeight, false));
    }

    @FXML
    void btnEditClicked(ActionEvent event) {
    	if(btnEdit.getText().equals("Cancel")) {
    		if(!requestCancelEditing())
    			return;
    	}
    	setEditing(!editing);
    }
    
    //returns true if editing is canceled.
    public boolean requestCancelEditing() {
    	if(editing) {
	    	ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
			ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
			Alert alert = new Alert(AlertType.CONFIRMATION, "All unsaved changed will be lost.", yes, no);
			alert.setTitle("Confirm cancel");
			alert.setHeaderText("Are you sure you want to cancel editing?");
			Optional<ButtonType> result = alert.showAndWait();
			if(result.orElse(no) == yes) {
				properties.clear();
				properties.addAll(startProperties.stream().map(prop -> prop.copy()).collect(Collectors.toList()));
				return true;
			}
			return false;
    	} else
    		return true;
    }

    private void setEditing(boolean b) {
    	editing = b;
    	listView.refresh();
    	btnSave.setDisable(!editing);
    	btnAdd.setDisable(!editing);
    	btnRemove.setDisable(!editing);
    	btnEdit.setText(editing ? "Cancel" : "Edit");
    }
    
    @FXML
    void btnSaveClicked(ActionEvent event) {
    	startProperties.clear();
    	startProperties.addAll(listView.getItems());
    	if(onSaveClicked != null)
    		onSaveClicked.run();
    	setEditing(false);
    }
    
    @FXML
    void btnAddClicked(ActionEvent event) {
    	properties.add(new Property("key", "value"));
    }
    
    @FXML
    void btnRemoveClicked(ActionEvent event) {
    	for(int i : listView.getSelectionModel().getSelectedIndices()) {
    		properties.remove(i);
    	}
    }
    
    public void setOnSavedAction(Runnable action) {
    	onSaveClicked = action;
    }
    
    public ArrayList<Property> getPropertyList() {
    	return startProperties;
    }
    
    public int getWidth() {
    	return Integer.valueOf(tfWidth.getText());
    }
    
    public int getHeight() {
    	return Integer.valueOf(tfHeight.getText());
    }
    
    public boolean getBloodied() {
    	return chkboxBloodied.isSelected();
    }
    
    class PropertyCell extends ListCell<Property> {
    	
    	private final PropertyCellController controller = new PropertyCellController();
    	private final PropertyCellEditorController editorController = new PropertyCellEditorController();
    	private Node view;
    	
    	public PropertyCell() {
    		prefWidthProperty().bind(listView.widthProperty().subtract(2));
    		setMaxWidth(Control.USE_PREF_SIZE);
    		view = controller.getView();
    	}
    	
    	@Override
    	public void updateItem(Property prop, boolean empty) {
    		super.updateItem(prop, empty);
    		if(empty) {
    			setGraphic(null);
    		} else {
    			if(editing) {
    				view = editorController.getView();
    				editorController.setProperty(prop);
    			} else {
    				view = controller.getView();
    				controller.setProperty(prop);
    			}
    			setGraphic(view);
    		}
    	}
    }
    
    class PropertyCellController {
    	HBox box;
    	Label key, value;
    	
    	PropertyCellController() {
    		box = new HBox();
    		key = new Label();
    		value = new Label();
    		key.setMaxWidth(100000);
    		value.setMaxWidth(100000);
    		key.setPrefWidth(100000);
    		value.setPrefWidth(100000);
    		key.setAlignment(Pos.BASELINE_LEFT);
    		value.setAlignment(Pos.BASELINE_LEFT);
    		box.getChildren().add(key);
    		box.getChildren().add(value);
    		HBox.setHgrow(key, Priority.ALWAYS);
    		HBox.setHgrow(value, Priority.ALWAYS);
    	}
    	
    	void setProperty(Property prop) {
    		key.setText(prop.getKey());
    		value.setText(prop.getValue());
    	}
    	
    	HBox getView() {
    		return box;
    	}
    }
    
    class PropertyCellEditorController {
    	Property prop;
    	
    	HBox box;
    	TextField key, value;
    	
    	PropertyCellEditorController() {
    		box = new HBox();
    		key = new TextField();
    		value = new TextField();
    		box.getChildren().add(key);
    		box.getChildren().add(value);
    		HBox.setHgrow(key, Priority.ALWAYS);
    		HBox.setHgrow(value, Priority.ALWAYS);
    		ChangeListener<Boolean> focusListener = new ChangeListener<Boolean>() {
    			@Override
    			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldVal, Boolean newVal) {
    				if(newVal)
    		    		listView.getSelectionModel().select(properties.indexOf(prop));
    			}
    		};
    		key.focusedProperty().addListener(focusListener);
    		value.focusedProperty().addListener(focusListener);
    		key.textProperty().addListener((obs, oldVal, newVal) -> prop.setKey(newVal));
    		value.textProperty().addListener((obs, oldVal, newVal) -> prop.setValue(newVal));
    	}
    	
    	void setProperty(Property _prop) {
    		prop = _prop;
    		key.setText(prop.getKey());
    		value.setText(prop.getValue());
    	}
    	
    	HBox getView() {
    		return box;
    	}
    }
}