package org.acme;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class GreetingMain implements QuarkusApplication {

    @Override
    public int run(String... args) throws Exception {
//        if (args != null && args.length == 1) {
//            System.out.println("Creating index dir");
//
//            String indexFile = "/Users/gunnar/Development/quarkus/quarkus-lucene/searchindex.json";
//            String indexDir = "/Users/gunnar/Development/quarkus/quarkus-lucene/index";
//
//            Directory dir = new SimpleFSDirectory(Paths.get(indexDir));
//
//            Analyzer analyzer = new StandardAnalyzer();
//            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
//
//            //Always overwrite the directory
//            iwc.setOpenMode(OpenMode.CREATE);
//
//            try(FileInputStream fis = new FileInputStream(indexFile);
//                    IndexWriter indexWriter = new IndexWriter(dir, iwc)) {
//
//                JsonReader reader = Json.createReader(fis);
//                for(JsonValue object : reader.readArray()){
//                    Document doc = new Document();
//                    JsonObject jsonObject = object.asJsonObject();
//
//                    doc.add(new TextField("content", jsonObject.getString("content"), Field.Store.YES));
//                    doc.add(new TextField("uri", jsonObject.getString("uri"), Field.Store.YES));
//                    doc.add(new TextField("title", jsonObject.getString("title"), Field.Store.YES));
//
//                    indexWriter.addDocument(doc);
//                }
//
//                indexWriter.commit();
//            }
//            catch (IOException e) {
//                throw new RuntimeException("Couldn't index JSON file", e);
//            }
//
//        }
//        else {
            System.out.println("Waiting for HTTP requests");
            Quarkus.waitForExit();
//        }

        return 0;
    }

    public static void main(String[] args) {
        System.out.println("Hello");
        Quarkus.run(args);
    }
}
