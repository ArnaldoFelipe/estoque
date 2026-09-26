package com.br.estoque.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter jsonMessageConverter(){
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public Queue filaMorta() {
        return new Queue("estoque.dlq", true);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("estoque.dlx");
    }

    @Bean
    public Binding bindingFilaMorta() {
        // Liga a fila morta na exchange morta usando a routing key de erro
        return BindingBuilder.bind(filaMorta()).to(deadLetterExchange()).with("estoque.baixar.dlq.rk");
    }
}
