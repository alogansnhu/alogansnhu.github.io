package edu.snhu.cs499.common;

import java.util.List;
import java.util.Optional;

import edu.snhu.cs499.audit.AuditAction;
import edu.snhu.cs499.audit.AuditEvent;
import edu.snhu.cs499.audit.AuditLogger;
import edu.snhu.cs499.exceptions.DuplicateEntityException;
import edu.snhu.cs499.exceptions.EntityNotFoundException;

public abstract class AbstractCrudService<T extends Identifiable<String>> implements CrudService<T, String> {
  protected final Repository<T, String> repository;
  protected final AuditLogger auditLogger;
  private final String entityType;

  protected AbstractCrudService(String entityType, Repository<T, String> repository, AuditLogger auditLogger) {
    this.entityType = ValidationUtils.requireNonBlank(entityType, "Entity type");
    this.repository = ValidationUtils.requireNotNull(repository, "Repository");
    this.auditLogger = ValidationUtils.requireNotNull(auditLogger, "Audit logger");
  }

  @Override
  public T create(T entity) {
    ValidationUtils.requireNotNull(entity, entityType);
    if (repository.existsById(entity.getId())) {
      throw new DuplicateEntityException(entityType, entity.getId());
    }
    beforeCreate(entity);
    repository.save(entity);
    afterCreate(entity);
    record(AuditAction.CREATE, entity.getId(), "Created " + entityType.toLowerCase() + ".");
    return entity;
  }

  @Override
  public Optional<T> getById(String id) {
    ValidationUtils.requireNonBlank(id, entityType + " ID");
    return repository.findById(id);
  }

  public T requireById(String id) {
    return getById(id).orElseThrow(() -> new EntityNotFoundException(entityType, id));
  }

  @Override
  public List<T> findAll() {
    return repository.findAll();
  }

  @Override
  public boolean deleteById(String id) {
    T existing = requireById(id);
    beforeDelete(existing);
    boolean deleted = repository.deleteById(existing.getId());
    if (deleted) {
      afterDelete(existing);
      record(AuditAction.DELETE, existing.getId(), "Deleted " + entityType.toLowerCase() + ".");
    }
    return deleted;
  }

  @Override
  public boolean existsById(String id) {
    ValidationUtils.requireNonBlank(id, entityType + " ID");
    return repository.existsById(id);
  }

  protected void record(AuditAction action, String entityId, String message) {
    auditLogger.record(AuditEvent.now(action, entityType, entityId, message));
  }

  protected void beforeCreate(T entity) {
  }

  protected void afterCreate(T entity) {
  }

  protected void beforeDelete(T entity) {
  }

  protected void afterDelete(T entity) {
  }
}
