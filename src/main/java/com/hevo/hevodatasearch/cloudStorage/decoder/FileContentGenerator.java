package com.hevo.hevodatasearch.cloudStorage.decoder;

import java.io.InputStream;

public class FileContentGenerator {
    private final FileContentDecoderStrategy fileContentDecoder;

    public FileContentGenerator(FileContentDecoderStrategy fileContentDecoder) {
        this.fileContentDecoder = fileContentDecoder;
    }

    public String generateContent(InputStream inputStream) {
        return fileContentDecoder.decodeContent(inputStream);
    }
}
