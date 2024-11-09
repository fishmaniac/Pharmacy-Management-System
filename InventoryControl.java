import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.time.LocalDate;

// enum OrderItemType {
//     EN_Prescription,
// }
//
// enum ItemType {
//     EN_LaysChips,
//     EN_Tylenol,
//     EN_Morphine,
// }

public class InventoryControl {
    // TODO: Make private
    public List<StockItemType> inventory;
    public List<Order> orders;
    public List<AutoOrder> auto_orders;

    public InventoryControl() {
	this.inventory = new ArrayList<>();
	this.orders = new ArrayList<>();
	this.auto_orders = new ArrayList<>();
    }

    // Backend Stock API
    public void addStock(StockItemType item) {
	this.inventory.add(item);
    }
    public void removeStock(StockItemType item) {
	this.inventory.remove(item);
    }

    // Backend Order API
    public void addOrder(Order order) {
	this.orders.add(order);
    }
    public void removeOrder(Order order) {
	this.orders.remove(order);
    }
    public void deliverOrder(Order order) {
	for (StockItemType item : order.getOrderItems()) {
	    addStock(item);
	}
    }
    public void addAutoOrder(AutoOrder auto_order) {
	this.auto_orders.add(auto_order);
    }
    public void removeAutoOrder(AutoOrder auto_order) {
	this.auto_orders.remove(auto_order);
    }
    public void createUniqueOrder(StockItemType order_item, int order_quantity) {
	for (Order order : this.orders) {
	    for (StockItemType item : order.getOrderItems()) {
		if (item.getName() == order_item.getName()) {
		    // Return early if the item is already ordered
		    return;
		}
	    }
	}

	order_item.setQuantity(order_quantity);//
	// TODO: Update expiration date on new orders
	Order new_order = new Order(
		-999,
		order_item,
		LocalDate.now()
		// .plusWeeks(1) // Delivery time here
		);
	System.out.println("New unique order: " + new_order);
	addOrder(new_order);
    }

    // Backend Updates API
    public void updateAutoOrders() {
	for (StockItemType item : this.inventory) {
	    for (AutoOrder auto_order : this.auto_orders) {
		if (
			item.getName().compareTo(auto_order.r_name()) == 0
			&& item.getQuantity() < auto_order.r_min_quantity()
		   ) {
		    createUniqueOrder(item.clone(), auto_order.r_order_quantity());
		   }
	    }
	}
    }
    public void updateDeliveries() {
	List<Order> removals = new ArrayList<>();
	for (Order order : this.orders) {
	    if (order.getShipmentDate()
		    .isAfter(LocalDate.now())
		    || order.getShipmentDate()
		    .isEqual(LocalDate.now())
	       ) {
		deliverOrder(order);
		removals.add(order);
	       }
	}
	for (Order order : removals) {
	    removeOrder(order);
	}
    }
    public void updateExpired() {
	// TODO
    }
}

class StockItemType implements Cloneable {
    protected int id;
    protected int quantity;
    protected double price;
    protected String name;

    public StockItemType(
	    int id,
	    int quantity,
	    double price,
	    String name
	    ) {
	this.id = id;
	this.quantity = quantity;
	this.price = price;
	this.name = name;
	    }

    public int getID() {
	return this.id;
    }
    public void setID(int id) {
	this.id = id;
    }
    public int getQuantity() {
	return this.quantity;
    }
    public void setQuantity(int quantity) {
	this.quantity = quantity;
    }
    public double getPrice() {
	return this.price;
    }
    public void setPrice(double price) {
	this.price = price;
    }
    public String getName() {
	return this.name;
    }
    public void setName(String name) {
	this.name = name;
    }

    @Override
    public StockItemType clone() {
	try {
	    return (StockItemType) super.clone();
	} catch (CloneNotSupportedException e) {
	    throw new AssertionError("Clone not supposed for StockItemType");
	}
    }
    @Override
    public String toString() {
	return "[ID: " + this.id
	    + ", Quantity: " + this.quantity
	    + ", Price: " + this.price
	    + ", Name: " + this.name + "]";
    }
}

class PrescriptionItemType extends StockItemType {
    private boolean is_controlled;
    private String drug_name;
    private LocalDate expiration_date;

    public PrescriptionItemType(
	    int id,
	    int quantity,
	    double price,
	    String name,
	    boolean is_controlled,
	    String drug_name,
	    LocalDate expiration_date
	    ) {
	super(id, quantity, price, name);
	this.is_controlled = is_controlled;
	this.drug_name = drug_name;
	this.expiration_date = expiration_date;
	    }

    public boolean getIsControlled() {
	return this.is_controlled;
    }
    public void setIsControlled(boolean is_controlled) {
	this.is_controlled = is_controlled;
    }
    public String getDrugName() {
	return this.drug_name;
    }
    public void setDrugName(String drug_name) {
	this.drug_name = drug_name;
    }
    public LocalDate getExpirationDate() {
	return this.expiration_date;
    }
    public void setExpirationDate(LocalDate expiration_date) {
	this.expiration_date = expiration_date;
    }

    @Override
    public PrescriptionItemType clone() {
	return (PrescriptionItemType) super.clone();
    }
    @Override
    public String toString() {
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
    private int order_id;
    private List<StockItemType> order_items;
    private LocalDate shipment_date;

    public Order(int order_id, List<StockItemType> order_items, LocalDate shipment_date) {
	this.order_id = order_id;
	this.order_items = order_items;
	this.shipment_date = shipment_date;
    }

    public Order(int order_id, StockItemType order_item, LocalDate shipment_date) {
	this.order_id = order_id;
	this.shipment_date = shipment_date;

	List<StockItemType> order_items = new ArrayList<>();
	order_items.add(order_item);
	this.order_items = order_items;
    }

    public int getID() {
	return this.order_id;
    }
    public List<StockItemType> getOrderItems() {
	return this.order_items;
    }
    public LocalDate getShipmentDate() {
	return this.shipment_date;
    }

    public void addItem(StockItemType item) {
	this.order_items.add(item);
    }

    @Override
    public String toString() {
	return "Order ID: " + this.order_id
	    + ", Shipment Date: " + this.shipment_date
	    + ", Items: " + this.order_items;
    }
}
