package xyz.ar06.disx.utils;

import org.json.JSONObject;
import xyz.ar06.disx.DisxLogger;
import xyz.ar06.disx.DisxMain;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

@Deprecated
public class DisxYoutubeInfoScraper {
    private static String apiURL = "http://disxytsourceapi.ar06.xyz/video_info";
    public static String scrapeTitle(String videoId){
        try {
            String finalizedUrl = apiURL + "?id=" + videoId + "&get=title";
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(finalizedUrl))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200){
                JSONObject jsonObject = new JSONObject(response.body());
                String url = jsonObject.getString("requested");
                return url;
            } else {
                DisxLogger.error("Disx Error: YT-SRC API 'video_info (title)' response failed. Status Code: " + response.statusCode());
                return "Video Not Found";
            }

        } catch (Exception e){
            e.printStackTrace();
            return "Video Not Found";
        }
    }

    public static int scrapeLengthInSeconds(String videoId){
        try {
            String finalizedUrl = apiURL + "?id=" + videoId + "&get=length";
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(finalizedUrl))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200){
                JSONObject jsonObject = new JSONObject(response.body());
                int info = jsonObject.getInt("requested");
                return info;
            } else {
                DisxLogger.error("Disx Error: YT-SRC API 'video_info (length)' response failed. Status Code: " + response.statusCode());
                return -1;
            }

        } catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public static ArrayList<String> scrapeLengthAndTitle(String videoId){
        try {
            String finalizedUrl = apiURL + "?id=" + videoId;
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(finalizedUrl))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200){
                JSONObject jsonObject = new JSONObject(response.body());
                int length = jsonObject.getInt("duration");
                String title = jsonObject.getString("title");
                ArrayList<String> result = new ArrayList<>();
                result.add(title);
                result.add(String.valueOf(length));
                return result;
            } else {
                DisxLogger.error("Disx Error: YT-SRC API 'video_info' response failed. Status Code: " + response.statusCode());
                return null;
            }

        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
