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

import java.io.IOException;
import java.io.InputStream;

public abstract class SampleInputStream extends InputStream {
    private boolean closed;
    private short currentSample;
    private boolean hasSample;

    @Override
    public void close() throws IOException {
        closed = true;
    }

    @Override
    public int read() throws IOException {
        if (closed) {
            throw new IOException("closed");
        }
        if (!this.hasSample) {
            if (!hasMoreSamples()) {
                return -1;
            }
            this.currentSample = this.nextSample();
            this.hasSample = true;
            return (this.currentSample >>> 8) & 0xFF;
        } else {
            this.hasSample = false;
            return this.currentSample & 0xFF;
        }
    }

    protected abstract boolean hasMoreSamples() throws IOException;

    protected abstract short nextSample() throws IOException;
}
