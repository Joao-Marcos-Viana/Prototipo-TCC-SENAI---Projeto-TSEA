package com.testeSistemas.hikoki.Controller;

import com.testeSistemas.hikoki.Entity.UserEntity;
import com.testeSistemas.hikoki.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<UserEntity> listarUsuarios() {
        return userService.listarUsuarios();
    }

    @PostMapping("/novoUser")
    public ResponseEntity<UserEntity> adicionarUsuario(@RequestBody UserEntity userEntity) {
        return ResponseEntity.ok(userService.adicionarUsuario(userEntity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserEntity> atualizarUsuario(@PathVariable Integer id,
                                                       @RequestBody UserEntity userEntity) {
        return ResponseEntity.ok(userService.atualizarUsuario(id, userEntity));
    }

    @DeleteMapping("/{id}")
    public void desligarUsuario(@PathVariable Integer id) {
        userService.desligarUsuario(id);
    }

}
