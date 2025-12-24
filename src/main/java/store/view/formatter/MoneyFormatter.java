package store.view.formatter;

import java.text.NumberFormat;
import java.util.Locale;

public class MoneyFormatter {
    private static final NumberFormat MONEY = NumberFormat.getInstance(Locale.KOREA);

    private MoneyFormatter() {
    }

    public static String format(int amount) {
        return MONEY.format(amount);
    }
}
