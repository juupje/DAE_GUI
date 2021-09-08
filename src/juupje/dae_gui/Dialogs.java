package juupje.dae_gui;

import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class Dialogs {
	static int openDialogs = 0;
	public static void error(String msg, Exception ex) {
		if(ex != null)
			ex.printStackTrace();
		else
			System.err.println(msg);
		if(openDialogs>5) return;
		Runnable rb = () -> {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Something went wrong");
			alert.setHeaderText("An exception occured");
			if(ex == null) {
				alert.setContentText(msg);
				alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			} else {
				alert.setContentText(msg + (ex.getMessage()!=null ? "\n" + ex.getMessage() : ""));
	
				Label lbl = new Label("The exception stacktrace was:");
				StringWriter sw;
				ex.printStackTrace(new PrintWriter(sw = new StringWriter()));
				TextArea text = new TextArea(sw.toString()) {{
					setWrapText(true);
					setEditable(false);
					setMaxWidth(Double.MAX_VALUE);
					setMaxHeight(Double.MAX_VALUE);
				}};
				
				GridPane.setVgrow(text, Priority.ALWAYS);
				GridPane.setHgrow(text, Priority.ALWAYS);
				GridPane content = new GridPane();
				content.setMaxWidth(Double.MAX_VALUE);
				content.add(lbl, 0, 0);
				content.add(text, 0, 1);
				alert.getDialogPane().setExpandableContent(content);
			}
			openDialogs += 1;
			alert.showAndWait();
			openDialogs -= 1;
		};
		if(Platform.isFxApplicationThread())
			rb.run();
		else
			Platform.runLater(rb);
	}
	
	public static void warning(String msg, boolean wait) {
		if(Platform.isFxApplicationThread()) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning");
			alert.setHeaderText("Something did not go as planned.");
			alert.setContentText(msg);
			if(wait)
				alert.showAndWait();
			else
				alert.show();
		} else {
			Platform.runLater(new Runnable() {
				public void run() {
					Alert alert = new Alert(AlertType.WARNING);
					alert.setTitle("Warning");
					alert.setHeaderText("Something did not go as planned.");
					alert.setContentText(msg);
					if(wait)
						alert.showAndWait();
					else
						alert.show();
				}
			});
		}
	}
	
	public static void info(String msg, boolean wait) {
		if(Platform.isFxApplicationThread()) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText(msg);
			if(wait)
				alert.showAndWait();
			else
				alert.show();
		} else {
			Platform.runLater(new Runnable() {
				public void run() {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Information");
					alert.setHeaderText(msg);
					if(wait)
						alert.showAndWait();
					else
						alert.show();
				}
			});
		}
	}
}
