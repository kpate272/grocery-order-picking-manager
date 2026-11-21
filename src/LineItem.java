/**
 * One line on a pick list, for example "2 x Bananas".
 * A line item belongs to exactly one pick list and points to one product.
 */
public class LineItem {
    private Product product;
    private int quantityRequested;
    private int quantityPicked;
    private ItemStatus status;
    private Product substituteProduct; // only set when the item is substituted

    public LineItem(Product product, int quantityRequested) {
        if (quantityRequested <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        this.product = product;
        this.quantityRequested = quantityRequested;
        this.quantityPicked = 0;
        this.status = ItemStatus.PENDING;
    }

    // Picker found the item and is taking qty of it
    public void pick(int qty) {
        checkStillPending();
        if (qty < 1 || qty > quantityRequested) {
            throw new IllegalArgumentException("Quantity must be between 1 and " + quantityRequested);
        }
        if (qty > product.getQuantityAvailable()) {
            throw new IllegalArgumentException("Only " + product.getQuantityAvailable() + " in stock");
        }
        product.removeStock(qty);
        quantityPicked = qty;
        status = ItemStatus.PICKED;
    }

    // Picker swaps the item for a similar product
    public void substitute(Product replacement) {
        checkStillPending();
        if (replacement == product) {
            throw new IllegalArgumentException("The substitute has to be a different product");
        }
        if (!replacement.isInStock()) {
            throw new IllegalArgumentException(replacement.getName() + " is out of stock too");
        }
        int qty = Math.min(quantityRequested, replacement.getQuantityAvailable());
        replacement.removeStock(qty);
        substituteProduct = replacement;
        quantityPicked = qty;
        status = ItemStatus.SUBSTITUTED;
    }

    // Picker could not find the item or anything close to it
    public void markOutOfStock() {
        checkStillPending();
        quantityPicked = 0;
        status = ItemStatus.OUT_OF_STOCK;
    }

    private void checkStillPending() {
        if (status != ItemStatus.PENDING) {
            throw new IllegalStateException("This item has already been handled");
        }
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantityRequested() {
        return quantityRequested;
    }

    public int getQuantityPicked() {
        return quantityPicked;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public Product getSubstituteProduct() {
        return substituteProduct;
    }
}
