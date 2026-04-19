/**
 * Contact
 * 
 * Represents an immutable identifier contact record.
 * 
 * - contactId: unique, <= 10 chars, non null, not updatable
 * - firstName: <= 10 chars, non null
 * - lastName: <= 10 chars, non null
 * - phone: exactly 10 digits, non null
 * - address: <= 30 chars, non null
 */
public class Contact {

    private final String contactId;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;

    public Contact(String contactId, String firstName, String lastName, String phone, String address) {
        this.contactId = validateId(contactId);
        this.firstName = validateName(firstName, "First name");
        this.lastName  = validateName(lastName,  "Last name");
        this.phone     = validatePhone(phone);
        this.address   = validateAddress(address);
    }

    // Getters
    public String getContactId() { return contactId; }
    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName; }
    public String getPhone()     { return phone; }
    public String getAddress()   { return address; }

    // Updatable fields
    public void setFirstName(String firstName) {
        this.firstName = validateName(firstName, "First name");
    }

    public void setLastName(String lastName) {
        this.lastName = validateName(lastName, "Last name");
    }

    public void setPhone(String phone) {
        this.phone = validatePhone(phone);
    }

    public void setAddress(String address) {
        this.address = validateAddress(address);
    }

    // Validation helpers
    private static String validateId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Contact ID cannot be null");
        }
        if (id.length() > 10) {
            throw new IllegalArgumentException("Contact ID must be at most 10 characters");
        }
        return id;
    }

    private static String validateName(String name, String label) {
        if (name == null) {
            throw new IllegalArgumentException(label + " cannot be null");
        }
        if (name.length() > 10) {
            throw new IllegalArgumentException(label + " must be at most 10 characters");
        }
        return name;
    }

    private static String validatePhone(String phone) {
        if (phone == null) {
            throw new IllegalArgumentException("Phone cannot be null");
        }
        // Exactly 10 digits
        if (!phone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone must be exactly 10 digits");
        }
        return phone;
    }

    private static String validateAddress(String address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        if (address.length() > 30) {
            throw new IllegalArgumentException("Address must be at most 30 characters");
        }
        return address;
    }
}