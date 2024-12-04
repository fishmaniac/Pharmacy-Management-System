package PharmacyManagementSystem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    private static void tui(String message) {
	Log.tui(message);
    }
    private static void requests()
    {
	tui("\nInvoke a request to the backend...\n\t"
		+ "-1: quit\n\t"
		+ "0: login\n\t"
		+ "1: logout\n\t"
		+ "2: create account\n\t"
		+ "3: create discount\n\t"
		+ "4: change password\n\t"
		+ "5: get inventory\n\t"
		+ "6: get orders\n\t"
		+ "7: get auto orders\n\t"
		+ "8: get accounts\n\t"
		+ "9: get patients\n\t"
		);
    }
    private static LocalDateTime expirationDate(Scanner scanner) {
	String date;
	tui("Enter year (yyyy):");
	int year = scanner.nextInt();
	scanner.nextLine();

	tui("Enter month (mm):");
	int month = scanner.nextInt();
	scanner.nextLine();

	tui("Enter day (dd):");
	int day = scanner.nextInt();
	scanner.nextLine();

	return LocalDateTime.of(LocalDate.of(year, month, day), LocalTime.MIDNIGHT);
    }

    private static Object requestData(Scanner scanner, final Request request)
    {
	List<Object> data = new ArrayList<>();
	Object text = null;

	switch (request)
	{
	    case Login:
		tui("Enter your username:");
		text = scanner.nextLine();
		return text;
	    case Logout:
		tui("Logging out...");
		return Response.LoggedOut;
	    case CreateAccount:
		tui("Enter account user's age:");
		data.add(scanner.nextInt());
		scanner.nextLine();
		tui("Enter account user's full name:");
		data.add(scanner.nextLine());
		tui("Enter account username:");
		data.add(scanner.nextLine());
		tui("Choose account permission level...\n\t"
			+ "0: Cashier\n\t"
			+ "1: PharmacyTechnician\n\t"
			+ "2: Pharmacist\n\t"
			+ "3: PharmacyManager\n\t"
			+ "4: Admin");

		switch (scanner.nextInt()) {
		    case 0:
			data.add(PermissionLevel.Cashier);
			break;
		    case 1:
			data.add(PermissionLevel.PharmacyTechnician);
			break;
		    case 2:
			data.add(PermissionLevel.Pharmacist);
			break;
		    case 3:
			data.add(PermissionLevel.PharmacyManager);
			break;
		    case 4:
			data.add(PermissionLevel.Admin);
			break;
		    default:
			Log.error("Invalid permission level.");
		}
		scanner.nextLine();
		return data;
	    case CreateStock:
		break;
	    case CreateDiscount:
		tui("Enter discount type...\n\t"
			+ "0: Flat discount:\n\t"
			+ "1: Percent discount:"
		   );
		text = scanner.nextInt();
		scanner.nextLine();

		switch ((int) text) {
		    case 0:
			tui("Enter flat discount (double):");
			data.add(DiscountType.Discount);
			break;
		    case 1:
			tui("Enter percent discount (double 0.0-1.0):");
			data.add(DiscountType.PercentDiscount);
			break;
		    default:
			tui("Invalid discount type.");
			return Response.DiscountFailed;
		}

		data.add(scanner.nextDouble());
		scanner.nextLine();

		tui("Enter expiration date...");
		data.add(expirationDate(scanner));

		tui("Enter Stock ID to apply discount to:");
		data.add(UUID.fromString(scanner.nextLine()));

		return data;
	    case CreateCustomer:
		break;
	    case CreateOrder:
		break;
	    case CreateAutoOrder:
		break;
	    case ChangePassword:
		tui("Enter current password:");
		text = scanner.nextLine();

		return text;
	    case GetAccounts:
		return Response.Ok;
	    case GetAutoOrders:
		return Response.Ok;
	    case GetInventory:
		return Response.Ok;
	    case GetOrders:
		return Response.Ok;
	    case GetCustomers:
		return Response.Ok;
	    case RemoveAccount:
		break;
	    case RemoveAutoOrder:
		break;
	    case RemoveCustomer:
		break;
	    case RemoveDiscount:
		break;
	    case RemoveDrug:
		break;
	    case RemoveOrder:
		break;
	}

	Log.error("Invalid request data.");

	return null;
    }
    private static Object responseData(Scanner scanner, Backend backend, Response response) {
	List<Object> data = new ArrayList<>();
	String text = null;
	String temp = null;

	switch (response) {
	    case AccountLocked:
		tui("Account locked. It must be unlocked by an admin.");
		return Response.AccountLocked;
	    case AccountCreated:
		tui("Account created.");
		return Response.AccountCreated;
	    case AccountDoesNotExist:
		tui("Account does not exist.");
		return Response.AccountDoesNotExist;
	    case FirstLogin:
	    case NewPassword:
		tui("Create a new password:");
		text = scanner.nextLine();

		tui("Confirm your password:");
		if (text.equals(scanner.nextLine())) {
		    tui("Password updated. Please relogin now.");
		    return text;
		} else tui("Password did not match.");

		return null;
	    case LoginFailed:
		tui("Login failed.");
		return Response.LoginFailed;
	    case AlreadyLoggedIn:
		tui("A user is already logged into the system.");
		return Response.AlreadyLoggedIn;
	    case GetPassword:
		tui("Enter your account password:");
		return scanner.nextLine();
	    case LoggedOut:
		tui("Logged out.");
		return Response.LoggedOut;
	    case DiscountCreated:
		tui("Discount created.");
		return Response.DiscountCreated;
	    case DiscountFailed:
		tui("Discount creation failed.");
		return Response.DiscountFailed;
	    case Unauthorized:
		tui("401: Unauthorized");
		return Response.Unauthorized;
	    case Forbidden:
		tui("403: Forbidden");
		return Response.Forbidden;
	    case NotFound:
		tui("404: Not found.");
		return Response.NotFound;
	    case Ok:
		break;
	}

	return null;
    }
    private static void request(Scanner scanner, Backend backend, int input)
    {
	if (input > Request.values().length - 1 || input < 0) {
	    tui("Invalid request.");
	    return;
	}

	Request request = Request.values()[input];
	Object data = requestData(scanner, request);
	Log.trace("DATA: " + data.toString());

	Response response = backend.receive(request, data);

	data = responseData(scanner, backend, response);
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
	    try 
	    {
		request(scanner, backend, request);

		requests();
		request = scanner.nextInt();
		scanner.nextLine();
	    }
	    catch (Exception e)
	    {
		Log.error("Exception in Main: " + e.getMessage());
		e.printStackTrace();
	    }
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
