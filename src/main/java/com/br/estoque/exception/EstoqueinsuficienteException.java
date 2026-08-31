package com.br.estoque.exception;

public class EstoqueinsuficienteException extends RuntimeException {
    public EstoqueinsuficienteException(String message) {
        super(message);
    }
}
