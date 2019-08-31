package controller;

import java.util.Optional;

import data.mapdata.Entity;
import data.mapdata.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
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
    
    private Entity entity;
    private boolean editing = false;
    
    ObservableList<Property> properties;
    
    public void setEntity(Entity _entity) {
    	entity = _entity;
    	listView.setCellFactory(new Callback<ListView<Property>, ListCell<Property>>() {
    		@Override
    		public PropertyCell call(ListView<Property> listView) {
    			return new PropertyCell();
    		}
    	});
    	properties = FXCollections.observableArrayList();
    	properties.addAll(entity.getProperties());
    	properties.add(new Property("Strength", "15"));
    	listView.setItems(properties);
    }

    @FXML
    void btnEditClicked(ActionEvent event) {
    	if(btnEdit.getText().equals("cancel")) {
    		ButtonType yes = new ButtonType("yes", ButtonBar.ButtonData.OK_DONE);
    		ButtonType no = new ButtonType("no", ButtonBar.ButtonData.CANCEL_CLOSE);
    		Alert alert = new Alert(AlertType.CONFIRMATION, "Are you sure you want to cancel editing?", yes, no);
    		alert.setTitle("Confirm cancel");
    		//alert.setHeaderText("Are you sure you want to cancel editing?");
    		//alert.setContentText("All unsaved changed will be lost.");
    		Optional<ButtonType> result = alert.showAndWait();
    		if(result.orElse(no) == yes) {
    			properties.clear();
    			properties.addAll(entity.getProperties());
    		}
    	}
    	editing = !editing;
    	listView.refresh();
    	btnSave.setDisable(!editing);
    	btnAdd.setDisable(!editing);
    	btnRemove.setDisable(!editing);
    	btnEdit.setText(editing ? "cancel" : "edit");
    }

    @FXML
    void btnSaveClicked(ActionEvent event) {
    	//TODO: save properties to the current entity.
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