package com.nordeste.raizes.api.controller;

import com.nordeste.raizes.dominio.enumeracao.SituacaoPedido;
import com.nordeste.raizes.dominio.modelo.Pedido;
import com.nordeste.raizes.infraestrutura.pagamento.GatewayPagamentoMock;
import com.nordeste.raizes.infraestrutura.repositorio.PedidoRepositorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/pagamentos")
@Tag(name = "Pagamentos", description = "Processamento de pagamentos via gateway mock")
@SecurityRequirement(name = "bearerAuth")
public class PagamentoController {

    private final PedidoRepositorio pedidoRepo;
    private final GatewayPagamentoMock gatewayMock;

    public PagamentoController(PedidoRepositorio pedidoRepo, GatewayPagamentoMock gatewayMock) {
        this.pedidoRepo = pedidoRepo;
        this.gatewayMock = gatewayMock;
    }

    @PostMapping("/processar/{pedidoId}")
    @Operation(summary = "Processar pagamento do pedido via gateway mock")
    public ResponseEntity<?> processarPagamento(@PathVariable Long pedidoId) {
        Pedido pedido = pedidoRepo.findById(pedidoId).orElse(null);
        if (pedido == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "PEDIDO_NAO_ENCONTRADO",
                            "mensagem", "Pedido " + pedidoId + " nao encontrado."));
        }

        if (pedido.getSituacao() != SituacaoPedido.AGUARDANDO_PAGAMENTO) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", "SITUACAO_INVALIDA",
                            "mensagem", "Este pedido nao esta aguardando pagamento."));
        }

        GatewayPagamentoMock.ResultadoPagamento resultado =
                gatewayMock.processar(pedido.getValorTotal(), pedido.getFormaPagamento());

        if (resultado.aprovado()) {
            pedido.setSituacao(SituacaoPedido.PAGAMENTO_APROVADO);
            pedido.setSituacaoPagamento("APROVADO");
        } else {
            pedido.setSituacao(SituacaoPedido.CANCELADO);
            pedido.setSituacaoPagamento("RECUSADO");
        }

        pedidoRepo.save(pedido);

        return ResponseEntity.ok(Map.of(
                "situacaoPagamento", pedido.getSituacaoPagamento(),
                "pedidoId", pedido.getId(),
                "novaSituacaoPedido", pedido.getSituacao(),
                "mensagem", resultado.mensagem(),
                "idTransacao", resultado.idTransacao()
        ));
    }
}
