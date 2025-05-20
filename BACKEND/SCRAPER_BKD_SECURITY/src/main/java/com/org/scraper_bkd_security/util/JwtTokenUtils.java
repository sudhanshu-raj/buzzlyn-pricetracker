package com.org.scraper_bkd_security.util;

import com.org.scraper_bkd_security.constants.ApplicationConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;
import static com.org.scraper_bkd_security.constants.ApplicationConstants.*;

@Component
public class JwtTokenUtils {

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET_DEFAULT_VALUE.getBytes(StandardCharsets.UTF_8));
    }
    public String generateToken(String email,String role,String phoneNumber) {
        return buildToken(email,role,phoneNumber ,JWT_EXPIRATION);
    }

    private String buildToken(String email,String role,String phoneNumber ,Long expiration) {
       return  Jwts.builder().issuer(JWT_ISSUER)
                .subject(email)
                .claim("authorities",role)
               .claim("phoneNumber",phoneNumber)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + expiration))
                .signWith(getSigningKey()).compact();
    }

    public String generatePasswordResetToken(String email) {
        return Jwts.builder()
                .issuer(JWT_ISSUER)
                .subject(email)
                .claim("type", "password_reset")
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + FORGETPASSWORD_TOKEN_EXPIRATION))
                .signWith(getSigningKey())
                .compact();

    }

    // Validate Token, returns true if not expired else false
    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> String.valueOf(claims.get("authorities")));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public static void main(String[] args) {
        JwtTokenUtils obj=new JwtTokenUtils();
        String token=obj.generateToken("test@gmai.com","ROLE_USER,ROLE_ADMIN","+919060117328");
        //System.out.println("token::"+token);

        Claims claims;
        try{
           String token2="eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJQcmljZVJhZGFyIiwic3ViIjoidGVzdEBnbWFpLmNvbSIsInR5cGUiOiJwYXNzd29yZF9yZXNldCIsImlhdCI6MTc0NTQwMTU5NiwiZXhwIjoxNzQ1NDAxODk2fQ.S6GKw09ZP6bDFhOvRy69IIcKyeJRicRwQIY95xcx63U";
           token2=token;
                    claims= obj.extractAllClaims(token2);
            System.out.println("clams ::"+claims);
            Boolean result=obj.validateToken(token2);
            System.out.println("is token expired:"+result);
            System.out.println("username::"+obj.extractUsername(token2));

        }
        catch (ExpiredJwtException e) {
            System.out.println("Token has expired");
        } catch (JwtException e) {
            System.out.println("Invalid token");
        }

    }

}
