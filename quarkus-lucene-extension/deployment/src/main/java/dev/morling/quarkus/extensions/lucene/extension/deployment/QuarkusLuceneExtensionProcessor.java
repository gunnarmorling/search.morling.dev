/*
 * Copyright Gunnar Morling
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package dev.morling.quarkus.extensions.lucene.extension.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

import dev.morling.quarkus.extensions.lucene.extension.DirectoryProvider;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedFileSystemResourceBuildItem;

class QuarkusLuceneExtensionProcessor {

    private static final String FEATURE = "quarkus-lucene-extension";

    private static final String SEARCH_INDEX_FILE = "/META-INF/searchindex.json";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public void createIndex(BuildProducer<GeneratedFileSystemResourceBuildItem> gen) throws Exception {


        Path indexLocation = Files.createTempDirectory("quarkus-build")
                .resolve("index")
                .toAbsolutePath();

        createIndexDirectory(indexLocation);
        Path indexDir = Paths.get("index");

        Files.walk(indexLocation)
            .filter(Files::isRegularFile)
            .forEach(file -> {
                try {
                    String path = indexDir.resolve(indexLocation.relativize(file)).toString();
                    gen.produce(new GeneratedFileSystemResourceBuildItem(path, Files.readAllBytes(file)));
                }
                catch (IOException e) {
                    throw new RuntimeException("Couldn't generate resource", e);
                }
            });
    }

    private Directory createIndexDirectory(Path root) throws Exception {
        Directory dir = new SimpleFSDirectory(root);

      Analyzer analyzer = new StandardAnalyzer();
      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

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
