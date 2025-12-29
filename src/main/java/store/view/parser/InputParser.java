package store.view.parser;

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

        return DelimitedParser.parseList(input, ITEM_DELIMITER, token -> {
            String normalized = token.replace("[", "").replace("]", "");
            String[] parts = normalized.split(NAME_QUANTITY_DELIMITER, 2);

            if (parts.length != 2) {
                throw new IllegalArgumentException(ErrorMessage.PREFIX + "올바른 형식으로 입력해주세요.");
            }

            String name = parts[0].trim();
            String qtyStr = parts[1].trim();

            if (name.isEmpty() || qtyStr.isEmpty()) {
                throw new IllegalArgumentException(ErrorMessage.PREFIX + "올바른 형식으로 입력해주세요.");
            }

            int quantity = StringToIntParser.parseInt(qtyStr);

            if (!seenNames.add(name)) {
                throw new IllegalArgumentException(
                        ErrorMessage.PREFIX + "같은 상품은 합쳐서 한번만 입력해주세요: " + name
                );
            }

            OrderItem orderItem = new OrderItem(name, quantity);
            orderItem.validatePositiveCount(quantity);
            return orderItem;
        });
    }
}