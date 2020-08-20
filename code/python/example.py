# pip install neo4j

from neo4j import GraphDatabase, basic_auth

driver = GraphDatabase.driver(
  "neo4j://<HOST>:7687",
  auth=basic_auth("<USERNAME>", "<PASSWORD>"))

def add_person(tx, name):
  cypher_query = (
    "CREATE (p:Person {name: $name}) ",
    "RETURN p AS node",
  )
  result = tx.run(cypher_query, name=name)
  record = result.single()

  if "node" in record.keys():
    person = record["node"]
    return person["name"]


def get_persons(tx, name):
  cypher_query = (
    "MATCH (p:Person) ",
    "WHERE p.name == $name "
    "RETURN ID(p) AS person",
  )
  result = tx.run(cypher_query, name=name)

  persons = []

  for record in result:
    persons.append(record["person"])

  return persons

with driver.session() as session:
  # Create a person node with name example
  name = session.write_transaction(add_person, "example")
  print("Person {} was created.".format(name))

with driver.session() as session:
  # Get person node IDs that are have the name example
  persons = session.read_transaction(get_persons, "example")
  for person in persons:
    print(person)

driver.close()
