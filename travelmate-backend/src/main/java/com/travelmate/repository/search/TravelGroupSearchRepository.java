package com.travelmate.repository.search;

import com.travelmate.document.TravelGroupDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Elasticsearch 여행 그룹 검색 Repository
 */
@Repository
public interface TravelGroupSearchRepository extends ElasticsearchRepository<TravelGroupDocument, String> {

    /**
     * 이름으로 검색 (Ngram 지원)
     */
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^3\", \"name.ngram^2\"], \"type\": \"best_fields\"}}")
    Page<TravelGroupDocument> findByNameWithNgram(String name, Pageable pageable);

    /**
     * 목적지로 검색
     */
    Page<TravelGroupDocument> findByDestinationContaining(String destination, Pageable pageable);

    /**
     * 여행 스타일로 검색
     */
    Page<TravelGroupDocument> findByTravelStyle(String travelStyle, Pageable pageable);

    /**
     * 태그로 검색
     */
    Page<TravelGroupDocument> findByTagsContaining(String tag, Pageable pageable);

    /**
     * 활성 그룹 검색
     */
    Page<TravelGroupDocument> findByIsActiveTrue(Pageable pageable);

    /**
     * 공개 그룹 검색
     */
    Page<TravelGroupDocument> findByIsPublicTrue(Pageable pageable);

    /**
     * 생성일 기준 최신 그룹
     */
    List<TravelGroupDocument> findTop10ByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * 특정 기간 내 그룹 검색
     */
    Page<TravelGroupDocument> findByStartDateBetween(
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );
}
