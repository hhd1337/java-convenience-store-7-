package store.view;

import java.util.List;
import store.dto.ReceiptDto;

public class OutputView {
    public void printStock(List<String> lines) {
        System.out.println("안녕하세요. w편의점입니다.");
        System.out.println("현재 보유하고 있는 상품입니다.\n");
        for (String line : lines) {
            System.out.println("- " + line);
        }
        System.out.println("\n구매하실 상품명과 수량을 입력해주세요. (예: [사이다-2],[감자칩-1])");
    }

    public void printGetMoreItemDuePromotion(String itemName, int benefitCount) {
        System.out.println("현재 " + itemName + "은(는)" + benefitCount + "개를 무료로 더 받을 수 있습니다. 추가하시겠습니까?");
        System.out.print("(Y/N)");
    }

    public void printBenefitCanNotApplyDueStockLack(String itemName, int benefitNotApplyCount) {
        System.out.println("현재 " + itemName + "은(는)" + benefitNotApplyCount + "");
    }

    public void printMembershipDiscountApplyOrNot() {
        System.out.println("멤버십 할인을 받으시겠습니까? (Y/N)");
    }

    public void printTotalReceipt(ReceiptDto receipt) {
        System.out.println("===============w 편의점==============");
        System.out.println("상품명\t\t수량\t금액");
        for (ReceiptDto.PurchaseLine line : receipt.purchases()) {
            System.out.println(line.name() + line.quantity() + line.amount());
        }

        System.out.println("================증 정================");
        for (ReceiptDto.GiftLine line : receipt.gifts()) {
            System.out.println(line.name() + line.quantity());
        }

        System.out.println("====================================");
        System.out.println(
                "총 구매액 " + receipt.summary().totalItemQuantity() + receipt.summary().totalAmountBeforeDiscount());
        System.out.println("행사할인 " + "-" + receipt.summary().promotionDiscount());
        System.out.println("멤버십할인" + "-" + receipt.summary().membershipDiscount());
        System.out.println("내실 돈" + receipt.summary().finalPayAmount());
    }
}