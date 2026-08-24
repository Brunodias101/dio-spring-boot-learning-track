package dio.budgeting.application;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class CalculateTotalByCategoryUseCase {
    private final TransactionRepository transactionRepository;

    public CalculateTotalByCategoryUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "calculate-total-by-category", description = "Calcula o valor total das transações de uma categoria, em centavos")
    public long execute(@ToolParam(description = "Categoria das transações a serem somadas") Category category) {
        return transactionRepository.sumAmountByCategory(category);
    }
}
