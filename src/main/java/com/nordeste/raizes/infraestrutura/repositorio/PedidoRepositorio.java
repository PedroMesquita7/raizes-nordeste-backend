package com.nordeste.raizes.infraestrutura.repositorio;

import com.nordeste.raizes.dominio.enumeracao.CanalAtendimento;
import com.nordeste.raizes.dominio.modelo.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepositorio extends JpaRepository<Pedido, Long> {
    List<Pedido> findByCanalPedido(CanalAtendimento canalPedido);
}
