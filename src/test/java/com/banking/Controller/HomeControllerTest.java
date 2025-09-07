package com.banking.Controller;

import com.banking.Entity.Account;
import com.banking.Repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for HomeController
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2024
 */
@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private Model model;

    @InjectMocks
    private HomeController homeController;

    private MockMvc mockMvc;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
        
        testAccount = new Account();
        testAccount.setAccountId(1L);
        testAccount.setEmail("test@example.com");
        testAccount.setCustomerName("Test User");
        testAccount.setLevel("SILVER");
    }

    /**
     * Test displaying customer home page
     */
    @Test
    void testHello_ShouldReturnHomePageView() {
        // When
        String result = homeController.hello();

        // Then
        assertEquals("home/homePage", result);
    }

    /**
     * Test displaying admin dashboard
     */
    @Test
    void testAdmin_ShouldReturnAdminDashboardView() {
        // When
        String result = homeController.admin();

        // Then
        assertEquals("home/adminDashboard", result);
    }

    /**
     * Test changing account level successfully
     */
    @Test
    void testChangeLevel_WithValidData_ShouldUpdateLevelAndRedirect() {
        // Given
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));

        // When
        String result = homeController.changeLevel("test@example.com", "GOLD", model);

        // Then
        assertEquals("redirect:/bank/admin", result);
        assertEquals("GOLD", testAccount.getLevel());
        verify(accountRepository).findByEmail("test@example.com");
        verify(accountRepository).save(testAccount);
    }

    /**
     * Test changing account level when account not found
     */
    @Test
    void testChangeLevel_WhenAccountNotFound_ShouldRedirectWithoutUpdate() {
        // Given
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When
        String result = homeController.changeLevel("nonexistent@example.com", "GOLD", model);

        // Then
        assertEquals("redirect:/bank/admin", result);
        verify(accountRepository).findByEmail("nonexistent@example.com");
        verify(accountRepository, never()).save(any(Account.class));
    }

    /**
     * Test changing account level with different case
     */
    @Test
    void testChangeLevel_WithLowerCaseLevel_ShouldConvertToUpperCase() {
        // Given
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));

        // When
        String result = homeController.changeLevel("test@example.com", "platinum", model);

        // Then
        assertEquals("redirect:/bank/admin", result);
        assertEquals("PLATINUM", testAccount.getLevel());
        verify(accountRepository).findByEmail("test@example.com");
        verify(accountRepository).save(testAccount);
    }

    /**
     * Test home page display via MockMvc
     */
    @Test
    void testHello_WithMockMvc_ShouldReturnHomePage() throws Exception {
        mockMvc.perform(get("/bank"))
                .andExpect(status().isOk())
                .andExpect(view().name("home/homePage"));
    }

    /**
     * Test admin dashboard display via MockMvc
     */
    @Test
    void testAdmin_WithMockMvc_ShouldReturnAdminDashboard() throws Exception {
        mockMvc.perform(get("/bank/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("home/adminDashboard"));
    }

    /**
     * Test change level via MockMvc
     */
    @Test
    void testChangeLevel_WithMockMvc_ShouldRedirectToAdmin() throws Exception {
        // Given
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));

        // When & Then
        mockMvc.perform(post("/bank/admin/change-level")
                .param("email", "test@example.com")
                .param("level", "GOLD"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bank/admin"));

        verify(accountRepository).findByEmail("test@example.com");
        verify(accountRepository).save(testAccount);
    }

    /**
     * Test change level with empty email via MockMvc
     */
    @Test
    void testChangeLevel_WithEmptyEmail_ShouldStillRedirect() throws Exception {
        // Given
        when(accountRepository.findByEmail("")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/bank/admin/change-level")
                .param("email", "")
                .param("level", "GOLD"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bank/admin"));

        verify(accountRepository).findByEmail("");
        verify(accountRepository, never()).save(any(Account.class));
    }

    /**
     * Test change level with null level via MockMvc
     */
    @Test
    void testChangeLevel_WithNullLevel_ShouldHandleGracefully() throws Exception {
        // Given
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));

        // When & Then
        mockMvc.perform(post("/bank/admin/change-level")
                .param("email", "test@example.com")
                .param("level", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bank/admin"));

        verify(accountRepository).findByEmail("test@example.com");
        verify(accountRepository).save(testAccount);
    }
}
