// go mod init my-module-path
// go run example.go
package main

import (
	"fmt"
	"github.com/neo4j/neo4j-go-driver/v4/neo4j"
)

func main() {
	credentials := neo4j.BasicAuth("<USERNAME>", "<PASSWORD>", "")
	driver, err := neo4j.NewDriver("neo4j://<HOST>:<BOLTPORT>", credentials)
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
	session := driver.NewSession(neo4j.SessionConfig{DatabaseName: "neo4j"})
	defer session.Close()
	return session.ReadTransaction(txFunc)
}

func getDairyProducts(transaction neo4j.Transaction) (interface{}, error) {
	cursor, err := transaction.Run(
		`MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]->
		(:Category {categoryName:$category})
		RETURN p.productName as product`,
		map[string]interface{}{"category": "Dairy Products"})
	if err != nil {
		return nil, err
	}
	var results []string
	for cursor.Next() {
		value, found := cursor.Record().Get("product")
		if found {
			results = append(results, value.(string))
		}
	}
	if err = cursor.Err(); err != nil {
		return nil, err
	}
	return results, nil
}