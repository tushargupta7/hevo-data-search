package com.hevo.hevodatasearch.cloudStorage.decoder;

import com.hevo.hevodatasearch.exception.FileContentDecodeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.hevo.hevodatasearch.constants.ErrorConstants.DOC_FILE_DECODE_ERROR;

public class TextFileContentDecode implements FileContentDecoderStrategy {
    @Override
    public String decodeContent(InputStream inputStream) {
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new FileContentDecodeException(String.format(DOC_FILE_DECODE_ERROR,".txt"), e);
        }

        return content.toString();
    }
}
