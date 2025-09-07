package com.banking.Service;

import com.banking.Entity.Account;
import com.banking.Repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2024
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AuthService authService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAccountId(1L);
        testAccount.setEmail("test@example.com");
        testAccount.setPassword("password123");
        testAccount.setCustomerName("Test User");
        testAccount.setRole("Customer");
        testAccount.setPhoneNumber("0123456789");
    }

    /**
     * Test successful account existence check
     */
    @Test
    void testAccountExist_WhenAccountExists_ShouldReturnTrue() {
        // Given
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));

        // When
        boolean result = authService.accountExist(testAccount);

        // Then
        assertTrue(result);
        verify(accountRepository).findByEmail(testAccount.getEmail());
    }

    /**
     * Test account existence check when account doesn't exist
     */
    @Test
    void testAccountExist_WhenAccountDoesNotExist_ShouldReturnFalse() {
        // Given
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When
        boolean result = authService.accountExist(testAccount);

        // Then
        assertFalse(result);
        verify(accountRepository).findByEmail(testAccount.getEmail());
    }

    /**
     * Test successful user registration
     */
    @Test
    void testRegister_WhenAccountDoesNotExist_ShouldRegisterSuccessfully() {
        // Given
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        authService.register(testAccount);

        // Then
        verify(accountRepository).findByEmail(testAccount.getEmail());
        verify(accountRepository).save(any(Account.class));
    }

    /**
     * Test registration failure when account already exists
     */
    @Test
    void testRegister_WhenAccountAlreadyExists_ShouldThrowException() {
        // Given
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(testAccount);
        });

        assertEquals("Tài khoản đã tồn tại, vui lòng chọn username khác!", exception.getMessage());
        verify(accountRepository).findByEmail(testAccount.getEmail());
        verify(accountRepository, never()).save(any(Account.class));
    }

    /**
     * Test successful login with valid credentials
     */
    @Test
    void testLogin_WithValidCredentials_ShouldReturnAccount() {
        // Given
        when(accountService.findByEmail(anyString())).thenReturn(testAccount);

        // When
        Account result = authService.login("test@example.com", "password123");

        // Then
        assertNotNull(result);
        assertEquals(testAccount.getEmail(), result.getEmail());
        assertEquals(testAccount.getPassword(), result.getPassword());
        verify(accountService).findByEmail("test@example.com");
    }

    /**
     * Test login failure with invalid email
     */
    @Test
    void testLogin_WithInvalidEmail_ShouldReturnNull() {
        // Given
        when(accountService.findByEmail(anyString())).thenReturn(null);

        // When
        Account result = authService.login("invalid@example.com", "password123");

        // Then
        assertNull(result);
        verify(accountService).findByEmail("invalid@example.com");
    }

    /**
     * Test login failure with invalid password
     */
    @Test
    void testLogin_WithInvalidPassword_ShouldReturnNull() {
        // Given
        when(accountService.findByEmail(anyString())).thenReturn(testAccount);

        // When
        Account result = authService.login("test@example.com", "wrongpassword");

        // Then
        assertNull(result);
        verify(accountService).findByEmail("test@example.com");
    }

    /**
     * Test login with null account
     */
    @Test
    void testLogin_WithNullAccount_ShouldReturnNull() {
        // Given
        when(accountService.findByEmail(anyString())).thenReturn(null);

        // When
        Account result = authService.login("test@example.com", "password123");

        // Then
        assertNull(result);
        verify(accountService).findByEmail("test@example.com");
    }
}
