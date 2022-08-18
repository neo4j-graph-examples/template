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
             var session = driver.session(SessionConfig.forDatabase("neo4j"))) {
            var query = new Query("""
                    MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]->(:Category {categoryName:$category}) \
                    RETURN p.productName as product\
                    """, Map.of("category", "Dairy Products"));

            var products = session.readTransaction(tx -> tx.run(query).stream()
                    .map(record -> record.get("product").asString())
                    .collect(Collectors.toList()));

            products.forEach(System.out::println);
        }
    }
}