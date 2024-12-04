package main.java.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Logs") 
public class LogSchema {
    @Id
    private String id;
    private String userId;
    private String userLog;

    public String getId() {
        return id;
    }
    public String getUserId() {
        return userId;
    }
    public String getUserLog() {
        return userLog;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public void setUserLog(String userLog) {
        this.userLog = userLog;
    }
    
    public String toString(){
        String logRepresentation = "";
        logRepresentation = "{"+ "id: "+ this.id + ", \n" + "userId: "+ this.userId+ ", \n" + "userLog: "+ this.userLog+"}";
        return logRepresentation;

    }

}
