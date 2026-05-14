package com.hireconnect.auth.resource;

import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth/admin/users")
@RequiredArgsConstructor
public class AdminResource {

    private final AuthService authService;


    @GetMapping
    public ResponseEntity<List<UserCredential>> manageUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    // Edit user details
    @PutMapping("/{id}")
    public ResponseEntity<UserCredential> editUser(@PathVariable int id, @RequestBody UserCredential userDetails) {
        return ResponseEntity.ok(authService.updateUser(id, userDetails));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        authService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{id}/suspend")
    public ResponseEntity<UserCredential> suspendUser(@PathVariable int id, @RequestParam boolean suspend) {
        return ResponseEntity.ok(authService.suspendUser(id, suspend));
    }
}
