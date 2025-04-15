public class SimulationResults {
    private final double averageQueueLength;
    private final double rejectionProbability;
    private final int totalArrivals;
    private final int totalRejected;
    private final int totalServed;
    
    public SimulationResults(double averageQueueLength, double rejectionProbability, 
                         int totalArrivals, int totalRejected, int totalServed) {
        this.averageQueueLength = averageQueueLength;
        this.rejectionProbability = rejectionProbability;
        this.totalArrivals = totalArrivals;
        this.totalRejected = totalRejected;
        this.totalServed = totalServed;
    }
    
    public double getAverageQueueLength() { return averageQueueLength; }
    public double getRejectionProbability() { return rejectionProbability; }
    public int getTotalArrivals() { return totalArrivals; }
    public int getTotalRejected() { return totalRejected; }
    public int getTotalServed() { return totalServed; }
} 