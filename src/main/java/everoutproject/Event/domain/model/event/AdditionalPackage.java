package everoutproject.Event.domain.model.event;

import java.math.BigDecimal;

public class AdditionalPackage {
    private final Long id;
    private String title;
    private String description;
    private BigDecimal price;

    public AdditionalPackage(Long id, String title, String description, BigDecimal price) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }

    public void setTitle(String title) {this.title = title;}
    public void setDescription(String description) {this.description = description;}
    public void setPrice(BigDecimal price) {this.price = price;}
}
