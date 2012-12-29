package org.reichhold.robus.roles.nlp;

/**
 * User: matthias
 * Date: 29.12.12
 */
public class NlpHelper {
    private RobusSentenceDetector detector;
    private RobusTokenizer myTok;
    private RobusTagger myTagger;

    public NlpHelper() {
        detector = new RobusSentenceDetector();
        myTok = new RobusTokenizer();
        myTagger = new RobusTagger();
    }

    public String[] detectSentences(String input) {
        return detector.detectSentences(input);
    }

    public String[] tokenize(String sentence) {
        return myTok.tokenize(sentence);
    }

    public String[] tag(String[] tokens) {
        return myTagger.tag(tokens);
    }
}
