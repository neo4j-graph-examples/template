// install dotnet on your system
// https://dotnet.microsoft.com/en-us/download/dotnet
// dotnet new console -o .
// dotnet add package Neo4j.Driver
// paste in this code into Program.cs
// dotnet run

using Neo4j.Driver;
using Neo4j.Driver.Mapping;

await using var driver = GraphDatabase.Driver(
    "neo4j+s://demo.neo4jlabs.com:7687",
    AuthTokens.Basic("mUser", "s3cr3t"));

var actors = await driver
    .ExecutableQuery(@"
        MATCH (m:Movie {title:$movieTitle})<-[:ACTED_IN]-(a:Person)
        RETURN a.name AS name, a.born AS born")
    .WithParameters(new { movieTitle = "The Matrix" })
    .WithConfig(new QueryConfig(database: "movies"))
    .ExecuteAsync()
    .AsObjectsAsync<Actor>();

foreach (var actor in actors)
    Console.WriteLine(actor);

record Actor(string name, int born);
