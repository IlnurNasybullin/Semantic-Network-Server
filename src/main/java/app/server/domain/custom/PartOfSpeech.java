package app.server.domain.custom;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PartOfSpeech {
    @JsonProperty("noun")
    noun,

    @JsonProperty("verb")
    verb,

    @JsonProperty("adverb")
    adverb,

    @JsonProperty("adjective")
    adjective
}
