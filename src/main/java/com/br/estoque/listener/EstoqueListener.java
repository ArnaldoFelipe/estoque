package com.br.estoque.listener;

import com.br.estoque.dto.rabbit.BaixaEstoqueEvent;
import com.br.estoque.dto.rabbit.ItemBaixaDTO;
import com.br.estoque.services.ProdutoService;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EstoqueListener {

    private final ProdutoService produtoService;

    @RabbitListener(queues = "estoque.baixar.queue")
    public void receberMensagem(BaixaEstoqueEvent evento){

        System.out.println("Mensagem Recebida do RabbitMQ! Pedido ID: " + evento.pedidoId());

        for (ItemBaixaDTO itens : evento.itens()){
            System.out.println("Baixando " +itens.quantidade()+ "unidades do produto " + itens.produtoId());
            produtoService.baixarEstoque(itens.produtoId(), itens.quantidade());
        }

        System.out.println("Estoque atualizado com sucesso!");
    }
}
