package gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.util.Pair;
import javafx.util.StringConverter;

public class ReorderableList<T> extends ListView<Pair<Integer,T>> {

	StringConverter<T> converter = null;
	public ReorderableList(ObservableList<Pair<Integer,T>> list) {
		super(list);
		this.setCellFactory(param -> new ReorderableListCell());
	}
	
	public ReorderableList(ObservableList<Pair<Integer,T>> list, StringConverter<T> converter) {
		this(list);
		setEditable(true);
		Objects.requireNonNull(converter);
		this.converter = converter;
	}
	
    private TextField createTextField(ReorderableListCell cell, Pair<Integer,T> value, StringConverter<T> converter) {
    	final TextField textField = new TextField(converter.toString(value.getValue()));

        textField.setOnAction(event -> {
            cell.commitEdit(new Pair<>(value.getKey(),converter.fromString(textField.getText())));
            event.consume();
        });
        textField.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cell.cancelEdit();
                t.consume();
            }
        });
        return textField;
    }
    
    private void startEditCell(ReorderableListCell cell, TextField textField, T value, StringConverter<T> s) {
    	if (textField != null) {
            textField.setText(converter.toString(value));
        }
        cell.setText(null);

        cell.setGraphic(textField);

        textField.selectAll();

        // requesting focus so that key input can immediately go into the
        // TextField (see RT-28132)
        textField.requestFocus();
    }
	
	private class ReorderableListCell extends ListCell<Pair<Integer,T>> {
		TextField textField = null;
		protected ReorderableListCell() {
	        ListCell<Pair<Integer,T>> thisCell = this;

	        //setContentDisplay(ContentDisplay.TEXT_ONLY);
	        setAlignment(Pos.CENTER);

	        setOnDragDetected(event -> {
	            if (getItem() == null) {
	                return;
	            }

	            Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
	            ClipboardContent content = new ClipboardContent();
	            content.putString(getItem().getKey().toString());
	            dragboard.setContent(content);

	            event.consume();
	        });

	        setOnDragOver(event -> {
	            if (event.getGestureSource() != thisCell &&
	                   event.getDragboard().hasString()) {
	                event.acceptTransferModes(TransferMode.MOVE);
	            }

	            event.consume();
	        });

	        setOnDragEntered(event -> {
	            if (event.getGestureSource() != thisCell &&
	                    event.getDragboard().hasString()) {
	                setOpacity(0.3);
	            }
	        });

	        setOnDragExited(event -> {
	            if (event.getGestureSource() != thisCell &&
	                    event.getDragboard().hasString()) {
	                setOpacity(1);
	            }
	        });

	        setOnDragDropped(event -> {
	            if (getItem() == null) {
	                return;
	            }

	            Dragboard db = event.getDragboard();
	            boolean success = false;

	            if (db.hasString()) {
	                ObservableList<Pair<Integer,T>> items = getListView().getItems();
	                Integer i = Integer.valueOf(db.getString());
	                int draggedIdx = -1;
	                int thisIdx = -1;
	                for(int j = 0; j < items.size(); j++) {
	                	if(items.get(j).getKey()==i)
	                		draggedIdx = j;
	                	if(items.get(j).getKey() == getItem().getKey())
	                		thisIdx = j;
	                	if(thisIdx != -1 && draggedIdx!=-1)
	                		break;
	                }
	                
	                Pair<Integer,T> temp = items.get(draggedIdx);
	                items.set(draggedIdx, getItem());
	                items.set(thisIdx, temp);

	                List<Pair<Integer,T>> itemscopy = new ArrayList<>(getListView().getItems());
	                getListView().getItems().setAll(itemscopy);

	                success = true;
	            }
	            event.setDropCompleted(success);

	            event.consume();
	        });

	        setOnDragDone(DragEvent::consume);
	    }
		
		/** {@inheritDoc} */
	    @Override
	    public void startEdit() {
	        super.startEdit();
	        if (!isEditing())
	            return;
	        if (textField == null)
	            textField = createTextField(this, getItem(), converter);
	        startEditCell(this, textField, getItem().getValue(), converter);
	    }
		
		@Override
		protected void updateItem(final Pair<Integer,T> item, boolean empty) {
			super.updateItem(item, empty);
			if(!empty) {
				if (isEditing()) {
	                if (textField != null)
	                    textField.setText(converter.toString(item.getValue()));
	                setText(null);
	                setGraphic(textField);
	            } else {
	                setText(converter.toString(item.getValue()));
	                setGraphic(null);
	            }
			} else {
				setText(null);
				setGraphic(null);
			}
		}
	}
}
