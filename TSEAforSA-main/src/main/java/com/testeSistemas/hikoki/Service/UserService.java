package com.testeSistemas.hikoki.Service;

import com.testeSistemas.hikoki.Entity.UserEntity;
import com.testeSistemas.hikoki.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<UserEntity> listarUsuarios() {
        Sort sort = Sort.by("setor").descending()
                .and(Sort.by("nomeUser"));
        return userRepository.findAll(sort);
    }

    public UserEntity adicionarUsuario(UserEntity userEntity) {
        String senhaCrip = passwordEncoder.encode(userEntity.getKey());
        userEntity.setKey(senhaCrip);
        return userRepository.save(userEntity);
    }

    public UserEntity atualizarUsuario(Integer id, UserEntity userEntity) {
        UserEntity userExistente = userRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
        userExistente.setNomeUser(userEntity.getNomeUser());
        userExistente.setKey(passwordEncoder.encode(userEntity.getKey()));
        userExistente.setSetor(userEntity.getSetor());
        return userRepository.save(userExistente);
    }

    public void desligarUsuario(Integer id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
        userRepository.delete(user);
    }
}