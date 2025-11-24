package bg.tuvarna.devicebackend.service;

import bg.tuvarna.devicebackend.controllers.exceptions.CustomException;
import bg.tuvarna.devicebackend.models.dtos.PassportCreateVO;
import bg.tuvarna.devicebackend.models.dtos.PassportUpdateVO;
import bg.tuvarna.devicebackend.models.entities.Passport;
import bg.tuvarna.devicebackend.repositories.PassportRepository;
import bg.tuvarna.devicebackend.services.PassportService;
import bg.tuvarna.devicebackend.utils.CustomPage;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PassportServiceTests {

    @Test
    void create_whenOverlap_throws() {
        PassportRepository repo = mock(PassportRepository.class);
        PassportService service = new PassportService(repo);

        PassportCreateVO vo = new PassportCreateVO(
                "Name",
                "Model",
                "PX",
                12,
                1,
                100
        );

        when(repo.findByFromSerialNumberBetween(eq("PX"), eq(1), eq(100)))
                .thenReturn(List.of(new Passport()));

        assertThrows(CustomException.class, () -> service.create(vo));
        verify(repo, never()).save(any());
    }

    @Test
    void update_whenNotFound_throws() {
        PassportRepository repo = mock(PassportRepository.class);
        PassportService service = new PassportService(repo);

        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> service.update(1L, new PassportUpdateVO(null, null, null, null, null, null)));
    }

    @Test
    void update_whenOverlapWithOtherId_throws() {
        PassportRepository repo = mock(PassportRepository.class);
        PassportService service = new PassportService(repo);

        Passport existing = new Passport();
        existing.setId(10L);
        existing.setSerialPrefix("PX");
        existing.setFromSerialNumber(1);
        existing.setToSerialNumber(100);
        existing.setWarrantyMonths(12);

        Passport other = new Passport();
        other.setId(11L);

        when(repo.findById(10L)).thenReturn(Optional.of(existing));

        when(repo.findByFromSerialNumberBetween(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(other));

        PassportUpdateVO vo = new PassportUpdateVO(
                null,   // name
                null,   // model
                "PX",   // serialPrefix
                12,     // warrantyMonths
                1,      // fromSerialNumber
                100     // toSerialNumber
        );

        assertThrows(CustomException.class, () ->
                service.update(10L, vo)
        );
    }


    @Test
    void findPassportBySerialId_invalidNumeric_throws() {
        PassportRepository repo = mock(PassportRepository.class);
        PassportService service = new PassportService(repo);

        Passport p = new Passport();
        p.setId(1L);
        p.setSerialPrefix("AAA");
        p.setFromSerialNumber(1);
        p.setToSerialNumber(9999);

        when(repo.findByFromSerial("AAAxx")).thenReturn(List.of(p));

        assertThrows(CustomException.class, () -> service.findPassportBySerialId("AAAxx"));
    }

    @Test
    void findPassportBySerialId_noMatch_throws() {
        PassportRepository repo = mock(PassportRepository.class);
        PassportService service = new PassportService(repo);

        Passport p = new Passport();
        p.setId(1L);
        p.setSerialPrefix("AAA");
        p.setFromSerialNumber(1);
        p.setToSerialNumber(10);

        when(repo.findByFromSerial("AAA999")).thenReturn(List.of(p));

        assertThrows(CustomException.class, () -> service.findPassportBySerialId("AAA999"));
    }

    @Test
    void delete_whenRepoThrows_wrapsToCustomException() {
        PassportRepository repo = mock(PassportRepository.class);
        PassportService service = new PassportService(repo);

        doThrow(new RuntimeException("boom")).when(repo).deleteById(5L);

        CustomException ex = assertThrows(CustomException.class, () -> service.delete(5L));
        assertTrue(ex.getMessage().toLowerCase().contains("can't delete passport"));
    }

    @Test
    void getPassports_mapsToCustomPage() {
        PassportRepository repo = mock(PassportRepository.class);
        PassportService service = new PassportService(repo);

        Passport p1 = new Passport(); p1.setId(1L);
        Passport p2 = new Passport(); p2.setId(2L);

        when(repo.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(p1, p2), PageRequest.of(0, 10), 2));

        CustomPage<Passport> page = service.getPassports(1, 10);

        assertNotNull(page);
        assertEquals(1, page.getCurrentPage());
        assertEquals(10, page.getSize());
        assertEquals(2, page.getTotalItems());
        assertEquals(1, page.getTotalPages());
        assertEquals(2, page.getItems().size());
    }

    @Test
    void getPassportsBySerialPrefix_delegates() {
        PassportRepository repo = mock(PassportRepository.class);
        PassportService service = new PassportService(repo);

        when(repo.findByFromSerial("PX123")).thenReturn(List.of(new Passport()));

        assertEquals(1, service.getPassportsBySerialPrefix("PX123").size());
        verify(repo, times(1)).findByFromSerial("PX123");
    }

    @Test
    void update_whenNoOverlap_updatesSuccessfully() {
        PassportRepository repo = mock(PassportRepository.class);
        PassportService service = new PassportService(repo);

        Passport existing = new Passport();
        existing.setId(1L);

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.findByFromSerialNumberBetween(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(existing));
        when(repo.save(any())).thenReturn(existing);

        PassportUpdateVO vo = new PassportUpdateVO(
                null,
                null,
                "PX",
                12,
                1,
                100
        );

        Passport result = service.update(1L, vo);

        assertNotNull(result);
    }

    @Test
    void create_whenRepoThrows_wrapsException() {
        PassportRepository repo = mock(PassportRepository.class);
        PassportService service = new PassportService(repo);

        when(repo.save(any()))
                .thenThrow(new RuntimeException("DB"));

        PassportCreateVO vo = new PassportCreateVO(
                "Name",
                "Model",
                "PX",
                12,
                1,
                10
        );

        assertThrows(RuntimeException.class, () -> service.create(vo));
    }

    @Test
    void passportCreateVO_instantiates() {
        PassportCreateVO vo = new PassportCreateVO(
                "Name","Model","PX",12,1,10
        );
        assertNotNull(vo);
    }

    @Test
    void passport_settersAndGetters() {
        Passport p = new Passport();
        p.setName("X");
        assertEquals("X", p.getName());
    }

}
