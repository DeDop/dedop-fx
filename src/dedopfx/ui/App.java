/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 by Norman Fomferra (https://github.com/forman) and contributors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dedopfx.ui;

import com.sun.javafx.geom.Rectangle;
import dedopfx.algo.Algorithm;
import dedopfx.algo.AlgorithmInputs;
import dedopfx.audio.*;
import dedopfx.store.PreferencesStore;
import dedopfx.store.PropertiesStore;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class App extends Application {

    public static String NAME = "DeDop FX";
    public static String VERSION = "0.1";

    public static final String DOC_FILE_EXTENSION = ".ddfx";
    public static final FileChooser.ExtensionFilter DOC_EXTENSION_FILTER = new FileChooser.ExtensionFilter("DeDop FX Files", "*" + DOC_FILE_EXTENSION);
    public static final int DEFAULT_INSET_SIZE = 10;

    private final Preferences preferences = Preferences.userNodeForPackage(App.class).node("v" + VERSION);

    private Controller controller;
    private Button playButton;
    private Button stopButton;
    private Label fileLabel;
    private ObservableList<String> recentDocumentFileList;
    private ObservableList<String> recentSourceFileList;
    private Menu loadRecentSourceFileMenu;
    private MenuItem loadSourceFileMenuItem;
    private Stage primaryStage;
    private ProgressBar progressBar;
    private Label progressLabel;
    private Menu openRecentDocumentFileMenu;
    private MenuItem saveMenuItem;
    private MenuItem newMenuItem;
    private MenuItem openMenuItem;

    private byte[] imageData;
    private WritableImage recordImage;
    private ImageView recordImageView;
    private ImageUpdateService imageUpdateService;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        if (System.getProperty("dedopfx.clearPrefs", "false").equalsIgnoreCase("true")) {
            clearPreferences();
        }

        this.primaryStage = primaryStage;

        imageUpdateService = new ImageUpdateService();
        imageUpdateService.onSucceededProperty().setValue(event -> {
            PixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteRgbInstance();
            recordImage.getPixelWriter().setPixels(0, 0,
                    (int) recordImage.getWidth(), (int) recordImage.getHeight(), pixelFormat,
                    imageData, 0, (int) recordImage.getWidth() * 3);
        });

        Algorithm.RecordObserver recordObserver = (recordIndex, recordCount, inputSamples) ->
                Platform.runLater(() -> {
                    updateProgress(recordIndex, recordCount);
                    updateRecordImage(inputSamples);
                });

        controller = new Controller(recordObserver);
        controller.getAlgorithmInputs().fromStore(new PreferencesStore(preferences.node("input")));

        ChangeListener<File> sourceFileListener = (observable, oldValue, newValue) -> {
            if (oldValue != null) {
                updateRecentSourceFileList(oldValue);
            }
            updateSourceFileLabel();
        };
        controller.getAlgorithmInputs().sourceFileProperty().addListener(sourceFileListener);

        controller.getAlgorithmInputs().sourceValuesProperty().addListener((observable, oldValue, newValue) -> {
            updateSourceFileLabel();
        });

        ChangeListener<Worker.State> enabledStateUpdater = (observable, oldValue, newValue) -> Platform.runLater(this::updateEnabledState);
        controller.getLoadSourceFileService().stateProperty().addListener(enabledStateUpdater);
        controller.getPlayService().stateProperty().addListener(enabledStateUpdater);

        ChangeListener<File> documentFileListener = (observable, oldValue, newValue) -> {
            if (oldValue != null) {
                updateRecentDocumentFileList(oldValue);
            }
            updateTitle();
            updateEnabledState();
        };
        controller.documentFileProperty().addListener(documentFileListener);

        Menu fileMenu = createFileMenu();
        Menu helpMenu = createHelpMenu();
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, helpMenu);
        if (System.getProperty("os.name", "").contains("Mac")) {
            menuBar.useSystemMenuBarProperty().set(true);
        }

        recentDocumentFileList = FXCollections.observableArrayList(getRecentDocumentFileList());
        //noinspection Convert2Lambda
        recentDocumentFileList.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> change) {
                updateOpenRecentDocumentFileMenu();
            }
        });
        updateOpenRecentDocumentFileMenu();

        recentSourceFileList = FXCollections.observableArrayList(getRecentSourceFileList());
        //noinspection Convert2Lambda
        recentSourceFileList.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> change) {
                updateOpenRecentSourceFileMenu();
            }
        });
        updateOpenRecentSourceFileMenu();

        playButton = new Button("Play");
        playButton.setOnAction((event) -> controller.getPlayService().restart());

        stopButton = new Button("Stop");
        stopButton.setOnAction((event) -> controller.getPlayService().cancel());

        fileLabel = new Label();
        updateSourceFileLabel();

        Tab playbackSettingsTab = new Tab("Playback", createPlaybackSettingsPane());
        playbackSettingsTab.setTooltip(new Tooltip("Settings that control the playback of the generated audio signal"));
        playbackSettingsTab.setClosable(false);
        Tab sourceMappingSettingsTab = new Tab("Source Mapping", createSourceMappingSettingsPane());
        sourceMappingSettingsTab.setTooltip(new Tooltip("Settings that control the mapping from source data to amplitudes and pitches"));
        sourceMappingSettingsTab.setClosable(false);
        Tab soundSynthesisSettingsTab = new Tab("Sound Synthesis", createSoundSynthesisSettingsPane());
        soundSynthesisSettingsTab.setTooltip(new Tooltip("Settings that control the timbre of the generated sound"));
        soundSynthesisSettingsTab.setClosable(false);

        TabPane settingsTabPane = new TabPane(playbackSettingsTab, sourceMappingSettingsTab, soundSynthesisSettingsTab);

        progressBar = new ProgressBar();
        progressBar.setPrefWidth(200);
        progressLabel = new Label();

        recordImageView = new ImageView();
        recordImageView.setFitHeight(128);

        StackPane recordImageViewPane = new StackPane();
        recordImageViewPane.setAlignment(Pos.TOP_LEFT);
        recordImageViewPane.getChildren().add(recordImageView);

        HBox recordImageViewBox = new HBox();
        recordImageViewBox.setPadding(new Insets(DEFAULT_INSET_SIZE));
        recordImageViewBox.getChildren().add(recordImageViewPane);

        // This is a hack to make the recordImageViewPane never larger than the recordImageViewBox
        DoubleBinding halfWidth = recordImageViewBox.widthProperty().divide(2);
        recordImageViewPane.minWidthProperty().bind(halfWidth);
        recordImageViewPane.maxWidthProperty().bind(halfWidth);
        recordImageViewPane.prefWidthProperty().bind(halfWidth);
        recordImageView.fitWidthProperty().bind(recordImageViewBox.widthProperty().subtract(2 * DEFAULT_INSET_SIZE));

        HBox progressBox = new HBox();
        progressBox.setPadding(new Insets(DEFAULT_INSET_SIZE));
        progressBox.setSpacing(DEFAULT_INSET_SIZE / 2);
        progressBox.getChildren().addAll(progressBar, progressLabel);

        HBox buttonBox = new HBox();
        buttonBox.setPadding(new Insets(DEFAULT_INSET_SIZE));
        buttonBox.setSpacing(DEFAULT_INSET_SIZE);
        buttonBox.getChildren().addAll(playButton, stopButton);

        AnchorPane bottomAnchorPane = new AnchorPane();
        bottomAnchorPane.getChildren().addAll(recordImageViewBox, progressBox, buttonBox);
        AnchorPane.setTopAnchor(recordImageViewBox, 5.0);
        AnchorPane.setLeftAnchor(recordImageViewBox, 5.0);
        AnchorPane.setRightAnchor(recordImageViewBox, 5.0);
        AnchorPane.setBottomAnchor(recordImageViewBox, 40.0);
        AnchorPane.setBottomAnchor(progressBox, 5.0);
        AnchorPane.setLeftAnchor(progressBox, 5.0);
        AnchorPane.setBottomAnchor(buttonBox, 5.0);
        AnchorPane.setRightAnchor(buttonBox, 5.0);

        HBox hBox = new HBox(fileLabel);
        hBox.setPadding(new Insets(5, 5, 5, 5));

        BorderPane borderPane0 = new BorderPane();
        borderPane0.setTop(hBox);
        borderPane0.setCenter(settingsTabPane);
        // borderPane0.setBottom();

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(menuBar);
        borderPane.setCenter(borderPane0);
        borderPane.setBottom(bottomAnchorPane);

        Scene scene = new Scene(borderPane, 480, 520);
        updateEnabledState();

        updateTitle();
        primaryStage.getIcons().addAll(IntStream
                .of(16, 24, 32, 48, 64, 128, 210, 256)
                .mapToObj(value -> new Image(String.format("dedopfx/resources/dedop-%d.png", value)))
                .toArray(Image[]::new));
        primaryStage.setScene(scene);
        Rectangle windowRectangle = getWindowRectangle();
        if (windowRectangle != null) {
            primaryStage.setX(windowRectangle.x);
            primaryStage.setY(windowRectangle.y);
            primaryStage.setWidth(windowRectangle.width);
            primaryStage.setHeight(windowRectangle.height);
        } else {
            primaryStage.centerOnScreen();
        }
        primaryStage.show();
    }

    private Node createSourceMappingSettingsPane() {
        GridPane settingsPane = createSettingsGridPane();

        int rowIndex = -1;

        DoubleProperty minSampleProperty = controller.getAlgorithmInputs().minSourceValueProperty();
        DoubleProperty maxSampleProperty = controller.getAlgorithmInputs().maxSourceValueProperty();
        InputFieldWithSlider minSource = new InputFieldWithSliderDoubleBase10("Minimum source value",
                AlgorithmInputs.DEFAULT_MIN_SOURCE_VALUE,
                AlgorithmInputs.DEFAULT_MAX_SOURCE_VALUE,
                minSampleProperty, "%.0f");
        minSource.addToGrid(settingsPane, ++rowIndex);

        InputFieldWithSlider maxSourceValue = new InputFieldWithSliderDoubleBase10("Maximum source value",
                AlgorithmInputs.DEFAULT_MIN_SOURCE_VALUE,
                AlgorithmInputs.DEFAULT_MAX_SOURCE_VALUE,
                maxSampleProperty, "%.0f");
        maxSourceValue.addToGrid(settingsPane, ++rowIndex);

        DoubleProperty amplitudeWeightingProperty = controller.getAlgorithmInputs().amplitudeWeightingProperty();
        InputFieldWithSlider amplitudeWeighting = new InputFieldWithSliderDouble("Amplitude weighting",
                0.0,
                1.0,
                amplitudeWeightingProperty, "%.2f");
        amplitudeWeighting.addToGrid(settingsPane, ++rowIndex);

        ++rowIndex;
        Property<TuningSystem> tuningSystemProperty = controller.getAlgorithmInputs().tuningSystemProperty();
        ChoiceBox<TuningSystem> tuningSystemChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(TuningSystem.values()));
        tuningSystemChoiceBox.valueProperty().bindBidirectional(tuningSystemProperty);
        settingsPane.add(new Label("Tuning system"), 0, rowIndex, 1, 1);
        settingsPane.add(tuningSystemChoiceBox, 2, rowIndex, 1, 1);

        DoubleProperty minFrequencyProperty = controller.getAlgorithmInputs().minFrequencyProperty();
        InputFieldWithSlider minFrequency = new InputFieldWithSliderDoubleBase2("Minimum frequency (Hz)",
                20.0,
                16000.0,
                minFrequencyProperty, "%.1f");
        minFrequency.addToGrid(settingsPane, ++rowIndex);

        DoubleProperty maxFrequencyProperty = controller.getAlgorithmInputs().maxFrequencyProperty();
        InputFieldWithSlider maxFrequency = new InputFieldWithSliderDoubleBase2("Maximum frequency (Hz)",
                20.0,
                16000.0,
                maxFrequencyProperty, "%.1f");

        IntegerProperty octaveSubdivisionsProperty = controller.getAlgorithmInputs().octaveSubdivisionCountProperty();
        InputFieldWithSlider octaveSubdivisions = new InputFieldWithSliderInteger("Octave subdivisions",
                1,
                128,
                octaveSubdivisionsProperty);

        final int extraRowIndex = ++rowIndex;
        Runnable installTuningSystem = () -> {
            TuningSystem tuningSystem = tuningSystemProperty.getValue();
            octaveSubdivisions.removeFromGrid(settingsPane);
            maxFrequency.removeFromGrid(settingsPane);
            if (tuningSystem == TuningSystem.LINEAR) {
                maxFrequency.addToGrid(settingsPane, extraRowIndex);
            } else {
                octaveSubdivisions.addToGrid(settingsPane, extraRowIndex);
            }
        };
        installTuningSystem.run();
        tuningSystemProperty.addListener((observable, oldValue, newValue) -> Platform.runLater(installTuningSystem));

        return settingsPane;
    }

    private Node createSoundSynthesisSettingsPane() {
        GridPane settingsPane = createSettingsGridPane();

        int rowIndex = 0;
        Property<Waveform> carrierWaveformProperty = controller.getAlgorithmInputs().carrierWaveformProperty();
        ChoiceBox<Waveform> carrierWaveformChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(Waveform.WAVEFORMS));
        carrierWaveformChoiceBox.valueProperty().bindBidirectional(carrierWaveformProperty);
        settingsPane.add(new Label("Carrier waveform"), 0, rowIndex, 1, 1);
        settingsPane.add(carrierWaveformChoiceBox, 2, rowIndex, 1, 1);

        rowIndex++;
        Property<Harmonics> harmonicsModeProperty = controller.getAlgorithmInputs().harmonicsModeProperty();
        ChoiceBox<Harmonics> harmonicsModeBox = new ChoiceBox<>(FXCollections.observableArrayList(Harmonics.values()));
        harmonicsModeBox.valueProperty().bindBidirectional(harmonicsModeProperty);
        settingsPane.add(new Label("Harmonics mode"), 0, rowIndex, 1, 1);
        settingsPane.add(harmonicsModeBox, 2, rowIndex, 1, 1);

        rowIndex++;
        IntegerProperty timbreComplexityProperty = controller.getAlgorithmInputs().partialCountProperty();
        InputFieldWithSlider timbreComplexity = new InputFieldWithSliderInteger("Number of partials",
                2,
                16,
                timbreComplexityProperty);
        timbreComplexity.addToGrid(settingsPane, rowIndex);

        rowIndex++;
        BooleanProperty useModulationProperty = controller.getAlgorithmInputs().modulationEnabledProperty();
        CheckBox useModulationCheckBox = new CheckBox("Enable frequency modulation (FM)");
        useModulationCheckBox.selectedProperty().bindBidirectional(useModulationProperty);
        settingsPane.add(useModulationCheckBox, 0, rowIndex, 3, 1);

        rowIndex++;
        Property<Waveform> modulationWaveformProperty = controller.getAlgorithmInputs().modulationWaveformProperty();
        ChoiceBox<Waveform> modulationWaveformChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(Waveform.WAVEFORMS));
        modulationWaveformChoiceBox.valueProperty().bindBidirectional(modulationWaveformProperty);
        settingsPane.add(new Label("Modulation waveform"), 0, rowIndex, 1, 1);
        settingsPane.add(modulationWaveformChoiceBox, 2, rowIndex, 1, 1);

        rowIndex++;
        DoubleProperty modulationIndexProperty = controller.getAlgorithmInputs().modulationDepthProperty();
        InputFieldWithSlider modulationIndex = new InputFieldWithSliderDoubleBase10("Modulation depth",
                0.001,
                10.,
                modulationIndexProperty, "%.3f");
        modulationIndex.addToGrid(settingsPane, rowIndex);

        rowIndex++;
        IntegerProperty modulationNomProperty = controller.getAlgorithmInputs().modulationNomProperty();
        InputFieldWithSlider modulationNom = new InputFieldWithSliderInteger("Modulation freq. nom.",
                1,
                16,
                modulationNomProperty);
        modulationNom.addToGrid(settingsPane, rowIndex);

        rowIndex++;
        IntegerProperty modulationDenomProperty = controller.getAlgorithmInputs().modulationDenomProperty();
        InputFieldWithSlider modulationDenom = new InputFieldWithSliderInteger("Modulation freq. denom.",
                1,
                32,
                modulationDenomProperty);
        modulationDenom.addToGrid(settingsPane, rowIndex);

        return settingsPane;
    }

    private Node createPlaybackSettingsPane() {
        GridPane settingsPane = createSettingsGridPane();

        int rowIndex = 0;
        IntegerProperty minRecordIndexProperty = controller.getAlgorithmInputs().minRecordIndexProperty();
        InputFieldWithSlider minRecordIndex = new InputFieldWithSliderInteger("Minimum position",
                0,
                1,
                minRecordIndexProperty);
        minRecordIndex.addToGrid(settingsPane, ++rowIndex);

        IntegerProperty maxRecordIndexProperty = controller.getAlgorithmInputs().maxRecordIndexProperty();
        InputFieldWithSlider maxRecordIndex = new InputFieldWithSliderInteger("Maximum position",
                0,
                1,
                maxRecordIndexProperty);
        maxRecordIndex.addToGrid(settingsPane, ++rowIndex);

        controller.getAlgorithmInputs().sourceValuesProperty().addListener((observable, oldValue, newValue) -> {
            int newMax = newValue != null ? newValue.length - 1 : 1;
            minRecordIndex.getSlider().setMax(newMax);
            maxRecordIndex.getSlider().setMax(newMax);
            minRecordIndexProperty.setValue(0);
            maxRecordIndexProperty.setValue(newMax);
        });

        IntegerProperty velocityProperty = controller.getAlgorithmInputs().velocityProperty();
        InputFieldWithSlider velocity = new InputFieldWithSliderInteger("Velocity",
                1,
                32,
                velocityProperty);
        velocity.addToGrid(settingsPane, ++rowIndex);

        DoubleProperty gainProperty = controller.getAlgorithmInputs().gainProperty();
        InputFieldWithSlider gain = new InputFieldWithSliderDoubleBase10("Gain",
                0.01,
                1000.0,
                gainProperty, "%.3f");
        gain.addToGrid(settingsPane, ++rowIndex);

        return settingsPane;
    }

    private GridPane createSettingsGridPane() {
        ColumnConstraints column1 = new ColumnConstraints();
        ColumnConstraints column2 = new ColumnConstraints();
        ColumnConstraints column3 = new ColumnConstraints();

        column2.setHgrow(Priority.ALWAYS);

        GridPane gridPane = new GridPane();
        gridPane.getColumnConstraints().addAll(column1, column2, column3);
        gridPane.setAlignment(Pos.TOP_LEFT);
        gridPane.setHgap(8);
        gridPane.setVgap(4);
        gridPane.setPadding(new Insets(DEFAULT_INSET_SIZE));
        return gridPane;
    }

    private void updateRecordImage(double[] inputSamples) {
        if (imageUpdateService.getState() == Worker.State.READY || imageUpdateService.getState() == Worker.State.SUCCEEDED) {
            imageUpdateService.reset();
            imageUpdateService.setInputSamples(inputSamples);
            imageUpdateService.start();
        }
    }

    private void updateProgress(int recordIndex, int recordCount) {
        double progress = recordIndex / (double) recordCount;
        progressBar.setProgress(progress);
        progressLabel.setText(String.format("%d of %d (%.0f%%)", recordIndex, recordCount, progress * 100.));
    }

    private void updateTitle() {
        File docFile = controller.getDocumentFile();
        String docFileInfo;
        if (docFile != null) {
            docFileInfo = docFile.getPath();
        } else {
            docFileInfo = "<New>";
        }
        primaryStage.setTitle(String.format("%s %s - %s", NAME, VERSION, docFileInfo));
    }

    private Menu createHelpMenu() {
        Menu helpMenu = new Menu("Help");
        MenuItem helpMenuItem1 = createLinkMenuItem("DeDop Website", "http://dedop.org/");
        MenuItem helpMenuItem2 = createLinkMenuItem("DeDop on GitHub", "https://github.com/DeDop");
        MenuItem helpMenuItem3 = createLinkMenuItem("S-3 Altimetry Test Data Set", "https://sentinel.esa.int/web/sentinel/user-guides/sentinel-3-altimetry/test-data-set");
        MenuItem aboutMenuItem = new MenuItem("About " + NAME);
        aboutMenuItem.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About " + NAME);
            alert.setHeaderText("DeDopFX is a sound generator that operates on satellite altimetry\n" +
                    "data and has been developed just for fun.");
            alert.setContentText("Copyright (c) 2017 by Norman Fomferra.\n" +
                    "This software is free and distributed under the terms an conditions of \n" +
                    "the MIT license.");
            alert.showAndWait();
        });
        helpMenu.getItems().addAll(
                helpMenuItem1,
                helpMenuItem2,
                helpMenuItem3,
                new SeparatorMenuItem(),
                aboutMenuItem);
        return helpMenu;
    }

    private Menu createFileMenu() {

        newMenuItem = new MenuItem("New");
        newMenuItem.setOnAction(t -> {
            controller.getAlgorithmInputs().setDefaults();
            updateEnabledState();
        });

        openMenuItem = new MenuItem("Open...");
        openMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        openMenuItem.setOnAction(t -> {
            String initialDirectoryPath = preferences.get("lastDocumentDirectory", System.getProperty("user.home"));
            final FileChooser fileChooser = createOpenDocumentFileChooser(initialDirectoryPath);
            final File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                openDocument(file);
                preferences.put("lastDocumentDirectory", file.getParent() != null ? file.getParent() : "");
            }
        });

        openRecentDocumentFileMenu = new Menu("Open Recent");

        saveMenuItem = new MenuItem("Save");
        saveMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        saveMenuItem.setOnAction(event -> saveDocument());

        MenuItem saveAsMenuItem = new MenuItem("Save As...");
        saveAsMenuItem.setOnAction(t -> saveDocumentAs());

        loadSourceFileMenuItem = new MenuItem("Load Source File...");
        loadSourceFileMenuItem.setOnAction(t -> loadSourceFile());

        loadRecentSourceFileMenu = new Menu("Load Recent Source File");

        MenuItem quitMenuItem = new MenuItem("Quit");
        quitMenuItem.setOnAction(t -> Platform.exit());

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(
                newMenuItem,
                openMenuItem,
                openRecentDocumentFileMenu,
                new SeparatorMenuItem(),
                loadSourceFileMenuItem,
                loadRecentSourceFileMenu,
                new SeparatorMenuItem(),
                saveMenuItem,
                saveAsMenuItem,
                new SeparatorMenuItem(),
                quitMenuItem);

        return fileMenu;
    }

    private void clearPreferences() {
        try {
            preferences.node("input").clear();
            preferences.clear();
            preferences.sync();
        } catch (BackingStoreException e) {
            // ok
        }
    }

    private void loadSourceFile() {
        String initialDirectoryPath = preferences.get("lastSourceDirectory", System.getProperty("user.home"));
        final FileChooser fileChooser = createLoadSourceFileChooser(initialDirectoryPath);
        final File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            loadSourceFile(file);
            preferences.put("lastSourceDirectory", file.getParent() != null ? file.getParent() : "");
        }
    }

    private void openDocument(File file) {
        file = ensureDocExtension(file);
        if (!checkFile(file, recentDocumentFileList)) {
            return;
        }
        Properties properties = new Properties();
        try {
            try (FileReader reader = new FileReader(file)) {
                properties.load(reader);
                controller.getAlgorithmInputs().fromStore(new PropertiesStore(properties));
                controller.setDocumentFile(file);

                File sourceFile = controller.getAlgorithmInputs().getSourceFile();
                if (sourceFile != null) {
                    loadSourceFile(sourceFile);
                }
            }
        } catch (IOException e) {
            ExceptionDialog.showError(String.format("Failed to open file '%s'", file), e);
        }
    }

    private void saveDocument() {
        if (controller.getDocumentFile() != null) {
            saveDocument(controller.getDocumentFile());
        } else {
            saveDocumentAs();
        }
    }

    private void saveDocument(File file) {
        file = ensureDocExtension(file);
        Properties properties = new Properties();
        controller.getAlgorithmInputs().toStore(new PropertiesStore(properties));
        try {
            try (FileWriter writer = new FileWriter(file)) {
                properties.store(writer, String.format("%s %s Document", NAME, VERSION));
                controller.setDocumentFile(file);
            }
        } catch (IOException e) {
            ExceptionDialog.showError(String.format("Failed to save file '%s'", file), e);
        }
    }

    private void saveDocumentAs() {
        String initialDirectoryPath = preferences.get("lastDocumentDirectory", System.getProperty("user.home"));
        final FileChooser fileChooser = createSaveDocumentAsFileChooser(initialDirectoryPath);
        final File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            saveDocument(file);
            preferences.put("lastDocumentDirectory", file.getParent() != null ? file.getParent() : "");
        }
    }

    private File ensureDocExtension(File file) {
        if (!file.getName().endsWith(DOC_FILE_EXTENSION)) {
            return new File(file.getParentFile(), file.getName() + DOC_FILE_EXTENSION);
        }
        return file;
    }

    private MenuItem createLinkMenuItem(String text, String urlStr) {
        MenuItem menuItem = new MenuItem(text);
        menuItem.setOnAction(event -> {
            try {
                java.awt.Desktop.getDesktop().browse(URI.create(urlStr));
            } catch (IOException e) {
                // ?
            }
        });
        return menuItem;
    }

    @Override
    public void stop() {
        putWindowRectangle(new Rectangle(
                (int) this.primaryStage.getX(),
                (int) this.primaryStage.getY(),
                (int) this.primaryStage.getWidth(),
                (int) this.primaryStage.getHeight()));


        if (controller.getDocumentFile() != null) {
            updateRecentDocumentFileList(controller.getDocumentFile());
        }
        if (controller.getAlgorithmInputs().getSourceFile() != null) {
            updateRecentSourceFileList(controller.getAlgorithmInputs().getSourceFile());
        }

        controller.getAlgorithmInputs().setSourceFile(null);
        controller.getAlgorithmInputs().toStore(new PreferencesStore(preferences.node("input")));

        try {
            preferences.sync();
        } catch (BackingStoreException e) {
            // ok
        }
    }

    private interface FileOpenHandler {
        void openFile(File file);
    }

    private void updateRecentDocumentFileList(File file) {
        updateRecentFileList(recentDocumentFileList, file);
        putRecentFileList("recentDocumentFileList", recentDocumentFileList);
    }

    private void updateRecentSourceFileList(File file) {
        updateRecentFileList(recentSourceFileList, file);
        putRecentFileList("recentSourceFileList", recentSourceFileList);
    }

    private void updateRecentFileList(ObservableList<String> list, File file) {
        int index = list.indexOf(file.getPath());
        if (index < 0) {
            list.add(0, file.getPath());
        } else if (index > 0) {
            list.remove(index);
            list.add(0, file.getPath());
        }
    }

    private void updateOpenRecentDocumentFileMenu() {
        updateRecentFileMenu(openRecentDocumentFileMenu, recentDocumentFileList, this::openDocument);
    }

    private void updateOpenRecentSourceFileMenu() {
        updateRecentFileMenu(loadRecentSourceFileMenu, recentSourceFileList, this::loadSourceFile);
    }

    private void updateRecentFileMenu(Menu menu, ObservableList<String> recentFileList, FileOpenHandler handler) {
        menu.getItems().setAll(recentFileList.stream().map(path -> {
            MenuItem menuItem = new MenuItem(path);
            menuItem.setOnAction(event -> handler.openFile(new File(path)));
            return menuItem;
        }).collect(Collectors.toList()));
    }

    private void loadSourceFile(File file) {
        if (file == null) {
            return;
        }
        if (!checkFile(file, recentSourceFileList)) {
            return;
        }
        Controller.LoadSourceFileService service = controller.getLoadSourceFileService();
        if (service.getState() == Worker.State.RUNNING || service.getState() == Worker.State.SCHEDULED) {
            throw new IllegalStateException();
        }
        if (file.equals(service.getSourceFile())) {
            return;
        }
        service.reset();
        service.setSourceFile(file);
        service.setOnSucceeded(workerStateEvent -> controller.getAlgorithmInputs().setSourceFile(file));
        service.setOnFailed(workerStateEvent -> {
            ExceptionDialog.showError("A problem occurred while opening the source data file",
                    workerStateEvent.getSource().getException());
            controller.getAlgorithmInputs().setSourceFile(null);
        });
        service.start();
    }

    private boolean checkFile(File file, ObservableList<String> recentFileList) {
        if (!file.exists() || !file.isFile()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Invalid source data file selected.");
            alert.setContentText(String.format("File '%s'\n" +
                    "does not exists or is invalid.", file));
            alert.showAndWait();
            recentFileList.remove(file.getPath());
            return false;
        }
        return true;
    }

    private void updateSourceFileLabel() {
        File sourceFile = controller.getAlgorithmInputs().getSourceFile();
        if (sourceFile != null) {
            double[][] sourceValues = controller.getAlgorithmInputs().getSourceValues();
            if (sourceValues != null) {
                fileLabel.setText(String.format("%s (%d records)", sourceFile.getPath(), sourceValues.length));
            } else {
                fileLabel.setText(String.format("%s (not loaded)", sourceFile.getPath()));
            }
        } else {
            fileLabel.setText("No source data file selected. Use 'Open' from the 'File' menu.");
        }
    }

    private Rectangle getWindowRectangle() {
        int invalid = Integer.MAX_VALUE;
        int windowX = preferences.getInt("windowX", invalid);
        int windowY = preferences.getInt("windowY", invalid);
        int windowWidth = preferences.getInt("windowWidth", invalid);
        int windowHeight = preferences.getInt("windowHeight", invalid);
        if (windowX != invalid && windowY != invalid && windowWidth != invalid && windowHeight != invalid) {
            return new Rectangle(windowX, windowY, windowWidth, windowHeight);
        }
        return null;
    }

    private void putWindowRectangle(Rectangle rectangle) {
        preferences.putInt("windowX", rectangle.x);
        preferences.putInt("windowY", rectangle.y);
        preferences.putInt("windowWidth", rectangle.width);
        preferences.putInt("windowHeight", rectangle.height);
    }

    private List<String> getRecentDocumentFileList() {
        return getRecentFileList("recentDocumentFileList");
    }

    private List<String> getRecentSourceFileList() {
        return getRecentFileList("recentSourceFileList");
    }

    private List<String> getRecentFileList(String key) {
        String value = preferences.get(key, null);
        if (value != null && !value.trim().isEmpty()) {
            return Arrays.asList(value.split(File.pathSeparator));
        } else {
            return new ArrayList<>();
        }
    }

    private void putRecentFileList(String key, List<String> recentFileList) {
        preferences.put(key, recentFileList.stream().collect(Collectors.joining(File.pathSeparator)));
    }

    private void updateEnabledState() {
        boolean hasSamples = controller.getAlgorithmInputs().hasSourceValues();
        boolean isLoadingSource = isScheduledOrRunning(controller.getLoadSourceFileService().getState());
        boolean isPlaying = isScheduledOrRunning(controller.getPlayService().getState());

        boolean canOpen = !isLoadingSource && !isPlaying;
        boolean canOpenRecentDocument = canOpen && !recentDocumentFileList.isEmpty();
        boolean canOpenRecentSource = canOpen && !recentSourceFileList.isEmpty();
        boolean canSave = controller.getDocumentFile() != null;
        boolean canPlay = hasSamples && !isLoadingSource && !isPlaying;

        newMenuItem.disableProperty().setValue(!canOpen);
        openMenuItem.disableProperty().setValue(!canOpen);
        openRecentDocumentFileMenu.disableProperty().setValue(!canOpenRecentDocument);
        saveMenuItem.disableProperty().setValue(!canSave);
        loadSourceFileMenuItem.disableProperty().setValue(!canOpen);
        loadRecentSourceFileMenu.disableProperty().setValue(!canOpenRecentSource);
        progressBar.disableProperty().setValue(!isPlaying);
        playButton.disableProperty().setValue(!canPlay);
        stopButton.disableProperty().setValue(!isPlaying);

        if (canPlay) {
            playButton.requestFocus();
        }
    }

    private static boolean isScheduledOrRunning(Worker.State state) {
        return state == Worker.State.SCHEDULED || state == Worker.State.RUNNING;
    }

    private FileChooser createOpenDocumentFileChooser(String initialDirectoryPath) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open");
        fileChooser.setInitialDirectory(new File(initialDirectoryPath));
        fileChooser.getExtensionFilters().addAll(DOC_EXTENSION_FILTER);
        return fileChooser;
    }

    private FileChooser createSaveDocumentAsFileChooser(String initialDirectoryPath) {
        File documentFile = controller.getDocumentFile();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As");
        fileChooser.setInitialDirectory(documentFile != null ? documentFile.getParentFile() : new File(initialDirectoryPath));
        fileChooser.setInitialFileName(documentFile != null ? documentFile.getName() : null);
        fileChooser.getExtensionFilters().addAll(DOC_EXTENSION_FILTER);
        return fileChooser;
    }

    private FileChooser createLoadSourceFileChooser(String initialDirectoryPath) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Source File");
        fileChooser.setInitialDirectory(new File(initialDirectoryPath));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("S-3 SRAL L1B Files", "*.nc"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        return fileChooser;
    }

    private class ImageUpdateService extends Service<Void> {
        private double[] inputSamples;

        public ImageUpdateService() {
        }

        public void setInputSamples(double[] inputSamples) {
            this.inputSamples = inputSamples;
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    int imageWidth = inputSamples.length;
                    int imageHeight = 128;
                    if (imageData == null || imageData.length != imageWidth * imageHeight * 3) {
                        recordImage = new WritableImage(imageWidth, imageHeight);
                        imageData = new byte[imageWidth * imageHeight * 3];
                        // System.out.println("imageWidth = " + imageWidth);
                        recordImageView.setImage(recordImage);
                        // System.out.println("recordImageView = " + recordImageView.getFitWidth());
                    }

                    int decay = 2;
                    for (int k = 0; k < imageWidth * imageHeight * 3; k++) {
                        int v = imageData[k] & 0xff;
                        if (v - decay >= 0) {
                            imageData[k] = (byte) (v - decay);
                        } else {
                            imageData[k] = 0;
                        }
                    }

                    for (int y = 1; y < imageHeight; y++) {
                        final int j1 = y * imageWidth;
                        for (int x = 0; x < imageWidth; x++) {
                            final int i1 = 3 * (j1 + x);
                            final int i0 = i1 - 3 * imageWidth;
                            imageData[i0] = imageData[i1];
                            imageData[i0 + 1] = imageData[i1 + 1];
                            imageData[i0 + 2] = imageData[i1 + 1];
                        }
                    }

                    for (int x = 0; x < inputSamples.length; x++) {
                        double inputSample = inputSamples[x];
                        int h = (int) (imageHeight * inputSample);
                        if (h < 0) {
                            h = 0;
                        }
                        if (h > imageHeight - 1) {
                            h = imageHeight - 1;
                        }
                        for (int y = imageHeight - h; y < imageHeight; y++) {
                            int k = (y * imageWidth + x) * 3;
                            imageData[k] = (byte) 255;
                            imageData[k + 1] = (byte) 255;
                            imageData[k + 2] = 0;
                        }
                    }

                    return null;
                }
            };
        }
    }
}


