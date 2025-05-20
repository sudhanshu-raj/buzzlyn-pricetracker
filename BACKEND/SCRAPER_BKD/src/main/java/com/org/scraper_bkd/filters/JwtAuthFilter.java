//package com.org.scraper_bkd.filters;
//
//import com.org.scraper_bkd.utils.JwtUtil;
//import io.jsonwebtoken.ExpiredJwtException;
//import io.jsonwebtoken.JwtException;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.hibernate.annotations.Filter;
//import org.slf4j.LoggerFactory;
//import org.slf4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.List;
//
//import static com.org.scraper_bkd.constants.AppConstant.JWT_HEADER;
//
//
//@Component
//@RequiredArgsConstructor
//public class JwtAuthFilter extends OncePerRequestFilter {
//
//    private static final Logger log =  LoggerFactory.getLogger(JwtAuthFilter.class);
//
//
//    private final JwtUtil jwtUtil;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        //String token = request.getHeader(JWT_HEADER);
//        String token=null;
//        Cookie[] cookies=request.getCookies();
////        for( Cookie cookie : cookies ) {
////            if(cookie.getName().equals(JWT_HEADER)){
////                token=cookie.getValue();
////            }
////            System.out.println(cookie.getName()+" :: "+cookie.getValue());
////        }
//        token="eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJQcmljZVJhZGFyIiwic3ViIjoicmFqc3VkaGFuc2h1OTQzMUBnbWFpbC5jb20iLCJhdXRob3JpdGllcyI6IlJPTEVfVVNFUiIsImlhdCI6MTc0NTM1NzM5NywiZXhwIjoxNzQ3OTQ5Mzk3fQ.7bVEsJECZA1vO0FOrUQFz0D1IzJD8ugXbYikGTEAzJE";
//        if (token != null ) {
//            try {
//                if (jwtUtil.validateToken(token)) {
//                    String username = jwtUtil.extractUsername(token);
//                    String role_str = jwtUtil.extractRole(token);
//                    List<GrantedAuthority> roles = jwtUtil.convertStringToAuthorities(role_str);
//                    UsernamePasswordAuthenticationToken authToken =
//                            new UsernamePasswordAuthenticationToken(username, null, roles);
//
//                    SecurityContextHolder.getContext().setAuthentication(authToken);
//
//                }
//            }
//            catch (ExpiredJwtException e) {
//                log.error("Expired JWT token received! , {}", e.getMessage());
//                response.setStatus(HttpStatus.BAD_REQUEST.value());
//                response.getWriter().write("Error: Expired Token received!");
//            } catch (JwtException e) {
//                log.error("Invalid JWT token received!, {}", e.getMessage());
//                response.setStatus(HttpStatus.BAD_REQUEST.value());
//                response.getWriter().write("Error: Invalid Token received!");
//            }
//            catch (Exception e){
//                log.error("Error while validating jwt token : {}",e.getMessage());
//                response.setStatus(HttpStatus.BAD_REQUEST.value());
//                response.getWriter().write("Error: Invalid Token received!");
//            }
//
//        }
//        else{
//            log.warn("Token not found in header");
//            response.setStatus(HttpStatus.BAD_REQUEST.value());
//            response.getWriter().write("Token not received");
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}
