package dev.morling.quarkus.extensions.lucene.extension;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.lucene.store.Directory;

@ApplicationScoped
public class DirectoryProvider {

    @Produces
    public Directory directory() {
        return DirectoryHolder.getDirectory();
    }
}
