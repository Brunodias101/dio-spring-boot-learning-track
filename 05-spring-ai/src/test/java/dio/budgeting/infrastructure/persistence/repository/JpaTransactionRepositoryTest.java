package dio.budgeting.infrastructure.persistence.repository;

import dio.budgeting.domain.Category;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaTransactionRepositoryTest {

    @Test
    void shouldDelegateTheCategoryTotalCalculationToThePersistenceRepository() {
        var transactionEntityRepository = mock(TransactionEntityRepository.class);
        when(transactionEntityRepository.sumAmountByCategory(Category.PHARMA)).thenReturn(2_350L);
        var repository = new JpaTransactionRepository(transactionEntityRepository);

        var total = repository.sumAmountByCategory(Category.PHARMA);

        assertThat(total).isEqualTo(2_350L);
        verify(transactionEntityRepository).sumAmountByCategory(Category.PHARMA);
    }
}
