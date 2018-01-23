package com.brwsoftware.brwx12viewer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

public class X12TransformConfiguration {
	private static final String LAST_IMPORT_FOLDER = "LastImportFolder";
	private static final String LAST_EXPORT_FOLDER = "LastExportFolder";

	private X12TransformConfiguration() {
	}

	public static File getAppFile() throws IOException {
		String theFolder = AppFolder.getAppFolder(X12ViewerApp.APP_NAME, null, X12ViewerApp.APP_AUTHOR);
		Path thePath = Paths.get(theFolder);
		if (Files.notExists(thePath)) {
			Files.createDirectories(thePath);
		}
		return new File(theFolder, "x12transform.xml");
	}

	public static void load() throws XMLStreamException, IOException {
		File x12TransformFile = getAppFile();
		if (x12TransformFile.exists()) {
			X12TransformManager.getInstance().loadFromXml(x12TransformFile);
		}
	}
		
	public static void save() throws FileNotFoundException, IOException, XMLStreamException, FactoryConfigurationError {
		X12TransformManager.getInstance().saveAsXml(getAppFile());
	}
	
	public static void importFrom(File theFile) throws XMLStreamException, IOException {
		X12TransformManager.getInstance().loadFromXml(theFile);
		setLastImportFolder(theFile);
	}
	
	public static void exportTo(File theFile) throws FileNotFoundException, IOException, XMLStreamException, FactoryConfigurationError {
		X12TransformManager.getInstance().saveAsXml(theFile);
		setLastExportFolder(theFile);
	}
	
	public static String getLastImportFolder() {
		Preferences pref = Preferences.userNodeForPackage(X12ViewerApp.class);
		return pref.get(LAST_IMPORT_FOLDER, new File(".").getAbsolutePath());
	}

	public static void setLastImportFolder(File file) {
		Preferences pref = Preferences.userNodeForPackage(X12ViewerApp.class);
		pref.put(LAST_IMPORT_FOLDER, file.getParent());
	}

	public static String getLastExportFolder() {
		Preferences pref = Preferences.userNodeForPackage(X12ViewerApp.class);
		return pref.get(LAST_EXPORT_FOLDER, new File(".").getAbsolutePath());
	}

	public static void setLastExportFolder(File file) {
		Preferences pref = Preferences.userNodeForPackage(X12ViewerApp.class);
		pref.put(LAST_EXPORT_FOLDER, file.getParent());
	}
}
