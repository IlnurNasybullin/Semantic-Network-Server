package app.server.util;

import app.server.domain.custom.PartOfSpeech;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class Result {

    private String fromWord;
    private PartOfSpeech partOfSpeech;
    private String toWord;
    private Long conceptId;

    public static class DefinitionKey {

        private final String word;
        private final PartOfSpeech partOfSpeech;

        public DefinitionKey(String word, PartOfSpeech partOfSpeech) {
            this.word = word;
            this.partOfSpeech = partOfSpeech;
        }

        public String getWord() {
            return word;
        }

        public PartOfSpeech getPartOfSpeech() {
            return partOfSpeech;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DefinitionKey that = (DefinitionKey) o;
            return Objects.equals(word, that.word) && partOfSpeech == that.partOfSpeech;
        }

        @Override
        public int hashCode() {
            return Objects.hash(word, partOfSpeech);
        }
    }

    public static class DefinitionValues {
        private final LinkedHashMap<Long, List<String>> values;

        public DefinitionValues() {
            this.values = new LinkedHashMap<>();
        }

        public void add(Long conceptId, String word) {
            List<String> list = values.getOrDefault(conceptId, new ArrayList<>());
            list.add(word);
            values.putIfAbsent(conceptId, list);
        }

        public DefinitionValues add(DefinitionValues values) {
            values.values.forEach((key, value) -> {
                if (this.values.containsKey(key)) {
                    this.values.get(key).addAll(value);
                } else {
                    this.values.put(key, value);
                }
            });

            return this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (values.size() > 1) {
                byte[] definition = {1};
                values.forEach((key, value) -> {
                    builder.append(definition[0]).append(". ")
                            .append(String.join(", ", value))
                            .append(" ");
                    definition[0]++;
                });

                return builder.toString();
            } else {
                values.forEach((key, value) -> {
                    builder.append(String.join(", ", value));
                });
            }

            return builder.toString();
        }
    }

    public Result(String fromWord, PartOfSpeech partOfSpeech, String toWord, Long conceptId) {
        this.fromWord = fromWord;
        this.partOfSpeech = partOfSpeech;
        this.toWord = toWord;
        this.conceptId = conceptId;
    }

    public String getFromWord() {
        return fromWord;
    }

    public void setFromWord(String fromWord) {
        this.fromWord = fromWord;
    }

    public PartOfSpeech getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(PartOfSpeech partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public String getToWord() {
        return toWord;
    }

    public void setToWord(String toWord) {
        this.toWord = toWord;
    }

    public Long getConceptId() {
        return conceptId;
    }

    public void setConceptId(Long conceptId) {
        this.conceptId = conceptId;
    }

    public DefinitionKey getDefinitionKey() {
        return new DefinitionKey(fromWord, partOfSpeech);
    }

    public DefinitionValues getDefinitionValues() {
        DefinitionValues definitionValues = new DefinitionValues();
        definitionValues.add(conceptId, toWord);
        return definitionValues;
    }
}
