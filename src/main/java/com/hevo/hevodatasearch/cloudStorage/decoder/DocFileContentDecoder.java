package com.hevo.hevodatasearch.cloudStorage.decoder;

import java.io.IOException;
import java.io.InputStream;

import com.hevo.hevodatasearch.exception.FileContentDecodeException;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;

import static com.hevo.hevodatasearch.constants.ErrorConstants.DOC_FILE_DECODE_ERROR;

public class DocFileContentDecoder implements FileContentDecoderStrategy {
    @Override
    public String decodeContent(InputStream inputStream) {
        try (HWPFDocument document = new HWPFDocument(inputStream)) {
            WordExtractor extractor = new WordExtractor(document);
            return extractor.getText();
        } catch (IOException e) {
            throw new FileContentDecodeException(String.format(DOC_FILE_DECODE_ERROR,".doc"), e);
        }
    }
}