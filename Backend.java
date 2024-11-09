import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import com.sun.net.httpserver.HttpServer;

enum Request {
    Login,
    Password,
    CreateAccount,
}

enum Response {
    CreatePassword,
    GetPassword,
}

public class Backend {
    private HashMap<String, Account> accounts;
    private List<Patient> patients;
    private Account logged_in;

    public void receive(Request request, String data) {
	switch(request) {
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
    // Would be from front end (maybe use CLI input)
    public String send(Response response) {
	switch (response) {
	    case CreatePassword:
		return "Password123";
	    case GetPassword:
		return "WrongPassword";
	    default:
		return null;
	}
    }
    public void createAccount(String data) {
	// TODO: Use data for account info
	Account account = new Account(-999, 55, "testname", "testlogin");
	accounts.put("testlogin", account);
    }
    public boolean login(String login) {
	if (accounts.containsKey(login)) {
	    Account account = accounts.get(login);
	    if (account.isFirstLogin()) {
		account.setPassword(createPassword());
	    }
	    else if (getPassword(account)) {
		this.logged_in = account;
		account.setFailureAttempts(0);
		return true;
	    }
	    else {
		account.setFailureAttempts(
			account.getFailureAttempts() + 1
			);
		// TODO: Lock out account if > 5
	    }
	}
	return false;
    }
    public String createPassword() {
	return send(Response.CreatePassword);
    }
    public boolean getPassword(Account account) {
	return send(Response.GetPassword) == account.getPassword();
    }
}

class Patient {
    private int id;
    private int age;
    private String name;
    private LocalDate last_access;

    public int getId() {
	return id;
    }
    public void setId(int id) {
	this.id = id;
    }
    public int getAge() {
	return age;
    }
    public void setAge(int age) {
	this.age = age;
    }
    public String getName() {
	return name;
    }
    public void setName(String name) {
	this.name = name;
    }
}

class Account {
    private int id;
    private int age;
    private int failure_attempts;
    private String name;
    private String login;
    private String password;
    private boolean first_login;
    private boolean locked;

    Account(int id, int age, String name, String login) {
	this.id = id;
	this.age = age;
	this.name = name;
	this.login = login;
	// Create password on first login
	this.password = null;
	this.first_login = true;
    }

    public int getId() {
	return id;
    }
    public void setId(int id) {
	this.id = id;
    }
    public int getAge() {
	return age;
    }
    public void setAge(int age) {
	this.age = age;
    }
    public String getName() {
	return name;
    }
    public void setName(String name) {
	this.name = name;
    }
    public String getLogin() {
	return login;
    }
    public void setLogin(String login) {
	this.login = login;
    }
    public String getPassword() {
	return password;
    }
    public void setPassword(String password) {
	this.password = password;
    }
    public boolean isFirstLogin() {
	return first_login;
    }
    public void setFirstLogin(boolean first_login) {
	this.first_login = first_login;
    }
    public int getFailureAttempts() {
	return failure_attempts;
    }
    public void setFailureAttempts(int failure_attempts) {
	this.failure_attempts = failure_attempts;
    }
    public boolean isLocked() {
	return locked;
    }
    public void setLocked(boolean locked) {
	this.locked = locked;
    }
}
