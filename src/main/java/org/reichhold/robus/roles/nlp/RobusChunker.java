package org.reichhold.robus.roles.nlp;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: matthias
 * Date: 29.12.12
 */
public class RobusChunker {

    private ChunkerModel model;
    private ChunkerME chunker;

    public RobusChunker() {
        InputStream modelIn = null;

        try {
            modelIn = new FileInputStream("/Users/matthias/Documents/workspace/robus/src/main/resources/openNlp/en-chunker.bin");

            model  = new ChunkerModel(modelIn);

            chunker = new ChunkerME(model);
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

    public String[] chunk (String[] tokens, String[] tags) {
        return chunker.chunk(tokens, tags);
    }
}
