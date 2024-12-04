package PharmacyManagementSystem;

import java.time.Duration;
import java.time.LocalDateTime;
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
    RemoveDrug,
    RemoveCustomer,
    RemoveOrder,
    RemoveAutoOrder,
}

enum Response {
    AccountLocked,
    AccountCreated,
    AccountDoesNotExist,
    FirstLogin,
    LoginFailed,
    AlreadyLoggedIn,
    GetPassword,
    LoggedOut,
    DiscountCreated,
    DiscountFailed,
    NewPassword,
    Ok,
    BadRequest,
    Unauthorized,
    Forbidden,
    NotFound,
}

enum DiscountType {
    Discount,
    PercentDiscount,
}

enum Status {
    Success,
    Fail,
}

/** {@link Backend} */
public class Backend {
    private InventoryControl inventory;
    private HashMap<String, Account> accounts;
    private List<Customer> customers;
    private Account logging_in;
    private Account logged_in;

    Backend() {
        this.inventory = new InventoryControl();
        this.accounts = new HashMap<String, Account>();
        this.customers = new ArrayList<Customer>();

        Account admin = new Account(999, "Admin", "admin", PermissionLevel.Admin);
        this.accounts.put(admin.getLogin(), admin);
    }

    private boolean auth(PermissionLevel permissions) {
        Account account = getLoggedIn();

        return account == null
                ? false
                : account.getPermissions().ordinal() >= permissions.ordinal();
    }

    /**
     * @param request
     * @param data
     */
    // Would be from front end (maybe use CLI input)
    @SuppressWarnings("unchecked")
    public Response receive(final Request request, Object data) {
        this.inventory.updateAutoOrders();
        this.inventory.updateDeliveries();
        /* TODO: Update expired */
        this.inventory.updateExpired();
        switch (request) {
            case Login:
                return checkLocked((String) data);
            case Logout:
                return logout();
            case CreateAccount:
                if (!auth(PermissionLevel.Admin)) return Response.Forbidden;
                return createAccount((List<Object>) data);
            case CreateStock:
                if (!auth(PermissionLevel.Pharmacist)) return Response.Forbidden;
                this.inventory.addStock((Stock) data);

                return Response.Ok;
            case CreateDiscount:
                if (!auth(PermissionLevel.PharmacyManager)) return Response.Forbidden;
                return createDiscount((List<Object>) data);
            case CreateCustomer:
                break;
            case CreateOrder:
                break;
            case CreateAutoOrder:
                break;
            case ChangePassword:
                if (this.logged_in == null) return Response.Unauthorized;
                return changePassword((String) data);
            case GetInventory:
                if (!auth(PermissionLevel.PharmacyTechnician)) return Response.Forbidden;

                Log.info("Inventory: " + this.inventory.getInventory());
                return Response.Ok;
            case GetOrders:
                if (!auth(PermissionLevel.PharmacyTechnician)) return Response.Forbidden;

                Log.info("Orders: " + this.inventory.getOrders());
                return Response.Ok;
            case GetAutoOrders:
                if (!auth(PermissionLevel.PharmacyTechnician)) return Response.Forbidden;

                Log.info("Auto Orders: " + this.inventory.getAutoOrders());
                return Response.Ok;
            case GetAccounts:
                if (!auth(PermissionLevel.PharmacyTechnician)) return Response.Forbidden;

                Log.info("Accounts: " + this.accounts);
                return Response.Ok;
            case GetCustomers:
                if (!auth(PermissionLevel.PharmacyTechnician)) return Response.Forbidden;

                Log.info("Patients: " + this.customers);
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
            Log.warning("Null response data: " + response);
            return;
        }

        switch (response) {
            case AccountLocked:
                setLoggingIn(null);
                break;
            case AccountCreated:
                break;
            case AccountDoesNotExist:
                break;
            case FirstLogin:
                logging_in.setPassword((String) data);
                logging_in.setFirstLogin(false);
                break;
            case LoginFailed:
                break;
            case AlreadyLoggedIn:
                break;
            case GetPassword:
                login((String) data);
                break;
            case LoggedOut:
                break;
            case DiscountCreated:
                break;
            case DiscountFailed:
                break;
            case NewPassword:
                logged_in.setPassword((String) data);
                logout();
            case Unauthorized:
                break;
            case Forbidden:
                break;
            case NotFound:
                break;
        }
    }

    /**
     * @param data
     */
    public Response createAccount(Object data) {
        final Account account = (Account) data;

        this.accounts.put(account.getLogin(), account);
        Log.info("Account created: " + account);

        return Response.AccountCreated;
    }

    public Response createDiscount(final List<Object> data) {
        Discount discount = (Discount) data.get(0);

        Stock item = this.inventory.getInventory().get((UUID) data.get(1));

        if (item == null) {
            return Response.NotFound;
        }

        item.setDiscount(discount);
        Log.info(discount.getClass().getName() + " created: " + discount);

        return Response.DiscountCreated;
    }

    public Response changePassword(final String data) {
        if (this.getLoggedIn().getPassword().equals((String) data)) {
            return Response.NewPassword;
        } else return Response.Forbidden;
    }

    /**
     * @param account
     */
    public void loginFailed(Account account) {
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
        if (!accounts.containsKey(login)) return Response.AccountDoesNotExist;
        if (getLoggedIn() != null) return Response.AlreadyLoggedIn;

        Account account = accounts.get(login);
        if (account.isLocked()) {
            // Do not login
            return Response.AccountLocked;
        } else if (account.isFirstLogin()) {
            setLoggingIn(account);
            // account.setPassword(createPassword());
            // Force relogin with newly created password
            return Response.FirstLogin;
        } else {
            setLoggingIn(account);
            return Response.GetPassword;
        }
        // else
        // {
        //     loginFailed(account);
        //     return Response.LoginFailed;
        // }
    }

    private void login(String data) {
        if (this.logging_in.getPassword().equals((String) data)) {
            setLoggedIn(this.logging_in);
            setLoggingIn(null);
            this.logged_in.setFailureAttempts(0);

            Log.info("Logged in: " + this.logged_in);
        } else {
            loginFailed(this.logging_in);
            setLoggingIn(null);

            Log.info("Login failed.");
        }
    }

    private Response logout() {
        this.setLoggedIn(null);

        return Response.LoggedOut;
    }

    /**
     * @return
     */
    //    public String createPassword()
    //    {
    // return send(Response.FirstLogin);
    //    }
    //    /**
    //     * @param account
    //     * @return
    //     */
    //    public boolean getPassword(final Account account)
    //    {
    // return send(Response.GetPassword) == account.getPassword();
    //    }
    public Account getLoggingIn() {
        return logging_in;
    }

    public void setLoggingIn(Account loggingIn) {
        this.logging_in = loggingIn;
    }

    public Account getLoggedIn() {
        return logged_in;
    }

    public void setLoggedIn(Account logged_in) {
        this.logged_in = logged_in;
    }
}

class Prescription {
    // Data Members
    private List<Stock> items;
    private LocalDateTime last_fill_time;
    private Duration refill_duration;

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
    protected int id;
    protected int age;
    protected String name;
    protected LocalDateTime last_access;

    // Getters/Setters
    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(final int age) {
        this.age = age;
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

    public void setLastAccess() {
        this.last_access = LocalDateTime.now();
    }
}

class Patient extends Customer {
    private List<Prescription> prescriptions;

    // Getters/Setters
    public List<Prescription> getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(final List<Prescription> prescriptions) {
        this.prescriptions = prescriptions;
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
    }

    Purchase(Stock item) {
        this.id = UUID.randomUUID();
        this.purchase_date = LocalDateTime.now();
        this.items = new ArrayList<>();
        this.items.add(item);
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
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
    private int age;
    private int failure_attempts;
    private String name;
    private String login;
    private String password;
    private List<Purchase> purchases;
    private PermissionLevel permissions;

    // Constructors
    /**
     * @param age
     * @param name
     * @param login
     */
    Account(
            final int age,
            final String name,
            final String login,
            final PermissionLevel permissions) {
        // TODO: Assign global ID to everything
        this.id = UUID.randomUUID();
        // TODO: Change to birthday..
        this.age = age;
        this.name = name;
        this.login = login;
        this.permissions = permissions;
        // Create password on first login
        this.password = null;
        this.first_login = true;
    }

    // Getters/Setters
    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(final int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(final String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
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
                + ", Age: "
                + this.age
                + ", Name: "
                + this.name
                + ", Login: "
                + this.login
                + ", Permissions: "
                + this.permissions;
    }
}
