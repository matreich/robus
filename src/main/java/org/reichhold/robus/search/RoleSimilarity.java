package org.reichhold.robus.search;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;

/**
 * Created with IntelliJ IDEA.
 * User: matthias
 * Date: 11.08.12
 * Time: 16:28
 * To change this template use File | Settings | File Templates.
 */
public class RoleSimilarity extends SimilarityBase {
    @Override
    protected float score(BasicStats basicStats, float freq, float docLen) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String toString() {
        return "org.reichhold.robus.search.RoleSimilarity";
    }

    /*String index;

    public RoleSimilarity()
    {
        index = "/Users/matthias/Documents/workspace/robus/src/main/resources/indexDemo";
    }

    public void computeCosSim()
    {
        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
            for (int i=0; i<reader.maxDoc(); i++) {

                Document doc = reader.document(i);

                Terms t = reader.getTermVector(i, "contents");
                Fields f = reader.getTermVectors(i);

                int j =0;
            }

            int i = 0;

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }*/

}
