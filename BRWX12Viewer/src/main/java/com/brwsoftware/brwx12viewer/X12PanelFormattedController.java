package com.brwsoftware.brwx12viewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import com.brwsoftware.brwx12library.X12Exception;
import com.brwsoftware.brwx12library.X12Identifier;

import javafx.application.Platform;
import javafx.fxml.FXML;

public class X12PanelFormattedController extends X12PanelController {
	@FXML
	public void initialize() {
		super.initialize();
	}
	
	@Override
	public void doOpenFile(File file) {
		Platform.runLater(new X12Runnable(file));
	}
	
    private class X12Runnable implements Runnable {
    	private File theFile;
                 
        X12Runnable(File theFile) {
            this.theFile = theFile;
        }
        
        public void run() {
        	boolean transformResult = false;
    		try {
    			transformResult = makeHtmlFile(theFile);
			} catch (IOException | X12Exception | TransformerException | XMLStreamException e) {
				setOutputMsg(e.getMessage());
				transformResult = false;
				e.printStackTrace();
			}
    		
    		final boolean hasHtml = transformResult;

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                	if(hasHtml) {
                		loadHtml();
                	} else {
                		loadOutputMsg();
                	}
                }
           });
        }
    }
    
	private boolean makeHtmlFile(final File theFile) throws FileNotFoundException, IOException, X12Exception, TransformerException, XMLStreamException {
		X12Identifier.Attributes attrX12 = X12Identifier.getAttributes(new FileInputStream(theFile));
		
		String tsid = attrX12.getTransactionSet().getID();
		String impl = attrX12.getTransactionSet().getImplementation();
		
		if(!X12TransformManager.getInstance().hasBestMatch(tsid, impl)) {
			setOutputMsg(String.format("No transform available for TSID=%s IMPL=%s", tsid, impl));
			return false;
		}
		
		X12TransformManager.X12Transform trfmX12 = X12TransformManager.getInstance().getBestMatch(tsid, impl);
		
		File tempFile = createTempFile();
		
		//Transform X12 to Html
		try (FileInputStream theInput = new FileInputStream(theFile);
				FileOutputStream theOutput = new FileOutputStream(tempFile);) {
			trfmX12.transformHtml(theInput, theOutput);
		}
		
		return true;
	}
}
