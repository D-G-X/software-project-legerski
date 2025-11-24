import de.hft.validatormock.dto.ValidationRequest;
import de.hft.validatormock.dto.ValidationResponse;
import de.hft.validatormock.service.MockValidatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationServiceTest {


    private MockValidatorService validationService;

    @BeforeEach
    void setUp() {
        validationService = new MockValidatorService();
    }

    @Test
    void testValidInput() {
        ValidationRequest request = new ValidationRequest(
                "Juan Perez",
                "AB1234",
                "Calle Mallorca 15, Palma"
        );

        ValidationResponse response = validationService.verifyDocuments(request);

        assertEquals("Valid", response.getStatus());
        assertNotNull(response.getTimestamp());
        assertEquals("Checked by mock validator.", response.getMessage());
    }

    @Test
    void testInvalidAddress() {
        ValidationRequest request = new ValidationRequest(
                "Juan Perez",
                "AB1234",
                "Calle Valencia 23, Madrid"
        );

        ValidationResponse response = validationService.verifyDocuments(request);

        assertEquals("Unvalid", response.getStatus());
        assertNotNull(response.getTimestamp());
        assertEquals("Address seems incorrect.", response.getMessage());
    }

    @Test
    void testNullAddress() {
        ValidationRequest request = new ValidationRequest(
                "Juan Perez",
                "AB1234",
                null
        );

        ValidationResponse response = validationService.verifyDocuments(request);

        assertEquals("Unvalid", response.getStatus());
        assertEquals("Address seems incorrect.", response.getMessage());
    }
}
