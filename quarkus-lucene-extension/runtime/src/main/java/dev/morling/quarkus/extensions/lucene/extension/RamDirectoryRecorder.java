package dev.morling.quarkus.extensions.lucene.extension;

import org.apache.lucene.store.RAMDirectory;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class RamDirectoryRecorder {

    public BeanContainerListener initializeDirectory(RAMDirectory directory, ShutdownContext shutdownContext) {
        BeanContainerListener beanContainerListener = new BeanContainerListener() {

            @Override
            public void created(BeanContainer container) {
                DirectoryHolder.initialize(directory);

                shutdownContext.addShutdownTask(new Runnable() {
                    @Override
                    public void run() {
                        directory.close();
                    }
                });
            }
        };

        return beanContainerListener;
    }
}
