import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
	    InventoryControl inventory_control = new InventoryControl();

	    LocalDate test_date = LocalDate.of(2024, 11, 8);
	    PrescriptionItemType test_prescription = new PrescriptionItemType(55, 2, 33.12, "Test Pres", false, "Prestest", test_date);

	    inventory_control.addStock(test_prescription);

	    System.out.println("TEST PRESCRIPTION:\n" + test_prescription);
	    System.out.println("INVENTORY:\n" + inventory_control.inventory);
	    System.out.println("ORDERS:\n" + inventory_control.orders);

	    AutoOrder auto_order = new AutoOrder(15, 100, "Test Pres");
	    inventory_control.addAutoOrder(auto_order);

	    // RUNS EVERY BACKEND CYCLE
	    inventory_control.updateAutoOrders();
	    inventory_control.updateDeliveries();

	    System.out.println("INVENTORY END:\n" + inventory_control.inventory);
    }
}
