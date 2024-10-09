package com.thecoders.cartunnbackend.iam.interfaces.rest;

import com.thecoders.cartunnbackend.iam.domain.model.commands.DeleteUserCommand;
import com.thecoders.cartunnbackend.iam.domain.model.queries.GetAllUsersQuery;
import com.thecoders.cartunnbackend.iam.domain.model.queries.GetUserByIdQuery;
import com.thecoders.cartunnbackend.iam.domain.services.UserCommandService;
import com.thecoders.cartunnbackend.iam.domain.services.UserQueryService;
import com.thecoders.cartunnbackend.iam.interfaces.rest.resources.UpdateUserResource;
import com.thecoders.cartunnbackend.iam.interfaces.rest.resources.UserResource;
import com.thecoders.cartunnbackend.iam.interfaces.rest.transform.UpdateUserCommandFromResourceAssembler;
import com.thecoders.cartunnbackend.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "User Management Endpoints")
public class UsersController {
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    public UsersController(UserCommandService userCommandService, UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
        this.userCommandService = userCommandService;
    }

    @GetMapping
    public ResponseEntity<List<UserResource>> getAllUsers() {
        var getAllUsersQuery = new GetAllUsersQuery();
        var users = userQueryService.handle(getAllUsersQuery);
        var userResources = users.stream().map(UserResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(userResources);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResource> getUserById(@PathVariable Long userId) {
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        var user = userQueryService.handle(getUserByIdQuery);
        if (user.isEmpty()) return ResponseEntity.notFound().build();
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return ResponseEntity.ok(userResource);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UpdateUserResource updateUserResource) {
        var updateUserCommand = UpdateUserCommandFromResourceAssembler.toCommandFromResource(userId, updateUserResource);
        var updatedUser = userCommandService.handle(updateUserCommand);

        if (updatedUser.isEmpty()) return ResponseEntity.badRequest().build();

        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(updatedUser.get());
        return ResponseEntity.ok(userResource);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        var deleteUserCommand = new DeleteUserCommand(userId);

        userCommandService.handle(deleteUserCommand);

        return ResponseEntity.ok("User deleted successfully");
    }
}
