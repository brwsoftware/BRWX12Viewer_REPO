package com.brwsoftware.brwx12viewer;

import java.io.IOException;
import java.net.URL;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;

public class X12ViewController {

	@FXML
	private BorderPane paneX12;

	@FXML
	private ToggleGroup viewType;

	@FXML
	private ToolBar tbFind;
    
	@FXML
    private TextField textFind;
    
	private Node panelUnformatted;
	private Node panelFormatted;
	
	@FXML
	public void initialize() {
		paneX12.addEventHandler(CommandEvent.COMMAND, new EventHandler<CommandEvent>() {
			@Override
			public void handle(CommandEvent event) {
				onCommandEvent(event);
				event.consume();
			}
		});
		
		textFind.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				Event.fireEvent(getCurSelX12Panel(), new CommandEvent(AppCommand.COMMAND_FIND_START, newValue));
			}
	    });
		
		panelUnformatted = newX12Panel("fxml/x12_unformatted.fxml");
		panelFormatted = newX12Panel("fxml/x12_formatted.fxml");

		paneX12.setCenter(panelUnformatted);
	}
	
	@FXML
	void onCloseFindBar(ActionEvent event) {
    	tbFind.setVisible(false);
    	Event.fireEvent(panelUnformatted, new CommandEvent(AppCommand.COMMAND_FIND_END));
    	Event.fireEvent(panelFormatted, new CommandEvent(AppCommand.COMMAND_FIND_END));
    	getCurSelX12Panel().requestFocus();
    }

	@FXML
	void onFindNext(ActionEvent event) {
		Event.fireEvent(getCurSelX12Panel(), new CommandEvent(AppCommand.COMMAND_FIND_NEXT));
	}

	@FXML
	void onFindPrev(ActionEvent event) {
		Event.fireEvent(getCurSelX12Panel(), new CommandEvent(AppCommand.COMMAND_FIND_PREV));
	}

	@FXML
	void onViewFormatted(ActionEvent event) {
		paneX12.setCenter(panelFormatted);
		panelFormatted.requestFocus();
	}

	@FXML
	void onViewUnformatted(ActionEvent event) {
		paneX12.setCenter(panelUnformatted);
		panelUnformatted.requestFocus();
	}
	
	private Parent newX12Panel(String fxml) {
		Parent root = null;
		URL url = getClass().getClassLoader().getResource(fxml);
		try {
			root = FXMLLoader.load(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return root;
	}

	private void onCommandEvent(CommandEvent event) {
		switch (event.getCommand()) {
		case AppCommand.COMMAND_FIND:
			tbFind.setVisible(true);
			textFind.requestFocus();
			break;
		case AppCommand.COMMAND_OPEN_FILE:
			onOpenFile(event);
			break;
		default:
			Event.fireEvent(getCurSelX12Panel(), event);
			break;
		}
	}

	private void onOpenFile(CommandEvent event) {
		Event.fireEvent(panelUnformatted, event);
		Event.fireEvent(panelFormatted, event);
	}
	
	protected Node getCurSelX12Panel() {
		return paneX12.getCenter();		
	}
}
