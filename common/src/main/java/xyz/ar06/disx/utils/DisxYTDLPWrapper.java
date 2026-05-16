package xyz.ar06.disx.utils;
import net.minecraft.server.MinecraftServer;
import xyz.ar06.disx.DisxLogger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class DisxYTDLPWrapper {
    private static File ytdlp;
    private static File ffmpeg;
    public static boolean extractYTDLP(MinecraftServer minecraftServer){
        try {
            InputStream resource = DisxYTDLPWrapper.class.getClassLoader().getResourceAsStream("yt-dlp");
            if (resource == null){
                throw new FileNotFoundException();
            }
            if (!Files.exists(Path.of(".disx-tmp"))){
                Files.createDirectory(Path.of(".disx-tmp"));
            }
            File file = new File(".disx-tmp/yt-dlp");
            Files.copy(resource, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            file.setExecutable(true);
            ytdlp = file;
            return true;
        }
        catch (FileNotFoundException f){
            DisxLogger.debug("YTDLP EXTRACTABLE NOT FOUND; SETTING YTDLP OBJECT TO NULL");
            f.printStackTrace();
            return false;
        } catch (IOException e) {
            DisxLogger.debug("RAN INTO RUNTIME EXCEPTION, SETTING YTDLP OBJECT TO NULL");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean extractFFMPEG(MinecraftServer minecraftServer){
        try {
            DisxLogger.debug("Attempting to extract FFMPEG");
            InputStream resource = null;
            String os = System.getProperty("os.name");
            String arch = System.getProperty("os.arch");
            String fileName = "ffmpeg";
            DisxLogger.debug("JAVA-DETECTED OS + ARCH: " + os + ", " + arch);
            if (os.contains("Windows") && arch.equals("amd64")){
                DisxLogger.debug("SCRIPT DETECTED OS + ARCH: Windows, amd64");
                resource = DisxYTDLPWrapper.class.getClassLoader().getResourceAsStream("ffmpeg-windows.exe");
                fileName = "ffmpeg-windows.exe";
            } else if (os.contains("Linux") && (arch.equals("amd64") || arch.equals("x86"))) {
                DisxLogger.debug("SCRIPT DETECTED OS + ARCH: Linux, amd64");
                resource = DisxYTDLPWrapper.class.getClassLoader().getResourceAsStream("ffmpeg-linux64");
            } else if (os.contains("Mac") && (arch.equals("x86_64"))){
                DisxLogger.debug("SCRIPT DETECTED OS + ARCH: Mac, x86_64");
                resource = DisxYTDLPWrapper.class.getClassLoader().getResourceAsStream("ffmpeg-macos64");
            } else if (os.contains("Mac") && (arch.equals("aarch64") || arch.equals("arm64"))){
                DisxLogger.debug("SCRIPT DETECTED OS + ARCH: Mac, aarch64/arm64");
                resource = DisxYTDLPWrapper.class.getClassLoader().getResourceAsStream("ffmpeg.macosarm");
            } else {
                DisxLogger.debug("UNABLE TO DETERMINE PROPER OS FOR FFMPEG BINARY; FEATURES THAT REQUIRE FFMPEG WILL NOT WORK!");
            }
            if (resource == null){
                throw new FileNotFoundException();
            }
            if (!Files.exists(Path.of(".disx-tmp"))){
                Files.createDirectory(Path.of(".disx-tmp"));
            }
            File file = new File(".disx-tmp/" + fileName);
            Files.copy(resource, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            file.setExecutable(true);
            ffmpeg = file;
            return true;
        } catch (FileNotFoundException f){
            DisxLogger.debug("FFMPEG EXTRACTABLE NOT FOUND; SETTING FFMPEG OBJECT TO NULL");
            f.printStackTrace();
            return false;
        } catch (IOException e) {
            DisxLogger.debug("RAN INTO RUNTIME EXCEPTION, SETTING FFMPEG OBJECT TO NULL");
            e.printStackTrace();
            return false;
        }
    }

    public static String getVideoAudioURL(String videoId) throws Exception {
        DisxLogger.debug("Video Audio URL call requested from YTDLP");
        String ytdlpPath = ytdlp.getAbsolutePath();
        ProcessBuilder processBuilder = new ProcessBuilder(
                ytdlpPath,
                "-f", "251",
                "--get-url",
                "https://youtube.com/watch?v=" + videoId
        );
        try {
            DisxLogger.debug("Attempting to run YTDLP subprocess");
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            String returnUrl = "";
            while ((line = reader.readLine()) != null) {
                DisxLogger.debug("YTDLP OUTPUT: " + line);
                if (line.startsWith("https")){
                    returnUrl = line;
                    DisxLogger.debug("DETECTED URL OUTPUT AS: " + returnUrl);
                }
            }

            int exitCode = process.waitFor();
            DisxLogger.debug("yt-dlp exited with code: " + exitCode);
            if (returnUrl.isEmpty()){
                throw new Exception("NO RETURN URL");
            }
            return returnUrl;
        } catch (IOException e) {
            throw new Exception("YT-DLP FAILED START");
        } catch (InterruptedException e) {
            throw new Exception("YT-DLP FAILED START");
        }
    }

    public static String getVideoName(String videoId){
        DisxLogger.debug("Video Title call requested from YTDLP");
        String result = "";
        ProcessBuilder processBuilder = new ProcessBuilder(
                ytdlp.getAbsolutePath(),
                "--print",
                "title",
                "https://youtube.com/watch?v=" + videoId
        );
        try {

            Process process = processBuilder.start();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line = "";
            DisxLogger.debug("Attempting to run YTDLP subprocess");
            while ((line = bufferedReader.readLine()) != null){
                DisxLogger.debug("YTDLP OUTPUT: " + line);
                if (!line.startsWith("WARNING") || !line.startsWith("ERROR")){
                    result = line;
                }
            }

        } catch (IOException e) {
            DisxLogger.error(e);
        }
        return result;
    }

    public static int getVideoLength(String videoId){
        int result = -1;
        DisxLogger.debug("Video Length call requested from YTDLP");
        ProcessBuilder processBuilder = new ProcessBuilder(
                ytdlp.getAbsolutePath(),
                "--print",
                "duration",
                "https://youtube.com/watch?v=" + videoId
        );
        try {

            Process process = processBuilder.start();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line = "";
            DisxLogger.debug("Attempting to run YTDLP subprocess");
            while ((line = bufferedReader.readLine()) != null){
                DisxLogger.debug("YTDLP OUTPUT: " + line);
                if (!line.startsWith("WARNING") || !line.startsWith("ERROR")){
                    result = Integer.parseInt(line);
                }
            }

        } catch (IOException e) {
            DisxLogger.error(e);
        }
        return result;
    }

    public static ArrayList<String> getTitleAndLength(String videoId){
        ArrayList<String> result = new ArrayList<String>();
        DisxLogger.debug("Video Length AND Duration call requested from YTDLP");
        ProcessBuilder processBuilder = new ProcessBuilder(
                ytdlp.getAbsolutePath(),
                "--print", "title",
                "--print", "duration",
                "https://youtube.com/watch?v=" + videoId
        );
        try {
            Process process = processBuilder.start();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line = "";
            DisxLogger.debug("Attempting to run YTDLP subprocess");
            while ((line = bufferedReader.readLine()) != null){
                DisxLogger.debug("YTDLP OUTPUT: " + line);
                result.add(line);
            }

        } catch (IOException e) {
            DisxLogger.error(e);
        }
        if (result.isEmpty()){
            return null;
        } else {
            return result;
        }
    }

    public static byte[] downloadToBuffer(String videoId) throws IOException, InterruptedException {
            File dir = new File("./.disx-tmp");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // Unique filename (avoid collisions)
            String filename = "track_" + System.currentTimeMillis() + ".wav";

            // Run yt-dlp
            String ffmpegLocationString = (ffmpeg != null ? ffmpeg.getAbsolutePath().toString() : "");
            String ffmpegLocationArg = (ffmpeg != null ? "--ffmpeg-location" : "");
            ProcessBuilder processBuilder = new ProcessBuilder(
                    ytdlp.getAbsolutePath(),
                    ffmpegLocationArg, ffmpegLocationString,
                    "-f", "bestaudio",
                    "-x",
                    "--audio-format", "wav",
                    "--postprocessor-args", "-ar 48000 -ac 2 -sample_fmt s16 -f s16be",
                    "-o", "./.disx-tmp/" + filename,
                    "https://www.youtube.com/watch?v=" + videoId
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line = "";
            DisxLogger.debug("Attempting to run YTDLP subprocess");
            while ((line = bufferedReader.readLine()) != null){
                DisxLogger.debug("YTDLP OUTPUT: " + line);
            }

            int exitCode = process.waitFor();
            File outputFile = new File(dir, filename);
            if (exitCode != 0 || !outputFile.exists()) {
                DisxLogger.debug("YTDLP process failed OR output file not created");
            }

            byte[] data = Files.readAllBytes(outputFile.toPath());

            if (!outputFile.delete()) {
                outputFile.deleteOnExit();
            }

            return data;
    }

}
