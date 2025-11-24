package bg.tuvarna.devicebackend.service;

import bg.tuvarna.devicebackend.config.JwtService;
import bg.tuvarna.devicebackend.controllers.exceptions.CustomException;
import bg.tuvarna.devicebackend.models.dtos.UserCreateVO;
import bg.tuvarna.devicebackend.models.entities.User;
import bg.tuvarna.devicebackend.repositories.UserRepository;
import bg.tuvarna.devicebackend.services.DeviceService;
import bg.tuvarna.devicebackend.services.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTests {
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private DeviceService deviceService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;

//    @Test
//    public void registerUserShouldThrowPhoneExistsException() {
//        UserCreateVO userCreateVO = new UserCreateVO(
//                "Ivan",
//                "123",
//                "Email",
//                "+123",
//                "adress",
//                LocalDate.now(),
//                "123451"
//        );
//
//        when(userRepository.getByPhone("+123")).thenReturn(new User());
//
//        CustomException ex = assertThrows(
//                CustomException.class,
//                ()-> userService.register(userCreateVO)
//        );
//
//        assertEquals("Phone already taken", ex.getMessage());
//    }
//
//    @Test
//    public void registerUserShouldThrowEmailExistsException() {
//        UserCreateVO userCreateVO = new UserCreateVO(
//                "Ivan",
//                "123",
//                "Email",
//                "+123",
//                "adress",
//                LocalDate.now(),
//                "123451"
//        );
//
//        when(userRepository.getByEmail("Email")).thenReturn(new User());
//
//        CustomException ex = assertThrows(
//                CustomException.class,
//                ()-> userService.register(userCreateVO)
//        );
//
//        assertEquals("Email already taken", ex.getMessage());
//    }

    @Test
    public void testRegisterUserSuccessfully() {
        UserCreateVO userCreateVO = new UserCreateVO(
                "Ivan",
                "password123",
                "ivan@abv.bg",
                "+359888000000",
                "Varna",
                LocalDate.of(1999, 5, 10),
                "987654"
        );

        when(userRepository.getByPhone("+359888000000")).thenReturn(null);
        when(userRepository.getByEmail("ivan@abv.bg")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_pwd");

        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(userCreateVO);

        verify(userRepository, times(1)).saveAndFlush(any(User.class));
    }

    @Test
    public void testRegisterUserWithExistingEmail() {
        UserCreateVO userCreateVO = new UserCreateVO(
                "Maria",
                "pass123",
                "maria@abv.bg",
                "+359888777777",
                "Sofia",
                LocalDate.of(2000, 1, 1),
                "999999"
        );

        when(userRepository.getByPhone("+359888777777")).thenReturn(null);
        when(userRepository.getByEmail("maria@abv.bg")).thenReturn(new User());

        CustomException ex = assertThrows(
                CustomException.class,
                () -> userService.register(userCreateVO)
        );

        assertEquals("Email already taken", ex.getMessage());
    }

    @Test
    public void testGetUserByPhone() {
        String phone = "+359888555555";
        User user = User.builder()
                .fullName("Petar Petrov")
                .email("petar@abv.bg")
                .phone(phone)
                .build();

        when(userRepository.findByEmailOrPhone(phone)).thenReturn(java.util.Optional.of(user));

        User foundUser = userService.getUserByUsername(phone);

        assertEquals("Petar Petrov", foundUser.getFullName());
        assertEquals("petar@abv.bg", foundUser.getEmail());
        assertEquals(phone, foundUser.getPhone());
    }

    @Test
    public void testRegisterDeviceAlreadyExists() {
        UserCreateVO userCreateVO = new UserCreateVO(
                "Stefan",
                "pass123",
                "stefan@abv.bg",
                "+359888111222",
                "Burgas",
                LocalDate.of(2022, 5, 20),
                "DEVICE123"
        );

        when(userRepository.getByPhone("+359888111222")).thenReturn(null);
        when(userRepository.getByEmail("stefan@abv.bg")).thenReturn(null);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded_pass");

        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        doThrow(new CustomException("Device already exists", null))
                .when(deviceService).alreadyExist("DEVICE123");

        CustomException ex = assertThrows(
                CustomException.class,
                () -> userService.register(userCreateVO)
        );

        verify(userRepository, times(1)).delete(any(User.class));
        assertEquals("Device already exists", ex.getMessage());
    }

    @Test
    public void testGetUserByEmail() {
        String email = "test@abv.bg";

        User user = User.builder()
                .fullName("Test User")
                .email(email)
                .phone("0888000000")
                .build();

        when(userRepository.findByEmailOrPhone(email))
                .thenReturn(java.util.Optional.of(user));

        User found = userService.getUserByUsername(email);

        assertEquals("Test User", found.getFullName());
        assertEquals(email, found.getEmail());
    }

    @Test
    public void testGetUserByUsernameNotFound() {
        when(userRepository.findByEmailOrPhone("missing"))
                .thenReturn(java.util.Optional.empty());

        CustomException ex = assertThrows(
                CustomException.class,
                () -> userService.getUserByUsername("missing")
        );

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    public void testRegisterUserWithExistingPhone() {
        UserCreateVO vo = new UserCreateVO(
                "Ivan",
                "pass",
                "ivan2@abv.bg",
                "0888123456",
                "Varna",
                LocalDate.now(),
                "DEV1"
        );

        when(userRepository.getByPhone("0888123456"))
                .thenReturn(new User());

        CustomException ex = assertThrows(
                CustomException.class,
                () -> userService.register(vo)
        );

        assertEquals("Phone already taken", ex.getMessage());
    }

    @Test
    public void testRegisterUserPasswordIsEncoded() {
        UserCreateVO vo = new UserCreateVO(
                "Ivan",
                "plainPass",
                "encode@abv.bg",
                "0888999999",
                "Varna",
                LocalDate.now(),
                "DEV2"
        );

        when(userRepository.getByPhone(any())).thenReturn(null);
        when(userRepository.getByEmail(any())).thenReturn(null);
        when(passwordEncoder.encode("plainPass")).thenReturn("ENCODED");

        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        userService.register(vo);

        verify(passwordEncoder, times(1)).encode("plainPass");
    }

    @Test
    public void testRegisterUserCallsDeviceService() {
        UserCreateVO vo = new UserCreateVO(
                "Ivan",
                "pass",
                "device@abv.bg",
                "0888777777",
                "Varna",
                LocalDate.now(),
                "DEVICE-OK"
        );

        when(userRepository.getByPhone(any())).thenReturn(null);
        when(userRepository.getByEmail(any())).thenReturn(null);
        when(passwordEncoder.encode(any())).thenReturn("ENC");

        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        userService.register(vo);

        verify(deviceService, times(1)).alreadyExist("DEVICE-OK");
    }

    @Test
    public void testRegisterUserWithInvalidEmailStillPersists() {
        UserCreateVO vo = new UserCreateVO(
                "Ivan",
                "password123",
                "not-an-email",
                "0888000000",
                "Varna",
                LocalDate.now(),
                "DEV-X"
        );

        when(userRepository.getByPhone(any())).thenReturn(null);
        when(userRepository.getByEmail(any())).thenReturn(null);
        when(passwordEncoder.encode(any())).thenReturn("ENC");

        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        userService.register(vo);

        verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    public void testRegisterUserEncodesShortPassword() {
        UserCreateVO vo = new UserCreateVO(
                "Ivan",
                "123",
                "short@abv.bg",
                "0888111111",
                "Varna",
                LocalDate.now(),
                "DEV-Y"
        );

        when(userRepository.getByPhone(any())).thenReturn(null);
        when(userRepository.getByEmail(any())).thenReturn(null);
        when(passwordEncoder.encode("123")).thenReturn("ENCODED");

        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        userService.register(vo);

        verify(passwordEncoder).encode("123");
    }

    @Test
    public void testRegisterUserWithValidData() {
        UserCreateVO userCreateVO = new UserCreateVO(
                "Ivan",
                "password123",
                "ivan@abv.bg",
                "+359888000000",
                "Varna",
                LocalDate.of(1999, 5, 10),
                "987654"
        );

        when(userRepository.getByPhone("+359888000000")).thenReturn(null);
        when(userRepository.getByEmail("ivan@abv.bg")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_pwd");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(userCreateVO);

        verify(userRepository, times(1)).saveAndFlush(any(User.class));
    }

    @Test
    void jwtGenerateTokenCreatesNonNullToken() {
        User user = User.builder()
                .id(1L)
                .build();

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void jwtExtractIdReturnsCorrectUserId() {
        User user = User.builder()
                .id(42L)
                .build();

        String token = jwtService.generateToken(user);

        String extractedId = jwtService.extractId(token);

        assertEquals("42", extractedId);
    }

    @Test
    void jwtTokenIsValidForCorrectUser() {
        User user = User.builder()
                .id(5L)
                .build();

        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void jwtTokenIsInvalidForDifferentUser() {
        User tokenOwner = User.builder()
                .id(1L)
                .build();

        User otherUser = User.builder()
                .id(2L)
                .build();

        String token = jwtService.generateToken(tokenOwner);

        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    void jwtInvalidTokenThrowsCustomException() {
        User user = User.builder()
                .id(1L)
                .build();

        assertThrows(
                CustomException.class,
                () -> jwtService.isTokenValid("invalid.jwt.token", user)
        );
    }
}
