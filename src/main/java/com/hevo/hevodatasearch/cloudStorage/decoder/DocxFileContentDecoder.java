package com.hevo.hevodatasearch.cloudStorage.decoder;

import com.hevo.hevodatasearch.exception.FileContentDecodeException;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;
import java.io.InputStream;

import static com.hevo.hevodatasearch.constants.ErrorConstants.DOC_FILE_DECODE_ERROR;

public class DocxFileContentDecoder implements FileContentDecoderStrategy{
    @Override
    public String decodeContent(InputStream inputStream) {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        } catch (IOException e) {
            throw new FileContentDecodeException(String.format(DOC_FILE_DECODE_ERROR,".docx"), e);
        }
    }
}
