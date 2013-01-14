package org.reichhold.robus;

import org.reichhold.robus.citeulike.CulFileReader;
import org.reichhold.robus.citeulike.CulWebReader;
import org.reichhold.robus.citeulike.RoleCreator;
import org.reichhold.robus.jobs.DataCleaner;
import org.reichhold.robus.jobs.LinkedInCrawler;
import org.reichhold.robus.lucene.LuceneIndex;
import org.reichhold.robus.roles.RoleWriter;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Scanner;

public class Robus {
    public static void main(String[] args)
    {
        //create user roles from citulike users & tags
        loadUserRoles();

        //Load title and abstract from citeUlike.org
        //loadCiteUlikeMetaData();

        // Create Lucene Index
        createLuceneIndex();

        Evaluator evaluator = new Evaluator();
        evaluator.doMapEvaluation();


        /* create role vectors */
        //updateRolesByOrganisation();

        //create access tokens for LinkedIn API
        //createLinedInTokens();

        //Getting detailed job advertisement data from linkedIn
        //createJobAdDetails();

        //Save citeUlike data set from file to db
        //createCiteUlikeData();


        //************** test functions ****************

        //testJson();

        //testGson();
        //testNlpChunker();

        //Split TREC archive files into single HTML files
        //Trec2Html converter = new Trec2Html();
        //converter.createHtmlFiles();
    }

    private static void loadUserRoles() {
        RoleCreator roles = new RoleCreator();
        //roles.createUserRoles("internet");
        roles.createUserRoles();
    }

    private static void loadCiteUlikeMetaData() {

        CulWebReader reader = new CulWebReader();
        //reader.loadAllTitlesAndAbstracts();
        reader.loadTitlesAndAbtractsForQuery("internet");
    }

    private static void createCiteUlikeData() {
        CulFileReader cul = new CulFileReader();
        cul.fileToDb();
    }

    private static void testNlpChunker() {
        //String text = "your responsibilities will include design, development, unit testing, and performance tuning of enterprise-class integrations and applications.";
        //String text = "Marin is experiencing tremendous business growth and is seeking to add an outstanding individual with strong prior experience working within a client services organization as an Online Marketing Manager, an integral part of an Account Management team that manages both client retention and growth through proactive strategic recommendations and rapid response to client issues or questions";
        String text = "Rockwell International Corp.'s Tulsa unit said it signed \n" +
                "    a tentative agreement extending its contract with Boeing Co.\n" +
                "    to provide structural parts for Boeing's 747 jetliners.";
        //NlpHelper nlp = new NlpHelper();
        //nlp.textToChunks(text);
    }

    private static void updateRolesByOrganisation() {
        RoleWriter roles = new RoleWriter();
        roles.updateRoles("EvalCo");
    }

    /***
     * adds all given (HTML) documents
     */
    private static void createLuceneIndex() {
        LuceneIndex indexer = new LuceneIndex();
        //indexer.createIndexes();
        indexer.createCulIndex(true, false);
        //indexer.printAllDocsWithRoleScores();
    }

    private static void createLinedInTokens() {
        LinkedInCrawler crawler = new LinkedInCrawler();

        System.out.print("Enter email address for linkedIn crawler:");
        Scanner in = new Scanner(System.in);
        String input = in.nextLine();
        crawler.setTokens(input);
    }

    private static void createJobAdDetails() {
        System.out.print("Receiving job details from linkedin");

        LinkedInCrawler crawler = new LinkedInCrawler();
        crawler.setJobDetails();

        DataCleaner cleaner = new DataCleaner();
        cleaner.createCleanJobs();
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
    }
}