package com.brwsoftware.brwx12viewer;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class ConfigurationListDialog {

	protected class Controller {

	    @FXML
	    private TableView<X12TransformManager.X12Transform> tableView;

	    @FXML
	    private TableColumn<X12TransformManager.X12Transform, String> colTSID;

	    @FXML
	    private TableColumn<X12TransformManager.X12Transform, String> colImpl;

	    @FXML
	    private TableColumn<X12TransformManager.X12Transform, String> colSchema;

	    @FXML
	    private TableColumn<X12TransformManager.X12Transform, String> colXslt;

		@FXML
		public void initialize() {
			colTSID.setCellValueFactory(new PropertyValueFactory<X12TransformManager.X12Transform, String>("tsid"));
			colImpl.setCellValueFactory(new PropertyValueFactory<X12TransformManager.X12Transform, String>("impl"));
			colSchema.setCellValueFactory(new PropertyValueFactory<X12TransformManager.X12Transform, String>("schemaPathName"));
			colXslt.setCellValueFactory(new PropertyValueFactory<X12TransformManager.X12Transform, String>("xsltPathName"));
						
			tableView.setItems(FXCollections.observableArrayList(X12TransformManager.getInstance().getArrayList()));
		}

	    @FXML
	    void onNew(ActionEvent event) {
	    	doItemDialog(-1);
	    }	    

	    @FXML
	    void onEdit(ActionEvent event) {
	    	int row = tableView.getSelectionModel().getSelectedIndex();
	    	if(row != -1) {
	    		doItemDialog(row);
	    	}
	    }
	    
	    @FXML
	    void onDelete(ActionEvent event) {
	    	int row = tableView.getSelectionModel().getSelectedIndex();
	    	if(row != -1) {
	    		Alert alert = new Alert(AlertType.CONFIRMATION);
	    		alert.setTitle("Confirme Delete");
	    		alert.setHeaderText(null);
	    		alert.setContentText("Are you sure you want to delete this item?");

	    		Optional<ButtonType> result = alert.showAndWait();
	    		if (result.get() == ButtonType.OK){
	    			X12TransformManager.X12Transform item = tableView.getItems().get(row);
	    			tableView.getItems().remove(row);
	    			X12TransformManager.getInstance().remove(item.getTSID(), item.getImpl());
	    		}
	    	}
	    }

	    @FXML
	    void onSave(ActionEvent event) {
	    	try {
				X12TransformConfiguration.save();
			} catch (IOException | XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }

	    @FXML
	    void onImport(ActionEvent event) {
			final FileChooser fc = new FileChooser();
			fc.setInitialDirectory(new File(X12TransformConfiguration.getLastImportFolder()));
			fc.getExtensionFilters().add(new ExtensionFilter("XML File", "*.xml"));
			File file = fc.showOpenDialog(null);
			
			if (file != null) {
				try {
					X12TransformConfiguration.importFrom(file);
					tableView.setItems(FXCollections.observableArrayList(X12TransformManager.getInstance().getArrayList()));
					tableView.refresh();
				} catch (XMLStreamException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				};
			}
	    }

	    @FXML
	    void onExport(ActionEvent event) {
			final FileChooser fc = new FileChooser();
			fc.setInitialDirectory(new File(X12TransformConfiguration.getLastExportFolder()));
			fc.getExtensionFilters().add(new ExtensionFilter("XML File", "*.xml"));
			File file = fc.showSaveDialog(null);
			
			if (file != null) {
				try {
					X12TransformConfiguration.exportTo(file);
				} catch (XMLStreamException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				};
			}
	    }
	    
	    private void doItemDialog(int row) {
	    	ConfigurationItemDialog dlg = new ConfigurationItemDialog();
	    	X12TransformManager.X12Transform item = null;
	    	if(row != -1) {
	    		item = tableView.getItems().get(row);
	    		dlg.setTSID(item.getTSID());
	    		dlg.setImpl(item.getImpl());
	    		dlg.setSchema(item.getSchemaPathName());
	    		dlg.setXslt(item.getXsltPathName());
	    	}
	    	
	    	if(dlg.doModal() == ButtonType.OK) {
	    		if(row == -1) {
	    			item = new X12TransformManager.X12Transform();
	    		}
	    		
	    		item.setTSID(dlg.getTSID());
	    		item.setImpl(dlg.getImpl());
	    		item.setSchemaPathName(dlg.getSchema());
	    		item.setXsltPathName(dlg.getXslt());
	    		
	    		if(row == -1) {
	    			X12TransformManager.getInstance().add(item);
	    			tableView.setItems(FXCollections.observableArrayList(X12TransformManager.getInstance().getArrayList()));
	    			tableView.refresh();
	    		}
	    	}
	    }
	}

	private Dialog<ButtonType> dlg;
	private Controller controller;	

	public ConfigurationListDialog() {
		dlg = new Dialog<>();
		controller = new Controller();
		
		FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/configuration_list.fxml"));
		loader.setController(controller);
		Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		dlg.setTitle("X12 Configuration Editor");
		dlg.getDialogPane().setContent(root);
		dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
		dlg.setResizable(true);
	}
	
	public ButtonType doModal() {
		Optional<ButtonType> result = dlg.showAndWait();
		 if (result.isPresent() && result.get() == ButtonType.OK) {
		     return ButtonType.OK;
		 }
		return ButtonType.CANCEL;		
	}	
}
