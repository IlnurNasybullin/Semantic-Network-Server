package app.server.util;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public enum FileFormat implements Serializable {
    @JsonProperty("pdf")
    pdf,

    @JsonProperty("docx")
    docx,

    @JsonProperty("xlsx")
    xlsx
}
