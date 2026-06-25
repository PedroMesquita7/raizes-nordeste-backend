package com.nordeste.raizes.infraestrutura.repositorio;

import com.nordeste.raizes.dominio.modelo.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EstoqueRepositorio extends JpaRepository<Estoque, Long> {
    Optional<Estoque> findByProdutoIdAndUnidadeId(Long produtoId, Long unidadeId);
}
