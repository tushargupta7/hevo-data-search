package com.hevo.hevodatasearch.cloudStorage;

import com.hevo.hevodatasearch.model.Document;

import java.util.List;

public interface CloudStorageApi {
    List<Document> fetchFilesInFolderPath(String folderPath);

}
