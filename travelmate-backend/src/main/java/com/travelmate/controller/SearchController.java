package com.travelmate.controller;

import com.travelmate.dto.SearchRequestDto;
import com.travelmate.dto.SearchResultDto;
import com.travelmate.service.ElasticsearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Elasticsearch 검색 REST API
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SearchController {

    private final ElasticsearchService elasticsearchService;

    /**
     * 고급 검색
     * POST /api/search
     */
    @PostMapping
    public ResponseEntity<SearchResultDto> search(@RequestBody SearchRequestDto request) {
        SearchResultDto results = elasticsearchService.advancedSearch(request);
        return ResponseEntity.ok(results);
    }

    /**
     * 간단한 키워드 검색
     * GET /api/search?q=keyword
     */
    @GetMapping
    public ResponseEntity<SearchResultDto> quickSearch(
            @RequestParam(name = "q") String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        SearchRequestDto request = SearchRequestDto.builder()
                .keyword(keyword)
                .page(page)
                .size(size)
                .build();

        SearchResultDto results = elasticsearchService.advancedSearch(request);
        return ResponseEntity.ok(results);
    }

    /**
     * 자동완성
     * GET /api/search/autocomplete?prefix=제주
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(@RequestParam String prefix) {
        List<String> suggestions = elasticsearchService.autocomplete(prefix);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * 인기 태그
     * GET /api/search/popular-tags
     */
    @GetMapping("/popular-tags")
    public ResponseEntity<List<String>> getPopularTags() {
        List<String> tags = elasticsearchService.getPopularTags();
        return ResponseEntity.ok(tags);
    }

    /**
     * 전체 재색인 (관리자 전용)
     * POST /api/search/reindex
     */
    @PostMapping("/reindex")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> reindex() {
        elasticsearchService.reindexAllGroups();
        return ResponseEntity.ok("Reindexing started");
    }
}
