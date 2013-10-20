package com.granveaud.offheapbench.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class FileUtils {
    final static private Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);
    final static private File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"), "offheapbench");

    static {
        // delete previous directory
        if (TEMP_DIR.exists()) {
            LOGGER.info("Deleting temp directory " + TEMP_DIR.getAbsolutePath());
            deleteDir(TEMP_DIR);
        }

        // create a temp directory
        if (!TEMP_DIR.mkdirs()) {
            throw new RuntimeException("Can't create " + TEMP_DIR.getAbsolutePath());
        }
    }

    private static void deleteDir(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                deleteDir(c);
        }

        if (!f.delete()) {
            throw new RuntimeException("Failed to delete file: " + f);
        }
    }

    public static File createTempFile() throws IOException {
        return File.createTempFile("offheapbench", null, TEMP_DIR);
    }
}
