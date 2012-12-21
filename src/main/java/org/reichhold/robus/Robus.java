package org.reichhold.robus;

import org.reichhold.robus.roles.Role;
import org.reichhold.robus.roles.RoleWriter;

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

        /* Create Lucene Index
        LuceneIndex indexer = new LuceneIndex();
        indexer.createIndexes();
        indexer.computeRoleScores();

        indexer.printAllDocsWithRoleScores();
        */

        /* create role vectors */

        RoleWriter roles = new RoleWriter();
        //roles.updateRoles("EvalCo");

        Role webDeveloper = new Role();
        webDeveloper.setName("WebDeveloper");
        webDeveloper.setOrganisation("EvalCo");
        webDeveloper.setKeyword1("Web");
        webDeveloper.setKeyword2("Developer");

        roles.addOrUpdateRole(webDeveloper);


        String test = "Skilled web developer with PHP and JS expertise. " +
                "Solid understanding of standards compliant HTML and CSS. " +
                "Must possess a can-do attitude and a passion for technology";


        //Split TREC archive files into single HTML files
        //Trec2Html converter = new Trec2Html();
        //converter.createHtmlFiles();
    }
}