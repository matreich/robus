/**
 * Created with IntelliJ IDEA.
 * User: matthias
 * Date: 21.07.12
 * Time: 12:15
 * To change this template use File | Settings | File Templates.
 */
package org.reichhold.robus.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class JobAd {
    @Id
    String jobId;
    String companyId;
    String companyName;
    String title;
    String description;
    String skills;

    public JobAd() {
        this.jobId = "";
        this.companyId = "";
        this.companyName = "";
        this.title = "";
        this.description = "";
        this.skills = "";
    }

    public JobAd(String JobId, String companyId, String companyName, String title, String description, String skills) {
        this.jobId = JobId;
        this.companyId = companyId;
        this.companyName = companyName;
        this.title = title;
        this.description = description;
        this.skills = skills;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }
}
