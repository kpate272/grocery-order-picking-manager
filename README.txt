Grocery Order-Picking Manager
ITCS 3112 - Final Project

This is a console program written in Java. A supervisor assigns pick lists
to pickers, and a picker works through a list one item at a time, marking
each item as picked, substituted, or out of stock. When a list is done, the
program shows a summary with the fill rate.


WHAT YOU NEED
-------------
Java 11 or newer. You can check by opening a terminal (or Command Prompt on
Windows) and typing:

    java -version


HOW TO RUN IT
-------------
Option 1: Run the jar file (easiest)

    1. Open a terminal in the GroceryPickingManager folder.
    2. Type:

           java -jar GroceryPickingManager.jar

Option 2: Compile and run from the source code

    1. Open a terminal in the GroceryPickingManager folder.
    2. Compile:

           javac -d out src/*.java

       (On Windows Command Prompt use: javac -d out src\*.java)

    3. Run:

           java -cp out Main

You can also open the src folder in an IDE like IntelliJ, Eclipse, or VS Code
and run Main.java.


HOW TO USE IT
-------------
Everything is done by typing a number from the menu and pressing Enter.

Main menu:
    1. Assign a new pick list   (Supervisor)
    2. Work on a pick list      (Picker)
    3. View picking stats       (Supervisor)
    4. Show store products
    0. Exit

1. Assign a new pick list
   Choose a picker, then add products one at a time by typing the product
   number and the quantity needed. Type 0 when you are done. The list is
   then assigned to that picker.

2. Work on a pick list
   Choose a picker, then choose one of their open lists. The program shows
   the next item, how many are needed, where it is in the store, and how
   many are in stock. For each item you choose what happened:
       1 = Picked it (then enter the quantity)
       2 = Substitute it (then choose the replacement product)
       3 = Mark out of stock
       0 = Pause and go back to the main menu
   If you pause, you can come back to the same list later and it carries on
   from the item you stopped at. Once every item is handled, the list is
   completed and the summary is shown.

3. View picking stats
   Shows every pick list with its picker, status, and fill rate. Choose one
   to see the full summary for that list.

4. Show store products
   Shows every product with its SKU, how many are in stock, and its aisle
   and shelf.


SAMPLE DATA
-----------
The program starts with some sample data so it can be tested right away:

    - Pickers: Alex and Priya
    - Supervisor: Sam
    - 10 products spread across 4 aisles
    - Pick list #1 is already assigned to Alex
    - Pick list #2 is already assigned to Priya

A few items are set up on purpose to test the different cases:

    - Large Eggs has 0 in stock, so it has to be substituted (for example
      with Free Range Eggs) or marked out of stock.
    - Basmati Rice: the list needs 4 but only 3 are in stock, so entering 4
      shows an error and you can pick 3 instead (a short pick).
    - Cheddar Cheese: the list needs 3 but only 2 are in stock.


QUICK TEST
----------
This walks through the main use cases in a couple of minutes:

    1. Choose 2 (Work on a pick list), then picker 1 (Alex), then list 1.
    2. Bananas: choose 1 and enter 2.
    3. Milk: choose 1 and enter 1.
    4. Large Eggs: choose 2 and pick Free Range Eggs as the substitute.
    5. Whole Wheat Bread: choose 0 to pause. You are back at the main menu.
    6. Choose 2, Alex, list 1 again. It resumes at Whole Wheat Bread.
       Choose 1 and enter 1.
    7. Basmati Rice: choose 1 and enter 4. You get an error because only 3
       are in stock. Enter 3.
    8. The list is now complete and the summary shows the fill rate.
    9. Choose 3 (View picking stats) to see both lists.
   10. Choose 1 to assign a new list to Priya, then 2 to work on it.

You can also try typing letters or numbers that are not on the menu. The
program shows a message and asks again instead of crashing.


HOW THE CODE MATCHES THE DESIGN
-------------------------------
Each class from the Milestone 3 class diagram has its own file in src:

    Employee.java     abstract base class for Picker and Supervisor
    Picker.java       works on pick lists
    Supervisor.java   assigns pick lists and views stats
    PickList.java     one order, made up of line items
    LineItem.java     one line on a pick list (pick, substitute, out of stock)
    Product.java      an item in the store, with its stock count
    Location.java     the aisle and shelf for a product
    ItemStatus.java   enum: PENDING, PICKED, SUBSTITUTED, OUT_OF_STOCK
    Main.java         the console menus, following the Milestone 4
                      activity diagram

Fill rate is worked out as the units filled (picked or substituted) divided
by the units ordered.


NOTES
-----
- All data is kept in memory, so everything resets when the program closes.
  This was a planned limit from the requirements (single session).
- The stock count on each product stands in for the Inventory System from
  the use case diagram. Picking or substituting an item lowers its stock.
