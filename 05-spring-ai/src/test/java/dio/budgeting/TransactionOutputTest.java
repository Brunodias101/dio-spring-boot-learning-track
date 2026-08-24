package dio.budgeting;

import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionOutputTest {

    @ParameterizedTest
    @CsvSource({
            "5000, 50.00",
            "3500, 35.00",
            "1050, 10.50"
    })
    void shouldConvertCentsToReais(long cents, double expectedValue) {
        var transaction = new Transaction(
                "Mercado",
                cents,
                Category.GROCERIES
        );

        var output = TransactionOutput.from(transaction);

        assertThat(output.value()).isEqualTo(expectedValue);
    }
}