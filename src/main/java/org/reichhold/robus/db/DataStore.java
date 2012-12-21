package org.reichhold.robus.db;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.reichhold.robus.jobs.CleanJobAd;
import org.reichhold.robus.jobs.JobAd;
import org.reichhold.robus.jobs.Token;
import org.reichhold.robus.roles.Role;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: matthias
 * Date: 26.07.12
 * Time: 15:51
 * Allows access to database via Hibernate. Configuration see hibernate.cfg.xml
 */
public class DataStore {
    Session session;

    public DataStore ()
    {
        session = InitSessionFactory.getInstance().openSession();
    }

    public List<Role> getRolesByOrganisation (String organisation) {

        Transaction tx = session.beginTransaction();

        Query q = session.createQuery( "from Role where organisation = '" + organisation + "'");
        List<Role> result = q.list();

        tx.commit();

        return result;
    }

    public int getNumberOfJobAds()
    {
        Transaction tx = session.beginTransaction();

        int count = ((Long)session.createQuery("select count(*) from JobAd").uniqueResult()).intValue();

        tx.commit();

        return count;
    }

    public List<String> getJobAdIds(int start, int limit, boolean withContent)
    {
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

        List<String> ids = new ArrayList<String>();

        for (JobAd j: (List<JobAd>) result)
        {
            ids.add(j.getJobId());
        }

        return ids;
    }

    public JobAd getJobAdById(String id)
    {
        Transaction tx = session.beginTransaction();

        JobAd job = (JobAd) session.get(JobAd.class, id);

        tx.commit();

        return job;
    }

    public void saveOrUpdateJobAd(JobAd j)
    {
        Transaction tx = session.beginTransaction();

        session.saveOrUpdate(j);

        tx.commit();
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


    public List<CleanJobAd> getCleanJobAdsByRole(Role role, int limit) {
        session = InitSessionFactory.getInstance().openSession();
        Transaction tx = session.beginTransaction();

        String titleClause = "%" + role.getKeyword1() + " " + role.getKeyword2() + "%";

        //List result = session.createQuery( "from CleanJobAd where title like '" + whereClause + "'").list();
        List result = session.createQuery( "from CleanJobAd where title like :t")
                .setParameter("t", titleClause)
                .list();
        //todo: if result.size < MINIMUM adapt query

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

        return result;
    }

    public void saveOrUpdateRole(Role element) {
        List<Role> roles = getRoles(element.getOrganisation(), element.getName());

        session = InitSessionFactory.getInstance().openSession();
        Transaction tx = session.beginTransaction();

        for (Role r : roles) {
            //role with same name & organisation already exists
            //delete this role (and its role terms) first
            session.delete(r);
            session.flush();
        }

        session.save(element);

        tx.commit();
        session.close();
    }

    public List<Role> getRoles(String organisation, String name)
    {
        session = InitSessionFactory.getInstance().openSession();
        Transaction tx = session.beginTransaction();

        List<Role> results = session.createQuery( " from Role where name = :n and organisation = :o")
                .setParameter("o", organisation)
                .setParameter("n", name)
                .list();

        tx.commit();
        session.close();

        return results;
    }
}
