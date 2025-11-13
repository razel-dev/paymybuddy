package com.alcaniz.paymybuddy.model;


import jakarta.persistence.*; // usage de l'API JPA pour le mapping
import lombok.*;
import java.time.Instant;

/**
 * Entité JPA représentant un utilisateur de l'application.
 * - Persistée dans la table "users".
 * - Lombok génère le code standard (getters/setters, builder, etc.).
 * - L'horodatage de création est géré par la base (DEFAULT CURRENT_TIMESTAMP).
 */
@Getter
@Setter
@Entity //classe persistable mappée sur une table.
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Constructeur requis par JPA; "protected" évite une instanciation involontaire côté application.

@ToString(exclude = "password") // Évite toute fuite du mot de passe dans les logs.
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Ne prend en compte que les champs annotés @Include (ici: id) pour equals/hashCode.
// L'égalité uniquement sur l'id afin d'éviter des effets de bord en persistance.
@Table(name = "users") // Nom explicite de la table. Associe l’entité à la table users.

        public class User {

    @Builder(toBuilder = true)
    private User(@NonNull String username, @NonNull String email, @NonNull String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @EqualsAndHashCode.Include
    @Id // désigne l'attribut id comme identifiant de l'entité
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Indique comment la clé est générée par la base. En l'espèce, Id est auto-incrementé coté base (MySQL/MariaDB).
    @Column(name = "id") // Colonne de la table users. @Column set au mapping des colonnes
    private Integer id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    // "unique = true" reflète l'intention métier d'un username unique.
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    // L'email est unique selon le schéma initial (UNIQUE).
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    // Valeur générée par la base (DEFAULT CURRENT_TIMESTAMP).
    // "insertable=false, updatable=false" indique à JPA/Hibernate de ne pas gérer ce champ en écriture.

    private Instant createdAt;
}