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

package dedopfx.store;

import java.io.File;

public abstract class AbstractStore implements Store {
    @Override
    public File get(String key, File defaultValue) {
        String value = get(key, (String) null);
        if (value != null && !value.trim().isEmpty()) {
            return new File(value);
        }
        return defaultValue;
    }

    @Override
    public void put(String key, File value) {
        put(key, value != null ? value.getPath() : "");
    }

    @Override
    public <T> T get(String key, T defaultValue, T[] enumValues) {
        String value = get(key, (String) null);
        if (value != null && enumValues != null) {
            for (T enumValue : enumValues) {
                if (enumValue.toString().equalsIgnoreCase(value)) {
                    return enumValue;
                }
            }
        }
        return defaultValue;
    }

    @Override
    public <T> void put(String key, T value) {
        put(key, value != null ? value.toString() : "");
    }

    @Override
    public boolean get(String key, boolean defaultValue) {
        String value = get(key, (String) null);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    @Override
    public void put(String key, boolean value) {
        put(key, String.valueOf(value));
    }

    @Override
    public int get(String key, int defaultValue) {
        String value = get(key, (String) null);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // ok
            }
        }
        return defaultValue;
    }

    @Override
    public void put(String key, int value) {
        put(key, String.valueOf(value));
    }

    @Override
    public double get(String key, double defaultValue) {
        String value = get(key, (String) null);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                // ok
            }
        }
        return defaultValue;
    }

    @Override
    public void put(String key, double value) {
        put(key, String.valueOf(value));
    }
}
