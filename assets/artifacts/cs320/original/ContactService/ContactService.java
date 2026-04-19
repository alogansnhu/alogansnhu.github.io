import java.util.HashMap;
import java.util.Map;

/**
 * ContactService
 * 
 * Manages Contact objects in memory.
 * 
 * - Add contacts with a unique ID (reject duplicates)
 * - Delete contacts by contact ID
 * - Update fields by contact ID
 */
public class ContactService {

    private final Map<String, Contact> contacts = new HashMap<>();

    /**
     * Adds a contact if its ID is unique.
     * @param contact contact to add (must be non-null).
     * @return true if added
     * @throws IllegalArgumentException if contact is null or ID already exists
     */
    public boolean addContact(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact cannot be null");
        }
        String id = contact.getContactId();
        if (id == null) {
            throw new IllegalArgumentException("Contact ID cannot be null");
        }
        if (contacts.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate ID");
        }
        contacts.put(id, contact);
        return true;
    }

    /**
     * Deletes a contact by ID.
     * @param contactId required non-null ID
     * @throws IllegalArgumentException if ID is null or not found
     */
    public void deleteContact(String contactId) {
        Contact c = getExisting(contactId);
        contacts.remove(c.getContactId());
    }

    /**
     * Updates the first name of a contact.
     */
    public void updateFirstName(String contactId, String newFirstName) {
        Contact c = getExisting(contactId);
        c.setFirstName(newFirstName);
    }

    /**
     * Updates the last name of a contact.
     */
    public void updateLastName(String contactId, String newLastName) {
        Contact c = getExisting(contactId);
        c.setLastName(newLastName);
    }

    /**
     * Updates the phone (number) of a contact.
     */
    public void updatePhone(String contactId, String newPhone) {
        Contact c = getExisting(contactId);
        c.setPhone(newPhone);
    }

    /**
     * Updates the address of a contact.
     */
    public void updateAddress(String contactId, String newAddress) {
        Contact c = getExisting(contactId);
        c.setAddress(newAddress);
    }

    // Internal helper
    private Contact getExisting(String contactId) {
        if (contactId == null) {
            throw new IllegalArgumentException("Contact ID cannot be null");
        }
        Contact c = contacts.get(contactId);
        if (c == null) {
            throw new IllegalArgumentException("Contact not found: " + contactId);
        }
        return c;
    }
}