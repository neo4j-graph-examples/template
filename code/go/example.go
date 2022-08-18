// go mod init my-module-path
// go run example.go
package main

import (
	"fmt"
	"github.com/neo4j/neo4j-go-driver/v4/neo4j"
)

func main() {
	credentials := neo4j.BasicAuth("mUser", "s3cr3t", "")
	driver, err := neo4j.NewDriver("neo4j+s://demo.neo4jlabs.com:7687", credentials)
	if err != nil {
		panic("Could not create driver")
	}
	defer driver.Close()
	results, err := runQuery(driver, getDairyProducts)
	if err != nil {
		panic(err)
	}
	fmt.Println(results)
}

func runQuery(driver neo4j.Driver, txFunc neo4j.TransactionWork) (interface{}, error) {
	session := driver.NewSession(neo4j.SessionConfig{DatabaseName: "movies"})
	defer session.Close()
	return session.ReadTransaction(txFunc)
}

func getDairyProducts(transaction neo4j.Transaction) (interface{}, error) {
	cursor, err := transaction.Run(
		`MATCH (m:Movie {title:$movieTitle})<-[:ACTED_IN]-(a:Person) 
		RETURN a.name as actorName`,
		map[string]interface{}{"movieTitle": "The Matrix"})
	if err != nil {
		return nil, err
	}
	var results []string
	for cursor.Next() {
		value, found := cursor.Record().Get("actorName")
		if found {
			results = append(results, value.(string))
		}
	}
	if err = cursor.Err(); err != nil {
		return nil, err
	}
	return results, nil
}