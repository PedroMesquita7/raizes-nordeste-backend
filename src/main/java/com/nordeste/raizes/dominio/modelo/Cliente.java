package com.nordeste.raizes.dominio.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Getter @Setter
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    private String telefone;
    private Integer idade;

    @Column(name = "aceite_lgpd", nullable = false)
    private Boolean aceiteLgpd = false;

    @Column(name = "data_aceite_lgpd")
    private LocalDateTime dataAceiteLgpd;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
}
