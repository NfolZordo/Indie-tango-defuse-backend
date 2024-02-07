package indie.tango.defuse.auth;

import indie.tango.defuse.config.JwtService;
import indie.tango.defuse.enums.Role;
import indie.tango.defuse.enums.TokenType;
import indie.tango.defuse.models.Token;
import indie.tango.defuse.models.User;
import indie.tango.defuse.repositoriy.TokenRepository;
import indie.tango.defuse.repositoriy.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final UserRepository userRepository;
  private final TokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthenticationResponse register(AuthenticationRequest request) {

    var password = passwordEncoder.encode(request.getPassword());
    var user = User.builder()
        .email(request.getEmail())
        .password(password)
        .role(Role.USER)
        .build();
//    userRepository.save(new User(request.getEmail(),request.getPassword(),Role.USER));
    userRepository.save(user);
//    saveUser(request.getEmail(),request.getPassword());
    var jwtToken = jwtService.generateToken(user);
    saveUserToken(user, jwtToken);
    return AuthenticationResponse.builder()
        .token(jwtToken)
        .build();
  }

  @Transactional
  private void saveUser (String email, String password) {
    userRepository.saveUser(email,password);
  }

  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword()
        )
    );
    var user = userRepository.findByEmail(request.getEmail())
        .orElseThrow();
    var jwtToken = jwtService.generateToken(user);
    revokeAllUserTokens(user);
    saveUserToken(user, jwtToken);
    return AuthenticationResponse.builder()
        .token(jwtToken)
        .build();
  }

  private void saveUserToken(User user, String jwtToken) {
    User persistedUser = userRepository.findByEmail(user.getEmail()).orElse(null);
    if (persistedUser == null) {
      persistedUser = userRepository.save(user);
    }
    var token = Token.builder()
            .user(persistedUser)
            .token(jwtToken)
            .tokenType(TokenType.BEARER)
            .expired(false)
            .revoked(false)
            .build();
    tokenRepository.save(token);
  }

  private void revokeAllUserTokens(User user) {
    var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
    if (validUserTokens.isEmpty())
      return;
    validUserTokens.forEach(token -> {
      token.setExpired(true);
      token.setRevoked(true);
    });
    tokenRepository.saveAll(validUserTokens);
  }
}
