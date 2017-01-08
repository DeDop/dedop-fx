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

import dedopfx.algo.Algorithm;
import dedopfx.algo.AlgorithmInputs;
import dedopfx.audio.PlayAudioTask;
import dedopfx.source.LoadL1bNetCDFFileTask;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;

class Controller {
    private final LoadSourceFileService loadSourceFileService = new LoadSourceFileService();
    private final PlayService playService = new PlayService();

    private final Property<File> documentFile = new SimpleObjectProperty<>(null);
    private final AlgorithmInputs algorithmInputs;
    private final Algorithm algorithm;

    public Controller(Algorithm.RecordObserver recordObserver) {
        algorithmInputs = new AlgorithmInputs();
        algorithm = new Algorithm(algorithmInputs, recordObserver);
    }

    public File getDocumentFile() {
        return documentFile.getValue();
    }

    public Property<File> documentFileProperty() {
        return documentFile;
    }

    public void setDocumentFile(File documentFile) {
        this.documentFile.setValue(documentFile);
    }

    LoadSourceFileService getLoadSourceFileService() {
        return loadSourceFileService;
    }

    PlayService getPlayService() {
        return playService;
    }

    public AlgorithmInputs getAlgorithmInputs() {
        return algorithmInputs;
    }

    public class LoadSourceFileService extends Service<Void> {

        private File sourceFile;

        public File getSourceFile() {
            return sourceFile;
        }

        void setSourceFile(File sourceFile) {
            this.sourceFile = sourceFile;
        }

        @Override
        protected Task<Void> createTask() {
            return new LoadL1bNetCDFFileTask(sourceFile, algorithmInputs);
        }
    }

    public class PlayService extends Service<Void> {

        @Override
        protected Task<Void> createTask() {
            return new PlayAudioTask(algorithm);
        }

    }

}
