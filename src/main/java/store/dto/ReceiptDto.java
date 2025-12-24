package store.dto;

import java.util.List;

public record ReceiptDto(
        List<ReceiptDto.PurchaseLine> purchases,
        List<ReceiptDto.GiftLine> gifts,
        ReceiptDto.PaymentSummary summary
) {
    public record PurchaseLine(String name, int quantity, int amount) {
    }

    public record GiftLine(String name, int quantity) {
    }

    public record PaymentSummary(int totalItemQuantity,
                                 int totalAmountBeforeDiscount,
                                 int promotionDiscount,
                                 int membershipDiscount,
                                 int finalPayAmount) {
    }
}