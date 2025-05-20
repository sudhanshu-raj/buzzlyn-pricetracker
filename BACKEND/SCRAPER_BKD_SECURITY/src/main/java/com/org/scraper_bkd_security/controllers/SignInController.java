package com.org.scraper_bkd_security.controllers;

import com.org.scraper_bkd_security.config.EmailOtpAuthenticationToken;
import com.org.scraper_bkd_security.dto.CheckUserExistsRequest;
import com.org.scraper_bkd_security.dto.OtpLoginRequest;
import com.org.scraper_bkd_security.dto.PasswordLoginRequest;
import com.org.scraper_bkd_security.handler.OAuth2SuccessHandler;
import com.org.scraper_bkd_security.models.Customer;
import com.org.scraper_bkd_security.repo.UserRepo;
import com.org.scraper_bkd_security.services.RateLimitService;
import com.org.scraper_bkd_security.services.SignInOtpService;
import com.org.scraper_bkd_security.services.SignUpService;
import com.org.scraper_bkd_security.util.JwtTokenUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static com.org.scraper_bkd_security.constants.ApplicationConstants.JWT_HEADER;
import static com.org.scraper_bkd_security.constants.ApplicationConstants.LOGINOTP_PREFIX;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class SignInController {

    private static final Logger logger = LoggerFactory.getLogger(SignInController.class);

    private final SignInOtpService signInOtpService;
    private final  AuthenticationManager authenticationManager;
    private final JwtTokenUtils jwtUtil;
    private final RateLimitService rateLimitService;
    private final SignUpService signUpService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final UserRepo userRepo;

    @PostMapping("/loginRequest-otp")
    public ResponseEntity<String> requestOtp(@RequestParam(required = true) @Email(message = "Invalid email format") String email,
                                             HttpServletRequest request) {
        String clientIp=request.getRemoteAddr();
        String key="login::"+email+":"+clientIp;
        if (rateLimitService.isUserRateLimited(key)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many OTP requests for this user. Please try again after an hour.");
        }

        String otp= signInOtpService.generateAndSendOtp(email,LOGINOTP_PREFIX);
        return ResponseEntity.ok("OTP sent to email :: "+otp);
    }

    @PostMapping("/otpLogin")
    public ResponseEntity<?> loginWithOtp(@RequestBody OtpLoginRequest loginRequest,HttpServletRequest request, HttpServletResponse response) {
        try {
            //check invalid attempts
            String ip=request.getRemoteAddr();
            String email = loginRequest.getEmail();;
            if (rateLimitService.isOtpBruteForceProtected(email, ip,"login")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body("Too many failed OTP attempts. Please try again later after an hour.");
            }

            try {
                Authentication authentication = authenticationManager.authenticate(
                        new EmailOtpAuthenticationToken(loginRequest.getEmail(), loginRequest.getOtp())
                );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    rateLimitService.resetRateLimitOnSuccess(email, ip,"login");
                    Customer customer=userRepo.findUserByEmail(email);
                    String token = jwtUtil.generateToken(loginRequest.getEmail(), "ROLE_USER",customer.getPhoneNumber());
                    response.addCookie(oAuth2SuccessHandler.createSecureCookie2(JWT_HEADER, token));
                    Map<String,Object> result=new HashMap<>();
                    Customer userData=userRepo.findUserByEmail(email);;
                    if(userData==null){
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unable to extract user data");
                    }
                    result.put("isAuthenticated",true);
                    result.put("user",userData);
                    String  profilePicBase64 =null;
                    if(userData.getImageData()!=null){
                        profilePicBase64=Base64.getEncoder().encodeToString(userData.getImageData());
                    }
                    result.put("profilePic",(profilePicBase64 != null ? profilePicBase64 : ""));
                    return ResponseEntity.ok(result);
            }
            catch(AuthenticationException e){
                rateLimitService.recordFailedLoginAttempt(email, ip,"login");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
            }

        } catch (Exception e) {
            logger.error("Error in loginWithOtp: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request");
        }
    }
    @PostMapping("/passwordLogin")
    public ResponseEntity<?> loginWithPassword(@Valid @RequestBody PasswordLoginRequest request,HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            if(authentication!=null){
                SecurityContextHolder.getContext().setAuthentication(authentication);
                Customer customer=userRepo.findUserByEmail(request.getEmail());
                String token = jwtUtil.generateToken(request.getEmail(),"ROLE_USER",customer.getPhoneNumber());
              response.addCookie(oAuth2SuccessHandler.createSecureCookie2(JWT_HEADER, token));
                Map<String,Object> result=new HashMap<>();
                Customer userData=userRepo.findUserByEmail(request.getEmail());;
                if(userData==null){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unable to extract user data");
                }
                result.put("isAuthenticated",true);
                result.put("user",userData);
                String profilePicBase64=null;
                if(userData.getImageData()!=null){
                    profilePicBase64=Base64.getEncoder().encodeToString(userData.getImageData());
                }
                result.put("profilePic",(profilePicBase64 != null ? profilePicBase64 : ""));
                return ResponseEntity.ok(result);
            }


            return ResponseEntity.ok("bad request");
        } catch (AuthenticationException e) {
            logger.error("seems something went wrong with loginWithPassword : {}",e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
        }
    }

    @PostMapping("/isEmailExists")
    public ResponseEntity<?> isEmailExits(@Valid @RequestBody CheckUserExistsRequest userExistsRequest){
        try{
            boolean isExists= signUpService.isEmailExists(userExistsRequest);
            if(isExists){
                return ResponseEntity.ok("Good to go");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email not exists");
        }
        catch(Exception e){
            logger.error("Error hitting /checkEmail api : {}",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to check the email existence now");
        }
    }

    @GetMapping("/authTokenCheck")
    public ResponseEntity<?> checkAuth(@CookieValue(value = "Authorization", required = false) String jwtToken) {
        try {
            if (jwtToken == null || jwtToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
            }
            try {
                Boolean isTokenExpired = jwtUtil.validateToken(jwtToken);
                if (isTokenExpired) {
                    String username = jwtUtil.extractUsername(jwtToken);
                    // You can fetch user details from DB if needed
                    Map<String,Object> result=new HashMap<>();
                    result.put("authenticated",true);
                    Customer userData=userRepo.findUserByEmail(username);
                    if(userData==null){
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unable to extract user data");
                    }
                    String profilePicBase64=null;
                    if(userData.getImageData()!=null){
                        profilePicBase64=Base64.getEncoder().encodeToString(userData.getImageData());
                    }
                    result.put("profilePic",(profilePicBase64 != null ? profilePicBase64 : ""));
                    result.put("user",userData);
                    return ResponseEntity.ok(result);
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Token");
            } catch (ExpiredJwtException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token expired");
            } catch (JwtException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Token");
            }

        }
        catch(Exception e){
            logger.error("Error while validate jwt token ::{}",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to validate the token");
        }
    }
    //here i have to work on this to make it secure
    @GetMapping("/profilePic")
    public ResponseEntity<?> getProfilePic(@RequestParam String email){
        try{
            Customer customer=userRepo.findUserByEmail(email);
            if(customer==null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid Email");
            }
            String profilePicBase64=null;
            if(customer.getImageData()!=null) {
                profilePicBase64 = Base64.getEncoder().encodeToString(customer.getImageData());
            }
            Map<String ,Object> result=new HashMap<>();
            result.put("profilePic",profilePicBase64!=null?profilePicBase64:"");
            result.put("imageFound",(profilePicBase64!=null));
            return ResponseEntity.ok(result);
        }
        catch(Exception e){
            logger.error("Error while getting profilepic: {}",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while checking the profile pic");
        }
    }

    @GetMapping("/authTokenCheck2")
    public ResponseEntity<?> checkAuthCokiie(@CookieValue(value = "Authorization", required = false) String jwtToken,HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        System.out.println("cookies got from the request::"+ Arrays.toString(cookies));


        if( cookies == null || cookies.length < 1 ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Seems no cookie there");
        }

        Cookie sessionCookie = null;
        System.out.println("iterationg the cookie list , if not empty");
        for( Cookie cookie : cookies ) {
//            System.out.println(cookie.getName()+" :: "+cookie.getValue());
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body("Seems there is no error while getting cookies, ");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,HttpServletResponse response) {
        try {
            Cookie[] cookies = request.getCookies();

            if( cookies == null || cookies.length < 1 ) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Unable to logout");
            }

            Cookie sessionCookie = null;
            System.out.println("iterating the cookie list , if not empty");
            for( Cookie cookie : cookies ) {
                System.out.println(cookie.getName()+" :: "+cookie.getValue());
            }
            response.addCookie(oAuth2SuccessHandler.createSecureCookie2(JWT_HEADER, ""));
            return ResponseEntity.ok("Logged out successfully");
        }
        catch(Exception e){
            logger.error("Error while logout: {}",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while logout");
        }
    }

}
