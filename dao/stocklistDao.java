package cs.toronto.edu.models;

public class Stocklist {
    private Integer stocklistId;
    private Integer userId;
    private String visibility; // "public" or "private"

    public Stocklist(Integer stocklistId, Integer userId, String visibility) {
        this.stocklistId = stocklistId;
        this.userId = userId;
        this.visibility = visibility;
    }

    public Integer getStocklistId() { return stocklistId; }
    public Integer getUserId() { return userId; }
    public String getVisibility() { return visibility; }
}
