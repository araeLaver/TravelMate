package com.travelmate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_group_id")
    private TravelGroup travelGroup;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Enumerated(EnumType.STRING)
    private Role role = Role.MEMBER;
    
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;
    
    @CreationTimestamp
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
    
    public enum Role {
        CREATOR, MEMBER
    }
    
    public enum Status {
        PENDING, ACCEPTED, REJECTED
    }
}