package com.travelmate.service;

import com.travelmate.dto.TravelGroupDto;
import com.travelmate.entity.TravelGroup;
import com.travelmate.repository.TravelGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Redis 캐싱이 적용된 여행 그룹 서비스 예시
 *
 * @Cacheable: 캐시에서 먼저 조회, 없으면 메서드 실행 후 캐시 저장
 * @CachePut: 항상 메서드 실행하고 결과를 캐시에 저장 (업데이트용)
 * @CacheEvict: 캐시 무효화
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CachedTravelGroupService {

    private final TravelGroupRepository travelGroupRepository;

    /**
     * 그룹 상세 조회 (캐싱)
     * - 캐시 키: travelGroupDetails::groupId
     * - TTL: 10분
     */
    @Cacheable(value = "travelGroupDetails", key = "#groupId")
    @Transactional(readOnly = true)
    public TravelGroupDto.DetailResponse getGroupDetail(Long groupId) {
        log.info("Cache miss - Fetching group detail from database: {}", groupId);

        TravelGroup group = travelGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        return convertToDetailResponse(group);
    }

    /**
     * 모든 그룹 조회 (캐싱)
     * - 캐시 키: travelGroups::all
     * - TTL: 5분
     */
    @Cacheable(value = "travelGroups", key = "'all'")
    @Transactional(readOnly = true)
    public List<TravelGroupDto.Response> getAllGroups() {
        log.info("Cache miss - Fetching all groups from database");

        return travelGroupRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 그룹 검색 (캐싱)
     * - 캐시 키: searchResults::purpose-lat-lng-radius
     * - TTL: 3분
     */
    @Cacheable(
            value = "searchResults",
            key = "#purpose + '-' + #latitude + '-' + #longitude + '-' + #radiusKm",
            condition = "#latitude != null && #longitude != null"
    )
    @Transactional(readOnly = true)
    public List<TravelGroupDto.Response> searchGroups(
            String purpose,
            Double latitude,
            Double longitude,
            Double radiusKm) {

        log.info("Cache miss - Searching groups: purpose={}, location=({},{}), radius={}",
                purpose, latitude, longitude, radiusKm);

        // 실제 검색 로직 (Repository에서 처리)
        List<TravelGroup> groups = travelGroupRepository.findAvailableGroups(
                purpose, latitude, longitude, radiusKm
        );

        return groups.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 그룹 생성 (캐시 무효화)
     * - 전체 그룹 목록 캐시 삭제
     * - 검색 결과 캐시 전체 삭제
     */
    @Caching(evict = {
            @CacheEvict(value = "travelGroups", key = "'all'"),
            @CacheEvict(value = "searchResults", allEntries = true)
    })
    @Transactional
    public TravelGroupDto.Response createGroup(TravelGroupDto.CreateRequest request, Long userId) {
        log.info("Creating new group and invalidating caches");

        TravelGroup group = new TravelGroup();
        // ... 그룹 생성 로직

        TravelGroup savedGroup = travelGroupRepository.save(group);
        return convertToResponse(savedGroup);
    }

    /**
     * 그룹 업데이트 (캐시 갱신 + 무효화)
     * - 상세 캐시 갱신 (@CachePut)
     * - 목록 캐시 무효화 (@CacheEvict)
     */
    @Caching(
            put = @CachePut(value = "travelGroupDetails", key = "#groupId"),
            evict = {
                    @CacheEvict(value = "travelGroups", key = "'all'"),
                    @CacheEvict(value = "searchResults", allEntries = true)
            }
    )
    @Transactional
    public TravelGroupDto.DetailResponse updateGroup(Long groupId, TravelGroupDto.UpdateRequest request) {
        log.info("Updating group and refreshing caches: {}", groupId);

        TravelGroup group = travelGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // ... 그룹 업데이트 로직

        TravelGroup updatedGroup = travelGroupRepository.save(group);
        return convertToDetailResponse(updatedGroup);
    }

    /**
     * 그룹 삭제 (모든 관련 캐시 무효화)
     */
    @Caching(evict = {
            @CacheEvict(value = "travelGroupDetails", key = "#groupId"),
            @CacheEvict(value = "travelGroups", key = "'all'"),
            @CacheEvict(value = "searchResults", allEntries = true)
    })
    @Transactional
    public void deleteGroup(Long groupId) {
        log.info("Deleting group and clearing all related caches: {}", groupId);
        travelGroupRepository.deleteById(groupId);
    }

    /**
     * 그룹 가입 (캐시 무효화)
     * - 해당 그룹 상세 캐시 삭제
     * - 전체 목록 캐시 삭제
     */
    @Caching(evict = {
            @CacheEvict(value = "travelGroupDetails", key = "#groupId"),
            @CacheEvict(value = "travelGroups", key = "'all'")
    })
    @Transactional
    public void joinGroup(Long groupId, Long userId) {
        log.info("User {} joining group {} and invalidating caches", userId, groupId);
        // ... 가입 로직
    }

    // Helper methods
    private TravelGroupDto.Response convertToResponse(TravelGroup group) {
        // 변환 로직
        return new TravelGroupDto.Response();
    }

    private TravelGroupDto.DetailResponse convertToDetailResponse(TravelGroup group) {
        // 변환 로직
        return new TravelGroupDto.DetailResponse();
    }
}
