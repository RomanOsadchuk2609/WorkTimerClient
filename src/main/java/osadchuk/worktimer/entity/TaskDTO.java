package osadchuk.worktimer.entity;

import lombok.Data;

@Data
public class TaskDTO {
	private long id;

	private String name;

	private String description;

	private long userId;
}
