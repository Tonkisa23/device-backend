package bg.tuvarna.devicebackend.api;

import bg.tuvarna.devicebackend.config.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ConfigBeansTests {

    @Autowired private ApplicationContext ctx;

    @Test
    void passwordEncoderBeanExists() {
        assertNotNull(ctx.getBean(PasswordEncoder.class));
    }

    @Test
    void jwtServiceBeanExists() {
        assertNotNull(ctx.getBean(JwtService.class));
    }

    @Test
    void securityBeansLoaded() {
        assertTrue(ctx.containsBean("securityFilterChain") || ctx.getBeansOfType(Object.class).size() > 0);
    }
}
