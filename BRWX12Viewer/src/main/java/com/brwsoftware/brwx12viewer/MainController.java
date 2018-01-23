package com.brwsoftware.brwx12viewer;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;

public class MainController {

	private static final String LAST_USED_FOLDER = "LastUsedFolder";

	@FXML
	private MenuBar menuBar;
	
    @FXML
    private TabPane tabPane;
	
    @FXML
	public void initialize() {
		//Utilize close buttons
		tabPane.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
		
		//Ctrl+F4 to close current tab
		tabPane.setOnKeyPressed(e -> {
			if (e.isControlDown() && e.getCode() == KeyCode.F4) {
				if (!tabPane.getTabs().isEmpty()) {
					tabPane.getTabs().remove(tabPane.getSelectionModel().getSelectedIndex());
				}
			}
		});
	}

	@FXML
	private void onFileMenuShowing(Event event) {
		Menu fileMenu = (Menu) event.getSource();
		if (fileMenu != null) {
			buildFileMenuMRU(fileMenu);
		}
	}

	@FXML
	void onFileExit(ActionEvent event) {
		Platform.exit();
	}

	@FXML
	void onFileOpen(ActionEvent event) {
		if(tabPane.getTabs().isEmpty()) {
			onFileNewTab(event);
		} else {
			Preferences pref = 	Preferences.userNodeForPackage(MainController.class);
			final FileChooser fc = new FileChooser();
			fc.setInitialDirectory(new File(getLastUsedFolder(pref)));
			File file = fc.showOpenDialog(tabPane.getScene().getWindow());
			
			if (file != null) {
				MruFileManager.getInstance().add(file);
				tabPane.getSelectionModel().getSelectedItem().setText(file.getName());
				forwardCommandEvent(new CommandEvent(AppCommand.COMMAND_OPEN_FILE, file));
			}
		}
	}

	@FXML
	void onFileNewTab(ActionEvent event) {		
		Preferences pref = 	Preferences.userNodeForPackage(MainController.class);
		final FileChooser fc = new FileChooser();
		fc.setInitialDirectory(new File(getLastUsedFolder(pref)));
		File file = fc.showOpenDialog(tabPane.getScene().getWindow());
		
		if (file != null) {
			setLastUsedFolder(pref, file);
			MruFileManager.getInstance().add(file);
			addNewTab(file);
			forwardCommandEvent(new CommandEvent(AppCommand.COMMAND_OPEN_FILE, file));			
		}
	}

	@FXML
	void onFileSaveAs(ActionEvent event) {
		forwardCommandEvent(new CommandEvent(AppCommand.COMMAND_SAVE_AS));
	}
	
	@FXML
	void onFilePrint(ActionEvent event) {
		forwardCommandEvent(new CommandEvent(AppCommand.COMMAND_PRINT));
	}
	
	@FXML
	private void onEditCopy(final ActionEvent event) {
		forwardCommandEvent(new CommandEvent(AppCommand.COMMAND_COPY));
	}
	
	@FXML
	private void onEditFind(final ActionEvent event) {
		forwardCommandEvent(new CommandEvent(AppCommand.COMMAND_FIND));
	}
	
	@FXML
	private void onEditSelectAll(final ActionEvent event) {
		forwardCommandEvent(new CommandEvent(AppCommand.COMMAND_SELECT_ALL));
	}
	
	@FXML
	private void onEditConfiguration(final ActionEvent event) {
		ConfigurationListDialog dlg = new ConfigurationListDialog();
		dlg.doModal();
	}
	
	@FXML
	private void onHelpAbout(final ActionEvent event) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("About BRWX12Viewer");
		alert.setHeaderText(null);
		alert.setContentText("BRWX12Viewer\nVersion 1.0.0");
		alert.showAndWait();
	}
	
	private String getLastUsedFolder(Preferences pref) {
		return pref.get(LAST_USED_FOLDER, new File(".").getAbsolutePath());
	}

	private void setLastUsedFolder(Preferences pref, File file) {
		 pref.put(LAST_USED_FOLDER, file.getParent());
	}
	
	private Parent newX12View() {
		Parent root = null;
		FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/x12_view.fxml"));
		loader.setController(new X12ViewController());
		try {
			root = loader.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return root;
	}
	
	private void forwardCommandEvent(CommandEvent event) {
		Tab theTab = tabPane.getSelectionModel().getSelectedItem();
		if (theTab != null) {
			Event.fireEvent(theTab.getContent(), event);
		}
	}
	
	private void addNewTab(File file) {
		Tab tab = new Tab();
		tab.setText(file.getName());
		
		Parent view = newX12View();
		tab.setContent(view);
		tabPane.getTabs().add(tab);
		tabPane.getSelectionModel().selectLast();
	}
	
	private void onFileMruAction(ActionEvent action) {
		MenuItem menuItem = (MenuItem) action.getSource();
		if (menuItem != null && menuItem.getUserData() != null) {
			File file = (File) menuItem.getUserData();
			MruFileManager.getInstance().add(file);
			
			//Note: maybe a preference to determine mru action (ie new tab or reuse tab)
			addNewTab(file);
//			if(tabPane.getTabs().isEmpty()) {
//				addNewTab(file);
//			} else {
//				tabPane.getSelectionModel().getSelectedItem().setText(file.getName());
//			}
			forwardCommandEvent(new CommandEvent(AppCommand.COMMAND_OPEN_FILE, file));
		}
	}

	private void buildFileMenuMRU(Menu fileMenu) {
		// Find the marker locations
		int mruStart = -1;
		int mruEnd = -1;
		int count = fileMenu.getItems().size();
		for (int i = 0; i < count; i++) {
			MenuItem menuItem = fileMenu.getItems().get(i);
			if (menuItem != null) {
				String menuId = menuItem.getId();
				if (menuId != null) {
					if (menuId.compareToIgnoreCase("mruStart") == 0) {
						mruStart = i;
					} else if (menuId.compareToIgnoreCase("mruEnd") == 0) {
						mruEnd = i;
					}
				}
			}
		}

		if (mruStart == -1 || mruEnd == -1) {
			return;
		}

		// Remove current MRU's
		for (int i = mruEnd - 1; i > mruStart; i--) {
			fileMenu.getItems().remove(i);
		}

		// Add MRU's
		int mruCount = MruFileManager.getInstance().size();
		if (mruCount > 0) {
			int index = 0;
			for (index = 0; index < mruCount; index++) {
				File file = MruFileManager.getInstance().get(index);
				String menuText = String.format("_%d %s", index + 1, file.getName());
				MenuItem menuItem = new MenuItem(menuText);
				menuItem.setUserData(file);
				menuItem.setOnAction(new EventHandler<ActionEvent>() {
					public void handle(ActionEvent action) {
						onFileMruAction(action);
					}
				});

				fileMenu.getItems().add(mruStart + index + 1, menuItem);
			}
			fileMenu.getItems().add(mruStart + index + 1, new SeparatorMenuItem());
		}
	}
}
