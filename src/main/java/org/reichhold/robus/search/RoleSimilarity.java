package org.reichhold.robus.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: matthias
 * Date: 11.08.12
 * Time: 16:28
 * To change this template use File | Settings | File Templates.
 */
public class RoleSimilarity {

    String index;

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
    }

}
