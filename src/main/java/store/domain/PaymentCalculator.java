package store.domain;

public class PaymentCalculator {
    public int calculateTotalPurchaseAmount(Order order, Stock stock) {
        // 재고 검증, 예외로직
        order.getOrderItems()
                .forEach(item -> stock.hasEnoughStock(item.getName(), item.getQuantity()));

        return order.getOrderItems().stream()
                .mapToInt(item -> stock.findPriceByName(item.getName()) * item.getQuantity())
                .sum();
    }
}
