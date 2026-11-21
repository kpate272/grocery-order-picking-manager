import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * ITCS 3112 Project - Grocery Order-Picking Manager
 *
 * Console program where a supervisor assigns pick lists and a picker works
 * through them one item at a time, marking each item as picked, substituted,
 * or out of stock. Data is kept in memory for one session only.
 */
public class Main {
    private static final Scanner in = new Scanner(System.in);

    private static final List<Product> products = new ArrayList<>();
    private static final List<Picker> pickers = new ArrayList<>();
    private static Supervisor supervisor;
    private static int nextListId = 1;

    public static void main(String[] args) {
        loadSampleData();

        System.out.println("=============================================");
        System.out.println("        Grocery Order-Picking Manager");
        System.out.println("=============================================");

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("MAIN MENU");
            System.out.println("1. Assign a new pick list   (Supervisor)");
            System.out.println("2. Work on a pick list      (Picker)");
            System.out.println("3. View picking stats       (Supervisor)");
            System.out.println("4. Show store products");
            System.out.println("0. Exit");
            int choice = readInt("Choose an option: ", 0, 4);

            switch (choice) {
                case 1: assignPickListMenu(); break;
                case 2: workOnPickListMenu(); break;
                case 3: viewStatsMenu(); break;
                case 4: showProducts(); break;
                case 0: running = false; break;
            }
        }
        System.out.println("Goodbye!");
    }

    // ---------------- Supervisor: assign a pick list ----------------

    private static void assignPickListMenu() {
        System.out.println();
        System.out.println("ASSIGN A NEW PICK LIST (Supervisor: " + supervisor.getName() + ")");
        Picker picker = choosePicker();
        if (picker == null) {
            return;
        }

        PickList list = new PickList(nextListId);
        List<Product> added = new ArrayList<>();

        while (true) {
            System.out.println();
            System.out.println("Products:");
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                System.out.printf("%2d. %-28s (in stock: %d)%n", i + 1, p.getName(), p.getQuantityAvailable());
            }
            int pick = readInt("Add which product? (0 when done): ", 0, products.size());
            if (pick == 0) {
                break;
            }
            Product product = products.get(pick - 1);
            if (added.contains(product)) {
                System.out.println(product.getName() + " is already on this list.");
                continue;
            }
            int qty = readInt("Quantity needed (1-50): ", 1, 50);
            list.addItem(product, qty);
            added.add(product);
            System.out.println("Added " + qty + " x " + product.getName() + ".");
        }

        if (list.getLineItems().isEmpty()) {
            System.out.println("No items were added, so the pick list was cancelled.");
            return;
        }
        supervisor.assignPickList(picker, list);
        nextListId++;
        System.out.println("Pick list #" + list.getId() + " with " + list.getLineItems().size()
                + " item(s) assigned to " + picker.getName() + ".");
    }

    // ---------------- Picker: work on a pick list ----------------

    private static void workOnPickListMenu() {
        System.out.println();
        System.out.println("WORK ON A PICK LIST");
        Picker picker = choosePicker();
        if (picker == null) {
            return;
        }

        List<PickList> open = picker.getOpenPickLists();
        if (open.isEmpty()) {
            System.out.println(picker.getName() + " has no open pick lists right now.");
            return;
        }

        System.out.println();
        System.out.println("Open pick lists for " + picker.getName() + ":");
        for (int i = 0; i < open.size(); i++) {
            PickList l = open.get(i);
            int done = l.getLineItems().size() - l.countWithStatus(ItemStatus.PENDING);
            System.out.printf("%d. Pick list #%d  (%d of %d items done, %s)%n",
                    i + 1, l.getId(), done, l.getLineItems().size(), l.getStatus());
        }
        int choice = readInt("Choose a pick list (0 to go back): ", 0, open.size());
        if (choice == 0) {
            return;
        }
        PickList list = open.get(choice - 1);
        boolean resuming = list.getStatus().equals(PickList.IN_PROGRESS);
        picker.startPickList(list);
        System.out.println((resuming ? "Resumed" : "Started") + " pick list #" + list.getId() + ".");

        // Loop through the items until none are left (or the picker pauses)
        while (!list.isComplete()) {
            LineItem item = list.getNextItem();
            Product p = item.getProduct();
            int position = list.getLineItems().indexOf(item) + 1;

            System.out.println();
            System.out.println("Item " + position + " of " + list.getLineItems().size());
            System.out.println("  Product:  " + p.getName() + " (SKU " + p.getSku() + ")");
            System.out.println("  Need:     " + item.getQuantityRequested());
            System.out.println("  Location: " + p.getLocation().getCode());
            System.out.println("  In stock: " + p.getQuantityAvailable());
            System.out.println("1. Picked it");
            System.out.println("2. Substitute it");
            System.out.println("3. Mark out of stock");
            System.out.println("0. Pause and go back to the main menu");
            int action = readInt("What happened? ", 0, 3);

            if (action == 0) {
                System.out.println("Pick list #" + list.getId() + " is paused. You can come back to it later.");
                return;
            }

            boolean handled = false;
            if (action == 1) {
                handled = handlePick(item);
            } else if (action == 2) {
                handled = handleSubstitute(item);
            } else if (action == 3) {
                item.markOutOfStock();
                System.out.println(p.getName() + " marked as out of stock.");
                handled = true;
            }

            if (handled) {
                list.completeItem(item);
            }
        }

        System.out.println();
        System.out.println("All items handled. Pick list #" + list.getId() + " is complete!");
        System.out.println(list.getSummary());
    }

    // Returns true if the item was picked, false if the picker backed out
    private static boolean handlePick(LineItem item) {
        Product p = item.getProduct();
        if (!p.isInStock()) {
            System.out.println("The system shows 0 " + p.getName() + " in stock.");
            System.out.println("Try a substitute or mark it out of stock instead.");
            return false;
        }
        while (true) {
            int qty = readInt("Quantity picked (1-" + item.getQuantityRequested() + "): ",
                    1, item.getQuantityRequested());
            try {
                item.pick(qty);
                System.out.println("Picked " + qty + " x " + p.getName() + ".");
                if (qty < item.getQuantityRequested()) {
                    System.out.println("Note: this is a short pick (" + qty + " of "
                            + item.getQuantityRequested() + ").");
                }
                return true;
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage() + ". Please try again.");
            }
        }
    }

    // Returns true if a substitute was chosen, false if the picker cancelled
    private static boolean handleSubstitute(LineItem item) {
        List<Product> options = new ArrayList<>();
        for (Product p : products) {
            if (p != item.getProduct() && p.isInStock()) {
                options.add(p);
            }
        }
        if (options.isEmpty()) {
            System.out.println("There are no other products in stock to use as a substitute.");
            return false;
        }

        System.out.println("Choose a substitute for " + item.getProduct().getName() + ":");
        for (int i = 0; i < options.size(); i++) {
            Product p = options.get(i);
            System.out.printf("%2d. %-28s %s (in stock: %d)%n",
                    i + 1, p.getName(), p.getLocation().getCode(), p.getQuantityAvailable());
        }
        int choice = readInt("Substitute with (0 to cancel): ", 0, options.size());
        if (choice == 0) {
            return false;
        }

        Product replacement = options.get(choice - 1);
        item.substitute(replacement);
        System.out.println("Substituted with " + item.getQuantityPicked() + " x " + replacement.getName() + ".");
        if (item.getQuantityPicked() < item.getQuantityRequested()) {
            System.out.println("Note: only " + item.getQuantityPicked() + " of the substitute were in stock.");
        }
        return true;
    }

    // ---------------- Supervisor: view stats ----------------

    private static void viewStatsMenu() {
        System.out.println();
        System.out.println("PICKING STATS");
        List<PickList> lists = supervisor.getAssignedLists();
        if (lists.isEmpty()) {
            System.out.println("No pick lists have been assigned yet.");
            return;
        }

        for (int i = 0; i < lists.size(); i++) {
            PickList l = lists.get(i);
            System.out.printf("%d. Pick list #%d  %-8s %-12s fill rate %.1f%%%n",
                    i + 1, l.getId(), l.getAssignedPicker().getName(), l.getStatus(), l.fillRate());
        }
        int choice = readInt("Choose a pick list to see details (0 to go back): ", 0, lists.size());
        if (choice == 0) {
            return;
        }
        System.out.println();
        supervisor.viewStats(lists.get(choice - 1));
    }

    // ---------------- Helpers ----------------

    private static void showProducts() {
        System.out.println();
        System.out.printf("%-6s %-28s %-8s %s%n", "SKU", "Product", "Stock", "Location");
        for (Product p : products) {
            System.out.printf("%-6s %-28s %-8d %s%n",
                    p.getSku(), p.getName(), p.getQuantityAvailable(), p.getLocation().getCode());
        }
    }

    private static Picker choosePicker() {
        System.out.println("Pickers:");
        for (int i = 0; i < pickers.size(); i++) {
            System.out.println((i + 1) + ". " + pickers.get(i).getName());
        }
        int choice = readInt("Choose a picker (0 to go back): ", 0, pickers.size());
        if (choice == 0) {
            return null;
        }
        return pickers.get(choice - 1);
    }

    // Keeps asking until the user types a whole number between min and max
    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            if (!in.hasNextLine()) {
                System.out.println();
                System.out.println("No more input. Goodbye!");
                System.exit(0);
            }
            String line = in.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Please enter a number from " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("That is not a valid number. Please try again.");
            }
        }
    }

    // Sample store data so the program can be tested right away
    private static void loadSampleData() {
        Location produceA = new Location("1", "A");
        Location produceB = new Location("1", "B");
        Location dairyA = new Location("2", "A");
        Location dairyB = new Location("2", "B");
        Location dairyC = new Location("2", "C");
        Location bakery = new Location("3", "A");
        Location dryB = new Location("4", "B");
        Location dryC = new Location("4", "C");

        products.add(new Product("1001", "Bananas (bunch)", 20, produceA));
        products.add(new Product("1002", "Gala Apples 3 lb bag", 8, produceB));
        products.add(new Product("2001", "2% Milk 4L", 5, dairyA));
        products.add(new Product("2002", "Large Eggs dozen", 0, dairyB));
        products.add(new Product("2003", "Free Range Eggs dozen", 6, dairyB));
        products.add(new Product("2004", "Cheddar Cheese 400g", 2, dairyC));
        products.add(new Product("3001", "Whole Wheat Bread", 10, bakery));
        products.add(new Product("3002", "White Bread", 4, bakery));
        products.add(new Product("4001", "Spaghetti 900g", 12, dryB));
        products.add(new Product("4002", "Basmati Rice 2kg", 3, dryC));

        Picker alex = new Picker(101, "Alex");
        Picker priya = new Picker(102, "Priya");
        pickers.add(alex);
        pickers.add(priya);
        supervisor = new Supervisor(201, "Sam");

        // Pick list #1 for Alex (has eggs that are out of stock, to test substitutes)
        PickList first = new PickList(nextListId++);
        first.addItem(products.get(0), 2);   // Bananas
        first.addItem(products.get(2), 1);   // Milk
        first.addItem(products.get(3), 1);   // Large Eggs (0 in stock)
        first.addItem(products.get(6), 1);   // Whole Wheat Bread
        first.addItem(products.get(9), 4);   // Basmati Rice (only 3 in stock)
        supervisor.assignPickList(alex, first);

        // Pick list #2 for Priya
        PickList second = new PickList(nextListId++);
        second.addItem(products.get(1), 1);  // Apples
        second.addItem(products.get(5), 3);  // Cheddar (only 2 in stock)
        second.addItem(products.get(8), 2);  // Spaghetti
        supervisor.assignPickList(priya, second);
    }
}
