package org.reichhold.robus.citeUlike;

/**
 * User: matthias
 * Date: 04.01.13
 */
public class CulWebReader {
    private final String CUL_ARTICLE_URL = "http://www.citeulike.org/json/article/";

    public CulDocument getDocumentByArticelId(String articleId) {
        //String url = "http://www.citeulike.org/json/article/11862427";
        String url = CUL_ARTICLE_URL + articleId;
        /*try {
            InputStream is = new URL(url).openStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            String text = json.toString();
            json.get("href");
            json.get("title");
            json.get("abstract");

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }*/
        return null;
    }
}
