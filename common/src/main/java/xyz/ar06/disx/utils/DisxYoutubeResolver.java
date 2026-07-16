package xyz.ar06.disx.utils;

import org.json.JSONObject;
import xyz.ar06.disx.DisxLogger;
import xyz.ar06.disx.DisxModInfo;
import xyz.ar06.disx.DisxTmpHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;

public class DisxYoutubeResolver {
    public static File resolveFile(String videoId){
        String apiURL = "https://disxytsourceapi.ar06.xyz/audio_pcm?id=";
        try {
            DisxLogger.debug("Audio file resolve requested. Checking tmp directory caches");
            File returnFile = new File("./.disx-tmp/cache/" + videoId + ".pcm.wav");
            if (returnFile.exists()){
                Thread.sleep(1000); //sleep for a second to ensure most clients got their audio lines ready for data streaming
                                            //only because the sleep needs to happen when the audio is loaded from cached
                DisxLogger.debug("Found cached audio file. Returning");
                DisxTmpHandler.markAudioUsed(returnFile);
                return returnFile;
            }
            DisxLogger.debug("Did not find in cache. Requesting from YT-SRC API");
            HttpClient httpClient = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .connectTimeout(Duration.ofSeconds(20))
                    .build();
            HttpRequest request = HttpRequest.newBuilder(URI.create(apiURL + videoId))
                    .GET()
                    .build();
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() == 200){
                DisxLogger.debug("Got response from YT-SRC API. Downloading audio...");
                InputStream in = response.body();
                File newFile = new File(DisxTmpHandler.TMP_CACHE_PATH + "/" + videoId + ".pcm.wav");
                Files.copy(in, Path.of(DisxTmpHandler.TMP_CACHE_PATH + "/" + videoId + ".pcm.wav"), StandardCopyOption.REPLACE_EXISTING);
                return newFile;
            } else {
                DisxLogger.error("Disx Error: YT-SRC API response failed. Status Code: " + response.statusCode());
                return null;
            }

        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String scrapeTitle(String videoId){
        String apiURL = "https://disxytsourceapi.ar06.xyz/video_info";
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
        String apiURL = "https://disxytsourceapi.ar06.xyz/video_info";
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
        String apiURL = "https://disxytsourceapi.ar06.xyz/video_info";
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
