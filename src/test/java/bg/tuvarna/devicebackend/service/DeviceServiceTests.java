package bg.tuvarna.devicebackend.service;

import bg.tuvarna.devicebackend.controllers.exceptions.CustomException;
import bg.tuvarna.devicebackend.models.dtos.DeviceCreateVO;
import bg.tuvarna.devicebackend.models.dtos.DeviceUpdateVO;
import bg.tuvarna.devicebackend.models.entities.Device;
import bg.tuvarna.devicebackend.models.entities.Passport;
import bg.tuvarna.devicebackend.models.entities.User;
import bg.tuvarna.devicebackend.models.enums.UserRole;
import bg.tuvarna.devicebackend.repositories.DeviceRepository;
import bg.tuvarna.devicebackend.repositories.PassportRepository;
import bg.tuvarna.devicebackend.repositories.UserRepository;
import bg.tuvarna.devicebackend.services.DeviceService;
import bg.tuvarna.devicebackend.utils.CustomPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DeviceServiceTests {

    @Autowired private DeviceService deviceService;
    @Autowired private DeviceRepository deviceRepository;
    @Autowired private PassportRepository passportRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void cleanDb() {
        jdbc.execute("DELETE FROM renovations");
        jdbc.execute("DELETE FROM devices");
        jdbc.execute("DELETE FROM users");
        jdbc.execute("DELETE FROM passports");
    }

    private Passport passport(String prefix, int from, int to, int months) {
        Passport p = new Passport();
        p.setSerialPrefix(prefix);
        p.setFromSerialNumber(from);
        p.setToSerialNumber(to);
        p.setWarrantyMonths(months);
        return passportRepository.saveAndFlush(p);
    }

    private User user(String email, String phone) {
        User u = new User();
        u.setEmail(email);
        u.setPhone(phone);
        u.setPassword("pass");
        u.setFullName("User");
        u.setAddress("Addr");
        u.setRole(UserRole.USER);
        return userRepository.saveAndFlush(u);
    }

    @Test
    void registerDevice_success() {
        passport("AAA", 1, 9999, 12);
        User u = user("user@abv.bg", "0888111111");

        Device d = deviceService.registerDevice("AAA123", LocalDate.of(2024, 1, 1), u);

        assertNotNull(d);
        assertEquals("AAA123", d.getSerialNumber());
        assertNotNull(d.getPassport());
        assertNotNull(d.getUser());
    }

    @Test
    void registerDevice_invalidSerial_throws() {
        passport("AAA", 1, 9999, 12);
        User u = user("bad@abv.bg", "0888222222");

        assertThrows(CustomException.class, () ->
                deviceService.registerDevice("INVALID", LocalDate.now(), u)
        );
    }

    @Test
    void isDeviceExists_missing_throws() {
        assertThrows(CustomException.class, () -> deviceService.isDeviceExists("MISSING"));
    }

    @Test
    void alreadyExist_whenDeviceExists_throws() {
        Passport p = passport("BBB", 1, 9999, 12);

        Device d = new Device();
        d.setSerialNumber("BBB111");
        d.setPassport(p);
        d.setPurchaseDate(LocalDate.of(2024, 1, 1));
        d.setWarrantyExpirationDate(LocalDate.of(2025, 1, 1));
        deviceRepository.saveAndFlush(d);

        assertThrows(CustomException.class, () -> deviceService.alreadyExist("BBB111"));
    }

    @Test
    void addAnonymousDevice_success() {
        passport("DDD", 1, 9999, 24);

        DeviceCreateVO vo = new DeviceCreateVO("DDD555", LocalDate.of(2024, 6, 1));
        Device d = deviceService.addAnonymousDevice(vo);

        assertNotNull(d);
        assertEquals("DDD555", d.getSerialNumber());
        assertNull(d.getUser());
        assertNotNull(d.getPassport());
    }

    @Test
    void updateDevice_userAddsExtraWarranty() {
        Passport p = passport("CCC", 1, 9999, 12);
        User u = user("u@u.bg", "0888333333");

        Device device = new Device();
        device.setSerialNumber("CCC999");
        device.setPassport(p);
        device.setUser(u);
        device.setPurchaseDate(LocalDate.of(2024, 1, 1));
        device.setWarrantyExpirationDate(LocalDate.of(2025, 1, 1));
        deviceRepository.saveAndFlush(device);

        DeviceUpdateVO vo = new DeviceUpdateVO(LocalDate.of(2024, 1, 1), "comment");
        Device updated = deviceService.updateDevice("CCC999", vo);

        assertNotNull(updated);
        assertEquals(LocalDate.of(2026, 1, 1), updated.getWarrantyExpirationDate());
    }

    @Test
    void updateDevice_withoutUser_noExtraWarranty() {
        Passport p = passport("NNN", 1, 9999, 12);

        Device device = new Device();
        device.setSerialNumber("NNN777");
        device.setPassport(p);
        device.setUser(null);
        device.setPurchaseDate(LocalDate.of(2024, 1, 1));
        device.setWarrantyExpirationDate(LocalDate.of(2025, 1, 1));
        deviceRepository.saveAndFlush(device);

        DeviceUpdateVO vo = new DeviceUpdateVO(LocalDate.of(2024, 1, 1), "c");
        Device updated = deviceService.updateDevice("NNN777", vo);

        assertNotNull(updated);
        assertEquals(LocalDate.of(2025, 1, 1), updated.getWarrantyExpirationDate());
    }

    @Test
    void getDevices_withoutSearch_returnsAll() {
        Passport p = passport("EEE", 1, 9999, 12);

        Device d1 = new Device();
        d1.setSerialNumber("EEE101");
        d1.setPassport(p);
        d1.setPurchaseDate(LocalDate.of(2024, 1, 1));
        d1.setWarrantyExpirationDate(LocalDate.of(2025, 1, 1));

        Device d2 = new Device();
        d2.setSerialNumber("EEE202");
        d2.setPassport(p);
        d2.setPurchaseDate(LocalDate.of(2024, 1, 1));
        d2.setWarrantyExpirationDate(LocalDate.of(2025, 1, 1));

        deviceRepository.saveAndFlush(d1);
        deviceRepository.saveAndFlush(d2);

        CustomPage<Device> page = deviceService.getDevices(null, 1, 10);

        assertNotNull(page);
        assertNotNull(page.getItems());
        assertEquals(2, page.getItems().size());
    }

    @Test
    void getDevices_withSearch_filters() {
        Passport p1 = passport("EEE", 1, 9999, 12);
        Passport p2 = passport("FFF", 1, 9999, 12);

        Device d1 = new Device();
        d1.setSerialNumber("EEE101");
        d1.setPassport(p1);
        d1.setPurchaseDate(LocalDate.of(2024, 1, 1));
        d1.setWarrantyExpirationDate(LocalDate.of(2025, 1, 1));

        Device d2 = new Device();
        d2.setSerialNumber("FFF202");
        d2.setPassport(p2);
        d2.setPurchaseDate(LocalDate.of(2024, 1, 1));
        d2.setWarrantyExpirationDate(LocalDate.of(2025, 1, 1));

        deviceRepository.saveAndFlush(d1);
        deviceRepository.saveAndFlush(d2);

        CustomPage<Device> page = deviceService.getDevices("eee", 1, 10);

        assertNotNull(page);
        assertEquals(1, page.getItems().size());
        assertEquals("EEE101", page.getItems().get(0).getSerialNumber());
    }

    @Test
    void deleteDevice_deletes() {
        Passport p = passport("ABC", 1000, 9999, 12);

        Device d = new Device();
        d.setSerialNumber("ABC8888");
        d.setPassport(p);
        d.setPurchaseDate(LocalDate.of(2024, 1, 1));
        d.setWarrantyExpirationDate(LocalDate.of(2025, 1, 1));
        deviceRepository.saveAndFlush(d);

        assertDoesNotThrow(() -> deviceService.deleteDevice("ABC8888"));
        assertTrue(deviceRepository.findById("ABC8888").isEmpty());
    }
}
