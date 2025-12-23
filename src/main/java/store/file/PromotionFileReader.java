package store.file;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import store.domain.promotion.Promotion;

public class PromotionFileReader {
    private static final Path PATH = Path.of("src/main/resources/promotions.md");

    public List<Promotion> read() {

        return DelimitedFileReader.read(PATH, columns -> {
            String name = columns[0].trim();
            int buy = Integer.parseInt(columns[1].trim());
            int get = Integer.parseInt(columns[2].trim());
            LocalDate startDate = LocalDate.parse(columns[3].trim());
            LocalDate endDate = LocalDate.parse(columns[4].trim());

            return new Promotion(name, buy, get, startDate, endDate);
        });
    }
}
