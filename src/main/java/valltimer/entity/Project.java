package valltimer.entity;

import java.util.Objects;

public class Project {
    long id;
    String projectName;

    public Project() {
    }

    public Project(long id, String projectName) {

        this.id = id;
        this.projectName = projectName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    public String toString() {
        return  projectName ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return id == project.id &&
                Objects.equals(projectName, project.projectName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, projectName);
    }
}
