package org.reichhold.robus.citeulike;

import com.mysql.jdbc.StringUtils;
import org.reichhold.robus.db.DataStore;

import java.io.*;
import java.util.*;

/**
 * User: matthias
 * Date: 26.01.13
 */
public class CulLinkoutsReader {

        private DataStore store;

        private TreeMap<String, CulUser> users;
        private TreeMap<String, CulDocument> documents;
        private TreeMap<String, CulTag> tags;
        private TreeMap<String, CulAssignment> assignments;

        private BufferedReader br;

        public CulLinkoutsReader() {
            store = new DataStore();
        }

        public void saveIds() {
            setBufferedReader();

            String line;
            long counter = 0;

            //Read File Line By Line
            try {
                List<CulDocument> docs = new ArrayList<CulDocument>();

                SortedSet<String> docIds = new TreeSet<String>();

                while ((line = br.readLine()) != null)   {
                    // Process the line
                    CulDocument doc = generateCulDocument("PMID", line);
                    if (doc == null) {
                        continue;
                    }

                    docs.add(doc);

                    if (docs.size() < 100) {
                        continue;
                    }
                    counter += store.saveCulDocuments(docs, false);
                    docs.clear();
                }

                counter += store.saveCulDocuments(docs, false);
                docs.clear();

                System.out.println(counter + " documents saved");

            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        private CulDocument generateCulDocument(String type, String line) {
            String key = getColumnFromLine(line, 0);

            CulDocument doc = (CulDocument) store.getSession().get(CulDocument.class, key);

            if (doc == null) {
                return null;
            }

            String idType = getColumnFromLine(line, 1).trim();

            if (!idType.equals(type)) {
                return null;
            }

            int index = 0;
            if (type.equals("PMID") && StringUtils.isNullOrEmpty(doc.getPmid())){
                index = 2;
                String pmid = getColumnFromLine(line, index);
                doc.setPmid(pmid);
            }
            if (type.equals("DOI") && StringUtils.isNullOrEmpty(doc.getDoi())){
                index = 3;
                String doi = getColumnFromLine(line, index);
                doc.setDoi(doi);
            }

            System.out.println("Udating doc " + doc.getId() + " " + doc.getPmid());

            return doc;
        }


        private String getColumnFromLine(String line, int index) {
            //line example: 1351299|f7d5debb9c7d4d9ab81c63cd26578e23|2007-05-31 14:01:35.797261+01|comparative
            String[] columns = line.split("\\|");

            if (columns.length <= index) {
                System.out.println("Invalid input line: expected " + index+1 + "columns, found " + columns.length);
                return "n/a";
            }

            return columns[index];
        }

        private void setBufferedReader() {
            // Open the file
            FileInputStream fstream = null;
            try {
                //fstream = new FileInputStream("/Volumes/Daten/Robus/Resources/datasets/CiteULike 20130102/linkouts-2012-12-31");
                fstream = new FileInputStream("/Volumes/Daten/Robus/Resources/datasets/CiteULike 20130102/linkouts-2013-01-02");
            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in));
        }
    }

