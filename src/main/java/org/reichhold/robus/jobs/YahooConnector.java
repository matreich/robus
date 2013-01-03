package org.reichhold.robus.jobs;

import org.reichhold.robus.db.DataStore;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.YahooApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import java.util.Scanner;

/**
 * User: matthias
 * Date: 31.12.12
 */
public class YahooConnector {

    OAuthService service = null;
    org.scribe.model.Token accessToken = null;
    org.scribe.model.Token requestToken;
    Database db;
    DataStore store;

    @Deprecated
    public YahooConnector()
    {
        //the linkedin service
        service = new ServiceBuilder()
                .provider(YahooApi.class)
                .apiKey("dj0yJmk9VDZJZzcwOXpJYWtwJmQ9WVdrOVZqUlpkMU0zTXpRbWNHbzlNQS0tJnM9Y29uc3VtZXJzZWNyZXQmeD01NQ--")
                .apiSecret("f8745fbfb7e29d2aaf63625a4609409254b7a68b")
                .build();

        requestToken = service.getRequestToken();

        Scanner in = new Scanner(System.in);
        System.out.println("Now go and authorize Scribe here:");
        System.out.println(service.getAuthorizationUrl(requestToken));
        System.out.println("And paste the verifier here");
        System.out.print(">>");
        Verifier verifier = new Verifier(in.nextLine());
        System.out.println();

        // Trade the Request Token and Verfier for the Access Token
        System.out.println("Trading the Request Token for an Access Token...");
        accessToken = service.getAccessToken(requestToken, verifier);
        System.out.println("Got the Access Token!");
        System.out.println("(if your curious it looks like this: "
                + accessToken + " )");
        System.out.println();

        // Now let's go and ask for a protected resource!
        System.out.println("Now we're going to access a protected resource...");
        OAuthRequest request1 = new OAuthRequest(Verb.GET,
                "https://api.del.icio.us/v1/tags/get");
        service.signRequest(accessToken, request1);
        Response response1 = request1.send();
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response1.getCode());
        System.out.println(response1.getBody());
    }
}
