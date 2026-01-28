package org.example;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing SAGA state persistence
 */
@Repository
public interface OrderSagaRepository extends MongoRepository<OrderSagaState, String> {

    Optional<OrderSagaState> findByOrderId(String orderId);

    List<OrderSagaState> findByStatus(SagaStatus status);

    List<OrderSagaState> findByStatusIn(List<SagaStatus> statuses);
}