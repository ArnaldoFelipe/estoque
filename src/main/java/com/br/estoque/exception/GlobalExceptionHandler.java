package com.br.estoque.exception;

import com.br.estoque.exception.dto.ErroResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleProdutoNaoEncontrado(ProdutoNaoEncontradoException ex){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErroResponse(
                        ex.getMessage(),
                        "PRODUTO_NAO_ENCONTRADO",
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(EstoqueinsuficienteException.class)
    public ResponseEntity<ErroResponse> handleEstoqueinsuficiente(EstoqueinsuficienteException ex){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErroResponse(
                        ex.getMessage(),
                        "ESTOQUE_INSUFICIENTE",
                        LocalDateTime.now()
                ));
    }
}
