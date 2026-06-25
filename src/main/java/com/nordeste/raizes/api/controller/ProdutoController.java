package com.nordeste.raizes.api.controller;

import com.nordeste.raizes.dominio.modelo.Produto;
import com.nordeste.raizes.infraestrutura.repositorio.ProdutoRepositorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/produtos")
@Tag(name = "Produtos", description = "Catalogo de produtos da rede")
@SecurityRequirement(name = "bearerAuth")
public class ProdutoController {

    private final ProdutoRepositorio produtoRepo;

    public ProdutoController(ProdutoRepositorio produtoRepo) {
        this.produtoRepo = produtoRepo;
    }

    @GetMapping
    @Operation(summary = "Listar todos os produtos ativos")
    public ResponseEntity<List<Produto>> listar() {
        return ResponseEntity.ok(produtoRepo.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        return produtoRepo.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erro", "PRODUTO_NAO_ENCONTRADO",
                                "mensagem", "Produto " + id + " nao encontrado.")));
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo produto")
    public ResponseEntity<?> cadastrar(@RequestBody Map<String, Object> body) {
        if (body.get("nome") == null || body.get("precoBase") == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "CAMPOS_OBRIGATORIOS",
                            "mensagem", "Campos 'nome' e 'precoBase' sao obrigatorios."));
        }
        Produto p = new Produto();
        p.setNome(body.get("nome").toString());
        p.setPrecoBase(new BigDecimal(body.get("precoBase").toString()));
        if (body.get("descricao") != null) p.setDescricao(body.get("descricao").toString());
        if (body.get("disponivelJunino") != null)
            p.setDisponivelJunino(Boolean.parseBoolean(body.get("disponivelJunino").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoRepo.save(p));
    }
}
