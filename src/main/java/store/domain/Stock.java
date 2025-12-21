package store.domain;

import java.util.List;

public class Stock {
    private List<Product> products;

    public Stock(List<Product> products) {
        this.products = products;
    }

    public boolean existsByName(String name) {
        if (products.stream().anyMatch(
                p -> p.getName().equals(name)
        )) {
            return true;
        }
        throw new IllegalArgumentException("[ERROR] 존재하지 않는 상품입니다. 상품이름을 다시 입력해주세요.");
    }

    public int findQuantityByName(String name) {
        return products.stream()
                .filter(p -> p.getName().equals(name))
                .mapToInt(p -> p.getQuantity()) //.mapToInt(Product::getQuantity)
                .sum();
    }
}
