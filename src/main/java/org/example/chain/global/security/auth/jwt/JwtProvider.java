package org.example.chain.global.security.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.lang.System.getenv;

@Component
@Slf4j
public class JwtProvider {

    private String secretKey; // 환경변수에서 key 값 가져오기

    private final Long accessTokenValidity;
    private final Long refreshTokenValidity;

    public JwtProvider(
            @Value("${jwt.access-token-expiration-minutes}") Long accessTokenExpirationMinutes,
            @Value("${jwt.refresh-token-expiration-days}") Long refreshTokenExpirationDays) {
        this.accessTokenValidity = accessTokenExpirationMinutes * 60 * 1000L;
        this.refreshTokenValidity = refreshTokenExpirationDays * 24 * 60 * 60 * 1000L;
        this.secretKey = getenv().get("KEY");
    }




    public String createToken(Claims claims, Long tokenValidity){
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidity);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public String createAccessToken(String username, Collection<? extends GrantedAuthority> roles){
        Claims claims = Jwts.claims() // jwt 내부 본문에 들어갈거
                .setSubject(username);
        claims.put("roles", roles);
        return createToken(claims, accessTokenValidity);
    }

    public String createRefreshToken(String username) {
        Claims claims = Jwts.claims()
                .setSubject(username);
        return createToken(claims, refreshTokenValidity);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("토큰 만료됨");
            return false;
        } catch (MalformedJwtException e) {
            log.warn("토큰 형식 오류");
            return false;
        } catch (UnsupportedJwtException e){
            log.warn("지원하지 않는 JWT");
            return false;
        } catch (SignatureException e){
            log.warn("JWT 서명 불일치");
            return false;
        }
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
