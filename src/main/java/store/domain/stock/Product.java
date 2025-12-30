package store.domain.stock;

public class Product {
    private final String name;
    private final int price;
    private int quantity;
    private String promotion;

    public Product(String name, int price, int quantity, String promotion) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.promotion = promotion;
    }

    public String getName() {
        return this.name;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public int getPrice() {
        return this.price;
    }

    public String getPromotion() {
        return this.promotion;
    }

    public void decreaseProduct(int num) {
        this.quantity = this.quantity - num;
    }
}
