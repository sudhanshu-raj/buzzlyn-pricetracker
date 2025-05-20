package com.org.scraper_bkd_security.controllers;

import com.org.scraper_bkd_security.dto.*;
import com.org.scraper_bkd_security.exceptions.OtpSendException;
import com.org.scraper_bkd_security.exceptions.UserRegistrationException;
import com.org.scraper_bkd_security.exceptions.UserRegistrationWarning;
import com.org.scraper_bkd_security.handler.OAuth2SuccessHandler;
import com.org.scraper_bkd_security.models.Customer;
import com.org.scraper_bkd_security.models.SignUpOtpManage;
import com.org.scraper_bkd_security.repo.SignUpOtpRepo;
import com.org.scraper_bkd_security.repo.UserRepo;
import com.org.scraper_bkd_security.services.SignUpService;
import com.org.scraper_bkd_security.services.RateLimitService;
import com.org.scraper_bkd_security.util.AvatarGenerator;
import com.org.scraper_bkd_security.util.JwtTokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.org.scraper_bkd_security.constants.ApplicationConstants.JWT_HEADER;

@RestController()
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginSignUpController {

    private static final Logger logger = LoggerFactory.getLogger(LoginSignUpController.class);

    private final SignUpService signUpService;
    private final SignUpOtpRepo signUpOtpRepo;
    private final UserRepo userRepo;
    private final RateLimitService rateLimitService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtTokenUtils jwtUtil;



    @PostMapping("/checkEmail")
    public ResponseEntity<String> isEmailExists(@RequestBody CheckUserExistsRequest requestData){
        try{
            boolean isExists= signUpService.isEmailExists(requestData);
            if(isExists){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Email exists already");
            }
            return ResponseEntity.ok("Good to go");
        }
        catch(Exception e){
            logger.error("Error hitting /checkEmail api : {}",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to check the email existence now");
        }
    }

    @PostMapping("/checkPhone")
    public ResponseEntity<String> isPhoneExists(@RequestBody CheckUserExistsRequest requestData){
        try{
            boolean isExists= signUpService.isPhoneExists(requestData);
            if(isExists){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Phone number exists already");
            }
            return ResponseEntity.ok("Good to go");
        }
        catch(Exception e){
            logger.error("Error hitting /checkPhone api : {}",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to check the phone number existence now");
        }
    }

    @PostMapping("/preRegisterCheck")
    public ResponseEntity<String> checkNewUser(@Valid @RequestBody CustomerDTO customerDTO){
        try{
            if(!signUpService.isUserExists(customerDTO)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body("Good to go");
            }
            else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User already exists");
            }

        }
        catch(UserRegistrationWarning e){
            throw e;
        }
        catch (Exception e){
            logger.error("Error during registration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error, try again");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody CustomerDTO customerDTO, HttpServletResponse response){
            try{

                Customer savedCustomer= signUpService.register(customerDTO);
                logger.info("New User Created of id : {}", savedCustomer.getId());
                String token = jwtUtil.generateToken(customerDTO.getEmail(),"ROLE_USER",savedCustomer.getPhoneNumber());
              response.addCookie(oAuth2SuccessHandler.createSecureCookie2(JWT_HEADER, token));
                Map<String,Object> result=new HashMap<>();
                result.put("isAuthenticated",true);
                result.put("user",savedCustomer);

                String profilePicBase64=null;
                if(savedCustomer.getImageData()!=null){
                    profilePicBase64=Base64.getEncoder().encodeToString(savedCustomer.getImageData());
                }
                result.put("profilePic",(profilePicBase64 != null ? profilePicBase64 : ""));
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(result);
            }
            catch(UserRegistrationWarning e){
                throw e;
            }
            catch(Exception e){
                logger.error("Error during registration: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Unexpected error, try again");
            }
    }

    @PostMapping("/registerOthUsr")
    public ResponseEntity<?> registerOAuthUser(@Valid @RequestBody OAuthUserRequest requestBody,HttpServletResponse response){
        try{
            Customer savedCustomer= signUpService.oauthUserRegister(requestBody);
            logger.info("New OAuth User Created of id : {}", savedCustomer.getId());
            String token = jwtUtil.generateToken(requestBody.getEmail(),"ROLE_USER",savedCustomer.getPhoneNumber());
            response.addCookie(oAuth2SuccessHandler.createSecureCookie2(JWT_HEADER, token));
            Map<String,Object> result=new HashMap<>();
            result.put("isAuthenticated",true);
            result.put("user",savedCustomer);

            String profilePicBase64=null;
            if(savedCustomer.getImageData()!=null){
                profilePicBase64=Base64.getEncoder().encodeToString(savedCustomer.getImageData());
            }
            result.put("profilePic",(profilePicBase64 != null ? profilePicBase64 : ""));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(result);
        }
        catch(UserRegistrationWarning | UserRegistrationException e){
            throw e;
        }
        catch(Exception e){
            logger.error("Error during registration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error, try again");
        }
    }


    @PostMapping("/signUpOtpSend")
    public ResponseEntity<?> signUpOtpSend(@Valid @RequestBody OtpRequestDTO requestBody, HttpServletRequest request){
        try{
            String clientIp = request.getRemoteAddr();
            String key="signUp::"+requestBody.getUserId()+":"+clientIp;

            if (rateLimitService.isUserRateLimited(key)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body("Too many OTP requests. Please try again after an hour.");
            }
                //commenting temporarily for development phase only , required for production
//            if (rateLimitService.isIpRateLimited(clientIp)) {
//                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
//                        .body("Too many OTP requests from this IP address. Please try again after an hour.");
//            }

            OtpResponseDTO otpResponseDto= signUpService.emailOtpSend(requestBody);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(otpResponseDto);
        }
        catch(OtpSendException e){
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("UserID already exists");
        }
        catch(Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating OTP");
        }
    }

    @PostMapping("/signUpOtpVerify")
    public ResponseEntity<?> signUpOtpVerification(@Valid @RequestBody VerifySignUpOtpRequest verifySignUpOtpRequest, HttpServletRequest request){
        try{
            String ip=request.getRemoteAddr();
            String userId=verifySignUpOtpRequest.getPt_ky();
            if (rateLimitService.isOtpBruteForceProtected(userId,ip,"signUp")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body("Too many failed OTP attempts. Please try again after an hour.");
            }
            Map<String,Boolean> result= signUpService.verifyOtp(verifySignUpOtpRequest);
            if(result.get("isOtpMatched")!=null && result.get("isOtpMatched")){
                rateLimitService.resetRateLimitOnSuccess(userId,ip,"signUp");
                return ResponseEntity.ok("OTP matched");
            }
            else if (result.get("expiredOtp")!=null && result.get("expiredOtp")){
                rateLimitService.recordFailedLoginAttempt(userId,ip,"signUp");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("OTP Expired");
            }
            else {
                rateLimitService.recordFailedLoginAttempt(userId,ip,"signUp");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid OTP");
            }
        }

        catch(Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error verifying OTP");
        }

    }

    @PostMapping("/checkUsrOtpVrfd")
    public ResponseEntity<?> checkUserOtpVerified(@RequestParam String userId){
        try{
            logger.debug("userId going to check for otpverified::{}",userId);
            List<SignUpOtpManage> existedData=signUpOtpRepo.findAllByUserID(userId);
            logger.debug("Existed data: {}", existedData);
            Map<String,Boolean> response=new HashMap<>();
            response.put("isUserNeedVerification",true);

            if(existedData!=null && !existedData.isEmpty() && (existedData.getFirst().isVerified() &&  !signUpService.isOtpExpired(existedData.getFirst().getUpdated(), 5) )){
                response.put("isUserNeedVerification",false);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(response);

        }
        catch(Exception e){
            logger.error("Error at /checkUsrOtpVrfd , : {}",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to check user otp verification");
        }
    }

    @GetMapping("/secure")
    public ResponseEntity<String> securePage(Authentication authentication) {
        if(authentication instanceof UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken){
            System.out.println(usernamePasswordAuthenticationToken);
        } else if (authentication instanceof OAuth2AuthenticationToken oAuth2AuthenticationToken) {
            System.out.println(oAuth2AuthenticationToken);
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

            // Get authorized client
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
            );
            if (client == null) {
                System.out.println("OAuth2AuthorizedClient is NULL!");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("client is null");
            }

            // Extract tokens
            String accessToken = client.getAccessToken().getTokenValue();
            OAuth2RefreshToken refreshToken = client.getRefreshToken();
            System.out.println("access token::"+accessToken);
            System.out.println("refresh token : "+refreshToken.getTokenValue());
        }
        return ResponseEntity.ok("Hello dear user, you successfully authenticated, these are your details )::");
    }

    @GetMapping("/tokens")
    public ResponseEntity<String> getTokens(
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client
    ) {
        String accessToken = client.getAccessToken().getTokenValue();
        OAuth2RefreshToken refreshToken = client.getRefreshToken();
        System.out.println("Access Token: " + accessToken);
        System.out.println("Refresh Token: " + refreshToken.getTokenValue());
        return ResponseEntity.ok("ok");
    }


}
