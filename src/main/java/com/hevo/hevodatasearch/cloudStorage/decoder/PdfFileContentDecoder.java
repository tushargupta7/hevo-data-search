package com.hevo.hevodatasearch.cloudStorage.decoder;

import com.hevo.hevodatasearch.exception.FileContentDecodeException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;

import static com.hevo.hevodatasearch.constants.ErrorConstants.DOC_FILE_DECODE_ERROR;

public class PdfFileContentDecoder implements FileContentDecoderStrategy {
    @Override
    public String decodeContent(InputStream inputStream) {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new FileContentDecodeException(String.format(DOC_FILE_DECODE_ERROR,".pdf"), e);
        }
    }
}
