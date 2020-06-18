package org.acme;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.StartupEvent;

@Path("/hello")
public class ExampleResource {

    @ConfigProperty(name = "indexFile", defaultValue = "/Users/gunnar/Development/quarkus/quarkus-lucene/searchindex.json")
    public String indexFile;

    @ConfigProperty(name = "indexDir", defaultValue = "/Users/gunnar/Development/quarkus/quarkus-lucene/index")
    public String indexDir;

    private Directory dir;

    private IndexSearcher searcher;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response hello(@QueryParam("q") String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(Json.createObjectBuilder()
                            .add("message", "A query must be specified using the 'q' query parameter")
                            .build()
                            )
                    .build();
        }

        IndexSearcher searcher = getSearcher();

        if (searcher == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity(Json.createObjectBuilder()
                            .add("message", "No index found")
                            .build()
                            )
                    .build();
        }

        try {
            Analyzer analyzer = new StandardAnalyzer();

            MultiFieldQueryParser parser = new MultiFieldQueryParser(
                    new String[]{"title", "content"},
                    analyzer);
            parser.setDefaultOperator(Operator.AND);

            Query query = parser.parse(queryString);

            TopDocs topDocs = searcher.search(query, 10);
            List<Document> documents = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                documents.add(searcher.doc(scoreDoc.doc));
            }

            JsonObjectBuilder result = Json.createObjectBuilder()
                    .add("message", "ok");

            JsonArrayBuilder results = Json.createArrayBuilder();
            for (Document document : documents) {
                results.add(Json.createObjectBuilder()
                        .add("uri", document.get("uri"))
                        .add("title", document.get("title")));
            }

            result.add("results", results);

            return Response.ok(result.build()).build();
        }
        catch (IOException | ParseException e) {
            throw new RuntimeException("Couldn't execute query", e);
        }
    }

    public void onStartup(@Observes StartupEvent se) {
        java.nio.file.Path path = Paths.get(indexDir);

        if (!Files.exists(path) || !path.iterator().hasNext()) {
            return;
        }

        getIndexDir();
    }

    private Directory getIndexDir() {
        if (dir != null) {
            return dir;
        }

        try {
            dir = new SimpleFSDirectory(Paths.get(indexDir));
        }
        catch (IOException e) {
            throw new RuntimeException("Couldn't open index directory", e);
        }

        return dir;
    }

    private IndexSearcher getSearcher() {
        if (searcher != null) {
            return searcher;
        }

        try {
            Directory dir = getIndexDir();
            IndexReader indexReader = DirectoryReader.open(dir);
            searcher = new IndexSearcher(indexReader);

            return searcher;
        }
        catch (IOException e) {
            return null;
        }
    }
}
