package org.reichhold.robus.hbm;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: matthias
 * Date: 26.07.12
 * Time: 15:51
 * To change this template use File | Settings | File Templates.
 */
public class DataStore {
    Session session;

    public DataStore ()
    {
    }

    public int getNumberOfJobAds()
    {
        session = InitSessionFactory.getInstance().openSession();
        Transaction tx = session.beginTransaction();

        int count = ((Long)session.createQuery("select count(*) from JobAd").uniqueResult()).intValue();

        tx.commit();
        session.close();

        return count;
    }

    public List<String> getJobAdIds(int start, int limit, boolean withContent)
    {
        session = InitSessionFactory.getInstance().openSession();
        Transaction tx = session.beginTransaction();

        Query q;
        List result;

        if(withContent)
        {
            q = session.createQuery( "from JobAd where title != ''" );
        }
        else
        {
            q = session.createQuery( "from JobAd where title = ''" );
        }

        if(start > 0)
        {
            q.setFirstResult(start);
        }
        if (limit > 0)
        {
            q.setMaxResults(limit);
        }

        result = q.list();

        tx.commit();
        session.close();

        List<String> ids = new ArrayList<String>();

        for (JobAd j: (List<JobAd>) result)
        {
            ids.add(j.getJobId());
        }

        return ids;
    }

    public JobAd getJobAdById(String id)
    {
        session = InitSessionFactory.getInstance().openSession();
        Transaction tx = session.beginTransaction();

        JobAd job = (JobAd) session.get(JobAd.class, id);

        tx.commit();
        session.close();

        return job;
    }

    public void saveOrUpdateJobAd(JobAd j)
    {
        session = InitSessionFactory.getInstance().openSession();
        Transaction tx = session.beginTransaction();

        session.saveOrUpdate(j);

        tx.commit();
        session.close();
    }

    public void saveOrUpdateJobAds(List<JobAd> jobs)
    {
        int i = 0;

        session = InitSessionFactory.getInstance().openSession();
        Transaction tx = session.beginTransaction();

        for (JobAd j:jobs)
        {
            if(j != null)
            {
                session.saveOrUpdate(j);
                i++;
            }
        }

        tx.commit();
        session.close();
        System.out.println("Inserted " + i + " job details into JobAd table");
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++");
    }

    public List<Token> getActiveTokens()
    {
        session = InitSessionFactory.getInstance().openSession();
        Transaction tx = session.beginTransaction();

        List result = session.createQuery( "from Token where isActive = true").list();

        tx.commit();
        session.close();

        return (List<Token>) result;
    }

    public void saveToken(Token t)
    {
        session = InitSessionFactory.getInstance().openSession();
        Transaction tx = session.beginTransaction();

        session.save(t);

        tx.commit();
        session.close();
    }

    public void saveOrUpdateCleanJobAd(CleanJobAd element)
    {
        session = InitSessionFactory.getInstance().openSession();
        Transaction tx = session.beginTransaction();

        session.save(element);

        tx.commit();
        session.close();
    }

    public List<String> getCleanJobAdIds(int limit)
    {
        session = InitSessionFactory.getInstance().openSession();
        Transaction tx = session.beginTransaction();

        //List result = session.createQuery( "from CleanJobAd" ).list();
        List result = session.createSQLQuery("select jobID from CleanJobAd").list();

        if(limit == 0)
        {
            limit = result.size();
        }

        if(result.size() > limit)
        {
            result = result.subList(0, limit);
        }

        tx.commit();
        session.close();

        /*List<String> ids = new ArrayList<String>();

        for (CleanJobAd j: (List<CleanJobAd>) result)
        {
            ids.add(j.getJobId());
        }

        return ids;*/
        return result;
    }

}
