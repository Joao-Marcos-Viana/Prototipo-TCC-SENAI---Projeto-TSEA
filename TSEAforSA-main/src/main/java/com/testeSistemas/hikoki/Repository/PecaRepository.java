package com.testeSistemas.hikoki.Repository;

import com.testeSistemas.hikoki.Entity.PecaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PecaRepository extends JpaRepository<PecaEntity, Integer> {

    Optional<PecaEntity> findByVersao(Double versao);

}
