package osadchuk.worktimer.util;

public class TimerConstants {
    private TimerConstants(){}

    public static final String EMPTY_STRING = "";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String OKAY = "Okay";
    public static final String NOT_SENT_DATA = "notSentData";
    public static final String REFRESH = "Refresh";
    public static final String TREE_VIEW_HEADER = "Tasks";

    public static final class APP {
        private APP(){}

        public static final String NAME = "WorkTimer";
        public static final int WIDTH = 325;
        public static final int HEIGHT = 450;
    }

    public static final class ERROR {
        private ERROR(){}

        public static final String CONNECTION = "Connection Error";
        public static final String AUTHENTICATION = "Authentication Error";
    }

    public enum PROPERTY {

        PROTOCOL("protocol"),
        IP_ADDRESS("ip"),
        PORT("port");

        private String name;

        PROPERTY(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static final class URL {
        private URL(){}

        public static final String TIME_LOG_START = "time_log/create";
        public static final String TIME_LOG_STOP = "time_log/stop";
    }

}
