package org.reichhold.robus;

import org.reichhold.robus.search.LuceneIndex;

import java.util.Scanner;

public class Robus {
    public static void main(String[] args)
    {
        Scanner in = new Scanner(System.in);
        String input = "";

        LinkedInCrawler crawler = new LinkedInCrawler();

        /*System.out.print("Would you like to create a new token (y/n)?");
        input = in.nextLine();

        if(input.equals("y"))
        {
            System.out.print("Enter email adress:");
            input = in.nextLine();
            crawler.setTokens(input);
        } */

        /*System.out.print("Would you like to receive job details (y/n)?");
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
        LuceneIndex indexer = new LuceneIndex();
        indexer.createIndex();


        //RoleSimilarityBase sim = new RoleSimilarityBase();
        //sim.computeCosSim();

        //Split TREC archive files into single HTML files
        //Trec2Html converter = new Trec2Html();
        //converter.createHtmlFiles();

    }
}