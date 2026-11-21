/**
 * An item the store sells. Each product is stored at one location.
 * The quantityAvailable field plays the part of the Inventory System
 * from the use case diagram, since it tells us how many are in stock.
 */
public class Product {
    private String sku;
    private String name;
    private int quantityAvailable;
    private Location location;

    public Product(String sku, String name, int quantityAvailable, Location location) {
        this.sku = sku;
        this.name = name;
        this.quantityAvailable = quantityAvailable;
        this.location = location;
    }

    public boolean isInStock() {
        return quantityAvailable > 0;
    }

    // Called when a picker takes some of this product off the shelf
    public void removeStock(int amount) {
        if (amount < 0 || amount > quantityAvailable) {
            throw new IllegalArgumentException("Not enough stock for " + name);
        }
        quantityAvailable -= amount;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    public Location getLocation() {
        return location;
    }
}
