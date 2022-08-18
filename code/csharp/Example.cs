// install dotnet core on your system
// dotnet new console .
// dotnet add package Neo4j.Driver
// paste in this code into Program.cs
// dotnet run

using Neo4j.Driver;

using var driver = GraphDatabase.Driver("neo4j://<HOST>:<BOLTPORT>", AuthTokens.Basic("<USERNAME>", "<PASSWORD>"));

var cypherQuery = @"
MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]->(:Category {categoryName:$category})
RETURN p.productName as product
";

await using var session = driver.AsyncSession(o => o.WithDatabase("neo4j"));
var result = await session.ReadTransactionAsync(async tx =>
{
    var r = await tx.RunAsync(cypherQuery, new { category = "Dairy Products" });
    return await r.ToListAsync();
});

foreach (var row in result)
    Console.WriteLine(row["product"].As<string>());