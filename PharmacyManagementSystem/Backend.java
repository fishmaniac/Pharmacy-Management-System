package PharmacyManagementSystem;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import com.sun.net.httpserver.HttpServer;

/** {@link Backend}
 * 
 */
public class Backend {
    private HashMap<String, Account> accounts;
    private List<Patient> patients;
    private Account logged_in;

    /**
     * @param request
     * @param data
     */
    // Would be from front end (maybe use CLI input)
    public void receive(final Request request, final String data)
    {
	switch(request)
	{
	    case Login:
		login(data);
		break;
	    case Password:
		break;
	    case CreateAccount:
		createAccount(data);
		break;
	}
    }
    /**
     * @param response
     * @return
     */
    public String send(final Response response)
    {
	switch (response)
	{
	    case CreatePassword:
		return "Password123";
	    case GetPassword:
		return "WrongPassword";
	    default:
		return null;
	}
    }
    /**
     * @param data
     */
    public void createAccount(final String data)
    {
	// TODO: Use data for account info
	final Account account = new Account(-999, 55, "testname", "testlogin");
	accounts.put("testlogin", account);
    }
    /**
     * @param login
     * @return
     */
    public boolean login(final String login)
    {
	if (accounts.containsKey(login))
	{
	    final Account account = accounts.get(login);
	    if (account.isFirstLogin())
	    {
		account.setPassword(createPassword());
	    }
	    else if (getPassword(account))
	    {
		this.logged_in = account;
		account.setFailureAttempts(0);
		return true;
	    }
	    else
	    {
		account.setFailureAttempts(
			account.getFailureAttempts() + 1
			);
		// TODO: Lock out account if > 5
	    }
	}
	return false;
    }
    /**
     * @return
     */
    public String createPassword()
    {
	return send(Response.CreatePassword);
    }
    /**
     * @param account
     * @return
     */
    public boolean getPassword(final Account account)
    {
	return send(Response.GetPassword) == account.getPassword();
    }
}

enum Request {
    Login,
    Password,
    CreateAccount,
}

enum Response {
    CreatePassword,
    GetPassword,
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
    Account(final int id, final int age, final String name, final String login)
    {
	this.id = id;
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
