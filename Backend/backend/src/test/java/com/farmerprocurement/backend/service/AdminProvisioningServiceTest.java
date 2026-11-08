package com.farmerprocurement.backend.service;

import com.farmerprocurement.backend.entity.User;
import com.farmerprocurement.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminProvisioningServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminProvisioningService service;

    @Test
    void provisionsFirstAdminWithHashedPasswordAndNoPublicRoleInput() {
        when(jdbcTemplate.queryForObject(
                eq("SELECT GET_LOCK(?, 0)"), eq(Integer.class), any()))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(
                eq("SELECT RELEASE_LOCK(?)"), eq(Integer.class), any()))
                .thenReturn(1);
        when(userRepository.existsByRole(User.Role.ADMIN)).thenReturn(false);
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("+919876543210")).thenReturn(false);
        when(passwordEncoder.encode("secure-password"))
                .thenReturn("bcrypt-hash");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 41L);
            return saved;
        });

        long userId = service.provisionFirstAdmin(
                "  Admin User ",
                " ADMIN@EXAMPLE.COM ",
                " +919876543210 ",
                LocalDate.of(1980, 1, 1),
                "secure-password".toCharArray());

        assertEquals(41L, userId);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("Admin User", saved.getFullName());
        assertEquals("admin@example.com", saved.getEmail());
        assertEquals(User.Role.ADMIN, saved.getRole());
        assertEquals("bcrypt-hash", saved.getPassword());
        assertTrue(saved.isEmailVerified());
        assertTrue(saved.isPhoneVerified());
        verify(jdbcTemplate).queryForObject(
                "SELECT RELEASE_LOCK(?)", Integer.class, "kisanmitra-admin-bootstrap");
    }

    @Test
    void refusesToProvisionWhenAnAdminAlreadyExists() {
        when(jdbcTemplate.queryForObject(
                eq("SELECT GET_LOCK(?, 0)"), eq(Integer.class), any()))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(
                eq("SELECT RELEASE_LOCK(?)"), eq(Integer.class), any()))
                .thenReturn(1);
        when(userRepository.existsByRole(User.Role.ADMIN)).thenReturn(true);

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> service.provisionFirstAdmin(
                        "Admin User",
                        "admin@example.com",
                        "+919876543210",
                        LocalDate.of(1980, 1, 1),
                        "secure-password".toCharArray()));

        assertTrue(error.getMessage().contains("already exists"));
        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(jdbcTemplate).queryForObject(
                "SELECT RELEASE_LOCK(?)", Integer.class, "kisanmitra-admin-bootstrap");
    }
}
