package auth_server.service.imp;

import auth_server.dtos.request.RequestRegister;
import auth_server.dtos.response.TokenResponse;
import auth_server.enums.AuthError;
import auth_server.enums.AuthMessage;
import auth_server.exception.IdRoleNotFoundException;
import auth_server.exception.RepeatUserException;
import auth_server.model.EcUserModel;
import auth_server.model.UserRoleModel;
import auth_server.repository.EcUserRepository;
import auth_server.repository.UserRoleRepository;
import auth_server.service.AuthService;
import auth_server.util.TokenExchangeUtil;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final EcUserRepository ecUserRepository;
  private final UserRoleRepository userRoleRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenExchangeUtil exchangeAuthorizationCodeForTokenUtil;


  @Override
  public String register(RequestRegister request) {
    if (Boolean.TRUE.equals(ecUserRepository.existsByUsername(request.getEmail()))) {
      throw RepeatUserException
          .builder()
          .message(AuthError.AUTH_ERROR_0002.getDescription())
          .build();
    }
    UserRoleModel userRoleModel = userRoleRepository.findByRoleDescription("ROLE_USER").orElseThrow(
        () -> IdRoleNotFoundException
            .builder()
            .message(AuthError.AUTH_ERROR_0003.getDescription())
            .build());

    EcUserModel userModel = EcUserModel
        .builder()
        .username(request.getEmail().trim())
        .password(passwordEncoder.encode(request.getPassword()))
        .userRole(userRoleModel)
        .creationDate(new Date())
        .build();
    ecUserRepository.save(userModel);

    return AuthMessage.AUTH_MESSAGES_1.getDescription();
  }

  @Override
  public TokenResponse exchangeToken(String code, String codeVerifier) {
    return exchangeAuthorizationCodeForTokenUtil.exchangeAuthorizationCodeForToken(code,
        codeVerifier);
  }

}
