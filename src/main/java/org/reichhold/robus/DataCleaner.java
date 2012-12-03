package org.reichhold.robus;

import org.jsoup.Jsoup;
import org.reichhold.robus.hbm.CleanJobAd;
import org.reichhold.robus.hbm.DataStore;
import org.reichhold.robus.hbm.JobAd;

import java.util.List;

public class DataCleaner {

    DataStore store;

    public DataCleaner(){
        //the database
        store = new DataStore();
    }

    public void createCleanJobs(){

        int limit = 10000;
        int max = store.getNumberOfJobAds();

        for(int start = 0; start < max; start += limit)
        {
            //select jobID from JobAd where title != ''
            List<String> rawJobs = store.getJobAdIds(start, limit, true);

            //select jobID from CleanJobAd
            List<String> cleanJobs = store.getCleanJobAdIds(0);

            System.out.println("Raw jobs: " + rawJobs.size() + "; clean jobs: " + cleanJobs.size());

            //remove jobs that have already been cleaned
            for(String cj:cleanJobs)
            {
                rawJobs.remove(cj);
            }
            System.out.println("Raw jobs to clean: " + rawJobs.size());

            int i = 0;
            for(String id:rawJobs)
            {
                //get JobAd details
                JobAd raw = store.getJobAdById(id);

                //create new CleanJobAd object
                CleanJobAd clean = new CleanJobAd();
                clean.setJobId(raw.getJobId());
                clean.setCompanyId(raw.getCompanyId());
                clean.setCompanyName(raw.getCompanyName());

                clean.setTitle(Jsoup.parse(raw.getTitle()).text());
                clean.setDescription(Jsoup.parse(raw.getDescription()).text());
                clean.setSkills(Jsoup.parse(raw.getSkills()).text());

                //save cleanJob to db
                store.saveOrUpdateCleanJobAd(clean);
                i++;
            }
            System.out.println("inserted " + i + " new clean jobs; start: " + start);
        }
    }
}
