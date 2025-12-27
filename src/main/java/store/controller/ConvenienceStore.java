package store.controller;

import store.domain.order.OrderItem;
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

        outputView.printStock(StockLineMapper.toLines(stock.getProducts()));
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