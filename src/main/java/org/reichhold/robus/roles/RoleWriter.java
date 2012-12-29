package org.reichhold.robus.roles;

import org.reichhold.robus.db.DataStore;
import org.reichhold.robus.jobs.CleanJobAd;
import org.reichhold.robus.roles.disco.DiscoReader;
import org.reichhold.robus.roles.nlp.NlpHelper;

import java.util.*;

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

        System.out.println("Start creating role terms for role " + role.getName());

        int maxRoleTerms = 50;
        int maxJobAds = 100;

        List<CleanJobAd> jobAds = getJobAds(role, maxJobAds);

        List<Job> jobs = new ArrayList<Job>();

        DiscoReader disco = new DiscoReader();
        disco.loadDiscoLevels();

        NlpHelper nlp = new NlpHelper();

        //for all jobAds:
        for (CleanJobAd jobAd : jobAds) {
            System.out.println("extracting terms from job " + jobAd.getJobId() + " " + jobAd.getTitle());

            //extract job terms for all three zones
            Job job = new Job(disco);
            job.getTitleZone().generateJobTerms(jobAd.getTitle(), nlp);
            job.getDescriptionZone().generateJobTerms(jobAd.getDescription(), nlp);
            job.getSkillsZone().generateJobTerms(jobAd.getSkills(), nlp);

            job.mergeAllJobTermsIntoRoleTerms(role);

            jobs.add(job);
        }

        List<RoleTerm> terms = mergeAllRoleTerms(jobs);

        return getTopRoleTerms(terms, maxRoleTerms);
    }

    private List<RoleTerm> getTopRoleTerms(List<RoleTerm> terms, int maxRoleTerms) {
        if (terms.size() <= maxRoleTerms) {
            return terms;
        }

        //only consider the terms with best weight
        Collections.sort(terms);
        return terms.subList(0, maxRoleTerms);
    }

    private List<RoleTerm> mergeAllRoleTerms(List<Job> jobs) {

        Map<String, RoleTerm> termMap = new HashMap<String, RoleTerm>();

        for (Job job : jobs) {

            for (Map.Entry<String, RoleTerm> entry : job.getRoleTerms().entrySet()) {

                RoleTerm term = entry.getValue();

                if(termMap.containsKey(term.getTerm())) {
                    //update weight
                    RoleTerm updateTerm = termMap.get(term.getTerm());
                    float currentWeight = updateTerm.getWeight();
                    currentWeight += term.getWeight();
                    updateTerm.setWeight(currentWeight);
                }
                else {
                    termMap.put(term.getTerm(), term);
                }
            }
        }

        List<RoleTerm> terms = new ArrayList<RoleTerm>(termMap.values());

        for (RoleTerm term : terms) {
            term.setWeight((float) Math.sqrt(term.getWeight()));
        }

        return terms;
    }

    private List<CleanJobAd> getJobAds(Role role, int maxJobAds) {
        return store.getCleanJobAdsByRole(role, maxJobAds);
    }
}
