package Server;

/**
 * Created by nicholas on 22-May-17.
 */
public class UserInfo {

    private static UserInfo instance = null;
    private String user;

    private UserInfo(){}

    public static UserInfo getInstance(){
        if(instance == null) {
            instance = new UserInfo();
        }
        return instance;
    }

    public void setUser(String user){
        this.user = user;
    }

    public String getUser(){
        return user;
    }

    public void resetInstance(){
        instance = null;
    }

}
