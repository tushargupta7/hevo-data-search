package com.hevo.hevodatasearch.ingestor;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import com.hevo.hevodatasearch.constants.ErrorConstants;
import com.hevo.hevodatasearch.exception.IngestionException;
import com.hevo.hevodatasearch.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
public class IngestorServiceImpl implements IngestorService {
    private final ElasticsearchClient esClient;
    private final Logger logger;
    @Value("${elasticsearch.index}")
    private String ES_INDEX;

    @Autowired
    public IngestorServiceImpl(ElasticsearchClient esClient) {
        this.esClient = esClient;
        this.logger = LoggerFactory.getLogger(this.getClass().getName());
    }

    @Override
    public void ingestDocuments(List<Document> documentList) {
        documentList.forEach(this::deleteAndIngest);
    }

    @Override
    public void cleanUpDeletedFilesAsync(List<Document> documentList, List<String> allIngestedFilesOfFolder) {
        Executor executor = Executors.newCachedThreadPool();
        CompletableFuture.runAsync(() -> cleanUpDeletedFiles(documentList, allIngestedFilesOfFolder), executor);
    }

    public void cleanUpDeletedFiles(List<Document> documentList, List<String> allIngestedFilesOfFolder) {
        Set<String> documentPathsSet = new HashSet<>();
        for (Document document : documentList) {
            documentPathsSet.add(document.getPath());
        }

        // Step 2: Filter the allIngestedFilesOfFolder list to keep only the paths not present in documentPathsSet
        allIngestedFilesOfFolder.removeIf(documentPathsSet::contains);

        // Step 3: Delete the documents with the filtered path list using the esClient
        for (String pathToDelete : allIngestedFilesOfFolder) {
            // Check if the document exists in Elasticsearch before attempting to delete
            try {
                SearchResponse<Document> response;
                response = esClient.search(s -> s
                                .index(ES_INDEX)
                                .query(q -> q
                                        .match(t -> t
                                                .field("path")
                                                .query(pathToDelete)
                                        )
                                ),
                        Document.class
                );
                if (Objects.requireNonNull(response.hits().total()).value()>0) {
                   DeleteResponse deleteResponse =  esClient.delete(s-> s
                            .index(ES_INDEX)
                            .id(getDocumentIdFromPath(pathToDelete)));
                   logger.info("Delete response : %s".formatted(deleteResponse));
                }
            } catch (IOException e) {
                // Handle exception
            }
        }

    }

    private void deleteAndIngest(Document document) {
        deleteDocumentIfExist(document);
        doIngestion(document);
    }

    private void deleteDocumentIfExist(Document document) {
        DeleteRequest deleteRequest = DeleteRequest.of(d -> d.index(ES_INDEX).id(document.getPath()));
        try {
            DeleteResponse deleteResponse = esClient.delete(deleteRequest);
            logger.info("Delete response :" + deleteResponse);
        } catch (IOException e) {
            String messageWithCause = String.format(ErrorConstants.EXCEPTION_FORMAT,
                    ErrorConstants.DELETION_DOC_FAILED, e.getMessage());
            throw new IngestionException(messageWithCause, e);
        }
    }

    private void doIngestion(Document document) {
        try {
            IndexResponse response = esClient.index(i -> i
                    .index(ES_INDEX)
                    .id(getDocumentIdFromPath(document.getPath()))
                    .document(document)
            );
            logger.info(String.format("Ingestion resposnse for filePath %s : %s", document.getPath(), response.toString()));
        } catch (IOException e) {
            String messageWithCause = String.format(ErrorConstants.EXCEPTION_FORMAT,
                    ErrorConstants.INGESTION_ERROR_MESSAGE, e.getMessage());
            throw new IngestionException(messageWithCause, e);
        }
    }

    private String getDocumentIdFromPath(String path) {
        return path.toLowerCase().
                replace("/", "_").
                replace(" ", "");
    }


}
