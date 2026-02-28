package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class SagaEventListener {
    private static final Logger logger = LoggerFactory.getLogger(SagaEventListener.class);
    private final OrderSagaRepository sagaRepository;

    public SagaEventListener(OrderSagaRepository sagaRepository) {
        this.sagaRepository = sagaRepository;
    }

    @RabbitListener(queues = RabbitMqConfig.SAGA_COMPENSATION_QUEUE)
    public void handleCompensateOrder(CompensateOrderEvent event) {
        logger.warn("❌ Starting Order compensation {} for SAGA {}. Reason: {}",
                event.getOrderId(), event.getSagaId(), event.getReason());

        try {
            OrderSagaState saga = sagaRepository.findByOrderId(event.getOrderId()).orElse(null);
            if (saga != null) {
                logger.info("💸 Issuing refund for customer: {}", saga.getCustomerId());
                logger.info("📦 Restoring inventory for the {} kg package", saga.getPackageWeight());
            }
            logger.info("✅ Order {} compensation completed successfully.", event.getOrderId());
        } catch (Exception e) {
            logger.error("🚨 Error during order {} compensation", event.getOrderId(), e);
        }
    }
}