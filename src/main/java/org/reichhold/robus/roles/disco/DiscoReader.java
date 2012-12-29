package org.reichhold.robus.roles.disco;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

/**
 * User: matthias
 * Date: 21.12.12
 */
public class DiscoReader {
    static final String NODE = "NODE";
    static final String NT = "NT";

    private Integer maxLevel;

    private HashMap<String, Integer> terms;

    public DiscoReader() {
        terms = new HashMap<String, Integer>();

        maxLevel = 7;
    }

    public Integer getMaxLevel() {
        return maxLevel;
    }

    public Map<String, Integer> getTerms() {
        return terms;
    }

    //disco LEVEL calculation
    public void loadDiscoLevels() {

        try {
            File discoFile = new File("/Users/matthias/Documents/workspace/robus/src/main/resources/DISCO_EN_hierarchy_only.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(discoFile);
            doc.getDocumentElement().normalize();

            NodeList topNodes = doc.getElementsByTagName(NODE);

            //only the first node in the disco file is relevant
            Node domainNode = topNodes.item(0);
            Element domainElement = (Element) domainNode;

            NodeList nodes = domainElement.getElementsByTagName(NT);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);

                String term = node.getChildNodes().item(0).getNodeValue().trim();
                Integer level = Integer.parseInt(node.getAttributes().item(0).getNodeValue());

                terms.put(term, level);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Integer getDiscoLevel(String term) {
        int level = 0;

        if (terms.containsKey(term)) {
            //found exact matching
            level = terms.get(term).intValue();
        }
        else {
            //try to find similar match...
            Set<String> allKeys = terms.keySet();

            for (String key : allKeys) {
                if (! key.contains(term)) {
                    continue;
                }

                int newLevel = terms.get(key).intValue();
                if (newLevel > level) {
                    level = newLevel;
                }
            }
        }

        return level;
    }
}
