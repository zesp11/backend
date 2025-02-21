package adventure.go.goadventure.dto;

import java.util.List;

public class PaginatedUserResponse {
    private int page;
    private int limit;
    private long total;
    private List<UserDTO> users;

    public PaginatedUserResponse(int page, int limit, long total, List<UserDTO> users) {
        this.page = page;
        this.limit = limit;
        this.total = total;
        this.users = users;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<UserDTO> getUsers() {
        return users;
    }

    public void setUsers(List<UserDTO> users) {
        this.users = users;
    }
}