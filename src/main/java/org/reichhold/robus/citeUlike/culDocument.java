package org.reichhold.robus.citeUlike;

import javax.persistence.*;
import java.util.List;

/**
 * User: matthias
 * Date: 03.01.13
 */
@Entity
@Table(name = "cul_document")
public class CulDocument {

    @Id
    @Column(name = "id")
    private String id;

    @Column (name = "path")
    private String path;

    @Column (name = "content_abstract")
    private String contentAbstract;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
