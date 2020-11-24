package com.neo4j.sandbox.updater;

import com.neo4j.sandbox.git.FakeNorthwindGit;
import com.neo4j.sandbox.github.GithubSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CodeUpdaterIT {

    private Path cloneLocation;

    private Updater updater;

    @BeforeEach
    void prepare(@TempDir Path tempDir) throws URISyntaxException {
        cloneLocation = tempDir.resolve("northwind");
        updater = new Updater(
                new FakeNorthwindGit(cloneLocation),
                new MetadataReader(),
                settings(testResourcesPath("/fake-template-repo"))
        );
    }

    @Test
    void updates_Python_example() throws Exception {
        updater.updateCodeExamples(cloneLocation, "https://github.com/neo4j-graph-examples/northwind");

        Path pythonExample = cloneLocation.resolve("code").resolve("python").resolve("example.py");
        assertThat(String.join("\n", Files.readAllLines(pythonExample))).isEqualTo(
                "# pip3 install neo4j-driver\n" +
                        "# python3 example.py\n" +
                        "\n" +
                        "from neo4j import GraphDatabase, basic_auth\n" +
                        "\n" +
                        "driver = GraphDatabase.driver(\n" +
                        "  \"neo4j+s://demo.neo4jlabs.com:7687\",\n" +
                        "  auth=basic_auth(\"northwind\", \"northwind\"))\n" +
                        "\n" +
                        "cypher_query = '''\n" +
                        "MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]->\n" +
                        "(:Category {categoryName:$category})\n" +
                        "RETURN p.productName as product\n" +
                        "'''\n" +
                        "\n" +
                        "with driver.session(database=\"northwind\") as session:\n" +
                        "  results = session.read_transaction(\n" +
                        "    lambda tx: tx.run(cypher_query,\n" +
                        "      category=\"Dairy Products\").data())\n" +
                        "\n" +
                        "  for record in results:\n" +
                        "    print(record['product'])\n" +
                        "\n" +
                        "driver.close()"
        );
    }

    @Test
    void updates_Java_example() throws Exception {
        updater.updateCodeExamples(cloneLocation, "https://github.com/neo4j-graph-examples/northwind");

        Path javaExample = cloneLocation.resolve("code").resolve("java").resolve("Example.java");
        assertThat(String.join("\n", Files.readAllLines(javaExample))).isEqualTo(
                "// Add your the driver dependency to your pom.xml build.gradle etc.\n" +
                        "// Java Driver Dependency: http://search.maven.org/#artifactdetails|org.neo4j.driver|neo4j-java-driver|4.0.1|jar\n" +
                        "// Reactive Streams http://search.maven.org/#artifactdetails|org.reactivestreams|reactive-streams|1.0.3|jar\n" +
                        "// download jars into current directory\n" +
                        "// java -cp \"*\" Example.java\n" +
                        "\n" +
                        "import org.neo4j.driver.*;\n" +
                        "import static org.neo4j.driver.Values.parameters;\n" +
                        "\n" +
                        "public class Example {\n" +
                        "\n" +
                        "  public static void main(String...args) {\n" +
                        "\n" +
                        "    Driver driver = GraphDatabase.driver(\"neo4j+s://demo.neo4jlabs.com:7687\",\n" +
                        "              AuthTokens.basic(\"northwind\",\"northwind\"));\n" +
                        "\n" +
                        "    try (Session session = driver.session(SessionConfig.forDatabase(\"northwind\"))) {\n" +
                        "\n" +
                        "      String cypherQuery =\n" +
                        "        \"MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]->\" +\n" +
                        "        \"(:Category {categoryName:$category})\" +\n" +
                        "        \"RETURN p.productName as product\";\n" +
                        "\n" +
                        "      var result = session.readTransaction(\n" +
                        "        tx -> tx.run(cypherQuery, \n" +
                        "                parameters(\"category\",\"Dairy Products\"))\n" +
                        "            .list());\n" +
                        "\n" +
                        "      for (Record record : result) {\n" +
                        "        System.out.println(record.get(\"product\").asString());\n" +
                        "      }\n" +
                        "    }\n" +
                        "    driver.close();\n" +
                        "  }\n" +
                        "}\n" +
                        "\n"
        );
    }

    @Test
    void updates_Csharp_example() throws Exception {
        updater.updateCodeExamples(cloneLocation, "https://github.com/neo4j-graph-examples/northwind");

        Path javaExample = cloneLocation.resolve("code").resolve("csharp").resolve("Example.cs");
        assertThat(String.join("\n", Files.readAllLines(javaExample))).isEqualTo(
                "// install dotnet core on your system\n" +
                        "// dotnet new console -o .\n" +
                        "// dotnet add package Neo4j.Driver\n" +
                        "// paste in this code into Program.cs\n" +
                        "// dotnet run\n" +
                        "\n" +
                        "using System;\n" +
                        "using System.Collections.Generic;\n" +
                        "using System.Text;\n" +
                        "using System.Threading.Tasks;\n" +
                        "using Neo4j.Driver;\n" +
                        "  \n" +
                        "namespace dotnet {\n" +
                        "  class Example {\n" +
                        "  static async Task Main() {\n" +
                        "    var driver = GraphDatabase.Driver(\"neo4j+s://demo.neo4jlabs.com:7687\", \n" +
                        "                    AuthTokens.Basic(\"northwind\", \"northwind\"));\n" +
                        "\n" +
                        "    var cypherQuery =\n" +
                        "      @\"\n" +
                        "      MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]->\n" +
                        "      (:Category {categoryName:$category})\n" +
                        "      RETURN p.productName as product\n" +
                        "      \";\n" +
                        "\n" +
                        "    var session = driver.AsyncSession(o => o.WithDatabase(\"northwind\"));\n" +
                        "    var result = await session.ReadTransactionAsync(async tx => {\n" +
                        "      var r = await tx.RunAsync(cypherQuery, \n" +
                        "              new { category=\"Dairy Products\"});\n" +
                        "      return await r.ToListAsync();\n" +
                        "    });\n" +
                        "\n" +
                        "    await session?.CloseAsync();\n" +
                        "    foreach (var row in result)\n" +
                        "      Console.WriteLine(row[\"product\"].As<string>());\n" +
                        "\t  \n" +
                        "    }\n" +
                        "  }\n" +
                        "}"
        );
    }

    private GithubSettings settings(Path testResourcesPath) {
        GithubSettings settings = new GithubSettings();
        settings.setWorkspace(testResourcesPath);
        return settings;
    }

    private Path testResourcesPath(String resourceName) throws URISyntaxException {
        URL resource = this.getClass().getResource(resourceName);
        return new File(resource.toURI()).toPath();
    }

}