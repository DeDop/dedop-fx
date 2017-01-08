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

package dedopfx.audio;

import dedopfx.algo.Algorithm;
import dedopfx.algo.AlgorithmSampleInputStream;
import javafx.concurrent.Task;

import javax.sound.sampled.*;
import java.io.InputStream;

public class PlayAudioTask extends Task<Void> {
    final Algorithm algorithm;

    public PlayAudioTask(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    protected Void call() throws Exception {

        int sampleRate = 44100;
        AudioFormat audioFormat = new AudioFormat(sampleRate, 16, 1, true, true);
        AudioFileFormat.Type[] audioFileTypes = AudioSystem.getAudioFileTypes();
        for (AudioFileFormat.Type audioFileType : audioFileTypes) {
            System.out.println("audioFileType = " + audioFileType);
        }
        AudioFormat.Encoding[] targetEncodings = AudioSystem.getTargetEncodings(audioFormat);
        for (AudioFormat.Encoding targetEncoding : targetEncodings) {
            System.out.println("targetEncoding = " + targetEncoding);
        }
        InputStream inputStream = new AlgorithmSampleInputStream(audioFormat.getSampleRate(), algorithm);

        SourceDataLine lineIn = AudioSystem.getSourceDataLine(audioFormat);
        lineIn.addLineListener(event -> {
            System.out.println("SourceDataLine: event = " + event);
        });
        lineIn.open(audioFormat);

        lineIn.start();
        int numBytesRead = 0;
        final int bufferSize = sampleRate / 10;
        final byte[] audioData = new byte[2 * bufferSize];
        long t0, t1, t2;
        try {
            while (numBytesRead != -1) {
                t0 = System.nanoTime();
                numBytesRead = inputStream.read(audioData, 0, audioData.length);
                t1 = System.nanoTime();
                if (numBytesRead >= 0) {
                    lineIn.write(audioData, 0, numBytesRead);
                    t2 = System.nanoTime();

                    double currentSampleRate = 1e9 * 0.5 * numBytesRead / (t1 - t0);
                    if (currentSampleRate < sampleRate) {
                        // todo: notify via this.recordObserver
                        System.out.printf("WARNING: Current sample rate is at %.1f per second. " +
                                        "This is only %.1f%% of the required sample rate!%n",
                                currentSampleRate, 100. * currentSampleRate/sampleRate);
                        System.out.printf("  read took %.1f ms for %s samples%n", (t1 - t0)/1e6, numBytesRead / 2);
                        System.out.printf("  write took %.1f ms%n", (t2 - t1)/1e6);
                        System.out.printf("  both took %.1f ms%n", (t2 - t0)/1e6);
                    }
                }
                if (isCancelled()) {
                    break;
                }
            }
        } finally {
            lineIn.drain();
            lineIn.close();
        }

        return null;
    }
}
