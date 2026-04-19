package edu.snhu.cs499.contact;

import edu.snhu.cs499.common.Identifiable;
import edu.snhu.cs499.common.ValidationUtils;

public class Contact implements Identifiable<String> {
  private final String contactId;
  private String firstName;
  private String lastName;
  private String phone;
  private String address;

  public Contact(String contactId, String firstName, String lastName, String phone, String address) {
    this.contactId = ValidationUtils.requireMaxLength(
        ValidationUtils.requireNonBlank(contactId, "Contact ID"),
        10,
        "Contact ID");
    this.firstName = ValidationUtils.requireMaxLength(
        ValidationUtils.requireNonBlank(firstName, "First name"),
        10,
        "First name");
    this.lastName = ValidationUtils.requireMaxLength(
        ValidationUtils.requireNonBlank(lastName, "Last name"),
        10,
        "Last name");
    this.phone = ValidationUtils.requirePattern(
        ValidationUtils.requireNonBlank(phone, "Phone"),
        "\\d{10}",
        "Phone",
        "exactly 10 digits");
    this.address = ValidationUtils.requireMaxLength(
        ValidationUtils.requireNonBlank(address, "Address"),
        30,
        "Address");
  }

  @Override
  public String getId() {
    return contactId;
  }

  public String getContactId() {
    return contactId;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getPhone() {
    return phone;
  }

  public String getAddress() {
    return address;
  }

  public void setFirstName(String firstName) {
    this.firstName = ValidationUtils.requireMaxLength(
        ValidationUtils.requireNonBlank(firstName, "First name"),
        10,
        "First name");
  }

  public void setLastName(String lastName) {
    this.lastName = ValidationUtils.requireMaxLength(
        ValidationUtils.requireNonBlank(lastName, "Last name"),
        10,
        "Last name");
  }

  public void setPhone(String phone) {
    this.phone = ValidationUtils.requirePattern(
        ValidationUtils.requireNonBlank(phone, "Phone"),
        "\\d{10}",
        "Phone",
        "exactly 10 digits");
  }

  public void setAddress(String address) {
    this.address = ValidationUtils.requireMaxLength(
        ValidationUtils.requireNonBlank(address, "Address"),
        30,
        "Address");
  }
}
