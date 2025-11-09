package cr.ac.una.portalwebpokeapi.dto;

/**
 * DTO utilizado para crear una orden a partir del carrito (flujo: Recibe pedido del cliente).
 * No incluye campos internos como id, userId, status ni fechas (el backend los gestiona).
 */
public class CheckoutRequest {
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String addressLine1;
    private String addressLine2;
    private String country;
    private String region;

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