package cr.ac.una.portalwebpokeapi.controller;

import cr.ac.una.portalwebpokeapi.service.OrderService;
import org.springframework.web.bind.annotation.*;
/*
Controller para la creacion de los eventos propios del Publisher-Suscriber
 */

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/{orderId}")
    public String createOrder(@PathVariable String orderId, @RequestParam String productos){
        orderService.createOrder(orderId, productos);
        return "Orden creada exitosamente: " + orderId;
    }
}
