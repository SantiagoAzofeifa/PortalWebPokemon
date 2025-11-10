package cr.ac.una.portalwebpokeapi.listeners;


import cr.ac.una.portalwebpokeapi.events.OrderCreateEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EventsListeners {

    @EventListener
    public void handleOrderCreateAndSendEmail(OrderCreateEvent e){
        System.out.println(e);
    }
}
