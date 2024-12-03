package PharmacyManagementSystem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static void tui(String message) {
	Log.tui(message);
    }
    private static void requests()
    {
	tui("Invoke a request to the backend...\n\t"
		+ "-1: quit\n\t"
		+ "0: login\n\t"
		+ "1: create account\n\t"
		+ "2: create discount"
		);
    }
    private static Object request_data(Scanner scanner, final Request request)
    {
	List<Object> data = new ArrayList<>();
	String text = null;

	switch (request)
	{
	    case Login:
		tui("Enter your username...");
		text = scanner.nextLine();
		return text;
	    case CreateAccount:
		tui("Enter account user's age: ");
		data.add(scanner.nextInt());
		scanner.nextLine();
		tui("Enter account user's full name: ");
		data.add(scanner.nextLine());
		tui("Enter account username: ");
		data.add(scanner.nextLine());
		return data;
	    case CreateDiscount:
		return data;
	}

	return null;
    }
    private static Object response_data(Scanner scanner, Backend backend, Response response) {
	List<Object> data = new ArrayList<>();
	String text = null;
	String temp = null;

	switch (response) {
	    case AccountLocked:
		tui("Account locked. It must be unlocked by an admin.");
		return null;
	    case AccountCreated:
		return null;
	    case AccountDoesNotExist:
		tui("Account does not exist.");
		return null;
	    case FirstLogin:
		tui("Create a new password...");
		text = scanner.nextLine();

		tui("Confirm your password...");
		if (text.equals(scanner.nextLine())) {
		    tui("Password updated.");
		    return text;
		} else tui("Password did not match.");

		return null;
	    case LoginFailed:
		tui("Login failed...");
		return null;
	    case GetPassword:
		tui("Enter your account password...");
		text = scanner.nextLine();

		return text;
	    case DiscountCreated:
		return null;
	}

	return null;
    }
    private static void request(Scanner scanner, Backend backend, int input)
    {
	if (input > Request.values().length - 1) {
	    tui("Invalid request.");
	}

	Request request = Request.values()[input];
	Object data = request_data(scanner, request);
	Log.trace("DATA: " + data.toString());

	Response response = backend.receive(request, data);

	data = response_data(scanner, backend, response);
	backend.send(response, data);
    }
    public static void main(String[] args)
    {
	Backend backend = new Backend();
	tui("Welcome to the Pharmacy Management System.");

	Scanner scanner = new Scanner(System.in);

	requests();
	int request = scanner.nextInt();
	scanner.nextLine();

	while (request != -1) {
	    request(scanner, backend, request);

	    requests();
	    request = scanner.nextInt();
	    scanner.nextLine();
	}

	scanner.close();


	// InventoryControl inventory_control = new InventoryControl();
	//
	// LocalDateTime test_date = LocalDateTime.of(2024, 11, 8, 0, 0);
	// Discount test_discount = new PercentDiscount(0.50);
	// Drug test_prescription = new Drug(55, 2, 33.12, "Test Pres", test_discount, true, "Prestest", test_date);
	//
	// inventory_control.addStock(test_prescription);
	//
	// System.out.println("TEST PRESCRIPTION:\n" + test_prescription);
	// System.out.println("INVENTORY:\n" + inventory_control.inventory);
	// System.out.println("ORDERS:\n" + inventory_control.orders);
	//
	// AutoOrder auto_order = new AutoOrder(15, 100, "Test Pres");
	// inventory_control.addAutoOrder(auto_order);
	//
	// // RUNS EVERY BACKEND CYCLE
	// inventory_control.updateAutoOrders();
	// inventory_control.updateDeliveries();
	//
	// System.out.println("INVENTORY END:\n" + inventory_control.inventory);
    }
}
