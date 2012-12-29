package org.reichhold.robus.roles;

import org.reichhold.robus.roles.disco.DiscoReader;
import org.reichhold.robus.roles.nlp.NlpHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * User: matthias
 * Date: 18.12.12
 */
public class JobZone {
    private Map<String, JobTerm> terms;

    private float zoneWeight;
    private DiscoReader discoReader;
    private Integer numberTerms;

    /***
     *
     * @param weight used when merged with other termCandidates
     */
    public JobZone(float weight, DiscoReader disco) {
        terms = new HashMap<String, JobTerm>();

        zoneWeight = weight;
        discoReader = disco;
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


    public void generateJobTerms(String input, NlpHelper nlp) {

        String[] sentences = nlp.detectSentences(input);

        for (String sentence : sentences) {
            String[] tokens = nlp.tokenize(sentence);

            String[] tags = nlp.tag(tokens);

            //POSSample sample = new POSSample(tokens, tags);
            //System.out.println("dddd  "+sample.toString());

            for (int i=0; i < tokens.length; i++) {
                int level = checkRelevance(tokens[i], tags[i]);

                if (level > 0) {
                    //this term is a noun and is included in DISCO, thus its relevant --> add to list

                    JobTerm term = new JobTerm();
                    term.setTerm(tokens[i].toLowerCase());
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
        int discoLevel = 0;

        if (token.length() > 1 && (tag.equals("NN") || tag.equals("NP"))) {
            discoLevel = computeDiscoLevel(token);
        }

        return discoLevel;
    }

    private int computeDiscoLevel(String token) {
        return discoReader.getDiscoLevel(token);
    }

    private void computeTermWeights() {

        int maxLevel = discoReader.getMaxLevel();

        for (Map.Entry<String, JobTerm> entry : terms.entrySet()){
            JobTerm term = entry.getValue();

            float termFreq = (float) term.getTermFreq() / numberTerms;   // 0 .. 1

            //float termPos = (float) Math.log10((float) ((1 + numberTerms) - term.getFirtPosition()) / numberTerms * 5 + 1);
            float termPos = (float) ((1 + numberTerms) - term.getFirtPosition()) / numberTerms;

            float normalizedDisco = (float) term.getDiscoLevel() / maxLevel;

            term.setWeight(termFreq + termPos * 0.1f + normalizedDisco * 2.0f);

            //System.out.println(term.getTerm() + ": DISCO=" +term.getDiscoLevel() + " WEIGHT=" + term.getWeight() );
        }
    }
}
