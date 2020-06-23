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
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
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

              FieldType ft = new FieldType();
              ft.setStored(true);
              ft.setStoreTermVectors(true);
              ft.setStoreTermVectorOffsets(true);
              ft.setStoreTermVectorPositions(true);
              ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);

              doc.add(new Field("content", jsonObject.getString("content"), ft));
              doc.add(new Field("title", jsonObject.getString("title"), ft));
              doc.add(new Field("uri", jsonObject.getString("uri"), ft));
              String tags = getTags(jsonObject);

              if (tags != null) {
                  doc.add(new Field("tags", tags, ft));
              }

              doc.add(new TextField("publicationdate", jsonObject.getString("publicationdate"), Field.Store.YES));

              indexWriter.addDocument(doc);
          }

          indexWriter.commit();

          return dir;
      }
      catch (IOException e) {
          throw new RuntimeException("Couldn't index JSON file", e);
      }
    }

    private String getTags(JsonObject jsonObject) {
        JsonValue tags = jsonObject.get("tags");

        if (tags != null) {
            if (tags.getValueType() == ValueType.ARRAY) {
                JsonString[] tagsAsString = tags.asJsonArray().toArray(new JsonString[0]);

                return Arrays.stream(tagsAsString)
                    .map(JsonString::getString)
                    .collect(Collectors.joining(", "));
            }
            else if (tags.getValueType() == ValueType.STRING) {
                return tags.toString();
            }
        }

        return null;
    }

    @BuildStep
    void registerAdditionalBeans(BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        additionalBeans.produce(new AdditionalBeanBuildItem(DirectoryProvider.class));
    }

    private static InputStream getSearchIndexFile() {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(SEARCH_INDEX_FILE);
    }
}
