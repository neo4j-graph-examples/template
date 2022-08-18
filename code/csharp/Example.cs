// install dotnet core on your system
// dotnet new console .
// dotnet add package Neo4j.Driver
// paste in this code into Program.cs
// dotnet run

using Neo4j.Driver;

using var driver = GraphDatabase.Driver("neo4j://<HOST>:<BOLTPORT>", AuthTokens.Basic("<USERNAME>", "<PASSWORD>"));

var cypherQuery = @"
MATCH (m:Movie {title:$movieTitle})<-[:ACTED_IN]-(a:Person) 
RETURN a.name as actorName
";

await using var session = driver.AsyncSession(o => o.WithDatabase("movies"));
var result = await session.ReadTransactionAsync(async tx =>
{
    var r = await tx.RunAsync(cypherQuery, new { movieTitle="The Matrix" });
    return await r.ToListAsync();
});

foreach (var row in result)
    Console.WriteLine(row["actorName"].As<string>());