package org.reichhold.robus;

import org.reichhold.robus.citeUlike.CulAssignment;
import org.reichhold.robus.citeUlike.CulDocument;
import org.reichhold.robus.citeUlike.CulTag;
import org.reichhold.robus.citeUlike.CulUser;
import org.reichhold.robus.db.DataStore;
import org.reichhold.robus.lucene.LuceneIndex;
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

        CulUser user = new CulUser();
        user.setId("userTest");
        user.setName("test user");

        CulDocument doc = new CulDocument();
        doc.setId("docID");
        doc.setPath("www.test.com/path/to/document.pdf");
        doc.setContentAbstract("this is the abstract of the test document");

        CulTag tag = new CulTag();
        tag.setTerm("tag1");

        CulAssignment ass1 = new CulAssignment();
        ass1.setUser(user);
        ass1.setDocument(doc);
        ass1.setTag(tag);
        ass1.setTimestamp("2013-01-03");

        DataStore store = new DataStore();
        store.saveOrUpdateCulAssignment(ass1);

        //Split TREC archive files into single HTML files
        //Trec2Html converter = new Trec2Html();
        //converter.createHtmlFiles();
    }
}