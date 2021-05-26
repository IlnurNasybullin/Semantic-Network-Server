package app.server.util;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public enum SortOrder implements Serializable {
    @JsonProperty("ascending")
    ASCENDING,

    @JsonProperty("descending")
    DESCENDING
}
