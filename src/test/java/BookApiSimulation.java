import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import javax.swing.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class BookApiSimulation extends Simulation {
    // Http configuration




    //Optional Bearer Token
    //private static final String BEARER_TOKEN = "";
    private HttpProtocolBuilder httpProcol = http
            .baseUrl("https://api.cloudia.ordina-jworks.io/api/cloudia")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");
            //.authorizationHeader("Bearer " + BEARER_TOKEN);


    // RUNTIME DATA
    private static final int USER_COUNT = Integer.parseInt(System.getProperty("USERS", "1000"));
    private static final int RAMP_DURATION = Integer.parseInt(System.getProperty("RAMP_DURATION", "900"));

    private static FeederBuilder.FileBased<Object> jsonFeeder = jsonFile("data/Books.json").random();
    @Override
    public void before() {
        System.out.printf("Running test with %d users%n", USER_COUNT);
        System.out.printf("Ramping users over %d seconds%n", RAMP_DURATION);
    }

    private static ChainBuilder getAllBooks =
            exec(http("Get method test")
                    .get(""));

    private static ChainBuilder createNewBook =
            feed(jsonFeeder)
                    .exec(http("Create New Book - #{title}")
                            .post("/books")
                            .body(ElFileBody("bodies/NewBook.json")).asJson());
    private static ChainBuilder getLastPostedBook =
            exec(http("Get Last Posted Book - #{title}")
                    .get("/books/#{id}").check(jmesPath("title").isEL("#{title}")));
    private static ChainBuilder deleteLastPostedBook =
            exec(http("Delete Book - #{title}")
                    .delete("/books/1"));



    private ScenarioBuilder scn = scenario("Book DB Testing")
            .exec(getAllBooks)
            .pause(1)
            .exec(createNewBook)
            .pause(1)
            .exec(getLastPostedBook)
            .pause(1)
            .exec(deleteLastPostedBook);



    //Load Simulation
    {
        setUp(
                scn.injectOpen(nothingFor(5),rampUsers(USER_COUNT).during(RAMP_DURATION)
                ).protocols(httpProcol));
    }
}
