package juupje.dae_gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import juupje.dae_gui.controllers.MainController;

public class DAE_GUI extends Application {

	MainController controller;
	@Override
	public void start(@SuppressWarnings("exports") Stage stage) throws Exception {
		stage.setResizable(false);
		FXMLLoader loader = new FXMLLoader(DAE_GUI.class.getResource("/juupje/dae_gui/assets/MainScreen.fxml"));
		stage.setScene(new Scene(loader.load()));
		
		controller = loader.getController();
		stage.show();
        stage.setTitle("DyAnnotationExtractor");
	}
	
    public static void main(String[] args) {
    	try {
    		launch(args);
    	} catch(Exception e) {
    		Dialogs.error("Something went wrong...", e);
    	}
    }
}