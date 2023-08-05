package com.hevo.hevodatasearch;

import com.hevo.hevodatasearch.cloudStorage.CloudStorageAPIFactory;
import com.hevo.hevodatasearch.cloudStorage.CloudStorageApi;
import com.hevo.hevodatasearch.dto.IngestResponseDto;
import com.hevo.hevodatasearch.ingestor.IngestorService;
import com.hevo.hevodatasearch.model.Document;
import com.hevo.hevodatasearch.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doc")
public class AppController {

    private final SearchService searchService;
    private final IngestorService ingestorService;
    private final CloudStorageAPIFactory cloudStorageAPIFactory;

    @Autowired
    public AppController(SearchService searchService, IngestorService ingestorService, CloudStorageAPIFactory cloudStorageAPIFactory) {
        this.searchService = searchService;
        this.ingestorService = ingestorService;
        this.cloudStorageAPIFactory = cloudStorageAPIFactory;
    }

    @PostMapping("/ingest")
    public ResponseEntity<IngestResponseDto> downloadAndIngestCloudFolderContent(@RequestParam("folder_path") String folderPath){
        CloudStorageApi cloudStorageApi = cloudStorageAPIFactory.createCloudStorageAPI("dropbox");
        List<Document> documentList = cloudStorageApi.fetchFilesInFolderPath(folderPath);
        List<String> allIngestedFilesOfFolder = searchService.findPathsOfFileFromFolderPath(folderPath);
        ingestorService.cleanUpDeletedFilesAsync(documentList,allIngestedFilesOfFolder);
        ingestorService.ingestDocuments(documentList);
        return ResponseEntity.ok(IngestResponseDto.builder()
                .pathList(documentList.stream()
                        .map(Document::getPath)
                        .collect(Collectors.toList()))
                .message("Successful Ingestion").build());
    }


    @GetMapping("/search")
    public ResponseEntity<List<String>> searchDocumentsForWord(@RequestParam String query) {
        List<String> docPaths =  searchService.findPathOfMatchingFiles(query);
        return ResponseEntity.ok(docPaths);
    }
}
