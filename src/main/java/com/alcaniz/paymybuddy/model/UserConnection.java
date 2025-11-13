package com.alcaniz.paymybuddy.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "user_connections")
public class UserConnection {



    @EqualsAndHashCode.Include
    @EmbeddedId
    private UserConnectionId id;

    @MapsId("ownerUserId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @MapsId("relatedUserId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "related_user_id", nullable = false)
    private User related;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}