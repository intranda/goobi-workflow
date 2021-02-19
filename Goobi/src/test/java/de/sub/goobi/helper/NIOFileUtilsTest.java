package de.sub.goobi.helper;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.Test;

public class NIOFileUtilsTest {
    private static NIOFileUtils nio = new NIOFileUtils();

    @Test
    public void deleteInDirShouldDeleteAllFilesInDir() throws IOException {
        //set up our directory
        Path testPath = Files.createTempDirectory("test");
        for (String name : new String[] { "test1", "test2", ".testhidden" }) {
            try (OutputStream os = Files.newOutputStream(testPath.resolve(name))) {
            }
        }

        nio.deleteInDir(testPath);

        try (Stream<Path> dirStream = Files.list(testPath)) {

            assertTrue("directory should be empty", dirStream.count() == 0);
        }
    }

    @Test
    public void deleteInDirShouldDeleteFilesAndFoldersInDir() throws IOException {
        //set up our directory
        Path testPath = Files.createTempDirectory("test");

        Path subPath1 = testPath.resolve("subDir1");
        Files.createDirectories(subPath1);
        Path subPath2 = testPath.resolve("subDir2");
        Files.createDirectories(subPath2);
        Path hiddenSubPath = testPath.resolve("hiddenSubDir");
        Files.createDirectories(hiddenSubPath);
        for (String name : new String[] { "test1", "test2", ".testhiddenFile" }) {
            try (OutputStream os = Files.newOutputStream(testPath.resolve(name))) {
            }
            try (OutputStream os = Files.newOutputStream(subPath2.resolve(name))) {
            }
            try (OutputStream os = Files.newOutputStream(hiddenSubPath.resolve(name))) {
            }
        }

        nio.deleteInDir(testPath);

        try (Stream<Path> dirStream = Files.list(testPath)) {

            assertTrue("directory should be empty", dirStream.count() == 0);
        }
    }
}
