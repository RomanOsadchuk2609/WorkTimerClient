package osadchuk.worktimer.entity;

import java.io.Serializable;
import java.util.Objects;

public class SimpleTask implements Serializable {
    private long id;
    private String name;
    private long userId;
    private String username;
    private long projectId;
    private String projectName;
    private boolean hasSubtask;
    private long parentTaskId;

    public SimpleTask() {
    }

    public SimpleTask(long projectId, String projectName, long id, String name,
                      long userId, String username) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.username = username;
    }

    public SimpleTask(long projectId, String projectName, long id, String name,
                      boolean hasSubtask, long parentTaskId, long userId, String username) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.id = id;
        this.name = name;
        this.hasSubtask = hasSubtask;
        this.parentTaskId = parentTaskId;
        this.userId = userId;
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleTask that = (SimpleTask) o;
        return projectId == that.projectId &&
                id == that.id &&
                hasSubtask == that.hasSubtask &&
                parentTaskId == that.parentTaskId &&
                userId == that.userId &&
                Objects.equals(projectName, that.projectName) &&
                Objects.equals(name, that.name) &&
                Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {

        return Objects.hash(projectId, projectName, id, name, userId, username);
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
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
