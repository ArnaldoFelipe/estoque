package com.br.estoque.controller;

import com.br.estoque.dto.AtualizarProdutoRequest;
import com.br.estoque.dto.ProdutoRequest;
import com.br.estoque.dto.ProdutoResponse;
import com.br.estoque.entities.Produto;
import com.br.estoque.services.ProdutoService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/produtos")
@AllArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping
    public ResponseEntity<ProdutoResponse> cadastrarProduto(@RequestBody @Valid ProdutoRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.cadastrarProduto(request));
    }

    @GetMapping("{produtoId}")
    public ResponseEntity<ProdutoResponse> buscarProdutoPorId(@PathVariable UUID produtoId){
        return ResponseEntity.ok(produtoService.buscarProdutoPorId(produtoId));
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponse>> listarProdutos(){
        return ResponseEntity.ok(produtoService.listarProdutos());
    }

    @PatchMapping("/{produtoId}")
    public ResponseEntity<ProdutoResponse> atualizarProduto(@RequestBody @Valid AtualizarProdutoRequest request, @PathVariable  UUID produtoId){
        return ResponseEntity.ok(produtoService.atualizarProduto(request, produtoId));
    }

    @DeleteMapping("/{produtoId}")
    public ResponseEntity<Void> desativarProduto(@PathVariable UUID produtoId){
        produtoService.desativarProduto(produtoId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{produtoId}/baixar-estoque")
    public ResponseEntity<Void> baixarEstoque(@PathVariable  UUID produtoId, @RequestParam Integer quantidade){
        produtoService.baixarEstoque(produtoId, quantidade);
        return ResponseEntity.noContent().build();
    }
}
