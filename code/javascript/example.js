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
  const session = driver.session({ database: "neo4j" });

  try {
    return await session.readTransaction(async tx => {
      const query = `
        MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]->
        (:Category {categoryName:$category})
        RETURN p.productName as product
        `;

      const params = { category: "Dairy Products" };

      const result = await tx.run(query, params);

      return result.records.map(record => record.get('product'))
    });
  } finally {
    await session.close();
  }
}

main()
  .then(() => console.log("Finished"))
  .catch(error => console.error(error))