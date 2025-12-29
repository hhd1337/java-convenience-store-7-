package store.view.parser;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class DelimitedParser {

    private DelimitedParser() {
    }

    // 입력 문자열을 delimiter로 분리한 뒤, 각 토큰을 normalize한 결과를 List로 반환한다.
    public static List<String> splitAndNormalize(String input, String delimiter) {
        return Arrays.stream(input.split(delimiter))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .toList();
    }

    // splitAndNormalize 후, 각 토큰을 mapper로 변환하여 List<T>로 반환한다.
    // mapper 내부에서 IllegalArgumentException을 던지면 상위 retryUntilValid에서 처리 가능.
    public static <T> List<T> parseList(String input, String delimiter, Function<String, T> mapper) {
        return splitAndNormalize(input, delimiter).stream()
                .map(mapper)
                .toList();
    }
}

