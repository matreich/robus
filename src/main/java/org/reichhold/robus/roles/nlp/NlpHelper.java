package org.reichhold.robus.roles.nlp;

/**
 * User: matthias
 * Date: 29.12.12
 */
public class NlpHelper {
    private RobusSentenceDetector detector;
    private RobusTokenizer myTok;
    private RobusTagger myTagger;
    private RobusChunker myChunker;

    public NlpHelper() {
        detector = new RobusSentenceDetector();
        myTok = new RobusTokenizer();
        myTagger = new RobusTagger();
        myChunker = new RobusChunker();
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

    public String[] chunk (String[] tokens, String[] tags) {
        return myChunker.chunk(tokens, tags);
    }

    public void textToChunks(String input) {
        String[] sentences = this.detectSentences(input);

        for (String sentence : sentences) {
            String[] tokens = this.tokenize(sentence);

            String[] tags = this.tag(tokens);

            String [] chunks = this.chunk(tokens, tags);

            for (int i=0; i<tokens.length; i++){
                System.out.println(tokens[i] + ": " + tags[i] + "\t" + chunks[i]);
            }
        }
    }
}
