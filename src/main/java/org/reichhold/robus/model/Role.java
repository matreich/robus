package org.reichhold.robus.model;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;

import javax.persistence.*;
import java.util.List;

/**
 * User: matthias
 * Date: 15.12.12
 */
@Entity
@Table (name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column (name = "role_id")
    private long roleId;

    @Column
    private String organisation;

    @Column
    private String name;

    @OneToMany (mappedBy = "role")
    private List<RoleTerm> roleTerms;

    public long getRoleId() {
        return roleId;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RoleTerm> getRoleTerms() {
        return roleTerms;
    }

    public void setRoleTerms(List<RoleTerm> roleTerms) {
        this.roleTerms = roleTerms;
    }

    @Transient
    /***
     * Creates a boolean query from all role terms and their weights
     * @param field the lucene field used for the query
     * @return a BooleanQuery including all weighted role terms (as TermQuery + boost)
     */
    public BooleanQuery getBooleanQuery(String field) {
        BooleanQuery query = new BooleanQuery();

        for (RoleTerm term : roleTerms) {
            TermQuery tq = new TermQuery(new Term(field, term.getTerm()));
            tq.setBoost(term.getWeight());
            query.add(tq, BooleanClause.Occur.SHOULD);
        }

        return query;
    }
}
