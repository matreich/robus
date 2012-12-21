package org.reichhold.robus.roles.nlp;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: matthias
 * Date: 18.12.12
 */
public class RobusTokenizer {
    private TokenizerModel model;
    private Tokenizer tokenizer;

    public RobusTokenizer() {
        InputStream modelIn = null;

        try {
            modelIn = new FileInputStream("/Users/matthias/Documents/workspace/robus/src/main/resources/openNlp/en-token.bin");

            model  = new TokenizerModel(modelIn);

            tokenizer = new TokenizerME(model);
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

    public String[] tokenize (String input) {
        return tokenizer.tokenize(input);
    }
}
