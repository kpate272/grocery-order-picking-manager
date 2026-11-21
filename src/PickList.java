import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * One customer order that has to be picked.
 * A pick list owns its line items (composition), so line items are
 * only ever created through addItem() and never shared between lists.
 */
public class PickList {
    public static final String ASSIGNED = "ASSIGNED";
    public static final String IN_PROGRESS = "IN PROGRESS";
    public static final String COMPLETE = "COMPLETE";

    private int id;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<LineItem> lineItems = new ArrayList<>();
    private Picker assignedPicker;
    private Supervisor assignedBy;

    public PickList(int id) {
        this.id = id;
        this.status = ASSIGNED;
    }

    public void addItem(Product product, int quantity) {
        if (!status.equals(ASSIGNED) || startTime != null) {
            throw new IllegalStateException("Items can only be added before picking starts");
        }
        lineItems.add(new LineItem(product, quantity));
    }

    // Package-level so only Supervisor.assignPickList sets this
    void setAssignment(Picker picker, Supervisor supervisor) {
        this.assignedPicker = picker;
        this.assignedBy = supervisor;
    }

    public void startPicking() {
        if (status.equals(COMPLETE)) {
            throw new IllegalStateException("Pick list #" + id + " is already complete");
        }
        // If the picker paused and came back, keep the original start time
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
        status = IN_PROGRESS;
    }

    // Called after the picker handles an item. Closes the list when nothing is left.
    public void completeItem(LineItem item) {
        if (!lineItems.contains(item)) {
            throw new IllegalArgumentException("That item is not on this pick list");
        }
        if (item.getStatus() == ItemStatus.PENDING) {
            throw new IllegalStateException("The item has not been handled yet");
        }
        if (isComplete() && !status.equals(COMPLETE)) {
            status = COMPLETE;
            endTime = LocalDateTime.now();
        }
    }

    public boolean isComplete() {
        return getNextItem() == null;
    }

    // First item that has not been handled yet, or null if there are none left
    public LineItem getNextItem() {
        for (LineItem item : lineItems) {
            if (item.getStatus() == ItemStatus.PENDING) {
                return item;
            }
        }
        return null;
    }

    // Units filled (picked or substituted) out of units ordered, as a percentage
    public double fillRate() {
        int requested = 0;
        int filled = 0;
        for (LineItem item : lineItems) {
            requested += item.getQuantityRequested();
            filled += item.getQuantityPicked();
        }
        if (requested == 0) {
            return 0;
        }
        return filled * 100.0 / requested;
    }

    public int countWithStatus(ItemStatus s) {
        int count = 0;
        for (LineItem item : lineItems) {
            if (item.getStatus() == s) {
                count++;
            }
        }
        return count;
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("---------------------------------------------\n");
        sb.append("Pick list #").append(id).append(" summary\n");
        sb.append("---------------------------------------------\n");
        sb.append("Picker:       ").append(assignedPicker == null ? "-" : assignedPicker.getName()).append("\n");
        sb.append("Assigned by:  ").append(assignedBy == null ? "-" : assignedBy.getName()).append("\n");
        sb.append("Status:       ").append(status).append("\n\n");

        for (LineItem item : lineItems) {
            sb.append(String.format("  %-28s %d/%d  %s",
                    item.getProduct().getName(),
                    item.getQuantityPicked(),
                    item.getQuantityRequested(),
                    item.getStatus()));
            if (item.getStatus() == ItemStatus.SUBSTITUTED) {
                sb.append(" (with ").append(item.getSubstituteProduct().getName()).append(")");
            }
            sb.append("\n");
        }

        sb.append("\n");
        sb.append("Items picked:       ").append(countWithStatus(ItemStatus.PICKED)).append("\n");
        sb.append("Items substituted:  ").append(countWithStatus(ItemStatus.SUBSTITUTED)).append("\n");
        sb.append("Items out of stock: ").append(countWithStatus(ItemStatus.OUT_OF_STOCK)).append("\n");
        sb.append("Items still to do:  ").append(countWithStatus(ItemStatus.PENDING)).append("\n");
        sb.append(String.format("Fill rate:          %.1f%%%n", fillRate()));

        if (startTime != null && endTime != null) {
            long seconds = Duration.between(startTime, endTime).getSeconds();
            sb.append(String.format("Time taken:         %d min %d sec%n", seconds / 60, seconds % 60));
        }
        sb.append("---------------------------------------------");
        return sb.toString();
    }

    public int getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public List<LineItem> getLineItems() {
        return lineItems;
    }

    public Picker getAssignedPicker() {
        return assignedPicker;
    }

    public Supervisor getAssignedBy() {
        return assignedBy;
    }
}
