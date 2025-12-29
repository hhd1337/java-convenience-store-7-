package store.domain.stock;

import java.util.List;
import store.util.ErrorMessage;

public class Stock {
    private List<Product> products;

    public Stock(List<Product> products) {
        this.products = products;
    }

    public void validateProductExistsByName(String name) {
        if (products.stream().noneMatch(
                p -> p.getName().equals(name)
        )) {
            throw new IllegalArgumentException(ErrorMessage.PREFIX + "존재하지 않는 상품입니다. 상품이름을 다시 입력해주세요.");
        }
    }

    public void validateEnoughStock(String name, int quantity) {
        if (quantity > findQuantityByName(name)) {
            throw new IllegalArgumentException(ErrorMessage.PREFIX + " 재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요.");
        }
    }

    public int findQuantityByName(String name) {
        return products.stream()
                .filter(p -> p.getName().equals(name))
                .mapToInt(Product::getQuantity)
                .sum();
    }

    public int findPriceByName(String name) {
        return products.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        ErrorMessage.PREFIX + " " + name + "은 존재하지 않는 상품입니다. 다시 입력해주세요."))
                .getPrice();
    }

    public String findPromotionOrNullByProductName(String name) {
        return products.stream()
                .filter(p -> p.getName().equals(name))
                .filter(p -> p.getPromotion() != null)
                .map(Product::getPromotion)
                .findFirst()
                .orElse(null);
    }

    public int findPromotionProductCountByName(String name) {
        validateProductExistsByName(name);

        Product promotionProduct = products.stream()
                .filter(p -> p.getName().equals(name))
                .filter(p -> p.getPromotion() != null)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessage.PREFIX + name + "은(는) 프로모션 상품이 없습니다."));
        return promotionProduct.getQuantity();
    }

    public List<Product> getProducts() {
        return this.products;
    }
}
