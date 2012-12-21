package org.reichhold.robus.roles;

import org.reichhold.robus.db.DataStore;

import java.util.List;

/**
 * User: matthias
 * Date: 15.12.12
 */
public class RoleReader {

    private String organisation;
    private List<Role> roles;
    private DataStore store;

    public RoleReader() {
        store = new DataStore();
    }

    public void loadRoles(String organisation) {
        this.organisation = organisation;

        roles = store.getRolesByOrganisation(organisation);
    }

    public String getOrganisation() {
        return organisation;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}
