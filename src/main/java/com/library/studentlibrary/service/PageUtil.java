package com.library.studentlibrary.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Спільні допоміжні методи для пагінації, сортування та пошуку. */
public final class PageUtil {

    /** Не більше 50 записів за один запит до сервера. */
    public static final int MAX_PAGE_SIZE = 50;
    public static final int DEFAULT_PAGE_SIZE = 10;
    /** Максимум слів у пошуковому запиті, що враховуються. */
    private static final int MAX_TOKENS = 5;

    private PageUtil() {
    }

    public static Pageable of(int page, int size, Sort sort) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize, sort);
    }

    public static Sort.Direction direction(String value) {
        return "desc".equalsIgnoreCase(value) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    /** Розбиває пошуковий запит на слова та перетворює кожне на шаблон LIKE. */
    public static List<String> likePatterns(String query) {
        List<String> patterns = new ArrayList<>();
        if (query == null || query.isBlank()) {
            return patterns;
        }
        Arrays.stream(query.trim().toLowerCase().split("\\s+"))
                .limit(MAX_TOKENS)
                .forEach(token -> patterns.add("%" + token
                        .replace("\\", "\\\\")
                        .replace("%", "\\%")
                        .replace("_", "\\_") + "%"));
        return patterns;
    }
}
