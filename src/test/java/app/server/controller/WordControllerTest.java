package app.server.controller;

import app.server.config.SpringWebConfig;
import app.server.config.WebConfiguration;
import app.server.resource.LanguageResource;
import app.server.resource.WordResource;
import app.server.util.ColumnData;
import app.server.util.QueryData;
import app.server.util.SortOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpringWebConfig.class, WebConfiguration.class})
@WebAppConfiguration
@EnableWebFlux
public class WordControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private final Set<Character> allowedSymbols = Set.of('-', '\'');

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    public void getAll() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/word"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].value", everyItem(not(blankOrNullString()))))
                .andExpect(jsonPath("$[*].languageId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].language", everyItem(nullValue())));
    }

    @Test
    public void getAllExpand() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/word").queryParam("expand", "true"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].value", everyItem(not(blankOrNullString()))))
                .andExpect(jsonPath("$[*].languageId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].language", everyItem(
                        allOf(hasKey("id"), hasKey("name"), hasKey("alphabet"))
                )))
                .andReturn();

        checkExpand(mvcResult);
    }

    private void checkExpand(MvcResult mvcResult) throws UnsupportedEncodingException, com.fasterxml.jackson.core.JsonProcessingException {
        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        WordResource[] resources = new ObjectMapper().readerFor(WordResource[].class).readValue(responseBody);
        for (WordResource resource : resources) {
            LanguageResource language = resource.getLanguage();
            Assert.isTrue(resource.getLanguageId().equals(language.getId()),
                    "The value of foreign key and value of nested object's primary key doesn't match!");
            String value = resource.getValue();
            String alphabet = language.getAlphabet();
            Assert.isTrue(wordContainsInAlphabet(value, alphabet),
                    String.format("The exist at least one symbol of word (%s) that doesn't contain in language alphabet (%s)!",
                            value, alphabet));
        }
    }

    private boolean wordContainsInAlphabet(String value, String alphabet) {
        int length = alphabet.length();

        Set<Character> characters = new HashSet<>(length);
        char[] symbols = new char[length];
        alphabet.getChars(0, length, symbols, 0);

        for (char symbol : symbols) {
            characters.add(symbol);
        }

        for (int i = 0; i < value.length(); i++) {
            char symbol = value.charAt(i);
            if (Character.isSpaceChar(symbol) || allowedSymbols.contains(symbol)) {
                continue;
            }

            if (!characters.contains(Character.toUpperCase(symbol)) && !characters.contains(Character.toLowerCase(symbol))) {
                return false;
            }
        }

        return true;
    }

    @Test
    public void filterLimit() throws Exception {
        QueryData queryData = new QueryData();
        int limit = 10;
        queryData.setLimit(limit);

        String json = getQueryDataJson(queryData);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/word/filter").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", iterableWithSize(lessThanOrEqualTo(limit))))
                .andExpect(jsonPath("$[*].value", everyItem(not(blankOrNullString()))))
                .andExpect(jsonPath("$[*].languageId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].language", everyItem(nullValue())));
    }

    @Test
    public void filterLimitAndExpand() throws Exception {
        QueryData queryData = new QueryData();
        queryData.setLimit(10);
        queryData.setExpand(true);

        String json = getQueryDataJson(queryData);

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/word/filter").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].value", everyItem(not(blankOrNullString()))))
                .andExpect(jsonPath("$[*].languageId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].language", everyItem(
                        allOf(hasKey("id"), hasKey("name"), hasKey("alphabet"))
                )))
                .andReturn();

        checkExpand(mvcResult);
    }

    private String getQueryDataJson(QueryData queryData) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writerFor(QueryData.class).writeValueAsString(queryData);
    }

    @Test
    public void filterLimitAndAscValueSort() throws Exception {
        QueryData queryData = new QueryData();
        int limit = 10;
        queryData.setLimit(limit);
        ColumnData columnData = new ColumnData();
        columnData.setColumn("value");
        columnData.setOrder(SortOrder.ASCENDING);
        queryData.setColumns(List.of(columnData));

        String json = getQueryDataJson(queryData);

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/word/filter").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", iterableWithSize(lessThanOrEqualTo(limit))))
                .andExpect(jsonPath("$[*].value", everyItem(not(blankOrNullString()))))
                .andExpect(jsonPath("$[*].languageId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].language", everyItem(nullValue())))
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        WordResource[] resources = new ObjectMapper().readerFor(WordResource[].class).readValue(responseBody);

        String lastValue = resources[0].getValue();
        String value;
        for (WordResource resource : resources) {
            value = resource.getValue();
            Assert.isTrue(lastValue.compareTo(value) <= 0, "The array of object aren't sorted by value!");
            lastValue = value;
        }
    }

    @Test
    public void filterLimitAndDescIdSort() throws Exception {
        QueryData queryData = new QueryData();
        int limit = 10;
        queryData.setLimit(limit);
        ColumnData columnData = new ColumnData();
        columnData.setColumn("id");
        columnData.setOrder(SortOrder.DESCENDING);
        queryData.setColumns(List.of(columnData));

        String json = getQueryDataJson(queryData);

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/word/filter").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", iterableWithSize(lessThanOrEqualTo(limit))))
                .andExpect(jsonPath("$[*].value", everyItem(not(blankOrNullString()))))
                .andExpect(jsonPath("$[*].languageId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].language", everyItem(nullValue())))
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        WordResource[] resources = new ObjectMapper().readerFor(WordResource[].class).readValue(responseBody);

        long lastId = Long.MAX_VALUE;
        Long id;
        for (WordResource resource : resources) {
            id = resource.getId();
            Assert.isTrue(lastId > id, "The array of object aren't sorted by id!");
            lastId = id;
        }
    }

    @Test
    public void filterLimitAndRegexValue() throws Exception {
        QueryData queryData = new QueryData();
        int limit = 10;
        queryData.setLimit(limit);
        ColumnData columnData = new ColumnData();
        columnData.setColumn("value");
        columnData.setRegex("%en%");
        queryData.setColumns(List.of(columnData));

        String json = getQueryDataJson(queryData);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/word/filter").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", iterableWithSize(lessThanOrEqualTo(limit))))
                .andExpect(jsonPath("$[*].value", everyItem(matchesPattern(".*en.*"))))
                .andExpect(jsonPath("$[*].languageId", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].language", everyItem(nullValue())));
    }

    @Test
    public void getById() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/word/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.value", not(blankOrNullString())))
                .andExpect(jsonPath("$.languageId", allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                )))
                .andExpect(jsonPath("$.language", nullValue()));
    }

    @Test
    public void getNullById() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/word/0"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    private void deleteResource(WordResource resource) throws Exception {
        Long id = resource.getId();
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/word?id=" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.value", equalTo(resource.getValue())))
                .andExpect(jsonPath("$.language", nullValue()))
                .andExpect(jsonPath("$.languageId").isNumber())
                .andExpect(jsonPath("$.languageId").value(resource.getLanguageId()));
    }

    @Test
    public void postAndDelete() throws Exception {
        MvcResult result = getPostedWord();
        WordResource resource = getWord(result);
        deleteResource(resource);
    }

    private MvcResult getPostedWord() throws Exception {
        WordResource wordResource = new WordResource();
        wordResource.setValue("test value");
        wordResource.setLanguageId(1);

        String json = new ObjectMapper().writerFor(WordResource.class).writeValueAsString(wordResource);
        return this.mockMvc.perform(MockMvcRequestBuilders.post("/word").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", greaterThan(0)))
                .andExpect(jsonPath("$.value", equalTo(wordResource.getValue())))
                .andExpect(jsonPath("$.languageId").isNumber())
                .andExpect(jsonPath("$.languageId").value(wordResource.getLanguageId()))
                .andExpect(jsonPath("$.language", nullValue()))
                .andReturn();
    }

    private WordResource getWord(MvcResult result) throws UnsupportedEncodingException, JsonProcessingException {
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return new ObjectMapper().readerFor(WordResource.class).readValue(content);
    }

    @Test
    public void postPutAndDelete() throws Exception {
        MvcResult result = getPostedWord();
        WordResource postWord = getWord(result);
        WordResource putWord = getPuttedWord(postWord);
        deleteResource(putWord);
    }

    private WordResource getPuttedWord(WordResource wordResource) throws Exception {
        wordResource.setValue("тестируемое слово");
        wordResource.setLanguageId(2);

        String json = getJson(wordResource);
        MvcResult mvcResult = this.mockMvc.perform(put("/word").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(wordResource.getId()))
                .andExpect(jsonPath("$.value", equalTo(wordResource.getValue())))
                .andExpect(jsonPath("$.languageId").isNumber())
                .andExpect(jsonPath("$.languageId").value(wordResource.getLanguageId()))
                .andExpect(jsonPath("$.language", nullValue()))
                .andReturn();

        return getWord(mvcResult);
    }

    private String getJson(WordResource wordResource) throws JsonProcessingException {
        return new ObjectMapper().writerFor(WordResource.class).writeValueAsString(wordResource);
    }

    @Test
    public void count() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/word/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", allOf(
                        greaterThanOrEqualTo(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                )));
    }
}
