# (requires Python 3.6 or newer and pip, check with `pip --version`)
# pip install neo4j
# python example.py

from neo4j import GraphDatabase

def products_of_category(transaction, category_):
    cypher_query = (
        "MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]->"
        "(:Category {categoryName:$category})\n"
        "RETURN p.productName as product")
    result = transaction.run(cypher_query, category=category_)
    return result.value("product")

uri = "neo4j://<HOST>:<BOLTPORT>"
auth = ("<USERNAME>", "<PASSWORD>")
driver = GraphDatabase.driver(uri, auth=auth)

with driver.session(database="neo4j") as session:
    categories = session.read_transaction(products_of_category,
                                          "Dairy Products")
    for category in categories:
        print(category)

driver.close()