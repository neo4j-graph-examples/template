// npm install --save neo4j-driver
// node example.js
const neo4j = require("neo4j-driver");

async function main() {
  const driver = neo4j.driver("neo4j+s://demo.neo4jlabs.com:7687", neo4j.auth.basic("mUser", "s3cr3t"));

  try {
    const products = await getMovies(driver)
    products.forEach(console.log)
  } catch (error) {
    console.error(error);
  } finally {
    await driver.close()
  }
}

async function getMovies(driver) {
  const session = driver.session({ database: "movies" });

  try {
    return await session.readTransaction(async tx => {
      const query = `
        MATCH (m:Movie {title:$movieTitle})<-[:ACTED_IN]-(a:Person) 
        RETURN a.name as actorName
        `;

      const params = { movieTitle: "The Matrix" };

      const result = await tx.run(query, params);

      return result.records.map(record => record.get('actorName'))
    });
  } finally {
    await session.close();
  }
}

main()
  .then(() => console.log("Finished"))
  .catch(error => console.error(error))