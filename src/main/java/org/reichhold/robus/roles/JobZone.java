package org.reichhold.robus.roles;

import org.reichhold.robus.roles.nlp.RobusSentenceDetector;
import org.reichhold.robus.roles.nlp.RobusTagger;
import org.reichhold.robus.roles.nlp.RobusTokenizer;

import java.util.HashMap;
import java.util.Map;

/**
 * User: matthias
 * Date: 18.12.12
 */
public class JobZone {
    private Map<String, JobTerm> terms;

    private float zoneWeight;

    private Integer numberTerms;

    /***
     *
     * @param weight used when merged with other termCandidates
     */
    public JobZone(float weight) {
        terms = new HashMap<String, JobTerm>();

        zoneWeight = weight;
        numberTerms = 0;
    }

    public Map<String, JobTerm> getTerms() {
        return terms;
    }

    public void setTerms(Map<String, JobTerm> terms) {
        this.terms = terms;
    }

    public float getZoneWeight() {
        return zoneWeight;
    }

    public void setZoneWeight(float zoneWeight) {
        this.zoneWeight = zoneWeight;
    }

    public Integer getNumberTerms() {
        return numberTerms;
    }

    /***
     * Increases term freq. if the term already exists
     * @param term
     * @return true if terms already exists
     */
    public boolean update(String term) {
        if (terms.containsKey(term)) {

            //term already exists; increase termFreq
            JobTerm existing = terms.get(term);
            existing.setTermFreq(existing.getTermFreq() + 1);
            terms.put(term, existing);

            return true;
        }

        return false;
    }

    /***
     * Adds or updates (increase term freq +1) the term candidate
     * @param candidate
     */
    public void add(JobTerm candidate) {
        if (terms.containsKey(candidate.getTerm())) {

            //term already exists; only increase termFreq
            update(candidate.getTerm());
            return;
        }

        terms.put(candidate.getTerm(), candidate);
    }


    public void generateJobTerms(String input) {
        RobusSentenceDetector detector = new RobusSentenceDetector();
        String[] sentences = detector.detectSentences(input);

        RobusTokenizer myTok = new RobusTokenizer();
        RobusTagger myTagger = new RobusTagger();

        for (String sentence : sentences) {
            String[] tokens = myTok.tokenize(sentence);

            String[] tags = myTagger.tag(tokens);

            //POSSample sample = new POSSample(tokens, tags);
            //System.out.println("dddd  "+sample.toString());

            for (int i=0; i < tokens.length; i++) {
                int level = checkRelevance(tokens[i], tags[i])
                if (level > 0) {
                    //this term is a noun and thus relevant --> add to list

                    JobTerm term = new JobTerm();
                    term.setTerm(tokens[i]);
                    term.setPosTag(tags[i]);
                    term.setTermFreq(1);
                    term.setFirtPosition(terms.size() + 1);
                    term.setDiscoLevel(level);

                    add(term);
                }
            }
        }

        numberTerms = terms.size();

        computeTermWeights();
    }

    private int checkRelevance(String token, String tag) {
        int isNoun = 0;

        if (tag.equals("NN") || tag.equals("NP")) {
            isNoun = 1;
        }

        int discoLevel = computeDiscoLevel(token);

        return isNoun * discoLevel;
    }

    private int computeDiscoLevel(String token) {
        return 0;  //To change body of created methods use File | Settings | File Templates.
    }

    private void computeTermWeights() {

        for (Map.Entry<String, JobTerm> entry : terms.entrySet()){
            JobTerm term = entry.getValue();

            float termFreq = (float) term.getTermFreq() / numberTerms;   // 0 .. 1

            //float termPos = (float) Math.log10((float) ((1 + numberTerms) - term.getFirtPosition()) / numberTerms * 5 + 1);
            float termPos = (float) ((1 + numberTerms) - term.getFirtPosition()) / numberTerms * 0.5f;

            term.setWeight(termFreq + termPos);
        }
    }
}
