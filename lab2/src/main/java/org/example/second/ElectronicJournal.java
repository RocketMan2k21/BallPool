package org.example.second;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ElectronicJournal {
    private static final int NUM_GROUPS = 3;
    private static final int NUM_LECTURERS = 4; // 1 лектор + 3 асистенти
    private static final int WEEKS = 10;

    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> journal;
    private Random random = new Random();

    public ElectronicJournal() {
        journal = new ConcurrentHashMap<>();
        initializeJournal();
    }

    private void initializeJournal() {
        for (int group = 1; group <= NUM_GROUPS; group++) {
            journal.put("Group" + group, new ConcurrentHashMap<>());
        }
    }

    public void recordGrades() {
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_LECTURERS);

        for (int week = 1; week <= WEEKS; week++) {
            final int currentWeek = week;
            executorService.execute(() -> {
                for (String groupName : journal.keySet()) {
                    int grade = generateGrade();
                    journal.get(groupName).put(currentWeek, grade);
                    System.out.printf("Week %d: %s grade = %d\n", currentWeek, groupName, grade);
                }
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int generateGrade() {
        return random.nextInt(61) + 40; // Від 40 до 100
    }

    public void printJournal() {
        for (String group : journal.keySet()) {
            System.out.println("\n" + group + " grades:");
            journal.get(group).forEach((week, grade) ->
                    System.out.printf("Week %d: %d\n", week, grade)
            );
        }
    }

    public static void main(String[] args) {
        ElectronicJournal journal = new ElectronicJournal();
        journal.recordGrades();
        journal.printJournal();
    }
}