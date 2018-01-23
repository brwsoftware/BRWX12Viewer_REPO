package com.brwsoftware.brwx12viewer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

public class X12PanelController {

	private File tempHtmlFile;
	private String outputMsg;
	private String searchText;
	
	@FXML
	protected WebView webView;
	
    @FXML
	public void initialize() {
		initWebView();
	}
    
	protected String getOutputMsg() {
		return outputMsg;
	}

	protected void setOutputMsg(String outputMsg) {
		this.outputMsg = outputMsg;
	}
    
    protected void initWebView() {
        WebEngine webEngine = webView.getEngine();
        
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>()
        {
            @Override
            public void changed(final ObservableValue<? extends Worker.State> observableValue, final State oldState, final State newState)
            {
                if (newState == State.SUCCEEDED)
                {
                	URL url = getClass().getClassLoader().getResource("files/mark.js");
                	if(url != null) {
                        webEngine.executeScript(makeInjectableScript(url));
                	}
                	url = null;
                	url = getClass().getClassLoader().getResource("files/search_webview.js");
                	if(url != null) {
                        webEngine.executeScript(makeInjectableScript(url));
                	}
                }
            }
        });
        
        webView.addEventHandler(CommandEvent.COMMAND, new EventHandler<CommandEvent>() {
			@Override
			public void handle(CommandEvent event) {
				onCommandEvent(event);
				
				//This seems to be important. Otherwise stack over flow would happen,
				event.consume();
			}
		});        
    }
    
	protected void onCommandEvent(CommandEvent event) {
		switch(event.getCommand())
		{
		case AppCommand.COMMAND_COPY:
			copyToClipboard();
			break;
		case AppCommand.COMMAND_SELECT_ALL:
			selectAll();
			break;
		case AppCommand.COMMAND_PRINT:
			doPrint();
			break;
		case AppCommand.COMMAND_SAVE_AS:
			doSaveAs();
			break;
		case AppCommand.COMMAND_OPEN_FILE:
			doOpenFile(event.getFile());
			break;
		case AppCommand.COMMAND_FIND_START:
			findStart(event.getText());
			break;
		case AppCommand.COMMAND_FIND_END:
			findEnd();
			break;
		case AppCommand.COMMAND_FIND_NEXT:
			findNext();
			break;
		case AppCommand.COMMAND_FIND_PREV:
			findPrev();
			break;
		}	
	}    
    
	private String makeInjectableScript(URL resource) {
		StringBuilder str = new StringBuilder();
		str.append("var script = document.createElement(\"script\");");
		str.append(String.format("script.src = \"%s\";", resource.toString()));
		str.append("script.charset = \"UTF-8\";" );
		str.append("document.getElementsByTagName(\"body\")[0].appendChild(script);");		
		return str.toString();
	}

	protected File createTempFile() throws IOException {
		// Important to use the html extension
		// WebEngine/WebKit uses the extension to know the file type.
		// If it doesn't recognize the type, such as ".tmp" it won't load.
		tempHtmlFile = File.createTempFile("brwx12viewer-", ".html");
		tempHtmlFile.deleteOnExit();
		return tempHtmlFile;
	}

	protected void copyTempFile(File dest) throws IOException {
		Files.copy(tempHtmlFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
	    FileTime now = FileTime.fromMillis(System.currentTimeMillis());
	    Files.setLastModifiedTime(dest.toPath(), now);
	}
	
	protected void loadHtml() {    	
		// Note: even though this doesn't produce "file://" it seems to work
		// The URL will be in the form of file:/c://A//B...
		File f = new File(tempHtmlFile.getAbsolutePath());
		try {
			webView.getEngine().load(f.toURI().toURL().toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}	

	protected void loadOutputMsg() {    	
		webView.getEngine().loadContent(outputMsg);
	}
	
	protected void copyToClipboard() {
		KeyEvent k = new KeyEvent(KeyEvent.KEY_PRESSED, "C", "", KeyCode.C, false, true, false, false);
		webView.fireEvent(k);
	}
	
	protected void selectAll() {
		KeyEvent k = new KeyEvent(KeyEvent.KEY_PRESSED, "A", "", KeyCode.A, false, true, false, false);
		webView.fireEvent(k);
	}
	
	protected void doPrint() {
		PrinterJob job = PrinterJob.createPrinterJob();
		if (job != null) {
			if (job.showPrintDialog(null)) {
				webView.getEngine().print(job);
				job.endJob();
			}
		}		
	}

	public void findStart(String findText) {
		boolean doMark = false;
		if(searchText == null || searchText.isEmpty()) {
			doMark = true;
		} else if(searchText.compareToIgnoreCase(findText) != 0) {
			doMark = true;
		}
		searchText = findText;

		StringBuilder script = new StringBuilder();
		if(doMark) {
			script.append(String.format("markText(\"%s\");", searchText));
		}
		script.append(String.format("window.find(\"%s\");", searchText));
			
		Platform.runLater(() -> {			
			webView.getEngine().executeScript(script.toString());
		});
	}
	
	public void findEnd() {
		searchText = null;
		Platform.runLater(() -> {			
			webView.getEngine().executeScript("removeMark();");
		});	
	}
	
	public void findNext() {
		if(searchText != null && !searchText.isEmpty()) {
			Platform.runLater(() -> {			
				webView.getEngine().executeScript(String.format("window.find(\"%s\", false, false, true);", searchText));
			});
		}
	}

	public void findPrev() {
		if(searchText != null && !searchText.isEmpty()) {
			Platform.runLater(() -> {			
				webView.getEngine().executeScript(String.format("window.find(\"%s\", false, true, true);", searchText));
			});
		}	
	}

	protected void doOpenFile(File file) {
		
	}
	
	protected void doSaveAs() {
		final FileChooser fc = new FileChooser();
		File file = fc.showSaveDialog(webView.getScene().getWindow());

		if (file != null) {
			if (file.exists()) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Confirm Save As");
				alert.setContentText(String.format("%s already exists\nDo you want to replace it?", file.getName()));
				
				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == ButtonType.OK){
					doSaveAs(file);
				}
			}
		}
	}
	
	protected void doSaveAs(File dest) {
		try {
			copyTempFile(dest);
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}
