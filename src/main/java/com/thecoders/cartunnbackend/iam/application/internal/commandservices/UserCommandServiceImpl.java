package com.thecoders.cartunnbackend.iam.application.internal.commandservices;

import com.thecoders.cartunnbackend.iam.application.internal.outboundservices.hashing.HashingService;
import com.thecoders.cartunnbackend.iam.application.internal.outboundservices.tokens.TokenService;
import com.thecoders.cartunnbackend.iam.domain.model.aggregates.User;
import com.thecoders.cartunnbackend.iam.domain.model.commands.DeleteUserCommand;
import com.thecoders.cartunnbackend.iam.domain.model.commands.SignInCommand;
import com.thecoders.cartunnbackend.iam.domain.model.commands.SignUpCommand;
import com.thecoders.cartunnbackend.iam.domain.model.commands.UpdateUserCommand;
import com.thecoders.cartunnbackend.iam.domain.model.valueobjects.Roles;
import com.thecoders.cartunnbackend.iam.domain.services.UserCommandService;
import com.thecoders.cartunnbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.thecoders.cartunnbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserCommandServiceImpl implements UserCommandService {
    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final RoleRepository roleRepository;

    public UserCommandServiceImpl(UserRepository userRepository, HashingService hashingService, TokenService tokenService, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByUsername(command.username()))
            throw new RuntimeException("Username already exists");

        var roles = command.roles();

        if (roles.isEmpty()) {
            var role = roleRepository.findByName(Roles.ROLE_CLIENT);
            roles.add(role.get());
        }

        roles = command.roles().stream().map(role -> roleRepository.findByName(role.getName()).orElseThrow(() -> new RuntimeException("Role not found"))).toList();

        var user = new User(command.username(), hashingService.encode(command.password()), roles);

        userRepository.save(user);

        return userRepository.findByUsername(command.username());
    }

    @Override
    public Optional<User> handle(UpdateUserCommand command) {
        // Verify if the user exists
        var user = userRepository.findById(command.id());

        if (user.isEmpty()) throw new RuntimeException("User not found");

        var userToUpdate = user.get();

        // Verify if roles are valid
        var roles = command.roles().stream().map(role -> roleRepository.findByName(role.getName()).orElseThrow(() -> new RuntimeException("Role not found"))).toList();

        // Verify if the username already exists
        if (userRepository.findByUsername(command.username()).isPresent() &&
                !userToUpdate.getUsername().equals(command.username()))
            throw new RuntimeException("Username already exists");

        try {
            var updatedUser = userToUpdate.updateInformation(command.username(), hashingService.encode(command.password()), roles);
            var savedUser = userRepository.save(updatedUser);
            return Optional.of(savedUser);
        } catch (Exception e) {
            throw new RuntimeException("Error while updating user: " + e.getMessage());
        }
    }

    @Override
    public void handle(DeleteUserCommand command) {
        var user = userRepository.findById(command.id());

        if (user.isEmpty()) throw new RuntimeException("User not found");

        try {
            userRepository.deleteById(user.get().getId());
        } catch (Exception e) {
            throw new RuntimeException("Error while deleting user: " + e.getMessage());
        }

    }

    @Override
    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {
        var user = userRepository.findByUsername(command.username());

        if (user.isEmpty()) throw new RuntimeException("User not found");

        if (!hashingService.matches(command.password(), user.get().getPassword()))
            throw new RuntimeException("Invalid password");

        var currentUser = user.get();

        var token = tokenService.generateToken(currentUser.getUsername());

        return Optional.of(ImmutablePair.of(currentUser, token));
    }
}
