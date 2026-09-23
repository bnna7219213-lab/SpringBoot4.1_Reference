package com.example.vadv.model;

import java.time.LocalDate;
import java.util.Map;

/**
 * Represents metadata attached to a document in the vector store.
 * Used for filtering, categorization, and source tracking.
 *
 * @param source   the original source file or URL
 * @param category the document category (e.g., "research", "guide", "overview")
 * @param author   the author of the document
 * @param date     the publication/ingestion date
 * @param language the document language (ISO 639-1 code)
 * @param tags     additional free-form tags for filtering
 */
public record DocumentMetadata(
        String source,
        String category,
        String author,
        LocalDate date,
        String language,
        String[] tags
) {
    /**
     * Converts this metadata to a Map for storage in Spring AI Document metadata.
     */
    public Map<String, Object> toMap() {
        var map = new java.util.HashMap<String, Object>();
        map.put("source", source);
        map.put("category", category);
        map.put("author", author);
        map.put("date", date.toString());
        map.put("language", language);
        if (tags != null && tags.length > 0) {
            map.put("tags", java.util.Arrays.toString(tags));
        }
        return map;
    }

    /**
     * Creates a DocumentMetadata from a Map (reverse of toMap).
     */
    public static DocumentMetadata fromMap(Map<String, Object> map) {
        var tagsStr = map.getOrDefault("tags", "").toString();
        String[] parsedTags = tagsStr.isEmpty() ? new String[0] : tagsStr.split(", ");

        return new DocumentMetadata(
                (String) map.getOrDefault("source", ""),
                (String) map.getOrDefault("category", ""),
                (String) map.getOrDefault("author", ""),
                LocalDate.parse(map.getOrDefault("date", LocalDate.now().toString()).toString()),
                (String) map.getOrDefault("language", "en"),
                parsedTags
        );
    }
}
