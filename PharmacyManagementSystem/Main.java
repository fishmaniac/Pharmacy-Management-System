package PharmacyManagementSystem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

class TUI {
    private static void tui(String message) {
        Log.tui(message);
    }

    private static LocalDateTime date(Scanner scanner) {
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

    public static String login(Scanner scanner) {
        tui("Enter your username:");
        return scanner.nextLine();
    }

    public static Object createAccount(Scanner scanner) {
        tui("Enter account user's age:");
        int age = scanner.nextInt();
        scanner.nextLine();

        tui("Enter account user's full name:");
        String name = scanner.nextLine();

        tui("Enter account username:");
        String login = scanner.nextLine();

        tui("Choose account permission level...");
        for (PermissionLevel permission : PermissionLevel.values()) {
            tui("\t" + permission.ordinal() + ": " + permission);
        }

        PermissionLevel permission;
        switch (scanner.nextInt()) {
            case 0:
                permission = PermissionLevel.Cashier;
                break;
            case 1:
                permission = PermissionLevel.PharmacyTechnician;
                break;
            case 2:
                permission = PermissionLevel.Pharmacist;
                break;
            case 3:
                permission = PermissionLevel.PharmacyManager;
                break;
            case 4:
                permission = PermissionLevel.Admin;
                break;
            default:
                Log.error("Invalid permission level.");
                return Response.BadRequest;
        }
        scanner.nextLine();

        return new Account(age, name, login, permission);
    }

    public static Object createStock(Scanner scanner) {
        tui("Enter stock type...\n\t" + "0: stock\n\t" + "1: drug");

        Stock stock;
        int input = scanner.nextInt();
        scanner.nextLine();

        StockType type;
        switch (input) {
            case 0:
                type = StockType.Stock;
                break;
            case 1:
                type = StockType.Drug;
                break;
            default:
                Log.error("Invalid stock type.");
                return Response.BadRequest;
        }

        tui("Enter item quantity:");
        int quantity = scanner.nextInt();
        scanner.nextLine();

        tui("Enter item price (double):");
        double price = scanner.nextDouble();
        scanner.nextLine();

        tui("Enter item name:");
        String name = scanner.nextLine();

        tui("Would you like to create a discount? (true / false)");
        Discount discount;
        boolean is_discount = scanner.nextBoolean();
        if (is_discount) {
            discount = createDiscount(scanner);
        } else discount = null;

        switch (type) {
            case Stock:
                return new Stock(quantity, price, name, discount);
            case Drug:
                break;
        }
        tui("Is the item a controlled substance? (true / false)");
        boolean is_controlled = scanner.nextBoolean();
        scanner.nextLine();

        tui("Enter drug name:");
        String drug_name = scanner.nextLine();

        tui("Enter expiration date:");
        LocalDateTime expiration = date(scanner);

        return new Drug(quantity, price, name, discount, is_controlled, drug_name, expiration);
    }

    public static Object createDiscountUUID(Scanner scanner) {
        List<Object> data = new ArrayList<>();

        data.add(createDiscount(scanner));

        tui("Enter Stock ID to apply discount to:");
        data.add(UUID.fromString(scanner.nextLine()));

        return data;
    }

    public static Discount createDiscount(Scanner scanner) {
        tui("Enter discount type...\n\t" + "0: flat discount\n\t" + "1: percent discount");
        int input = scanner.nextInt();
        scanner.nextLine();

        DiscountType type;
        switch (input) {
            case 0:
                tui("Enter flat discount (double):");
                type = DiscountType.Discount;
                break;
            case 1:
                tui("Enter percent discount (double 0.0-1.0):");
                type = DiscountType.PercentDiscount;
                break;
            default:
                throw new IllegalArgumentException("Invalid discount type.");
        }
        double discount = scanner.nextDouble();
        scanner.nextLine();

        tui("Enter expiration date...");
        LocalDateTime expiration = date(scanner);

        switch (type) {
            case Discount:
                return new Discount(discount, expiration);
            case PercentDiscount:
                return new PercentDiscount(discount, expiration);
        }

        throw new IllegalArgumentException("Cannot instantiate Discount");
    }

    public static Customer createCustomer(Scanner scanner) {
        return null;
    }

    public static Order createOrder(Scanner scanner) {
        return null;
    }

    public static AutoOrder createAutoOrder(Scanner scanner) {
        return null;
    }

    public static String changePassword(Scanner scanner) {
        tui("Enter current password:");
        return scanner.nextLine();
    }
}

public class Main {
    private static void tui(String message) {
        Log.tui(message);
    }

    private static void requests() {
        tui("\nInvoke a request to the backend...");
        tui("\t-1: quit");
        for (Request request : Request.values()) {
            tui("\t" + request.ordinal() + ": " + request);
        }
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

    private static Object requestData(Scanner scanner, final Request request) {
        List<Object> data = new ArrayList<>();
        Object text = null;

        switch (request) {
            case Login:
                return TUI.login(scanner);
            case Logout:
                tui("Logging out...");
                return Response.LoggedOut;
            case CreateAccount:
                return TUI.createAccount(scanner);
            case CreateStock:
                return TUI.createStock(scanner);
            case CreateDiscount:
                return TUI.createDiscountUUID(scanner);
            case CreateCustomer:
                break;
            case CreateOrder:
                break;
            case CreateAutoOrder:
                break;
            case ChangePassword:
                return TUI.changePassword(scanner);
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

                return Response.Ok;
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
            case Ok:
                tui("200: OK.");
                return Response.Ok;
            case BadRequest:
                tui("400: Bad Request.");
                return Response.BadRequest;
            case Unauthorized:
                tui("401: Unauthorized");
                return Response.Unauthorized;
            case Forbidden:
                tui("403: Forbidden");
                return Response.Forbidden;
            case NotFound:
                tui("404: Not found.");
                return Response.NotFound;
        }
        return Response.NotFound;
    }

    private static void request(Scanner scanner, Backend backend, int input) {
        if (input > Request.values().length - 1 || input < 0) {
            tui("Invalid request.");
            return;
        }

        Request request = Request.values()[input];
        Object data = requestData(scanner, request);
        Log.trace("DATA: " + data);

        Response response = backend.receive(request, data);

        data = responseData(scanner, backend, response);
        backend.send(response, data);
    }

    public static void main(String[] args) {
        Backend backend = new Backend();
        tui("Welcome to the Pharmacy Management System.");

        Scanner scanner = new Scanner(System.in);

        requests();
        int request = scanner.nextInt();
        scanner.nextLine();

        while (request != -1) {
            try {
                request(scanner, backend, request);

                requests();
                request = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                Log.error("Exception in Main: " + e.getMessage());
                e.printStackTrace();
            }
        }

        scanner.close();

        // InventoryControl inventory_control = new InventoryControl();
        //
        // LocalDateTime test_date = LocalDateTime.of(2024, 11, 8, 0, 0);
        // Discount test_discount = new PercentDiscount(0.50);
        // Drug test_prescription = new Drug(55, 2, 33.12, "Test Pres", test_discount, true,
        // "Prestest",
        // test_date);
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
