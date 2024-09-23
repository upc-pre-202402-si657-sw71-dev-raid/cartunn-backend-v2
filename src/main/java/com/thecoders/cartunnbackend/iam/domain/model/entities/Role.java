package com.thecoders.cartunnbackend.iam.domain.model.entities;

import com.thecoders.cartunnbackend.iam.domain.model.valueobjects.Roles;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.With;

import java.util.List;

@Getter
@Entity
@Data
@AllArgsConstructor
@With
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, length = 20)
    private Roles name;

    public Role() {
    }

    public Role(Roles name) {
        this.name = name;
    }

    public static Role getDefaultRole() {
        return new Role(Roles.ROLE_CLIENT);
    }

    public static Role toRoleFromName(String name) {
        return new Role(Roles.valueOf(name));
    }

    public static List<Role> validateRoleSet(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of(getDefaultRole());
        }

        return roles;
    }

    public String getStringName() {
        return name.name();
    }
}
