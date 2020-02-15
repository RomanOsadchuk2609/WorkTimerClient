package osadchuk.worktimer.entity;

public class Task {
	private long id;
	private String taskName;
	private long userId;

	public Task(long id, String taskName, long userId) {
		this.id = id;
		this.taskName = taskName;
		this.userId = userId;
	}

	public Task(SimpleTask simpleTask) {
		this.id = simpleTask.getId();
		this.taskName = simpleTask.getName();
		this.userId = simpleTask.getUserId();
	}

	public Task() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return taskName;
	}
}
