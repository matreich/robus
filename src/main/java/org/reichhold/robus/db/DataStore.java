package org.reichhold.robus.db;

import com.mysql.jdbc.StringUtils;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.reichhold.robus.citeulike.CulAssignment;
import org.reichhold.robus.citeulike.CulDocument;
import org.reichhold.robus.citeulike.CulTag;
import org.reichhold.robus.citeulike.CulUser;
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

    public Session getSession() {
        return session;
    }

    public void closeSession() {
        session.close();
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

        //get all entries whose title contains kw1 and kw2
        List result = session.createQuery( "from CleanJobAd WHERE INSTR(title, :kw1) > 0 and INSTR(title, :kw2) > 0")
                .setParameter("kw1", role.getKeyword1())
                .setParameter("kw2", role.getKeyword2())
                .setMaxResults(limit)
                .list();

        //if result.size < MINIMUM adapt query?
        List result2 = null;
        if (result.size() < 5) {
            result2 = session.createQuery( "from CleanJobAd WHERE INSTR(title, :kw1) > 0 OR INSTR(title, :kw2) > 0")
                    .setParameter("kw1", role.getKeyword1())
                    .setParameter("kw2", role.getKeyword2())
                    .setMaxResults(limit)
                    .list();
        }

        if (result2 != null) {
            result.addAll(result2);
        }

        //todo: use lucene search and get top k job ads....

        /*if(limit == 0)
        {
            limit = result.size();
        }

        if(result.size() > limit)
        {
            result = result.subList(0, limit);
        }*/

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

    /***
     * terms that occur in many roles are not significant and can therfor be deleted
     * this methods delets all terms that occur in more then X roles
     * where X = (numberRoles - 1) / 2
     * @return number of deleted entries (role_terms)
     */
    public Integer deleteTermsByFrequency()
    {
        session = InitSessionFactory.getInstance().openSession();
        Transaction tx = session.beginTransaction();

        Integer numberRoles = ((Long)session.createQuery("select count(*) from Role").uniqueResult()).intValue();

        int minFreq;
        minFreq = numberRoles > 2 ? (numberRoles - 1) / 2 : 1;

        String query = "delete from role_term where term in (" +
                "select term from (" +
                "select term, count(term) c from role_term group by term order by c desc) t where c > :treshold)";

        int rows = session.createSQLQuery(query)
                .setParameter("treshold", minFreq)
                .executeUpdate();

        tx.commit();
        session.close();

        System.out.println("Deleted " + rows + " RoleTerms due to too high document frequency (IDF)");

        return rows;
    }

    public void saveOrUpdateObject(Object entity)
    {
        try {
            Transaction tx = session.beginTransaction();
            session.saveOrUpdate(entity);
            tx.commit();
        } catch (Exception e) {
          System.out.println("Error saving/updating entity!");
          e.printStackTrace();
        }
    }

    public long saveCulDocuments(List<CulDocument> entities) {
        long counter = 0;

        Session mySession = InitSessionFactory.getInstance().openSession();
        Transaction tx = mySession.beginTransaction();

        for (CulDocument e:entities)
        {
            try {
                mySession.save(e);
                counter = counter + 1;

            } catch (Exception ex) {
                //ex.printStackTrace();
            }
        }
        mySession.flush();
        mySession.clear();
        tx.commit();
        return counter;
    }

    public long saveCulTags(List<CulTag> entities) {
        long counter = 0;

        Session mySession = InitSessionFactory.getInstance().openSession();
        Transaction tx = mySession.beginTransaction();

        for (CulTag e:entities)
        {
            try {
                mySession.save(e);
                counter = counter + 1;

            } catch (Exception ex) {
                //ex.printStackTrace();
            }
        }
        mySession.flush();
        mySession.clear();
        tx.commit();
        return counter;
    }

    public long saveCulAssignments(List<CulAssignment> entities) {
        long counter = 0;

        Session mySession = InitSessionFactory.getInstance().openSession();
        Transaction tx = mySession.beginTransaction();

        for (CulAssignment e:entities)
        {
            try {
                mySession.save(e);
                counter = counter + 1;

            } catch (Exception ex) {
                //ex.printStackTrace();
            }
        }
        mySession.flush();
        mySession.clear();
        tx.commit();
        return counter;
    }

    public long saveCulUsers(List<CulUser> entities) {

        long counter = 0;

        Session mySession = InitSessionFactory.getInstance().openSession();
        Transaction tx = mySession.beginTransaction();

        for (CulUser e:entities)
        {
            try {
                //System.out.println(counter + ": " + e.getId());
                mySession.save(e);
                counter = counter + 1;

            } catch (Exception ex) {
                //ex.printStackTrace();
            }
        }
        mySession.flush();
        mySession.clear(); // not needed ? (see below)
        tx.commit();
        return counter;
    }

    public List<CulDocument> getEmptyCulDocuments(int number) {
        Transaction tx = session.beginTransaction();

        Query q = session.createQuery( "from CulDocument where path is null" );
        q.setMaxResults(number);
        List<CulDocument> result = q.list();

        tx.commit();

        return result;
    }

    public void saveOrUpdateObjects(List entities, String entityName) {
        long counter = 0;

        //Session mySession = InitSessionFactory.getInstance().openSession();
        Transaction tx = session.beginTransaction();


        for (Object e:entities)
        {
            try {
                //mySession.persist(entityName, e);
                session.saveOrUpdate(entityName, e);
                counter = counter + 1;

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        session.flush();
        //mySession.clear(); // not needed ? (see below)
        tx.commit();
    }

    public List<CulAssignment> getAssignmentsForUser(String userId) {
        Session mySession = InitSessionFactory.getInstance().openSession();
        Transaction tx = mySession.beginTransaction();

        CulUser user = new CulUser();
        user.setId(userId);

        //CulUser user = (CulUser) mySession.get(CulUser.class, userId);

        Query q = mySession.createQuery( "from CulAssignment where user = :u" );
        q.setParameter("u", user);
        q.setMaxResults(1000);
        List<CulAssignment> result = q.list();

        tx.commit();
        mySession.close();

        return result;
    }

    public List<CulUser> getCulUsersByTag(String term, int maxResults) {
        //select a.user, count(a.user) number from cul_assignment a where a.tag = 'java' group by a.user order by number desc;
        CulTag tag = new CulTag();
        tag.setTerm(term);

        Transaction tx = session.beginTransaction();

        Query q = session.createQuery( "select user from CulAssignment where tag = :tag group by user order by count(user) desc" );
        q.setParameter("tag", tag);
        q.setMaxResults(maxResults);
        List<CulUser> result = q.list();

        tx.commit();

        return result;
    }

    public List<CulUser> getCulUsersByNumberOfTags(int numberTags, int maxResults) {

        int min = numberTags - 50;
        int max = numberTags + 50;
        Transaction tx = session.beginTransaction();

        Query q = session.createSQLQuery("select user from ( select user, count(*) c from cul_assignment group by user) ca2 where c > :min and c < :max");
        //Query q = session.createSQLQuery("select user from ( select user, count(*) c from cul_assignment group by user) ca2 where c > 98 and c < 102");
        q.setParameter("min", min);
        q.setParameter("max", max);
        q.setMaxResults(maxResults);
        List<String> results = q.list();

        tx.commit();

        List<CulUser> users = new ArrayList<CulUser>();
        for (String result:results) {
            users.add((CulUser) session.get(CulUser.class, result));
        }

        return users;
    }

    public CulUser getCulUserByRoleName(String role) {
        Transaction tx = session.beginTransaction();

        Query q = session.createQuery( "from CulUser where robusRole = :role" );
        q.setParameter("role", role);
        CulUser user = (CulUser) q.uniqueResult();
        tx.commit();

        return user;
    }

    public List<CulDocument> getDocumentsByTag(String tagTerm) {
        Transaction tx = session.beginTransaction();

        CulTag tag = (CulTag) session.get(CulTag.class, tagTerm);

        Query q = session.createSQLQuery("select distinct(document) from cul_assignment where tag = :tag");
        q.setParameter("tag", tag);
        //and (user = 'fd72178f9f812a46ba4f7c599858cd7a' or user = '617e233adc60a7573c5e5025358250fd')

        List<CulDocument> result = new ArrayList<CulDocument>();
        ScrollableResults results = q.scroll();

        while (results.next()) {

            CulDocument doc = (CulDocument) session.get(CulDocument.class, (String)results.get(0));

            if(!StringUtils.isNullOrEmpty(doc.getPath())) {
                continue;
            }

            result.add(doc);
            if (result.size() == 100) {
                break;
            }
        }

        tx.commit();

        return result;
    }

    public List<CulDocument> getDocumentsBySqlQuery(String query) {
        Transaction tx = session.beginTransaction();

        Query q = session.createSQLQuery(query);

        List<CulDocument> result = new ArrayList<CulDocument>();
        ScrollableResults results = q.scroll();

        while (results.next()) {

            CulDocument doc = (CulDocument) session.get(CulDocument.class, (String)results.get(0));

            if(!StringUtils.isNullOrEmpty(doc.getPath())) {
                continue;
            }

            result.add(doc);
            if (result.size() == 100) {
                break;
            }
        }

        tx.commit();

        return result;
    }
}
