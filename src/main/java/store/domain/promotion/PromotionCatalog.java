package store.domain.promotion;

import java.util.List;
import store.util.ErrorMessage;

public class PromotionCatalog {
    private List<Promotion> promotions;

    public PromotionCatalog(List<Promotion> promotions) {
        this.promotions = promotions;
    }

    public Promotion findPromotionByName(String name) {
        return promotions.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(ErrorMessage.PREFIX + "이름이 " + name + "인 프로모션은 존재하지 않습니다."));
    }
}
