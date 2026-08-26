package com.br.estoque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProdutoRequest(

        @NotBlank(message = "Deve informar um nome")
        String nome,

        @NotBlank(message = "Deve informar uma descrição")
        String descricao,

        @NotNull(message = "O valor não pode ser nulo")
        @Positive(message = "O valor deve ser maior que zero")
        BigDecimal valor,

        @NotNull(message = "O valor não pode ser nulo")
        @PositiveOrZero(message = "A quantidade não pode ser negativa")
        Integer qtEstoque
) {
}
