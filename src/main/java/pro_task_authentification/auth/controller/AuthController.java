package pro_task_authentification.auth.controller;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import pro_task_authentification.auth.model.User;
import pro_task_authentification.auth.service.AuthService;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Value("${jwt.secret}")
    private String jwtSecret; // JWT-Key von .properties

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login (@RequestBody AuthRequest authRequest){
        logger.info("Search for user in DB. UserName: {}", authRequest.getUsername());
        Optional<User> user = authService.authenticate(authRequest.getUsername(), authRequest.getPassword());
        
        if (user.isPresent()){
            //Make JWT
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            String token = Jwts.builder()
                .setSubject(user.get().getId().toString()) // userName
                .setIssuedAt(new Date()) // StartTime
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // FinishTime
                .signWith(key) // SekretKey
                .compact();
            
                logger.info("Key received. UserName: {}", authRequest.getUsername());
            return ResponseEntity.ok(new AuthResponse(token));
        } 
        else {
            logger.warn("Key NOT received. UserName: {}", authRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }
}
