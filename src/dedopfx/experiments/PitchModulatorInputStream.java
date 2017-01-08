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

package dedopfx.experiments;

import dedopfx.audio.SampleInputStream;

import javax.sound.sampled.AudioFormat;
import java.util.Arrays;

public class PitchModulatorInputStream extends SampleInputStream {
    private AudioFormat audioFormat;
    private int[][] data;
    private double[] frequencies;
    private long sampleCount;
    private double velocity;
    private double volume;
    private double sampleMin;
    private double sampleMax;
    private int lastSampleIndex = -1;

    PitchModulatorInputStream(AudioFormat audioFormat, int[][] data) {
        this.audioFormat = audioFormat;
        this.data = data;

        this.sampleMin = 0.;
        this.sampleMax = 42453470;

        this.volume = 0.03;

        // Velocity = 8 will play each burst for 1/8 sec
        this.velocity = 16.0;

        // see https://en.wikipedia.org/wiki/Piano_key_frequencies
        int aKey = 80;
        double aFrequency = 440;

        int numKeys = data[0].length;
        double[] frequencies = new double[numKeys];
        for (int key = 0; key < numKeys; key++) {
            frequencies[key] = Math.pow(2.0, (key - aKey) / 12.0) * aFrequency;
        }
        this.frequencies = frequencies;
        System.out.println("frequencies = " + Arrays.toString(frequencies));
    }

    protected boolean hasMoreSamples() {
        return true;
    }

    protected short nextSample() {
        double t = this.sampleCount / audioFormat.getSampleRate();
        this.sampleCount++;
        int sampleIndex = (int) (Math.floor(this.velocity * t) % this.data.length);
        if (sampleIndex != lastSampleIndex) {
            lastSampleIndex = sampleIndex;
            System.out.println("sampleIndex = " + sampleIndex);
        }

        int[] burst = this.data[sampleIndex];
        double value = 0;
        for (int i = 0; i < burst.length; i++) {
            //double pitch = frequencies[i];
            double pitch = 220 / 4 + (i / (burst.length - 1.0)) * 4 * 880;
            //double pitch = 220 + (i / (burst.length - 1.0)) * 880;
            //double pitch = 220/2 + (Math.pow(2, i / (burst.length - 1.0)) - 1.0) * 2 * 880;
            //double pitch = Math.pow(2, (i - 49) / 12.) * 440;
            long sample = burst[i] & 0xFFFFFFFFL;
            double gain = (sample - sampleMin) / (sampleMax - sampleMin);
            gain *= gain;
            if (gain > 1.) {
                gain = 1.;
            }
            if (gain > 0) {
                value += gain * Math.sin(2.0 * Math.PI * pitch * t);
            }
        }
        value *= this.volume;
        if (value < -1) {
            value = -1;
            System.out.println("BOTTOM!");
        }
        if (value > 1) {
            value = 1;
            System.out.println("TOP!");
        }
        short sample = (short) (Short.MAX_VALUE * value);
        //System.out.println("dedopfx = " + dedopfx);
        return sample;
    }

}
