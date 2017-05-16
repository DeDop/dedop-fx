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
import javafx.beans.value.ChangeListener;

import java.util.Arrays;

public class Algorithm {

    public interface RecordObserver {
        void onRecord(int recordIndex, int recordCount, double[] inputSamples);
    }

    private final AlgorithmInputs algorithmInputs;
    private final RecordObserver recordObserver;

    private double time;
    private int currentRecordIndex = -1;
    private double[] normalizedSourceValues;
    private double[] carrierFrequencies;

    public Algorithm(AlgorithmInputs algorithmInputs, RecordObserver recordObserver) {
        this.algorithmInputs = algorithmInputs;
        this.recordObserver = recordObserver;

        ChangeListener<Object> normalizedSourceValuesUpdater = (observable, oldValue, newValue) -> updateNormalizedSourceValues();
        algorithmInputs.sourceValuesProperty().addListener(normalizedSourceValuesUpdater);
        algorithmInputs.minRecordIndexProperty().addListener(normalizedSourceValuesUpdater);
        algorithmInputs.maxRecordIndexProperty().addListener(normalizedSourceValuesUpdater);
        algorithmInputs.velocityProperty().addListener(normalizedSourceValuesUpdater);
        updateNormalizedSourceValues();

        ChangeListener<Object> carrierFrequenciesUpdater = (observable, oldValue, newValue) -> updateCarrierFrequencies();
        algorithmInputs.sourceValuesProperty().addListener(carrierFrequenciesUpdater);
        algorithmInputs.tuningSystemProperty().addListener(carrierFrequenciesUpdater);
        algorithmInputs.minFrequencyProperty().addListener(carrierFrequenciesUpdater);
        algorithmInputs.maxFrequencyProperty().addListener(carrierFrequenciesUpdater);
        algorithmInputs.octaveSubdivisionCountProperty().addListener(carrierFrequenciesUpdater);
        updateCarrierFrequencies();
    }

    public void setTime(double time) {
        this.time = time;
        updateNormalizedSourceValues();
    }

    public double computeOutput() {
        final double[][] sourceValues = algorithmInputs.getSourceValues();
        final double gain = algorithmInputs.getGain();
        final double amplitudeWeighting = algorithmInputs.getAmplitudeWeighting();
        final Waveform carrierWaveform = algorithmInputs.getCarrierWaveform();
        final Harmonics harmonicsMode = algorithmInputs.getHarmonicsMode();
        final int partialCount = algorithmInputs.getPartialCount();
        final boolean modulationEnabled = algorithmInputs.isModulationEnabled();
        final Waveform modulationWaveform = algorithmInputs.getModulationWaveform();
        final double modulationDepth = algorithmInputs.getModulationDepth();
        final double modulationNom = algorithmInputs.getModulationNom();
        final double modulationDenom = algorithmInputs.getModulationDenom();

        if (sourceValues == null) {
            return 0;
        }
        if (normalizedSourceValues == null) {
            updateNormalizedSourceValues();
        }
        if (carrierFrequencies == null) {
            updateCarrierFrequencies();
        }

        final boolean harmonicsEnabled = algorithmInputs.getHarmonicsMode() != Harmonics.OFF;
        final double modulationRatio = modulationNom / modulationDenom;

        double carrierFrequency = 0;
        double value;
        double valueSum = 0.;
        double amplitude;
        double amplitudeSum = 0;
        double frequency;
        double phase;
        int partialIndex;

        final int sampleCount = sourceValues[0].length;
        for (int i = 0; i < sampleCount; i++) {
            partialIndex = harmonicsEnabled ? i % partialCount : 0;
            if (partialIndex == 0) {
                carrierFrequency = carrierFrequencies[i];
            }

            amplitude = normalizedSourceValues[i];
            if (amplitude > 0.) {
                amplitude /= partialIndex + 1;

                if (harmonicsMode == Harmonics.OVERTONES) {
                    frequency = carrierFrequency * (partialIndex + 1);
                } else if (harmonicsMode == Harmonics.UNDERTONES) {
                    frequency = carrierFrequency / (partialIndex + 1);
                } else {
                    frequency = carrierFrequency;
                }

                phase = 0.;
                if (modulationEnabled) {
                    phase += modulationDepth * modulationWaveform.compute(modulationRatio * frequency * time);
                }

                valueSum += amplitude * carrierWaveform.compute(frequency * time + phase);
                amplitudeSum += amplitude;
            }
        }

        if (amplitudeSum > 0.) {
            double sum = (1.0 - amplitudeWeighting) * (sampleCount / 2) + amplitudeWeighting * amplitudeSum;
            value = valueSum / sum;
        } else {
            value = 0;
        }

        value *= gain;

        if (value < -1.) {
            value = -1.;
            // todo: signal bottomClip clip
            // System.out.println("bottom clip at " + currentRecordIndex);
        }
        if (value > 1.) {
            value = 1.;
            // todo: signal top clip
            // System.out.println("top clip at " + currentRecordIndex);
        }

        return value;
    }

    private void updateNormalizedSourceValues() {
        final double[][] sourceValues = algorithmInputs.getSourceValues();
        final double minSourceValue = algorithmInputs.getMinSourceValue();
        final double maxSourceValue = algorithmInputs.getMaxSourceValue();
        final double velocity = algorithmInputs.getVelocity();
        int minRecordIndex = algorithmInputs.getMinRecordIndex();
        int maxRecordIndex = algorithmInputs.getMaxRecordIndex();
        if (sourceValues != null) {
            final int recordCount = sourceValues.length;
            if (minRecordIndex < 0) {
                minRecordIndex = 0;
            }
            if (minRecordIndex > recordCount - 1) {
                minRecordIndex = recordCount - 1;
            }
            if (maxRecordIndex < 0) {
                maxRecordIndex = 0;
            }
            if (maxRecordIndex > recordCount - 1) {
                maxRecordIndex = recordCount - 1;
            }
            if (minRecordIndex > maxRecordIndex) {
                int t = minRecordIndex;
                minRecordIndex = maxRecordIndex;
                maxRecordIndex = t;
            }
            final int selectedRecordCount = 1 + maxRecordIndex - minRecordIndex;
            final double recordIndexFloat = velocity * time;
            final double recordIndexFloor = Math.floor(recordIndexFloat);
            final double recordWeight = recordIndexFloat - recordIndexFloor;
            final int recordIndex1 = minRecordIndex + (int) (recordIndexFloor % selectedRecordCount);
            final int recordIndex2 = recordIndex1 + 1 <= maxRecordIndex ? recordIndex1 + 1 : recordIndex1;
            final double[] record1 = sourceValues[recordIndex1];
            final double[] record2 = sourceValues[recordIndex2];
            final int sampleCount = record1.length;
            if (normalizedSourceValues == null || normalizedSourceValues.length != sampleCount) {
                normalizedSourceValues = new double[sampleCount];
            }
            for (int i = 0; i < sampleCount; i++) {
                final double sourceValue = record1[i] + recordWeight * (record2[i] - record1[i]);
                double normalizedSourceValue = (sourceValue - minSourceValue) / (maxSourceValue - minSourceValue);
                if (normalizedSourceValue < 0.) {
                    normalizedSourceValue = 0.;
                }
                if (normalizedSourceValue > 1.) {
                    normalizedSourceValue = 1.;
                }
                normalizedSourceValues[i] = normalizedSourceValue;
            }
            if (recordIndex1 != currentRecordIndex) {
                if (recordObserver != null) {
                    recordObserver.onRecord(recordIndex1, sourceValues.length, normalizedSourceValues);
                }
                currentRecordIndex = recordIndex1;
            }
        } else {
            normalizedSourceValues = null;
            currentRecordIndex = -1;
        }
    }


    private void updateCarrierFrequencies() {
        final double[][] sourceValues = algorithmInputs.getSourceValues();
        final TuningSystem tuningSystem = algorithmInputs.getTuningSystem();
        final double minFrequency = algorithmInputs.getMinFrequency();
        final double maxFrequency = algorithmInputs.getMaxFrequency();
        final int octaveSubdivisionCount = algorithmInputs.getOctaveSubdivisionCount();

        if (sourceValues != null) {
            final int[] scaleKeys = tuningSystem.getKeys();
            final int sampleCount = sourceValues[0].length;
            if (carrierFrequencies == null || carrierFrequencies.length != sampleCount) {
                carrierFrequencies = new double[sampleCount];
            }
            for (int i = 0; i < sampleCount; i++) {
                final double carrierFrequency;
                if (tuningSystem == TuningSystem.LINEAR) {
                    carrierFrequency = minFrequency + (i / (sampleCount - 1.0)) * (maxFrequency - minFrequency);
                } else if (tuningSystem == TuningSystem.EQUAL_TEMPERAMENT) {
                    carrierFrequency = equalTemperament(minFrequency, i, octaveSubdivisionCount);
                } else {
                    // TODO: Don't use octaveSubdivisionCount for scales. Use new octaveCount instead which
                    // subdivides the samples array into octaveCount octaves. We then get:
                    // int sampleCountPerOctave = sampleCount / octaveCount;
                    // int octave = 12 * (i * octaveCount) / sampleCount;
                    // int scaleIndex = ((i % sampleCountPerOctave) * scaleKeys.length) / sampleCountPerOctave
                    // int key = scaleKeys[scaleIndex]
                    final int octave = 12 * (i / octaveSubdivisionCount);
                    final int key = scaleKeys[i % scaleKeys.length];
                    carrierFrequency = equalTemperament(minFrequency, octave + key, 12.0);
                }
                carrierFrequencies[i] = carrierFrequency;
            }
        } else {
            carrierFrequencies = null;
        }
        System.out.println("carrierFrequencies = " + Arrays.toString(carrierFrequencies));
    }

    private static double equalTemperament(double minFrequency, int keyIndex, double octaveSubdivisionCount) {
        return minFrequency * Math.pow(2, keyIndex / octaveSubdivisionCount);
    }
}
