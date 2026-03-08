package xyz.ar06.disx.utils;

import xyz.ar06.disx.DisxLogger;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public class DisxUUIDUtil {
    private static final String MOJANG_API_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String MOJANG_PROFILE_API_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

    public static UUID getUuidFromUsername(String username) throws Exception {
        // Construct the URL for Mojang's API endpoint
        URL url = new URL(MOJANG_API_URL + username);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Check for successful response
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new Exception("Failed to get UUID from Mojang API");
        }

        // Read the response from the API
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // Parse the JSON response
            JSONObject jsonObject = new JSONObject(response.toString());
            DisxLogger.debug("RESPONSE JSON: " + jsonObject.toString());
            String uuidString = jsonObject.getString("id");
            DisxLogger.debug("UUID GOTTEN: " + uuidString);
            // Convert UUID string to UUID object
            return UUID.fromString(formatUuid(uuidString));
        }
    }

    private static String formatUuid(String uuid) {
        // Format UUID to standard UUID format
        return uuid.replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5"
        );
    }

    public static String getUsernameFromUuid(UUID uuid) throws Exception {
        // Construct the URL for Mojang's API endpoint
        URL url = new URL(MOJANG_PROFILE_API_URL + uuid.toString().replace("-", ""));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Check for successful response
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            return "NULL";
        }

        // Read the response from the API
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // Parse the JSON response
            JSONObject jsonObject = new JSONObject(response.toString());
            return jsonObject.getString("name");
        }
    }
}
