package paci.estiam.classes;

public class User {


    private int id;
    private String login;
    private String avatarUrl;

    public User() {

    }
    public User(int id, String login) {
        this.id = id;
        this.login = login;

    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String name) {
        this.login = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
