package org.reichhold.robus.roles.nlp;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: matthias
 * Date: 18.12.12
 */
public class RobusTagger {
    private POSModel model;
    private POSTaggerME tagger;

    public RobusTagger() {
        InputStream modelIn = null;

        try {
            modelIn = new FileInputStream("/Users/matthias/Documents/workspace/robus/src/main/resources/openNlp/en-pos-brown.bin");

            model  = new POSModel(modelIn);

            tagger = new POSTaggerME(model);
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

    public String[] tag (String[] input) {
        return tagger.tag(input);
    }
}
