package org.reichhold.robus.citeulike;

import org.json.JSONException;
import org.json.JSONObject;
import org.reichhold.robus.db.DataStore;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.List;

/**
 * User: matthias
 * Date: 04.01.13
 */
public class CulWebReader {
    private final String CUL_ARTICLE_URL = "http://www.citeulike.org/json/article/";
    private DataStore store;

    public CulWebReader() {
    }

    public void loadAllTitlesAndAbstracts() {
        int number = 100;

        //IP gets blocked if moe then maxNumber requests are sent...
        int maxNumber = 5000;
        int counter = 0;

        while (number > 0 && counter < maxNumber) {
            number = loadTitlesAndAbstracts(number);
            counter += number;
        }
        System.out.println(counter + " docs processed.");
    }

    private int loadTitlesAndAbstracts(int number) {
        //get first N jobs (that dont have title/abstract data) from db
        store = new DataStore();
        List<CulDocument> docs = store.getEmptyCulDocuments(number);

        for (CulDocument doc : docs) {
            setCulDocumentMetaData(doc);
        }
        store.saveOrUpdateObjects(docs, "CulDocument");
        store.closeSession();
        System.out.println(docs.size() + " docs processed");

        return docs.size();
    }

    private void setCulDocumentMetaData(CulDocument doc) {

        String url = CUL_ARTICLE_URL + doc.getId();
        InputStream is = null;
        try {
            URL u = new URL(url);
            URLConnection hc = u.openConnection();
            hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.7; de-AT; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            is = hc.getInputStream();
            //is = new URL(url).openStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);

            String path = json.get("href").toString();
            if (path.length() > 255) {
                path = path.substring(0, 254);
                System.out.println("Path too long for doc " + doc.getId());
            }
            doc.setPath(path);

            String title = json.get("title").toString();
            if (title.length() > 255) {
                title = title.substring(0, 254);
            }
            doc.setTitle(title);

            if (json.has("abstract")) {
                String abs = json.get("abstract").toString();
                if (abs.length() > 3071) {
                    abs = abs.substring(0, 3070);
                    System.out.println("Abstract too long for doc " + doc.getId());
                }
                doc.setContentAbstract(abs);
            }
            else {
                doc.setContentAbstract("n/a");
            }

        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }

    private String readAll(Reader rd) throws IOException {
        //JSON String has to Start with '{' and end with '}'
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }

        String json = sb.toString();
        int start = json.indexOf("{");
        if (start > 0) {
            json = json.substring(start);
        }

        int end = json.lastIndexOf("}");

        if (end + 1 < json.length()) {
            json = json.substring(0, end+1);
        }

        if (sb.toString().contains("Your IP has been blocked")) {
            //System.out.println("Your IP has been blocked");
            System.out.println(sb.toString());
            System.exit(1);
        }

        return json;
    }


    public void loadTitlesAndAbtractsForQuery(String query) {

        while (true) {
            store = new DataStore();
            List<CulDocument> docs = store.getDocumentsByTag(query);

            if (docs.size() == 0) {
                return;
            }

            for (CulDocument doc : docs) {
                setCulDocumentMetaData(doc);
            }
            store.saveOrUpdateObjects(docs, "CulDocument");
            store.closeSession();
            System.out.println(docs.size() + " docs processed");
        }
    }
}
