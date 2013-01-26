package org.reichhold.robus.lucene;

import com.mysql.jdbc.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.jsoup.Jsoup;
import org.reichhold.robus.citeulike.CulDocument;
import org.reichhold.robus.db.DataStore;
import org.reichhold.robus.jobs.CleanJobAd;
import org.reichhold.robus.roles.Role;
import org.reichhold.robus.roles.RoleReader;

import java.io.*;
import java.util.Date;
import java.util.List;

/** Index all text files under a directory.
 * <p>
 * This is a command-line application demonstrating simple Lucene indexing.
 * Run it with no command-line arguments for usage information.
 */
public class LuceneIndex {

    private String defaultIndexPath;
    private String roleIndexPath;
    private String docsPath;
    private RoleReader roleReader;
    private final String culIndexPath;
    private final String jobAdIndexPath;

    public LuceneIndex() {
        defaultIndexPath = "/Users/matthias/Documents/workspace/robus/src/main/resources/defaultIndex";
        roleIndexPath = "/Users/matthias/Documents/workspace/robus/src/main/resources/roleIndex";
        docsPath = "/Volumes/Daten/Robus/Resources/TrecHtmlDemo/";
        culIndexPath = "/Users/matthias/Documents/workspace/robus/src/main/resources/culIndex";
        jobAdIndexPath = "/Users/matthias/Documents/workspace/robus/src/main/resources/jobAdIndex";

        roleReader = new RoleReader();
        roleReader.loadRoles("CiteULike");
    }

    public void createIndexes() {
        //createFileIndex(defaultIndexPath, docsPath, true, false);
        createFileIndex(roleIndexPath, docsPath, true, true);
    }

    /***
     * creates an index for all documents stored in cul_document
     * @param create if true: Creates a new index in the directory, removing any previously indexed documents;
     *                  else: Adds new documents to an existing index
     */
    public void createCulIndex(boolean create, boolean createRoleScoreFields) {
        try {
            Directory dir = FSDirectory.open(new File(culIndexPath));
            Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_40);
            //Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
            iwc.setSimilarity(new BM25Similarity());

            if (create) {
                iwc.setOpenMode(OpenMode.CREATE);
            } else {
                iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            }

            IndexWriter writer = new IndexWriter(dir, iwc);
            System.out.println("Indexing cul_documents to directory '" + culIndexPath + "'...");

            //get cul_documents
            DataStore store = new DataStore();
            String queryString = "from CulDocument where path is not null";
            Session session = store.getSession();
            Query q = session.createQuery(queryString);
            ScrollableResults results = q.scroll();

            int counter = 0;

            while (results.next() )
            {
                CulDocument cul = (CulDocument) results.get(0);
                indexCulDocument(cul, writer, createRoleScoreFields);
                counter += 1;
                clearSessionCache(counter, session);
            }

            results.close();
            session.close();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        if (createRoleScoreFields) {
            this.computeRoleScores();
        }
    }

    private void indexCulDocument(CulDocument cul, IndexWriter writer, boolean createRoleScoreFields) {
        Document doc = new Document();

        // Add the path of the file as a field named "path".  Use a
        // field that is indexed (i.e. searchable), but don't tokenize
        // the field into separate words and don't index term frequency
        // or positional information:
        Field idField = new StringField("id", cul.getId(), Field.Store.YES);
        doc.add(idField);

        Field pathField = new StringField("path", cul.getPath(), Field.Store.YES);
        doc.add(pathField);

        String title = cul.getTitle() + ". ";
        String content = cul.getContentAbstract();

        //store both texts into one field
        TextField field = new TextField("contents", title + content, Field.Store.YES);
        doc.add(field);

        if (createRoleScoreFields) {
            //create role score fields for each defined role
            float roleRelevance = 0.0f;

            List<Role> roles = roleReader.getRoles();
            for (Role role : roles) {
                Field roleScoreField = new FloatField(role.getName(), roleRelevance, Field.Store.YES);

                doc.add(roleScoreField);
            }
        }

        if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
            try {
                writer.addDocument(doc);
                System.out.println("adding cul_document " + doc.get("path"));
            } catch (IOException e) {
                System.out.println("Error adding cul_document" + doc.get("path"));
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        else
        {
            try {
                writer.updateDocument(new Term("id", doc.get("id")), doc);
                System.out.println("updating cul_document " + doc.get("path"));
            } catch (IOException e) {
                System.out.println("Error updating cul_document" + doc.get("path"));
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    /***
     * creates an index for all documents stored in CleanJobAd
     * @param create
     * @param createRoleScoreFields
     */
    public void createJobAdIndex(boolean create, boolean createRoleScoreFields) {
        try {
            Directory dir = FSDirectory.open(new File(jobAdIndexPath));
            Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_40);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
            iwc.setSimilarity(new BM25Similarity());

            if (create) {
                iwc.setOpenMode(OpenMode.CREATE);
            } else {
                iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            }

            IndexWriter writer = new IndexWriter(dir, iwc);
            System.out.println("Indexing CleanJobAds to directory '" + jobAdIndexPath + "'...");

            //get cul_documents
            DataStore store = new DataStore();
            String queryString = "from CleanJobAd where title is not null";
            Session session = store.getSession();
            Query q = session.createQuery(queryString);
            ScrollableResults results = q.scroll();

            int counter = 0;

            while (results.next() )
            {
                CleanJobAd job = (CleanJobAd) results.get(0);
                indexJobAdDocument(job, writer, createRoleScoreFields);
                counter += 1;
                clearSessionCache(counter, session);
            }

            results.close();
            session.close();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        if (createRoleScoreFields) {
            this.computeRoleScores();
        }
    }

    private void indexJobAdDocument(CleanJobAd jobAd, IndexWriter writer, boolean createRoleScoreFields) {
        Document doc = new Document();

        // Add the path of the file as a field named "path".  Use a
        // field that is indexed (i.e. searchable), but don't tokenize
        // the field into separate words and don't index term frequency
        // or positional information:
        Field idField = new StringField("jobId", jobAd.getJobId(), Field.Store.YES);
        doc.add(idField);

        String title = jobAd.getTitle() + ". ";
        String description = jobAd.getDescription() + ". ";
        String skills = jobAd.getSkills();

        //store both texts into one field
        TextField field = new TextField("contents", title + description + skills, Field.Store.YES);
        doc.add(field);

        if (createRoleScoreFields) {
            //create role score fields for each defined role
            float roleRelevance = 0.0f;

            List<Role> roles = roleReader.getRoles();
            for (Role role : roles) {
                Field roleScoreField = new FloatField(role.getName(), roleRelevance, Field.Store.YES);

                doc.add(roleScoreField);
            }
        }

        if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
            try {
                writer.addDocument(doc);
                System.out.println("adding CleanJobAd " + doc.get("jobId"));
            } catch (IOException e) {
                System.out.println("Error adding CleanJobAd " + doc.get("jobId"));
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        else
        {
            try {
                writer.updateDocument(new Term("id", doc.get("id")), doc);
                System.out.println("updating cul_document " + doc.get("path"));
            } catch (IOException e) {
                System.out.println("Error updating cul_document" + doc.get("path"));
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }


    private void clearSessionCache(int counter, Session session) {
        if ( counter % 100 == 0) {
            session.flush();
            session.clear();
        }
    }

    /***
     * Index all text/html files under a directory.
     * @param indexPath the destination for the index to be created
     * @param docsPath the documents that should be added to the index
     * @param create if true, a new index will be created; else the existin one will be updated
     * @param setRoleScores if true, a field for each enterprise role is added to every document in the index
     */
    private void createFileIndex(String indexPath, String docsPath, boolean create, boolean setRoleScores) {

        if (docsPath == null || indexPath == null) {
            System.err.println("indexPath and docsPath may not be null");
            System.exit(1);
        }

        final File docDir = new File(docsPath);
        if (!docDir.exists() || !docDir.canRead()) {
            System.out.println("Document directory '" +docDir.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + indexPath + "'...");

            Directory dir = FSDirectory.open(new File(indexPath));
            Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_40);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);

            if (create) {
                // Create a new index in the directory, removing any
                // previously indexed documents:
                iwc.setOpenMode(OpenMode.CREATE);
            } else {
                // Add new documents to an existing index:
                iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            }

            // Optional: for better indexing performance, if you
            // are indexing many documents, increase the RAM
            // buffer.  But if you do this, increase the max heap
            // size to the JVM (eg add -Xmx512m or -Xmx1g):
            //
            // iwc.setRAMBufferSizeMB(256.0);

            IndexWriter writer = new IndexWriter(dir, iwc);
            indexDocs(writer, docDir, setRoleScores);

            // NOTE: if you want to maximize search performance,
            // you can optionally call forceMerge here.  This can be
            // a terribly costly operation, so generally it's only
            // worth it when your index is relatively static (ie
            // you're done adding documents to it):
            //
            // writer.forceMerge(1);

            writer.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    /**
     * Indexes the given file using the given writer, or if a directory is given,
     * recurses over files and directories found under the given directory.
     *
     * NOTE: This method indexes one document per input file.  This is slow.  For good
     * throughput, put multiple documents into your input file(s).  An example of this is
     * in the benchmark module, which can create "line doc" files, one document per line,
     * using the
     * <a href="../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
     * >WriteLineDocTask</a>.
     *
     * @param writer Writer to the index where the given file/dir info will be stored
     * @param file The file to index, or the directory to recurse into to find files to index
     * @param setRoleScores if true, a field for each enterprise role is added to every document in the index
     * @throws IOException
     */
    private void indexDocs(IndexWriter writer, File file, boolean setRoleScores)
            throws IOException {
        // do not try to index files that cannot be read
        if (file.canRead()) {
            if (file.isDirectory()) {
                String[] files = file.list();
                // an IO error could occur
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        indexDocs(writer, new File(file, files[i]), setRoleScores);
                    }
                }
            }
            else {
                FileInputStream fis;
                try {
                    fis = new FileInputStream(file);
                } catch (FileNotFoundException fnfe) {
                    // at least on windows, some temporary files raise this exception with an "access denied" message
                    // checking if the file can be read doesn't help
                    return;
                }

                try {

                    // make a new, empty document
                    Document doc = new Document();

                    // Add the path of the file as a field named "path".  Use a
                    // field that is indexed (i.e. searchable), but don't tokenize
                    // the field into separate words and don't index term frequency
                    // or positional information:
                    Field pathField = new StringField("path", file.getPath(), Field.Store.YES);
                    doc.add(pathField);

                    // Add the last modified date of the file a field named "modified".
                    // Use a LongField that is indexed (i.e. efficiently filterable with
                    // NumericRangeFilter).  This indexes to milli-second resolution, which
                    // is often too fine.  You could instead create a number based on
                    // year/month/day/hour/minutes/seconds, down the resolution you require.
                    // For example the long value 2011021714 would mean
                    // February 17, 2011, 2-3 PM.
                    doc.add(new LongField("modified", file.lastModified(), Field.Store.YES));

                    // Add the contents of the file to a field named "contents".  Specify a Reader,
                    // so that the text of the file is tokenized and indexed, but not stored.
                    // Note that FileReader expects the file to be in UTF-8 encoding.
                    // If that's not the case searching for special characters will fail.

                    //Matthias: index html vs. text
                    boolean indexHtml = true;

                    if(indexHtml)
                    {
                        //Index HTML payload only
                        InputStream is = fis;
                        String content =  "";

                        try{
                            content = Jsoup.parse(is, null, "").text();
                        }
                        catch (Exception e)
                        {
                            System.out.println("file format error: " + e.getMessage());
                            e.printStackTrace();

                        }

                        TextField field = new TextField("contents", content, Field.Store.YES);
                        doc.add(field);

                    }
                    else
                    {
                        //Index complete text
                        doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(fis, "UTF-8"))));
                    }

                    if (setRoleScores) {
                        //create role score fields for each defined role
                        float roleRelevance = 0.0f;

                        List<Role> roles = roleReader.getRoles();
                        for (Role role : roles) {
                            Field roleScoreField = new FloatField(role.getName(), roleRelevance, Field.Store.YES);

                            doc.add(roleScoreField);
                        }
                    }

                    if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                        // New index, so we just add the document (no old document can be there):
                        writer.addDocument(doc);
                        System.out.println("adding " + file + "; ID:" + doc.get("docId"));
                    }
                    else
                    {
                        // Existing index (an old copy of this document may have been indexed) so
                        // we use updateDocument instead to replace the old one matching the exact
                        // path, if present:
                        System.out.println("updating " + file + "; java score: " + doc.getField("JavaDeveloper").numericValue() + "; sales score: " + doc.getField("AccountManager").numericValue());
                        writer.updateDocument(new Term("path", file.getPath()), doc);
                    }

                }
                catch (Exception e)
                {
                    System.out.println("ERROR: " + e.getMessage());
                }
                finally {
                    fis.close();
                }
            }
        }
    }

    /***
     * role scores are computed at searching time ....
     */
    @Deprecated
    public void computeRoleScores() {
        try {
            Directory roleIndexDir = FSDirectory.open(new File(culIndexPath));


            Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_40);
            String field = "contents";

            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);

            int maxResults = 10000;
            List<Role> roles = roleReader.getRoles();

            for (Role role : roles) {
                IndexReader roleIndexReader = DirectoryReader.open(roleIndexDir);
                IndexSearcher roleIndexSearcher = new IndexSearcher(roleIndexReader);
                IndexWriter writer = new IndexWriter(roleIndexDir, iwc);

                computeRoleScore(roleIndexReader, roleIndexSearcher, field, writer, maxResults, role);

                writer.close();
                roleIndexReader.close();
            }

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void computeRoleScore(String roleName) {
        try {
            Directory roleIndexDir = FSDirectory.open(new File(roleIndexPath));

            IndexReader roleIndexReader = DirectoryReader.open(roleIndexDir);
            IndexSearcher roleIndexSearcher = new IndexSearcher(roleIndexReader);

            Analyzer analyzer;
            //analyzer = new StandardAnalyzer(Version.LUCENE_40);
            analyzer = new EnglishAnalyzer(Version.LUCENE_40);

            String field = "contents";

            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            IndexWriter writer = new IndexWriter(roleIndexDir, iwc);

            int maxResults = 10;
            List<Role> roles = roleReader.getRoles();

            for (Role role : roles) {
                if (role.getName().equals(roleName)) {
                    computeRoleScore(roleIndexReader, roleIndexSearcher, field, writer, maxResults, role);
                }
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private void computeRoleScore(IndexReader roleIndexReader, IndexSearcher roleIndexSearcher, String field, IndexWriter writer, int maxResults, Role role) throws IOException {
        System.out.println("Computing scores for role " + role.getName());
        BooleanQuery query = role.getBooleanQuery(field);

        TopDocs results = roleIndexSearcher.search(query, maxResults);

        if (results.totalHits < 1) {
            System.out.println("Did not find any relevant docs for role " + role.getName());
            return;
        }

        ScoreDoc[] hits = results.scoreDocs;
        //update role scores for all documents relevant for this role vector
        for(ScoreDoc hit : hits) {
            Document oldDoc = roleIndexReader.document(hit.doc);

            Document newDoc = new Document();

            Field idField = new StringField("id", oldDoc.getField("id").stringValue(), Field.Store.YES);
            newDoc.add(idField);

            Field pathField = new StringField("path", oldDoc.getField("path").stringValue(), Field.Store.YES);
            newDoc.add(pathField);

            //newDoc.add(new LongField("modified", (Long) oldDoc.getField("modified").numericValue(), Field.Store.YES));

            TextField contentsField = new TextField("contents", oldDoc.getField("contents").stringValue(), Field.Store.YES);
            newDoc.add(contentsField);

            List<Role> roles = roleReader.getRoles();
            for (Role r : roles) {
                Float roleScore = (Float) oldDoc.getField(r.getName()).numericValue();
                if(r.getName().equals(role.getName())) {
                    //set new role score value
                    //todo: move to role score query!
                    roleScore = 2 * hit.score + 1;
                }

                Field roleScoreField = new FloatField(r.getName(), roleScore, Field.Store.YES);
                newDoc.add(roleScoreField);
            }

            writer.deleteDocuments(new Term("id", oldDoc.get("id")));
            writer.commit();

            writer.addDocument(newDoc);

            /*Document doc = roleIndexReader.document(hit.doc);

            Field f = (Field) doc.getField(role.getName());
            f.setFloatValue(hit.score);

            doc.removeField(role.getName());
            doc.add(f);

            writer.updateDocument(new JobTerm("path", doc.get("path")), doc);
            writer.commit();   */
        }
    }

    public void printAllDocsWithRoleScores() {

        try {
            Directory roleIndexDir = FSDirectory.open(new File(culIndexPath));
            IndexReader reader = DirectoryReader.open(roleIndexDir);

            int counter = 0;

            for (int i=0; i<reader.maxDoc(); i++) {

                Document doc = reader.document(i);


                /*if (doc.get("id").equals("9006044"))
                {
                    int j=0;
                }*/
                if ((Float) doc.getField("marketing-internet").numericValue() == 0.0) {
                    continue;
                }
                counter ++;

                List<IndexableField> fields = doc.getFields();
                System.out.print("\n Document>>> " + counter);
                for (IndexableField field:fields) {
                    String value = field.stringValue();

                    if (StringUtils.isNullOrEmpty(value)) {
                        value = String.valueOf(field.numericValue());
                    }

                    if (value != null && value.length() > 30) {
                        value = value.substring(0, 26) + "...";
                    }

                    System.out.print("; " + field.name() + ": " + value);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}