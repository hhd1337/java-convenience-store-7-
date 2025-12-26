package store.view;

import camp.nextstep.edu.missionutils.Console;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import store.util.ErrorMessage;

public class InputView {

    public Map<String, Integer> readProductNameAndQuantity() {
        String input = Console.readLine();
        return parseProductNameAndQuantity(input);
    }

    private Map<String, Integer> parseProductNameAndQuantity(String input) {
        Map<String, Integer> quantitiesByName = new LinkedHashMap<>();

        String[] tokens = input.split(",");

        Arrays.stream(tokens)
                .map(String::trim)
                .map(s -> s.replace("[", "").replace("]", ""))
                .forEach(item -> {
                    String[] parts = item.split("-");
                    String name = parts[0].trim();
                    int quantity = Integer.parseInt(parts[1].trim());

                    if (quantitiesByName.containsKey(name)) {
                        throw new IllegalArgumentException(ErrorMessage.PREFIX + "같은 상품은 합쳐서 한번만 입력해주세요: " + name);
                    }

                    quantitiesByName.put(name, quantity);
                });

        return quantitiesByName;
    }
}