package org.reichhold.robus.citeulike;

import com.mysql.jdbc.StringUtils;
import org.reichhold.robus.db.DataStore;
import org.reichhold.robus.roles.Role;
import org.reichhold.robus.roles.RoleWriter;

import java.util.*;

/**
 * User: matthias
 * Date: 06.01.13
 */
public class RoleCreator {
    private DataStore store;
    private List<String> stopwords;
    public static boolean ASC = true;
    public static boolean DESC = false;
    private String organisation;
    private String searchTag;

    public RoleCreator() {

        organisation = "CiteULike";
        //searchTag = "java";
    }

    /***
     * creates a role for each citeUlike user.
     * Top most popular CUL tags are used as role keywords
     */
    public void createUserRoles(String searchTag) {
        this.searchTag = searchTag;
        store = new DataStore();
        List<CulUser> users = store.getCulUsersByTag(searchTag, 200);

        createUserRoles(users, "CUL100");
    }

    public void createUserRoles() {
        List<CulUser> users = new ArrayList<CulUser>();
        store = new DataStore();

        //for testing only...
        //users.add(new CulUser("617e233adc60a7573c5e5025358250fd"));  //internet-marketing
        //users.add(new CulUser("fd72178f9f812a46ba4f7c599858cd7a"));  //security internet

        /*users.add((CulUser) store.getSession().get(CulUser.class, "6c6607d9867205b88b4f024992e02d53"));
        users.add((CulUser) store.getSession().get(CulUser.class, "d028927e630155f99aa0c564644dcb23"));
        users.add((CulUser) store.getSession().get(CulUser.class, "c93dd93c877452c3d64fa8e148544d3b"));
        users.add((CulUser) store.getSession().get(CulUser.class, "ac4f8a03e1046b06f89c309a019f4d70"));
        users.add((CulUser) store.getSession().get(CulUser.class, "4b9e02136874b6a1b77903e066cbdc98"));
        users.add((CulUser) store.getSession().get(CulUser.class, "3f0f34326d630fa692b4fe91a62d4b92"));
        users.add((CulUser) store.getSession().get(CulUser.class, "2f4505a49b221ac047990e84d24c14ac"));
        users.add((CulUser) store.getSession().get(CulUser.class, "0556114be83c6b3c7228e08d92525c96"));
        users.add((CulUser) store.getSession().get(CulUser.class, "4115bb6343ed947b1f23550d879a9db9"));
        users.add((CulUser) store.getSession().get(CulUser.class, "f143ae11633e71998e1111cd033b0d85"));*/

        users = store.getCulUsersByNumberOfTags(1500, 100);

        createUserRoles(users, "CUL1500");
    }

    private void createUserRoles(List<CulUser> users, String organisation) {
        //store = new DataStore();
        //DataStore userStore = new DataStore();

        //stopwords = loadStopwords();
        String roleName;

        for( CulUser user:users) {
            roleName = createUserRole(user, organisation);

            if (StringUtils.isNullOrEmpty(roleName)) {
                continue;
            }

            user.setRobusRole(roleName);
            //userStore.saveOrUpdateObject(user);
            store.getSession().saveOrUpdate(user);
        }

        //create role terms for newly added roles
        RoleWriter roles = new RoleWriter();
        roles.updateRoles(organisation);
    }

    private String createUserRole(CulUser user, String organisation) {

        List<CulAssignment> assignments = user.getAssignments();

        Map<String, Integer> tags = new HashMap<String, Integer>();

        //only consider users with >n and <m tags
        /*if (assignments.size() < 50 || assignments.size() > 50000) {
            //System.out.println("Number of tags out of range. user: " + userId + "; number of tags: " + assignments.size());
            return "";
        }*/

        for (CulAssignment ca : assignments) {
            String tag = ca.getTag().getTerm();

            if (tag.length() < 3) {
                //ignore tags with only 2 characters
                continue;
            }

            if (tags.containsKey(tag)) {
                //increase number of occurences
                tags.put(tag, tags.get(tag) + 1);

            }
            else {
                tags.put(tag, 1);
            }
        }

        //remove stop words
        tags = removeStopwords(tags);

        //sort entries by values desc
        tags = sortMap(tags, DESC);

        //get top 2 tags
        String keyword1;
        String keyword2;

        if (tags.size() <= 1) {
            return "";
        }

        Iterator<String> it = tags.keySet().iterator();
        keyword1 = it.next();
        keyword1 = splitTags(keyword1);

        keyword2 = it.next();
        keyword2 = splitTags(keyword2);

        /*if (!keyword1.contains(searchTag) && !keyword2.contains(searchTag)) {
            return "";
        }*/

        Role role = new Role();
        role.setKeyword1(keyword1);
        role.setKeyword2(keyword2);
        role.setOrganisation(organisation);
        role.setName(keyword1 + "-" + keyword2);

        store.saveOrUpdateObject(role);
        System.out.println("Created new role " + role.getName() + " from user " + user.getId());
        return role.getName();
    }

    private String splitTags(String input) {
        String result = input.replace('-', ' ');
        result = result.replace('_', ' ');

        return result;
    }

    public Map<String, Integer> removeStopwords(Map<String, Integer> tags) {
        stopwords = loadStopwords();

        Map<String, Integer> cleanTags = new HashMap<String, Integer>();


        for (String key : tags.keySet()) {

            /*if (key.contains("file-import")) {
                continue;
            }*/

            if (stopwords.contains(key)) {
                continue;
            }
            cleanTags.put(key, tags.get(key));
        }

        return cleanTags;
    }

    public Map<String, Integer> sortMap(Map<String, Integer> unsortMap, final boolean order)
    {

        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    private List<String> loadStopwords() {
        List<String> stopwords = new ArrayList<String>();
        stopwords.add("no-tag");
        stopwords.add("misc");
        stopwords.add("file-import");
        stopwords.add("bibtex-import");
        stopwords.add("and");
        stopwords.add("the");
        stopwords.add("use");
        stopwords.add("usa");
        stopwords.add("all");
        stopwords.add("are");
        stopwords.add("bib");
        stopwords.add("for");
        stopwords.add("new");
        stopwords.add("old");
        stopwords.add("other");
        stopwords.add("mine");
        stopwords.add("todo");
        stopwords.add("have");
        stopwords.add("how-to");
        stopwords.add("googlescholar");
        stopwords.add("google-scholar");
        stopwords.add("citeulike");
        stopwords.add("job");
        stopwords.add("diss");
        stopwords.add("*neu");
        stopwords.add("mir");
        stopwords.add("gut");
        stopwords.add("uni");
        stopwords.add("juergen");
        stopwords.add("_pardem_cleaned_041012");
        stopwords.add("*from-readcube");
        stopwords.add("2010may27");
        stopwords.add("visweb-08-10-15");
        stopwords.add("*wei-xing-refs-2010-08-13");
        stopwords.add("wei-xing-bib-import-10-08-14");
        stopwords.add("454");
        stopwords.add("2012");
        stopwords.add("2011");
        stopwords.add("2010");
        stopwords.add("2009");
        stopwords.add("2008");
        stopwords.add("2007");
        stopwords.add("2006");
        stopwords.add("2005");
        stopwords.add("2004");
        stopwords.add("2003");
        stopwords.add("2002");
        stopwords.add("2001");
        stopwords.add("2000");
        stopwords.add("1999");
        stopwords.add("*2012-import");

        return stopwords;
    }
}
