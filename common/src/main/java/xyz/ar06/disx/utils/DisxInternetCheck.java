package xyz.ar06.disx.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DisxInternetCheck {
    public static boolean checkInternet(){
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("http://www.google.com"))
                    .build();
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response == null){
                throw new Exception("No Internet Connection");
            }
        } catch (Exception e){
            return false;
        }
        return true;
    }
}
