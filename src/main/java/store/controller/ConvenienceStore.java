package store.controller;

import camp.nextstep.edu.missionutils.DateTimes;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import store.domain.order.Order;
import store.domain.order.OrderItem;
import store.domain.order.PromotionAppliedItem;
import store.domain.payment.Payment;
import store.domain.promotion.Promotion;
import store.domain.promotion.PromotionCatalog;
import store.domain.stock.Stock;
import store.dto.ReceiptDto;
import store.dto.ReceiptDto.GiftLine;
import store.dto.ReceiptDto.PaymentSummary;
import store.dto.ReceiptDto.PurchaseLine;
import store.file.ProductFileReader;
import store.file.PromotionFileReader;
import store.view.InputView;
import store.view.OutputView;
import store.view.mapper.StockLineMapper;

public class ConvenienceStore {
    private final InputView inputView;
    private final OutputView outputView;

    public ConvenienceStore(InputView inputView, OutputView outputView) {
        this.inputView = inputView;
        this.outputView = outputView;
    }

    public void open() {
        Stock stock = new Stock(ProductFileReader.read());
        PromotionCatalog pc = new PromotionCatalog(PromotionFileReader.read());
        LocalDate today = DateTimes.now().toLocalDate();

        // 1. 인사, 재고 출력
        outputView.printStock(StockLineMapper.toLines(stock.getProducts()));
        // 2. 구매할 상품명과 수량 입력받아 저장
        List<OrderItem> orderItems = inputView.readProductNameAndQuantity();

        // 3. 주문 입력 직후, 재고에 존재하는 상품인지, 재고에 상품이 충분한지 검증
        for (OrderItem item : orderItems) {
            stock.validateProductExistsByName(item.getName());
            stock.validateEnoughStock(item.getName(), item.getQuantity());
        }

        // 4. PromotionAppliedItem 리스트를 만들음. 이후 사용자와 대화하며 orderItems와 promotionAppliedItems를 계속 수정
        List<PromotionAppliedItem> promotionAppliedItems = calculatePromotionAppliedItems(orderItems, stock, pc);

        // 5. Stock과 비교해서 프로모션 적용해서 무료로 더 받을 수 있는지 확인, 사용자에게 묻고 OrderItem에 반영하기
        for (OrderItem item : orderItems) {
            int addCount = getAdditionalFreeCount(item, stock, pc, today);
            if (addCount > 0) {
                outputView.printGetMoreItemDuePromotion(item.getName(), addCount);
                if (inputView.readYesNo()) {
                    item.increaseQuantity(addCount);
                }
            }
        }

        // 6. 프로모션 재고부족, 일부수량 프로모션 혜택없이 구매해야 할 경우, 일부수량 정가로 결제할지 묻고 OrderItem에 반영
        for (OrderItem item : orderItems) {
            // 4-1. 프로모션 재고부족, 일부수량 프로모션 혜택없이 구매해야 할 경우가 있는지 확인
            int count = calculateNonPromotionAppliedQuantity(item, stock, pc);

            if (count > 0) {
                outputView.printBenefitCanNotApplyDueStockLack(item.getName(), count);
                // 4-2. 일부수량 정가로 결제할지 묻기
                if (!inputView.readYesNo()) { // 4-3. OrderItem에 반영
                    item.decreaseQuantity(count); // 정가결제 안한다고 함. -> 아이템 수량 count 만큼 차감해야 함.
                }
                // 정가결제 한다고 함. -> 아이템 수량 조작할 필요 없음.
                // "해당 수량만큼 정가결제 할거다." 는 기록 할 필요 없음.
                // 그냥 계산로직을 프로모션 다 쓰면 정가품목으로 계산하게 하면 됨.
            }
        }

        // orderItems 변동에 따른 promotionAppliedItems 최신화
        promotionAppliedItems = calculatePromotionAppliedItems(orderItems, stock, pc);

        // 7. Order, Payment 생성
        Order order = new Order(orderItems);
        Payment payment = new Payment(order, stock);

        // 8. 멤버십 할인 받을지 입력받기
        outputView.printMembershipDiscountApplyOrNot();
        boolean membershipDC = inputView.readYesNo();

        // 9. 영수증 출력
        ReceiptDto receiptDto = createReceiptDto(payment, promotionAppliedItems, membershipDC, pc);
        outputView.printTotalReceipt(receiptDto);
        // 10. 재고 차감

        // 11. 다른상품구매할지 입력받아 해당 여부에 따라 while문 탈출/종료 혹은 1번으로 돌아갈지 결정

    }

    private ReceiptDto createReceiptDto(Payment payment, List<PromotionAppliedItem> promotionAppliedItems,
                                        boolean membershipDiscountYes, PromotionCatalog pc) {
        // Payment에서 필요한 정보들 꺼내서 ReceiptDto 만들기
        Order order = payment.getOrder();
        List<OrderItem> orderItems = order.getOrderItems();

        Stock stock = payment.getStock();

        List<ReceiptDto.PurchaseLine> purchases;
        List<ReceiptDto.GiftLine> gifts;
        ReceiptDto.PaymentSummary summary;

        purchases = orderItems.stream()
                .map(item -> {
                    String name = item.getName();
                    int quantity = item.getQuantity();
                    int amount = quantity * stock.findPriceByName(name);

                    return new PurchaseLine(name, quantity, amount);
                })
                .toList();

        gifts = promotionAppliedItems.stream()
                .map(item -> {
                    String itemName = item.name();
                    Promotion promotion = findPromotionOrNull(itemName, stock, pc);
                    if (promotion != null) {
                        int get = promotion.getGet();
                        int buy = promotion.getBuy();
                        int giftQuantity = calculateGiftQuantity(buy, get, item.quantity());
                        return new GiftLine(itemName, giftQuantity);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();

        int totalItemQuantity = orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
        int totalAmountBeforeDiscount = payment.calculateTotalPurchaseAmount();
        int promotionDiscount = gifts.stream()
                .mapToInt(line -> stock.findPriceByName(line.name()) * line.quantity())
                .sum();

        int membershipDiscount = 0;
        if (membershipDiscountYes) {
            membershipDiscount = (int) Math.min(8000, (totalAmountBeforeDiscount - promotionDiscount) * 0.3);
        }

        int finalPayAmount = totalAmountBeforeDiscount - promotionDiscount - membershipDiscount;

        summary = new PaymentSummary(totalItemQuantity, totalAmountBeforeDiscount, promotionDiscount,
                membershipDiscount, finalPayAmount);

        return new ReceiptDto(purchases, gifts, summary);
    }

    private List<PromotionAppliedItem> calculatePromotionAppliedItems(List<OrderItem> orderItems, Stock stock,
                                                                      PromotionCatalog pc) {
        List<PromotionAppliedItem> promotionAppliedItems = new ArrayList<>();

        for (OrderItem item : orderItems) {
            String name = item.getName();
            // 이 아이템이 프로모션상품이 없으면 다음 아이템으로 넘어감.
            try {
                stock.findPromotionByProductName(name);
            } catch (IllegalArgumentException e) {
                continue;
            }

            int nonPromotionAppliedQuantity = calculateNonPromotionAppliedQuantity(item, stock, pc);

            promotionAppliedItems.add(new PromotionAppliedItem(name, item.getQuantity() - nonPromotionAppliedQuantity));
        }

        return promotionAppliedItems;
    }

    // 원래 : 주문수량 중 프로모션 재고를 초과한 수량 반환, 음수이면 0 반환
    // 변경해야 함 :
    // (A) 프로모션을 적용할 수 있는 ‘최대 총개수(구매+증정)’를 구하고
    // (B) orderQuantity에서 그걸 뺀 나머지 개수 = “프로모션 할인이 적용되지 않는 개수”
    private int calculateNonPromotionAppliedQuantity(OrderItem item, Stock stock, PromotionCatalog pc) {
        int orderQuantity = item.getQuantity();
        String itemName = item.getName();

        try {
            int promotionStockQuantity = stock.findPromotionProductCountByName(itemName);
            String promotionName = stock.findPromotionByProductName(itemName);
            Promotion promotion = pc.findPromotionByName(promotionName);
            int get = promotion.getGet();
            int buy = promotion.getBuy();
            int cycle = buy + get;

            int promotionApplicable = (promotionStockQuantity / cycle) * cycle;
            return Math.max(0, orderQuantity - promotionApplicable);

        } catch (IllegalArgumentException e) {
            return orderQuantity; // 프로모션 상품이 없으면, 프로모션 적용 불가 수량 = 전체 주문 수량
        }
    }

    private int getAdditionalFreeCount(OrderItem item, Stock stock, PromotionCatalog pc, LocalDate today) {
        String itemName = item.getName();
        int orderQuantity = item.getQuantity();
        Promotion promotion = findPromotionOrNull(itemName, stock, pc);
        if (promotion != null) {
            if (!promotion.isActive(today)) {
                return 0;
            }
            int get = promotion.getGet();
            int buy = promotion.getBuy();
            int cycle = buy + get;
            int remain = orderQuantity % cycle;

            if (orderQuantity < buy || remain == 0 || remain < buy) {
                return 0;
            }
            return cycle - remain;
        }
        return 0;
    }

    private Promotion findPromotionOrNull(String productName, Stock stock, PromotionCatalog pc) {
        try {
            String promoName = stock.findPromotionByProductName(productName);
            return pc.findPromotionByName(promoName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private int calculateGiftQuantity(int buy, int get, int promotionAppliedItemsQuantity) {
        int cycle = get + buy;
        return (promotionAppliedItemsQuantity / cycle) * get;
    }
}