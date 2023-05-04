package main

import (
	"context"
	"fmt"
	"github.com/neo4j/neo4j-go-driver/v5/neo4j"
)

func main() {
	ctx := context.Background()
	// Aura requires you to use "neo4j+s" scheme, so that your queries are processed using an encrypted connection
	// (You may need to replace your connection details, username and password)
	uri := "neo4j+s://<Bolt url for Neo4j Aura database>"
	auth := neo4j.BasicAuth("<Username for Neo4j Aura database>", "<Password for Neo4j Aura database>", "")
	driver, err := neo4j.NewDriverWithContext(uri, auth)
	if err != nil {
		panic(err)
	}
	// Don't forget to close the driver connection when you are finished with it
	defer closeResource(ctx, driver)

	// To learn more about the Cypher syntax, see https://neo4j.com/docs/cypher-manual/current/
	// The Reference Card is also a good resource for keywords https://neo4j.com/docs/cypher-refcard/current/
	createRelationshipBetweenPeopleQuery := `
		MERGE (p1:Person { name: $person1_name })
		MERGE (p2:Person { name: $person2_name })
		MERGE (p1)-[:KNOWS]->(p2)
		RETURN p1, p2`
	params := map[string]any{
		"person1_name": "Alice",
		"person2_name": "David",
	}

	// Using ExecuteQuery allows the driver to handle retries and transient errors for you
	result, err := neo4j.ExecuteQuery(ctx, driver, createRelationshipBetweenPeopleQuery, params,
		neo4j.EagerResultTransformer)
	if err != nil {
		panic(err)
	}
	for _, record := range result.Records {
		fmt.Printf("First: '%s'\n", getPersonName(record, "p1"))
		fmt.Printf("Second: '%s'\n", getPersonName(record, "p2"))
	}

	readPersonByName := `
		MATCH (p:Person)
		WHERE p.name = $person_name
		RETURN p.name AS name`
	result, err = neo4j.ExecuteQuery(ctx, driver, readPersonByName, map[string]any{"person_name": "Alice"},
		neo4j.EagerResultTransformer)
	if err != nil {
		panic(err)
	}
	for _, record := range result.Records {
		name, _, err := neo4j.GetRecordValue[string](record, "name")
		if err != nil {
			panic(err)
		}
		fmt.Printf("Person name: '%s' \n", name)
	}
}

func closeResource(ctx context.Context, closer interface{ Close(context.Context) error }) {
	if err := closer.Close(ctx); err != nil {
		panic(err)
	}
}

func getPersonName(record *neo4j.Record, key string) string {
	firstPerson, _, err := neo4j.GetRecordValue[neo4j.Node](record, key)
	if err != nil {
		panic(err)
	}
	firstPersonName, err := neo4j.GetProperty[string](firstPerson, "name")
	if err != nil {
		panic(err)
	}
	return firstPersonName
}
