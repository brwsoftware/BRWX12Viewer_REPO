package com.brwsoftware.brwx12viewer;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;

public class ConfigurationItemDialog {
	
	protected class Controller {
	    @FXML
	    private TextField txtTSID;

	    @FXML
	    private TextField txtImpl;

	    @FXML
	    private TextField txtSchema;

	    @FXML
	    private TextField txtXslt;
	    
		@FXML
		public void initialize() {
		}
				
	    @FXML
	    void onBrowseSchema(ActionEvent event) {
			final FileChooser fc = new FileChooser();
			fc.setInitialFileName(txtSchema.getText());
			File file = fc.showOpenDialog(null);
			
			if (file != null) {
				txtSchema.setText(file.getPath());			
			}
	    }

	    @FXML
		void onBrowseXslt(ActionEvent event) {
			final FileChooser fc = new FileChooser();
			fc.setInitialFileName(txtXslt.getText());
			File file = fc.showOpenDialog(null);

			if (file != null) {
				txtXslt.setText(file.getPath());
			}
		}
	}

	private Dialog<ButtonType> dlg;
	private Controller controller;	

	public ConfigurationItemDialog() {
		dlg = new Dialog<>();
		controller = new Controller();
		
		FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/configuration_item.fxml"));
		loader.setController(controller);
		Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		dlg.getDialogPane().setContent(root);
		dlg.getDialogPane().getButtonTypes().add(ButtonType.OK);
		dlg.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
		dlg.setResizable(true);
		
		final Button btOk = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
		btOk.addEventFilter(ActionEvent.ACTION, event -> {
			if (!isValidTSID(getTSID()) ||
					!isValidSchemeFile(getSchema()) || 
					!isValidXsltFile(getXslt())) {
				event.consume();
			}
		});
	}
	
	public String getTSID() {
		return controller.txtTSID.getText();
	}

	public void setTSID(String value) {
		controller.txtTSID.setText(value);
	}

	public String getImpl() {
		return controller.txtImpl.getText();
	}

	public void setImpl(String value) {
		controller.txtImpl.setText(value);
	}

	public String getSchema() {
		return controller.txtSchema.getText();
	}

	public void setSchema(String value) {
		controller.txtSchema.setText(value);
	}

	public String getXslt() {
		return controller.txtXslt.getText();
	}

	public void setXslt(String value) {
		controller.txtXslt.setText(value);
	}

	public ButtonType doModal() {
		Optional<ButtonType> result = dlg.showAndWait();
		 if (result.isPresent() && result.get() == ButtonType.OK) {
		     return ButtonType.OK;
		 }
		return ButtonType.CANCEL;		
	}	

	private boolean isValidTSID(String tsid) {
		if (tsid == null || tsid.isEmpty()) {
    		Alert alert = new Alert(AlertType.WARNING);
    		alert.setTitle("Required Value");
    		alert.setHeaderText(null);
    		alert.setContentText("Please enter a value for TSID");

    		alert.showAndWait();
    		return false;
		}
		
		return true;
	}
	
	private boolean isValidSchemeFile(String schemaPathName) {
		return isValidConfigurationFile("SCHEMA", schemaPathName);
	}

	private boolean isValidXsltFile(String schemaPathName) {
		return isValidConfigurationFile("XSLT", schemaPathName);
	}
	
	private boolean isValidConfigurationFile(String label, String pathName) {
		if (pathName == null || pathName.isEmpty()) {
    		Alert alert = new Alert(AlertType.WARNING);
    		alert.setTitle("Required Value");
    		alert.setHeaderText(null);
    		alert.setContentText("Please enter a value for " +label);

    		alert.showAndWait();
    		return false;
		}

		File file = new File(pathName);
		if(!file.isFile() || !file.exists()) {
    		Alert alert = new Alert(AlertType.WARNING);
    		alert.setTitle("Required Value");
    		alert.setHeaderText(null);
    		alert.setContentText(label + " does not appear to be a valid file");

    		alert.showAndWait();
    		return false;			
		}
		
		return true;
	}
}
