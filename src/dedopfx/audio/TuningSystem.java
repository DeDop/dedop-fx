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

/**
 * see https://en.wikipedia.org/wiki/Musical_tuning#Tuning_systems
 */
public enum TuningSystem {
    LINEAR("Linear"),
    EQUAL_TEMPERAMENT("Equal Temperament"),
    CHROMATIC_SCALE("Chromatic Scale", new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}),
    DIATONIC_SCALE("Diatonic Scale", new int[]{0, 2, 3, 5, 7, 8, 10}),
    PENTATONIC_SCALE("Pentatonic Scale", new int[]{0, 3, 5, 7, 10});

    private final String label;
    private final int[] keys;

    TuningSystem(String label) {
        this(label, null);
    }

    TuningSystem(String label, int[] tones) {
        this.label = label;
        this.keys = tones;
    }

    public int[] getKeys() {
        return keys;
    }

    @Override
    public String toString() {
        return label;
    }
}
