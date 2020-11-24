package com.neo4j.sandbox.updater;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetadataReader {

    public Metadata readMetadata(Reader path) throws IOException {
        Metadata.Builder result = Metadata.builder();
        List<String> lines = readContents(path);
        for (Iterator<String> iterator = lines.iterator(); iterator.hasNext(); ) {
            String line = iterator.next();
            if (line.startsWith(":query:")) {
                StringBuilder queryBuilder = new StringBuilder();
                while (line.trim().endsWith("+")) {
                    queryBuilder.append(processQueryLine(line));
                    queryBuilder.append("\n");
                    if (!iterator.hasNext()) {
                        break;
                    }
                    line = iterator.next();
                }
                String query = queryBuilder.toString().trim();
                result.setQuery(query);
            }
            if (line.startsWith(":param-name:")) {
                result.setParameterName(line.substring(":param-name:".length()).trim());
            } else if (line.startsWith(":param-value:")) {
                result.setParameterValue(line.substring(":param-value:".length()).trim());
            } else if (line.startsWith(":result-column:")) {
                result.setResultColumn(line.substring(":result-column:".length()).trim());
            } else if (line.startsWith(":expected-result:")) {
                result.setExpectedResult(line.substring(":expected-result:".length()).trim());
            }
        }
        return result.build();
    }

    private List<String> readContents(Reader path) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(path)) {
            return bufferedReader.lines().collect(Collectors.toList());
        }
    }

    private String processQueryLine(String line) {
        String strippedLine = line.replace(":query: ", "").trim();
        return strippedLine.substring(0, strippedLine.length() - "+".length()).trim();
    }
}
