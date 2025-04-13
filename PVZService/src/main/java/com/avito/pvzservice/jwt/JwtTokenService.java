package com.avito.pvzservice.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenService {

  private final SecretKey secretKey; // Секретный ключ для подписи токена
  private final long validityInMilliseconds = 3600000; // Время жизни токена (1 час)

  public JwtTokenService() {
    // Генерация ключа на основе строки
    String base64Key =
        Base64.getEncoder()
            .encodeToString(
                "dsajudsjapifaosdsoikawdsapfhjnasidwejsodhahdwo".getBytes(StandardCharsets.UTF_8));

    // Преобразуйте Base64-строку в SecretKey
    this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Key));
  }

  // Генерация токена
  public String generateToken(String role) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMilliseconds);

    return Jwts.builder()
        .setSubject(role)
        .setIssuedAt(now)
        .setExpiration(validity)
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  // Валидация токена
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(secretKey) // Используем SecretKey
          .build()
          .parseClaimsJws(removeHeaderFromToken(token));
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  // Извлечение имени пользователя из токена
  public String getRoleFromToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(secretKey) // Используем SecretKey
        .build()
        .parseClaimsJws(removeHeaderFromToken(token))
        .getBody()
        .getSubject();
  }

  private String removeHeaderFromToken(String token) {
    return token.substring(7);
  }
}
