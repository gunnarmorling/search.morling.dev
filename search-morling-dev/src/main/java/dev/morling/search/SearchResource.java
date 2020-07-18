/*
 * Copyright Gunnar Morling
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package dev.morling.search;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
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
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.StartupEvent;

@Path("/")
public class SearchResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchResource.class);

    @Inject
    Directory dir;

    private IndexSearcher searcher;

    private IndexReader indexReader;

    private AtomicBoolean firstCall = new AtomicBoolean(true);

    public void setupSearcher(@Observes StartupEvent se) {
        try {
            indexReader = DirectoryReader.open(dir);
            searcher = new IndexSearcher(indexReader);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/ping")
    public String ping() {
        return "{ \"isFirstCall\" : " + firstCall.getAndSet(false) + "}";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/search")
    public Response search(@QueryParam("q") String queryString) {
        return query(queryString);
    }

    private Response query(String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(Json.createObjectBuilder()
                            .add("message", "A query must be specified using the 'q' query parameter")
                            .build()
                            )
                    .build();
        }

        LOGGER.info("Executing query: {}", queryString);

        try {
            Analyzer analyzer = new StandardAnalyzer();

            MultiFieldQueryParser parser = new MultiFieldQueryParser(
                    new String[]{"title", "content"},
                    analyzer);
            parser.setDefaultOperator(Operator.AND);
            Query query = parser.parse(queryString);

            TopDocs topDocs = searcher.search(query, 10);

            JsonObjectBuilder result = Json.createObjectBuilder()
                    .add("message", "ok");

            JsonArrayBuilder results = Json.createArrayBuilder();

            FastVectorHighlighter fvh = new FastVectorHighlighter();
            SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter();
            Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(query));

            for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                String title = getBestFragmentFromFvh(query, "title", topDocs.scoreDocs[i].doc, fvh);
                if (title == null) {
                    title = getBestFragmentFromSimpleHighlighter("title", topDocs.scoreDocs[i].doc, highlighter, analyzer);
                }
                if (title == null) {
                    title = searcher.doc(topDocs.scoreDocs[i].doc).get("title");
                }

                String fragment = getBestFragmentFromFvh(query, "content", topDocs.scoreDocs[i].doc, fvh);
                if (fragment == null) {
                    fragment = getBestFragmentFromSimpleHighlighter("content", topDocs.scoreDocs[i].doc, highlighter, analyzer);
                }
                if (fragment != null) {
                    fragment = "..." + fragment;
                    if (!fragment.endsWith(".")) {
                        fragment = fragment + "...";
                    }
                    fragment = fragment.replaceAll(":\n\\s+", ". ");
                    fragment = fragment.replaceAll(".\n\\s+", ". ");
                }
                else {
                    fragment = "";
                }

                results.add(Json.createObjectBuilder()
                        .add("publicationdate", searcher.doc(topDocs.scoreDocs[i].doc).get("publicationdate"))
                        .add("uri", searcher.doc(topDocs.scoreDocs[i].doc).get("uri"))
                        .add("title", title)
                        .add("fragment", fragment)
                );
            }

            result.add("results", results);

            return Response.ok(result.build()).build();
        }
        catch (IOException | ParseException e) {
            throw new RuntimeException("Couldn't execute query", e);
        }
    }

    private String getBestFragmentFromSimpleHighlighter(String fieldName, int docId, Highlighter highlighter, Analyzer analyzer) {
        try {
            TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), docId, fieldName, analyzer);
            TextFragment[] frag = highlighter.getBestTextFragments(tokenStream, searcher.doc(docId).get(fieldName), false, 1);
            if (frag.length == 1) {
                return frag[0].toString();
            }
        }
        catch (IOException | InvalidTokenOffsetsException e) {
            throw new RuntimeException("Couldn't highlight search result", e);
        }

        return null;
    }

    private String getBestFragmentFromFvh(Query query, String fieldName, int docId, FastVectorHighlighter fvh) {
        try {
            return fvh.getBestFragment(fvh.getFieldQuery(query), indexReader, docId, fieldName, 300);
        }
        catch (IOException e) {
            throw new RuntimeException("Couldn't highlight search result", e);
        }
    }
}
