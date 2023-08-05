package com.hevo.hevodatasearch.cloudStorage;

import com.dropbox.core.android.DropboxParseException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderBuilder;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.hevo.hevodatasearch.cloudStorage.decoder.*;
import com.hevo.hevodatasearch.constants.ErrorConstants;
import com.hevo.hevodatasearch.exception.CloudStorageException;
import com.hevo.hevodatasearch.model.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
class DropboxApi implements CloudStorageApi {

    private final DbxClientV2 client;

    @Autowired
    public DropboxApi(DbxClientV2 client) {
        this.client = client;
    }

    public String downloadFile(String filePath) {
        InputStream inputStream = handleDropboxAction(() -> client.files().download(filePath).getInputStream(),
                String.format("Error downloading file: %s", filePath));
        FileContentGenerator fileContentGenerator = getFileContentGenerator(filePath);
        return fileContentGenerator.generateContent(inputStream);
    }

    private FileContentGenerator getFileContentGenerator(String filePath) {
        FileContentDecoderStrategy contentDecoderStrategy = new TextFileContentDecode();
        if (filePath.endsWith(".txt")) {
            contentDecoderStrategy = new TextFileContentDecode();
        } else if (filePath.endsWith(".pdf")) {
            contentDecoderStrategy = new PdfFileContentDecoder();
        } else if (filePath.endsWith(".doc")) {
            contentDecoderStrategy = new DocFileContentDecoder();
        } else if (filePath.endsWith(".docx")) {
            contentDecoderStrategy = new DocxFileContentDecoder();
        }
        return new FileContentGenerator(contentDecoderStrategy);
    }

    // Method to download files in parallel and create Document objects
    public List<Document> fetchFilesInFolderPath(String folderPath) {
        List<String> filepaths = fetchListOfFiles(folderPath);
        List<Document> documents = new ArrayList<>();
        int numThreads = Runtime.getRuntime().availableProcessors(); // You can adjust the number of threads as needed.

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        List<Future<Document>> futures = new ArrayList<>();

        for (String filePath : filepaths) {
            // Check if the file has the required tag (e.g., "file")

            Callable<Document> task = () -> {
                String content = downloadFile(filePath);
                return Document.builder().path(filePath).content(content).build();
            };
            Future<Document> future = executor.submit(task);
            futures.add(future);

        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (Future<Document> future : futures) {
            try {
                documents.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new CloudStorageException("Error fetching files", e);
            }
        }

        return documents;
    }

    public List<String> fetchListOfFiles(String folderPath) {
        ListFolderBuilder listFolderBuilder = client.files().listFolderBuilder("/" + folderPath);
        listFolderBuilder.withRecursive(true);

        ListFolderResult folderResult = handleDropboxAction(listFolderBuilder::start, String.format("Error listing folder: %s", folderPath));
        return folderResult.getEntries().stream()
                .filter(entity -> entity instanceof FileMetadata)
                .map(entity -> (FileMetadata) entity)
                .map(entity -> entity.getPathDisplay())
                .collect(Collectors.toList());
    }

    public void deleteFile(String filePath) {
        handleDropboxAction(() -> client.files().deleteV2(filePath), String.format("Error deleting file: %s", filePath));
    }

    public void deleteFolder(String folderPath) {
        handleDropboxAction(() -> client.files().deleteV2(folderPath), String.format("Error deleting folder: %s", folderPath));
    }

    private <T> T handleDropboxAction(DropboxActionResolver<T> action, String exceptionMessage) {
        try {
            return action.perform();
        } catch (Exception e) {
            String messageWithCause = String.format(ErrorConstants.EXCEPTION_FORMAT, exceptionMessage, e.getMessage());
            throw new CloudStorageException(messageWithCause, e);
        }
    }

}
