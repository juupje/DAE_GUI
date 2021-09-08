module dae_gui {
	requires javafx.fxml;
	requires javafx.controls;
	requires javafx.base;
	requires java.prefs;
	requires java.desktop;
	requires DyAnnotationExtractor;
	requires java.base;
	exports juupje.dae_gui;
	opens juupje.dae_gui to javafx.graphics;
	opens juupje.dae_gui.controllers to javafx.fxml;
}