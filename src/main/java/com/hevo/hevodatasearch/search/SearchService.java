package com.hevo.hevodatasearch.search;

import java.util.List;

public interface SearchService {
    List<String> findPathOfMatchingFiles(String query);
    List<String> findPathsOfFileFromFolderPath(String folderPath);
}
