package org.reichhold.robus;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Trec2Html {

    String docspath = "/Volumes/Daten/Robus/Resources/";
    String sourceDir = "TrecSource/";
    String targetDir = "TrecHtml/";

    public Trec2Html()
    {}

    public void createHtmlFiles()
    {
        File files = new File(docspath + sourceDir);

        for(File f : files.listFiles())
        {
            createHtmlFiles(f);
        }
    }

    private void createHtmlFiles(File f)
    {
        createHtmlFiles(f.getName());
    }

    public void createHtmlFiles(String trecFileName)
    {
        try
        {
            File file = new File(docspath + sourceDir + trecFileName);
            InputStream input = new FileInputStream(file);

            System.out.println("Created InputStream from file " + trecFileName);
            System.out.println("Start parsing ...");

            Document doc = Jsoup.parse(input, null, "", Parser.xmlParser());
            Elements docEls = doc.getElementsByTag("DOC");

            System.out.println("Found " + docEls.size() + " DOC-Tags in file.");

            for(Element docEl : docEls)
            {
                String docno = docEl.getElementsByTag("DOCNO").text();
                String html = docEl.getElementsByTag("html").outerHtml();

                FileWriter fstream = new FileWriter(docspath + targetDir + docno + ".html");
                BufferedWriter outH = new BufferedWriter(fstream);
                outH.write(html);
                //Close the output stream
                outH.close();
            }

            input.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}