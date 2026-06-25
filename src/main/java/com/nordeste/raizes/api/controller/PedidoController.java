package com.nordeste.raizes.api.controller;

import com.nordeste.raizes.dominio.enumeracao.CanalAtendimento;
import com.nordeste.raizes.dominio.enumeracao.SituacaoPedido;
import com.nordeste.raizes.dominio.modelo.*;
import com.nordeste.raizes.infraestrutura.repositorio.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pedidos")
@Tag(name = "Pedidos", description = "Gestao de pedidos da rede")
@SecurityRequirement(name = "bearerAuth")
public class PedidoController {

    private final PedidoRepositorio pedidoRepo;
    private final ClienteRepositorio clienteRepo;
    private final UnidadeRepositorio unidadeRepo;
    private final ProdutoRepositorio produtoRepo;
    private final EstoqueRepositorio estoqueRepo;

    public PedidoController(PedidoRepositorio pedidoRepo, ClienteRepositorio clienteRepo,
                            UnidadeRepositorio unidadeRepo, ProdutoRepositorio produtoRepo,
                            EstoqueRepositorio estoqueRepo) {
        this.pedidoRepo = pedidoRepo;
        this.clienteRepo = clienteRepo;
        this.unidadeRepo = unidadeRepo;
        this.produtoRepo = produtoRepo;
        this.estoqueRepo = estoqueRepo;
    }

    @PostMapping
    @Operation(summary = "Criar novo pedido com validacao de estoque")
    public ResponseEntity<?> criarPedido(@RequestBody Map<String, Object> body) {
        List<String> faltando = new ArrayList<>();
        if (body.get("clienteId") == null) faltando.add("clienteId");
        if (body.get("unidadeId") == null) faltando.add("unidadeId");
        if (body.get("canalPedido") == null) faltando.add("canalPedido");
        if (body.get("formaPagamento") == null) faltando.add("formaPagamento");
        if (body.get("itens") == null) faltando.add("itens");

        if (!faltando.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "CAMPOS_OBRIGATORIOS",
                            "mensagem", "Campos obrigatorios ausentes: " + faltando));
        }

        Long clienteId = Long.parseLong(body.get("clienteId").toString());
        Long unidadeId = Long.parseLong(body.get("unidadeId").toString());

        Cliente cliente = clienteRepo.findById(clienteId).orElse(null);
        if (cliente == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "CLIENTE_NAO_ENCONTRADO",
                            "mensagem", "Cliente " + clienteId + " nao encontrado."));
        }

        Unidade unidade = unidadeRepo.findById(unidadeId).orElse(null);
        if (unidade == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "UNIDADE_NAO_ENCONTRADA",
                            "mensagem", "Unidade " + unidadeId + " nao encontrada."));
        }

        CanalAtendimento canal;
        try {
            canal = CanalAtendimento.valueOf(body.get("canalPedido").toString().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "CANAL_INVALIDO",
                            "mensagem", "Canal de pedido invalido. Use: APP, TOTEM, BALCAO, PICKUP, WEB."));
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itensReq = (List<Map<String, Object>>) body.get("itens");

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setUnidade(unidade);
        pedido.setCanalPedido(canal);
        pedido.setFormaPagamento(body.get("formaPagamento").toString());
        pedido.setCriadoEm(LocalDateTime.now());

        BigDecimal totalPedido = BigDecimal.ZERO;
        List<ItemPedido> listaItens = new ArrayList<>();

        for (Map<String, Object> itemReq : itensReq) {
            Long produtoId = Long.parseLong(itemReq.get("produtoId").toString());
            int qtd = Integer.parseInt(itemReq.get("quantidade").toString());

            Produto produto = produtoRepo.findById(produtoId).orElse(null);
            if (produto == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erro", "PRODUTO_NAO_ENCONTRADO",
                                "mensagem", "Produto " + produtoId + " nao encontrado."));
            }

            Estoque estoque = estoqueRepo.findByProdutoIdAndUnidadeId(produtoId, unidadeId).orElse(null);
            if (estoque == null || estoque.getQuantidade() < qtd) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("erro", "ESTOQUE_INSUFICIENTE",
                                "mensagem", "Estoque insuficiente para o produto " + produtoId));
            }

            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setQuantidade(qtd);
            item.setPrecoUnitario(produto.getPrecoBase());
            item.setSubtotal(produto.getPrecoBase().multiply(BigDecimal.valueOf(qtd)));
            listaItens.add(item);
            totalPedido = totalPedido.add(item.getSubtotal());
        }

        pedido.setValorTotal(totalPedido);
        pedido.setItens(listaItens);
        Pedido salvo = pedidoRepo.save(pedido);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "pedidoId", salvo.getId(),
                "situacao", salvo.getSituacao(),
                "canalPedido", salvo.getCanalPedido(),
                "valorTotal", salvo.getValorTotal(),
                "situacaoPagamento", salvo.getSituacaoPagamento()
        ));
    }

    @GetMapping
    @Operation(summary = "Listar todos os pedidos, com filtro opcional por canal")
    public ResponseEntity<?> listarPedidos(@RequestParam(required = false) String canalPedido) {
        List<Pedido> lista;
        if (canalPedido != null) {
            try {
                CanalAtendimento canal = CanalAtendimento.valueOf(canalPedido.toUpperCase());
                lista = pedidoRepo.findByCanalPedido(canal);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "CANAL_INVALIDO", "mensagem", "Canal invalido: " + canalPedido));
            }
        } else {
            lista = pedidoRepo.findAll();
        }
        return ResponseEntity.ok(Map.of("total", lista.size(), "pedidos", lista));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID com seus itens")
    public ResponseEntity<?> buscarPedido(@PathVariable Long id) {
        Pedido pedido = pedidoRepo.findById(id).orElse(null);
        if (pedido == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "PEDIDO_NAO_ENCONTRADO", "mensagem", "Pedido " + id + " nao encontrado."));
        }
        return ResponseEntity.ok(pedido);
    }

    @PatchMapping("/{id}/situacao")
    @Operation(summary = "Atualizar situacao do pedido")
    public ResponseEntity<?> atualizarSituacao(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Pedido pedido = pedidoRepo.findById(id).orElse(null);
        if (pedido == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "PEDIDO_NAO_ENCONTRADO", "mensagem", "Pedido nao encontrado."));
        }

        String novaSituacao = body.get("situacao");
        if (novaSituacao == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "CAMPOS_OBRIGATORIOS", "mensagem", "Campo 'situacao' e obrigatorio."));
        }

        try {
            pedido.setSituacao(SituacaoPedido.valueOf(novaSituacao.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "SITUACAO_INVALIDA", "mensagem", "Situacao invalida: " + novaSituacao));
        }

        pedidoRepo.save(pedido);
        return ResponseEntity.ok(Map.of("pedidoId", pedido.getId(), "novaSituacao", pedido.getSituacao()));
    }
}
