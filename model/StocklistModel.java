package cs.toronto.edu.models;

public class Stocklist {
    private Integer listId;
    private String listName;
    private Integer userId;

    public Stocklist(Integer listId, String listName, Integer userId) {
        this.listId = listId;
        this.listName = listName;
        this.userId = userId;
    }

    public Integer getListId() { return listId; }
    public String getListName() { return listName; }
    public Integer getUserId() { return userId; }
}
