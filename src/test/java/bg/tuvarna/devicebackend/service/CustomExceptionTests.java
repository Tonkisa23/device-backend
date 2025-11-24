package bg.tuvarna.devicebackend.service;

import bg.tuvarna.devicebackend.controllers.exceptions.CustomException;
import bg.tuvarna.devicebackend.controllers.exceptions.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomExceptionTests {

    @Test
    void constructor_withMessageAndCode_setsFields() {
        CustomException ex = new CustomException("boom", ErrorCode.Failed);

        assertEquals("boom", ex.getMessage());
        assertEquals(ErrorCode.Failed, ex.getErrorCode());
    }
}
