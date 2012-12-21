package org.reichhold.robus.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
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

    public LuceneIndex() {
        defaultIndexPath = "/Users/matthias/Documents/workspace/robus/src/main/resources/defaultIndex";
        roleIndexPath = "/Users/matthias/Documents/workspace/robus/src/main/resources/roleIndex";
        docsPath = "/Volumes/Daten/Robus/Resources/TrecHtmlDemo/";

        roleReader = new RoleReader();
        roleReader.loadRoles("EvalCo");
    }

    public String getDefaultIndexPath() {
        return defaultIndexPath;
    }

    public void setDefaultIndexPath(String defaultIndexPath) {
        this.defaultIndexPath = defaultIndexPath;
    }

    public String getRoleIndexPath() {
        return roleIndexPath;
    }

    public void setRoleIndexPath(String roleIndexPath) {
        this.roleIndexPath = roleIndexPath;
    }

    public String getDocsPath() {
        return docsPath;
    }

    public void setDocsPath(String docsPath) {
        this.docsPath = docsPath;
    }

    public void createIndexes() {
        //createIndex(defaultIndexPath, docsPath, true, false);
        createIndex(roleIndexPath, docsPath, true, true);
    }

    public void computeRoleScores() {
        try {
            Directory roleIndexDir = FSDirectory.open(new File(roleIndexPath));


            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
            String field = "contents";

            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);

            int maxResults = 1000;
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

            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
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

            Field pathField = new StringField("path", oldDoc.getField("path").stringValue(), Field.Store.YES);
            newDoc.add(pathField);

            newDoc.add(new LongField("modified", (Long) oldDoc.getField("modified").numericValue(), Field.Store.YES));

            TextField contentsField = new TextField("contents", oldDoc.getField("contents").stringValue(), Field.Store.YES);
            newDoc.add(contentsField);

            List<Role> roles = roleReader.getRoles();
            for (Role r : roles) {
                Float roleScore = (Float) oldDoc.getField(r.getName()).numericValue();
                if(r.getName().equals(role.getName())) {
                    //set new role score value
                    roleScore = hit.score;
                }

                Field roleScoreField = new FloatField(r.getName(), roleScore, Field.Store.YES);
                newDoc.add(roleScoreField);
            }

            writer.deleteDocuments(new Term("path", oldDoc.get("path")));
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

    /***
     * Index all text/html files under a directory.
     * @param indexPath the destination for the index to be created
     * @param docsPath the documents that should be added to the index
     * @param create if true, a new index will be created; else the existin one will be updated
     * @param setRoleScores if true, a field for each enterprise role is added to every document in the index
     */
    private void createIndex(String indexPath, String docsPath, boolean create, boolean setRoleScores) {

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
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
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


    public void printAllDocsWithRoleScores() {

        try {
            Directory roleIndexDir = FSDirectory.open(new File(roleIndexPath));
            IndexReader reader = DirectoryReader.open(roleIndexDir);

            for (int i=0; i<reader.maxDoc(); i++) {

                Document doc = reader.document(i);
                String docId = doc.get("docId");
                String path = doc.get("path");
                String roleScores = "";

                for (Role role : roleReader.getRoles()) {
                    roleScores += role.getName() + ": " + doc.getField(role.getName()).numericValue() + "; ";
                }

                System.out.println(docId + ": " + path + ": " + roleScores);
            }

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}