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
            throw new IllegalArgumentException(ErrorMessage.PREFIX + name + "상품 재고가 부족합니다. 다시 입력해주세요.");
        }
    }

    public int findQuantityByName(String name) {
        return products.stream()
                .filter(p -> p.getName().equals(name))
                .mapToInt(p -> p.getQuantity()) //.mapToInt(Product::getQuantity)
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
}
