package edu.snhu.cs499.common;

import java.util.List;
import java.util.Optional;

public interface CrudService<T extends Identifiable<ID>, ID> {
  T create(T entity);

  Optional<T> getById(ID id);

  List<T> findAll();

  boolean deleteById(ID id);

  boolean existsById(ID id);
}
