package com.hevo.hevodatasearch.ingestor;

import com.hevo.hevodatasearch.model.Document;

import java.util.List;

public interface IngestorService {
    void ingestDocuments(List<Document> documentList);
    void cleanUpDeletedFilesAsync(List<Document> documentList, List<String> allIngestedFilesOfFolder);
}
