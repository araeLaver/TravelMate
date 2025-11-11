package com.travelmate.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Elasticsearch용 여행 그룹 문서
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "travel_groups")
@Setting(settingPath = "elasticsearch/travel-group-settings.json")
public class TravelGroupDocument {

    @Id
    private String id;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer"),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword),
            @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer")
        }
    )
    private String name;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String description;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer"),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword)
        }
    )
    private String destination;

    @Field(type = FieldType.Keyword)
    private String travelStyle;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Integer)
    private Integer currentMembers;

    @Field(type = FieldType.Integer)
    private Integer maxMembers;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime startDate;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime endDate;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime createdAt;

    @GeoPointField
    private GeoPoint location;

    @Field(type = FieldType.Boolean)
    private Boolean isPublic;

    @Field(type = FieldType.Boolean)
    private Boolean isActive;

    @Field(type = FieldType.Long)
    private Long creatorId;

    @Field(type = FieldType.Text)
    private String creatorName;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeoPoint {
        private Double lat;
        private Double lon;

        public static GeoPoint of(Double lat, Double lon) {
            return new GeoPoint(lat, lon);
        }
    }
}
