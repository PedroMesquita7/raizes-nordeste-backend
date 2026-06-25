package com.nordeste.raizes.infraestrutura.repositorio;

import com.nordeste.raizes.dominio.modelo.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {
}
