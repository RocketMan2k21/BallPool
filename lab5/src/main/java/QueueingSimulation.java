import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class QueueingSimulation {
    // Simulation parameters
    private final int numChannels;
    private final int queueCapacity;
    private final long simulationTimeMillis;
    private final double arrivalMinTime;
    private final double arrivalMaxTime;
    private final double serviceMean;
    private final double serviceStdDev;
    private final double monitoringInterval;
    
    // Shared state
    private final BlockingQueue<Customer> queue;
    private final AtomicBoolean simulationRunning = new AtomicBoolean(true);
    private final AtomicInteger totalArrivals = new AtomicInteger(0);
    private final AtomicInteger totalRejected = new AtomicInteger(0);
    private final AtomicInteger totalServed = new AtomicInteger(0);
    private final List<Integer> queueLengthSamples = new CopyOnWriteArrayList<>();
    private final String[] channelStatus;
    private final Lock statusLock = new ReentrantLock();
    
    // Random generator
    private final Random random = new Random();
    
    public QueueingSimulation(int numChannels, int queueCapacity, int simulationTime,
                         double arrivalMinTime, double arrivalMaxTime, 
                         double serviceMean, double serviceStdDev,
                         double monitoringInterval) {
        this.numChannels = numChannels;
        this.queueCapacity = queueCapacity;
        this.simulationTimeMillis = simulationTime * 1000;
        this.arrivalMinTime = arrivalMinTime;
        this.arrivalMaxTime = arrivalMaxTime;
        this.serviceMean = serviceMean;
        this.serviceStdDev = serviceStdDev;
        this.monitoringInterval = monitoringInterval;
        
        this.queue = new ArrayBlockingQueue<>(queueCapacity);
        this.channelStatus = new String[numChannels];
        for (int i = 0; i < numChannels; i++) {
            channelStatus[i] = "Idle";
        }
    }
    
    public SimulationResults runSimulation() {
        // Create thread pool for channels, producer, and monitor
        ExecutorService executor = Executors.newFixedThreadPool(numChannels + 2);
        
        // Start producer thread
        executor.submit(new Producer());
        
        // Start service channel threads
        for (int i = 0; i < numChannels; i++) {
            executor.submit(new ServiceChannel(i));
        }
        
        // Start monitoring thread
        executor.submit(new Monitor());
        
        // Wait for simulation time to complete
        try {
            Thread.sleep(simulationTimeMillis);
            simulationRunning.set(false);
            
            // Shutdown the executor and wait for tasks to complete
            executor.shutdown();
            
            // Wait for all tasks to complete (maximum 10 seconds)
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("Forcing shutdown of remaining tasks...");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Simulation interrupted: " + e.getMessage());
            executor.shutdownNow();
        }
        
        // Calculate final statistics
        double avgQueueLength = queueLengthSamples.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        
        double rejectionProbability = totalArrivals.get() > 0 
                ? (double) totalRejected.get() / totalArrivals.get() 
                : 0.0;
                
        return new SimulationResults(
            avgQueueLength,
            rejectionProbability,
            totalArrivals.get(),
            totalRejected.get(),
            totalServed.get()
        );
    }
    
    /**
     * Producer thread that generates customers at random intervals
     */
    private class Producer implements Runnable {
        @Override
        public void run() {
            while (simulationRunning.get()) {
                // Generate a customer
                int customerId = totalArrivals.incrementAndGet();
                Customer customer = new Customer(customerId);
                
                // Try to add customer to the queue
                if (queue.offer(customer)) {
                    System.out.println("Customer " + customerId + " arrived and joined the queue.");
                } else {
                    // Queue is full, customer is rejected
                    totalRejected.incrementAndGet();
                    System.out.println("Customer " + customerId + " arrived but was rejected (queue full).");
                }
                
                // Wait for next customer
                try {
                    double interarrivalTime = arrivalMinTime + (arrivalMaxTime - arrivalMinTime) * random.nextDouble();
                    Thread.sleep((long)(interarrivalTime * 1000));
                } catch (InterruptedException e) {
                    if (!simulationRunning.get()) {
                        break;
                    }
                    Thread.currentThread().interrupt();
                }
            }
            
            System.out.println("Producer has finished generating customers.");
        }
    }
    
    /**
     * Service channel thread that processes customers from the queue
     */
    private class ServiceChannel implements Runnable {
        private final int channelId;
        
        public ServiceChannel(int channelId) {
            this.channelId = channelId;
        }
        
        @Override
        public void run() {
            while (simulationRunning.get() || !queue.isEmpty()) {
                try {
                    // Try to get a customer from the queue with a timeout
                    Customer customer = queue.poll(100, TimeUnit.MILLISECONDS);
                    
                    if (customer != null) {
                        // Update channel status
                        updateChannelStatus("Serving customer " + customer.getId());
                        
                        System.out.println("Channel " + (channelId + 1) + " started serving customer " + customer.getId());
                        
                        // Serve the customer (normally distributed service time)
                        double serviceTime = generateServiceTime();
                        Thread.sleep((long)(serviceTime * 1000));
                        
                        // Customer has been served
                        totalServed.incrementAndGet();
                        updateChannelStatus("Idle");
                        
                        System.out.println("Channel " + (channelId + 1) + " finished serving customer " + customer.getId());
                    }
                } catch (InterruptedException e) {
                    if (!simulationRunning.get() && queue.isEmpty()) {
                        break;
                    }
                    Thread.currentThread().interrupt();
                }
            }
            
            System.out.println("Channel " + (channelId + 1) + " has shut down.");
        }
        
        private void updateChannelStatus(String status) {
            statusLock.lock();
            try {
                channelStatus[channelId] = status;
            } finally {
                statusLock.unlock();
            }
        }
        
        private double generateServiceTime() {
            // Generate normally distributed service time (with minimum of 0.001)
            double time;
            do {
                time = random.nextGaussian() * serviceStdDev + serviceMean;
            } while (time < 0.001);
            return time;
        }
    }
    
    /**
     * Monitor thread that collects statistics
     */
    private class Monitor implements Runnable {
        @Override
        public void run() {
            long lastDisplayUpdate = System.currentTimeMillis();
            
            while (simulationRunning.get() || !queue.isEmpty()) {
                // Sample the current queue length
                int currentQueueLength = queue.size();
                queueLengthSamples.add(currentQueueLength);
                
                // Display current state periodically
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastDisplayUpdate >= 1000) {  // Update every second
                    displaySystemState();
                    lastDisplayUpdate = currentTime;
                }
                
                // Wait before next sample
                try {
                    Thread.sleep((long)(monitoringInterval * 1000));
                } catch (InterruptedException e) {
                    if (!simulationRunning.get() && queue.isEmpty()) {
                        break;
                    }
                    Thread.currentThread().interrupt();
                }
            }
            
            // Final state display
            displaySystemState();
            System.out.println("Monitor has finished collecting statistics.");
        }
        
        private void displaySystemState() {
            System.out.println("\n============================================================");
            System.out.println("SYSTEM STATE:");
            System.out.println("Queue Length: " + queue.size() + "/" + queueCapacity);
            System.out.println("Arrivals: " + totalArrivals.get() + 
                             ", Rejected: " + totalRejected.get() + 
                             ", Served: " + totalServed.get());
            System.out.println("Channel Status:");
            
            statusLock.lock();
            try {
                for (int i = 0; i < channelStatus.length; i++) {
                    System.out.println("  Channel " + (i + 1) + ": " + channelStatus[i]);
                }
            } finally {
                statusLock.unlock();
            }
            
            System.out.println("============================================================\n");
        }
    }
} 