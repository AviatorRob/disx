package xyz.ar06.disx;

import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class DisxTmpHandler {
    private static final String TMP_PATH = ".disx-tmp";
    public static void onServerStart(MinecraftServer minecraftServer){
        File dir = new File(TMP_PATH);
        Path path = Path.of(TMP_PATH);
        if (dir.exists()){
            DisxLogger.debug("Server start detected; tmp directory already exists, clearing it");
            try {
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
    }
}
