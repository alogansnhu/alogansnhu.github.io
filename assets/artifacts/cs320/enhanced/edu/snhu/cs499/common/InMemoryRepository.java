package edu.snhu.cs499.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryRepository<T extends Identifiable<ID>, ID> implements Repository<T, ID> {
  private final Map<ID, T> storage = new HashMap<>();

  @Override
  public T save(T entity) {
    storage.put(entity.getId(), entity);
    return entity;
  }

  @Override
  public Optional<T> findById(ID id) {
    return Optional.ofNullable(storage.get(id));
  }

  @Override
  public List<T> findAll() {
    return new ArrayList<>(storage.values());
  }

  @Override
  public boolean deleteById(ID id) {
    return storage.remove(id) != null;
  }

  @Override
  public boolean existsById(ID id) {
    return storage.containsKey(id);
  }
}
