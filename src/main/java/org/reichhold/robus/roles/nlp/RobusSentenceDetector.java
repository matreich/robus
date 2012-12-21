package org.reichhold.robus.roles.nlp;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: matthias
 * Date: 18.12.12
 */
public class RobusSentenceDetector {
    private SentenceModel model;
    private SentenceDetectorME detector;

    public RobusSentenceDetector() {
        InputStream modelIn = null;

        try {
            modelIn = new FileInputStream("/Users/matthias/Documents/workspace/robus/src/main/resources/openNlp/en-sent.bin");

            model  = new SentenceModel(modelIn);

            detector = new SentenceDetectorME(model);
        }
        catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                }
                catch (IOException e) {
                }
            }
        }
    }

    public String[] detectSentences(String input) {
        return detector.sentDetect(input);
    }
}
