package bg.tuvarna.devicebackend.repository;

import bg.tuvarna.devicebackend.models.entities.Device;
import bg.tuvarna.devicebackend.models.entities.Passport;
import bg.tuvarna.devicebackend.repositories.DeviceRepository;
import bg.tuvarna.devicebackend.repositories.PassportRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DeviceRepositoryTests {

    @Autowired
    DeviceRepository deviceRepository;
    @Autowired
    PassportRepository passportRepository;

    @Test
    void deleteBySerialNumber_works() {
        Passport p = new Passport();
        p.setSerialPrefix("DEL");
        p.setFromSerialNumber(1);
        p.setToSerialNumber(10);
        p.setWarrantyMonths(12);
        passportRepository.save(p);

        Device d = new Device();
        d.setSerialNumber("DEL1");
        d.setPassport(p);
        d.setPurchaseDate(LocalDate.now());
        d.setWarrantyExpirationDate(LocalDate.now().plusMonths(12));
        deviceRepository.save(d);

        deviceRepository.deleteBySerialNumber("DEL1");

        assertFalse(deviceRepository.existsById("DEL1"));
    }
}
