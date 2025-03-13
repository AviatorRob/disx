package xyz.ar06.disx;

import dev.architectury.platform.Platform;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

public class DisxModInfo {
    private static final String VERSION = "0.3.0";
    private static final boolean DEV_BUILD = true;
    private static final boolean FORCE_DEBUG = true;
    private static final String[] debugKeys = new String[]{
            "skibidi gooning",
            "uninstall brainrot.exe"
    };
    private static final boolean TEST_TRACK_ENABLED = false;
    private static boolean DEBUG = false;
    private static String LATEST_VERSION = "N/A - NO INTERNET";
    private static final String DISCORD_URL = "http://discord.ar06.xyz";
    private static final String MODRINTH_URL = "https://modrinth.com/mod/disx";
    private static final String CURSEFORGE_URL = "https://www.curseforge.com/minecraft/mc-mods/disx";
    private static final String GITHUB_URL = "https://github.com/AviatorRob/disx";
    private static final String ROADMAP_URL = "https://trello.com/b/JwbWrPbE";
    private static final String PATREON_URL = "https://www.patreon.com/c/ar06";
    private static final String LATEST_VERSION_URL = "https://raw.githubusercontent.com/AviatorRob/disx/master/LATEST_VERSION.json";

    private static final String CARRYON_CONFIG_INSTRUCTIONS_URL = "https://github.com/Tschipp/CarryOn/wiki/Black---and-Whitelist-Config";

    private static Boolean isUpToDate = true;
    private static int versionsOutdated = 0;
    private static final String[] potentialModConflicts = {"carryon"};

    public static void pullLatestVersion(){
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(LATEST_VERSION_URL))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse the JSON response
                String responseBody = response.body();
                JSONObject json = new JSONObject(responseBody);
                JSONObject subJson = (JSONObject) json.get(Platform.getMinecraftVersion());
                JSONArray versionsJson = subJson.getJSONArray("versions");
                LATEST_VERSION = versionsJson.getString(0).toString();
                if (!LATEST_VERSION.equals(VERSION)){
                    isUpToDate = false;
                    int versionOutdateCounter = 1;
                    boolean foundOnVersionsList = false;
                    for (int i = 1; i < versionsJson.length(); i++){
                        String next = versionsJson.getString(i);
                        if (next.equals(VERSION)) {
                            foundOnVersionsList = true;
                            break;
                        } else {
                            versionOutdateCounter++;
                            DisxLogger.debug("+1 VERSION OUTDATED");
                        }
                    }
                    versionsOutdated = versionOutdateCounter;
                    DisxLogger.debug(Integer.toString(versionsOutdated));
                    if (!foundOnVersionsList){
                        versionsOutdated = 0;
                        isUpToDate = true;
                        DisxLogger.debug("This version not found on version control. Dev build?");
                    }

                }
            } else {
                DisxLogger.error("Version Fetch Request failed with status code: " + response.statusCode());
            }
        } catch (Exception e) {
            if (e instanceof ConnectException){
                DisxLogger.error(e.getCause() + ": Failed to get latest Disx version. Is there an internet connection readily available?");
            } else {
                e.printStackTrace();
            }

        }
    }

    public static HashMap<String, Boolean> getPotentialModConflicts(){
        HashMap<String, Boolean> foundConflicts = new HashMap<>();
        for (String s : potentialModConflicts){
            if (Platform.getOptionalMod(s).isPresent()){
                foundConflicts.put(s, true);
            } else {
                foundConflicts.put(s, false);
            }
        }
        return foundConflicts;
    }

    public static String getPatreonUrl() {
        return PATREON_URL;
    }

    public static String getLatestVersion() {
        return LATEST_VERSION;
    }

    public static String getCurseforgeUrl() {
        return CURSEFORGE_URL;
    }

    public static String getDiscordUrl() {
        return DISCORD_URL;
    }

    public static String getVERSION() {
        return VERSION;
    }

    public static String getGithubUrl() {
        return GITHUB_URL;
    }

    public static String getModrinthUrl() {
        return MODRINTH_URL;
    }

    public static String getRoadmapUrl() {
        return ROADMAP_URL;
    }

    public static Boolean getIsUpToDate() {
        return isUpToDate;
    }

    public static int getVersionsOutdated() {
        return versionsOutdated;
    }

    public static String getCarryonConfigInstructionsUrl() {
        return CARRYON_CONFIG_INSTRUCTIONS_URL;
    }

    public static boolean getIsDevBuild(){
        return DEV_BUILD;
    }

    public static boolean isDEBUG() {
        return (FORCE_DEBUG || DEBUG);
    }

    public static boolean isTESTTRACK() {
        return TEST_TRACK_ENABLED;
    }

    public static void setDEBUG(boolean DEBUG) {
        DisxModInfo.DEBUG = DEBUG;
    }

    public static String[] getDebugKeys() {
        return debugKeys;
    }
}
