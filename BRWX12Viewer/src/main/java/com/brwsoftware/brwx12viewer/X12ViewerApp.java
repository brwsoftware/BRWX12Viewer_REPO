package com.brwsoftware.brwx12viewer;

import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

public class X12ViewerApp extends Application {
	
	public static final String APP_NAME = "BRWX12Viewer";
	public static final String APP_AUTHOR = "BRWSoftware";
	
	private static final String LAST_X = "lastX";
	private static final String LAST_Y = "lastY";
	private static final String LAST_CX = "lastCX";
	private static final String LAST_CY = "lastCY";
	
	private Stage primaryStage;
		
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/main.fxml"));
			loader.setController(new MainController());
			Parent root = loader.load();
			Scene scene = new Scene(root);
			
			primaryStage.setTitle("BRWX12Viewer");
			primaryStage.setScene(scene);
			
			X12TransformConfiguration.load();			
			MruFileManager.getInstance().load();
			
			Preferences pref = 	Preferences.userNodeForPackage(X12ViewerApp.class);
			int x = pref.getInt(LAST_X, -1);
			int y = pref.getInt(LAST_Y, -1);
			int cx = pref.getInt(LAST_CX, -1);
			int cy = pref.getInt(LAST_CY, -1);
			
			Window window = primaryStage.getScene().getWindow();
			if (x != -1) {
				window.setX(x);
				window.setY(y);
				window.setHeight(cy);
				window.setWidth(cx);
			}
			
			primaryStage.show();

			this.primaryStage = primaryStage;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop(){
	    Window window = primaryStage.getScene().getWindow();

		Preferences pref = 	Preferences.userNodeForPackage(X12ViewerApp.class);
		pref.putInt(LAST_X, (int)window.getX());
		pref.putInt(LAST_Y, (int)window.getY());
		pref.putInt(LAST_CX, (int)window.getWidth());
		pref.putInt(LAST_CY, (int)window.getHeight());
		
		MruFileManager.getInstance().save();
	}
}
