package cr.ac.una.portalwebpokeapi.events;

public class OrderCreateEvent {
    private final String orderId;
    private final String productos;


    public OrderCreateEvent(String orderId, String productos) {
        this.orderId = orderId;
        this.productos = productos;
    }

    public String getOrderId() {
        return orderId;
    }

    public String toString(){
        return "Se recibio una orden de pedido de stocks para {orderId=" + orderId + ", productos=" + productos + '}';
    }
}
