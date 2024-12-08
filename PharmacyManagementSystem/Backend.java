package PharmacyManagementSystem;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

enum Request {
    Login,
    Logout,
    CreateAccount,
    CreateStock,
    CreateDiscount,
    CreateCustomer,
    CreatePrescription,
    CreateOrder,
    CreateAutoOrder,
    ChangePassword,
    GetAccounts,
    GetInventory,
    GetCustomers,
    GetOrders,
    GetAutoOrders,
    RemoveAccount,
    RemoveDiscount,
    RemoveStock,
    RemoveCustomer,
    RemoveOrder,
    RemoveAutoOrder,
    UnlockAccount,
}

enum Response {
    FirstLogin,
    GetPassword,
    NewPassword,
    Ok,
    BadRequest,
    Unauthorized,
    Forbidden,
    NotFound,
}

enum CustomerType {
    Customer,
    Patient,
}

enum DiscountType {
    FlatDiscount,
    PercentDiscount,
}

enum Status {
    Success,
    Fail,
}

/** {@link Backend} */
public class Backend {
    private InventoryControl inventory;
    private HashMap<UUID, Account> accounts;
    private HashMap<UUID, Customer> customers;
    private Account logging_in;
    private Account logged_in;

    Backend() {
        this.inventory = new InventoryControl();
        this.accounts = new HashMap<UUID, Account>();
        this.customers = new HashMap<UUID, Customer>();

        initAdmin();
    }


    public void update() {
        this.inventory.updateAutoOrders();
        this.inventory.updateDeliveries();
        this.inventory.updateExpired();
        updateCustomers();
    }

    /**
     * @param request
     * @param data
     */
    // Would be from front end (maybe use CLI input)
    public Response receive(final Request request, Object data) {
        switch (request) {
            case Login:
                return checkLocked((String) data);
            case Logout:
                return logout();
            case CreateAccount:
                if (!auth(PermissionLevel.Admin)) return Response.Forbidden;
                return createAccount((Account) data);
            case CreateStock:
                if (!auth(PermissionLevel.PharmacyManager)) return Response.Forbidden;
                this.inventory.addStock((Stock) data);

                return Response.Ok;
            case CreateDiscount:
                if (!auth(PermissionLevel.PharmacyManager)) return Response.Forbidden;
                return createDiscount((List<Object>) data);
            case CreateCustomer:
                if (!auth(PermissionLevel.Cashier)) return Response.Forbidden;
                return createCustomer((Customer) data);
            case CreatePrescription:
                if (!auth(PermissionLevel.Pharmacist)) return Response.Forbidden;
                return createPrescription((List<Object>) data);
            case CreateOrder:
                if (!auth(PermissionLevel.PharmacyManager)) return Response.Forbidden;
                return createOrder((Order) data);
            case CreateAutoOrder:
                if (!auth(PermissionLevel.PharmacyManager)) return Response.Forbidden;
                return createAutoOrder((AutoOrder) data);
            case ChangePassword:
                if (this.logged_in == null) return Response.Unauthorized;
                return changePassword((String) data);
            case GetInventory:
                if (!auth(PermissionLevel.PharmacyTechnician)) return Response.Forbidden;

                Log.tui("Inventory: " + this.inventory.getStock());
                return Response.Ok;
            case GetOrders:
                if (!auth(PermissionLevel.PharmacyTechnician)) return Response.Forbidden;

                Log.tui("Orders: " + this.inventory.getOrders());
                return Response.Ok;
            case GetAutoOrders:
                if (!auth(PermissionLevel.PharmacyTechnician)) return Response.Forbidden;

                Log.tui("Auto Orders: " + this.inventory.getAutoOrders());
                return Response.Ok;
            case GetAccounts:
                if (!auth(PermissionLevel.PharmacyTechnician)) return Response.Forbidden;

                Log.tui("Accounts: " + this.accounts);
                return Response.Ok;
            case GetCustomers:
                if (!auth(PermissionLevel.PharmacyTechnician)) return Response.Forbidden;

                Log.tui("Patients: " + this.customers);
                return Response.Ok;
            case RemoveAccount:
                if (!auth(PermissionLevel.Admin)) return Response.Forbidden;
                return removeAccount((String) data);
            case RemoveDiscount:
                if (!auth(PermissionLevel.PharmacyManager)) return Response.Forbidden;
                return removeDiscount((String) data);
            case RemoveStock:
                if (!auth(PermissionLevel.PharmacyManager)) return Response.Forbidden;
                return removeStock((String) data);
            case RemoveCustomer:
                if (!auth(PermissionLevel.PharmacyManager)) return Response.Forbidden;
                return removeCustomer((String) data);
            case RemoveOrder:
                if (!auth(PermissionLevel.PharmacyManager)) return Response.Forbidden;
                return removeOrder((String) data);
            case RemoveAutoOrder:
                if (!auth(PermissionLevel.PharmacyManager)) return Response.Forbidden;
                return removeAutoOrder((String) data);
            case UnlockAccount:
                if (!auth(PermissionLevel.Admin)) return Response.Forbidden;
                return unlockAccount((String) data);
        }

        return Response.NotFound;
    }

    /**
     * @param response
     * @param data
     */
    public void send(final Response response, Object data) {
        Account logging_in = getLoggingIn();
        Account logged_in = getLoggedIn();

        if (data == null) {
            Log.error("Null response data: " + response);
            return;
        }

        switch (response) {
            case FirstLogin:
                logging_in.setPassword((String) data);
                logging_in.setFirstLogin(false);
                break;
            case GetPassword:
                login((String) data);
                break;
            case NewPassword:
                logged_in.setPassword((String) data);
                logout();
        }
    }

    private void updateCustomers() {
        for (UUID id : this.customers.keySet()) {
            if (
                this.customers.get(id).last_access.isBefore(
                    LocalDateTime.now().minusYears(5)
                )
            ) {
                this.customers.remove(id);
            }
        }
    }

    private void initAdmin() {
        Account admin =
                new Account(
                        LocalDateTime.of(1970, 1, 1, 0, 0),
                        "Admin",
                        "admin",
                        PermissionLevel.Admin);
        admin.setFirstLogin(false);
        admin.setPassword("admin");

        this.accounts.put(admin.getLogin(), admin);
    }

    private boolean auth(PermissionLevel required) {
        Account account = getLoggedIn();

        return account != null && account.getPermissions().ordinal() >= required.ordinal();
    }

    /**
     * @param account
     */
    private Response createAccount(final Account account) {
        if (this.accounts.containsKey(account.getLogin())) return Response.Forbidden;
        this.accounts.put(account.getLogin(), account);
        Log.tui("Account created: " + account);

        return Response.Ok;
    }

    private Response createDiscount(final List<Object> data) {
        Discount discount = (Discount) data.get(0);

        Stock item = this.inventory.getStock().get((UUID) data.get(1));
        if (item == null) {
            Log.tui("Discount creation failed.");
            return Response.NotFound;
        }

        item.setDiscount(discount);
        Log.tui(discount.getClass().getName() + " created: " + discount);

        return Response.Ok;
    }

    private Response createCustomer(final Customer customer) {
        if (customer == null) return Response.BadRequest;
        this.customers.put(customer.getID(), customer);

        return Response.Ok;
    }

    private Response createPrescription(final List<Object> data) {
        if (data == null) return Response.BadRequest;

        Patient customer = (Patient) this.customers.get(data.get(0));
        if (customer == null) return Response.BadRequest;
        customer.addPrescription((Prescription) data.get(1));

        return Response.Ok;
    }

    private Response createOrder(final Order order) {
        if (order == null) return Response.BadRequest;
        this.inventory.addOrder(order);

        return Response.Ok;
    }

    private Response createAutoOrder(final AutoOrder order) {
        if (order == null) return Response.BadRequest;
        this.inventory.addAutoOrder(order);

        return Response.Ok;
    }

    private Response changePassword(final String data) {
        if (this.getLoggedIn().getPassword().equals((String) data)) {
            return Response.NewPassword;
        } else return Response.Forbidden;
    }

    private Response removeAccount(final String data) {
        UUID id = UUID.fromString(data);
        if (id.equals(this.getLoggedIn().getID())) {
            Log.error("Cannot delete current account.");
            return Response.Forbidden;
        }
        if (this.accounts.remove(id) != null) return Response.Ok;
        else return Response.NotFound;
    }

    private Response removeDiscount(final String data) {
        Stock stock = this.inventory.getStock().get(UUID.fromString(data));
        if (stock == null) return Response.NotFound;

        stock.setDiscount(null);
        return Response.Ok;
    }

    private Response removeStock(final String data) {
        if (this.inventory.getStock().remove(UUID.fromString(data)) != null) return Response.Ok;
        else return Response.NotFound;
    }

    private Response removeCustomer(final String data) {
        if (this.customers.remove(UUID.fromString(data)) != null) return Response.Ok;
        else return Response.NotFound;
    }

    private Response removeOrder(final String data) {
        if (this.accounts.remove(UUID.fromString(data)) != null) return Response.Ok;
        else return Response.NotFound;
    }

    private Response removeAutoOrder(final String data) {
        if (this.accounts.remove(UUID.fromString(data)) != null) return Response.Ok;
        else return Response.NotFound;
    }

    private Response unlockAccount(final String data) {
        Account account = this.accounts.get(UUID.fromString(data));
        if (account == null) return Response.NotFound;
        if (account.isLocked()) {
            account.setLocked(false);
            return Response.Ok;
        } else return Response.BadRequest;
    }

    /**
     * @param account
     */
    private void loginFailed(Account account) {
        account.setFailureAttempts(account.getFailureAttempts() + 1);
        if (account.getFailureAttempts() >= 5) {
            account.setFailureAttempts(0); // lock the account
            account.setLocked(true);
            Log.tui("Account has been locked due to many attempts.");
        } else {
            Log.tui("Failed attempts: " + account.getFailureAttempts());
        }
    }

    /**
     * @param login
     * @return
     */
    private Response checkLocked(final String login) {
        UUID key = UUID.nameUUIDFromBytes(login.getBytes());
        if (!accounts.containsKey(key)) {
            Log.error("Account does not exist.");
            return Response.NotFound;
        }
        if (getLoggedIn() != null) {
            Log.error("A user is already logged into the system.");
            return Response.BadRequest;
        }

        Account account = accounts.get(key);
        if (account.isLocked()) {
            // Do not login
            Log.info("Account is locked. It must be unlocked by an admin.");
            setLoggingIn(null);
            return Response.Forbidden;
        } else if (account.isFirstLogin()) {
            setLoggingIn(account);
            // Force relogin with newly created password
            return Response.FirstLogin;
        } else {
            setLoggingIn(account);
            return Response.GetPassword;
        }
    }

    private void login(String data) {
        UUID key = UUID.nameUUIDFromBytes(data.getBytes());
        if (this.logging_in.getPassword().equals(key)) {
            setLoggedIn(this.logging_in);
            setLoggingIn(null);
            this.logged_in.setFailureAttempts(0);

            Log.info(
                    "Welcome "
                    + this.logged_in.getName()
                    + ". You are "
                    + this.logged_in.getAge()
                    + " years old.");
        } else {
            loginFailed(this.logging_in);
            setLoggingIn(null);

            Log.info("Login failed.");
        }
    }

    private Response logout() {
        this.setLoggedIn(null);
        Log.info("Logged out.");

        return Response.Ok;
    }

    private Account getLoggingIn() {
        return logging_in;
    }

    private void setLoggingIn(Account loggingIn) {
        this.logging_in = loggingIn;
    }

    private Account getLoggedIn() {
        return logged_in;
    }

    private void setLoggedIn(Account logged_in) {
        this.logged_in = logged_in;
    }
}

class Prescription {
    // Data Members
    private UUID id;
    private List<Stock> items;
    private LocalDateTime last_fill_time;
    private Duration refill_duration;

    Prescription(List<Stock> items, Duration refill_duration) {
        this.id = UUID.randomUUID();
        this.items = items;
        this.last_fill_time = null;
        this.refill_duration = refill_duration;
        Log.audit("Prescription created: " + this);
    }

    // Getters/Setters
    public List<Stock> getItems() {
        return items;
    }

    public void setItems(final List<Stock> items) {
        this.items = items;
    }

    public LocalDateTime getLastFillTime() {
        return last_fill_time;
    }

    public void setLastFillTime(final LocalDateTime last_fill) {
        this.last_fill_time = last_fill;
    }

    public Duration getRefillDuration() {
        return refill_duration;
    }

    public void setRefillDuration(final Duration refill_duration) {
        this.refill_duration = refill_duration;
    }
}

class Customer {
    protected UUID id;
    protected LocalDateTime birthday;
    protected String name;
    protected LocalDateTime last_access;

    Customer(LocalDateTime birthday, String name) {
        this.id = UUID.randomUUID();
        this.birthday = birthday;
        this.name = name;
        this.last_access = LocalDateTime.now();
        Log.audit("Customer created: " + this);
    }

    // Getters/Setters
    public UUID getID() {
        return id;
    }

    public void setID(final UUID id) {
        this.id = id;
    }

    public int getAge() {
        return Period.between(this.birthday.toLocalDate(), LocalDate.now()).getYears();
    }

    public LocalDateTime getBirthday() {
        return birthday;
    }

    public void setBirthday(final LocalDateTime birthday) {
        this.birthday = birthday;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public LocalDateTime getLastAccess() {
        return last_access;
    }

    public void setLastAccess(LocalDateTime time) {
        this.last_access = time;
    }
    public void setLastAccessNow() {
        this.last_access = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "ID: " + this.getID()
            + ", Birthday: " + this.getBirthday()
            + ", Name: " + this.getName()
            + ", Last access: " + this.getLastAccess();
    }
}

class Patient extends Customer {
    private List<Prescription> prescriptions;
    private List<Prescription> prescription_history;

    Patient(LocalDateTime birthday, String name) {
        super(birthday, name);
    }

    Patient(
            LocalDateTime birthday,
            String name,
            List<Prescription> prescriptions
           ) {
        super(birthday, name);
        this.prescriptions = prescriptions;
        this.prescription_history = this.prescriptions;
           }

    // Getters/Setters
    public List<Prescription> getPrescriptions() {
        return prescriptions;
    }

    public void addPrescription(Prescription prescription) {
        Log.audit("Prescription added to Patient " + this.getID());
        this.prescriptions.add(prescription);
        this.prescription_history.add(prescription);
    }

    @Override
    public String toString() {
        return super.toString() + ", Prescriptions: " + this.getPrescriptions();
    }
}

class Purchase {
    // Data Members
    private UUID id;
    private LocalDateTime purchase_date;
    private List<Stock> items;

    // Getters/Setters
    Purchase(List<Stock> items) {
        this.id = UUID.randomUUID();
        this.purchase_date = LocalDateTime.now();
        this.items = items;
        Log.audit("Purchase created: " + this);
    }

    Purchase(Stock item) {
        this.id = UUID.randomUUID();
        this.purchase_date = LocalDateTime.now();
        this.items = new ArrayList<>();
        this.items.add(item);
        Log.audit("Purchase created: " + this);
    }

    public UUID getID() {
        return id;
    }

    public void setID(final UUID id) {
        this.id = id;
    }

    public LocalDateTime getPurchaseDate() {
        return purchase_date;
    }

    public void setPurchaseDate(final LocalDateTime purchase_date) {
        this.purchase_date = purchase_date;
    }

    public List<Stock> getItems() {
        return items;
    }

    public void setItems(final List<Stock> items) {
        this.items = items;
    }
}

enum PermissionLevel {
    Cashier,
    PharmacyTechnician,
    Pharmacist,
    PharmacyManager,
    Admin,
}

class Account {
    // Data Members
    private boolean first_login;
    private boolean locked;
    private UUID id;
    private LocalDateTime birthday;
    private int failure_attempts;
    private String name;
    private UUID login;
    private UUID password;
    private List<Purchase> purchases;
    private PermissionLevel permissions;

    // Constructors
    /**
     * @param birthday
     * @param name
     * @param login
     * @param permissions
     */
    Account(
            final LocalDateTime birthday,
            final String name,
            final String login,
            final PermissionLevel permissions) {
        this.birthday = birthday;
        this.name = name;
        this.login = UUID.nameUUIDFromBytes(login.getBytes());
        this.id = this.login;
        this.permissions = permissions;
        // Create password on first login
        this.password = null;
        this.first_login = true;
        Log.audit("Account created: " + this);
            }

    // Getters/Setters
    public UUID getID() {
        return id;
    }

    public void setID(final UUID id) {
        this.id = id;
    }

    public int getAge() {
        return Period.between(this.birthday.toLocalDate(), LocalDate.now()).getYears();
    }

    public LocalDateTime getBirthday() {
        return birthday;
    }

    public void setBirthday(final LocalDateTime birthday) {
        this.birthday = birthday;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public UUID getLogin() {
        return login;
    }

    public void setLogin(final String login) {
        this.login = UUID.nameUUIDFromBytes(login.getBytes());
    }

    public UUID getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = UUID.nameUUIDFromBytes(password.getBytes());
    }

    public boolean isFirstLogin() {
        return first_login;
    }

    public void setFirstLogin(final boolean first_login) {
        this.first_login = first_login;
    }

    public int getFailureAttempts() {
        return failure_attempts;
    }

    public void setFailureAttempts(final int failure_attempts) {
        this.failure_attempts = failure_attempts;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(final boolean locked) {
        this.locked = locked;
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }

    public void setPurchases(final List<Purchase> purchases) {
        this.purchases = purchases;
    }

    public PermissionLevel getPermissions() {
        return permissions;
    }

    public void setPermissions(PermissionLevel permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return "ID: "
            + this.id
            + ", Birthday: "
            + this.birthday
            + ", Name: "
            + this.name
            + ", Login: "
            + this.login
            + ", Permissions: "
            + this.permissions;
    }
}
