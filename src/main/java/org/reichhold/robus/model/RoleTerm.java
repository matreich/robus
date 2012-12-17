package org.reichhold.robus.model;

import javax.persistence.*;

/**
 * User: matthias
 * Date: 15.12.12
 */
@Entity
@Table (name = "role_term")
public class RoleTerm {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column (name = "term_id")
    private long termId;

    @Column (name = "term")
    private String term;

    @Column (name = "weight" )
    private Float weight;

    @ManyToOne
    @JoinColumn (name = "role")
    private Role role;

    public long getTermId() {
        return termId;
    }

    public void setTermId(long termId) {
        this.termId = termId;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
