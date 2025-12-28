package store.controller;

import java.util.ArrayList;
import java.util.List;
import store.domain.order.GiftItem;
import store.domain.order.Order;
import store.domain.order.OrderItem;
import store.domain.payment.Payment;
import store.domain.promotion.Promotion;
import store.domain.promotion.PromotionCatalog;
import store.domain.stock.Stock;
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
        PromotionCatalog promotionCatalog = new PromotionCatalog(PromotionFileReader.read());

        // 1. 인사, 재고 출력
        outputView.printStock(StockLineMapper.toLines(stock.getProducts()));
        // 2. 구매할 상품명과 수량 입력받아 저장
        List<OrderItem> orderItems = inputView.readProductNameAndQuantity();

        // 3. 주문 입력 직후, 재고에 존재하는 상품인지, 재고에 상품이 충분한지 검증
        for (OrderItem item : orderItems) {
            stock.validateProductExistsByName(item.getName());
            stock.validateEnoughStock(item.getName(), item.getQuantity());
        }

        // 4. GiftItem 리스트를 만들음. 이후 사용자와 대화하며 orderItems와 giftItems를 계속 수정
        List<GiftItem> giftItems = calculateGiftItems(orderItems, stock, promotionCatalog);

        // 5. Stock과 비교해서 프로모션 적용해서 무료로 더 받을 수 있는지 확인, 사용자에게 묻고 OrderItem에 반영하기
        for (OrderItem item : orderItems) {
            int addCount = getAdditionalFreeCount(item, stock, promotionCatalog);
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
            int count = calculateNonPromotionAppliedQuantity(item, stock);

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

        // 7. Order, Payment 생성
        Order order = new Order(orderItems);
        Payment payment = new Payment(order, stock);

        // 8. 멤버십 할인 받을지 입력받기
        outputView.printMembershipDiscountApplyOrNot();
        boolean membershipDC = inputView.readYesNo();

        // 9. 영수증 출력

        // 10. 재고 차감

        // 11. 다른상품구매할지 입력받아 해당 여부에 따라 while문 탈출/종료 혹은 1번으로 돌아갈지 결정

    }

    private List<GiftItem> calculateGiftItems(List<OrderItem> orderItems, Stock stock, PromotionCatalog pc) {
        List<GiftItem> giftItems = new ArrayList<>();

        for (OrderItem item : orderItems) {
            String name = item.getName();
            String promotionName;
            // 이 아이템이 프로모션상품이 없으면 다음 아이템으로 넘어감.
            try {
                promotionName = stock.findPromotionByProductName(name);
            } catch (IllegalArgumentException e) {
                continue;
            }

            Promotion promotion = pc.findPromotionByName(promotionName);

            int buy = promotion.getBuy();
            int get = promotion.getGet();
            int cycle = buy + get;
            int PromoProductQuantity = (item.getQuantity() / cycle) * get;

            // 재고에 현재 프로모션 아이템 수량이 계산한 PromoProductQuantity 만큼 있는지 확인.
            // 주문수량 중 프로모션 재고를 초과한 수량을 받아서 그만큼을 제외한, 현재 해당상품 프로모션상품 수를 giftItems객체에 대입
            int num = calculateNonPromotionAppliedQuantity(item, stock);
            if (num > 0) {
                PromoProductQuantity = PromoProductQuantity - num;
            }

            giftItems.add(new GiftItem(name, PromoProductQuantity));
        }

        return giftItems;
    }

    // 주문수량 중 프로모션 재고를 초과한 수량 반환, 음수이면 0 반환
    private int calculateNonPromotionAppliedQuantity(OrderItem item, Stock stock) {
        int orderQuantity = item.getQuantity();
        String itemName = item.getName();

        int promotionStockQuantity = stock.findPromotionProductCountByName(itemName);

        return Math.max(0, orderQuantity - promotionStockQuantity);
    }

    private int getAdditionalFreeCount(OrderItem item, Stock stock, PromotionCatalog pc) {
        int orderQuantity = item.getQuantity();

        String promotionName = stock.findPromotionByProductName(item.getName());
        Promotion promotion = pc.findPromotionByName(promotionName);

        int buy = promotion.getBuy();
        int get = promotion.getGet();

        int cycle = buy + get;
        int remain = orderQuantity % cycle;

        if (orderQuantity < buy || remain == 0 || remain < buy) {
            return 0;
        }
        return cycle - remain;
    }
}