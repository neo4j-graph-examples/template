// npm install --save neo4j-driver
// node example.js
const neo4j = require("neo4j-driver");

async function main() {
  const driver = neo4j.driver("neo4j://<HOST>:<BOLTPORT>", neo4j.auth.basic("<USERNAME>", "<PASSWORD>"));

  try {
    const products = await getDairyProducts(driver)
    products.forEach(product => console.log(product))
  } catch (error) {
    console.error(error);
  } finally {
    await driver.close()
  }
}

async function getDairyProducts(driver) {
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