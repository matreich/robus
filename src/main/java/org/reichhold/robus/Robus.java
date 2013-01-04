package org.reichhold.robus;

import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import org.reichhold.robus.citeUlike.CulFileReader;
import org.reichhold.robus.lucene.LuceneIndex;
import org.reichhold.robus.roles.RoleWriter;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

public class Robus {
    public static void main(String[] args)
    {
        /* Load Job data from linkedIn
        Scanner in = new Scanner(System.in);
        String input = "";

        LinkedInCrawler crawler = new LinkedInCrawler();

        system.out.print("Would you like to create a new token (y/n)?");
        input = in.nextLine();

        if(input.equals("y"))
        {
            System.out.print("Enter email adress:");
            input = in.nextLine();
            crawler.setTokens(input);
        }

        System.out.print("Would you like to receive job details (y/n)?");
        input = in.nextLine();

        if(input.equals("y"))
        {
            crawler.setJobDetails();

            DataCleaner cleaner = new DataCleaner();
            cleaner.createCleanJobs();
        }
        */


        /*System.out.print("Would you like to create the Lucene Index (y/n)?");
        input = in.nextLine();

        if(input.equals("y"))
        { */

        /* Create Lucene Index */
        LuceneIndex indexer = new LuceneIndex();
        //indexer.createIndexes();
        //indexer.computeRoleScores();
        //indexer.printAllDocsWithRoleScores();

        /* create role vectors */
        RoleWriter roles = new RoleWriter();
        //roles.updateRoles("EvalCo");

        //String text = "your responsibilities will include design, development, unit testing, and performance tuning of enterprise-class integrations and applications.";
        //String text = "Marin is experiencing tremendous business growth and is seeking to add an outstanding individual with strong prior experience working within a client services organization as an Online Marketing Manager, an integral part of an Account Management team that manages both client retention and growth through proactive strategic recommendations and rapid response to client issues or questions";
        String text = "Rockwell International Corp.'s Tulsa unit said it signed \n" +
                "    a tentative agreement extending its contract with Boeing Co.\n" +
                "    to provide structural parts for Boeing's 747 jetliners.";
        //NlpHelper nlp = new NlpHelper();
        //nlp.textToChunks(text);

        CulFileReader cul = new CulFileReader();
        //cul.fileToDb();

        testJson();

        //Split TREC archive files into single HTML files
        //Trec2Html converter = new Trec2Html();
        //converter.createHtmlFiles();
    }

    private static void testJson() {
        //String url = "http://www.citeulike.org/json/user/matreich";
        String url = "http://www.citeulike.org/json/article/11862427";
        InputStream is = null;
        try {
            is = new URL(url).openStream();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);

            JSONObject json = new JSONObject(jsonText);
            String text = json.toString();
            String path = json.get("href").toString();
            String title = json.get("title").toString();
            json.get("abstract");


        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JSONException e) {
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
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private static void testGson() {
        //String url = "http://www.citeulike.org/json/user/matreich";
        String url = "http://www.citeulike.org/json/article/11862427";

        //http://www.citeulike.org/user/matreich/article/11862427
        InputStream is = null;
        try {
            is = new URL(url).openStream();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Reader reader = new BufferedReader((new InputStreamReader(is, Charset.forName("UTF-8"))));

        Gson gson = new Gson();
        gson.fromJson(reader, Object.class);
        String text = gson.toString();
    }
}