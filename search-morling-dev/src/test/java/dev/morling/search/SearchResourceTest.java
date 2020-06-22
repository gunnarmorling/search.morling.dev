package dev.morling.search;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class SearchResourceTest {

    @Test
    public void testSearchEndpoint() {
        given()
          .when().get("/search?q=bean")
          .then()
             .statusCode(200)
             .body(containsString("Enforcing Java Record Invariants With Bean Validation"))
             .body(containsString("The Emitter Parameter Pattern for Flexible SPI Contracts"));
    }
}
