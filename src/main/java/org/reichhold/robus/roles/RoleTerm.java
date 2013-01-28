package org.reichhold.robus.roles;

import javax.persistence.*;

/**
 * User: matthias
 * Date: 15.12.12
 */
@Entity
@Table (name = "role_term")
public class RoleTerm implements Comparable<RoleTerm> {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column (name = "term_id")
    private long termId;

    @Column (name = "term")
    private String term;

    @Column (name = "weight" )
    private Float weight;

    //@ManyToOne
    @ManyToOne ( cascade = CascadeType.ALL )
    @JoinColumn (name = "role")
    private Role role;

    public RoleTerm() {
    }

    public RoleTerm(String term, Float weight, Role role) {
        this.term = term;
        this.weight = weight;
        this.role = role;
    }

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

    @Override
    public int compareTo(RoleTerm roleTerm) {
        if (this.getWeight() < roleTerm.getWeight()) {
            return 1;
        } else {
            return -1;
        }
    }
}
