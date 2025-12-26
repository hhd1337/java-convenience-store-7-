package store.view.parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import store.domain.order.OrderItem;
import store.util.ErrorMessage;

public class InputParser {
    private static final String ITEM_DELIMITER = ",";
    private static final String NAME_QUANTITY_DELIMITER = "-";

    public static List<OrderItem> parseOrderItems(String input) {
        Set<String> seenNames = new HashSet<>();

        return Arrays.stream(input.split(ITEM_DELIMITER))
                .map(String::trim)
                .map(s -> s.replace("[", "").replace("]", "")) // 사이다-2
                .map(token -> token.split(NAME_QUANTITY_DELIMITER))
                .map(parts -> {
                    String name = parts[0].trim();
                    int quantity = Integer.parseInt(parts[1].trim());

                    if (!seenNames.add(name)) {
                        throw new IllegalArgumentException(ErrorMessage.PREFIX + "같은 상품은 합쳐서 한번만 입력해주세요: " + name);
                    }

                    OrderItem orderItem = new OrderItem(name, quantity);
                    orderItem.validatePositiveCount(quantity);
                    return orderItem;
                })
                .toList();
    }
}
