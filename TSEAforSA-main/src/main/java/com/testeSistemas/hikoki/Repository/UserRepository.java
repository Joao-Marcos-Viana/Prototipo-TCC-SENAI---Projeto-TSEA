package com.testeSistemas.hikoki.Repository;

import com.testeSistemas.hikoki.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
}
