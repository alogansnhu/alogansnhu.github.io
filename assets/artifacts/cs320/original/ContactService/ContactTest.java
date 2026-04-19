import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ContactTest {

    @Test
    void testValidContactCreation() {
        Contact c = new Contact("abc123", "Alice", "Smith", "1234567890", "123 Main Street");
        assertEquals("abc123", c.getContactId());
        assertEquals("Alice", c.getFirstName());
        assertEquals("Smith", c.getLastName());
        assertEquals("1234567890", c.getPhone());
        assertEquals("123 Main Street", c.getAddress());
    }

    @Test
    void testIdConstraints() {
        assertThrows(IllegalArgumentException.class,
                () -> new Contact(null, "A", "B", "0123456789", "Addr"));

        assertThrows(IllegalArgumentException.class,
                () -> new Contact("toolongidhere", "A", "B", "0123456789", "Addr"));
    }

    @Test
    void testFirstNameConstraints() {
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("id", null, "B", "0123456789", "Addr"));
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("id", "VeryLongName", "B", "0123456789", "Addr"));
    }

    @Test
    void testLastNameConstraints() {
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("id", "A", null, "0123456789", "Addr"));
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("id", "A", "VeryLongName", "0123456789", "Addr"));
    }

    @Test
    void testPhoneConstraints() {
        // null
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("id", "A", "B", null, "Addr"));
        // not 10 digits
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("id", "A", "B", "123456789", "Addr"));
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("id", "A", "B", "12345678901", "Addr"));
        // has letters
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("id", "A", "B", "12345abcde", "Addr"));

        // exactly 10 digits ok
        Contact c = new Contact("id", "A", "B", "0123456789", "Addr");
        assertEquals("0123456789", c.getPhone());
    }

    @Test
    void testAddressConstraints() {
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("id", "A", "B", "0123456789", null));
        String thirtyOne = "1234567890123456789012345678901"; // 31 chars
        assertEquals(31, thirtyOne.length());
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("id", "A", "B", "0123456789", thirtyOne));
    }

    @Test
    void testSettersWorkWithValidation() {
        Contact c = new Contact("abc", "A", "B", "0123456789", "Addr");
        c.setFirstName("Tom");
        c.setLastName("Lee");
        c.setPhone("1112223333");
        c.setAddress("42 Park Ave");
        assertAll(
                () -> assertEquals("Tom", c.getFirstName()),
                () -> assertEquals("Lee", c.getLastName()),
                () -> assertEquals("1112223333", c.getPhone()),
                () -> assertEquals("42 Park Ave", c.getAddress())
        );

        // invalid updates throw
        assertThrows(IllegalArgumentException.class, () -> c.setFirstName(null));
        assertThrows(IllegalArgumentException.class, () -> c.setLastName("WayTooLongName"));
        assertThrows(IllegalArgumentException.class, () -> c.setPhone("123456789")); // 9 digits

        // build a 31-char string without String.repeat
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 31; i++) sb.append('x');
        assertThrows(IllegalArgumentException.class, () -> c.setAddress(sb.toString()));
    }
}