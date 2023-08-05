package com.hevo.hevodatasearch.cloudStorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CloudStorageAPIFactory {

    @Autowired
    private DropboxApi dropboxAPI;

    public CloudStorageApi createCloudStorageAPI(String provider) {
        if ("dropbox".equalsIgnoreCase(provider)) {
            return dropboxAPI;
        } else {
            throw new IllegalArgumentException("Invalid cloud storage provider specified.");
        }
    }
}
