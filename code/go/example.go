// go mod init main
// go run example.go
package main

import (
	"context"
	"fmt"
	"github.com/neo4j/neo4j-go-driver/v5/neo4j"
	"reflect"
)

func main() {
	results, err := runQuery("neo4j+s://demo.neo4jlabs.com:7687", "movies", "mUser", "s3cr3t")
	if err != nil {
		panic(err)
	}
	for _, result := range results {
		fmt.Println(result)
	}
}

func runQuery(uri, database, username, password string) (_ []string, err error) {
	ctx := context.Background()
	driver, err := neo4j.NewDriverWithContext(uri, neo4j.BasicAuth(username, password, ""))
	if err != nil {
		return nil, err
	}
	defer func() { err = handleClose(ctx, driver, err) }()
	query := "MATCH (m:Movie {title:$movieTitle})<-[:ACTED_IN]-(a:Person) RETURN a.name as actorName"
	params := map[string]any{"movieTitle": "The Matrix"}
	result, err := neo4j.ExecuteQuery(ctx, driver, query, params,
		neo4j.EagerResultTransformer,
		neo4j.ExecuteQueryWithDatabase(database),
		neo4j.ExecuteQueryWithReadersRouting())
	if err != nil {
		return nil, err
	}
	actorNames := make([]string, len(result.Records))
	for i, record := range result.Records {
		// this assumes all actors have names, hence ignoring the 2nd returned value
		name, _, err := neo4j.GetRecordValue[string](record, "actorName")
		if err != nil {
			return nil, err
		}
		actorNames[i] = name
	}
	return actorNames, nil
}

func handleClose(ctx context.Context, closer interface{ Close(context.Context) error }, previousError error) error {
	err := closer.Close(ctx)
	if err == nil {
		return previousError
	}
	if previousError == nil {
		return err
	}
	return fmt.Errorf("%v closure error occurred:\n%s\ninitial error was:\n%w", reflect.TypeOf(closer), err.Error(), previousError)
}
