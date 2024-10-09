package com.thecoders.cartunnbackend.iam.domain.model.commands;

import com.thecoders.cartunnbackend.iam.domain.model.entities.Role;

import java.util.List;

public record UpdateUserCommand(Long id, String username, String password, List<Role> roles) {
}
