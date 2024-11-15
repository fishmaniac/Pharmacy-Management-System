package PharmacyManagementSystem;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args)
    {
	Backend backend = new Backend();
	InventoryControl inventory_control = new InventoryControl();

	LocalDateTime test_date = LocalDateTime.of(2024, 11, 8, 0, 0);
	Discount test_discount = new PercentDiscount(0.50);
	Drug test_prescription = new Drug(55, 2, 33.12, "Test Pres", test_discount, true, "Prestest", test_date);

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
