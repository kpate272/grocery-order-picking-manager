/**
 * Where a product sits in the store (aisle and shelf).
 * One location can hold more than one product.
 */
public class Location {
    private String aisle;
    private String shelf;

    public Location(String aisle, String shelf) {
        this.aisle = aisle;
        this.shelf = shelf;
    }

    public String getAisle() {
        return aisle;
    }

    public String getShelf() {
        return shelf;
    }

    // Short code that a picker can read quickly, for example "Aisle 2, Shelf B"
    public String getCode() {
        return "Aisle " + aisle + ", Shelf " + shelf;
    }
}
