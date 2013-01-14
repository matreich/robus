package org.reichhold.robus.roles;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.hibernate.Session;
import org.reichhold.robus.db.DataStore;
import org.reichhold.robus.jobs.CleanJobAd;
import org.reichhold.robus.roles.disco.DiscoReader;
import org.reichhold.robus.roles.nlp.NlpHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * User: matthias
 * Date: 18.12.12
 */
public class RoleWriter {
    //private Role role;
    private DataStore store;

    public RoleWriter() {
        store = new DataStore();
    }

    /***
     * loads all roles for a given organisation; updates the role terms for each role
     * @param organisation the organisation
     */
    public void updateRoles(String organisation) {
        RoleReader reader = new RoleReader();
        reader.loadRoles(organisation);

        for (Role r : reader.getRoles()) {
            addOrUpdateRole(r);
        }

        store.deleteTermsByFrequency();
    }

    /***
     * Adds a new role and its role vector to the db
     * @param role the new role
     */
    public void addOrUpdateRole(Role role) {
        role.setRoleTerms(createRoleTerms(role));

        store.saveOrUpdateRole(role);
    }

    /***
     * creates a new role vector for a existing role
     * @param role the existing role without role terms
     */
    private List<RoleTerm> createRoleTerms(Role role) {


        int maxRoleTerms = 25;
        int maxJobAds = 10;

        List<CleanJobAd> jobAds = getJobAds(role, maxJobAds);
        System.out.println("\nStart creating role terms for role " + role.getName() + " out of " + jobAds.size() + " jobAds");

        List<Job> jobs = new ArrayList<Job>();

        DiscoReader disco = new DiscoReader();
        disco.loadDiscoLevels();

        NlpHelper nlp = new NlpHelper();

        for (CleanJobAd jobAd : jobAds) {
            System.out.println("extracting terms from job " + jobAd.getJobId() + " " + jobAd.getTitle());

            //extract job terms for all three zones
            Job job = new Job(disco);
            job.getTitleZone().generateJobTerms(jobAd.getTitle(), nlp);
            job.getDescriptionZone().generateJobTerms(jobAd.getDescription(), nlp);
            job.getSkillsZone().generateJobTerms(jobAd.getSkills(), nlp);

            job.mergeAllJobTermsIntoRoleTerms(role);

            jobs.add(job);
        }

        List<RoleTerm> terms = mergeAllRoleTerms(jobs);

        return getTopRoleTerms(terms, maxRoleTerms);
    }

    private List<RoleTerm> getTopRoleTerms(List<RoleTerm> terms, int maxRoleTerms) {
        if (terms.size() <= maxRoleTerms) {
            return terms;
        }

        //only consider the terms with best weight
        Collections.sort(terms);
        return terms.subList(0, maxRoleTerms);
    }

    private List<RoleTerm> mergeAllRoleTerms(List<Job> jobs) {

        Map<String, RoleTerm> termMap = new HashMap<String, RoleTerm>();

        for (Job job : jobs) {

            for (Map.Entry<String, RoleTerm> entry : job.getRoleTerms().entrySet()) {

                RoleTerm term = entry.getValue();

                if(termMap.containsKey(term.getTerm())) {
                    //update weight
                    RoleTerm updateTerm = termMap.get(term.getTerm());
                    float currentWeight = updateTerm.getWeight();
                    currentWeight += term.getWeight();
                    updateTerm.setWeight(currentWeight);
                }
                else {
                    termMap.put(term.getTerm(), term);
                }
            }
        }

        List<RoleTerm> terms = new ArrayList<RoleTerm>(termMap.values());

        for (RoleTerm term : terms) {
            term.setWeight((float) Math.sqrt(term.getWeight()));
        }

        return terms;
    }

    private List<CleanJobAd> getJobAds(Role role, int maxJobAds) {

        //get job ads (that contain the keyword in their title) from the db
        //return store.getCleanJobAdsByRole(role, maxJobAds);

        IndexReader reader;
        IndexSearcher searcher;
        Analyzer analyzer;
        QueryParser parser;
        String indexPath = "/Users/matthias/Documents/workspace/robus/src/main/resources/jobAdIndex";
        String queryString = role.getKeyword1() + " " + role.getKeyword2();
        List<CleanJobAd> jobs = new ArrayList<CleanJobAd>();

        try {
            store = new DataStore();
            reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
            searcher = new IndexSearcher(reader);
            analyzer = new EnglishAnalyzer(Version.LUCENE_40);
            parser = new QueryParser(Version.LUCENE_40, "contents", analyzer);
            searcher.setSimilarity(new BM25Similarity());

            Query query = parser.parse(queryString);
            TopDocs results = searcher.search(query, null, maxJobAds);

            ScoreDoc[] hits = results.scoreDocs;

            CleanJobAd job;
            Session session = store.getSession();
            for (int i=0; i<hits.length; i++) {
                String id = searcher.doc(hits[i].doc).get("jobId");
                job = (CleanJobAd) session.get(CleanJobAd.class, id);
                jobs.add(job);
            }

            //printResult(result);
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            return jobs;
        }
    }
}
