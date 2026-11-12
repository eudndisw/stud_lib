package com.library.studentlibrary.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageUtilTest {

    @Test
    void pageSize_isLimitedTo50() {
        Pageable pageable = PageUtil.of(0, 1000, Sort.unsorted());
        assertEquals(PageUtil.MAX_PAGE_SIZE, pageable.getPageSize());
        assertEquals(50, pageable.getPageSize());
    }

    @Test
    void invalidPageAndSize_fallBackToDefaults() {
        Pageable pageable = PageUtil.of(-3, 0, Sort.unsorted());
        assertEquals(0, pageable.getPageNumber());
        assertEquals(PageUtil.DEFAULT_PAGE_SIZE, pageable.getPageSize());
    }

    @Test
    void likePatterns_splitWordsAndLowerCase() {
        List<String> patterns = PageUtil.likePatterns("  Франко   Іван ");
        assertEquals(List.of("%франко%", "%іван%"), patterns);
    }

    @Test
    void likePatterns_escapeWildcards() {
        List<String> patterns = PageUtil.likePatterns("100%_");
        assertEquals(List.of("%100\\%\\_%"), patterns);
    }

    @Test
    void likePatterns_blankQuery_returnsEmptyList() {
        assertTrue(PageUtil.likePatterns("   ").isEmpty());
        assertTrue(PageUtil.likePatterns(null).isEmpty());
    }

    @Test
    void direction_defaultsToAscending() {
        assertEquals(Sort.Direction.ASC, PageUtil.direction(null));
        assertEquals(Sort.Direction.ASC, PageUtil.direction("whatever"));
        assertEquals(Sort.Direction.DESC, PageUtil.direction("DESC"));
    }
}
