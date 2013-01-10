package org.reichhold.robus.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.TopDocs;

import java.util.List;

/**
 * User: matthias
 * Date: 08.01.13
 */
public class RoleSearchResult {
    private String roleName;
    private String query;
    private TopDocs results;
    private List<Document> documents;
    private Float averagePrecision;

    public RoleSearchResult() {
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public TopDocs getResults() {
        return results;
    }

    public void setResults(TopDocs results) {
        this.results = results;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public Float getAveragePrecision() {
        return averagePrecision;
    }
}
