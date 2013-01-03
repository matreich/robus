package org.reichhold.robus.citeUlike;

import javax.persistence.*;
import java.util.List;

/**
 * User: matthias
 * Date: 03.01.13
 */
@Entity
@Table(name = "cul_tag")
public class CulTag {

    @Id
    @Column(name = "term")
    private String term;

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL)
    private List<CulAssignment> assignments;

    public CulTag() {
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public List<CulAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<CulAssignment> assignments) {
        this.assignments = assignments;
    }
}
