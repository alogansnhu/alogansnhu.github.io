package edu.snhu.cs499.contact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import edu.snhu.cs499.exceptions.DuplicateEntityException;
import edu.snhu.cs499.exceptions.EntityNotFoundException;

public class ContactServiceTest {

  @Test
  void createContact_storesContactsAndPreventsDuplicateIds() {
    ContactService service = new ContactService();

    service.createContact("C1", "Drew", "Logan", "1234567890", "123 Main Street");
    service.createContact("C2", "Amy", "Stone", "0987654321", "42 Park Ave");

    assertEquals(2, service.findAll().size());
    assertEquals(true, service.getById("C1").isPresent());
    assertThrows(DuplicateEntityException.class,
        () -> service.createContact("C1", "Other", "Name", "1112223333", "Somewhere"));
  }

  @Test
  void updateContact_updatesExistingContact() {
    ContactService service = new ContactService();
    service.createContact("C1", "Drew", "Logan", "1234567890", "123 Main Street");

    Contact updated = service.updateContact("C1", "Andrew", "Logan", "2223334444", "55 Elm Road");

    assertEquals("Andrew", updated.getFirstName());
    assertEquals("2223334444", updated.getPhone());
  }

  @Test
  void deleteById_removesExistingContact() {
    ContactService service = new ContactService();
    service.createContact("C1", "Drew", "Logan", "1234567890", "123 Main Street");

    assertEquals(true, service.deleteById("C1"));
    assertEquals(false, service.getById("C1").isPresent());
  }

  @Test
  void operations_throwForMissingContacts() {
    ContactService service = new ContactService();
    assertThrows(EntityNotFoundException.class,
        () -> service.updateContact("MISSING", "A", "B", "1234567890", "Addr"));
    assertThrows(EntityNotFoundException.class, () -> service.deleteById("MISSING"));
  }
}
