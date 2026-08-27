package com.br.estoque.services;

import com.br.estoque.dto.AtualizarProdutoRequest;
import com.br.estoque.dto.ProdutoRequest;
import com.br.estoque.dto.ProdutoResponse;
import com.br.estoque.entities.Produto;
import com.br.estoque.exception.ProdutoNaoEncontradoException;
import com.br.estoque.mapper.ProdutoMapper;
import com.br.estoque.repository.ProdutoRepository;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.Temporal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;

    @Transactional
    public ProdutoResponse cadastrarProduto(ProdutoRequest request){
        Produto produto = produtoMapper.toEntity(request);
        return produtoMapper.toResponse(produtoRepository.save(produto));
    }

    public ProdutoResponse buscarProdutoPorId(UUID produtoId){
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não foi encontrado no estoque"));

        return produtoMapper.toResponse(produto);
    }

    public List<ProdutoResponse> listarProdutos(){
        List<Produto> produtos = produtoRepository.findAll();
        return produtoMapper.toResponseList(produtos);
    }

    @Transactional
    public ProdutoResponse atualizarProduto(AtualizarProdutoRequest request, UUID produtoId){
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não foi encontrado no estoque"));

        produtoMapper.atualizarProduto(produto, request);

        return produtoMapper.toResponse(produtoRepository.save(produto));
    }

    @Transactional
    public void desativarProduto(UUID produtoId){
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não foi encontrado no estoque"));

        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    @Transactional
    public void baixarEstoque(UUID produtoId, Integer quantidadeComprada){
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não foi encontrado no estoque"));

        if(produto.getQtEstoque() < quantidadeComprada){
            throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNome());
        }

        produto.setQtEstoque(produto.getQtEstoque() - quantidadeComprada);
    }
}
