package org.reichhold.robus.roles;

import org.reichhold.robus.roles.disco.DiscoReader;

import java.util.HashMap;
import java.util.Map;

/**
 * User: matthias
 * Date: 19.12.12
 */
public class Job {

    private JobZone titleZone;
    private JobZone descriptionZone;
    private JobZone skillsZone;

    private Map<String, RoleTerm> roleTerms;

    public Job(DiscoReader disco) {
        titleZone = new JobZone(0.5f, disco);
        descriptionZone = new JobZone(0.2f, disco);
        skillsZone = new JobZone(0.3f, disco);

        roleTerms = new HashMap<String, RoleTerm>();
    }

    public JobZone getTitleZone() {
        return titleZone;
    }

    public void setTitleZone(JobZone titleZone) {
        this.titleZone = titleZone;
    }

    public JobZone getDescriptionZone() {
        return descriptionZone;
    }

    public void setDescriptionZone(JobZone descriptionZone) {
        this.descriptionZone = descriptionZone;
    }

    public JobZone getSkillsZone() {
        return skillsZone;
    }

    public void setSkillsZone(JobZone skillsZone) {
        this.skillsZone = skillsZone;
    }

    public Map<String, RoleTerm> getRoleTerms() {
        return roleTerms;
    }

    /***
     * merges for each term all term weights from all zones into one single term weight
     * adds one entry to the role term list.
     */
    public void mergeAllJobTermsIntoRoleTerms(Role role) {
        for (Map.Entry<String, JobTerm> entry : descriptionZone.getTerms().entrySet()){

            JobTerm descriptionTerm = entry.getValue();
            JobTerm titleTerm = null;
            JobTerm skillsTerm = null;

            if(titleZone.getTerms().containsKey(descriptionTerm.getTerm())) {
                titleTerm = titleZone.getTerms().get(descriptionTerm.getTerm());

                //remove term from list so that it is not considered once in merging process
                titleZone.getTerms().remove(titleTerm.getTerm());
            }

            if(skillsZone.getTerms().containsKey(descriptionTerm.getTerm())) {
                skillsTerm = skillsZone.getTerms().get(descriptionTerm.getTerm());

                //remove term from list so that it is not considered once in merging process
                skillsZone.getTerms().remove(skillsTerm.getTerm());
            }

            Float termWeight = computeTermWeight(descriptionTerm, titleTerm, skillsTerm);

            roleTerms.put(descriptionTerm.getTerm(), new RoleTerm(descriptionTerm.getTerm(), termWeight, role));
        }

        for (Map.Entry<String, JobTerm> entry : titleZone.getTerms().entrySet()){

            JobTerm titleTerm = entry.getValue();
            JobTerm skillsTerm = null;

            if(skillsZone.getTerms().containsKey(titleTerm.getTerm())) {
                skillsTerm = skillsZone.getTerms().get(titleTerm.getTerm());

                //remove term from list so that it is not considered once in merging process
                skillsZone.getTerms().remove(skillsTerm.getTerm());
            }

            Float termWeight = computeTermWeight(null, titleTerm, skillsTerm);

            roleTerms.put(titleTerm.getTerm(), new RoleTerm(titleTerm.getTerm(), termWeight, role));
        }

        for (Map.Entry<String, JobTerm> entry : skillsZone.getTerms().entrySet()){

            JobTerm skillsTerm = entry.getValue();

            Float termWeight = computeTermWeight(null, null, skillsTerm);

            roleTerms.put(skillsTerm.getTerm(), new RoleTerm(skillsTerm.getTerm(), termWeight, role));
        }
    }

    private Float computeTermWeight(JobTerm descriptionTerm, JobTerm titleTerm, JobTerm skillsTerm) {
        Float descriptionWeight = 0.0f;
        if (descriptionTerm != null) {
            descriptionWeight = 0.2f * descriptionTerm.getWeight();
        }

        Float titleWeight = 0.0f;
        if(titleTerm != null) {
            titleWeight = 0.5F * titleTerm.getWeight();
        }

        Float skillsWeight = 0.0f;
        if(skillsTerm != null) {
            skillsWeight = 0.5f * skillsTerm.getWeight();
        }

        return descriptionWeight + titleWeight + skillsWeight;
    }
}
