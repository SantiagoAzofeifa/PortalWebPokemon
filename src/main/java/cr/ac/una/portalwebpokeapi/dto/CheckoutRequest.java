package cr.ac.una.portalwebpokeapi.dto;

/**
 * DTO (Data Transfer Object) utilizado durante el proceso de checkout.
 *
 * Representa los datos enviados por el cliente al confirmar una orden a partir del carrito.
 *
 * Este objeto solo transporta información básica de contacto y dirección.
 * Los campos relacionados con control interno (id, userId, estado, fechas, totales, etc.)
 * son gestionados internamente por el backend durante la creación de la orden.
 */
public class CheckoutRequest {
    /** Nombre completo del cliente que realiza la orden. */
    private String customerName;

    /** Correo electrónico de contacto. */
    private String customerEmail;

    /** Número telefónico del cliente. */
    private String customerPhone;

    /** Línea 1 de la dirección (por ejemplo: calle, número, edificio). */
    private String addressLine1;

    /** Línea 2 de la dirección (complementaria, opcional). */
    private String addressLine2;

    /** País donde se entrega el pedido. */
    private String country;

    /** Región, provincia o estado dentro del país. */
    private String region;

    // ----- Getters y Setters -----

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
}
