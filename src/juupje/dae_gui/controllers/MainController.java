package juupje.dae_gui.controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.filechooser.FileSystemView;

import dsk.anotex.AnnotationExtractor;
import dsk.anotex.Constants;
import dsk.anotex.core.AnnotatedDocument;
import dsk.anotex.core.Annotation;
import dsk.anotex.core.FileFormat;
import dsk.anotex.exporter.AnnotationExporter;
import dsk.anotex.exporter.ExporterFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import juupje.dae_gui.DAE_GUI;
import juupje.dae_gui.Dialogs;

public class MainController {

    @FXML
    private TableView<Annotation> table;
    @FXML
    private TextField fieldInputFile;
    @FXML
    private Button btnSelectInput;
    @FXML
    private CheckBox chkBtnCustomOutput;
    @FXML
    private TextField fieldOutputFile;
    @FXML
    private Button btnSelectOutput;
    @FXML
    private Button btnImport;
    @FXML
    private Spinner<Integer> spnrStartPage;
    @FXML
    private Spinner<Integer> spnrEndPage;
    
    
    private FileChooser fc = null;
    private static final FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
    private static final FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt");
    private static final FileChooser.ExtensionFilter mdFilter = new FileChooser.ExtensionFilter("Markdown files (*.md)", "*.md");
    
    private static Preferences prefs;
    private static final String PREFKEY_LAST_DIR = "LAST_DIR";
    private static File lastDirectory = FileSystemView.getFileSystemView().getDefaultDirectory();
    
    private boolean customOutput = false;
    
    private AnnotationExtractor extractor;
    private AnnotatedDocument document;
    
    private final ObservableList<Annotation> annotations = FXCollections.observableArrayList();
    private final HashMap<String, Object> settings = new HashMap<>();
    
    @FXML
    void initialize() {
    	chkBtnCustomOutput.selectedProperty().addListener((obs, oldVal, newVal) -> {
    		if(oldVal==newVal) return;
    		fieldOutputFile.setDisable(!newVal);
    		btnSelectOutput.setDisable(!newVal);
    		customOutput = newVal;
    	});
    	fieldInputFile.textProperty().addListener((obs, oldVal, newVal) -> {
    		btnImport.setDisable(newVal == null || newVal.length()==0);
    	});
    	extractor = new AnnotationExtractor();
    	
    	TableColumn<Annotation, Integer> pageColumn = new TableColumn<>("Page");
    	pageColumn.setCellValueFactory(new PropertyValueFactory<>("page"));
    	pageColumn.setResizable(false);
    	TableColumn<Annotation, String> highlightColumn = new TableColumn<>("Highlight");
    	highlightColumn.setCellValueFactory(new PropertyValueFactory<>("highlight"));
    	highlightColumn.setMinWidth(100);
    	highlightColumn.maxWidthProperty().bind(table.widthProperty().multiply(0.5));
    	highlightColumn.setPrefWidth(300);
    	TableColumn<Annotation, String> annotationColumn = new TableColumn<>("Annotation");
    	annotationColumn.setCellValueFactory(new PropertyValueFactory<>("text"));
    	annotationColumn.setMinWidth(100);
    	annotationColumn.maxWidthProperty().bind(table.widthProperty().multiply(0.5));
    	annotationColumn.setPrefWidth(300);
    	table.getColumns().add(pageColumn);
    	table.getColumns().add(highlightColumn);
    	table.getColumns().add(annotationColumn);
    	table.setItems(annotations);
    }
    
    void setupFileChooser() {
    	fc = new FileChooser();
    	prefs = Preferences.userNodeForPackage(DAE_GUI.class);
    	lastDirectory = new File(prefs.get(PREFKEY_LAST_DIR, lastDirectory.toString()));
    }
    
    void updateLastDirectory(File dir) {
    	if(!dir.isDirectory())
    		dir = dir.getParentFile();
    	lastDirectory = dir;
    	prefs.put(PREFKEY_LAST_DIR, dir.getAbsolutePath());
    	try {
			prefs.flush();
		} catch (BackingStoreException e) {
			Dialogs.error("Something went wrong while saving preferences", e);
		}
    }
    
    @FXML
    void onSelectInputClicked(ActionEvent event) {
    	if(fc == null)
    		setupFileChooser();
    	fc.getExtensionFilters().clear();
    	fc.getExtensionFilters().add(pdfFilter);
    	fc.setInitialDirectory(lastDirectory);
		File file = fc.showOpenDialog(null);
		if(file == null) return;
		if(!file.exists()) {
			Dialogs.warning("File not found", true);
			return;
		}
		updateLastDirectory(file);
		fieldInputFile.setText(file.getPath());
    }

    @FXML
    void onSelectOutputClicked(ActionEvent event) {
    	if(fc == null)
    		setupFileChooser();
    	fc.getExtensionFilters().clear();
    	fc.getExtensionFilters().add(txtFilter);
    	fc.getExtensionFilters().add(mdFilter);
    	fc.setInitialDirectory(lastDirectory);
		File file = fc.showSaveDialog(null);
		if(file == null) return;
		updateLastDirectory(file);
		fieldOutputFile.setText(file.getPath());
		
    }
    
    @FXML
    void onImportClicked(ActionEvent event) {
    	try {
    		document = extractor.readAnnotations(fieldInputFile.getText());
    		
    		spnrStartPage.setDisable(false);
    		spnrStartPage.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, document.getNumberOfPages()));
    		spnrStartPage.getValueFactory().setValue(1);
    		spnrEndPage.setDisable(false);
    		spnrEndPage.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, document.getNumberOfPages()));
    		spnrEndPage.getValueFactory().setValue(document.getNumberOfPages());
    		
    		spnrStartPage.getValueFactory().valueProperty().addListener((obs, oldVal, newVal) -> reloadPages(newVal, spnrEndPage.getValue()));
    		spnrEndPage.getValueFactory().valueProperty().addListener((obs, oldVal, newVal) -> reloadPages(spnrStartPage.getValue(), newVal));
    		if(annotations.size()>0)
    			annotations.clear();
    		annotations.addAll(document.getAnnotations());
    		
    		Dialogs.info("Extracted " + document.getAnnotations().size() + " annotations.", true);
    		
    	} catch(Exception e) {
    		Dialogs.error("Could not import file", e);
    	}
    }
    
    private void reloadPages(int begin, int end) {
    	if(end<begin) {
    		annotations.clear();
    		return;
    	}
    	annotations.setAll(document.getAnnotations().stream().filter(
    			annotation -> annotation.getPage()>=begin && annotation.getPage()<=end)
    			.collect(Collectors.toList()));
    }
    
    private String getOutputFile(FileFormat type) {
    	if(customOutput) {
    		String filename = fieldOutputFile.getText();
    		if(filename == null || filename.length()>0) {
    			if(filename.endsWith(type.getExtension()))
    				return filename;
    			return filename+type.getExtension();
    		}
    	}
    	return fieldInputFile.getText() + type.getExtension();
    }
    
    /**
     * Get output writer for specified input file.
     * @param outputFile Output file name.
     * @return Output writer.
     * @throws FileNotFoundException 
     */
    protected Writer getOutputWriter(String outputFile) throws FileNotFoundException {
        File outFile = new File(outputFile);

        // Create necessary directories fore the output path.
        File outFileDir = outFile.getParentFile();
        if (outFileDir != null)
            outFileDir.mkdirs();

        // Create buffered file writer.
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile),
            StandardCharsets.UTF_8));
    }
    
    private void export(FileFormat format) {
    	String outputFile = getOutputFile(format);
        AnnotationExporter exporter = ExporterFactory.createExporter(format);

        try (Writer output = getOutputWriter(outputFile)) {
        	settings.put(Constants.BEGIN, spnrStartPage.getValue());
        	settings.put(Constants.END, spnrEndPage.getValue());
            exporter.export(document, settings, output);
            Dialogs.info("Exported annotations!", false);
        } catch (IOException e) {
            Dialogs.error("Export error", e);
        }
    }
    
    @FXML
    void onExportTextClicked(ActionEvent event) {
    	export(FileFormat.TEXT);
    }
    
    @FXML
    void onExportMarkdownClicked(ActionEvent event) {
    	export(FileFormat.MARKDOWN);
    }
}

