package adventure.go.goadventure.dto;

import java.util.List;

public class PaginatedResponse<T> {
    private List<T> data;
    private Pagination pagination;

    public PaginatedResponse(int page, int limit, long total, List<T> data) {
        this.data = data;
        this.pagination = new Pagination(page, limit, total);
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}