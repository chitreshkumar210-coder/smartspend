package com.fullStack.expenseTracker.services.impls;

import com.fullStack.expenseTracker.dto.reponses.ApiResponseDto;
import com.fullStack.expenseTracker.dto.requests.ResetPasswordRequestDto;
import com.fullStack.expenseTracker.dto.requests.SignUpRequestDto;
import com.fullStack.expenseTracker.enums.ApiResponseStatus;
import com.fullStack.expenseTracker.enums.ERole;
import com.fullStack.expenseTracker.exceptions.UserAlreadyExistsException;
import com.fullStack.expenseTracker.factories.RoleFactory;
import com.fullStack.expenseTracker.models.Role;
import com.fullStack.expenseTracker.models.User;
import com.fullStack.expenseTracker.repository.UserRepository;
import com.fullStack.expenseTracker.services.NotificationService;
import com.fullStack.expenseTracker.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"NullAway", "null"})
class AuthServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RoleFactory roleFactory;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "EXPIRY_PERIOD", 60000L);
    }

    @Test
    @DisplayName("save should create user when username and email are available")
    void save_shouldCreateUser() throws Exception {
        SignUpRequestDto request = new SignUpRequestDto("jane", "jane@example.com", "password123");

        when(userService.existsByUsername("jane")).thenReturn(false);
        when(userService.existsByEmail("jane@example.com")).thenReturn(false);
        when(roleFactory.getInstance("user")).thenReturn(new Role(ERole.ROLE_USER));
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<ApiResponseDto<?>> response = authService.save(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ApiResponseDto<?> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiResponseStatus.SUCCESS, body.getStatus());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("save should throw when email already exists")
    void save_shouldThrowWhenEmailExists() throws Exception {
        SignUpRequestDto request = new SignUpRequestDto("jane", "jane@example.com", "password123");

        when(userService.existsByUsername("jane")).thenReturn(false);
        when(userService.existsByEmail("jane@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.save(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("resetPassword should update password when current password matches")
    void resetPassword_shouldUpdatePassword() throws Exception {
        ResetPasswordRequestDto request = new ResetPasswordRequestDto("john@example.com", "oldPass123", "newPass123");
        User storedUser = new User();
        storedUser.setEmail("john@example.com");
        storedUser.setPassword("encoded-old");

        when(userService.existsByEmail("john@example.com")).thenReturn(true);
        when(userService.findByEmail("john@example.com")).thenReturn(storedUser);
        when(passwordEncoder.matches("oldPass123", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("encoded-new");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<ApiResponseDto<?>> response = authService.resetPassword(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ApiResponseDto<?> body = response.getBody();
        assertNotNull(body);
        assertEquals("Reset successful: Password has been successfully reset!", body.getResponse());
        assertEquals("encoded-new", storedUser.getPassword());
    }
}

