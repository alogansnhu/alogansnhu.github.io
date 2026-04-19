package edu.snhu.cs499.contact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

import edu.snhu.cs499.exceptions.InvalidRequestException;

public class ContactTest {

  @Test
  void constructor_acceptsValidValues() {
    Contact contact = new Contact("C1", "Drew", "Logan", "1234567890", "123 Main Street");

    assertEquals("C1", contact.getContactId());
    assertEquals("Drew", contact.getFirstName());
    assertEquals("Logan", contact.getLastName());
    assertEquals("1234567890", contact.getPhone());
    assertEquals("123 Main Street", contact.getAddress());
  }

  @Test
  void constructor_rejectsInvalidValues() {
    assertThrows(InvalidRequestException.class, () -> new Contact(null, "Drew", "Logan", "1234567890", "Addr"));
    assertThrows(InvalidRequestException.class,
        () -> new Contact("ABCDEFGHIJK", "Drew", "Logan", "1234567890", "Addr"));
    assertThrows(InvalidRequestException.class, () -> new Contact("C1", "", "Logan", "1234567890", "Addr"));
    assertThrows(InvalidRequestException.class, () -> new Contact("C1", "Drew", "", "1234567890", "Addr"));
    assertThrows(InvalidRequestException.class, () -> new Contact("C1", "Drew", "Logan", "123", "Addr"));
    assertThrows(InvalidRequestException.class, () -> new Contact("C1", "Drew", "Logan", "1234567890", ""));
  }

  @Test
  void setters_updateFieldsWithValidation() {
    Contact contact = new Contact("C1", "Drew", "Logan", "1234567890", "123 Main Street");

    contact.setFirstName("Andy");
    contact.setLastName("Stone");
    contact.setPhone("0987654321");
    contact.setAddress("42 Park Ave");

    assertEquals("Andy", contact.getFirstName());
    assertEquals("Stone", contact.getLastName());
    assertEquals("0987654321", contact.getPhone());
    assertEquals("42 Park Ave", contact.getAddress());

    assertThrows(InvalidRequestException.class, () -> contact.setFirstName(null));
    assertThrows(InvalidRequestException.class, () -> contact.setPhone("bad"));
  }

  @Test
  void contactId_isImmutable() throws Exception {
    Field idField = Contact.class.getDeclaredField("contactId");
    assertEquals(true, Modifier.isFinal(idField.getModifiers()));
  }
}
