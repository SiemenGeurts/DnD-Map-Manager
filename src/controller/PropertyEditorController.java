package controller;

import data.mapdata.Entity;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class PropertyEditorController {

    @FXML
    private ListView<StringProperty> listView;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnSave;
    
    private Entity entity;
    
    ObservableList<StringProperty> properties;
    
    public void setEntity(Entity _entity) {
    	entity = _entity;
    	properties = FXCollections.observableArrayList();
    	properties.addAll(entity.getProperties());
    	listView.setItems(properties);
    }

    @FXML
    void btnEditClicked(ActionEvent event) {
    	btnEdit.setVisible(false);
    	btnSave.setVisible(true);
    }

    @FXML
    void btnSaveClicked(ActionEvent event) {
    	btnEdit.setVisible(true);
    	btnSave.setVisible(false);
    }
    
    class PropertyCell extends ListCell<StringProperty> {
    	@Override
    	public void updateItem(StringProperty prop, boolean empty) {
    		super.updateItem(prop, empty);
    		
    	}
    }
}
