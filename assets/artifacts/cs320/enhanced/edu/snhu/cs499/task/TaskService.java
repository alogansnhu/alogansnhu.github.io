package edu.snhu.cs499.task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.snhu.cs499.audit.AuditAction;
import edu.snhu.cs499.audit.AuditLogger;
import edu.snhu.cs499.audit.InMemoryAuditLogger;
import edu.snhu.cs499.common.AbstractCrudService;
import edu.snhu.cs499.common.InMemoryRepository;
import edu.snhu.cs499.common.Repository;
import edu.snhu.cs499.common.ValidationUtils;

public class TaskService extends AbstractCrudService<Task> {
  private static final Comparator<Task> TASK_ORDER = Comparator.comparing(Task::getPriority).reversed()
      .thenComparing(Task::getDueDate)
      .thenComparing(Task::getTaskId);

  private final NavigableSet<Task> tasksByPriority = new TreeSet<>(TASK_ORDER);

  public TaskService() {
    this(new InMemoryRepository<>(), new InMemoryAuditLogger());
  }

  public TaskService(Repository<Task, String> repository, AuditLogger auditLogger) {
    super("Task", repository, auditLogger);
  }

  public Task createTask(String taskId, String name, String description, Task.Priority priority,
      LocalDateTime dueDate) {
    return create(new Task(taskId, name, description, priority, dueDate));
  }

  public Task updateTask(String taskId, String name, String description, Task.Priority priority,
      LocalDateTime dueDate) {
    Task existing = requireById(taskId);
    tasksByPriority.remove(existing);
    existing.setName(name);
    existing.setDescription(description);
    existing.setPriority(priority);
    existing.setDueDate(dueDate);
    repository.save(existing);
    tasksByPriority.add(existing);
    record(AuditAction.UPDATE, existing.getId(), "Updated task.");
    return existing;
  }

  public List<Task> getTasksByPriority() {
    return Collections.unmodifiableList(new ArrayList<>(tasksByPriority));
  }

  public Optional<Task> peekMostUrgentTask() {
    return tasksByPriority.isEmpty() ? Optional.empty() : Optional.of(tasksByPriority.first());
  }

  public List<Task> getTasksDueBefore(LocalDateTime deadline) {
    ValidationUtils.requireNotNull(deadline, "Deadline");
    return tasksByPriority.stream()
        .filter(task -> !task.getDueDate().isAfter(deadline))
        .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
  }

  @Override
  protected void afterCreate(Task entity) {
    tasksByPriority.add(entity);
  }

  @Override
  protected void afterDelete(Task entity) {
    tasksByPriority.remove(entity);
  }
}
