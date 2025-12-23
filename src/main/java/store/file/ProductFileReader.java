package store.file;

import java.nio.file.Path;
import java.util.List;
import store.domain.stock.Product;

public class ProductFileReader {

    private static final Path PATH = Path.of("src/main/resources/products.md");

    public static List<Product> read() {
        return DelimitedFileReader.read(PATH, columns -> {
            String name = columns[0].trim();
            int price = Integer.parseInt(columns[1].trim());
            int quantity = Integer.parseInt(columns[2].trim());
            String promotion = columns[3].trim();

            if ("null".equals(promotion)) {
                promotion = null;
            }

            return new Product(name, price, quantity, promotion);
        });
    }
}


