package com.nordeste.raizes.infraestrutura.repositorio;

import com.nordeste.raizes.dominio.modelo.Unidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnidadeRepositorio extends JpaRepository<Unidade, Long> {
}
