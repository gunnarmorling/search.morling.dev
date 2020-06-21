package dev.morling.quarkus.extensions.lucene.extension;

import org.apache.lucene.store.Directory;

public class DirectoryHolder {

    private static Directory directory;

    static void initialize(Directory directory) {
        DirectoryHolder.directory = directory;
    }

    static Directory getDirectory() {
        return directory;
    }
}
