package org.reichhold.robus.citeUlike;

import javax.persistence.*;
import java.util.List;

/**
 * User: matthias
 * Date: 03.01.13
 */
@Entity
@Table(name = "cul_user")
public class CulUser {

    @Id
    @Column (name = "id")
    private String id;

    @Column (name = "name")
    private String name;

    @OneToMany (mappedBy = "user", cascade = CascadeType.ALL)
    private List<CulAssignment> assignments;

    public CulUser() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CulAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<CulAssignment> assignments) {
        this.assignments = assignments;
    }
}
