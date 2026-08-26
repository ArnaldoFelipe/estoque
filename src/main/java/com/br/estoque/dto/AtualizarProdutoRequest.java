package com.br.estoque.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AtualizarProdutoRequest(
        @Size(min = 1, message = "A descrição não pode ser vazia")
        String nome,

        @Size(min = 1, message = "A descrição não pode ser vazia")
        String descricao,

        @Positive(message = "O valor deve ser maior que zero")
        BigDecimal valor,

        @PositiveOrZero(message = "A quantidade não pode ser negativa")
        Integer qtEstoque
) {
}
