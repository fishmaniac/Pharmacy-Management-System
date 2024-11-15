package PharmacyManagementSystem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** {@link InventoryControl}
 * 
 */
public class InventoryControl {
    // TODO: Make private
    public List<Stock> inventory;
    public List<Order> orders;
    public List<AutoOrder> auto_orders;

    public InventoryControl()
    {
	this.inventory = new ArrayList<>();
	this.orders = new ArrayList<>();
	this.auto_orders = new ArrayList<>();
    }

    // Getters/Setters
    public List<Stock> getInventory() {
	return inventory;
    }
    public void setInventory(final List<Stock> inventory) {
	this.inventory = inventory;
    }
    public List<Order> getOrders() {
	return orders;
    }
    public void setOrders(final List<Order> orders) {
	this.orders = orders;
    }
    public List<AutoOrder> getAutoOrders() {
	return auto_orders;
    }
    public void setAutoOrders(final List<AutoOrder> auto_orders) {
	this.auto_orders = auto_orders;
    }

    // Backend Stock API
    /**
     * @param item
     */
    public void addStock(final Stock item)
    {
	this.inventory.add(item);
    }
    /**
     * @param item
     */
    public void removeStock(final Stock item)
    {
	this.inventory.remove(item);
    }

    // Backend Order API
    /**
     * @param order
     */
    public void addOrder(final Order order)
    {
	this.orders.add(order);
    }
    /**
     * @param order
     */
    public void removeOrder(final Order order)
    {
	this.orders.remove(order);
    }
    /**
     * @param order
     */
    public void deliverOrder(final Order order)
    {
	for (final Stock item : order.getOrderItems())
	{
	    addStock(item);
	}
    }
    /**
     * @param auto_order
     */
    public void addAutoOrder(final AutoOrder auto_order)
    {
	this.auto_orders.add(auto_order);
    }
    /**
     * @param auto_order
     */
    public void removeAutoOrder(final AutoOrder auto_order)
    {
	this.auto_orders.remove(auto_order);
    }
    /**
     * @param order_item
     * @param order_quantity
     */
    public void createUniqueOrder(final Stock order_item, final int order_quantity)
    {
	for (final Order order : this.orders)
	{
	    for (final Stock item : order.getOrderItems())
	    {
		if (item.getName() == order_item.getName())
		{
		    // Return early if the item is already ordered
		    return;
		}
	    }
	}

	order_item.setQuantity(order_quantity);//
					       // TODO: Update expiration date on new orders
	final Order new_order = new Order(
		-999,
		order_item,
		LocalDateTime.now()
		// .plusWeeks(1) // Delivery time here
		);
	System.out.println("New unique order: " + new_order);
	addOrder(new_order);
    }

    // Backend Updates API
    /**
     * 
     */
    public void updateAutoOrders()
    {
	for (final Stock item : this.inventory)
	{
	    for (final AutoOrder auto_order : this.auto_orders)
	    {
		if (
			item.getName().compareTo(auto_order.r_name()) == 0
			&& item.getQuantity() < auto_order.r_min_quantity()
		   )
		{
		    createUniqueOrder(item.clone(), auto_order.r_order_quantity());
		}
	    }
	}
    }
    /**
     * 
     */
    public void updateDeliveries()
    {
	final List<Order> removals = new ArrayList<>();
	for (final Order order : this.orders)
	{
	    if (order.getShipmentDate()
		    .isAfter(LocalDateTime.now())
		    || order.getShipmentDate()
		    .isEqual(LocalDateTime.now())
	       )
	    {
		deliverOrder(order);
		removals.add(order);
	    }
	}
	for (final Order order : removals)
	{
	    removeOrder(order);
	}
    }
    /**
     * 
     */
    public void updateExpired()
    {
	// TODO
    }
}

class Discount {
    protected double discount;

    /**
     * @param discount
     */
    Discount(final double discount) {
	this.discount = discount;
    }
    /**
     * @param price
     * @return
     */
    public double getDiscount(final double price) {
	if (price < discount) return 0;

    	return price - discount;
    }
}

class PercentDiscount extends Discount {
    /**
     * Constructs a {@code PercentDiscount} instance with the specified discount value.
     * 
     * The discount value should be a decimal between 0.0 and 1.0, inclusive. 
     * If the discount is greater than 1.0, an {@link IllegalArgumentException} will be thrown.
     * 
     * @param discount A decimal value representing the discount. Must be between 0.0 and 1.0 inclusive.
     * 
     * @throws IllegalArgumentException if the discount is greater than 1.0 or less than 0.0.
     */
    PercentDiscount(final double discount) {
	super(discount);
	if (discount > 1.0 || discount < 0.0) throw new IllegalArgumentException(
		"Invalid discount amount"
		);
    }
    /**
     * @param price
     * @return
     */
    public double getPercentDiscount(final double price) {
	return price * discount;
    }
}

class Stock implements Cloneable {
    // Data Members
    protected int id;
    protected int quantity;
    protected double price;
    protected String name;
    protected Discount discount;

    // Constructors
    /**
     * @param id
     * @param quantity
     * @param price
     * @param name
     * @param discount
     */
    public Stock(final int id, final int quantity, final double price, final String name, final Discount discount)
    {
	this.id = id;
	this.quantity = quantity;
	this.price = price;
	this.discount = discount;
	this.name = name;
    }

    // Getters/Setters
    public int getId() {
	return id;
    }
    public void setId(final int id) {
	this.id = id;
    }
    public int getQuantity()
    {
	return this.quantity;
    }
    public void setQuantity(final int quantity)
    {
	this.quantity = quantity;
    }
    // TODO: Use discount to get price
    public double getPrice()
    {
	return this.price;
    }
    public void setPrice(final double price)
    {
	this.price = price;
    }
    public Discount getDiscount() {
	return discount;
    }
    public void setDiscount(final Discount discount) {
	this.discount = discount;
    }
    public String getName()
    {
	return this.name;
    }
    public void setName(final String name)
    {
	this.name = name;
    }

    // Override Methods
    @Override
    public Stock clone()
    {
	try {
	    return (Stock) super.clone();
	} catch (final CloneNotSupportedException e)
	{
	    throw new AssertionError("Clone not supposed for StockItemType");
	}
    }
    @Override
    public String toString()
    {
	return "[ID: " + this.id
	    + ", Quantity: " + this.quantity
	    + ", Price: " + this.price
	    + ", Name: " + this.name + "]";
    }
}

class Drug extends Stock {
    // Data Members
    private boolean is_controlled;
    private String drug_name;
    private LocalDateTime expiration_date;

    // Constructors
    /**
     * @param id
     * @param quantity
     * @param price
     * @param name
     * @param discount
     * @param is_controlled
     * @param drug_name
     * @param expiration_date
     */
    public Drug(final int id, final int quantity, final double price, final String name, final Discount discount,
	    final boolean is_controlled, final String drug_name, final LocalDateTime expiration_date)
    {
	super(id, quantity, price, name, discount);
	this.is_controlled = is_controlled;
	this.drug_name = drug_name;
	this.expiration_date = expiration_date;
    }

    // Getters/Setters
    public boolean getIsControlled()
    {
	return this.is_controlled;
    }
    public void setIsControlled(final boolean is_controlled)
    {
	this.is_controlled = is_controlled;
    }
    public String getDrugName()
    {
	return this.drug_name;
    }
    public void setDrugName(final String drug_name)
    {
	this.drug_name = drug_name;
    }
    public LocalDateTime getExpirationDate()
    {
	return this.expiration_date;
    }
    public void setExpirationDate(final LocalDateTime expiration_date)
    {
	this.expiration_date = expiration_date;
    }

    // Override Methods
    @Override
    public Drug clone()
    {
	return (Drug) super.clone();
    }
    @Override
    public String toString()
    {
	return super.toString() +
	    "-[Controlled: " + this.is_controlled
	    + ", Drug Name: " + this.drug_name
	    + ", Expiration Date: " + this.expiration_date + "]";
    }
}

record AutoOrder(
	int r_min_quantity,
	int r_order_quantity,
	String r_name
	){}

class Order {
    // Data Members
    private final int order_id;
    private final List<Stock> order_items;
    private final LocalDateTime shipment_date;

    // Constructors
    /**
     * @param order_id
     * @param order_items
     * @param shipment_date
     */
    public Order(final int order_id, final List<Stock> order_items, final LocalDateTime shipment_date)
    {
	this.order_id = order_id;
	this.order_items = order_items;
	this.shipment_date = shipment_date;
    }

    /**
     * @param order_id
     * @param order_item
     * @param shipment_date
     */
    public Order(final int order_id, final Stock order_item, final LocalDateTime shipment_date)
    {
	this.order_id = order_id;
	this.shipment_date = shipment_date;

	final List<Stock> order_items = new ArrayList<>();
	order_items.add(order_item);
	this.order_items = order_items;
    }

    // Getters/Setters
    public int getID()
    {
	return this.order_id;
    }
    public List<Stock> getOrderItems()
    {
	return this.order_items;
    }
    public LocalDateTime getShipmentDate()
    {
	return this.shipment_date;
    }

    public void addItem(final Stock item)
    {
	this.order_items.add(item);
    }

    // Override Methods
    @Override
    public String toString()
    {
	return "Order ID: " + this.order_id
	    + ", Shipment Date: " + this.shipment_date
	    + ", Items: " + this.order_items;
    }
}
