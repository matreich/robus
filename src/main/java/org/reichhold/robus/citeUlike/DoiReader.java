package org.reichhold.robus.citeulike;

import com.mysql.jdbc.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.reichhold.robus.db.DataStore;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * User: matthias
 * Date: 26.01.13
 */
public class DoiReader {

    public DoiReader() {

    }

    public void loadAbstracts() {
        DataStore store = new DataStore();

        List<CulDocument> docs = store.getDocumentsBySqlQuery("select * from cul_document_eval where content_abstract = 'n/a' and doi is not null", 100, false);
        for (CulDocument doc:docs) {
            String content = getAbtractsByDoi(doc.getDoi());

            if(StringUtils.isNullOrEmpty(content) || content.equals("n/a")) {
                continue;
            }

            doc.setContentAbstract(content);
            store.saveOrUpdateObject(doc);
            System.out.println("Updating doc " + doc.getId() + " " + doc.getContentAbstract());
        }
    }

    public String getAbtractsByDoi(String doi) {
        //String url = "http://dx.doi.org/" + "10.1038/35065725";
        String url = "http://dx.doi.org/" + doi;
        InputStream is = null;
        URL u = null;
        URLConnection hc;

        try {
            u = new URL(url);
            hc = u.openConnection();
            //hc.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 5.1)");
            //hc.setRequestProperty("User-Agent", "Mozilla/4.0 (Macintosh; U; Intel Mac OS X 10.3; de-DE; rv:1.9.2.2) Gecko/20100316 Firefox/3.5.1");
            //hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.1) Gecko/2008070208 Firefox/3.0.1");
            //hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.0; de-AT) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.2.149.29 Safari/525.13");
            //hc.setRequestProperty("User-Agent", "Opera/9.25 (Windows NT 6.1 U; de)");

            is = hc.getInputStream();

            String link = String.valueOf(hc.getURL());
            String keyword = "abstract";

            if (link.contains("springer.com") || link.contains("apa.org") || link.contains("oxfordjournals.org") || link.contains("acm.org") || link.contains("sciencedirect.com") || link.contains("http://projecteuclid.org/")) {
                keyword = "Abstract";
            }
            else if (link.contains("wiley.com")) {
                keyword = "References";
            }
            else if (link.contains("ahajournals.org")) {
                keyword = "Key Words:";
            }
            else if (link.contains("ingentaconnect.com") ) {
                keyword = "Abstract:";
            }
            else if (link.contains("hematologylibrary.org")) {
                keyword = "Author Affiliations";
            }
            else if (link.contains("sciencemag.org")) {
                keyword = "DOI:";
            }

            else {
                return "n/a";
            }

            String inputString  = org.apache.commons.io.IOUtils.toString(is, hc.getContentEncoding());

            //find abstract
            if (StringUtils.isNullOrEmpty(inputString)) {
                return "n/a";
            }

            Document doc = Jsoup.parse(inputString);
            Element bodyElement = doc.body();
            inputString = bodyElement.text();

            int start = inputString.indexOf(keyword);

            if (start < 0) {
                return "n/a";
            }
            start += keyword.length() + 1;
            int end = start + 1000;
            if (end > inputString.length()) {
                end = inputString.length();
            }

            String abstractString = inputString.substring(start, end);

            return "(AUTO)" + abstractString;

        } catch (MalformedURLException e) {
            //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "n/a";
    }
}
