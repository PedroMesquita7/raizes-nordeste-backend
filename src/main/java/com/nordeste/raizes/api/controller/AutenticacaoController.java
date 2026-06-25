package com.nordeste.raizes.api.controller;

import com.nordeste.raizes.dominio.modelo.Usuario;
import com.nordeste.raizes.infraestrutura.repositorio.UsuarioRepositorio;
import com.nordeste.raizes.infraestrutura.seguranca.ServicoJwt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/autenticacao")
@Tag(name = "Autenticacao", description = "Endpoints de login e registro")
public class AutenticacaoController {

    private final AuthenticationManager authManager;
    private final ServicoJwt servicoJwt;
    private final UsuarioRepositorio usuarioRepo;
    private final PasswordEncoder encoder;

    public AutenticacaoController(AuthenticationManager authManager, ServicoJwt servicoJwt,
                                   UsuarioRepositorio usuarioRepo, PasswordEncoder encoder) {
        this.authManager = authManager;
        this.servicoJwt = servicoJwt;
        this.usuarioRepo = usuarioRepo;
        this.encoder = encoder;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario e obter token JWT")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String senha = body.get("senha");

        if (email == null || senha == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "CAMPOS_OBRIGATORIOS", "mensagem", "Email e senha sao obrigatorios."));
        }

        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, senha));
            Usuario usuario = (Usuario) auth.getPrincipal();
            String token = servicoJwt.gerarToken(usuario, usuario.getPerfil());
            return ResponseEntity.ok(Map.of("token", token, "perfil", usuario.getPerfil()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "CREDENCIAIS_INVALIDAS", "mensagem", "Email ou senha incorretos."));
        }
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar novo usuario no sistema")
    public ResponseEntity<?> registrar(@RequestBody Map<String, String> body) {
        List<String> faltando = new ArrayList<>();
        if (body.get("nome") == null) faltando.add("nome");
        if (body.get("email") == null) faltando.add("email");
        if (body.get("senha") == null) faltando.add("senha");
        if (body.get("perfil") == null) faltando.add("perfil");

        if (!faltando.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "CAMPOS_OBRIGATORIOS",
                            "mensagem", "Campos obrigatorios ausentes: " + faltando));
        }

        if (usuarioRepo.existsByEmail(body.get("email"))) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", "EMAIL_JA_CADASTRADO", "mensagem", "Este email ja esta em uso."));
        }

        Usuario novo = new Usuario();
        novo.setNome(body.get("nome"));
        novo.setEmail(body.get("email"));
        novo.setSenha(encoder.encode(body.get("senha")));
        novo.setPerfil(body.get("perfil").toUpperCase());
        novo.setCriadoEm(LocalDateTime.now());
        usuarioRepo.save(novo);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensagem", "Usuario cadastrado com sucesso."));
    }
}
