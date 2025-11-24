package bg.tuvarna.devicebackend.repository;

import bg.tuvarna.devicebackend.controllers.exceptions.CustomException;
import bg.tuvarna.devicebackend.models.dtos.UserCreateVO;
import bg.tuvarna.devicebackend.models.entities.User;
import bg.tuvarna.devicebackend.models.enums.UserRole;
import bg.tuvarna.devicebackend.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepoTests {
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .fullName("gosho")
                .email("gosho@abv.bg")
                .phone("0888123456")
                .role(UserRole.USER)
                .build();

        userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

//    @Test
//    void userFindBySearchName() {
//        User user = userRepository
//                .searchBy("gosho", Pageable.ofSize(1))
//                .getContent()
//                .getFirst();
//        assertEquals("0888123456", user.getPhone());
//    }
//
//    @Test
//    void userFindBySearchPhone() {
//        User user = userRepository.searchBy("0888123456", Pageable.ofSize(1)).getContent().getFirst();
//        assertEquals("gosho", user.getFullName());
//    }

    @Test
    void findAllUsersTest() {
        List<User> users = userRepository.findAll();

        assertEquals(1, users.size());
        assertEquals("gosho", users.getFirst().getFullName());

        users.forEach(u ->
                System.out.println("User: " + u.getFullName() + ", Email: " + u.getEmail() + ", Phone: " + u.getPhone())
        );
    }

    @Test
    void deleteUserTest() {
        User user = userRepository.findAll().getFirst();
        userRepository.deleteById(user.getId());

        assertEquals(0, userRepository.count());
    }

    @Test
    void userFindBySearchEmail() {
        User user = userRepository.searchBy("gosho@abv.bg", Pageable.ofSize(1)).getContent().getFirst();
        assertEquals("gosho", user.getFullName());
    }

    @Test
    void findUserByEmail() {
        User user = userRepository.getByEmail("gosho@abv.bg");
        assertEquals("gosho", user.getFullName());
    }

    @Test
    void findUserByPhone() {
        User user = userRepository.getByPhone("0888123456");
        assertEquals("gosho", user.getFullName());
    }

    @Test
    void searchByNameReturnsResult() {
        User user = userRepository
                .searchBy("gosho", Pageable.ofSize(5))
                .getContent()
                .getFirst();

        assertEquals("gosho@abv.bg", user.getEmail());
    }

    @Test
    void searchByPhoneReturnsResult() {
        User user = userRepository
                .searchBy("0888123456", Pageable.ofSize(5))
                .getContent()
                .getFirst();

        assertEquals("gosho", user.getFullName());
    }

    @Test
    void searchByUnknownReturnsEmpty() {
        List<User> users = userRepository
                .searchBy("unknown", Pageable.ofSize(5))
                .getContent();

        assertEquals(0, users.size());
    }

    @Test
    void userFindByPartialName() {
        List<User> users = userRepository.searchBy("gos", Pageable.ofSize(5)).getContent();
        assertFalse(users.isEmpty());
        assertEquals("gosho", users.get(0).getFullName());
    }

    @Test
    void userFindByInvalidPhone() {
        List<User> users = userRepository.searchBy("1234567890", Pageable.ofSize(5)).getContent();
        assertTrue(users.isEmpty());
    }

    @Test
    void userFindByEmailCaseInsensitive() {
        User user = userRepository.searchBy("GOSHO@ABV.bg", Pageable.ofSize(1)).getContent().getFirst();
        assertEquals("gosho", user.getFullName());
    }


}