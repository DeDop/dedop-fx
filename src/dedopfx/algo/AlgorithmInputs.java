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

package dedopfx.algo;

import dedopfx.audio.Harmonics;
import dedopfx.audio.TuningSystem;
import dedopfx.audio.Waveform;
import dedopfx.store.Store;
import javafx.beans.property.*;

import java.io.File;

public class AlgorithmInputs {

    public static final int VERSION = 1;
    public static final File DEFAULT_SOURCE_FILE = null;
    public static final double DEFAULT_MIN_SOURCE_VALUE = 1;
    public static final double DEFAULT_MAX_SOURCE_VALUE = 1e9;
    public static final int DEFAULT_MIN_RECORD_INDEX = 0;
    public static final int DEFAULT_MAX_RECORD_INDEX = -1;
    public static final double DEFAULT_GAIN = 0.1;
    public static final int DEFAULT_VELOCITY = 16;
    public static final Waveform DEFAULT_WAVEFORM = Waveform.TRIANGLE;
    public static final TuningSystem DEFAULT_TUNING_SYSTEM = TuningSystem.LINEAR;
    public static final double DEFAULT_MIN_FREQUENCY = 0.5 * 440;
    public static final double DEFAULT_MAX_FREQUENCY = 10 * 440;
    public static final int DEFAULT_OCTAVE_SUBDIVISION_COUNT = 12;
    public static final double DEFAULT_AMPLITUDE_SUM_RATIO = 1.0;
    public static final Harmonics DEFAULT_HARMONICS_MODE = Harmonics.OFF;
    public static final int DEFAULT_PARTIALS_COUNT = 2;
    public static final boolean DEFAULT_MODULATION_ENABLED = false;
    public static final double DEFAULT_MODULATION_DEPTH = 0.1;
    public static final int DEFAULT_MODULATION_NOM = 1;
    public static final int DEFAULT_MODULATION_DENOM = 8;

    private final Property<double[][]> sourceValues = new SimpleObjectProperty<>(null);
    private final DoubleProperty minContainedSourceValue = new SimpleDoubleProperty(0);
    private final DoubleProperty maxContainedSourceValue = new SimpleDoubleProperty(-1);

    private final Property<File> sourceFile = new SimpleObjectProperty<>(DEFAULT_SOURCE_FILE);
    private final DoubleProperty minSourceValue = new SimpleDoubleProperty(DEFAULT_MIN_SOURCE_VALUE);
    private final DoubleProperty maxSourceValue = new SimpleDoubleProperty(DEFAULT_MAX_SOURCE_VALUE);
    private final IntegerProperty minRecordIndex = new SimpleIntegerProperty(DEFAULT_MIN_RECORD_INDEX);
    private final IntegerProperty maxRecordIndex = new SimpleIntegerProperty(DEFAULT_MAX_RECORD_INDEX);
    private final DoubleProperty gain = new SimpleDoubleProperty(DEFAULT_GAIN);
    private final IntegerProperty velocity = new SimpleIntegerProperty(DEFAULT_VELOCITY);
    private final Property<TuningSystem> tuningSystem = new SimpleObjectProperty<>(DEFAULT_TUNING_SYSTEM);
    private final DoubleProperty minFrequency = new SimpleDoubleProperty(DEFAULT_MIN_FREQUENCY);
    private final DoubleProperty maxFrequency = new SimpleDoubleProperty(DEFAULT_MAX_FREQUENCY);
    private final IntegerProperty octaveSubdivisionCount = new SimpleIntegerProperty(DEFAULT_OCTAVE_SUBDIVISION_COUNT);
    private final DoubleProperty amplitudeWeighting = new SimpleDoubleProperty(DEFAULT_AMPLITUDE_SUM_RATIO);
    private final Property<Waveform> carrierWaveform = new SimpleObjectProperty<>(DEFAULT_WAVEFORM);
    private final Property<Waveform> modulationWaveform = new SimpleObjectProperty<>(DEFAULT_WAVEFORM);
    private final Property<Harmonics> harmonicsMode = new SimpleObjectProperty<>(DEFAULT_HARMONICS_MODE);
    private final IntegerProperty partialCount = new SimpleIntegerProperty(DEFAULT_PARTIALS_COUNT);
    private final BooleanProperty modulationEnabled = new SimpleBooleanProperty(DEFAULT_MODULATION_ENABLED);
    private final DoubleProperty modulationDepth = new SimpleDoubleProperty(DEFAULT_MODULATION_DEPTH);
    private final IntegerProperty modulationNom = new SimpleIntegerProperty(DEFAULT_MODULATION_NOM);
    private final IntegerProperty modulationDenom = new SimpleIntegerProperty(DEFAULT_MODULATION_DENOM);

    public void setDefaults() {
        sourceValues.setValue(null);
        minContainedSourceValue.setValue(0.);
        maxContainedSourceValue.setValue(1.);

        sourceFile.setValue(DEFAULT_SOURCE_FILE);
        minSourceValue.setValue(DEFAULT_MIN_SOURCE_VALUE);
        maxSourceValue.setValue(DEFAULT_MAX_SOURCE_VALUE);
        minRecordIndex.setValue(DEFAULT_MIN_RECORD_INDEX);
        maxRecordIndex.setValue(DEFAULT_MAX_RECORD_INDEX);
        gain.setValue(DEFAULT_GAIN);
        velocity.setValue(DEFAULT_VELOCITY);
        amplitudeWeighting.setValue(DEFAULT_AMPLITUDE_SUM_RATIO);
        tuningSystem.setValue(DEFAULT_TUNING_SYSTEM);
        minFrequency.setValue(DEFAULT_MIN_FREQUENCY);
        maxFrequency.setValue(DEFAULT_MAX_FREQUENCY);
        octaveSubdivisionCount.setValue(DEFAULT_OCTAVE_SUBDIVISION_COUNT);
        carrierWaveform.setValue(DEFAULT_WAVEFORM);
        harmonicsMode.setValue(DEFAULT_HARMONICS_MODE);
        partialCount.setValue(DEFAULT_PARTIALS_COUNT);
        modulationEnabled.setValue(DEFAULT_MODULATION_ENABLED);
        modulationWaveform.setValue(DEFAULT_WAVEFORM);
        modulationDepth.setValue(DEFAULT_MODULATION_DEPTH);
        modulationNom.setValue(DEFAULT_MODULATION_NOM);
        modulationDenom.setValue(DEFAULT_MODULATION_DENOM);
    }

    public void toStore(Store store) {
        store.put("version", VERSION);
        store.put("sourceFile", sourceFile.getValue());
        store.put("minSourceValue", minSourceValue.get());
        store.put("maxSourceValue", maxSourceValue.get());
        store.put("minRecordIndex", minRecordIndex.get());
        store.put("maxRecordIndex", maxRecordIndex.get());
        store.put("gain", gain.get());
        store.put("velocity", velocity.get());
        store.put("amplitudeWeighting", amplitudeWeighting.get());
        store.put("tuningSystem", tuningSystem.getValue());
        store.put("minFrequency", minFrequency.get());
        store.put("maxFrequency", maxFrequency.get());
        store.put("octaveSubdivisionCount", octaveSubdivisionCount.get());
        store.put("carrierWaveform", carrierWaveform.getValue());
        store.put("harmonicsMode", harmonicsMode.getValue());
        store.put("partialCount", partialCount.get());
        store.put("modulationEnabled", modulationEnabled.get());
        store.put("modulationWaveform", modulationWaveform.getValue());
        store.put("modulationDepth", modulationDepth.get());
        store.put("modulationNom", modulationNom.get());
        store.put("modulationDenom", modulationDenom.get());
    }

    public void fromStore(Store store) {
        sourceFile.setValue(store.get("sourceFile", sourceFile.getValue()));
        sourceValues.setValue(null);
        minSourceValue.set(store.get("minSourceValue", DEFAULT_MIN_SOURCE_VALUE));
        maxSourceValue.set(store.get("maxSourceValue", DEFAULT_MAX_SOURCE_VALUE));
        minRecordIndex.set(store.get("minRecordIndex", DEFAULT_MIN_RECORD_INDEX));
        maxRecordIndex.set(store.get("maxRecordIndex", DEFAULT_MAX_RECORD_INDEX));
        gain.set(store.get("gain", DEFAULT_GAIN));
        velocity.set(store.get("velocity", DEFAULT_VELOCITY));
        amplitudeWeighting.set(store.get("amplitudeWeighting", DEFAULT_AMPLITUDE_SUM_RATIO));
        tuningSystem.setValue(store.get("tuningSystem", DEFAULT_TUNING_SYSTEM, TuningSystem.values()));
        minFrequency.set(store.get("minFrequency", DEFAULT_MIN_FREQUENCY));
        maxFrequency.set(store.get("maxFrequency", DEFAULT_MAX_FREQUENCY));
        octaveSubdivisionCount.set(store.get("octaveSubdivisionCount", DEFAULT_OCTAVE_SUBDIVISION_COUNT));
        carrierWaveform.setValue(store.get("carrierWaveform", DEFAULT_WAVEFORM, Waveform.WAVEFORMS));
        harmonicsMode.setValue(store.get("harmonicsMode", DEFAULT_HARMONICS_MODE, Harmonics.values()));
        partialCount.set(store.get("partialCount", DEFAULT_PARTIALS_COUNT));
        modulationEnabled.set(store.get("modulationEnabled", DEFAULT_MODULATION_ENABLED));
        modulationWaveform.setValue(store.get("modulationWaveform", DEFAULT_WAVEFORM, Waveform.WAVEFORMS));
        modulationDepth.setValue(store.get("modulationDepth", DEFAULT_MODULATION_DEPTH));
        modulationNom.setValue(store.get("modulationNom", DEFAULT_MODULATION_NOM));
        modulationDenom.setValue(store.get("modulationDenom", DEFAULT_MODULATION_DENOM));
    }

    public boolean hasSourceValues() {
        return getSourceValues() != null;
    }

    public double[][] getSourceValues() {
        return sourceValues.getValue();
    }

    public Property<double[][]> sourceValuesProperty() {
        return sourceValues;
    }

    public void setSourceValues(double[][] sourceValues, double minContainedSourceValue, double maxContainedSourceValue) {
        this.sourceValues.setValue(sourceValues);
        this.minContainedSourceValue.set(minContainedSourceValue);
        this.maxContainedSourceValue.set(maxContainedSourceValue);
    }

    public double getMinContainedSourceValue() {
        return minContainedSourceValue.get();
    }

    public DoubleProperty minContainedSourceValueProperty() {
        return minContainedSourceValue;
    }

    public void setMinContainedSourceValue(double minContainedSourceValue) {
        this.minContainedSourceValue.set(minContainedSourceValue);
    }

    public double getMaxContainedSourceValue() {
        return maxContainedSourceValue.get();
    }

    public DoubleProperty maxContainedSourceValueProperty() {
        return maxContainedSourceValue;
    }

    public void setMaxContainedSourceValue(double maxContainedSourceValue) {
        this.maxContainedSourceValue.set(maxContainedSourceValue);
    }

    public File getSourceFile() {
        return sourceFile.getValue();
    }

    public Property<File> sourceFileProperty() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile.setValue(sourceFile);
    }

    public double getMinSourceValue() {
        return minSourceValue.get();
    }

    public DoubleProperty minSourceValueProperty() {
        return minSourceValue;
    }

    public double getMaxSourceValue() {
        return maxSourceValue.get();
    }

    public DoubleProperty maxSourceValueProperty() {
        return maxSourceValue;
    }

    public int getMinRecordIndex() {
        return minRecordIndex.get();
    }

    public IntegerProperty minRecordIndexProperty() {
        return minRecordIndex;
    }

    public int getMaxRecordIndex() {
        return maxRecordIndex.get();
    }

    public IntegerProperty maxRecordIndexProperty() {
        return maxRecordIndex;
    }

    public double getGain() {
        return gain.get();
    }

    public DoubleProperty gainProperty() {
        return gain;
    }

    public int getVelocity() {
        return velocity.get();
    }

    public IntegerProperty velocityProperty() {
        return velocity;
    }

    public TuningSystem getTuningSystem() {
        return tuningSystem.getValue();
    }

    public Property<TuningSystem> tuningSystemProperty() {
        return tuningSystem;
    }

    public double getMinFrequency() {
        return minFrequency.get();
    }

    public DoubleProperty minFrequencyProperty() {
        return minFrequency;
    }

    public double getMaxFrequency() {
        return maxFrequency.get();
    }

    public DoubleProperty maxFrequencyProperty() {
        return maxFrequency;
    }

    public int getOctaveSubdivisionCount() {
        return octaveSubdivisionCount.get();
    }

    public IntegerProperty octaveSubdivisionCountProperty() {
        return octaveSubdivisionCount;
    }

    public Waveform getCarrierWaveform() {
        return carrierWaveform.getValue();
    }

    public Property<Waveform> carrierWaveformProperty() {
        return carrierWaveform;
    }

    public double getAmplitudeWeighting() {
        return amplitudeWeighting.get();
    }

    public DoubleProperty amplitudeWeightingProperty() {
        return amplitudeWeighting;
    }

    public Harmonics getHarmonicsMode() {
        return harmonicsMode.getValue();
    }

    public int getPartialCount() {
        return partialCount.get();
    }

    public IntegerProperty partialCountProperty() {
        return partialCount;
    }

    public Property<Harmonics> harmonicsModeProperty() {
        return harmonicsMode;
    }

    public void setHarmonicsMode(Harmonics harmonicsMode) {
        this.harmonicsMode.setValue(harmonicsMode);
    }

    public Waveform getModulationWaveform() {
        return modulationWaveform.getValue();
    }

    public Property<Waveform> modulationWaveformProperty() {
        return modulationWaveform;
    }

    public double getModulationDepth() {
        return modulationDepth.get();
    }

    public DoubleProperty modulationDepthProperty() {
        return modulationDepth;
    }

    public boolean isModulationEnabled() {
        return modulationEnabled.get();
    }

    public BooleanProperty modulationEnabledProperty() {
        return modulationEnabled;
    }

    public void setModulationEnabled(boolean modulationEnabled) {
        this.modulationEnabled.set(modulationEnabled);
    }

    public int getModulationNom() {
        return modulationNom.get();
    }

    public IntegerProperty modulationNomProperty() {
        return modulationNom;
    }

    public void setModulationNom(int modulationNom) {
        this.modulationNom.set(modulationNom);
    }

    public int getModulationDenom() {
        return modulationDenom.get();
    }

    public IntegerProperty modulationDenomProperty() {
        return modulationDenom;
    }

    public void setModulationDenom(int modulationDenom) {
        this.modulationDenom.set(modulationDenom);
    }
}
