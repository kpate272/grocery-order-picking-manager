import java.util.ArrayList;
import java.util.List;

/**
 * The worker who walks the store and collects the items on a pick list.
 * A picker can be given many pick lists over time.
 */
public class Picker extends Employee {
    private List<PickList> pickLists = new ArrayList<>();

    public Picker(int id, String name) {
        super(id, name);
    }

    public void startPickList(PickList list) {
        if (list.getAssignedPicker() != this) {
            throw new IllegalStateException("This pick list is not assigned to " + getName());
        }
        list.startPicking();
    }

    // Used by Supervisor.assignPickList so both sides of the link stay in sync
    void addPickList(PickList list) {
        pickLists.add(list);
    }

    public List<PickList> getPickLists() {
        return pickLists;
    }

    // Lists that still have work left on them
    public List<PickList> getOpenPickLists() {
        List<PickList> open = new ArrayList<>();
        for (PickList list : pickLists) {
            if (!list.isComplete()) {
                open.add(list);
            }
        }
        return open;
    }
}
