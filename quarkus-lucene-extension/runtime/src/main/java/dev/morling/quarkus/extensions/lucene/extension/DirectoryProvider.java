/*
 * Copyright Gunnar Morling
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package dev.morling.quarkus.extensions.lucene.extension;

import java.io.IOException;
import java.nio.file.Paths;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class DirectoryProvider {

    @ConfigProperty(name = "lucene.index.directory", defaultValue = "index")
    String indexDirectory;


    @Produces
    public Directory directory() {
        try {
            return new SimpleFSDirectory(Paths.get(indexDirectory));
        }
        catch (IOException e) {
            throw new RuntimeException("Couldn't load index", e);
        }
    }
}
