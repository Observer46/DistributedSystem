package agh.sr.REST.api.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AnalyzedData {
    private Float value;
    private String source;
    private Integer index;
}
