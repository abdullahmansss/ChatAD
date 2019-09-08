package softagi.chatad.Models;

public class ChatModel
{
    private String msg,name,img,id;

    public ChatModel() {
    }

    public ChatModel(String msg, String name, String img, String id) {
        this.msg = msg;
        this.name = name;
        this.img = img;
        this.id = id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
