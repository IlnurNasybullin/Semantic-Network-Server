package app.server.controller;

import app.server.config.SpringWebConfig;
import app.server.config.WebConfiguration;
import app.server.resource.ConceptResource;
import app.server.util.ColumnData;
import app.server.util.QueryData;
import app.server.util.SortOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration(classes = {SpringWebConfig.class, WebConfiguration.class})
@SpringBootTest
@AutoConfigureMockMvc
public class ConceptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getAll() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/concept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].id", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].name", everyItem(not(blankOrNullString()))))
                .andExpect(jsonPath("$[*].parent", everyItem(nullValue())))
                .andExpect(jsonPath("$[*].parentId", everyItem(anyOf(
                        nullValue(), allOf(
                                greaterThan(0), anyOf(
                                        isA(Integer.class), isA(Long.class)
                                )
                        )
                ))));
    }

    @Test
    public void getAllExpand() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/concept").queryParam("expand", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].id", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].name", everyItem(not(blankOrNullString()))))
                .andExpect(jsonPath("$[*].parentId", everyItem(anyOf(
                        nullValue(), allOf(
                                greaterThan(0), anyOf(
                                        isA(Integer.class), isA(Long.class)
                                )
                        )
                ))))
                .andExpect(jsonPath("$[*].parent", everyItem(anyOf(
                        nullValue(), allOf(
                            hasKey("id"), hasKey("name"), hasKey("parent"), hasKey("parentId")
                        )
                ))))
                .andReturn();

        checkExpand(mvcResult);
    }

    private void checkExpand(MvcResult mvcResult) throws UnsupportedEncodingException, JsonProcessingException {
        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ConceptResource[] resources = new ObjectMapper().readerFor(ConceptResource[].class).readValue(responseBody);
        for (ConceptResource resource: resources) {
            Long parentId = resource.getParentId();
            if (parentId != null) {
                Assert.isTrue(Objects.equals(parentId, resource.getParent().getId()),
                        "The value of foreign key and value of nested object's primary key doesn't match!");
            }
        }
    }

    @Test
    public void filterLimit() throws Exception {
        QueryData queryData = new QueryData();
        int limit = 10;
        queryData.setLimit(limit);

        String json = getQueryDataJson(queryData);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/concept/filter").contentType(MediaType.APPLICATION_JSON)
            .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", iterableWithSize(lessThanOrEqualTo(limit))))
                .andExpect(jsonPath("$[*].id", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].name", everyItem(not(blankOrNullString()))))
                .andExpect(jsonPath("$[*].parent", everyItem(nullValue())))
                .andExpect(jsonPath("$[*].parentId", everyItem(anyOf(
                        nullValue(), allOf(
                                greaterThan(0), anyOf(
                                        isA(Integer.class), isA(Long.class)
                                )
                        )
                ))));
    }

    @Test
    public void filterLimitAndExpand() throws Exception{
        QueryData queryData = new QueryData();
        int limit = 10;
        queryData.setLimit(limit);
        queryData.setExpand(true);

        String json = getQueryDataJson(queryData);

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/concept/filter").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", iterableWithSize(lessThanOrEqualTo(limit))))
                .andExpect(jsonPath("$[*].id", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].name", everyItem(not(blankOrNullString()))))
                .andExpect(jsonPath("$[*].parentId", everyItem(anyOf(
                        nullValue(), allOf(
                                greaterThan(0), anyOf(
                                        isA(Integer.class), isA(Long.class)
                                )
                        )
                ))))
                .andExpect(jsonPath("$[*].parent", everyItem(anyOf(
                        nullValue(), allOf(
                                hasKey("id"), hasKey("name"), hasKey("parent"), hasKey("parentId")
                        )
                ))))
                .andReturn();

        checkExpand(mvcResult);
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

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/concept/filter").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", iterableWithSize(lessThanOrEqualTo(limit))))
                .andExpect(jsonPath("$[*].id", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].name", everyItem(not(blankOrNullString()))))
                .andExpect(jsonPath("$[*].parent", everyItem(nullValue())))
                .andExpect(jsonPath("$[*].parentId", everyItem(anyOf(
                        nullValue(), allOf(
                                greaterThan(0), anyOf(
                                        isA(Integer.class), isA(Long.class)
                                )
                        )
                ))))
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ConceptResource[] resources = new ObjectMapper().readerFor(ConceptResource[].class).readValue(responseBody);

        String lastName = resources[0].getName();
        String name;
        for (ConceptResource resource: resources) {
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

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/concept/filter").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", iterableWithSize(lessThanOrEqualTo(limit))))
                .andExpect(jsonPath("$[*].id", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].name", everyItem(not(blankOrNullString()))))
                .andExpect(jsonPath("$[*].parent", everyItem(nullValue())))
                .andExpect(jsonPath("$[*].parentId", everyItem(anyOf(
                        nullValue(), allOf(
                                greaterThan(0), anyOf(
                                        isA(Integer.class), isA(Long.class)
                                )
                        )
                ))))
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ConceptResource[] resources = new ObjectMapper().readerFor(ConceptResource[].class).readValue(responseBody);

        long lastId = Long.MAX_VALUE;
        Long id;
        for (ConceptResource resource: resources) {
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
        columnData.setRegex("%en%");
        queryData.setColumns(List.of(columnData));

        String json = getQueryDataJson(queryData);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/concept/filter").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", iterableWithSize(lessThanOrEqualTo(limit))))
                .andExpect(jsonPath("$[*].id", everyItem(allOf(
                        greaterThan(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                ))))
                .andExpect(jsonPath("$[*].name", everyItem(matchesPattern(".*en.*"))))
                .andExpect(jsonPath("$[*].parent", everyItem(nullValue())))
                .andExpect(jsonPath("$[*].parentId", everyItem(anyOf(
                        nullValue(), allOf(
                                greaterThan(0), anyOf(
                                        isA(Integer.class), isA(Long.class)
                                )
                        )
                ))));

    }

    @Test
    public void getById() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/concept/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.name", not(blankOrNullString())))
                .andExpect(jsonPath("$.parentId", anyOf(
                        nullValue(), allOf(greaterThan(0),
                                anyOf(
                                        isA(Integer.class), isA(Long.class)
                                )
                        ))
                ))
                .andExpect(jsonPath("$.parent", is(nullValue())));
    }

    @Test
    public void getNullById() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/concept/0"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void postAndDelete() throws Exception {
        MvcResult result = getPostedConcept();
        ConceptResource resource = getConcept(result);
        deleteResource(resource);
    }

    private MvcResult getPostedConcept() throws Exception {
        ConceptResource conceptResource = new ConceptResource();
        String name = "test_concept";
        conceptResource.setName(name);

        String json = getJson(conceptResource);
        return this.mockMvc.perform(MockMvcRequestBuilders.post("/concept").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", greaterThan(0)))
                .andExpect(jsonPath("$.name", equalTo(name)))
                .andExpect(jsonPath("$.parent", is(nullValue())))
                .andExpect(jsonPath("$.parentId", is(nullValue())))
                .andReturn();
    }

    private String getJson(ConceptResource conceptResource) throws JsonProcessingException {
        if (conceptResource == null) {
            return null;
        }

        return new ObjectMapper().writerFor(ConceptResource.class).writeValueAsString(conceptResource);
    }

    private void deleteResource(ConceptResource resource) throws Exception {
        Long id = resource.getId();
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/concept?id=" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", anyOf(isA(
                        Integer.class), isA(Long.class)
                )))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name", equalTo(resource.getName())))
                .andExpect(jsonPath("$.parent", equalTo(getJson(resource.getParent()))))
                .andExpect(jsonPath("$.parentId", anyOf(isA(
                        Integer.class), isA(Long.class), nullValue()
                )))
                .andExpect(jsonPath("$.parentId").value(resource.getParentId()));
    }

    private ConceptResource getConcept(MvcResult result) throws UnsupportedEncodingException, JsonProcessingException {
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        return new ObjectMapper().readerFor(ConceptResource.class).readValue(content);
    }

    @Test
    public void postPutAndDelete() throws Exception {
        MvcResult result = getPostedConcept();
        ConceptResource postConcept = getConcept(result);
        ConceptResource putConcept = getPuttedConcept(postConcept);
        deleteResource(putConcept);
    }

    private ConceptResource getPuttedConcept(ConceptResource conceptResource) throws Exception {
        conceptResource.setName("not_test_concept");
        long parentId = 1L;
        conceptResource.setParentId(parentId);
        String json = getJson(conceptResource);
        MvcResult mvcResult = this.mockMvc.perform(put("/concept").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", anyOf(isA(
                        Integer.class), isA(Long.class)
                )))
                .andExpect(jsonPath("$.id").value(conceptResource.getId()))
                .andExpect(jsonPath("$.name", equalTo(conceptResource.getName())))
                .andExpect(jsonPath("$.parentId", anyOf(isA(
                        Integer.class), isA(Long.class)
                )))
                .andExpect(jsonPath("$.parentId").value(parentId))
                .andExpect(jsonPath("$.parent", nullValue()))
                .andReturn();

        return getConcept(mvcResult);
    }

    @Test
    public void count() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/concept/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", allOf(
                        greaterThanOrEqualTo(0), anyOf(
                                isA(Integer.class), isA(Long.class)
                        )
                )));
    }

}
