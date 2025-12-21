package store.domain;

public class OrderItem {
    private String name;
    private int quantity;

    public OrderItem(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }
}