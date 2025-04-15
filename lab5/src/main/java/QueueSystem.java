import java.util.concurrent.atomic.AtomicInteger;

public class QueueSystem {
    public static void main(String[] args) {
        // Configuration parameters
        int numChannels = 3;                // Number of service channels
        int queueCapacity = 5;             // Maximum queue length
        int simulationTime = 15;           // Total simulation time in seconds
        double arrivalMinTime = 0.3;       // Minimum time between arrivals
        double arrivalMaxTime = 1.0;       // Maximum time between arrivals
        double serviceMean = 2.0;          // Mean service time
        double serviceStdDev = 0.5;        // Standard deviation of service time
        double monitoringInterval = 0.1;   // Statistics collection interval
        int numRuns = 5;                   // Number of simulation runs for statistical significance

        // Run multiple simulations
        double totalAvgQueueLength = 0;
        double totalRejectionProbability = 0;

        for (int run = 1; run <= numRuns; run++) {
            System.out.println("\n==== Starting Simulation Run " + run + " ====\n");
            
            QueueingSimulation simulation = new QueueingSimulation(
                numChannels, queueCapacity, simulationTime,
                arrivalMinTime, arrivalMaxTime, serviceMean, serviceStdDev,
                monitoringInterval
            );
            
            SimulationResults results = simulation.runSimulation();
            
            totalAvgQueueLength += results.getAverageQueueLength();
            totalRejectionProbability += results.getRejectionProbability();
            
            System.out.println("\n==== Simulation Run " + run + " Results ====");
            System.out.println("Average Queue Length: " + results.getAverageQueueLength());
            System.out.println("Rejection Probability: " + results.getRejectionProbability());
            System.out.println("Total Arrivals: " + results.getTotalArrivals());
            System.out.println("Total Rejected: " + results.getTotalRejected());
            System.out.println("Total Served: " + results.getTotalServed());
            System.out.println("==========================================\n");
        }
        
        // Calculate overall statistics from multiple runs
        double finalAvgQueueLength = totalAvgQueueLength / numRuns;
        double finalRejectionProbability = totalRejectionProbability / numRuns;
        
        System.out.println("\n==== Overall Statistics (Average of " + numRuns + " Runs) ====");
        System.out.println("Average Queue Length: " + finalAvgQueueLength);
        System.out.println("Rejection Probability: " + finalRejectionProbability);
        System.out.println("=================================================\n");
        
        // Calculate theoretical measures for specific cases (M/M/c/K)
        calculateTheoreticalMeasures(numChannels, queueCapacity, 1.0 / ((arrivalMinTime + arrivalMaxTime) / 2), 1.0 / serviceMean);
    }
    
    /**
     * Calculate theoretical measures for M/M/c/K queue
     * (Assumes Poisson arrivals and exponential service times)
     */
    private static void calculateTheoreticalMeasures(int c, int k, double lambda, double mu) {
        double rho = lambda / (c * mu); // Traffic intensity
        
        // Calculate p0 (probability of empty system)
        double sum = 0;
        for (int n = 0; n < c; n++) {
            sum += Math.pow(lambda / mu, n) / factorial(n);
        }
        sum += Math.pow(lambda / mu, c) / factorial(c) * (1 - Math.pow(rho, k - c + 1)) / (1 - rho);
        double p0 = 1 / sum;
        
        // Calculate rejection probability
        double pk = p0 * Math.pow(lambda / mu, c + k - c) / (factorial(c) * Math.pow(c, k - c));
        
        // Calculate average queue length (excluding customers in service)
        double lq = 0;
        if (rho != 1) {
            lq = p0 * Math.pow(lambda / mu, c) * rho / (factorial(c) * Math.pow(1 - rho, 2)) 
                * (1 - Math.pow(rho, k - c + 1) - (k - c + 1) * Math.pow(rho, k - c) * (1 - rho));
        } else {
            lq = p0 * Math.pow(lambda / mu, c) * (k - c) * (k - c + 1) / (2 * factorial(c));
        }
        
        System.out.println("\n==== Theoretical Measures (M/M/" + c + "/" + (c + k) + ") ====");
        System.out.println("Arrival rate (λ): " + lambda + " customers/time unit");
        System.out.println("Service rate (μ): " + mu + " customers/time unit");
        System.out.println("Traffic intensity (ρ): " + rho);
        System.out.println("Probability of empty system (p₀): " + p0);
        System.out.println("Rejection probability: " + pk);
        System.out.println("Average queue length: " + lq);
        System.out.println("=======================================\n");
    }
    
    private static double factorial(int n) {
        if (n <= 1) return 1;
        double result = 1.0;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
} 