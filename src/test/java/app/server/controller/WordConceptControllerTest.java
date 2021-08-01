package app.server.controller;

import app.server.config.SpringWebConfig;
import app.server.config.WebConfiguration;
import app.server.domain.Concept;
import app.server.domain.custom.PartOfSpeech;
import app.server.resource.ConceptResource;
import app.server.resource.WordConceptResource;
import app.server.resource.WordResource;
import app.server.util.ColumnData;
import app.server.util.QueryData;
import app.server.util.SortOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.reactive.config.EnableWebFlux;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ContextConfiguration(classes = {SpringWebConfig.class, WebConfiguration.class})
@SpringBootTest
@AutoConfigureMockMvc
public class WordConceptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getAll() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/word_concept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].id", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].wordId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].conceptId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].partOfSpeech", everyItem(anyOf(
                        is(PartOfSpeech.adjective.name()), is(PartOfSpeech.adverb.name()),
                        is(PartOfSpeech.verb.name()), is(PartOfSpeech.noun.name())
                ))))
                .andExpect(jsonPath("$[*].word", everyItem(nullValue())))
                .andExpect(jsonPath("$[*].concept", everyItem(nullValue())));
    }

    @Test
    public void getAllExpand() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/word_concept").queryParam("expand", "true"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].id", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].wordId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].conceptId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].partOfSpeech", everyItem(anyOf(
                        is(PartOfSpeech.adjective.name()), is(PartOfSpeech.adverb.name()),
                        is(PartOfSpeech.verb.name()), is(PartOfSpeech.noun.name())
                ))))
                .andExpect(jsonPath("$[*].word", everyItem(allOf(
                        hasKey("id"), hasKey("value"), hasKey("language"), hasKey("languageId")
                ))))
                .andExpect(jsonPath("$[*].concept", everyItem(allOf(
                        hasKey("id"), hasKey("name"), hasKey("parent"), hasKey("parentId")
                ))))
                .andReturn();

        checkExpand(mvcResult);
    }

    private void checkExpand(MvcResult mvcResult) throws UnsupportedEncodingException, JsonProcessingException {
        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        WordConceptResource[] resources = new ObjectMapper().readerFor(WordConceptResource[].class).readValue(responseBody);
        for (WordConceptResource resource: resources) {
            Long conceptId = resource.getConceptId();
            Assert.isTrue(resource.getConcept().getId().equals(conceptId),
                    "The value of foreign key and value of nested object's primary key doesn't match!");

            Long wordId = resource.getWordId();
            Assert.isTrue(resource.getWord().getId().equals(wordId),
                    "The value of foreign key and value of nested object's primary key doesn't match!");
        }
    }

    @Test
    public void filterLimit() throws Exception {
        QueryData queryData = new QueryData();
        int limit = 10;
        queryData.setLimit(limit);

        String json = getQueryDataJson(queryData);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/word_concept/filter").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", iterableWithSize(lessThanOrEqualTo(limit))))
                .andExpect(jsonPath("$[*].id", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].wordId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].conceptId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].partOfSpeech", everyItem(anyOf(
                        is(PartOfSpeech.adjective.name()), is(PartOfSpeech.adverb.name()),
                        is(PartOfSpeech.verb.name()), is(PartOfSpeech.noun.name())
                ))))
                .andExpect(jsonPath("$[*].word", everyItem(nullValue())))
                .andExpect(jsonPath("$[*].concept", everyItem(nullValue())));
    }

    @Test
    public void filterLimitAndExpand() throws Exception{
        QueryData queryData = new QueryData();
        queryData.setLimit(10);
        queryData.setExpand(true);

        String json = getQueryDataJson(queryData);

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/word_concept/filter").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", iterableWithSize(lessThanOrEqualTo(10))))
                .andExpect(jsonPath("$[*].id", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].wordId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].conceptId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].partOfSpeech", everyItem(anyOf(
                        is(PartOfSpeech.adjective.name()), is(PartOfSpeech.adverb.name()),
                        is(PartOfSpeech.verb.name()), is(PartOfSpeech.noun.name())
                ))))
                .andExpect(jsonPath("$[*].word", everyItem(allOf(
                        hasKey("id"), hasKey("value"), hasKey("language"), hasKey("languageId")
                ))))
                .andExpect(jsonPath("$[*].concept", everyItem(allOf(
                        hasKey("id"), hasKey("name"), hasKey("parent"), hasKey("parentId")
                ))))
                .andReturn();

        checkExpand(mvcResult);
    }

    private String getQueryDataJson(QueryData queryData) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writerFor(QueryData.class).writeValueAsString(queryData);
    }

    @Test
    public void filterLimitAndAscConceptNameSortAndExpand() throws Exception {
        QueryData queryData = new QueryData();
        int limit = 10;
        queryData.setLimit(limit);
        ColumnData columnData = new ColumnData();
        columnData.setColumn("concept.name");
        columnData.setOrder(SortOrder.ASCENDING);
        queryData.setColumns(List.of(columnData));
        queryData.setExpand(true);

        String json = getQueryDataJson(queryData);

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/word_concept/filter").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", iterableWithSize(lessThanOrEqualTo(limit))))
                .andExpect(jsonPath("$[*].id", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].wordId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].conceptId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].partOfSpeech", everyItem(anyOf(
                        is(PartOfSpeech.adjective.name()), is(PartOfSpeech.adverb.name()),
                        is(PartOfSpeech.verb.name()), is(PartOfSpeech.noun.name())
                ))))
                .andExpect(jsonPath("$[*].word", everyItem(allOf(
                        hasKey("id"), hasKey("value"), hasKey("language"), hasKey("languageId")
                ))))
                .andExpect(jsonPath("$[*].concept", everyItem(allOf(
                        hasKey("id"), hasKey("name"), hasKey("parent"), hasKey("parentId")
                ))))
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        WordConceptResource[] resources = new ObjectMapper().readerFor(WordConceptResource[].class).readValue(responseBody);

        String lastName = resources[0].getConcept().getName();
        String name;
        for (WordConceptResource resource: resources) {
            name = resource.getConcept().getName();
            Assert.isTrue(lastName.compareTo(name) <= 0,
                    "The array of object aren't sorted by concept name!");
            lastName = name;
        }
    }

    @Test
    public void filterLimitAndDescWordValueSortAndExpand() throws Exception {
        QueryData queryData = new QueryData();
        int limit = 10;
        queryData.setLimit(limit);
        ColumnData columnData = new ColumnData();
        columnData.setColumn("word.value");
        columnData.setOrder(SortOrder.DESCENDING);
        queryData.setColumns(List.of(columnData));
        queryData.setExpand(true);

        String json = getQueryDataJson(queryData);

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/word_concept/filter").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", iterableWithSize(lessThanOrEqualTo(limit))))
                .andExpect(jsonPath("$[*].id", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].wordId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].conceptId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].partOfSpeech", everyItem(anyOf(
                        is(PartOfSpeech.adjective.name()), is(PartOfSpeech.adverb.name()),
                        is(PartOfSpeech.verb.name()), is(PartOfSpeech.noun.name())
                ))))
                .andExpect(jsonPath("$[*].word", everyItem(allOf(
                        hasKey("id"), hasKey("value"), hasKey("language"), hasKey("languageId")
                ))))
                .andExpect(jsonPath("$[*].concept", everyItem(allOf(
                        hasKey("id"), hasKey("name"), hasKey("parent"), hasKey("parentId")
                ))))
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        WordConceptResource[] resources = new ObjectMapper().readerFor(WordConceptResource[].class).readValue(responseBody);

        String lastValue = resources[0].getWord().getValue();
        String value;
        for (WordConceptResource resource: resources) {
            value = resource.getWord().getValue();
            Assert.isTrue(lastValue.compareTo(value) >= 0,
                    "The array of object aren't sorted by word value!");
            lastValue = value;
        }
    }

    @Test
    public void filterLimitAndRegexNameAndExpand() throws Exception {
        QueryData queryData = new QueryData();
        int limit = 10;
        queryData.setLimit(limit);
        ColumnData columnData = new ColumnData();
        columnData.setColumn("concept.name");
        columnData.setRegex("%en%");
        queryData.setColumns(List.of(columnData));
        queryData.setExpand(true);

        String json = getQueryDataJson(queryData);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/word_concept/filter").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", iterableWithSize(lessThanOrEqualTo(limit))))
                .andExpect(jsonPath("$[*].id", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].wordId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].conceptId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].partOfSpeech", everyItem(anyOf(
                        is(PartOfSpeech.adjective.name()), is(PartOfSpeech.adverb.name()),
                        is(PartOfSpeech.verb.name()), is(PartOfSpeech.noun.name())
                ))))
                .andExpect(jsonPath("$[*].word", everyItem(allOf(
                        hasKey("id"), hasKey("value"), hasKey("language"), hasKey("languageId")
                ))))
                .andExpect(jsonPath("$[*].concept", everyItem(allOf(
                        hasKey("id"), hasKey("name"), hasKey("parent"), hasKey("parentId")
                ))))
                .andExpect(jsonPath("$[*].word.name", everyItem(matchesPattern(".*en.*"))));
    }

    @Test
    public void getById() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/word_concept/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.wordId", allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                )))
                .andExpect(jsonPath("$.conceptId", allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                )))
                .andExpect(jsonPath("$.partOfSpeech", anyOf(
                        is(PartOfSpeech.adjective.name()), is(PartOfSpeech.adverb.name()),
                        is(PartOfSpeech.verb.name()), is(PartOfSpeech.noun.name())
                )))
                .andExpect(jsonPath("$.word", nullValue()))
                .andExpect(jsonPath("$.concept", nullValue()));
    }

    @Test
    public void getNullById() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/word_concept/0"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void postAndDelete() throws Exception {
        ConceptResource concept = getPostedConceptResource("test_entity");
        WordResource word = getPostedWordResource("test word", 1);
        WordConceptResource resource = getPostedResource(concept, word, PartOfSpeech.noun);

        deleteResource(resource);
        deleteConceptResource(concept);
        deleteWordResource(word);
    }

    private void deleteWordResource(WordResource word) throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/word?id=" + word.getId()))
                .andExpect(status().isOk());
    }

    private void deleteConceptResource(ConceptResource concept) throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/concept?id=" + concept.getId()))
                .andExpect(status().isOk());
    }

    private void deleteResource(WordConceptResource resource) throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/word_concept?id=" + resource.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(resource.getId()))
                .andExpect(jsonPath("$.wordId").isNumber())
                .andExpect(jsonPath("$.wordId").value(resource.getWordId()))
                .andExpect(jsonPath("$.conceptId").isNumber())
                .andExpect(jsonPath("$.conceptId").value(resource.getConceptId()))
                .andExpect(jsonPath("$.partOfSpeech", equalTo(resource.getPartOfSpeech().name())))
                .andExpect(jsonPath("$.word", nullValue()))
                .andExpect(jsonPath("$.concept", nullValue()));
    }

    private WordConceptResource getPostedResource(ConceptResource concept, WordResource word, PartOfSpeech partOfSpeech) throws Exception {
        WordConceptResource resource = new WordConceptResource();
        resource.setConcept(concept);
        resource.setConceptId(concept.getId());
        resource.setWord(word);
        resource.setWordId(word.getId());
        resource.setPartOfSpeech(partOfSpeech);

        String json = getJson(resource, WordConceptResource.class);

        String postJson = this.mockMvc.perform(MockMvcRequestBuilders.post("/word_concept").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                )))
                .andExpect(jsonPath("$.wordId").isNumber())
                .andExpect(jsonPath("$.wordId").value(word.getId()))
                .andExpect(jsonPath("$.conceptId").isNumber())
                .andExpect(jsonPath("$.conceptId").value(concept.getId()))
                .andExpect(jsonPath("$.partOfSpeech", equalTo(partOfSpeech.name())))
                .andExpect(jsonPath("$.word", nullValue()))
                .andExpect(jsonPath("$.concept", nullValue()))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        return fromJson(WordConceptResource.class, postJson);
    }

    private WordResource getPostedWordResource(String value, Integer languageId) throws Exception {
        WordResource resource = new WordResource();
        resource.setValue(value);
        resource.setLanguageId(languageId);

        String json = getJson(resource, WordResource.class);

        String postWord = this.mockMvc.perform(MockMvcRequestBuilders.post("/word").contentType(MediaType.APPLICATION_JSON)
            .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        return fromJson(WordResource.class, postWord);
    }

    private ConceptResource getPostedConceptResource(String name) throws Exception {
        ConceptResource resource = new ConceptResource();
        resource.setName(name);

        String json = getJson(resource, ConceptResource.class);

        String postConcept = this.mockMvc.perform(MockMvcRequestBuilders.post("/concept").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        return fromJson(ConceptResource.class, postConcept);
    }

    private <T> T fromJson(Class<T> tClass, String json) throws JsonProcessingException {
        return new ObjectMapper().readerFor(tClass).readValue(json);
    }

    private String getJson(Object object, Class<?> aClass) throws JsonProcessingException {
        return new ObjectMapper().writerFor(aClass).writeValueAsString(object);
    }

    @Test
    public void postPutAndDelete() throws Exception {
        ConceptResource postConcept = getPostedConceptResource("test_entity");
        WordResource postWord = getPostedWordResource("test word", 1);
        WordConceptResource resource = getPostedResource(postConcept, postWord, PartOfSpeech.noun);

        ConceptResource putConcept = getPostedConceptResource("test_put_entity");
        WordResource putWord = getPostedWordResource("тестовое слово", 2);

        resource.setWord(putWord);
        resource.setWordId(putWord.getId());

        resource.setConcept(putConcept);
        resource.setConceptId(putConcept.getId());

        WordConceptResource putResource = getPuttedResource(resource);

        deleteResource(putResource);

        deleteConceptResource(postConcept);
        deleteWordResource(postWord);

        deleteConceptResource(putConcept);
        deleteWordResource(putWord);
    }

    private WordConceptResource getPuttedResource(WordConceptResource resource) throws Exception {
        String json = getJson(resource, WordConceptResource.class);

        String putJson = this.mockMvc.perform(MockMvcRequestBuilders.put("/word_concept").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(resource.getId()))
                .andExpect(jsonPath("$.wordId").isNumber())
                .andExpect(jsonPath("$.wordId").value(resource.getWordId()))
                .andExpect(jsonPath("$.conceptId").isNumber())
                .andExpect(jsonPath("$.conceptId").value(resource.getConceptId()))
                .andExpect(jsonPath("$.partOfSpeech", equalTo(resource.getPartOfSpeech().name())))
                .andExpect(jsonPath("$.word", nullValue()))
                .andExpect(jsonPath("$.concept", nullValue()))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        return fromJson(WordConceptResource.class, putJson);
    }

    @Test
    public void count() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/word_concept/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", allOf(
                        greaterThanOrEqualTo(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                )));
    }

}
