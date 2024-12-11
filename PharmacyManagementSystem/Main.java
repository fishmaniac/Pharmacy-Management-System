package PharmacyManagementSystem;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

class TUI {
    private static void tui(String message) {
        Log.tui(message);
    }

    private static LocalDateTime date(Scanner scanner) {
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

        switch (type) {
            case FlatDiscount:
                tui("Enter discount (double):");
                break;
            case PercentDiscount:
                tui("Enter discount (double 0.0-1.0):");
                break;
        }
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
        tui("Enter stock type...");
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

        data.add(new Prescription(prescription_items, Duration.ofDays(days)));

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

            tui("Enter the minimum stock to initiate the auto order:");
            int minimum_quantity = scanner.nextInt();
            scanner.nextLine();

            MinStock min_stock = new MinStock(minimum_quantity, item.getQuantity());
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
        tui("Enter stock ID to remove:");
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
        tui("Enter auto order ID to remove:");
        return scanner.nextLine();
    }

    public static int removeNotification(Scanner scanner) {
        tui("Enter the notification index to remove:");
        int index = scanner.nextInt();
        scanner.nextLine();
        return index;
    }

    public static String unlockAccount(Scanner scanner) {
        tui("Enter account ID to unlock:");
        return scanner.nextLine();
    }

    private static Object getField(Scanner scanner, Object field) {
        if (field.equals(String.class)) {
            return scanner.nextLine();
        } else if (field.equals(LocalDateTime.class)) {
            return date(scanner);
        } else if (field.equals(Boolean.class)) {
            tui("Enter a boolean (true / false):");
            boolean val = scanner.nextBoolean();
            scanner.nextLine();
            return Boolean.valueOf(val);
        } else if (field.equals(Integer.class)) {
            int val = scanner.nextInt();
            scanner.nextLine();
            return Integer.valueOf(val);
        } else if (field.equals(Double.class)) {
            tui("Enter a double:");

            double val = scanner.nextDouble();
            scanner.nextLine();
            return Double.valueOf(val);
        } else if (field.equals(PermissionLevel.class)) {
            return enumOption(scanner, PermissionLevel.class);
        } else if (field.equals(Discount.class)) {
            return createDiscount(scanner);
        } else {
            Log.error("Unknown field: " + field.getClass());
            return null;
        }
    }

    private static Object updateField(Scanner scanner, Object field, String name) {
        tui("Would you like to update " + name + "? (true / false)");
        boolean update = scanner.nextBoolean();
        scanner.nextLine();
        tui("Enter " + name + ":");
        if (update) return getField(scanner, field);

        return null;
    }

    public static List<Object> updateAccount(Scanner scanner) {
        tui("Enter account ID to update:");
        UUID id = UUID.fromString(scanner.nextLine());

        LocalDateTime date = (LocalDateTime) updateField(scanner, LocalDateTime.class, "birthday");
        String name = (String) updateField(scanner, String.class, "name");
        String login = (String) updateField(scanner, String.class, "login");
        PermissionLevel permissions =
                (PermissionLevel) updateField(scanner, PermissionLevel.class, "permissions");

        List<Object> data = new ArrayList<>();
        data.add(id);
        data.add(date);
        data.add(name);
        data.add(login);
        data.add(permissions);

        return data;
    }

    public static List<Object> updateStock(Scanner scanner) {
        tui("Enter stock ID to update:");
        UUID id = UUID.fromString(scanner.nextLine());

        Integer quantity = (Integer) updateField(scanner, Integer.class, "quantity");
        Double price = (Double) updateField(scanner, Double.class, "price (double)");
        String name = (String) updateField(scanner, String.class, "name");
        Discount discount = (Discount) updateField(scanner, Discount.class, "discount");

        List<Object> data = new ArrayList<>();
        data.add(id);
        data.add(quantity);
        data.add(price);
        data.add(name);
        data.add(discount);

        return data;
    }

    public static List<Object> updateDrug(Scanner scanner) {
        tui("Enter drug ID to update:");
        UUID id = UUID.fromString(scanner.nextLine());

        Integer quantity = (Integer) updateField(scanner, Integer.class, "quantity");
        Double price = (Double) updateField(scanner, Double.class, "price (double)");
        String name = (String) updateField(scanner, String.class, "name");
        Discount discount = (Discount) updateField(scanner, Discount.class, "discount");
        Boolean is_controlled = (Boolean) updateField(scanner, Boolean.class, "is_controlled");
        String drug_name = (String) updateField(scanner, String.class, "drug name");
        LocalDateTime expiration_date =
                (LocalDateTime) updateField(scanner, LocalDateTime.class, "expiration date");

        List<Object> data = new ArrayList<>();
        data.add(id);
        data.add(quantity);
        data.add(price);
        data.add(name);
        data.add(discount);
        data.add(is_controlled);
        data.add(drug_name);
        data.add(expiration_date);

        return data;
    }

    public static List<Object> updateCustomer(Scanner scanner) {
        tui("Enter customer ID to update:");
        UUID id = UUID.fromString(scanner.nextLine());

        LocalDateTime birthday =
                (LocalDateTime) updateField(scanner, LocalDateTime.class, "birthday");
        String name = (String) updateField(scanner, String.class, "name");
        LocalDateTime last_access =
                (LocalDateTime) updateField(scanner, LocalDateTime.class, "last access");

        List<Object> data = new ArrayList<>();
        data.add(id);
        data.add(birthday);
        data.add(name);
        data.add(last_access);

        return data;
    }

    public static List<Object> updateOrder(Scanner scanner) {
        tui("Enter order ID to update:");
        UUID id = UUID.fromString(scanner.nextLine());

        LocalDateTime shipment_date =
                (LocalDateTime) updateField(scanner, LocalDateTime.class, "shipment date");

        List<Object> data = new ArrayList<>();
        data.add(id);
        data.add(shipment_date);

        return data;
    }

    public static List<Object> purchaseStock(Scanner scanner) {
        List<Object> data = new ArrayList<>();

        tui("Enter customer ID:");
        data.add(UUID.fromString(scanner.nextLine()));

        tui("Enter the amount of items in the order:");
        int items = scanner.nextInt();
        scanner.nextLine();

        List<UUID> barcodes = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();
        for (int i = 0; i < items; i++) {
            tui("Enter item barcode (ID):");
            barcodes.add(UUID.fromString(scanner.nextLine()));
            tui("Enter the amount to purchase:");
            quantities.add(Integer.valueOf(scanner.nextInt()));
            scanner.nextLine();
        }
        data.add(barcodes);
        data.add(quantities);

        return data;
    }

    public static List<Object> pickupPrescription(Scanner scanner) {
        List<Object> data = new ArrayList<>();

        tui("Enter customer ID:");
        data.add(UUID.fromString(scanner.nextLine()));

        tui("Enter prescription ID:");
        data.add(UUID.fromString(scanner.nextLine()));

        return data;
    }
}

public class Main {
    private static void tui(String message) {
        Log.tui(message);
    }

    private static void requests() {
        tui("\tInvoke a request to the backend...");
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
        switch (request) {
            case Login:
                return TUI.login(scanner);
            case Logout:
                return Response.Ok;
            case CreateAccount:
                return TUI.createAccount(scanner);
            case CreateStock:
                return TUI.createStock(scanner);
            case CreateDiscount:
                return TUI.createDiscountUUID(scanner);
            case CreateCustomer:
                return TUI.createCustomer(scanner);
            case CreatePrescription:
                return TUI.createPrescription(scanner);
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
            case GetNotifications:
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
            case RemoveNotification:
                return TUI.removeNotification(scanner);
            case UnlockAccount:
                return TUI.unlockAccount(scanner);
            case UpdateAccount:
                return TUI.updateAccount(scanner);
            case UpdateStock:
                return TUI.updateStock(scanner);
            case UpdateDrug:
                return TUI.updateDrug(scanner);
            case UpdateCustomer:
                return TUI.updateCustomer(scanner);
            case UpdateOrder:
                return TUI.updateOrder(scanner);
            case PurchaseStock:
                return TUI.purchaseStock(scanner);
            case PickupPrescription:
                return TUI.pickupPrescription(scanner);
        }

        Log.error("Invalid request data.");
        return null;
    }

    private static Object responseData(Scanner scanner, Response response) {
        String text = null;

        switch (response) {
            case FirstLogin:
                tui("First login."); // Fall through
            case NewPassword:
                tui("Create a new password:");
                text = scanner.nextLine();

                tui("Confirm your password:");
                if (text.equals(scanner.nextLine())) {
                    tui("Password updated. Please relogin now.");
                    return text;
                } else tui("Password did not match.");

                return Response.Ok;
            case GetPassword:
                tui("Enter your account password:");
                return scanner.nextLine();
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

    private static void request(Scanner scanner, API api, int input) {
        if (input > Request.values().length - 1 || input < 0) {
            throw new IllegalArgumentException("Invalid request.");
        }

        Request request = Request.values()[input];
        Object data = requestData(scanner, request);
        Log.trace("DATA: " + data);

        Response response = api.receive(request, data);
        Log.audit("Request " + request + " made with data: " + data);

        data = responseData(scanner, response);
        Log.audit("Response " + response + " received with data: " + data);

        api.send(response, data);
    }

    int input(Scanner scanner) {
        while (true) {
            try {
                requests();
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                tui("Invalid request.");
                scanner.nextLine();
            } finally {
                scanner.nextLine();
            }
        }
    }

    public static void main(String[] args) {
        Backend backend = Backend.get();
        API api = new API();
        tui("Welcome to the Pharmacy Management System.");

        Scanner scanner = new Scanner(System.in);

        requests();
        // Must be valid or system will shutdown.
        int request = scanner.nextInt();
        scanner.nextLine();

        while (request != -1) {
            try {
                backend.update();
                request(scanner, api, request);
            } catch (Exception e) {
                Log.error("Exception in Main: " + e.getMessage());
                e.printStackTrace();
            } finally {
                while (true) {
                    try {
                        requests();
                        request = scanner.nextInt();
                        scanner.nextLine();
                        break;
                    } catch (InputMismatchException e) {
                        tui("Invalid request.");
                        scanner.nextLine();
                    }
                }
            }
        }

        scanner.close();
    }
}
