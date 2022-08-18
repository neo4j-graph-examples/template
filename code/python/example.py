# (requires Python 3.6 or newer and pip, check with `pip --version`)
# pip install neo4j
# python example.py

from neo4j import GraphDatabase

def products_of_category(transaction, movie_title_):
    cypher_query = (
        "MATCH (m:Movie {title:$movieTitle})<-[:ACTED_IN]-(a:Person)" 
        "RETURN a.name as actorName")
    result = transaction.run(cypher_query, movie_title=movie_title_)
    return result.value("actorName")

uri = "neo4j+s://demo.neo4jlabs.com:7687"
auth = ("mUser", "s3cr3t")
driver = GraphDatabase.driver(uri, auth=auth)

with driver.session(database="movies") as session:
    categories = session.read_transaction(movies,
                                          "The Matrix")
    for category in categories:
        print(category)

driver.close()