package com.InnovateIU;

import com.InnovateIU.DocumentManager.Document;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.InnovateIU.DocumentManager.Author;
import static com.InnovateIU.DocumentManager.SearchRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocumentManagerTest {

    DocumentManager manager;

    Map<String, Document> storage;


    @SuppressWarnings("unchecked")
    @SneakyThrows
    @BeforeEach
    public void setUp() {
        manager = new DocumentManager();

        Field field = manager.getClass().getDeclaredField("storage");
        field.setAccessible(true);

        storage = (Map<String, Document>) field.get(manager);

        Document document = Document
                .builder()
                .id("123")
                .title("Some title")
                .content("Some content")
                .author(Author
                        .builder()
                        .id("1")
                        .name("Ivan")
                        .build())
                .created(Instant.now())
                .build();

        storage.put("123", document);
    }

    @Test
    public void saveShouldSaveNewDocument() {
        Document document = generateDocument();
        Document savedDocument = manager.save(document);

        assertEquals(2, storage.size());
        assertEquals("Some content 123", storage.get(savedDocument.getId()).getContent());
    }

    @Test
    public void saveShouldUpdateDocument() {
        Document document = storage.get("123");
        document.setContent(document.getContent() + " 456");
        Document savedDocument = manager.save(document);

        assertEquals(1, storage.size());
        assertEquals("Some content 456", storage.get("123").getContent());
        assertEquals("Some content 456", savedDocument.getContent());
    }

    @Test
    public void searchShouldReturnListOfDocumentsByTitles() {
        var titlePrefixes = List.of("So", "title");
        var request = SearchRequest.builder().titlePrefixes(titlePrefixes).build();
        var documents = generateListOfDocuments();
        documents.forEach(d -> storage.put(d.getId(), d));

        var resultList = manager.search(request);

        assertEquals(3, resultList.size());

    }

    @Test
    public void searchShouldReturnListOfDocumentsByContents() {
        var contents = List.of("So", "twu");
        var request = SearchRequest.builder().containsContents(contents).build();
        var documents = generateListOfDocuments();
        documents.forEach(d -> storage.put(d.getId(), d));

        var resultList = manager.search(request);

        assertEquals(5, resultList.size());

    }

    @Test
    public void searchShouldReturnListOfDocumentsByAuthor() {
        var authorIds = List.of("11", "2");
        var request = SearchRequest.builder().authorIds(authorIds).build();
        var documents = generateListOfDocuments();
        documents.forEach(d -> storage.put(d.getId(), d));

        var resultList = manager.search(request);

        assertEquals(1, resultList.size());
        assertEquals("title 111", resultList.get(0).getTitle());
        assertEquals("3", resultList.get(0).getId());

    }

    @Test
    public void searchShouldReturnListOfDocumentsCreatedUntilGivenTime() {
        Instant to = Instant.parse("2024-01-15T13:00:00Z");
        var request = SearchRequest.builder().createdTo(to).build();
        var documents = generateListOfDocuments();
        documents.forEach(d -> storage.put(d.getId(), d));

        var resultList = manager.search(request);

        assertEquals(1, resultList.size());
        assertEquals("Some", resultList.get(0).getTitle());
        assertEquals("1", resultList.get(0).getId());

    }

    @Test
    public void searchShouldReturnListOfDocumentsCreatedFromGivenTime() {
        Instant from = Instant.parse("2024-01-15T13:00:00Z");
        var request = SearchRequest.builder().createdFrom(from).build();
        var documents = generateListOfDocuments();
        documents.forEach(d -> storage.put(d.getId(), d));

        var resultList = manager.search(request);

        assertEquals(4, resultList.size());

    }

    @Test
    public void searchShouldReturnNoOne() {
        Instant from = Instant.parse("2024-01-15T13:00:00Z");
        var request = SearchRequest.builder()
                .createdFrom(from)
                .titlePrefixes(List.of("Bla Bla"))
                .build();
        var documents = generateListOfDocuments();
        documents.forEach(d -> storage.put(d.getId(), d));

        var resultList = manager.search(request);

        assertEquals(0, resultList.size());

    }


    @Test
    public void findByIdShouldReturnDocumentWithSpecifiedId() {
        assertTrue(manager.findById("123").isPresent());
    }

    private static Document generateDocument() {
        return Document
                .builder()
                .id(null)
                .title("Some title")
                .content("Some content 123")
                .author(Author
                        .builder()
                        .id("1")
                        .name("Ivan")
                        .build())
                .created(Instant.now())
                .build();
    }

    private static List<Document> generateListOfDocuments() {
        var d0 = generateDocument();
        d0.setId("1");
        d0.setTitle("Some");
        d0.setCreated(Instant.parse("2023-01-15T13:00:00Z"));

        var d1 = generateDocument();
        d1.setId("2");
        d1.setTitle("Title");

        var d2 = generateDocument();
        d2.setId("3");
        d2.setAuthor(Author.builder().id("2").name("Ann").build());
        d2.setTitle("title 111");

        var d3 = generateDocument();
        d3.setId("4");
        d3.setContent("One twu three");
        d3.setTitle("1");
        return List.of(d0, d1, d2, d3);
    }

}