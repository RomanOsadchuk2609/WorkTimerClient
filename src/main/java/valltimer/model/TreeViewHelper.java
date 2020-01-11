package valltimer.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import javafx.scene.control.TreeItem;
import valltimer.Utils;
import valltimer.entity.Project;
import valltimer.entity.SimpleTask;
import valltimer.entity.Task;

public class TreeViewHelper{

    public TreeViewHelper() {

    }
        // This method creates an ArrayList of TreeItems (Products)
    public ArrayList<TreeItem> getTasks(){
        ArrayList<TreeItem> products = new ArrayList<TreeItem>();
        /*TreeItem cars = new TreeItem("Cars");
        cars.getChildren().addAll(getCars());
        TreeItem buses = new TreeItem("Buses");
        buses.getChildren().addAll(getBuses());
        TreeItem trucks = new TreeItem("Trucks");
        trucks.getChildren().addAll(getTrucks());
        TreeItem motorbikes = new TreeItem("Motorcycles");
        motorbikes.getChildren().addAll(getMotorcycles());
        products.add(cars);
        products.add(buses);
        products.add(trucks);
        products.add(motorbikes);*/
        return this.getProjects(Utils.simpleTaskList);
    }


    private ArrayList<TreeItem> getProjects(List<SimpleTask> list){
        if (list != null) {

            ArrayList<TreeItem> projects = new ArrayList<TreeItem>();
            Set<Project> projectsSet = new HashSet<>();
            for (SimpleTask simpleTask:list){
                projectsSet.add(new Project(simpleTask.getProjectId(),simpleTask.getProjectName()));
            }
            for (Project p:projectsSet){
                TreeItem item = new TreeItem(p);
                item.getChildren().addAll(this.getTasksByProject(list,p.getId()));
                projects.add(item);
            }
            return projects;
        }
        return null;
    }

    private ArrayList<TreeItem> getTasksByProject(List<SimpleTask> list, long projectId){
        ArrayList<TreeItem> tasks = new ArrayList<TreeItem>();

        for (SimpleTask simpleTask:list){
            if (simpleTask.getProjectId()==projectId && simpleTask.getParentTaskId()==0){
                Task task = new Task(simpleTask.getTaskId(),simpleTask.getTaskName(),simpleTask.getPerformerId());
                TreeItem treeItem = new TreeItem(task);
                if (simpleTask.isHasSubtask()){
                    treeItem.getChildren().addAll(this.getSubtasksByTask(list,simpleTask.getTaskId()));
                }
                tasks.add(treeItem);
            }
        }

        return tasks;
    }

    private ArrayList<TreeItem> getSubtasksByTask(List<SimpleTask> list, long taskId){
        ArrayList<TreeItem> tasks = new ArrayList<TreeItem>();


        for (SimpleTask simpleTask:list){
            if (simpleTask.getParentTaskId()==taskId){
                Task task = new Task(simpleTask.getTaskId(),simpleTask.getTaskName(),simpleTask.getPerformerId());
                TreeItem treeItem = new TreeItem(task);
                if (simpleTask.isHasSubtask()){
                    treeItem.getChildren().addAll(getSubtasksByTask(list,simpleTask.getTaskId()));
                }
                tasks.add(treeItem);
            }
        }

        return tasks;
    }

}

