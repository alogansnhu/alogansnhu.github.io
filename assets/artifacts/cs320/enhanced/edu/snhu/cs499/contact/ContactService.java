package edu.snhu.cs499.contact;

import edu.snhu.cs499.audit.AuditAction;
import edu.snhu.cs499.audit.AuditLogger;
import edu.snhu.cs499.audit.InMemoryAuditLogger;
import edu.snhu.cs499.common.AbstractCrudService;
import edu.snhu.cs499.common.InMemoryRepository;
import edu.snhu.cs499.common.Repository;

public class ContactService extends AbstractCrudService<Contact> {
  public ContactService() {
    this(new InMemoryRepository<>(), new InMemoryAuditLogger());
  }

  public ContactService(Repository<Contact, String> repository, AuditLogger auditLogger) {
    super("Contact", repository, auditLogger);
  }

  public Contact createContact(String contactId, String firstName, String lastName, String phone, String address) {
    return create(new Contact(contactId, firstName, lastName, phone, address));
  }

  public Contact updateContact(String contactId, String firstName, String lastName, String phone, String address) {
    Contact existing = requireById(contactId);
    existing.setFirstName(firstName);
    existing.setLastName(lastName);
    existing.setPhone(phone);
    existing.setAddress(address);
    repository.save(existing);
    record(AuditAction.UPDATE, existing.getId(), "Updated contact.");
    return existing;
  }
}
