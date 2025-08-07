package com.travelmate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "travel_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelGroup {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Purpose purpose;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;
    
    @OneToMany(mappedBy = "travelGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GroupMember> members;
    
    @Column(name = "max_members")
    private Integer maxMembers = 4;
    
    @Column(name = "meeting_latitude")
    private Double meetingLatitude;
    
    @Column(name = "meeting_longitude")
    private Double meetingLongitude;
    
    @Column(name = "meeting_address")
    private String meetingAddress;
    
    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;
    
    @Enumerated(EnumType.STRING)
    private Status status = Status.RECRUITING;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public enum Purpose {
        TRANSPORTATION, DINING, SIGHTSEEING, ACCOMMODATION, SHOPPING, ACTIVITY
    }
    
    public enum Status {
        RECRUITING, IN_PROGRESS, COMPLETED, CANCELLED
    }
}