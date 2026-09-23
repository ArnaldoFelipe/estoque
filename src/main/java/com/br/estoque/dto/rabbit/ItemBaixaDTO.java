package com.br.estoque.dto.rabbit;

import java.util.UUID;

public record ItemBaixaDTO(
        UUID produtoId,
        Integer quantidade
) {
}
