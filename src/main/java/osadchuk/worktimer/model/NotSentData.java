package osadchuk.worktimer.model;

public class NotSentData {
    private long timerId;
    private long date;

    public NotSentData(long timerId, long date) {
        this.timerId = timerId;
        this.date = date;
    }

    public NotSentData() {
    }

    public long getTimerId() {
        return timerId;
    }

    public void setTimerId(long timerId) {
        this.timerId = timerId;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
