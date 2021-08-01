package app.server.controller;

import app.server.config.SpringWebConfig;
import app.server.config.WebConfiguration;
import app.server.resource.LanguageResource;
import app.server.util.ColumnData;
import app.server.util.QueryData;
import app.server.util.SortOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration(classes = {SpringWebConfig.class, WebConfiguration.class})
@SpringBootTest
@AutoConfigureMockMvc
public class LanguageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final static Matcher<String> uniqueAlphabetSymbolsConstraint = new BaseMatcher<>() {

        @Override
        public boolean matches(Object actual) {
            if (!(actual instanceof String)) {
                return false;
            }

            String alphabet = (String) actual;
            char[] symbols = new char[alphabet.length()];
            alphabet.getChars(0, alphabet.length(), symbols, 0);
            Set<Character> characters = new HashSet<>();
            for (char symbol : symbols) {
                if (Character.isSpaceChar(symbol) || !characters.add(symbol)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("The string value has space or not unique (duplicated) symbol!");
        }
    };

    @Test
    public void getAll() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/language"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].id", everyItem(allOf(
                        greaterThan(0), isA(Integer.class)
                ))))
                .andExpect(jsonPath("$[*].name", everyItem(not(blankOrNullString()))))
                .andExpect(jsonPath("$[*].alphabet", everyItem(uniqueAlphabetSymbolsConstraint)));
    }

    @Test
    public void getAllExpand() throws Exception {
        getAll();
    }

    @Test
    public void filterLimit() throws Exception {
        QueryData queryData = new QueryData();
        int limit = 10;
        queryData.setLimit(limit);

        String json = getQueryDataJson(queryData);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/language/filter").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", iterableWithSize(lessThanOrEqualTo(limit))))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].id", everyItem(allOf(
                        greaterThan(0), isA(Integer.class)
                ))))
                .andExpect(jsonPath("$[*].name", everyItem(not(blankOrNullString()))))
                .andExpect(jsonPath("$[*].alphabet", everyItem(uniqueAlphabetSymbolsConstraint)));
    }

    @Test
    public void filterLimitAndExpand() throws Exception{
        filterLimit();
    }

    private String getQueryDataJson(QueryData queryData) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writerFor(QueryData.class).writeValueAsString(queryData);
    }

    @Test
    public void filterLimitAndAscNameSort() throws Exception {
        QueryData queryData = new QueryData();
        int limit = 10;
        queryData.setLimit(limit);
        ColumnData columnData = new ColumnData();
        columnData.setColumn("name");
        columnData.setOrder(SortOrder.ASCENDING);
        queryData.setColumns(List.of(columnData));

        String json = getQueryDataJson(queryData);

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/language/filter").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", iterableWithSize(lessThanOrEqualTo(limit))))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].id", everyItem(allOf(
                        greaterThan(0), isA(Integer.class)
                ))))
                .andExpect(jsonPath("$[*].name", everyItem(not(blankOrNullString()))))
                .andExpect(jsonPath("$[*].alphabet", everyItem(uniqueAlphabetSymbolsConstraint)))
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        LanguageResource[] resources = new ObjectMapper().readerFor(LanguageResource[].class).readValue(responseBody);

        String lastName = resources[0].getName();
        String name;
        for (LanguageResource resource: resources) {
            name = resource.getName();
            Assert.isTrue(lastName.compareTo(name) <= 0, "The array of object aren't sorted by name!");
            lastName = name;
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

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/language/filter").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", iterableWithSize(lessThanOrEqualTo(limit))))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].id", everyItem(allOf(
                        greaterThan(0), isA(Integer.class)
                ))))
                .andExpect(jsonPath("$[*].name", everyItem(not(blankOrNullString()))))
                .andExpect(jsonPath("$[*].alphabet", everyItem(uniqueAlphabetSymbolsConstraint)))
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        LanguageResource[] resources = new ObjectMapper().readerFor(LanguageResource[].class).readValue(responseBody);

        int lastId = Integer.MAX_VALUE;
        Integer id;
        for (LanguageResource resource: resources) {
            id = resource.getId();
            Assert.isTrue(lastId > id, "The array of object aren't sorted by id!");
            lastId = id;
        }
    }

    @Test
    public void filterLimitAndRegexName() throws Exception {
        QueryData queryData = new QueryData();
        int limit = 10;
        queryData.setLimit(limit);
        ColumnData columnData = new ColumnData();
        columnData.setColumn("name");
        columnData.setRegex("%а%");
        queryData.setColumns(List.of(columnData));

        String json = getQueryDataJson(queryData);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/language/filter").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", iterableWithSize(lessThanOrEqualTo(limit))))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].id", everyItem(allOf(
                        greaterThan(0), isA(Integer.class)
                ))))
                .andExpect(jsonPath("$[*].name", everyItem(not(blankOrNullString()))))
                .andExpect(jsonPath("$[*].alphabet", everyItem(uniqueAlphabetSymbolsConstraint)));
    }

    @Test
    public void getById() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/language/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.name", not(blankOrNullString())))
                .andExpect(jsonPath("$.alphabet", uniqueAlphabetSymbolsConstraint));
    }

    @Test
    public void getNullById() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/language/0"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void postAndDelete() throws Exception {
        MvcResult result = getPostedLanguage();
        LanguageResource resource = getLanguage(result);
        deleteResource(resource);
    }

    private MvcResult getPostedLanguage() throws Exception {
        LanguageResource languageResource = new LanguageResource();
        String name = "test_binary";
        String alphabet = "01";

        languageResource.setName(name);
        languageResource.setAlphabet(alphabet);

        String json = getJson(languageResource);
        return this.mockMvc.perform(MockMvcRequestBuilders.post("/language").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", greaterThan(0)))
                .andExpect(jsonPath("$.name", equalTo(name)))
                .andExpect(jsonPath("$.alphabet", equalTo(alphabet)))
                .andReturn();
    }

    private String getJson(LanguageResource languageResource) throws JsonProcessingException {
        if (languageResource == null) {
            return null;
        }

        return new ObjectMapper().writerFor(LanguageResource.class).writeValueAsString(languageResource);
    }

    private void deleteResource(LanguageResource resource) throws Exception {
        Integer id = resource.getId();
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/language?id=" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", isA(Integer.class)))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name", equalTo(resource.getName())))
                .andExpect(jsonPath("$.alphabet", equalTo(resource.getAlphabet())));
    }

    private LanguageResource getLanguage(MvcResult result) throws UnsupportedEncodingException, JsonProcessingException {
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return new ObjectMapper().readerFor(LanguageResource.class).readValue(content);
    }

    @Test
    public void postPutAndDelete() throws Exception {
        MvcResult result = getPostedLanguage();
        LanguageResource postLanguage = getLanguage(result);
        LanguageResource putLanguage = getPuttedLanguage(postLanguage);
        deleteResource(putLanguage);
    }

    private LanguageResource getPuttedLanguage(LanguageResource languageResource) throws Exception {
        languageResource.setName("test_numeral");
        languageResource.setAlphabet("0123456789");

        String json = getJson(languageResource);
        MvcResult mvcResult = this.mockMvc.perform(put("/language").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", isA(Integer.class)))
                .andExpect(jsonPath("$.id").value(languageResource.getId()))
                .andExpect(jsonPath("$.name", equalTo(languageResource.getName())))
                .andExpect(jsonPath("$.alphabet", uniqueAlphabetSymbolsConstraint))
                .andReturn();

        return getLanguage(mvcResult);
    }

    @Test
    public void count() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/language/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", allOf(
                        greaterThanOrEqualTo(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                )));
    }

}
