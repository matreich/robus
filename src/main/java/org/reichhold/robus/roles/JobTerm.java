package org.reichhold.robus.roles;

/**
 * User: matthias
 * Date: 18.12.12
 */
public class JobTerm {
    private String term;

    private String posTag;

    private Integer discoLevel;

    private Integer termFreq;

    private Integer firtPosition;

    private Float weight;

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getPosTag() {
        return posTag;
    }

    public void setPosTag(String posTag) {
        this.posTag = posTag;
    }

    public Integer getDiscoLevel() {
        return discoLevel;
    }

    public void setDiscoLevel(Integer discoLevel) {
        this.discoLevel = discoLevel;
    }

    public Integer getTermFreq() {
        return termFreq;
    }

    public void setTermFreq(Integer termFreq) {
        this.termFreq = termFreq;
    }

    public Integer getFirtPosition() {
        return firtPosition;
    }

    public void setFirtPosition(Integer firtPosition) {
        this.firtPosition = firtPosition;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }
}
