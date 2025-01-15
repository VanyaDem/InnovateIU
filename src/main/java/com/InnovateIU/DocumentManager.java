package com.InnovateIU;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc.
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final Map<String, Document> storage = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        DocumentSaver saver = DocumentSaverFactory.getInstance().getDocumentSaver(document);
        return saver.saveDocument(document, storage); // Polymorphism GRASP pattern
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return storage
                .values()
                .stream()
                .filter(document -> isDocumentMatchingRequest(document, request))
                .collect(Collectors.toList());
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        Document document = storage.get(id);
        return Optional.of(document);
    }

    private boolean isDocumentMatchingRequest(Document document, SearchRequest request) {
        return Optional.of(document)
                .filter(d -> isMatching(request.getTitlePrefixes(), d.getTitle()::startsWith))
                .filter(d -> isMatching(request.getContainsContents(), d.getContent()::contains))
                .filter(d -> isMatching(request.getAuthorIds(), d.getAuthor().getId()::equals))
                .filter(d -> isCreatedBefore(d.getCreated(), request.getCreatedTo()))
                .filter(d -> isCreatedAfter(d.getCreated(), request.getCreatedFrom()))
                .isPresent();
    }

    private boolean isMatching(List<String> requestsAttributes, Predicate<String> predicate) {
        return requestsAttributes == null || requestsAttributes.stream().anyMatch(predicate);
    }

    private boolean isCreatedBefore(Instant created, Instant to) {
        return to == null || created.isBefore(to);
    }

    private boolean isCreatedAfter(Instant created, Instant from) {
        return from == null || created.isAfter(from);
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }

    /**
     * Defines a method for saving a document to storage.
     * This is part of the Polymorphism GRASP pattern, as different
     * implementations handle the document-saving behavior depending on the state of the document.
     */
    public interface DocumentSaver {
        Document saveDocument(Document document, Map<String, Document> storage);
    }

    public static class NewDocumentSaver implements DocumentSaver {
        @Override
        public Document saveDocument(Document document, Map<String, Document> storage) {
            String id = UUID.randomUUID().toString();
            document.setId(id);
            storage.put(id, document);
            return document;
        }
    }

    public static class ExistedDocumentSaver implements DocumentSaver {
        @Override
        public Document saveDocument(Document document, Map<String, Document> storage) {
            return storage.put(document.getId(), document);
        }
    }

    public static class DocumentSaverFactory {

        private static final DocumentSaverFactory INSTANCE = new DocumentSaverFactory();

        private DocumentSaverFactory() {}

        public static DocumentSaverFactory getInstance() {
            return INSTANCE;
        }

        public DocumentSaver getDocumentSaver(Document document) {
            return document.getId() == null ? new NewDocumentSaver() : new ExistedDocumentSaver();
        }
    }
}