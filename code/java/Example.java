// Use Java 17 for this example.
// Add the driver dependency to your pom.xml build.gradle etc.
// Java Driver Dependency:  http://search.maven.org/#artifactdetails|org.neo4j.driver|neo4j-java-driver|4.4.9|jar
// Reactive Streams http://search.maven.org/#artifactdetails|org.reactivestreams|reactive-streams|1.0.3|jar
// download jars into current directory
// java -cp "*" Example.java

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Query;
import org.neo4j.driver.SessionConfig;

import java.util.Map;
import java.util.stream.Collectors;

public class Example {
    public static void main(String[] args) {
        try (var driver = GraphDatabase.driver("neo4j://<HOST>:<BOLTPORT>", AuthTokens.basic("<USERNAME>", "<PASSWORD>"));
             var session = driver.session(SessionConfig.forDatabase("movies"))) {
            var query = new Query("""
                    MATCH (m:Movie {title:$movieTitle})<-[:ACTED_IN]-(a:Person) \
                    RETURN a.name as actorName\
                    """, Map.of("movieTitle", "The Matrix"));

            var result = session.readTransaction(tx -> tx.run(query).stream()
                    .map(record -> record.get("actorName").asString())
                    .collect(Collectors.toList()));

            result.forEach(System.out::println);
        }
    }
}