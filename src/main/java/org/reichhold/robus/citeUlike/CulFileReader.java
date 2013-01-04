package org.reichhold.robus.citeUlike;

import org.reichhold.robus.db.DataStore;

import java.io.*;
import java.util.*;

/**
 * User: matthias
 * Date: 03.01.13
 */
public class CulFileReader {

    private DataStore store;

    private TreeMap<String, CulUser> users;
    private TreeMap<String, CulDocument> documents;
    private TreeMap<String, CulTag> tags;
    private TreeMap<String, CulAssignment> assignments;

    private BufferedReader br;

    public CulFileReader() {
        store = new DataStore();
    }

    public void fileToDb(){

        //saveUsers();
        //saveDocuments();
        //saveTags();
        saveAssignments();
    }

    private void saveUsers() {
        setBufferedReader();

        String line;
        long counter = 0;

        //Read File Line By Line
        try {
            List<CulUser> users = new ArrayList<CulUser>();

            SortedSet<String> userIds = new TreeSet<String>();

            while ((line = br.readLine()) != null)   {
                // Process the line
                CulUser user = generateUser(line);
                if (user == null) {
                    continue;
                }

                boolean isNew = userIds.add(user.getId());
                if (!isNew) {
                    continue;
                }

                users.add(user);

                if (users.size() < 10000) {
                    continue;
                }
                counter += store.saveCulUsers(users);
                users.clear();
            }

            counter += store.saveCulUsers(users);
            users.clear();

            System.out.println(counter + " users saved");

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private CulUser generateUser(String line) {
        String key = getColumnFromLine(line, 1);

        CulUser user = new CulUser();
        user.setId(key);
        return user;
    }

    private void saveDocuments() {
        setBufferedReader();

        String line;
        long counter = 0;

        //Read File Line By Line
        try {
            List<CulDocument> docs = new ArrayList<CulDocument>();

            SortedSet<String> docIds = new TreeSet<String>();

            while ((line = br.readLine()) != null)   {
                // Process the line
                CulDocument doc = generateCulDocument(line);
                if (doc == null) {
                    continue;
                }

                boolean isNew = docIds.add(doc.getId());
                if (!isNew) {
                    continue;
                }

                docs.add(doc);

                if (docs.size() < 10000) {
                    continue;
                }
                counter += store.saveCulDocuments(docs);
                docs.clear();
            }

            counter += store.saveCulDocuments(docs);
            docs.clear();

            System.out.println(counter + " documents saved");

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private CulDocument generateCulDocument(String line) {
        String key = getColumnFromLine(line, 0);

        CulDocument doc = new CulDocument();
        doc.setId(key);

        return doc;
    }

    private void saveTags() {
        setBufferedReader();

        String line;
        long counter = 0;

        //Read File Line By Line
        try {
            List<CulTag> tags = new ArrayList<CulTag>();

            SortedSet<String> tagIds = new TreeSet<String>();

            while ((line = br.readLine()) != null)   {
                // Process the line
                CulTag tag = generateTag(line);
                if (tag == null) {
                    continue;
                }

                boolean isNew = tagIds.add(tag.getTerm());
                if (!isNew) {
                    continue;
                }

                tags.add(tag);

                if (tags.size() < 10000) {
                    continue;
                }
                counter += store.saveCulTags(tags);
                tags.clear();
            }

            counter += store.saveCulTags(tags);
            tags.clear();

            System.out.println(counter + " tags saved");

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private CulTag generateTag(String line) {
        String key = getColumnFromLine(line, 3);

        CulTag tag = new CulTag();
        tag.setTerm(key);

        return tag;
    }

    private void saveAssignments() {
        setBufferedReader();

        String line;
        long counter = 0;

        //Read File Line By Line
        try {
            List<CulAssignment> cas = new ArrayList<CulAssignment>();

            while ((line = br.readLine()) != null)   {
                // Process the line
                CulAssignment ca = generateAssignment(line);
                if (ca == null) {
                    continue;
                }

                cas.add(ca);

                if (cas.size() < 10000) {
                    continue;
                }
                counter += store.saveCulAssignments(cas);
                cas.clear();
            }

            counter += store.saveCulAssignments(cas);
            cas.clear();

            System.out.println(counter + " assignments saved");

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private CulAssignment generateAssignment(String line) {
        String docId = getColumnFromLine(line, 0);
        String userId = getColumnFromLine(line, 1);
        String timestamp = getColumnFromLine(line, 2);
        String tagId = getColumnFromLine(line, 3);

        CulDocument doc = new CulDocument();
        doc.setId(docId);
        CulUser user = new CulUser();
        user.setId(userId);
        CulTag tag = new CulTag();
        tag.setTerm(tagId);

        CulAssignment ca = new CulAssignment();
        ca.setDocument(doc);
        ca.setUser(user);
        ca.setTimestamp(timestamp);
        ca.setTag(tag);

        return ca;
    }

    private String getColumnFromLine(String line, int index) {
        //line example: 1351299|f7d5debb9c7d4d9ab81c63cd26578e23|2007-05-31 14:01:35.797261+01|comparative
        String[] columns = line.split("\\|");

        if (columns.length != 4) {
            System.out.println("Invalid input line: expected 4 columns, found " + columns.length);
            return "";
        }

        return columns[index];
    }

    private void setBufferedReader() {
        // Open the file
        FileInputStream fstream = null;
        try {
            fstream = new FileInputStream("/Volumes/Daten/Robus/Resources/datasets/CiteULike 20130102/current");
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(fstream);
        br = new BufferedReader(new InputStreamReader(in));
    }
}
