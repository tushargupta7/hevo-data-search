package com.hevo.hevodatasearch.cloudStorage.decoder;

import java.io.InputStream;

public interface FileContentDecoderStrategy {
    String decodeContent(InputStream inputStream);
}
