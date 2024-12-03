package PharmacyManagementSystem;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import com.sun.net.httpserver.HttpServer;

enum Request {
    Login,
    CreateAccount,
    CreateDiscount,
}

enum Response {
    AccountLocked,
    AccountCreated,
    AccountDoesNotExist,
    FirstLogin,
    LoginFailed,
    GetPassword,
    DiscountCreated,
}

enum Status {
    Success,
    Fail,
}

/** {@link Backend}
 * 
 */
public class Backend {
    private HashMap<String, Account> accounts;
    private List<Patient> patients;
    private Account logging_in;
    private Account logged_in;

    Backend()
    {
	this.accounts = new HashMap<String, Account>();
	this.patients = new ArrayList<Patient>();
    }
    //    private boolean checkData(final Request request, Object data) {
    // switch(request)
    // {
    //     case Login:
    // 	return data instanceof String;
    //     case CreateAccount:
    // 	return data instanceof List<?>;
    //     case CreateDiscount:
    // 	break;
    // }
    //    }
    /**
     * @param request
     * @param data
     */
    // Would be from front end (maybe use CLI input)
    @SuppressWarnings("unchecked")
    public Response receive(final Request request, Object data)
    {
	// TODO: Add status code
	switch(request)
	{
	    case Login:
		return login((String) data);
	    case CreateAccount:
		return createAccount((List<Object>) data);
	    case CreateDiscount:
		return createDiscount((List<Object>) data);
	}

	return null;
    }
    /**
     * @param response
     * @return
     */
    public boolean send(final Response response, Object data)
    {
	Account new_account = getLoggingIn();
	Account account = getLoggedIn();


	switch (response)
	{
	    case AccountLocked:
		return false;
	    case FirstLogin:
		new_account.setPassword((String) data);
		new_account.setFirstLogin(false);
		return true;
	    case GetPassword:
		if (new_account.getPassword().equals((String) data))
		{
		    setLoggedIn(new_account);
		    setLoggingIn(null);

		    Log.info("Logged in: " + new_account);

		    return true;
		}
		else
		{
		    loginFailed(new_account);
		    setLoggingIn(null);

		    Log.info("Login failed.");

		    return false;
		}
	}
	return false;
    }
    /**
     * @param data
     */
    public Response createAccount(final List<Object> data)
    {
	// TODO: Use data for account info
	final Account account = new Account(55, "testname", "testlogin");
	accounts.put("testlogin", account);
	Log.info("Account created: " + account);

	return Response.AccountCreated;
    }
    public Response createDiscount(final List<Object> data)
    {
	final Discount discount = new Discount(10);
	Log.info("Discount created: " + discount);

	return Response.DiscountCreated;
    }
    /**
     * @param login
     * @return
     */
    public void loginFailed(final Account account) {
	account.setFailureAttempts(account.getFailureAttempts() + 1);
	if(account.getFailureAttempts() > 5)
	{
	    // TODO: Lock Out Account
	    //	    - Disable login
	    //	    - Require the account to be reset by an administrator
	    //	    - Add in functionality to reset account
	    account.setFailureAttempts(0); // lock the account
	    account.setLocked(true);
	    Log.tui("Account has been locked due to many attempts.");
	}
	else
	{
	    Log.tui("Failed attempts: " + account.getFailureAttempts());
	}

    }
    public Response login(final String login)
    {
	if (!accounts.containsKey(login)) return Response.AccountDoesNotExist;

	final Account account = accounts.get(login);
	if (account.isLocked())
	{
	    // Do not login
	    return Response.AccountLocked;
	}
	else if (account.isFirstLogin())
	{
	    setLoggingIn(account);
	    // account.setPassword(createPassword());
	    // Force relogin with newly created password
	    return Response.FirstLogin;
	}
	else
	{
	    setLoggingIn(account);
	    account.setFailureAttempts(0);
	    return Response.GetPassword;
	}
	// else
	// {
	//     loginFailed(account);
	//     return Response.LoginFailed;
	// }
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

class Patient {
    private int id;
    private int age;
    private String name;
    private LocalDateTime last_access;
    private List<Prescription> prescriptions;

    // Getters/Setters
    public int getId()
    {
	return id;
    }
    public void setId(final int id)
    {
	this.id = id;
    }
    public int getAge()
    {
	return age;
    }
    public void setAge(final int age)
    {
	this.age = age;
    }
    public String getName()
    {
	return name;
    }
    public void setName(final String name)
    {
	this.name = name;
    }
    public LocalDateTime getLastAccess() {
	return last_access;
    }
    public void setLastAccess() {
	this.last_access = LocalDateTime.now();
    }
    public List<Prescription> getPrescriptions() {
	return prescriptions;
    }
    public void setPrescriptions(final List<Prescription> prescriptions) {
	this.prescriptions = prescriptions;
    }
}

class Purchase {
    // Data Members
    private int id;
    private LocalDateTime purchase_date;
    private List<Stock> items;

    // Getters/Setters
    public int getId() {
	return id;
    }
    public void setId(final int id) {
	this.id = id;
    }
    public LocalDateTime getPurchase_date() {
	return purchase_date;
    }
    public void setPurchase_date(final LocalDateTime purchase_date) {
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
    Pharmacist,
    PharmacyManager,
    Admin,
}

class Account {
    // Data Members
    private boolean first_login;
    private boolean locked;
    private int id;
    private int age;
    private int failure_attempts;
    private String name;
    private String login;
    private String password;
    private List<Purchase> purchases;

    // Constructors
    /**
     * @param id
     * @param age
     * @param name
     * @param login
     */
    Account(final int age, final String name, final String login)
    {
	// TODO: Assign global ID to everything
	this.id = -1;
	// TODO: Change to birthday..
	this.age = age;
	this.name = name;
	this.login = login;
	// Create password on first login
	this.password = null;
	this.first_login = true;
    }

    // Getters/Setters
    public int getId()
    {
	return id;
    }
    public void setId(final int id)
    {
	this.id = id;
    }
    public int getAge()
    {
	return age;
    }
    public void setAge(final int age)
    {
	this.age = age;
    }
    public String getName()
    {
	return name;
    }
    public void setName(final String name)
    {
	this.name = name;
    }
    public String getLogin()
    {
	return login;
    }
    public void setLogin(final String login)
    {
	this.login = login;
    }
    public String getPassword()
    {
	return password;
    }
    public void setPassword(final String password)
    {
	this.password = password;
    }
    public boolean isFirstLogin()
    {
	return first_login;
    }
    public void setFirstLogin(final boolean first_login)
    {
	this.first_login = first_login;
    }
    public int getFailureAttempts()
    {
	return failure_attempts;
    }
    public void setFailureAttempts(final int failure_attempts)
    {
	this.failure_attempts = failure_attempts;
    }
    public boolean isLocked()
    {
	return locked;
    }
    public void setLocked(final boolean locked)
    {
	this.locked = locked;
    }
    public List<Purchase> getPurchases() {
	return purchases;
    }
    public void setPurchases(final List<Purchase> purchases) {
	this.purchases = purchases;
    }
}
