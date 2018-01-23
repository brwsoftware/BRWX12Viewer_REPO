package com.brwsoftware.brwx12viewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import com.brwsoftware.brwx12library.ISASegment;
import com.brwsoftware.brwx12library.X12Exception;
import com.brwsoftware.brwx12library.X12Reader;
import com.brwsoftware.brwx12library.X12Segment;

import javafx.application.Platform;
import javafx.fxml.FXML;

public class X12PanelUnformattedController extends X12PanelController {

	private File rawX12File;

	@FXML
	public void initialize() {
		super.initialize();
	}

	@Override
	public void doOpenFile(File file) {
		rawX12File = file;
		Platform.runLater(new X12Runnable(file));
	}
	
	private class X12Runnable implements Runnable {
		private File theFile;

		X12Runnable(File theFile) {
			this.theFile = theFile;
		}

		@Override
		public void run() {
			boolean transformResult = false;
			try {
				transformResult = makeHtmlFile(theFile);
			} catch (IOException | X12Exception e) {
				setOutputMsg(e.getMessage());
				transformResult = false;
				e.printStackTrace();
			}

			final boolean hasHtml = transformResult;

			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if (hasHtml) {
						loadHtml();
					} else {
						loadOutputMsg();
					}
				}
			});
		}

		private boolean makeHtmlFile(final File theFile) throws IOException, X12Exception {

			File tempFile = createTempFile();

			try (FileWriter theOutput = new FileWriter(tempFile);) {
				theOutput.write("<html><body><pre>");

				X12Reader reader = new X12Reader(new FileInputStream(theFile.getPath()));
				ISASegment isa = reader.readISA();
				theOutput.write(isa.toString());
				X12Segment seg = reader.readSegment();
				while (seg != null) {
					theOutput.write(System.lineSeparator());
					theOutput.write(seg.toString());
					seg = reader.readSegment();
				}

				theOutput.write("</pre></body></html>");
			}

			return true;
		}
	}

	@Override
	protected void doSaveAs(File dest) {
		try (FileWriter theOutput = new FileWriter(dest);) {
			X12Reader reader = new X12Reader(new FileInputStream(rawX12File.getPath()));
			ISASegment isa = reader.readISA();
			theOutput.write(isa.toString());
			X12Segment seg = reader.readSegment();
			while (seg != null) {
				theOutput.write(System.lineSeparator());
				theOutput.write(seg.toString());
				seg = reader.readSegment();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (X12Exception e) {
			e.printStackTrace();
		}
	}
}
