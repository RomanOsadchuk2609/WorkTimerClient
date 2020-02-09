package osadchuk.worktimer.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TimeLogDTO {
	private long id;

	private LocalDateTime startTime;

	private LocalDateTime endTime;

	private long userId;

	private long taskId;

}
