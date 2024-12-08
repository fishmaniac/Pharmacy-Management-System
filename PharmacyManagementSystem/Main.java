package PharmacyManagementSystem;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
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

    public static <E extends Enum<E>> E enumOption(Scanner scanner, Class<E> type) {
        tui("Choose an option:");

        E[] list = type.getEnumConstants();
        for (E e : list) {
            tui(e.ordinal() + ": " + e);
        }

        int option = scanner.nextInt();
        scanner.nextLine();
        if (option > type.getEnumConstants().length - 1) {
            throw new IllegalArgumentException("Invalid option.");
        } else return list[option];
    }

    public static Discount createDiscount(Scanner scanner) {
        tui("Enter discount type...");
        DiscountType type = enumOption(scanner, DiscountType.class);

        double discount = scanner.nextDouble();
        scanner.nextLine();

        tui("Enter expiration date...");
        LocalDateTime expiration = date(scanner);

        switch (type) {
            case FlatDiscount:
                return new Discount(discount, expiration);
            case PercentDiscount:
                return new PercentDiscount(discount, expiration);
        }

        throw new IllegalArgumentException("Cannot instantiate Discount");
    }

    public static String login(Scanner scanner) {
        tui("Enter your username:");
        return scanner.nextLine();
    }

    public static Account createAccount(Scanner scanner) {
        tui("Enter account user's birthday:");
        LocalDateTime birthday = date(scanner);

        tui("Enter account user's full name:");
        String name = scanner.nextLine();

        tui("Enter account username:");
        String login = scanner.nextLine();

        tui("Choose account permission level...");
        PermissionLevel permission = enumOption(scanner, PermissionLevel.class);

        return new Account(birthday, name, login, permission);
    }

    public static Stock createStock(Scanner scanner) {
        tui("Enter stock type...\n\t" + "0: stock\n\t" + "1: drug");

        Stock stock;
        int input = scanner.nextInt();
        scanner.nextLine();

        StockType type = enumOption(scanner, StockType.class);

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

    public static List<Object> createDiscountUUID(Scanner scanner) {
        List<Object> data = new ArrayList<>();

        data.add(createDiscount(scanner));

        tui("Enter Stock ID to apply discount to:");
        data.add(UUID.fromString(scanner.nextLine()));

        return data;
    }

    public static Customer createCustomer(Scanner scanner) {
        tui("Enter customer type...");
        CustomerType type = enumOption(scanner, CustomerType.class);

        tui("Enter customer date of birth:");
        LocalDateTime birthday = date(scanner);

        tui("Enter customer name:");
        String name = scanner.nextLine();

        switch (type) {
            case Customer:
                return new Customer(birthday, name);
            case Patient:
                return new Patient(birthday, name);
        }
        throw new IllegalArgumentException("Unknown customer type.");
    }

    public static Object createPrescription(Scanner scanner) {
        List<Object> data = new ArrayList<Object>();
        tui("Enter patient ID to add prescription to:");
        data.add(scanner.nextLine());

        tui("Enter the amount of items in the prescription:");
        int items = scanner.nextInt();
        scanner.nextLine();

        List<Stock> prescription_items = new ArrayList<Stock>();
        for (int i = 0; i < items; i++) {
            prescription_items.add(createStock(scanner));
        }

        tui("Enter the time between refills in days:");
        int days = scanner.nextInt();
        scanner.nextLine();

        data.add(new Prescription(
            prescription_items,
            Duration.ofDays(days)
        ));

        return data;
    }

    public static Order createOrder(Scanner scanner) {
        tui("Enter the amount of items in the order:");
        int items = scanner.nextInt();
        scanner.nextLine();

        List<Stock> order_items = new ArrayList<Stock>();
        for (int i = 0; i < items; i++) {
            order_items.add(createStock(scanner));
        }

        return new Order(order_items);
    }

    public static AutoOrder createAutoOrder(Scanner scanner) {
        tui("Enter the amount of items in the auto order:");
        int items = scanner.nextInt();
        scanner.nextLine();

        List<Stock> order_items = new ArrayList<Stock>();
        HashMap<UUID, MinStock> quantities = new HashMap<UUID, MinStock>();
        for (int i = 0; i < items; i++) {
            Stock item = createStock(scanner);
            order_items.add(item);

            tui("Enter the quantity to order:");
            int order_quantity = scanner.nextInt();
            scanner.nextLine();

            tui("Enter the minimum stock to initiate the auto order:");
            int minimum_quantity = scanner.nextInt();
            scanner.nextLine();

            MinStock min_stock = new MinStock(minimum_quantity, order_quantity);
            quantities.put(item.getID(), min_stock);
        }
        Order order = new Order(order_items);
        return new AutoOrder(quantities, order);
    }

    public static String changePassword(Scanner scanner) {
        tui("Enter current password:");
        return scanner.nextLine();
    }

    public static String removeAccount(Scanner scanner) {
        tui("Enter account ID to remove:");
        return scanner.nextLine();
    }

    public static String removeDiscount(Scanner scanner) {
        tui("Enter stock ID to remove discount from:");
        return scanner.nextLine();
    }

    public static String removeStock(Scanner scanner) {
        tui("Enter drug ID to remove:");
        return scanner.nextLine();
    }

    public static String removeCustomer(Scanner scanner) {
        tui("Enter customer ID to remove:");
        return scanner.nextLine();
    }

    public static String removeOrder(Scanner scanner) {
        tui("Enter order ID to remove:");
        return scanner.nextLine();
    }

    public static String removeAutoOrder(Scanner scanner) {
        tui("Enter auto order ID to remove");
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
                return TUI.createCustomer(scanner);
            case CreateOrder:
                return TUI.createOrder(scanner);
            case CreateAutoOrder:
                return TUI.createAutoOrder(scanner);
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
                return TUI.removeAccount(scanner);
            case RemoveDiscount:
                return TUI.removeDiscount(scanner);
            case RemoveStock:
                return TUI.removeStock(scanner);
            case RemoveCustomer:
                return TUI.removeCustomer(scanner);
            case RemoveOrder:
                return TUI.removeOrder(scanner);
            case RemoveAutoOrder:
                return TUI.removeAutoOrder(scanner);
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
    }
}
