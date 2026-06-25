package com.nordeste.raizes.dominio.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
@Getter @Setter
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    @Column(name = "preco_base", nullable = false)
    private BigDecimal precoBase;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "disponivel_junino", nullable = false)
    private Boolean disponivelJunino = false;
}
