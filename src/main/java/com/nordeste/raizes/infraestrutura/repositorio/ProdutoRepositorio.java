package com.nordeste.raizes.infraestrutura.repositorio;

import com.nordeste.raizes.dominio.modelo.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepositorio extends JpaRepository<Produto, Long> {
}
