package com.br.estoque.listener;

import com.br.estoque.dto.rabbit.BaixaEstoqueEvent;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EstoqueDlqListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = @QueueBinding(
         value = @Queue(value = "estoque.dlq", durable = "true"),
         exchange = @Exchange(value = "estoque.dlx", type = "direct", ignoreDeclarationExceptions = "true"),
         key = "estoque.baixar.dlq.rk"

    ))
    public void processarFilaMorta(BaixaEstoqueEvent evento){

        System.out.println("DLQ: Falha confirmada para o pedido: " + evento.pedidoId());
        System.out.println("SAGA: Avisando o microsserviço de Pedidos para CANCELAR a compra...");

        rabbitTemplate.convertAndSend("pedido.exchange", "pedido.cancelar.rk", evento);
    }
}
