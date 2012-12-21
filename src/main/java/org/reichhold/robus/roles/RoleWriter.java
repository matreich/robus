package org.reichhold.robus.roles;

import org.reichhold.robus.db.DataStore;
import org.reichhold.robus.jobs.CleanJobAd;

import java.util.ArrayList;
import java.util.List;

/**
 * User: matthias
 * Date: 18.12.12
 */
public class RoleWriter {
    //private Role role;
    private DataStore store;

    public RoleWriter() {
        store = new DataStore();
    }

    /***
     * loads all roles for a given organisation; updates the role terms for each role
     * @param organisation the organisation
     */
    public void updateRoles(String organisation) {
        RoleReader reader = new RoleReader();
        reader.loadRoles(organisation);

        for (Role r : reader.getRoles()) {
            addOrUpdateRole(r);
        }
    }

    /***
     * Adds a new role and its role vector to the db
     * @param role the new role
     */
    public void addOrUpdateRole(Role role) {
        role.setRoleTerms(createRoleTerms(role));

        store.saveOrUpdateRole(role);
    }

    /***
     * creates a new role vector for a existing role
     * @param role the existing role without role terms
     */
    private List<RoleTerm> createRoleTerms(Role role) {
        int maxRoleTerms = 100;
        int maxJobAds = 100;

        List<CleanJobAd> jobAds = getJobAds(role, maxJobAds);

        List<Job> jobs = new ArrayList<Job>();

        //for all jobAds:
        for (CleanJobAd jobAd : jobAds) {

            // do tokenizing and POS tagging
            //todo: gorß-/kleinschreibung --> alle terms klein machen?

            //extract job terms for all three zones
            Job job = new Job();
            job.getTitleZone().generateJobTerms(jobAd.getTitle());
            job.getDescriptionZone().generateJobTerms(jobAd.getDescription());
            job.getSkillsZone().generateJobTerms(jobAd.getSkills());

            job.mergeAllJobTermsIntoRoleTerms(role);

            jobs.add(job);
        }

        //todo: merge the weighted terms of all jobs into one list of role terms

        //todo: terms.sortByRoleTerm.weight
        List<RoleTerm> terms = new ArrayList<RoleTerm>();

        return terms;
    }

    private List<CleanJobAd> getJobAds(Role role, int maxJobAds) {
        return store.getCleanJobAdsByRole(role, maxJobAds);
    }
}
