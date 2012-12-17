package org.reichhold.robus.jobAdData;

import org.reichhold.robus.model.DataStore;
import org.reichhold.robus.model.JobAd;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LinkedInCrawler {

    OAuthService service = null;
    Token accessToken = null;
    Token requestToken;
    Database db;
    DataStore store;

    public LinkedInCrawler()
    {
        //the linkedin service
        service = new ServiceBuilder()
                .provider(LinkedInApi.class)
                .apiKey("q3jdb27qxifx")
                .apiSecret("3LOkLwJLzrDM1C3U")
                .build();

        requestToken = service.getRequestToken();

        //the database
        store = new DataStore();
    }

    public void setJobDetails()
    {
        //load all active tokens from db
        List<org.reichhold.robus.model.Token> tokens = store.getActiveTokens();

        //for each token
        for(org.reichhold.robus.model.Token t:tokens)
        {
            //select jobads where title is emtpy
            List<String> ids = store.getJobAdIds(0, 1000, false);

            //create access token
            accessToken = new Token(t.getToken(), t.getSecret());
            System.out.println("---------------------------------------");
            System.out.println("Generated token for user " + t.getUser());

            //load job details
            List<JobAd> jobs = getJobDetails(ids, false);

            //save job details to db
            store.saveOrUpdateJobAds(jobs);
        }
    }

    public void setTokens(String email)
    {
        //Token t1 = crawler.requestNewToken("matthias.reichhold@imendo.at");
        //store.saveToken(t1);

        org.reichhold.robus.model.Token t2 = requestNewToken(email);
        store.saveToken(t2);
    }

    private void generateAccessToken(String token, String secret)
    {

    }

    private org.reichhold.robus.model.Token requestNewToken(String user){

        String authUrl = service.getAuthorizationUrl(requestToken);

        System.out.println("AuthUrl: " + authUrl);
        System.out.println("And paste the verifier here");
        System.out.print(">>");
        Scanner in = new Scanner(System.in);
        Verifier verifier = new Verifier(in.nextLine());

        Token linkedinToken = service.getAccessToken(requestToken, verifier);

        org.reichhold.robus.model.Token myToken = new org.reichhold.robus.model.Token();
        myToken.setUser(user);
        myToken.setToken(linkedinToken.getToken());
        myToken.setSecret(linkedinToken.getSecret());
        myToken.setActive(true);

        return myToken;
    }
    
    /***
     * Receives job ids from LinkedIn API and save them to database
     * @param start position to start from
     * @return number of received job ads
     */
    public int loadJobIds(int start){

        int count = 20;
        int max = start + 60000;
        boolean hasResults = true;

        String apiUrl = "http://api.linkedin.com/v1/job-search?";
        String crit1 = "job-title=Java+Developer&";
        String crit2= "country-code=us&count=" + count + "&";
        String startPre = "start=";

        while(hasResults && start < max)
        {
            System.out.println("Retrieving job data from API at pos " + start);

            OAuthRequest request = new OAuthRequest(Verb.GET, apiUrl + crit2 + startPre + start);
            //service.signRequest(accessToken, request);
            service.signRequest(accessToken, request);

            Response response = request.send();

            if(!response.isSuccessful())
            {
                System.out.println("Couldnt receive job ids from API; Response Code:" + response.getCode());
                System.out.println("Response Body:" + response.getBody());
                return start;
            }
            List<String> ids = getJobIds(response.getStream());
            if(ids == null || ids.size() <= 0)
            {
                db.Close();
                return start;
            }

            List<JobAd> jobs = getJobDetails(ids, true);

            if(jobs == null || jobs.size() <= 0)
            {
                db.Close();
                return start;
            }

            db.InsertJobAds(jobs);
            start += count;
        }

        db.Close();

        return start;
    }

    private List<JobAd> getJobDetails(List<String> ids, boolean getIdsOnly)
    {
        if(ids == null || ids.size() <= 0)
        {
            System.out.println("Didnt get any ids from API stream (ids == null)");
            return null;
        }

        List<JobAd> jobs = new ArrayList<JobAd>();
        int i = 0;

        for (String id:ids)
        {
            try {
                JobAd details = GetJobDetails(id, getIdsOnly);

                if(details == null)
                {
                    break;
                }

                jobs.add(details);
                i++;

                if(i%100 == 0)
                {
                    System.out.println(" Received job details # " + i);
                }
            }
            catch (Exception e) {
                System.out.println("Could not get details for job " + id);
                return jobs;
            }
        }

        return jobs;
    }

    private JobAd GetJobDetails(String jobId, boolean getIdOnly)
    {
        if(getIdOnly)
        {
            JobAd idJob = new JobAd();
            idJob.setJobId(jobId);
            return idJob;
        }

        String url1 = "http://api.linkedin.com/v1/jobs/";
        String url2 = ":(id,company,position,description,skills-and-experience)";

        OAuthRequest request = new OAuthRequest(Verb.GET, url1 + jobId + url2);

        service.signRequest(accessToken, request);

        Response response = request.send();

        if(!response.isSuccessful())
        {
            System.out.println("Couldnt receive details for job " + jobId + "; Response Code:" + response.getCode());
            System.out.println(response.getBody());
            return null;
        }

        InputStream input = response.getStream();

        JobAd job = new JobAd();
        NodeList elements = getElements(input, "job");
        if(elements == null || elements.getLength() == 0)
        {
            System.out.println("Couldnt get job elements for job" + jobId + "; Response Code:" + response.getCode());
            return null;
        }
        Element details = (Element)elements.item(0);
        job.setJobId(GetElementByTag(details, "id"));
        job.setCompanyName(GetElementByTag(details, "name"));
        job.setCompanyId(GetElementByTag(details, "name"));
        job.setTitle(GetElementByTag(details, "title"));
        job.setDescription(GetElementByTag(details, "description"));
        job.setSkills(GetElementByTag(details, "skills-and-experience"));

        return job;
    }

    private List<String> getJobIds(InputStream input)
    {
        List<String> ids = new ArrayList<String>();

        NodeList jobList = getElements(input, "job");

        if(jobList == null || jobList.getLength() <= 0)
        {
            System.out.println("Could not retrieve data from API (no job elements found)");
            return null;
        }

        for(int i = 0 ; i < jobList.getLength(); i++)
        {
            ids.add(GetElementByTag((Element) jobList.item(i), "id"));
        }

        return ids;
    }

    private String GetElementByTag(Element job, String tag)
    {
        String jobId = "";

        try
        {
            NodeList nl = job.getElementsByTagName(tag);
            Element el = (Element)nl.item(0);
            jobId = el.getFirstChild().getNodeValue();

            return jobId;
        }
        catch (Exception ex){
            //System.out.println("Could not find data for tag: " + tag);
            return "";
        }

    }

    private NodeList getElements(InputStream input, String tag)
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Document doc;
        NodeList list = null;
        try {
            doc = db.parse(input);
            list = doc.getElementsByTagName(tag);
        } catch (SAXException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NullPointerException e){
            System.out.println("No elements for tag " + tag);
        }

        return list;
    }
}
