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

public interface Waveform {

    double compute(double t);

    String toString();

    Waveform SINE = new Waveform() {
        double TWO_PI = 2.0 * Math.PI;

        @Override
        public final double compute(double t) {
            return Math.sin(TWO_PI * t);
        }

        @Override
        public String toString() {
            return "Sine";
        }
    };

    Waveform SQUARE = new Waveform() {
        @Override
        public final double compute(double t) {
            double x = t - Math.floor(t);
            return x < 0.5 ? 1.0 : -1.0;
        }

        @Override
        public String toString() {
            return "Square";
        }
    };

    Waveform TRIANGLE = new Waveform() {
        @Override
        public final double compute(double t) {
            t += 0.25;
            double x = t - Math.floor(t);
            if (x < 0.5) {
                return 4.0 * x - 1.0;
            } else {
                return 3.0 - 4.0 * x;
            }
        }

        @Override
        public String toString() {
            return "Triangle";
        }
    };

    Waveform SAWTOOTH = new Waveform() {
        @Override
        public final double compute(double t) {
            t += 0.5;
            double x = t - Math.floor(t);
            return 2.0 * x - 1.0;
        }

        @Override
        public String toString() {
            return "Sawtooth";
        }
    };

    Waveform[] WAVEFORMS = {
            SINE,
            SQUARE,
            TRIANGLE,
            SAWTOOTH,
            combine(SINE, SQUARE),
            combine(SINE, TRIANGLE),
            combine(SINE, SAWTOOTH),
            combine(SQUARE, TRIANGLE),
            combine(SQUARE, SAWTOOTH),
            combine(TRIANGLE, SAWTOOTH),
    };

    static Waveform combine(Waveform waveform1, Waveform waveform2) {
        String name = waveform1.toString() + " + " + waveform2.toString();
        return new Waveform() {
            @Override
            public double compute(double t) {
                return 0.5 * (waveform1.compute(t) + waveform2.compute(t));
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    static Waveform combine(Waveform waveform1, Waveform waveform2, Waveform waveform3) {
        String name = waveform1.toString() + " + " + waveform2.toString() + " + " + waveform3.toString();
        return new Waveform() {
            @Override
            public double compute(double t) {
                return (waveform1.compute(t) + waveform2.compute(t) + waveform3.compute(t)) / 3.0;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }
}
