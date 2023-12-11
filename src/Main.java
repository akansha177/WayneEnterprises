// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    private static final int ORDER_COST = 1000;
    private static final int CANCELLED_ORDER_COST = 250;
    private static final int TARGET_REVENUE = 1000000;
    private static final int MIN_CARGO_WEIGHT = 10;
    private static final int MAX_CARGO_WEIGHT = 50;
    private static final int MIN_DEPARTURE_CARGO = 50;
    private static final int MAX_DEPARTURE_CARGO = 300;
    private static final int MAINTENANCE_TRIPS = 5;
    private static final int MAINTENANCE_TIME = 1;


    private static int totalRevenue = 0;
    private static int totalOrdersDelivered = 0;
    private static int totalOrdersCancelled = 0;
    private static int consecutiveCancellations = 0;

    private static final BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>();

    static class Order {
        private final int customerId;
        private final int cargoWeight;
        private final String destination;

        public Order(int customerId, int cargoWeight, String destination) {
            this.customerId = customerId;
            this.cargoWeight = cargoWeight;
            this.destination = destination;
        }
        public int getCustomerId() {
            return customerId;
        }
        public int getCargoWeight() {
            return cargoWeight;
        }
        public String getDestination() {
            return destination;
        }
    }

    static class CustomerThread extends Thread {
        private final int customerId;
        public CustomerThread(int customerId) {
            this.customerId = customerId;
        }
        public void run() {
            while (totalRevenue < TARGET_REVENUE) {
                int cargoWeight = new Random().nextInt(MAX_CARGO_WEIGHT - MIN_CARGO_WEIGHT + 1) + MIN_CARGO_WEIGHT;
                String destination = (new Random().nextBoolean()) ? "Gotham" : "Atlanta";
                Order order = new Order(customerId, cargoWeight, destination);
                try {
                    if (cancelOrder()) {
                        System.out.println("Customer " + customerId + "'s order was cancelled");
                        totalOrdersCancelled++;
                        consecutiveCancellations++;
                        continue;
                    }
                    orderQueue.put(order);
                    System.out.println("Customer" + customerId + " place order for" + cargoWeight + "tons to " + destination + ".");
                    consecutiveCancellations = 0;//reset consecutive cancellations
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep((long) (Math.random() * 400) + 100);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        private boolean cancelOrder() {
            return consecutiveCancellations >= 3;
        }
    }
    static class ShippingThread extends Thread {
        private final int shipId;

        public ShippingThread(int shipId) {
            this.shipId = shipId;
        }
        public void run() {
            while (totalRevenue < TARGET_REVENUE) {
                try {
                    Order order = orderQueue.take();
                    processOrder(order);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep((long) (Math.random() * 400) + 100);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void processOrder(Order order) {
            if (cancelOrder()) {
                System.out.println("Customer " + order.getCustomerId() + "'s order was cancelled");
                totalOrdersCancelled++;
                consecutiveCancellations++;
                return;
            }
            //process the order, update revenue etc.
            totalRevenue += ORDER_COST;
            totalOrdersDelivered++;

            int trips = totalOrdersDelivered / MAINTENANCE_TRIPS;
            if (trips > 0 && trips % MAINTENANCE_TRIPS == 0) {
                System.out.println("Ship" + shipId + "is going to maintenance");
                try {
                    Thread.sleep(MAINTENANCE_TIME * 1000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            consecutiveCancellations = 0;// reset consecutive cancellations
        }
        private boolean cancelOrder() {
            return consecutiveCancellations >= 3;
        }
    }
    public static void main(String[] args) {
        Thread[] customerThreads = new Thread[7];
        Thread[] shippingThreads = new Thread[5];

        //start customer threads
        for (int i = 0; i < customerThreads.length; i++) {
            customerThreads[i] = new CustomerThread(i + 1);
            customerThreads[i].start();
        }

        //starting ship threads
        for (int i = 0; i < shippingThreads.length; i++) {
            shippingThreads[i] = new ShippingThread(i + 1);
            shippingThreads[i].start();
        }
        // wait for all threads to finish
        try {
            for (Thread customerThread : customerThreads) {
                customerThread.join();
            }
            for (Thread shippingThread : shippingThreads) {
                shippingThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Simulation complete. Total revenue : $" + totalRevenue);
        System.out.println("Total orders delivered: " + totalOrdersDelivered);
        System.out.println("Total orders cancelled: " + totalOrdersCancelled);
    }
}