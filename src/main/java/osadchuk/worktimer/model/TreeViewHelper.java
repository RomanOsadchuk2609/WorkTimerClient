package osadchuk.worktimer.model;

import javafx.scene.control.TreeItem;
import osadchuk.worktimer.Utils;
import osadchuk.worktimer.entity.Task;

import java.util.List;
import java.util.stream.Collectors;

public class TreeViewHelper {

	public List<TreeItem<Task>> getTasks() {
		return Utils.simpleTaskList.stream()
				.map(Task::new)
				.map(TreeItem::new)
				.collect(Collectors.toList());
	}
}

