package dev.morling.quarkus.extensions.lucene.extension.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

import java.io.IOException;
import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.RAMDirectory;

import dev.morling.quarkus.extensions.lucene.extension.DirectoryProvider;
import dev.morling.quarkus.extensions.lucene.extension.RamDirectoryRecorder;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerListenerBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.deployment.recording.RecorderContext;

class QuarkusLuceneExtensionProcessor {

    private static final String FEATURE = "quarkus-lucene-extension";

    private static final String SEARCH_INDEX_FILE = "/META-INF/searchindex.json";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(STATIC_INIT)
    public void build(RamDirectoryRecorder recorder, RecorderContext recorderContext,
            BuildProducer<BeanContainerListenerBuildItem> beanContainerListener,
            ShutdownContextBuildItem shutdownContext) throws Exception {

        RAMDirectory directory = getRamDirectory();
System.out.println("### DIR" + directory);
        beanContainerListener.produce(new BeanContainerListenerBuildItem(
                recorder.initializeDirectory(directory, shutdownContext)));
    }

    private RAMDirectory getRamDirectory() {
      RAMDirectory dir = new RAMDirectory();

      Analyzer analyzer = new StandardAnalyzer();
      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

      //Always overwrite the directory
      iwc.setOpenMode(OpenMode.CREATE);

      try(InputStream fis = getSearchIndexFile();
              IndexWriter indexWriter = new IndexWriter(dir, iwc)) {

          JsonReader reader = Json.createReader(fis);
          for(JsonValue object : reader.readArray()){
              Document doc = new Document();
              JsonObject jsonObject = object.asJsonObject();

              doc.add(new TextField("content", jsonObject.getString("content"), Field.Store.YES));
              doc.add(new TextField("uri", jsonObject.getString("uri"), Field.Store.YES));
              doc.add(new TextField("title", jsonObject.getString("title"), Field.Store.YES));

              indexWriter.addDocument(doc);
          }

          indexWriter.commit();

          return dir;
      }
      catch (IOException e) {
          throw new RuntimeException("Couldn't index JSON file", e);
      }
    }

    @BuildStep
    void registerAdditionalBeans(BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        additionalBeans.produce(new AdditionalBeanBuildItem(DirectoryProvider.class));
    }

    private static InputStream getSearchIndexFile() {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(SEARCH_INDEX_FILE);
    }
}
