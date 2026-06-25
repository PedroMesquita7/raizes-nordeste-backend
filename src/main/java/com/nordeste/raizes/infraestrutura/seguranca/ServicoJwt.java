package com.nordeste.raizes.infraestrutura.seguranca;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class ServicoJwt {

    @Value("${app.jwt.secret}")
    private String segredoJwt;

    @Value("${app.jwt.expiracao-ms}")
    private long expiracaoMs;

    private SecretKey obterChave() {
        return Keys.hmacShaKeyFor(segredoJwt.getBytes(StandardCharsets.UTF_8));
    }

    public String gerarToken(UserDetails usuario, String perfil) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("perfil", perfil);

        return Jwts.builder()
                .claims(claims)
                .subject(usuario.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiracaoMs))
                .signWith(obterChave())
                .compact();
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public boolean tokenValido(String token, UserDetails usuario) {
        String email = extrairEmail(token);
        return email.equals(usuario.getUsername()) && !tokenExpirado(token);
    }

    private boolean tokenExpirado(String token) {
        return extrairClaims(token).getExpiration().before(new Date());
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(obterChave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
