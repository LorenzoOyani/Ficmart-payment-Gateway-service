package com.org.persistence.repository;

import com.org.persistence.entities.IdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyEntity, Long> {

    Optional<IdempotencyEntity> findByIdempotencyKeyAndEndpoint(String key,  String endpoint);
}
