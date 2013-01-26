package org.reichhold.robus.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.reichhold.robus.roles.Role;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * User: matthias
 * Date: 08.01.13
 */
public class LuceneSearcher {
    private String index;
    private String field;
    private IndexReader reader;
    private IndexSearcher searcher;
    private Analyzer analyzer;
    private BufferedReader in;
    private QueryParser parser;
    private int maxHits;

    public LuceneSearcher() {
        index = "/Users/matthias/Documents/workspace/robus/src/main/resources/culIndex";
        field = "contents";
        maxHits = 20;

        try {
            reader = DirectoryReader.open(FSDirectory.open(new File(index)));
            searcher = new IndexSearcher(reader);
            analyzer = new EnglishAnalyzer(Version.LUCENE_40);
            //analyzer = new StandardAnalyzer(Version.LUCENE_40);
            in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            parser = new QueryParser(Version.LUCENE_40, field, analyzer);
            searcher.setSimilarity(new BM25Similarity());

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public List<RoleSearchResult> doRoleSearchesForQuery(String query, List<String> roles) {
        List<RoleSearchResult> results = new ArrayList<RoleSearchResult>();
        //RoleSearchResult standardResult = doStandardSearch(query, 1000);
        //results.add(standardResult);

        for (String role:roles) {
            RoleSearchResult result = doRoleSearch(role, query, 1000);
            results.add(result);
        }
        return results;
    }

    public List<RoleSearchResult> doRoleSearches(Role role, List<String> queries, int maxResults) {
        List<RoleSearchResult> results = new ArrayList<RoleSearchResult>();

        for (String query:queries) {
            //RoleSearchResult result = doRoleSearch(role.getName(), query, maxResults);
            RoleSearchResult result = doOnDemandRoleSearch(role, query, maxResults);
                    results.add(result);
        }

        return results;
    }

    public List<RoleSearchResult> doStandardSearches(List<String> queries, int maxResults) {
        List<RoleSearchResult> results = new ArrayList<RoleSearchResult>();

        for (String query:queries) {
            RoleSearchResult result = doStandardSearch(query, maxResults);
            results.add(result);
        }

        return results;
    }

    public RoleSearchResult doRoleSearch(String roleName, String queryString, int maxResults) {
        RoleSearchResult result = new RoleSearchResult();
        result.setRoleName(roleName);
        result.setQuery(queryString);

        try {
            Query query = parser.parse(queryString);
            CustomScoreQuery roleScoreQuery = new RoleScoreQuery(query, roleName);
            TopDocs results = searcher.search(roleScoreQuery, null, maxResults);
            result.setResults(results);

            ScoreDoc[] hits = result.getResults().scoreDocs;
            List<Document> docs = new ArrayList<Document>();
            for (int i=0; i<hits.length; i++) {
                docs.add(searcher.doc(hits[i].doc));
            }
            result.setDocuments(docs);

            printResult(result);
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            return result;
        }

    }

    public RoleSearchResult doStandardSearch(String queryString, int maxResults) {
        RoleSearchResult result = new RoleSearchResult();
        result.setRoleName("standard");
        result.setQuery(queryString);

        try {
            Query query = parser.parse(queryString);
            TopDocs results = searcher.search(query, null, maxResults);
            result.setResults(results);

            ScoreDoc[] hits = result.getResults().scoreDocs;
            List<Document> docs = new ArrayList<Document>();
            for (int i=0; i<hits.length; i++) {
                docs.add(searcher.doc(hits[i].doc));
            }
            result.setDocuments(docs);

            //printResult(result);
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            return result;
        }
    }

    public RoleSearchResult doOnDemandRoleSearch(Role role, String queryString, int maxResults) {
        RoleSearchResult result = new RoleSearchResult();
        result.setRoleName(role.getName());
        result.setQuery(queryString);

        try {
            Query query = parser.parse(queryString);
            Query roleQuery = role.getBooleanQuery(field);
            CustomScoreQuery roleScoreQuery = new RoleScoreQuery(query, roleQuery);
            TopDocs results = searcher.search(roleScoreQuery, null, maxResults);

            result.setResults(results);

            ScoreDoc[] hits = result.getResults().scoreDocs;
            List<Document> docs = new ArrayList<Document>();
            for (int i=0; i<hits.length; i++) {
                docs.add(searcher.doc(hits[i].doc));
            }
            result.setDocuments(docs);

            //printResult(result);
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            return result;
        }

    }

    private void printResult(RoleSearchResult result) {
        ScoreDoc[] hits = result.getResults().scoreDocs;

        for (int i=0; i<hits.length; i++) {
            Document doc =  result.getDocuments().get(i);
            String path = doc.get("path");
            System.out.println((i+1) + ". " + "Role: " + result.getRoleName() + " Query:" + result.getQuery() + " Score:" + hits[i].score + " Path:"  + path);
        }
    }
}
