package dio.budgeting.application;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CalculateTotalByCategoryUseCaseTest {

    @Test
    void shouldReturnTheTotalInCentsForTheRequestedCategory() {
        var transactionRepository = mock(TransactionRepository.class);
        when(transactionRepository.sumAmountByCategory(Category.GROCERIES)).thenReturn(8_550L);
        var useCase = new CalculateTotalByCategoryUseCase(transactionRepository);

        var total = useCase.execute(Category.GROCERIES);

        assertThat(total).isEqualTo(8_550L);
        verify(transactionRepository).sumAmountByCategory(Category.GROCERIES);
    }
}
