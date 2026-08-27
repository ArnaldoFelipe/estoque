package com.br.estoque.exception.dto;

import java.time.LocalDateTime;

public record ErroResponse(
        String mensagem,
        String codigo,
        LocalDateTime timesTamp
) {
}
