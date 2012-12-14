package org.reichhold.robus.search;

import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.util.BytesRef;

/**
 * User: matthias
 * Date: 10.12.12
 */
public class RoleSimilarity extends DefaultSimilarity {

    @Override
    public float scorePayload(int doc, int start, int end, BytesRef payload) {
        float f = super.scorePayload(doc, start, end, payload);
        return f;
    }

    @Override
    public float coord(int overlap, int maxOverlap)  {
        float f = super.coord(overlap, maxOverlap);
        System.out.println("coord = " + f);
        return f;
    }

    @Override
    public float queryNorm(float sumOfSquaredWeights) {
        float f = super.queryNorm(sumOfSquaredWeights);
        System.out.println("QueryNorm = " + f);
        return f;
    }

    @Override
    public float tf(float freq){
        float f = super.tf(freq);
        //System.out.println("TF = " + f + "; freq = " + freq);
        return f;
    }

    @Override
    public float idf(long docFreq, long numDocs) {
        float f = super.idf(docFreq, numDocs);
        System.out.println("IDF = " + f);
        return f;
    }
}
