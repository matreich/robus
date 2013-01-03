package org.reichhold.robus.citeUlike;

import javax.persistence.*;

/**
 * User: matthias
 * Date: 03.01.13
 */
@Entity
@Table (name = "cul_assignment")
public class CulAssignment {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column (name = "id")
    private long id;

    @Column(name = "timestamp")
    private String timestamp;

    @ManyToOne ( cascade = CascadeType.ALL )
    @JoinColumn (name = "user")
    private CulUser user;

    @ManyToOne ( cascade = CascadeType.ALL )
    @JoinColumn (name = "document")
    private CulDocument document;

    @ManyToOne ( cascade = CascadeType.ALL )
    @JoinColumn (name = "tag")
    private CulTag tag;

    public CulAssignment() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public CulUser getUser() {
        return user;
    }

    public void setUser(CulUser user) {
        this.user = user;
    }

    public CulDocument getDocument() {
        return document;
    }

    public void setDocument(CulDocument document) {
        this.document = document;
    }

    public CulTag getTag() {
        return tag;
    }

    public void setTag(CulTag tag) {
        this.tag = tag;
    }
}
