package cr.ac.una.portalwebpokeapi.service;

import cr.ac.una.portalwebpokeapi.events.OrderCreateEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final ApplicationEventPublisher applicationEventPublisher;

    public OrderService(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void createOrder(String orderId, String productos){
        System.out.println("Creacion de orden de pedido de stocks para orderId=" + orderId);
        applicationEventPublisher.publishEvent(new OrderCreateEvent(orderId, productos));
    }
}
