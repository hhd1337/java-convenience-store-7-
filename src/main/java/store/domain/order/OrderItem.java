package store.domain.order;

import store.util.ErrorMessage;

public class OrderItem {
    private final String name;
    private int quantity;

    public OrderItem(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() {
        return this.name;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public int calculateItemTotalPrice(int price) {
        return this.quantity * price;
    }

    public void validatePositiveCount(int count) {
        if (count < 1) {
            throw new IllegalArgumentException(ErrorMessage.PREFIX + "1개 이상의 상품만 구매하실 수 있습니다.");
        }
    }

    public void increaseQuantity(int additionalCount) {
        this.quantity = this.quantity + additionalCount;
    }

    public void decreaseQuantity(int count) {
        this.quantity = this.quantity - count;
    }
}