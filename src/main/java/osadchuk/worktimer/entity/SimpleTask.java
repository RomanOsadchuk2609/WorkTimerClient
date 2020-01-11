package osadchuk.worktimer.entity;

import java.io.Serializable;
import java.util.Objects;

public class SimpleTask implements Serializable {
    private long projectId;
    private String projectName;
    private long taskId;
    private String taskName;
    private boolean hasSubtask;
    private long parentTaskId;
    private long performerId;
    private String username;

    public SimpleTask() {
    }

    public SimpleTask(long projectId, String projectName, long taskId, String taskName,
                      long performerId, String username) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.taskId = taskId;
        this.taskName = taskName;
        this.performerId = performerId;
        this.username = username;
    }

    public SimpleTask(long projectId, String projectName, long taskId, String taskName,
                      boolean hasSubtask, long parentTaskId, long performerId, String username) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.taskId = taskId;
        this.taskName = taskName;
        this.hasSubtask = hasSubtask;
        this.parentTaskId = parentTaskId;
        this.performerId = performerId;
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleTask that = (SimpleTask) o;
        return projectId == that.projectId &&
                taskId == that.taskId &&
                hasSubtask == that.hasSubtask &&
                parentTaskId == that.parentTaskId &&
                performerId == that.performerId &&
                Objects.equals(projectName, that.projectName) &&
                Objects.equals(taskName, that.taskName) &&
                Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {

        return Objects.hash(projectId, projectName, taskId, taskName, performerId, username);
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isHasSubtask() {
        return hasSubtask;
    }

    public void setHasSubtask(boolean hasSubtask) {
        this.hasSubtask = hasSubtask;
    }

    public long getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(long parentTaskId) {
        this.parentTaskId = parentTaskId;
    }
}
