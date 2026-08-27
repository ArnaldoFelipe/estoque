package com.br.estoque.mapper;

import com.br.estoque.dto.AtualizarProdutoRequest;
import com.br.estoque.dto.ProdutoRequest;
import com.br.estoque.dto.ProdutoResponse;
import com.br.estoque.entities.Produto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProdutoMapper {

    Produto toEntity(ProdutoRequest request);

    ProdutoResponse toResponse(Produto produto);

    List<ProdutoResponse> toResponseList(List<Produto> produtos);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void atualizarProduto(@MappingTarget Produto produto, AtualizarProdutoRequest request);
}
