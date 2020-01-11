package osadchuk.worktimer.model;

import java.io.Serializable;

public class UserInfo implements Serializable{
    private String username;
    private String pasword;

    public UserInfo() {
    }

    public UserInfo(String username, String pasword) {

        this.username = username;
        this.pasword = pasword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasword() {
        return pasword;
    }

    public void setPasword(String pasword) {
        this.pasword = pasword;
    }
}
