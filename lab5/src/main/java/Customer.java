/**
 * Class representing a customer in the system
 */
public class Customer {
    private final int id;
    private final long arrivalTime;
    
    public Customer(int id) {
        this.id = id;
        this.arrivalTime = System.currentTimeMillis();
    }
    
    public int getId() {
        return id;
    }
    
    public long getArrivalTime() {
        return arrivalTime;
    }
} 