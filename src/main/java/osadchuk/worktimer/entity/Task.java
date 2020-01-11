package osadchuk.worktimer.entity;

public class Task {
    long id;
    String taskName;
    long performerId;

    public Task(long id, String taskName, long performerId) {
        this.id = id;
        this.taskName = taskName;
        this.performerId = performerId;
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

    public long getPerformerId() {
        return performerId;
    }

    public void setPerformerId(long performerId) {
        this.performerId = performerId;
    }

    @Override
    public String toString() {
        return taskName;
    }
}
