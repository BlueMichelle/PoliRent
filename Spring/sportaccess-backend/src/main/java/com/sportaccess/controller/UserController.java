package com.sportaccess.controller;

import com.sportaccess.model.User;
import com.sportaccess.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    /** GET /api/users — Lista todos los usuarios (Admin) */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /** GET /api/users/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/users/register — Registrar usuario tras login Firebase
     * Cuando el usuario hace login en Firebase, la app llama a este endpoint
     * para guardar/sincronizar el usuario en nuestra base de datos MySQL.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> body) {
        String firebaseUid = body.get("firebaseUid");
        String email = body.get("email");
        String nombre = body.get("nombre");
        String telefono = body.getOrDefault("telefono", "");

        // Si ya existe, devolver el usuario existente
        if (userRepository.existsByEmail(email)) {
            return userRepository.findByEmail(email)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        }

        User user = new User();
        user.setFirebaseUid(firebaseUid);
        user.setEmail(email);
        user.setNombre(nombre);
        user.setTelefono(telefono);
        user.setRol(User.Role.CLIENTE);

        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /** GET /api/users/firebase/{uid} — Buscar por UID de Firebase */
    @GetMapping("/firebase/{uid}")
    public ResponseEntity<User> getByFirebaseUid(@PathVariable String uid) {
        return userRepository.findByFirebaseUid(uid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
