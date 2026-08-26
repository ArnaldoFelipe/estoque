package com.br.estoque.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoResponse(
        UUID produtoId,
        String nome,
        String descricao,
        BigDecimal valor,
        Integer qtEstoque,
        Boolean ativo
) {
}
