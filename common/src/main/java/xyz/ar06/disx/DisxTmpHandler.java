package xyz.ar06.disx;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class DisxTmpHandler {
    public static final String TMP_PATH = ".disx-tmp";
    public static final String TMP_CACHE_PATH = TMP_PATH + "/cache";
    public static final String TMP_PROCESSED_CACHE_PATH = TMP_PATH + "/processed_cache";

    public static void onServerStart(MinecraftServer minecraftServer){
        File dir = new File(TMP_PATH);
        Path path = Path.of(TMP_PATH);
        if (!dir.exists()){
            DisxLogger.debug("Server start detected; tmp directory does not exist; initializing it");
            initTempPath();
        }

        DisxLogger.debug("Registering temp-file handling loops");
        //TickEvent.ServerLevelTick.SERVER_POST.register(DisxTmpHandler::cacheTTLHandler);
        TickEvent.ServerLevelTick.SERVER_POST.register(DisxTmpHandler::cacheMaxCapacityHandler);
    }

    private static void initTempPath(){
        try {
            Files.createDirectory(Path.of(TMP_PATH));
            Files.createDirectory(Path.of(TMP_CACHE_PATH));
            Files.createDirectory(Path.of(TMP_PROCESSED_CACHE_PATH));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void clearTempPath(){
        try {
            Path path = Path.of(TMP_PATH);
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file); // Delete each file
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir); // Delete directory after files inside are gone
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void markAudioUsed(File audioFile){
        audioFile.setLastModified(System.currentTimeMillis());
    }

    private static void cleanCache(long cacheSize){
        File[] files = new File(TMP_CACHE_PATH).listFiles();

        Arrays.sort(files,
                Comparator.comparingLong(File::lastModified));

        for (File f : files) {
            if (cacheSize <= MAX_CACHE_SIZE) break;
            cacheSize -= f.length();
            f.delete();
        }
    }

    private static void cleanProcessedCache(long cacheSize){
        File[] files = new File(TMP_PROCESSED_CACHE_PATH).listFiles();

        Arrays.sort(files,
                Comparator.comparingLong(File::lastModified));

        for (File f : files) {
            if (cacheSize <= MAX_CACHE_SIZE) break;
            cacheSize -= f.length();
            f.delete();
        }
    }

    public static void cacheTTLHandler(MinecraftServer minecraftServer) {
        try {
            if (new File(TMP_CACHE_PATH).exists()){
                List<File> filesStream = Files.list(Path.of(TMP_CACHE_PATH))
                        .map(path -> new File(path.toUri()))
                        .toList();
                for (File file : filesStream){
                    long currentMilli = System.currentTimeMillis();
                    long oldMilli = file.lastModified();
                    if (currentMilli - oldMilli >= 3600000L){
                        String fileName = file.getName();
                        String videoId = fileName.substring(0, fileName.length() - 4);
                        DisxLogger.debug("Cached video '" + videoId + "' has reached max TTL. Deleting");
                        file.delete();
                    }
                }
            }

            if (new File(TMP_PROCESSED_CACHE_PATH).exists()){
                List<File> filesStream = Files.list(Path.of(TMP_CACHE_PATH))
                        .map(path -> new File(path.toUri()))
                        .toList();
                for (File file : filesStream){
                    long currentMilli = System.currentTimeMillis();
                    long oldMilli = file.lastModified();
                    if (currentMilli - oldMilli == 3600000L){
                        String fileName = file.getName();
                        String videoId = fileName.substring(0, fileName.length() - 4);
                        DisxLogger.debug("Cached video '" + videoId + "' has reached max TTL. Deleting");
                        file.delete();
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static long MAX_CACHE_SIZE = 5L * 1024 * 1024 * 1024;
    public static void cacheMaxCapacityHandler(MinecraftServer minecraftServer) {
        try {
            long totalBytes;
            if (new File(TMP_CACHE_PATH).exists()){
                Stream<Path> files = Files.list(Path.of(TMP_CACHE_PATH));
                totalBytes = files
                        .filter(Files::isRegularFile)
                        .mapToLong(path -> {
                            try {
                                return Files.size(path);
                            } catch (IOException e) {
                                return 0L;
                            }
                        })
                        .sum();
                if (totalBytes >= MAX_CACHE_SIZE){
                    DisxLogger.debug("Audio cache reached 5 GB threshold- cleaning cache");
                    cleanCache(totalBytes);
                }
            }
            if (new File(TMP_PROCESSED_CACHE_PATH).exists()){
                Stream<Path> files = Files.list(Path.of(TMP_PROCESSED_CACHE_PATH));
                totalBytes = files
                        .filter(Files::isRegularFile)
                        .mapToLong(path -> {
                            try {
                                return Files.size(path);
                            } catch (IOException e) {
                                return 0L;
                            }
                        })
                        .sum();
                if (totalBytes >= MAX_CACHE_SIZE){
                    DisxLogger.debug("Processed audio cache reached 5 GB threshold- cleaning cache");
                    cleanProcessedCache(totalBytes);
                }
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
