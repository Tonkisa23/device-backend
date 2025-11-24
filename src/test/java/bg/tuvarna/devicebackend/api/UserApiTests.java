package bg.tuvarna.devicebackend.api;

import bg.tuvarna.devicebackend.config.JwtAuthenticationFilter;
import bg.tuvarna.devicebackend.models.entities.User;
import bg.tuvarna.devicebackend.models.enums.UserRole;
import bg.tuvarna.devicebackend.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.ApplicationContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;

import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserApiTests {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ApplicationContext applicationContext;


    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        User user = User.builder()
                .fullName("gosho")
                .email("gosho@abv.bg")
                .password(passwordEncoder.encode("Az$um_GOSHO123"))
                .role(UserRole.USER)
                .build();

        userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

//    @Test
//    void userRegistrationFailed() throws Exception {
//        MvcResult registration1 = mvc.perform(
//                post("/api/v1/users/registration")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                                {
//                                  "fullName": "Georgi Ivanov",
//                                  "email": "gosho@abv.bg",
//                                  "phone": "123456789",
//                                  "username": "gosho123",
//                                  "password": "Az$um_GOSHO123"
//                                }
//                                """)
//        ).andReturn();
//        assertEquals(400, registration1.getResponse().getStatus());
//
//        ErrorResponse errorResponse = mapper.readValue(
//                registration1.getResponse().getContentAsString(),
//                ErrorResponse.class
//        );
//
//        assertEquals("Email already taken", errorResponse.getError());
//    }
//
//    @Test
//    void userLoginSuccess() throws Exception {
//        mvc.perform(
//                    post("/api/v1/users/login")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content("""
//                                    {
//                                      "username": "gosho@abv.bg",
//                                      "password": "Az$um_GOSHO123"
//                                    }""")
//                )
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.token").isNotEmpty());
//    }

//    @Test
//    void accessProtectedEndpointWithoutToken() throws Exception {
//        mvc.perform(
//                        get("/api/v1/users/getUser")
//                )
//                .andExpect(status().isUnauthorized());
//    }

    @Test
    void loginWithWrongPassword() throws Exception {
        mvc.perform(
                        post("/api/v1/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                              "username": "gosho@abv.bg",
                              "password": "wrong"
                            }
                            """)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithUnknownUser() throws Exception {
        mvc.perform(
                        post("/api/v1/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                              "username": "unknown@abv.bg",
                              "password": "123"
                            }
                            """)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrationServerErrorOnEmptyBody() throws Exception {
        mvc.perform(
                        post("/api/v1/users/registration")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getUserWithoutAuth() throws Exception {
        mvc.perform(
                        get("/api/v1/users/getUser")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidMethodOnLogin() throws Exception {
        mvc.perform(
                        get("/api/v1/users/login")
                )
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void accessProtectedEndpointWithInvalidToken() throws Exception {
        mvc.perform(
                        get("/api/v1/users/getUser")
                                .header("Authorization", "Bearer invalidToken")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpointWithFakeTokenFails() throws Exception {
        mvc.perform(
                        get("/api/v1/users/getUser")
                                .header("Authorization", "Bearer faketoken")
                )
                .andExpect(status().isUnauthorized());
    }


    @Test
    void loginWithInvalidUsername() throws Exception {
        mvc.perform(
                        post("/api/v1/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                              "username": "invalid@user.com",
                              "password": "wrongpassword"
                            }
                            """)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Wrong credentials!"));
    }

    @Test
    void loginWithBrokenJsonTriggersAuthFilterException() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .content("{ invalid json")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void loginWithEmptyBodyTriggersAuthFilter() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .content("")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void accessWithGarbageJwtTokenTriggersJwtFilter() throws Exception {
        mvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer garbage.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessWithMalformedAuthorizationHeaderTriggersJwtFilter() throws Exception {
        mvc.perform(get("/api/users/me")
                        .header("Authorization", "Basic something"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void securityConfigBeansAreLoaded() {
        assertNotNull(applicationContext.getBean(AuthenticationManager.class));
        assertNotNull(applicationContext.getBean(PasswordEncoder.class));
    }

    @Test
    void jwtAuthenticationFilterBeanExists() {
        assertNotNull(applicationContext.getBean(JwtAuthenticationFilter.class));
    }

    @Test
    void openApiConfigBeanLoads() {
        assertNotNull(applicationContext.getBean(OpenAPI.class));
    }

}