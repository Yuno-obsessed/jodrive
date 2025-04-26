package sanity.nil.meta.dto;

import java.util.List;

public class Paged<T> {

    List<T> elements;
    public int totalPages;
    public boolean next;
    public boolean previous;

    public Paged<T> of(List<T> elements, int totalPages, boolean next, boolean previous) {
        this.elements = elements;
        this.totalPages = totalPages;
        this.next = next;
        this.previous = previous;
        return this;
    }
}
