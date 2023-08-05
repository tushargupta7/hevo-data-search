package com.hevo.hevodatasearch.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Document {
    String _id;
    String path;
    String content;
    String fileName;
}
