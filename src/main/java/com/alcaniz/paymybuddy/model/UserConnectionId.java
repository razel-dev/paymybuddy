package com.alcaniz.paymybuddy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter // génére automatiqment des methodes set/get pour les champs mais
       // n'influence pas le comportement de persistance d'ou implement serializable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Embeddable // @Embeddable utilisé comme ID (EmbeddedId/IdClass) => Serializable requis.
           // exigence jpa pour que le transport/stockage fonctionne correctement.


public class UserConnectionId implements Serializable {


    @Column(name = "owner_user_id", nullable = false)
    private Integer ownerUserId;

    @Column(name = "related_user_id", nullable = false)
    private Integer relatedUserId;
}