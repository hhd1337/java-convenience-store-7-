package store.domain.payment;

import store.domain.order.Order;
import store.domain.order.OrderItem;
import store.domain.stock.Stock;

public class Payment {
    private Order order;
    private final Stock stock;

    public Payment(Order order, Stock stock) {
        this.order = order;
        this.stock = stock;
    }

    public int calculateTotalPurchaseAmount() {
        validateOrderAgainstStock();

        return order.getOrderItems().stream()
                .mapToInt(this::calculateItemPurchaseAmount)
                .sum();
    }

    private int calculateItemPurchaseAmount(OrderItem item) {
        // validateOrderAgainstStock();
        int unitPrice = stock.findPriceByName(item.getName());
        return item.calculateItemTotalPrice(unitPrice);
    }

    private void validateOrderAgainstStock() {
        order.getOrderItems().forEach(item -> {
            stock.validateProductExistsByName(item.getName()); // 주문 아이템이 재고에 존재하는지
            stock.validateEnoughStock(item.getName(), item.getQuantity()); // 주문한 수량이 재고에 충분히 있는지
        });
    }

    public Order getOrder() {
        return this.order;
    }

    public Stock getStock() {
        return this.stock;
    }
}
