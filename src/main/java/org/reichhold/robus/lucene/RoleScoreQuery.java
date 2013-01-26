package org.reichhold.robus.lucene;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queries.function.valuesource.FloatFieldSource;
import org.apache.lucene.search.Query;

/**
 * User: matthias
 * Date: 18.12.12
 */
public class RoleScoreQuery extends CustomScoreQuery {

    public RoleScoreQuery(Query subQuery, String roleName) {
        super(subQuery, new FunctionQuery( new FloatFieldSource(roleName)));

        //this.setStrict(true); // do not normalize score values from ValueSourceQuery!
    }

    /***
     * Does only work, when scoringQuery finds a result for every resutl in subQuery.
     * @param subQuery the default query
     * @param scoringQuery the role vector
     */
    public RoleScoreQuery(Query subQuery, Query scoringQuery) {
        super(subQuery, scoringQuery);

        //this.setStrict(true); // do not normalize score values from ValueSourceQuery!
    }

    @Override
    protected CustomScoreProvider getCustomScoreProvider(AtomicReaderContext reader) {
        return new CustomScoreProvider(reader) {
            @Override
            public float customScore(int doc, float subQueryScore, float valSrcScore){
                //merge scores; formular = http://research.microsoft.com/pubs/145110/sheldonssc-lambdamerge-wsdm11.pdf
                float weight = 0.1f;

                /*System.out.println(" ROLE SCORE " + valSrcScore +  " // " + subQueryScore);
                if (valSrcScore == 0) {
                    weight = 0;

                }*/

                float mergedScore = (1 - weight) * subQueryScore + weight * valSrcScore;

                //System.out.println("Computing score --> defaultScore: " + subQueryScore + " roleScore: " + valSrcScore  + " = mergedScore: " + mergedScore);
                return mergedScore;
            }
        };
    }
}
