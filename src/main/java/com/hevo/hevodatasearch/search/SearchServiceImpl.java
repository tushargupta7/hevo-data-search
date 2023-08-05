package com.hevo.hevodatasearch.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.hevo.hevodatasearch.constants.ErrorConstants;
import com.hevo.hevodatasearch.exception.SearchQueryException;
import com.hevo.hevodatasearch.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class SearchServiceImpl implements SearchService {

    @Value("${elasticsearch.index}")
    private String ES_INDEX;
    final private ElasticsearchClient esClient;
    private final Logger logger;

    @Autowired
    public SearchServiceImpl(ElasticsearchClient esClient) {
        this.esClient = esClient;
        this.logger = LoggerFactory.getLogger(this.getClass().getName());
    }

    @Override
    public List<String> findPathOfMatchingFiles(String query) {
        SearchResponse<Document> response;
        try {
            response = getDocumentSearchResponse("content",query);
        } catch (IOException e) {
            throw new SearchQueryException(String.format(ErrorConstants.EXCEPTION_FORMAT,
                    ErrorConstants.QUERY_ERROR_MESSAGE, e.getMessage()));
        }

        return getDocumentPaths(response);
    }

    private SearchResponse<Document> getDocumentSearchResponse(String field,String value) throws IOException {
        SearchResponse<Document> response;
        response = esClient.search(s -> s
                        .index(ES_INDEX)
                        .query(q -> q
                                .match(t -> t
                                        .field(field)
                                        .query(value)
                                )
                        ),
                Document.class
        );
        return response;
    }

    @Override
    public List<String> findPathsOfFileFromFolderPath(String folderPath) {

        SearchResponse<Document> response;
        try {
            response = getDocumentSearchResponse("path", "/%s".formatted(folderPath));
        } catch (IOException e) {
            throw new SearchQueryException(String.format(ErrorConstants.EXCEPTION_FORMAT,
                    ErrorConstants.QUERY_ERROR_MESSAGE, e.getMessage()));
        }
        // Process the search hits and extract the file paths
        return getDocumentPaths(response);


    }

    private List<String> getDocumentPaths(SearchResponse<Document> response) {
        List<Hit<Document>> hits = response.hits().hits();
        List<String> result = new ArrayList<>();
        for (Hit<Document> hit : hits) {
            Document document = hit.source();
            result.add(Objects.requireNonNull(document).getPath());
        }
        logger.info("List of Files%s".formatted(result));
        return result;
    }
}
