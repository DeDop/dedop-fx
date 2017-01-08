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

package dedopfx.source;

import dedopfx.algo.AlgorithmInputs;
import javafx.application.Platform;
import javafx.concurrent.Task;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;

public class LoadL1bNetCDFFileTask extends Task<Void> {
    private final File sourceFile;
    private final AlgorithmInputs algorithmInputs;

    public LoadL1bNetCDFFileTask(File sourceFile, AlgorithmInputs algorithmInputs) {
        this.sourceFile = sourceFile;
        this.algorithmInputs = algorithmInputs;
    }

    @Override
    protected Void call() throws IOException {
        try (NetcdfFile netcdfFile = NetcdfFile.open(sourceFile.getPath())) {
            // List<Variable> variables = netcdfFile.getVariables();
            // for (Variable variable : variables) {
            //     System.out.println("variable = " + variable.getNameAndDimensions());
            // }

            String waveformCountsVarName = "i2q2_meas_ku_l1b_echo_sar_ku";
            Variable waveformCountsVar = netcdfFile.findVariable(waveformCountsVarName);
            if (waveformCountsVar == null) {
                throw new IOException(String.format("Can't find variable \"%s\"", waveformCountsVarName));
            }
            Array waveformCounts = waveformCountsVar.read();
            int[] waveformCountsShape = waveformCounts.getShape();
            //Object obj = samples.copyToNDJavaArray();
            if (waveformCountsShape.length != 2) {
                throw new IOException(String.format("Expected variable \"%s\" to be a 2D array, but is a %s",
                        waveformCountsVarName, waveformCounts.toString()));
            }
            int recordCount = waveformCountsShape[0];
            int waveformSize = waveformCountsShape[1];

            String waveformScalingsVarName = "scale_factor_ku_l1b_echo_sar_ku";
            Variable waveformScalingsVar = netcdfFile.findVariable(waveformScalingsVarName);
            if (waveformScalingsVar == null) {
                throw new IOException(String.format("Can't find variable \"%s\"", waveformScalingsVarName));
            }
            Array waveformScalings = waveformScalingsVar.read();
            int[] waveformScalingsShape = waveformScalings.getShape();
            //Object obj = samples.copyToNDJavaArray();
            if (waveformScalingsShape.length != 1) {
                throw new IOException(String.format("Expected variable \"%s\" to be a 1D array, but is a %s",
                        waveformScalingsVarName, waveformCounts.toString()));
            }
            if (waveformScalingsShape[0] != recordCount) {
                throw new IOException(String.format("Expected variable \"%s\" to be of size %s, but is %s",
                        waveformScalingsVarName, waveformCountsShape[1], recordCount));
            }

            double[][] sourceValues = new double[recordCount][waveformSize];
            double minSampleValue = Double.MAX_VALUE;
            double maxSampleValue = -Double.MAX_VALUE;
            for (int recordIndex = 0, k = 0; recordIndex < recordCount; recordIndex++) {
                double waveformScaling = waveformScalings.getDouble(recordIndex) * 0.01;
                for (int sampleIndex = 0; sampleIndex < waveformSize; sampleIndex++) {
                    double waveformCount = waveformCounts.getDouble(k++) * 0.001;
                    //System.out.println("waveformScaling = " + waveformScaling);
                    //System.out.println("waveformCount = " + waveformCount);
                    double sample = waveformScaling * waveformCount;
                    sourceValues[recordIndex][sampleIndex] = sample;
                    minSampleValue = Math.min(minSampleValue, sample);
                    maxSampleValue = Math.max(maxSampleValue, sample);
                }
                updateProgress(recordIndex + 1, recordCount);
                if (isCancelled()) {
                    return null;
                }
            }
            final double minContainedSourceValue = minSampleValue;
            final double maxContainedSourceValue = maxSampleValue;
            System.out.println("minSampleValue = " + minSampleValue);
            System.out.println("maxSampleValue = " + maxSampleValue);
            if (!isCancelled()) {
                Platform.runLater(() -> {
                    algorithmInputs.setSourceFile(sourceFile);
                    algorithmInputs.setSourceValues(sourceValues, minContainedSourceValue, maxContainedSourceValue);
                });
            }
            return null;
        }
    }
}

