import java.util.ArrayList;
import java.util.List;

/**
 * The person who creates pick lists, assigns them to pickers,
 * and checks the picking stats.
 */
public class Supervisor extends Employee {
    private List<PickList> assignedLists = new ArrayList<>();

    public Supervisor(int id, String name) {
        super(id, name);
    }

    public void assignPickList(Picker picker, PickList list) {
        if (list.getLineItems().isEmpty()) {
            throw new IllegalArgumentException("A pick list needs at least one item");
        }
        list.setAssignment(picker, this);
        picker.addPickList(list);
        assignedLists.add(list);
    }

    public void viewStats(PickList list) {
        System.out.println(list.getSummary());
    }

    public List<PickList> getAssignedLists() {
        return assignedLists;
    }
}
