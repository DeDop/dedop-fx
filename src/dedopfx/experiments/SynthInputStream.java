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
import java.io.IOException;

public class SynthInputStream extends SampleInputStream {
    private long sampleCount;
    private AudioFormat audioFormat;

    SynthInputStream(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
    }

    @Override
    protected boolean hasMoreSamples() throws IOException {
        return true;
    }

    protected short nextSample() {
        double t = this.sampleCount / audioFormat.getSampleRate();
        double pitch = pitch(t);
        this.sampleCount++;
        return (short) (Short.MAX_VALUE * this.wave(pitch * t));
    }

    protected double pitch(double t) {
        return 440.0 * 0.5 * (1.0 + wave(t));
    }

    protected double wave(double x) {
        return Math.sin(2.0 * Math.PI * x);
    }
}
