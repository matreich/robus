package org.reichhold.robus.citeulike;

import javax.persistence.*;
import java.util.List;

/**
 * User: matthias
 * Date: 03.01.13
 */
@Entity
@Table(name = "cul_user_eval")
public class CulUser {

    @Id
    @Column (name = "id")
    private String id;

    @Column (name = "name")
    private String name;

    @Column (name = "robus_role")
    private String robusRole;

    @OneToMany (mappedBy = "user", cascade = CascadeType.ALL)
    private List<CulAssignment> assignments;

    public CulUser() {
    }

    public CulUser(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRobusRole() {
        return robusRole;
    }

    public void setRobusRole(String robusRole) {
        this.robusRole = robusRole;
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
