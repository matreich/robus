package org.reichhold.robus;

import org.reichhold.robus.citeulike.CulAssignment;
import org.reichhold.robus.citeulike.CulUser;
import org.reichhold.robus.citeulike.RoleCreator;
import org.reichhold.robus.db.DataStore;
import org.reichhold.robus.lucene.LuceneSearcher;
import org.reichhold.robus.lucene.RoleSearchResult;
import org.reichhold.robus.roles.Role;

import java.text.DecimalFormat;
import java.util.*;

/**
 * User: matthias
 * Date: 08.01.13
 */
public class Evaluator {

    public void doMapEvaluation(){
        //List<String> queries = new ArrayList<String>();
        //queries.add("internet");
        //queries.add("database");
        //queries.add("business");

        DataStore store = new DataStore();
        List<Role> roles;
        roles = store.getRolesByOrganisation("CUL1500");
        //roles = store.getRoles("CiteULike", "marketing-internet");

        LuceneSearcher searcher = new LuceneSearcher();
        int maxResults =100;

        Float mMapStandard = 0.0f;
        Float mMapRoles = 0.0f;

        RoleCreator roleCreator = new RoleCreator();

        for (Role role:roles) {
            Map<String, Integer> tagMap = new HashMap<String, Integer>();
            List<String> queries = new ArrayList<String>();

            CulUser culUser = store.getCulUserByRoleName(role.getName());
            if(culUser == null) {
                System.out.println("Could not find a CulUser for role " + role.getName());
                continue;
            }


            List<CulAssignment> cas = culUser.getAssignments();
            if(cas == null) {
                continue;
            }

            for (CulAssignment ca:cas) {
                String tag = ca.getTag().getTerm();
                if (tag.length() < 3) {
                    //ignore tags with only 2 characters
                    continue;
                }
                if (tagMap.containsKey(tag)) {
                    //increase number of occurences
                    tagMap.put(tag, tagMap.get(tag) + 1);

                }
                else {
                    tagMap.put(tag, 1);
                }


            }

            //remove stop words
            tagMap = roleCreator.removeStopwords(tagMap);

            //sort entries by values desc
            tagMap = roleCreator.sortMap(tagMap, false);

            //load top n tags as queries

            int maxQueries = 10;
            if (tagMap.size() < maxQueries ) {
                maxQueries = tagMap.size();
            }

            Iterator<String> it = tagMap.keySet().iterator();
            for (int i=0; i<maxQueries; i++) {
                String query = it.next();
                queries.add(query);
            }
            //queries.add("sustainability");

            //perform non-role-sensitive search
            List<RoleSearchResult> resultsStandard = searcher.doStandardSearches(queries, maxResults);

            //perform role-sensitive search for every query
            List<RoleSearchResult> resultsRole = searcher.doRoleSearches(role, queries, maxResults);

            Float mapRole = 0.0f;
            mapRole = computeMAP(resultsRole, role.getName());
            mMapRoles += mapRole;

            Float mapStandard = computeMAP(resultsStandard, role.getName());
            mMapStandard += mapStandard;

            System.out.println("MAP for role-sensitive search | " + new DecimalFormat("0.0000").format(mapRole) +
                    " | MAP for standard search | " + new DecimalFormat("0.0000").format(mapStandard) + " | for Role " + role.getName());
        }

        mMapRoles = mMapRoles / roles.size();
        mMapStandard = mMapStandard / roles.size();
        float improvement = 1.0f/mMapStandard * mMapRoles - 1;

        System.out.println("Overall MMAP for role-sensitive search | " + new DecimalFormat("0.0000").format(mMapRoles) +
                " | MMAP for standard search | " + new DecimalFormat("0.0000").format(mMapStandard) + " | improvement | " + new DecimalFormat("#.#%").format(improvement));

        int i=0;
        //searcher.doStandardSearch("java");
        //searcher.doRoleSearch("JavaDeveloper", "java");
    }

    private Float computeMAP(List<RoleSearchResult> results, String role) {
        DataStore store = new DataStore();
        CulUser culUser = store.getCulUserByRoleName(role);
        if (culUser == null) {
            System.out.println("Error computing MAP for role " + role + ": couldn't find a CulUser.");
            return 0.0f;
        }

        //nicht nötig!
        //List<CulAssignment> userAssignments = store.getAssignmentsForUser(culUser.getId());
        Float sumAP = 0.0f;

        for (RoleSearchResult result:results) {
            sumAP += computeAveragePrecision(result, culUser.getAssignments());
        }

        return sumAP / results.size();  //To change body of created methods use File | Settings | File Templates.
    }

    private Float computeAveragePrecision(RoleSearchResult result, List<CulAssignment> userAssignments) {
        int numTotalHits = 0;
        float numRelevantHits = 0.0f;
        Float sumAveragePrecision = 0.0f;
        float ap = 0;

        if (result == null || result.getDocuments() == null) {
            return ap;
        }

        for(int i=1; i<=result.getDocuments().size(); i++) {
            numTotalHits = i;

            if (isRelevant(result.getDocuments().get(i-1).get("id"), result.getQuery(), userAssignments)) {
                numRelevantHits += 1.0;
                sumAveragePrecision += numRelevantHits / numTotalHits;
            }
        }

        if (numRelevantHits > 0) {
            ap = sumAveragePrecision / numRelevantHits;
        }
        return ap;
    }

    /***
     * a document is considered relevant for a query when a user has tagged the document with the query term
     * @param culId
     * @param query
     * @param userAssignments
     * @return
     */
    private boolean isRelevant(String culId, String query, List<CulAssignment> userAssignments) {
        for(CulAssignment ua: userAssignments) {
            //todo: process multi word query?!
            if (ua.getTag().getTerm().equals(query) && ua.getDocument().getId().equals(culId)) {
                return true;
            }
        }
        return false;
    }
}
