package fr.redfroggy.bdd.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
final class UserController {

    private final List<UserDTO> users = new ArrayList<>();

    @GetMapping("/users")
    public List<UserDTO> getAll() {
        return users;
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> get(@PathVariable("id") String id) {
        UserDTO currentUser = users.stream().filter(u -> u.getId()
                .equals(id)).findFirst()
                .orElse(null);
        if (currentUser == null) {
            return ResponseEntity
                    .notFound().build();
        }
        return ResponseEntity.
                ok(currentUser);
    }

    @PostMapping(value = "/users")
    public ResponseEntity<UserDTO> addUser(@RequestBody  UserDTO user) {

        UserDTO currentUser = users.stream().filter(u -> u.getId()
                .equals(user.getId())).findFirst()
                .orElse(null);
        if (currentUser == null) {
            users.add(user);
            return ResponseEntity.status(201)
                    .body(user);
        }
        return ResponseEntity.
                badRequest()
                .build();

    }

    @DeleteMapping(value = "/users/{id}")
    public ResponseEntity<UserDTO> addUser(@PathVariable("id") String id) {

        UserDTO currentUser = users.stream().filter(u -> u.getId()
                .equals(id)).findFirst()
                .orElse(null);
        if (currentUser == null) {
            return ResponseEntity
                    .notFound().build();
        }
        return ResponseEntity.
                ok().build();

    }
}
