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
@NoArgsConstructor
@ToString(exclude = "password")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "users")
public class User {


    @EqualsAndHashCode.Include
    @Id // désigne l'attribut id comme identifiant de l'entité
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Indique comment la clé est générée par la base. En l'espèce, Id est auto-incrementé coté base (MySQL/MariaDB).
    @Column(name = "id") // Colonne de la table users. @Column set au mapping des colonnes
    private Integer id;

    @Column(name = "username", nullable = false, length = 100)
    // "unique = true" retiré pour s'aligner sur le schéma actuel (pas de contrainte UNIQUE en base)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    // L'email est unique selon le schéma initial (UNIQUE).
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

}