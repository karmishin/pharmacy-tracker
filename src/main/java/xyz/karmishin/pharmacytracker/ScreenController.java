package xyz.karmishin.pharmacytracker;

import java.util.HashMap;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class ScreenController {
	private static final ScreenController INSTANCE = new ScreenController();
	private Scene mainScene = new Scene(new Group());
	private HashMap<String, Pane> screenMap = new HashMap<>();
	
	private ScreenController() { }

	public static ScreenController getInstance() {
		return INSTANCE;
	}
	
	public Scene getMainScene() {
		return mainScene;
	}
	
	public void addScreen(String name, Pane pane) {
		screenMap.put(name, pane);
	}
	
	public void activate(String name) {
		mainScene.setRoot(screenMap.get(name));
	}
}
