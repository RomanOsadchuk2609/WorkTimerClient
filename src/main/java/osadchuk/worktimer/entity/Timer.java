package osadchuk.worktimer.entity;

import java.util.Date;

public class Timer {
    private long id;
    private long starttime;
    private long endTime;

    public Timer() {
    }

    public Timer(long id, long starttime, long endTime) {

        this.id = id;
        this.starttime = starttime;
        this.endTime = endTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStarttime() {
        return starttime;
    }

    public void setStarttime(long starttime) {
        this.starttime = starttime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
