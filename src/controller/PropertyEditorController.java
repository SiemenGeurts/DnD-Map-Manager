package controller;

import java.util.ArrayList;
import java.util.Optional;

import data.mapdata.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
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
    	properties.addAll(props);
    	listView.setItems(properties);
    	tfWidth.textProperty().addListener((observable, oldVal, newVal) -> {
    		if(!newVal.matches("\\d*"))
    			tfWidth.setText(newVal.replaceAll("[^\\d]", ""));
    		if(newVal.length()==0)
    			tfWidth.setText("1");
    		if(newVal.charAt(0)=='-' || newVal.charAt(0)=='0')
    			tfWidth.setText(newVal.substring(1));
    	});
    	tfHeight.textProperty().addListener((observable, oldVal, newVal) -> {
    		if(!newVal.matches("\\d*"))
    			tfHeight.setText(newVal.replaceAll("[^\\d]", ""));
    		if(newVal.length()==0)
    			tfHeight.setText("1");
    		if(newVal.charAt(0)=='-' || newVal.charAt(0)=='0')
    			tfHeight.setText(newVal.substring(1));
    	});
    }

    @FXML
    void btnEditClicked(ActionEvent event) {
    	if(btnEdit.getText().equals("Cancel")) {
    		ButtonType yes = new ButtonType("yes", ButtonBar.ButtonData.OK_DONE);
    		ButtonType no = new ButtonType("no", ButtonBar.ButtonData.CANCEL_CLOSE);
    		Alert alert = new Alert(AlertType.CONFIRMATION, "All unsaved changed will be lost.", yes, no);
    		alert.setTitle("Confirm cancel");
    		alert.setHeaderText("Are you sure you want to cancel editing?");
    		Optional<ButtonType> result = alert.showAndWait();
    		if(result.orElse(no) == yes) {
    			properties.clear();
    			properties.addAll(startProperties);
    		}
    	}
    	editing = !editing;
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
    }
    
    @FXML
    void btnAddClicked(ActionEvent event) {
    	properties.add(new Property("key", "value"));
    }
    
    @FXML
    void btnRemoveClicked(ActionEvent event) {
    	for(int i : listView.getSelectionModel().getSelectedIndices()) {
    		System.out.println("removing index "  + i);
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
    	private final Node view = controller.getView();
    	
    	public PropertyCell() {
    		prefWidthProperty().bind(listView.widthProperty().subtract(2));
    		setMaxWidth(Control.USE_PREF_SIZE);
    	}
    	
    	@Override
    	public void updateItem(Property prop, boolean empty) {
    		super.updateItem(prop, empty);
    		if(empty) {
    			setGraphic(null);
    		} else {
    			controller.setProperty(prop);
    			setGraphic(view);
    		}
    	}
    }
    
    class PropertyCellController {
    	Property prop;
    	
    	HBox box;
    	TextField key, value;
    	
    	PropertyCellController() {
    		box = new HBox();
    		key = new TextField();
    		value = new TextField();
    		box.getChildren().add(key);
    		box.getChildren().add(value);
    		ChangeListener<Boolean> focusListener = new ChangeListener<Boolean>() {
    			@Override
    			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldVal, Boolean newVal) {
    				if(newVal)
    		    		listView.getSelectionModel().select(properties.indexOf(prop));
    			}
    		};
    		key.focusedProperty().addListener(focusListener);
    		value.focusedProperty().addListener(focusListener);
    		key.addEventHandler(ActionEvent.ACTION, event -> prop.setKey(key.getText()));
    		value.addEventHandler(ActionEvent.ACTION, event -> prop.setValue(value.getText()));
    	}
    	
    	void setProperty(Property _prop) {
    		prop = _prop;
    		key.setDisable(!editing);
    		value.setDisable(!editing);
    		key.setText((String) prop.getKey());
    		value.setText(prop.getValue());
    	}
    	
    	HBox getView() {
    		return box;
    	}
    }
}