package com.nordeste.raizes.infraestrutura.pagamento;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;

@Component
public class GatewayPagamentoMock {

    private static final BigDecimal LIMITE_APROVACAO = new BigDecimal("500.00");

    public ResultadoPagamento processar(BigDecimal valorTotal, String formaPagamento) {
        boolean aprovado = valorTotal.compareTo(LIMITE_APROVACAO) <= 0;
        String idTransacao = UUID.randomUUID().toString();

        if (aprovado) {
            return new ResultadoPagamento(true, idTransacao, "Pagamento aprovado com sucesso.");
        } else {
            return new ResultadoPagamento(false, idTransacao,
                    "Pagamento recusado: valor acima do limite permitido para simulacao.");
        }
    }

    public record ResultadoPagamento(boolean aprovado, String idTransacao, String mensagem) {}
}
