package store.controller;

import java.util.List;
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

        // 3. Stock과 비교해서 프로모션 적용해서 무료로 더 받을 수 있는지 확인, 사용자에게 묻고 OrderItem에 반영하기
        for (OrderItem item : orderItems) {
            int addCount = getAdditionalFreeCount(item, stock, promotionCatalog);
            if (addCount > 0) {
                outputView.printGetMoreItemDuePromotion(item.getName(), addCount);
                if (inputView.readYesNo()) {
                    item.increaseQuantity(addCount);
                }
            }
        }
        // 4. 프로모션 재고부족, 일부수량 프로모션 혜택없이 구매해야 할 경우, 일부수량 정가로 결제할지 묻고 OrderItem에 반영

        // 3. Order, Payment 생성
        Order order = new Order(orderItems);
        Payment payment = new Payment(order, stock);

        // 4. 멤버십 할인 받을지 입력받기
        outputView.printMembershipDiscountApplyOrNot();
        boolean membershipDC = inputView.readYesNo();
        // 5. 영수증 출력

        // 6. 재고 차감

        // 7. 다른상품구매할지 입력받아 해당 여부에 따라 while문 탈출/종료 혹은 1번으로 돌아갈지 결정

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