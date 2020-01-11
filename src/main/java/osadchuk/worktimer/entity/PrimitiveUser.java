package osadchuk.worktimer.entity;

public class PrimitiveUser {
    private long id;
    private String name;
    private String base64photo;

    public PrimitiveUser(long id, String name, String base64photo) {
        this.id = id;
        this.name = name;
        this.base64photo = base64photo;
    }

    public PrimitiveUser(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public PrimitiveUser() {

    }

    public String getBase64photo() {
        return base64photo;
    }

    public void setBase64photo(String base64photo) {
        this.base64photo = base64photo;
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
}
