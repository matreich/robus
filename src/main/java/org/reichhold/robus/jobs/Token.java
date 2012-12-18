package org.reichhold.robus.jobs;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created with IntelliJ IDEA.
 * User: matthias
 * Date: 26.07.12
 * Time: 14:13
 * To change this template use File | Settings | File Templates.
 */

@Entity
public class Token {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int Id;
    private String user;
    private String token;
    private String secret;
    private boolean isActive;

    public Token() {
    }

    public Token(int id, String user, String token, String secret, boolean active) {
        Id = id;
        this.user = user;
        this.token = token;
        this.secret = secret;
        isActive = active;
    }


    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
