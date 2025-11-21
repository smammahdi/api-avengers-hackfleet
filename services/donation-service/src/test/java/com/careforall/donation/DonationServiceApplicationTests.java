package com.careforall.donation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Donation Service Application Tests
 *
 * Basic smoke test to ensure the application context loads successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
class DonationServiceApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures that the Spring application context loads successfully
    }
}
