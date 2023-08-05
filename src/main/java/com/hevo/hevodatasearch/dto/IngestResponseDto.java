package com.hevo.hevodatasearch.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
public class IngestResponseDto {
    List<String> pathList;
    String message;
}
