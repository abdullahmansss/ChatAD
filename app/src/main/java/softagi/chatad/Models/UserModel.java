package softagi.chatad.Models;

public class UserModel
{
    private String username,email,picture;

    public UserModel() {
    }

    public UserModel(String username, String email, String picture) {
        this.username = username;
        this.email = email;
        this.picture = picture;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
