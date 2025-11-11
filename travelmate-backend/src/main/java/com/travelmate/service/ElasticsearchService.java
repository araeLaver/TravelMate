package com.travelmate.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.travelmate.document.TravelGroupDocument;
import com.travelmate.dto.SearchRequestDto;
import com.travelmate.dto.SearchResultDto;
import com.travelmate.entity.TravelGroup;
import com.travelmate.repository.TravelGroupRepository;
import com.travelmate.repository.search.TravelGroupSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Elasticsearch 검색 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchService {

    private final TravelGroupSearchRepository searchRepository;
    private final TravelGroupRepository travelGroupRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 고급 검색 (Multi-field, Fuzzy, Boosting)
     */
    public SearchResultDto advancedSearch(SearchRequestDto request) {
        try {
            // Query 빌더 생성
            NativeQueryBuilder queryBuilder = new NativeQueryBuilder();

            // Bool Query 구성
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

            // 1. 텍스트 검색 (이름, 설명, 목적지)
            if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
                MultiMatchQuery multiMatchQuery = MultiMatchQuery.of(m -> m
                    .query(request.getKeyword())
                    .fields("name^3", "name.ngram^2", "description^2", "destination^2")
                    .type(TextQueryType.BestFields)
                    .fuzziness("AUTO")
                );
                boolQueryBuilder.must(Query.of(q -> q.multiMatch(multiMatchQuery)));
            }

            // 2. 여행 스타일 필터
            if (request.getTravelStyle() != null && !request.getTravelStyle().isEmpty()) {
                TermQuery termQuery = TermQuery.of(t -> t
                    .field("travelStyle")
                    .value(request.getTravelStyle())
                );
                boolQueryBuilder.filter(Query.of(q -> q.term(termQuery)));
            }

            // 3. 태그 필터
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                TermsQuery termsQuery = TermsQuery.of(t -> t
                    .field("tags")
                    .terms(terms -> terms.value(
                        request.getTags().stream()
                            .map(tag -> FieldValue.of(tag))
                            .collect(Collectors.toList())
                    ))
                );
                boolQueryBuilder.filter(Query.of(q -> q.terms(termsQuery)));
            }

            // 4. 멤버 수 범위
            if (request.getMinMembers() != null || request.getMaxMembers() != null) {
                RangeQuery.Builder rangeBuilder = new RangeQuery.Builder().field("currentMembers");

                if (request.getMinMembers() != null) {
                    rangeBuilder.gte(JsonData.of(request.getMinMembers()));
                }
                if (request.getMaxMembers() != null) {
                    rangeBuilder.lte(JsonData.of(request.getMaxMembers()));
                }

                boolQueryBuilder.filter(Query.of(q -> q.range(rangeBuilder.build())));
            }

            // 5. 날짜 범위
            if (request.getStartDate() != null || request.getEndDate() != null) {
                RangeQuery.Builder rangeBuilder = new RangeQuery.Builder().field("startDate");

                if (request.getStartDate() != null) {
                    rangeBuilder.gte(JsonData.of(request.getStartDate().toString()));
                }
                if (request.getEndDate() != null) {
                    rangeBuilder.lte(JsonData.of(request.getEndDate().toString()));
                }

                boolQueryBuilder.filter(Query.of(q -> q.range(rangeBuilder.build())));
            }

            // 6. 지리적 검색 (위도/경도 반경)
            if (request.getLatitude() != null && request.getLongitude() != null && request.getRadius() != null) {
                GeoDistanceQuery geoQuery = GeoDistanceQuery.of(g -> g
                    .field("location")
                    .distance(request.getRadius() + "km")
                    .location(loc -> loc.latlon(latlon -> latlon
                        .lat(request.getLatitude())
                        .lon(request.getLongitude())
                    ))
                );
                boolQueryBuilder.filter(Query.of(q -> q.geoDistance(geoQuery)));
            }

            // 7. 활성/공개 그룹만
            boolQueryBuilder.filter(Query.of(q -> q.term(t -> t.field("isActive").value(true))));
            boolQueryBuilder.filter(Query.of(q -> q.term(t -> t.field("isPublic").value(true))));

            // Query 설정
            queryBuilder.withQuery(Query.of(q -> q.bool(boolQueryBuilder.build())));

            // 정렬
            String sortField = request.getSortBy() != null ? request.getSortBy() : "createdAt";
            SortOrder sortOrder = "asc".equalsIgnoreCase(request.getSortOrder())
                ? SortOrder.Asc
                : SortOrder.Desc;
            queryBuilder.withSort(s -> s.field(f -> f.field(sortField).order(sortOrder)));

            // 페이징
            Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 20
            );
            queryBuilder.withPageable(pageable);

            // 검색 실행
            NativeQuery query = queryBuilder.build();
            SearchHits<TravelGroupDocument> searchHits = elasticsearchOperations.search(query, TravelGroupDocument.class);

            // 결과 변환
            List<SearchResultDto.GroupResult> results = searchHits.getSearchHits().stream()
                .map(hit -> convertToGroupResult(hit))
                .collect(Collectors.toList());

            return SearchResultDto.builder()
                .results(results)
                .totalResults(searchHits.getTotalHits())
                .page(request.getPage() != null ? request.getPage() : 0)
                .size(results.size())
                .took(searchHits.getSearchHits().isEmpty() ? 0 :
                      searchHits.getSearchHits().get(0).getScore())
                .build();

        } catch (Exception e) {
            log.error("Advanced search failed", e);
            throw new RuntimeException("검색 실행 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 자동완성 검색
     */
    public List<String> autocomplete(String prefix) {
        if (prefix == null || prefix.length() < 2) {
            return new ArrayList<>();
        }

        try {
            NativeQuery query = new NativeQueryBuilder()
                .withQuery(Query.of(q -> q.match(m -> m
                    .field("name.ngram")
                    .query(prefix)
                )))
                .withMaxResults(10)
                .build();

            SearchHits<TravelGroupDocument> hits = elasticsearchOperations.search(query, TravelGroupDocument.class);

            return hits.getSearchHits().stream()
                .map(hit -> hit.getContent().getName())
                .distinct()
                .limit(10)
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Autocomplete search failed", e);
            return new ArrayList<>();
        }
    }

    /**
     * 인기 검색어 (최근 생성된 그룹의 태그)
     */
    public List<String> getPopularTags() {
        try {
            List<TravelGroupDocument> recentGroups = searchRepository.findTop10ByIsActiveTrueOrderByCreatedAtDesc();

            return recentGroups.stream()
                .flatMap(group -> group.getTags() != null ? group.getTags().stream() : null)
                .distinct()
                .limit(10)
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to get popular tags", e);
            return new ArrayList<>();
        }
    }

    /**
     * 전체 재색인 (Full Reindex)
     */
    @Async
    @Transactional(readOnly = true)
    public void reindexAllGroups() {
        log.info("Starting full reindex of travel groups...");

        try {
            // 기존 인덱스 삭제
            searchRepository.deleteAll();

            // DB에서 모든 그룹 조회
            List<TravelGroup> groups = travelGroupRepository.findAll();

            // Elasticsearch에 저장
            List<TravelGroupDocument> documents = groups.stream()
                .map(this::convertToDocument)
                .collect(Collectors.toList());

            searchRepository.saveAll(documents);

            log.info("Reindexing completed. Total groups indexed: {}", documents.size());

        } catch (Exception e) {
            log.error("Reindexing failed", e);
        }
    }

    /**
     * 단일 그룹 색인
     */
    public void indexGroup(TravelGroup group) {
        try {
            TravelGroupDocument document = convertToDocument(group);
            searchRepository.save(document);
            log.debug("Group indexed: {}", group.getId());
        } catch (Exception e) {
            log.error("Failed to index group: {}", group.getId(), e);
        }
    }

    /**
     * 그룹 색인 삭제
     */
    public void deleteGroupIndex(Long groupId) {
        try {
            searchRepository.deleteById(groupId.toString());
            log.debug("Group index deleted: {}", groupId);
        } catch (Exception e) {
            log.error("Failed to delete group index: {}", groupId, e);
        }
    }

    /**
     * TravelGroup -> TravelGroupDocument 변환
     */
    private TravelGroupDocument convertToDocument(TravelGroup group) {
        TravelGroupDocument.GeoPoint location = null;
        if (group.getLatitude() != null && group.getLongitude() != null) {
            location = TravelGroupDocument.GeoPoint.of(
                group.getLatitude(),
                group.getLongitude()
            );
        }

        return TravelGroupDocument.builder()
            .id(group.getId().toString())
            .name(group.getName())
            .description(group.getDescription())
            .destination(group.getDestination())
            .travelStyle(group.getTravelStyle())
            .tags(group.getTags() != null ?
                  List.of(group.getTags().split(",")) :
                  new ArrayList<>())
            .currentMembers(group.getCurrentMembers())
            .maxMembers(group.getMaxMembers())
            .startDate(group.getStartDate())
            .endDate(group.getEndDate())
            .createdAt(group.getCreatedAt())
            .location(location)
            .isPublic(group.getIsPublic())
            .isActive(true)
            .creatorId(group.getCreator() != null ? group.getCreator().getId() : null)
            .creatorName(group.getCreator() != null ? group.getCreator().getNickname() : null)
            .build();
    }

    /**
     * SearchHit -> GroupResult 변환
     */
    private SearchResultDto.GroupResult convertToGroupResult(SearchHit<TravelGroupDocument> hit) {
        TravelGroupDocument doc = hit.getContent();

        return SearchResultDto.GroupResult.builder()
            .id(Long.parseLong(doc.getId()))
            .name(doc.getName())
            .description(doc.getDescription())
            .destination(doc.getDestination())
            .travelStyle(doc.getTravelStyle())
            .tags(doc.getTags())
            .currentMembers(doc.getCurrentMembers())
            .maxMembers(doc.getMaxMembers())
            .startDate(doc.getStartDate())
            .endDate(doc.getEndDate())
            .createdAt(doc.getCreatedAt())
            .score(hit.getScore())
            .build();
    }
}
