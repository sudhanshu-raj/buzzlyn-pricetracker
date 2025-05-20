package com.org.scraper_bkd_security.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.org.scraper_bkd_security.dto.*;
import com.org.scraper_bkd_security.exceptions.OtpSendException;
import com.org.scraper_bkd_security.exceptions.UserRegistrationException;
import com.org.scraper_bkd_security.exceptions.UserRegistrationWarning;
import com.org.scraper_bkd_security.models.Customer;
import com.org.scraper_bkd_security.models.SignUpOtpManage;
import com.org.scraper_bkd_security.repo.SignUpOtpRepo;
import com.org.scraper_bkd_security.repo.UserRepo;
import com.org.scraper_bkd_security.util.AvatarGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static com.org.scraper_bkd_security.constants.ApplicationConstants.*;

@Service
@RequiredArgsConstructor
public class SignUpService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private static final Logger logger = LoggerFactory.getLogger(SignUpService.class);

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final SignUpOtpRepo signUpOtpRepo;
    private final AvatarGenerator avatarGenerator;

    public boolean isEmailExists(CheckUserExistsRequest data) {
        if (data == null || data.getEmail() == null || data.getEmail().trim().isEmpty()) {
            logger.warn("Invalid request data for email existence check");
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        try {
            Customer existedEmail = userRepo.findUserByEmail(data.getEmail());
            return existedEmail != null;
        } catch (Exception e) {
            logger.error("Error checking if email exists: {}", e.getMessage());
            throw new RuntimeException("Unable to verify email existence");
        }
    }

    public boolean isPhoneExists(CheckUserExistsRequest data) {
        if (data == null || data.getPhoneNumber() == null || data.getPhoneNumber().trim().isEmpty()) {
            logger.warn("Invalid request data for phone number existence check");
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        try {
            Customer existedPhone = userRepo.findUserByPhoneNumber(data.getPhoneNumber());
            return existedPhone != null;
        } catch (Exception e) {
            logger.error("Error checking if phone number exists: {}", e.getMessage());
            throw new RuntimeException("Unable to verify phone number existence");
        }
    }

    public boolean isUserExists(CustomerDTO customerDTO){
        try{
            Customer existedEmail=userRepo.findUserByEmail(customerDTO.getEmail());
            Customer existedPhoneNumber=userRepo.findUserByPhoneNumber(customerDTO.getPhoneNumber());
            if(existedEmail!=null && existedPhoneNumber!=null){
                throw new UserRegistrationWarning("Email & Number already exists",HttpStatus.BAD_REQUEST,"Seems user email and phone already exists in database");
            }
            if(existedEmail!=null){
                throw new UserRegistrationWarning("Email already exists",HttpStatus.BAD_REQUEST,"Seems user email already exists in database");
            }
            if(existedPhoneNumber!=null){
                throw new UserRegistrationWarning("Phone number already exists",HttpStatus.BAD_REQUEST,"Seems user phone number already exists in database");
            }

            return false;
        }
        catch(UserRegistrationWarning e){
            logger.warn("Warning : {} , body : {}", e.getMessage(), e.getResponseBody());
            throw e;
        }
        catch (Exception e){
            logger.error("Error during registration: {}", e.getMessage(), e);
            throw new UserRegistrationException("Unexpected Error while signup", HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());

        }
    }

    private boolean
    isUserVerified(CustomerDTO customerDTO) {
        try{
            Customer existedCustomer=userRepo.findUserByEmail(customerDTO.getEmail());
            if(existedCustomer!=null){
                throw new UserRegistrationWarning("User already exists",HttpStatus.BAD_REQUEST,"Seems user already exists in database");
            }
            if ((customerDTO.getEmail_pt_ky() == null || customerDTO.getEmail_pt_ky().isEmpty()) ||
                    (customerDTO.getPhone_pt_ky()==null || customerDTO.getPhone_pt_ky().isEmpty())){
                throw new UserRegistrationWarning("Missing required params",HttpStatus.BAD_REQUEST,"pt_ky is not in request");
            }
            SignUpOtpManage emailOtpRecord=signUpOtpRepo.findById(SignUpService.decodeLoginId(customerDTO.getEmail_pt_ky())).orElseThrow(
                    () ->  new UserRegistrationWarning("Invalid request",HttpStatus.BAD_REQUEST,"email_pt_ky is not found")
            );

            SignUpOtpManage phoneOtpRecord=signUpOtpRepo.findById(SignUpService.decodeLoginId(customerDTO.getPhone_pt_ky())).orElseThrow(
                    () -> new UserRegistrationWarning("Invalid request",HttpStatus.BAD_REQUEST,"phone_pt_ky is not found")
            );

            if(!customerDTO.getEmail().equalsIgnoreCase(emailOtpRecord.getUserID()) || !emailOtpRecord.isVerified()){
                throw new UserRegistrationWarning("Email not verified",HttpStatus.BAD_REQUEST,"Customer Email not verified");
            }

            if(!customerDTO.getPhoneNumber().equalsIgnoreCase(phoneOtpRecord.getUserID()) || !phoneOtpRecord.isVerified()){
                throw new UserRegistrationWarning("Number not verified",HttpStatus.BAD_REQUEST,"Customer phone number not verified");
            }

            return true;
        }
        catch(UserRegistrationWarning e) {
            logger.warn("Warning : {} , body : {}", e.getMessage(), e.getResponseBody());
            throw e;
        }
        catch(Exception e){
            logger.error(e.getMessage());
            throw new UserRegistrationException("Unexpected Error while signup", HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
        }
    }
    //normal function to register through the form data
    public Customer register(CustomerDTO customerDTO){
        try {
           if (isUserVerified(customerDTO) && !isUserExists(customerDTO)) {
               byte[] profilePic=avatarGenerator.generateAvatar(customerDTO.getFirstName());
               Customer newCustomer = Customer.builder()
                       .firstName(customerDTO.getFirstName())
                       .lastName(customerDTO.getLastName())
                       .email(customerDTO.getEmail())
                       .phoneNumber(customerDTO.getPhoneNumber())
                       .password(customerDTO.getPassword())
                       .isGoogleOAuthUser(false)
                       .isEmailVerified(true)
                       .isPhoneVerified(true)
                       .imageData(profilePic)
                       .role("USER")
                       .build();
               String safePassword = passwordEncoder.encode(newCustomer.getPassword());
               newCustomer.setPassword(safePassword);
               Customer savedData=userRepo.save(newCustomer);
               //also clear the signupotp table data
               clearSignUpOtpTable(SignUpService.decodeLoginId(customerDTO.getEmail_pt_ky()));
               clearSignUpOtpTable(SignUpService.decodeLoginId(customerDTO.getPhone_pt_ky()));
               return savedData;
           }
           else{
               throw new UserRegistrationWarning("User not verified or already exists",HttpStatus.BAD_REQUEST,"Seems user email or phone or maybe both is not verified or may be exists already");
           }
        }
        catch(UserRegistrationWarning e){
            throw e;
        }
        catch (Exception e){
           logger.error(e.getMessage());
           throw new UserRegistrationException("Unexpected Error while signup", HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
        }
    }

    public Customer oauthUserRegister(OAuthUserRequest requestBody){

        String email=requestBody.getEmail();
        try {
            Customer existedData = userRepo.findUserByEmail(email);
            if(existedData==null){
                throw new UserRegistrationException("Google user doesn't exists",HttpStatus.BAD_REQUEST,"Seems google user trying to do registration without verifying the account with google ");
            }
            SignUpOtpManage otpManageData=signUpOtpRepo.findById(SignUpService.decodeLoginId(requestBody.getPt_ky())).orElseThrow(
                    () -> new UserRegistrationWarning("Invalid request",HttpStatus.BAD_REQUEST,"pt_ky is not found")
            );
            if(!otpManageData.isVerified()){
                throw new UserRegistrationWarning("Phone number not verified",HttpStatus.BAD_REQUEST,"Seems user trying to register with google account but phone number is not verified");
            }
            existedData.setPhoneNumber(otpManageData.getUserID());
            existedData.setPhoneVerified(true);
            Customer savedData=userRepo.save(existedData);
            //also clear the singupotp table data
            clearSignUpOtpTable(SignUpService.decodeLoginId(requestBody.getPt_ky()));
            return savedData;

        }
        catch(UserRegistrationWarning e){
            logger.warn("Warning: {} , body {}",e.getMessage(),e.getResponseBody());
            throw e;
        }
        catch(UserRegistrationException e){
            logger.error("Warning: {} , body {}",e.getMessage(),e.getResponseBody());
            throw e;
        }
        catch (Exception e){
            logger.error(e.getMessage());
            throw new UserRegistrationException("Unexpected Error while signup", HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
        }

    }

    public OtpResponseDTO emailOtpSend(OtpRequestDTO otpRequest){
        Random random=new Random();
        int otp=random.nextInt(9000)+1000;

        try {

            String userID = otpRequest.getUserId();
            String isEmail = otpRequest.getIsEmail();
            String otpId = otpRequest.getOtpId();
            boolean isEmail_ = "yes".equalsIgnoreCase(isEmail);

            long id;

            if (otpId != null && !otpId.isEmpty()) {
                //came here hmm, means user re-sending the otp
                long otpId_ = decodeLoginId(otpId);
                id = otpId_;
                SignUpOtpManage existedData = signUpOtpRepo.findById(otpId_).orElse(null);
                if (existedData != null && existedData.isEmail() == isEmail_) {
                    int count = existedData.getOtpCount();
                    existedData.setOtpCount(count + 1);
                    existedData.setOtp(otp);
                    signUpOtpRepo.save(existedData);
                }
            } else {
                //check if same userId exists
                List<SignUpOtpManage> existedData=signUpOtpRepo.findAllByUserID(userID);
                logger.debug("Existed data: {}", existedData);

                //here check if user already verified the otp in last 5 min,if yes then no need to verify
                if(existedData!=null && !existedData.isEmpty() && (existedData.getFirst().isVerified() &&  !isOtpExpired(existedData.getFirst().getUpdated(), 5) )){
                    String userIdType=isEmail_ ? "Email" :"Number";
                    throw new OtpSendException(userIdType+" already verified",HttpStatus.ALREADY_REPORTED,"Can't create otp again for duplicate userId");
                }
                else if(existedData!=null && !existedData.isEmpty()){
                    SignUpOtpManage topExistedData=existedData.getFirst();
                    topExistedData.setExpired(false);
                    topExistedData.setOtp(otp);
                    topExistedData.setOtpCount(topExistedData.getOtpCount()+1);
                    signUpOtpRepo.save(topExistedData);
                    id=topExistedData.getId();
                }
                else {
                    System.out.println("creating new data");
                    SignUpOtpManage newData = SignUpOtpManage.builder()
                            .userID(Encode.forHtml(userID))
                            .isEmail(isEmail_)
                            .otp(otp)
                            .otpCount(0)
                            .build();

                    SignUpOtpManage savedData = signUpOtpRepo.save(newData);
                    id = savedData.getId();
                    System.out.println("id "+id);
                }
            }
            String encodedId = encodeLoginId(id);
            return OtpResponseDTO.builder()
                    .pt_ky(encodedId)
                    .otp(otp)
                    .isEmail(isEmail)
                    .build();

        }
        catch( OtpSendException e){
            logger.error("OtpSendException ::"+e.getMessage());
            throw e;
        }
        catch (Exception e){
            logger.error(e.getMessage());
            throw new OtpSendException("Unexpected error while generating OTP",HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
        }
    }

    public Map<String,Boolean> verifyOtp(VerifySignUpOtpRequest verifySignUpOtpRequest){
        Map <String,Boolean> result=new HashMap<>();
        result.put("isOtpMatched", false);
        try {
            long otpId = decodeLoginId(verifySignUpOtpRequest.getPt_ky());

            SignUpOtpManage existedData = signUpOtpRepo.findById(otpId).orElse(null);
            if (existedData == null) {
                result.put("invalidOtp",true);
                return result;
            }
            boolean isEmail = "yes".equalsIgnoreCase(verifySignUpOtpRequest.getIsEmail());
            if(existedData.isExpired() || isOtpExpired(existedData.getUpdated(),OTP_EXPIRE_TIME)){
                result.put("expiredOtp",true);
                return result;
            }

            if(existedData.isEmail() == isEmail && existedData.getOtp() == verifySignUpOtpRequest.getOtp()) {
                existedData.setExpired(true);
                existedData.setVerified(true);
                signUpOtpRepo.save(existedData);
                result.put("isOtpMatched",true);
            }
            else{
                result.put("invalidOtp",true);
            }
        }
        catch (Exception e){
            logger.error(e.getMessage());
            result.put("invalidOtp",true);
        }
       return result;
    }

    void clearSignUpOtpTable(long id) {
        try {
            Optional<SignUpOtpManage> optionalData = signUpOtpRepo.findById(id);
            if (optionalData.isPresent()) {
                SignUpOtpManage data = optionalData.get();
                signUpOtpRepo.deleteById(id);
                logger.info("SignupOtp data {} , deleted successfully", id);
            }
        } catch (Exception e) {
            logger.error("Error while deleting the singupotp table data : {}", e.getMessage());
        }
    }


    public static String encodeLoginId(long loginId) {
        if (loginId < 1) {
            throw new IllegalArgumentException("Login id must be 1 or greater");
        }
        loginId=loginId+OTPID_KEY;
        String idStr = String.valueOf(loginId);
        return Base64.getEncoder().encodeToString(idStr.getBytes(StandardCharsets.UTF_8));
    }
    public static long decodeLoginId(String encodedLoginId) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedLoginId);
        String decodedStr = new String(decodedBytes, StandardCharsets.UTF_8);
        long loginId = Long.parseLong(decodedStr);
        if (loginId < 1) {
            throw new IllegalArgumentException("Decoded login id must be 1 or greater");
        }
        loginId=loginId-OTPID_KEY;
        return loginId;
    }

    public boolean isOtpExpired(Date updated,long OTP_EXPIRE_TIME) {
        // Convert the old Date to Instant
        Instant updatedInstant = updated.toInstant();
        Instant now = Instant.now();

        Duration duration = Duration.between(updatedInstant, now);
        return duration.toMinutes() > OTP_EXPIRE_TIME;
    }

    private boolean isGoogleTokenValid(String googleRefreshToken) {
        // Call Google's token endpoint to validate refresh token
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("refresh_token", googleRefreshToken);
        params.add("grant_type", "refresh_token");

        try {
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                    "https://oauth2.googleapis.com/token",
                    new HttpEntity<>(params, headers),
                    JsonNode.class
            );
            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        SignUpService service = new SignUpService(null, null, null,null);
        // Manually assign clientId and clientSecret values
        service.clientId = "781281963056-a94cg1n3k2r9sbruvvpnnsvo2raumrbm.apps.googleusercontent.com";
        service.clientSecret = "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";

        // Call isGoogleTokenValid with a dummy refresh token
        String refresh_token="1//0gAZFO1Rar7doCgYIARAAGBASNwF-L9IrZRTgHKx7XgUdOslFP1A172DXSJYb2QukhcqP0fXgvXBYnh_9A-y3zUWQmxOeB6QmVI4";
        boolean valid = service.isGoogleTokenValid(refresh_token);
        System.out.println("Google refresh token valid: " + valid);
    }
}
