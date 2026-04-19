import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ContactServiceTest {

    @Test
    void testAddContactAndRejectDuplicateId() {
        ContactService svc = new ContactService();
        Contact a = new Contact("u1", "Amy", "Jones", "1234567890", "10 Oak St");
        assertTrue(svc.addContact(a));

        // duplicate
        Contact dup = new Contact("u1", "Other", "Name", "1112223333", "Some Addr");
        assertThrows(IllegalArgumentException.class, () -> svc.addContact(dup));
    }

    @Test
    void testAddNullContactThrows() {
        ContactService svc = new ContactService();
        assertThrows(IllegalArgumentException.class, () -> svc.addContact(null));
    }

    @Test
    void testDeleteContact() {
        ContactService svc = new ContactService();
        Contact a = new Contact("u1", "Amy", "Jones", "1234567890", "10 Oak St");
        svc.addContact(a);

        // delete ok
        assertDoesNotThrow(() -> svc.deleteContact("u1"));

        // cannot delete again / not found
        assertThrows(IllegalArgumentException.class, () -> svc.deleteContact("u1"));
    }

    @Test
    void testDeleteWithNullIdThrows() {
        ContactService svc = new ContactService();
        assertThrows(IllegalArgumentException.class, () -> svc.deleteContact(null));
    }

    @Test
    void testUpdateFirstName() {
        ContactService svc = new ContactService();
        Contact a = new Contact("u1", "Amy", "Jones", "1234567890", "10 Oak St");
        svc.addContact(a);

        svc.updateFirstName("u1", "Ann");
        assertEquals("Ann", a.getFirstName());

        // invalid update throws
        assertThrows(IllegalArgumentException.class, () -> svc.updateFirstName("u1", null));
    }

    @Test
    void testUpdateLastName() {
        ContactService svc = new ContactService();
        Contact a = new Contact("u1", "Amy", "Jones", "1234567890", "10 Oak St");
        svc.addContact(a);

        svc.updateLastName("u1", "Ng");
        assertEquals("Ng", a.getLastName());

        // invalid update throws
        assertThrows(IllegalArgumentException.class, () -> svc.updateLastName("u1", "WayTooLongName"));
    }

    @Test
    void testUpdatePhone() {
        ContactService svc = new ContactService();
        Contact a = new Contact("u1", "Amy", "Jones", "1234567890", "10 Oak St");
        svc.addContact(a);

        svc.updatePhone("u1", "0987654321");
        assertEquals("0987654321", a.getPhone());

        // invalid
        assertThrows(IllegalArgumentException.class, () -> svc.updatePhone("u1", "123"));
    }

    @Test
    void testUpdateAddress() {
        ContactService svc = new ContactService();
        Contact a = new Contact("u1", "Amy", "Jones", "1234567890", "10 Oak St");
        svc.addContact(a);

        svc.updateAddress("u1", "22 Pine Road");
        assertEquals("22 Pine Road", a.getAddress());

        // invalid: 31-char string without String.repeat
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 31; i++) sb.append('x');
        assertThrows(IllegalArgumentException.class, () -> svc.updateAddress("u1", sb.toString()));
    }

    @Test
    void testUpdateNonexistentIdThrows() {
        ContactService svc = new ContactService();
        assertThrows(IllegalArgumentException.class, () -> svc.updateFirstName("nope", "X"));
        assertThrows(IllegalArgumentException.class, () -> svc.updateLastName("nope", "X"));
        assertThrows(IllegalArgumentException.class, () -> svc.updatePhone("nope", "0123456789"));
        assertThrows(IllegalArgumentException.class, () -> svc.updateAddress("nope", "123 St"));
    }

    @Test
    void testUpdateWithNullIdThrows() {
        ContactService svc = new ContactService();
        assertThrows(IllegalArgumentException.class, () -> svc.updateFirstName(null, "X"));
        assertThrows(IllegalArgumentException.class, () -> svc.updateLastName(null, "X"));
        assertThrows(IllegalArgumentException.class, () -> svc.updatePhone(null, "0123456789"));
        assertThrows(IllegalArgumentException.class, () -> svc.updateAddress(null, "123 St"));
    }
}