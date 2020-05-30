package controller;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import actions.ActionEncoder;
import app.Constants;
import app.ServerGameHandler;
import data.mapdata.Entity;
import helpers.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class InitiativeListController {

	@FXML
	private ListView<InitiativeEntry> listview;
	@FXML
	private ObservableList<InitiativeEntry> list;
	@FXML
	private Button btnRemove;
	@FXML
	private Button btnClear;
	@FXML
	private Button btnPrev;
	@FXML
	private Button btnNext;
	@FXML
	private ButtonBar bar;

	final Comparator<InitiativeEntry> comp = (InitiativeEntry e1, InitiativeEntry e2) -> e1.compareTo(e2);
	
	private boolean isServer = true;
	private ServerGameHandler gameHandler;
	
	@FXML
	public void initialize() {
		list = FXCollections.observableArrayList();
		listview.setCellFactory(param -> new ListCell<InitiativeEntry>() {
            private ImageView imageView = new ImageView();
            {
            	imageView.setFitHeight(100);
                imageView.setPreserveRatio(true);
            }
            @Override
            public void updateItem(InitiativeEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(entry.getInitiative() + "  "  + entry.getName());
                    imageView.setImage(entry.getImage());
                    setGraphic(imageView);
                }
            }
        });
		
		listview.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		listview.setItems(list);
		listview.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if(isServer)
				gameHandler.sendUpdate(ActionEncoder.selectInitiative(newVal.entity.getID()), oldVal == null ? "" : ActionEncoder.selectInitiative(oldVal.entity.getID()));
		});
		
		btnRemove.setOnAction(event -> {
			InitiativeEntry entry = listview.getSelectionModel().getSelectedItem();
			if(isServer)
				gameHandler.sendUpdate(ActionEncoder.removeInitiative(entry.entity.getID()), ActionEncoder.addInitiative(entry.entity.getID(), entry.getInitiative()));
			list.remove(entry);
		});
		btnClear.setOnAction(event -> {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setHeaderText("Are you sure you want to clear the initiative list?");
			alert.setContentText("You can't undo this operation.");
			alert.setTitle("Are you sure?");
			Optional<ButtonType> result = alert.showAndWait();
			if(result.orElse(ButtonType.CANCEL) == ButtonType.OK) {
				list.clear();
				gameHandler.sendForcedUpdate(ActionEncoder.clearInitiative());
			}				
		});
		
		btnNext.setOnAction(event -> {
			int index = listview.getSelectionModel().getSelectedIndex();
			index = (index+1)%list.size();
			listview.getSelectionModel().clearAndSelect(index);
		});
		
		btnPrev.setOnAction(event -> {
			int index = listview.getSelectionModel().getSelectedIndex();
			index = (index == 0 ? list.size()-1 : index-1);
			listview.getSelectionModel().clearAndSelect(index);
		});
	}
	
	public void setGameHandler(ServerGameHandler handler) {
		this.gameHandler = handler;
	}
	
	public List<InitiativeEntry> getAll() {
		return Collections.unmodifiableList(list);
	}
	
	public void setMode(short mode) {
		isServer = mode == Constants.SERVERMODE;
		bar.setVisible(isServer);
		//listview.setMouseTransparent(!isServer);
		//listview.setFocusTraversable(isServer);
		listview.setDisable(!isServer);
	}
	
	public boolean remove(int id) {
		for(int i = 0; i < list.size(); i++)
			if(list.get(i).entity.getID()==id) {
				if(isServer)
					gameHandler.sendUpdate(ActionEncoder.removeInitiative(id), ActionEncoder.addInitiative(id, list.get(i).getInitiative()));
				final int j = i;
				Utils.safeRun(()->list.remove(j));
				return true;
			}
		return false;
	}
	
	public void clear() {
		Utils.safeRun(() -> list.clear());
	}
	
	public boolean select(int id) {
		for(int i = 0; i < list.size(); i++)
			if(list.get(i).entity.getID()==id) {
				final int j = i;
				Utils.safeRun(() -> listview.getSelectionModel().clearAndSelect(j));
				return true;
			}
		return false;
	}
	
	public void addEntity(Entity entity, int initiative) {
		if(entity == null)
			System.err.println("Could not add null entity!");
		Utils.safeRun(() -> {
			list.add(new InitiativeEntry(entity, initiative));
			list.sort(comp);
			if(isServer)
				gameHandler.sendUpdate(ActionEncoder.addInitiative(entity.getID(), initiative), ActionEncoder.removeInitiative(entity.getID()));
		});
	}
	
	boolean success = false;
	public void addEntity(Entity entity) {
		success=false;
		while(!success) {
			TextInputDialog dialog = new TextInputDialog("10");
			dialog.setTitle("Initiative Entry");
			dialog.setHeaderText("Initiative for " + entity.getName());
			dialog.setContentText("Enter intiative:");
	
			Optional<String> result = dialog.showAndWait();
			result.ifPresent(value -> {
				try {
					int initiative = Integer.valueOf(value);
					success = true;
					addEntity(entity, initiative);
				} catch(NumberFormatException e) {}
			});
		}
	}
	
	public class InitiativeEntry implements Comparable<InitiativeEntry> {
		Entity entity;
		int initiative;
		
		public InitiativeEntry(Entity entity, int initiative) {
			this.entity = entity;
			this.initiative = initiative;
		}
		
		public Entity getEntity() {
			return entity;
		}
		
		public int getInitiative() {
			return initiative;
		}
		
		Image getImage() {
			return entity.getTexture();
		}
		
		String getName() {
			return entity.getName();
		}

		@Override
		public int compareTo(InitiativeEntry other) {
			if(other.initiative<initiative)
				return -1;
			else if(other.initiative>initiative)
				return 1;
			return 0;
		}
	}
	
}
