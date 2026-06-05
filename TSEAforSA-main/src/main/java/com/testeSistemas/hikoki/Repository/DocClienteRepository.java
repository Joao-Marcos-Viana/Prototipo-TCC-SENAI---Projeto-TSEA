package com.testeSistemas.hikoki.Repository;

import com.testeSistemas.hikoki.Entity.DocCliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocClienteRepository extends JpaRepository<DocCliente, Integer> {
}
