package com.farmerprocurement.backend.config;

import com.farmerprocurement.backend.service.AdminProvisioningService;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.Console;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;

@Component
@ConditionalOnProperty(name = "app.provision-admin", havingValue = "true")
public class AdminProvisioningCommand implements ApplicationRunner {

    private final AdminProvisioningService provisioningService;
    private final ApplicationContext applicationContext;

    public AdminProvisioningCommand(
            AdminProvisioningService provisioningService,
            ApplicationContext applicationContext) {
        this.provisioningService = provisioningService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        if (applicationContext instanceof WebServerApplicationContext) {
            throw new IllegalStateException(
                    "Admin provisioning is disabled in web-server mode; use the standalone command.");
        }
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException(
                    "Admin provisioning requires an interactive terminal; no credentials were read.");
        }

        String fullName = required(console.readLine("Administrator full name: "), "Full name");
        String email = required(console.readLine("Administrator email: "), "Email");
        String phoneNumber = required(console.readLine("Phone number (+countrycode): "), "Phone number");
        String birthDateText = required(
                console.readLine("Date of birth (YYYY-MM-DD): "),
                "Date of birth");
        LocalDate dateOfBirth;
        try {
            dateOfBirth = LocalDate.parse(birthDateText);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Date of birth must use YYYY-MM-DD format.", exception);
        }
        if (!dateOfBirth.isBefore(LocalDate.now())
                || Period.between(dateOfBirth, LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("Administrator must be at least 18 years old.");
        }
        if (!phoneNumber.matches("^\\+[1-9]\\d{7,14}$")) {
            throw new IllegalArgumentException(
                    "Phone number must include its country code, for example +919876543210.");
        }

        char[] password = console.readPassword("New password (minimum 12 characters): ");
        char[] confirmation = console.readPassword("Confirm password: ");
        try {
            if (password == null || password.length < 12) {
                throw new IllegalArgumentException("Password must contain at least 12 characters.");
            }
            if (!Arrays.equals(password, confirmation)) {
                throw new IllegalArgumentException("Passwords do not match.");
            }
            long userId = provisioningService.provisionFirstAdmin(
                    fullName, email, phoneNumber, dateOfBirth, password);
            console.printf("First administrator created successfully (user ID %d).%n", userId);
        } finally {
            if (password != null) {
                Arrays.fill(password, '\0');
            }
            if (confirmation != null) {
                Arrays.fill(confirmation, '\0');
            }
        }
    }

    private String required(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        return value;
    }
}
