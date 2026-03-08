package xyz.ar06.disx.config;


import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.EventResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;
import xyz.ar06.disx.*;
import xyz.ar06.disx.utils.DisxUUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.json.*;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class DisxConfigHandler {
    public static class SERVER {
        private static final String filePath = "config/disx/disx_server_config.properties";
        private static final String useWhitelistJsonPath = "config/disx/player_use_whitelist.json";
        private static final String useBlacklistJsonPath = "config/disx/player_use_blacklist.json";
        private static final String dimensionBlacklistJsonPath = "config/disx/dimension_blacklist.json";
        private static final InputStream defaultCopy = DisxConfigHandler.class.getClassLoader().getResourceAsStream("disx_server_config.properties");
        private static final InputStream defaultUseWhitelistJson = DisxConfigHandler.class.getClassLoader().getResourceAsStream("player_use_whitelist.json");
        private static final InputStream defaultUseBlacklistJson = DisxConfigHandler.class.getClassLoader().getResourceAsStream("player_use_blacklist.json");
        private static final InputStream defaultDimensionBlacklistJson = DisxConfigHandler.class.getClassLoader().getResourceAsStream("dimension_blacklist.json");
        private static final Properties properties = new Properties();
        private static JSONObject useWhitelist;
        private static JSONObject useBlacklist;
        private static JSONObject dimensionBlacklist;
        public static void initializeConfig(MinecraftServer minecraftServer){
            //Config Properties File Check/Create/Initialization/Update
            File dir = new File(filePath);
            File parent = dir.getParentFile();
            File configFolder = parent.getParentFile();
            if (!parent.exists() || !dir.exists() || !configFolder.exists()){
                configFolder.mkdir();
                parent.mkdir();
                try {
                    FileOutputStream outputStream = new FileOutputStream(dir);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = defaultCopy.read(buffer)) != -1){
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    defaultCopy.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
            try {
                InputStream input = new FileInputStream(new File(filePath));
                if (input == null){
                    DisxLogger.error("Disx Server Config Not Found and or Not Generated!");
                } else {
                    properties.load(input);
                    input.close();
                }
            } catch (IOException e){
                e.printStackTrace();
            }
            updateConfig();
            DisxModInfo.setDEBUG(Boolean.parseBoolean(properties.getProperty("debug_mode")));
            //DisxModInfo.setUseYtsrc(Boolean.parseBoolean(properties.getProperty("use_live_ytsrc")));
            //DisxModInfo.setRefreshToken(properties.getProperty("refresh_token"));
            DisxModInfo.setAudioRadius(Integer.parseInt(properties.getProperty("audio_radius")));
            DisxModInfo.setSoundParticles(Boolean.parseBoolean(properties.getProperty("sound_particles")));
            //JSON Check/Create/Initialization
            //Player Use Whitelist JSON
            File dir2 = new File(useWhitelistJsonPath);
            if (!dir2.exists()){
                try {
                    FileOutputStream outputStream = new FileOutputStream(dir2);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = defaultUseWhitelistJson.read(buffer)) != -1){
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    defaultUseWhitelistJson.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
            try {
                InputStream input = new FileInputStream(dir2);
                if (input == null){
                    DisxLogger.error("Disx Use Whitelist Json File Not Found and or Not Generated!");
                } else {
                    JSONTokener jsonTokener = new JSONTokener(input);
                    JSONObject jsonObject = new JSONObject(jsonTokener);
                    useWhitelist = jsonObject;
                }
            } catch (IOException e){
                e.printStackTrace();
            }
            //Player Use Blacklist JSON
            File dir3 = new File(useBlacklistJsonPath);
            if (!dir3.exists()){
                try {
                    FileOutputStream outputStream = new FileOutputStream(dir3);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = defaultUseBlacklistJson.read(buffer)) != -1){
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    defaultUseBlacklistJson.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
            try {
                InputStream input = new FileInputStream(dir3);
                if (input == null){
                    DisxLogger.error("Disx Use Blacklist Json File Not Found and or Not Generated!");
                } else {
                    JSONTokener jsonTokener = new JSONTokener(input);
                    JSONObject jsonObject = new JSONObject(jsonTokener);
                    useBlacklist = jsonObject;
                }
            } catch (IOException e){
                e.printStackTrace();
            }
            //Dimension Blacklist JSON
            File dir4 = new File(dimensionBlacklistJsonPath);
            if (!dir4.exists()){
                try {
                    FileOutputStream outputStream = new FileOutputStream(dir4);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = defaultDimensionBlacklistJson.read(buffer)) != -1){
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    defaultDimensionBlacklistJson.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
            try {
                InputStream input = new FileInputStream(dir4);
                if (input == null){
                    DisxLogger.error("Disx Dimension Blacklist Json File Not Found and or Not Generated!");
                } else {
                    JSONTokener jsonTokener = new JSONTokener(input);
                    JSONObject jsonObject = new JSONObject(jsonTokener);
                    dimensionBlacklist = jsonObject;
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        public static void updateConfig(){
            int config_version = Integer.parseInt(properties.getProperty("config_version"));
            if (config_version < 1){
                updateProperty("debug_mode", "false");
                updateProperty("config_version", "1");
            }
            if (config_version < 2){
                //updateProperty("use_live_ytsrc", "false");
                updateProperty("config_version", "2");
                updateProperty("refresh_token","");
            }
            if (config_version < 3){
                updateProperty("audio_radius", "25");
                updateProperty("sound_particles", "true");
                updateProperty("config_version", "3");
            }
            if (config_version < 4){
                if (properties.containsKey("use_live_ytsrc")){
                    properties.remove("use_live_ytsrc");
                }
                if (properties.containsKey("refresh_token")){
                    properties.remove("refresh_token");
                }
                updateProperty("config_version", "4");
            }
        }

        public static String getProperty(String key){
            return properties.getProperty(key);
        }

        public static void updateProperty(String key, String value){
            properties.setProperty(key, value);
            DisxModInfo.setDEBUG(Boolean.parseBoolean(properties.getProperty("debug_mode", "false")));
            //DisxModInfo.setUseYtsrc(Boolean.parseBoolean(properties.getProperty("use_live_ytsrc", "false")));
            //DisxModInfo.setRefreshToken(properties.getProperty("refresh_token", ""));
            DisxModInfo.setSoundParticles(Boolean.parseBoolean(properties.getProperty("sound_particles", "true")));
            DisxModInfo.setAudioRadius(Integer.parseInt(properties.getProperty("audio_radius", "25")));
            DisxServerPacketIndex.ServerPackets.serverConfigSendAll();
            updateConfigFile();
        }

        private static void updateConfigFile(){
            try (OutputStream outputStream = new FileOutputStream(filePath)) {
                properties.store(outputStream, "DO NOT EDIT, MODIFY, OR TOUCH CONFIG_ENV OR CONFIG_VERSION");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static boolean isOnUseBlacklist(UUID uuid) {
            JSONArray jsonArray = useBlacklist.getJSONArray("blacklist");
            List<Object> list = jsonArray.toList();
            for (Object o : list){
                if (o.toString().equals(uuid.toString())){
                    return true;
                }
            }
            return false;
        }

        public static boolean isOnUseWhitelist(UUID uuid) {
            if (getProperty("player_use_whitelist_enabled").equals("false")){
                return true;
            }
            JSONArray jsonArray = useWhitelist.getJSONArray("whitelist");
            List<Object> list = jsonArray.toList();
            for (Object o : list){
                if (o.toString().equals(uuid.toString())){
                    return true;
                }
            }
            return false;
        }

        public static boolean isOnDimensionBlacklist(ResourceKey<Level> dimension) {
            ResourceLocation dimensionLocation = dimension.location();
            JSONArray jsonArray = dimensionBlacklist.getJSONArray("dimension_blacklist");
            List<Object> list = jsonArray.toList();
            for (Object o : list){
                if (o.equals(dimensionLocation.toString())){
                    return true;
                }
            }
            return false;
        }

        public static List<Object> getDimensionBlacklist() {
            return dimensionBlacklist.getJSONArray("dimension_blacklist").toList();
        }

        public static List<Object> getUseWhitelist() {
            return useWhitelist.getJSONArray("whitelist").toList();
        }

        public static List<Object> getUseBlacklist() {
            return useBlacklist.getJSONArray("blacklist").toList();
        }

        public static String addToUseWhitelist(String username, CommandContext<CommandSourceStack> context){
            try {
                UUID uuid = DisxUUIDUtil.getUuidFromUsername(username);
                List<Object> whitelist = getUseWhitelist();
                if (whitelist.contains(uuid.toString())){
                    context.getSource().sendFailure(Component.translatable("sysmsg.disx.configcmd.whitelist_modify_add_dupe"));
                    return "duplicate";
                }
                whitelist.add(uuid.toString());
                DisxLogger.debug("WHITELIST LIST OBJECT: " + whitelist);
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                for (Object o : whitelist){
                    jsonArray.put(o);
                }
                jsonObject.put("whitelist", jsonArray);
                DisxLogger.debug(jsonObject);
                FileWriter fileWriter = new FileWriter(useWhitelistJsonPath);
                jsonObject.write(fileWriter);
                fileWriter.close();
                useWhitelist = jsonObject;
                context.getSource().sendSystemMessage(Component.translatable("sysmsg.disx.configcmd_whitelist_modified_add", username));
                return "success";
            } catch (Exception e) {
                e.printStackTrace();
                context.getSource().sendFailure(Component.translatable("sysmsg.disx.configcmd.whitelist_modify_err"));
                return "failure";
            }
        }


        public static String removeFromUseWhitelist(String username, CommandContext<CommandSourceStack> context){
            try {
                UUID uuid = DisxUUIDUtil.getUuidFromUsername(username);
                List<Object> whitelist = getUseWhitelist();
                DisxLogger.debug("checking if user is on whitelist");
                if (whitelist.contains(uuid.toString())){
                    DisxLogger.debug("user is, removing them");
                    whitelist.remove(uuid.toString());
                } else {
                    context.getSource().sendFailure(Component.translatable("sysmsg.disx.configcmd.whitelist_modify_remove_err"));
                    return "notfoundonit";
                }
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                for (Object o : whitelist){
                    jsonArray.put(o);
                }
                jsonObject.put("whitelist", jsonArray);
                DisxLogger.debug(jsonObject);
                FileWriter fileWriter = new FileWriter(useWhitelistJsonPath);
                jsonObject.write(fileWriter);
                fileWriter.close();
                useWhitelist = jsonObject;
                context.getSource().sendSystemMessage(Component.translatable("sysmsg.disx.configcmd_whitelist_modified_remove", username));
                return "success";
            } catch (Exception e) {
                e.printStackTrace();
                context.getSource().sendFailure(Component.translatable("sysmsg.disx.configcmd.whitelist_modify_err"));
                return "failure";
            }
        }

        public static String addToUseBlacklist(String username, CommandContext<CommandSourceStack> context){
            try {
                UUID uuid = DisxUUIDUtil.getUuidFromUsername(username);
                List<Object> blacklist = getUseBlacklist();
                if (blacklist.contains(uuid.toString())){
                    context.getSource().sendFailure(Component.translatable("sysmsg.disx.configcmd.blacklist_modify_add_dupe"));
                    return "duplicate";
                }
                blacklist.add(uuid.toString());
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                for (Object o : blacklist){
                    jsonArray.put(o);
                }
                jsonObject.put("blacklist", jsonArray);
                FileWriter fileWriter = new FileWriter(useBlacklistJsonPath);
                jsonObject.write(fileWriter);
                fileWriter.close();
                useBlacklist = jsonObject;
                context.getSource().sendSystemMessage(Component.translatable("sysmsg.disx.configcmd_blacklist_modified_add", username));
                return "success";
            } catch (Exception e) {
                e.printStackTrace();
                context.getSource().sendFailure(Component.translatable("sysmsg.disx.configcmd.blacklist_modify_err"));
                return "failure";
            }
        }

        public static String removeFromUseBlacklist(String username, CommandContext<CommandSourceStack> context){
            try {
                UUID uuid = DisxUUIDUtil.getUuidFromUsername(username);
                List<Object> blacklist = getUseBlacklist();
                if (blacklist.contains(uuid.toString())){
                    blacklist.remove(uuid.toString());
                } else {
                    context.getSource().sendFailure(Component.translatable("sysmsg.disx.configcmd.blacklist_modify_remove_err"));
                    return "notfoundonit";
                }
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                for (Object o : blacklist){
                    jsonArray.put(o);
                }
                jsonObject.put("blacklist", jsonArray);
                FileWriter fileWriter = new FileWriter(useBlacklistJsonPath);
                jsonObject.write(fileWriter);
                fileWriter.close();
                useBlacklist = jsonObject;
                context.getSource().sendSystemMessage(Component.translatable("sysmsg.disx.configcmd_blacklist_modified_remove", username));
                return "success";
            } catch (Exception e) {
                e.printStackTrace();
                context.getSource().sendFailure(Component.translatable("sysmsg.disx.configcmd.blacklist_modify_err"));
                return "failure";
            }
        }

        public static String addToDimensionBlacklist(ResourceLocation dimension){
            try {
                List<Object> blacklist = getDimensionBlacklist();
                if (!blacklist.contains(dimension.toString())) {
                    blacklist.add(dimension.toString());
                } else {
                    return "duplicate";
                }
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                for (Object o : blacklist) {
                    jsonArray.put(o);
                }
                jsonObject.put("dimension_blacklist", jsonArray);
                FileWriter fileWriter = new FileWriter(dimensionBlacklistJsonPath);
                jsonObject.write(fileWriter);
                fileWriter.close();
                dimensionBlacklist = jsonObject;
                return "success";
            } catch (Exception e){
                e.printStackTrace();
                return "failure";
            }
        }

        public static String removeFromDimensionBlacklist(ResourceLocation dimension){
            try {
                List<Object> blacklist = getDimensionBlacklist();
                if (blacklist.contains(dimension.toString())) {
                    blacklist.remove(dimension.toString());
                } else {
                    return "notfoundonit";
                }
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                for (Object o : blacklist) {
                    jsonArray.put(o);
                }
                jsonObject.put("dimension_blacklist", jsonArray);
                FileWriter fileWriter = new FileWriter(dimensionBlacklistJsonPath);
                jsonObject.write(fileWriter);
                fileWriter.close();
                dimensionBlacklist = jsonObject;
                return "success";
            } catch (Exception e){
                e.printStackTrace();
                return "failure";
            }
        }
    }


    public static class CLIENT {
        private static final String filePath = "config/disx/disx_client_config.properties";
        private static final InputStream defaultCopy = DisxConfigHandler.class.getClassLoader().getResourceAsStream("disx_client_config.properties");

        private static final Properties properties = new Properties();
        public static void initializeConfig(Minecraft minecraft){
            //Config Properties File Check/Create/Initialization/Update
            File dir = new File(filePath);
            File parent = dir.getParentFile();
            File configFolder = parent.getParentFile();
            if (!parent.exists() || !dir.exists() || !configFolder.exists()){
                configFolder.mkdir();
                parent.mkdir();
                try {
                    FileOutputStream outputStream = new FileOutputStream(dir);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = defaultCopy.read(buffer)) != -1){
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    defaultCopy.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
            try {
                InputStream input = new FileInputStream(new File(filePath));
                if (input == null){
                    DisxLogger.error("Disx Client Config Not Found and or Not Generated!");
                } else {
                    properties.load(input);
                    input.close();
                }
            } catch (IOException e){
                e.printStackTrace();
            }
            DisxModInfo.setDEBUG(Boolean.parseBoolean(getProperty("debug_mode")));
        }

        public static String getProperty(String key){
            return properties.getProperty(key);
        }

        public static void updateProperty(String key, String value){
            properties.setProperty(key, value);
            DisxModInfo.setDEBUG(Boolean.parseBoolean(getProperty("debug_mode")));
            updateConfigFile();
        }

        private static void updateConfigFile(){
            try (OutputStream outputStream = new FileOutputStream(filePath)) {
                properties.store(outputStream, "DO NOT EDIT, MODIFY, OR TOUCH CONFIG_ENV OR CONFIG_VERSION");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
