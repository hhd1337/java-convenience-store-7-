package store.view;

import camp.nextstep.edu.missionutils.Console;
import java.util.List;
import store.domain.order.OrderItem;
import store.util.ErrorMessage;
import store.view.parser.InputParser;

public class InputView {

    public List<OrderItem> readProductNameAndQuantity() {
        String input = Console.readLine();
        return InputParser.parseOrderItems(input);
    }

    public boolean readYesNo() {
        String input = Console.readLine().trim().toUpperCase();
        if (input.equals("Y")) {
            return true;
        }
        if (input.equals("N")) {
            return false;
        }
        throw new IllegalArgumentException(ErrorMessage.PREFIX + " 잘못된 입력입니다. 다시 입력해주세요.");
    }


}