package org.reichhold.robus.citeulike;

import javax.persistence.*;
import java.util.List;

/**
 * User: matthias
 * Date: 03.01.13
 */
@Entity
@Table(name = "cul_document_eval")
public class CulDocument {

    @Id
    @Column(name = "id")
    private String id;

    @Column (name = "path")
    private String path;

    @Column (name = "content_abstract")
    private String contentAbstract;

    @Column (name = "title")
    private String title;

    @Column (name = "doi")
    private String doi;

    @Column (name = "pmid")
    private String pmid;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL)
    private List<CulAssignment> assignments;

    public CulDocument() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getPmid() {
        return pmid;
    }

    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentAbstract() {
        return contentAbstract;
    }

    public void setContentAbstract(String contentAbstract) {
        this.contentAbstract = contentAbstract;
    }

    public List<CulAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<CulAssignment> assignments) {
        this.assignments = assignments;
    }
}
