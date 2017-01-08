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

package dedopfx.ui;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

abstract class InputFieldWithSlider<T extends Number> {
    private final String labelText;
    private final T min;
    private final T max;
    private final Property<Number> value;

    private boolean adjustingValueFromSlider;
    private boolean adjustingValueFromTextField;

    private Label label;
    private Slider slider;
    private TextField textField;
    private boolean init;

    public InputFieldWithSlider(String labelText, T min, T max, Property<Number> valueProperty) {
        this.labelText = labelText;
        this.min = min;
        this.max = max;
        this.value = valueProperty;
        this.init = false;
    }

    public Label getLabel() {
        return label;
    }

    public Slider getSlider() {
        return slider;
    }

    public TextField getTextField() {
        return textField;
    }

    public Property<Number> valueProperty() {
        return this.value;
    }

    public String toText(T value) {
        return value.toString();
    }

    public abstract T fromText(String text);

    public double toSliderValue(T value) {
        return value.doubleValue();
    }

    public abstract T fromSliderValue(double value);

    public void addToGrid(GridPane gridPane, int row) {
        if (!init) {
            init();
        }
        gridPane.add(label, 0, row);
        gridPane.add(slider, 1, row);
        gridPane.add(textField, 2, row);
    }

    public void removeFromGrid(GridPane gridPane) {
        if (!init) {
            return;
        }
        gridPane.getChildren().removeAll(label, slider, textField);
    }

    private void init() {
        init = true;
        T initialValue = (T) this.value.getValue();
        label = new Label(this.labelText);
        slider = new Slider(toSliderValue(min), toSliderValue(max), toSliderValue(initialValue));
        textField = new TextField(toText(initialValue));
        textField.setAlignment(Pos.CENTER_RIGHT);
        slider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number oldVal, Number newVal) -> {
            if (!adjustingValueFromSlider) {
                try {
                    adjustingValueFromSlider = true;
                    this.value.setValue(fromSliderValue(newVal.doubleValue()));
                } finally {
                    adjustingValueFromSlider = false;
                }
            }
        });

        textField.textProperty().addListener((ObservableValue<? extends String> ov, String oldVal, String newVal) -> {
            if (!adjustingValueFromTextField) {
                try {
                    adjustingValueFromTextField = true;
                    this.value.setValue(fromText(newVal));
                } catch (NumberFormatException e) {
                    // ok
                } finally {
                    adjustingValueFromTextField = false;
                }
            }
        });

        this.value.addListener((observable, oldValue, newValue) -> {
            if (!adjustingValueFromSlider) {
                slider.setValue(toSliderValue((T) newValue));
            }
            if (!adjustingValueFromTextField) {
                textField.setText(toText((T) newValue));
            }
        });
    }
}
