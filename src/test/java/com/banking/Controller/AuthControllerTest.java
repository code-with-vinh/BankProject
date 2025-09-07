package com.banking.Controller;

import com.banking.Entity.Account;
import com.banking.Repository.AccountRepository;
import com.banking.Service.AccountService;
import com.banking.Service.AuthService;
import com.banking.Security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2024
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private AuthService authService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Model model;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        
        testAccount = new Account();
        testAccount.setAccountId(1L);
        testAccount.setEmail("test@example.com");
        testAccount.setPassword("password123");
        testAccount.setCustomerName("Test User");
        testAccount.setRole("Customer");
        testAccount.setPhoneNumber("0123456789");
    }

    /**
     * Test displaying registration form
     */
    @Test
    void testShowRegisterForm_ShouldReturnRegisterView() {
        // When
        String result = authController.showRegisterForm(model);

        // Then
        assertEquals("auth/register", result);
        verify(model).addAttribute(eq("account"), any(Account.class));
    }

    /**
     * Test successful user registration
     */
    @Test
    void testRegister_WithValidData_ShouldRedirectToLogin() {
        // Given
        doNothing().when(authService).register(any(Account.class));

        // When
        String result = authController.register(testAccount, model);

        // Then
        assertEquals("redirect:/auth/login", result);
        verify(authService).register(testAccount);
        verify(model).addAttribute("message", "Đăng ký thành công!");
    }

    /**
     * Test registration failure
     */
    @Test
    void testRegister_WithInvalidData_ShouldReturnRegisterView() {
        // Given
        doThrow(new RuntimeException("Email already exists")).when(authService).register(any(Account.class));

        // When
        String result = authController.register(testAccount, model);

        // Then
        assertEquals("auth/register", result);
        verify(authService).register(testAccount);
        verify(model).addAttribute("error", "Email already exists");
    }

    /**
     * Test displaying login form
     */
    @Test
    void testShowLoginForm_ShouldReturnLoginView() {
        // When
        String result = authController.showLoginForm();

        // Then
        assertEquals("auth/login", result);
    }

    /**
     * Test successful login for customer
     */
    @Test
    void testDoLogin_WithValidCustomerCredentials_ShouldRedirectToBank() {
        // Given
        when(authService.login(anyString(), anyString())).thenReturn(testAccount);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("jwt-token");

        // When
        var result = authController.doLogin("test@example.com", "password123", response, null);

        // Then
        assertNotNull(result);
        assertEquals("/bank", result.getUrl());
        verify(authService).login("test@example.com", "password123");
        verify(jwtUtil).generateToken("test@example.com", "Customer");
        verify(response).addCookie(any(Cookie.class));
    }

    /**
     * Test successful login for admin
     */
    @Test
    void testDoLogin_WithValidAdminCredentials_ShouldRedirectToAdminDashboard() {
        // Given
        Account adminAccount = new Account();
        adminAccount.setAccountId(2L);
        adminAccount.setEmail("admin@example.com");
        adminAccount.setPassword("password123");
        adminAccount.setCustomerName("Admin User");
        adminAccount.setRole("Admin");
        adminAccount.setPhoneNumber("0987654321");
        
        when(authService.login(anyString(), anyString())).thenReturn(adminAccount);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("jwt-token");

        // When
        var result = authController.doLogin("admin@example.com", "password123", response, null);

        // Then
        assertNotNull(result);
        assertEquals("/admin/dashboard", result.getUrl());
        verify(authService).login("admin@example.com", "password123");
        verify(jwtUtil).generateToken("admin@example.com", "Admin");
        verify(response).addCookie(any(Cookie.class));
    }

    /**
     * Test login failure with invalid credentials
     */
    @Test
    void testDoLogin_WithInvalidCredentials_ShouldRedirectToLoginWithError() {
        // Given
        when(authService.login(anyString(), anyString())).thenReturn(null);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        // When
        var result = authController.doLogin("test@example.com", "wrongpassword", response, redirectAttributes);

        // Then
        assertNotNull(result);
        assertEquals("/auth/login", result.getUrl());
        verify(authService).login("test@example.com", "wrongpassword");
        verify(redirectAttributes).addFlashAttribute("error", "Email hoặc mật khẩu không đúng");
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
        verify(response, never()).addCookie(any(Cookie.class));
    }

    /**
     * Test logout functionality
     */
    @Test
    void testLogout_ShouldClearCookieAndRedirectToBank() {
        // When
        var result = authController.logout(response);

        // Then
        assertNotNull(result);
        assertEquals("/bank", result.getUrl());
        verify(response).addCookie(any(Cookie.class));
    }

    /**
     * Test registration form display via MockMvc
     */
    @Test
    void testShowRegisterForm_WithMockMvc_ShouldReturnRegisterPage() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("account"));
    }

    /**
     * Test login form display via MockMvc
     */
    @Test
    void testShowLoginForm_WithMockMvc_ShouldReturnLoginPage() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    /**
     * Test successful registration via MockMvc
     */
    @Test
    void testRegister_WithMockMvc_ShouldRedirectToLogin() throws Exception {
        // Given
        doNothing().when(authService).register(any(Account.class));

        // When & Then
        mockMvc.perform(post("/auth/register")
                .param("email", "test@example.com")
                .param("password", "password123")
                .param("customerName", "Test User")
                .param("phoneNumber", "0123456789"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));

        verify(authService).register(any(Account.class));
    }

    /**
     * Test successful login via MockMvc
     */
    @Test
    void testDoLogin_WithMockMvc_ShouldRedirectToBank() throws Exception {
        // Given
        when(authService.login(anyString(), anyString())).thenReturn(testAccount);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("jwt-token");

        // When & Then
        mockMvc.perform(post("/auth/login")
                .param("email", "test@example.com")
                .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bank"));

        verify(authService).login("test@example.com", "password123");
        verify(jwtUtil).generateToken("test@example.com", "Customer");
    }

    /**
     * Test logout via MockMvc
     */
    @Test
    void testLogout_WithMockMvc_ShouldRedirectToBank() throws Exception {
        mockMvc.perform(get("/auth/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bank"));
    }
}
