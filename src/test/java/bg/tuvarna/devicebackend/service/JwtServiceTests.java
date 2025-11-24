package bg.tuvarna.devicebackend.service;

import bg.tuvarna.devicebackend.config.JwtService;
import bg.tuvarna.devicebackend.controllers.exceptions.CustomException;
import bg.tuvarna.devicebackend.models.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtServiceTests {

    @Autowired private JwtService jwtService;

    @Test
    void extractId_invalidToken_throws() {
        assertThrows(CustomException.class, () -> jwtService.extractId("not.a.jwt"));
    }

    @Test
    void isTokenValid_malformedToken_throws() {
        User user = User.builder().id(1L).build();
        assertThrows(CustomException.class, () -> jwtService.isTokenValid("garbage.jwt.token", user));
    }

    @Test
    void isTokenValid_nullToken_throws() {
        User user = User.builder().id(1L).build();
        assertThrows(Exception.class, () -> jwtService.isTokenValid(null, user));
    }

    @Test
    void generateToken_twoUsersDifferentTokens() {
        User u1 = User.builder().id(1L).build();
        User u2 = User.builder().id(2L).build();

        String t1 = jwtService.generateToken(u1);
        String t2 = jwtService.generateToken(u2);

        assertNotNull(t1);
        assertNotNull(t2);
        assertNotEquals(t1, t2);
    }

    @Test
    void extractId_validToken_returnsId() {
        User user = new User();
        user.setId(5L);

        String token = jwtService.generateToken(user);

        Long id = Long.valueOf(jwtService.extractId(token));

        assertEquals(5L, id);
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        User user = new User();
        user.setId(3L);

        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token, user));
    }

}
