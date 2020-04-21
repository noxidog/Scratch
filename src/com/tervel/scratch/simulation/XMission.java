package com.tervel.scratch.simulation;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class XMission {
    public static final int POPULATION_SIZE = 1000;
    public static final int INITIAL_INFECTION_RATE = 10; //Percent
    public static final int TRANSMISSION_PROBABILITY = 30; //Percent
    public static final int ILLNESS_DURATION = 14;//Days
    public static final int CONGREGATION_PROBABILITY = 30;
    public static final int MAX_GROUP_SIZE = 10;

    static class Individual {
        int infected = 0;
        boolean isContageous() {
            return infected > 0;
        }

        public void infect(int illnessDuration) {
            if (0 == infected)
                infected = illnessDuration;
        }

        public void evolve() {
            if (0 != infected) {
                if (0==--infected)
                    infected = -1;
            }
        }
    }

    static class PopulationStatistics {
        public int infectedCount = 0;

        public void reset() {
            infectedCount = 0;
        }
    }

    private static Random rand = new Random();
    private static Individual[] makeInitial(int size,
                                            int initialInfectionRate,
                                            int illnessDuration) {
        Individual[] ret = new Individual[size];
        for (int i = 0; i < ret.length; i++) {
            Individual individual = new Individual();
            if (rand.nextInt(100) < initialInfectionRate)
                individual.infected = illnessDuration;
            ret[i] = individual;
        }
        return ret;
    }

    private static Individual removeNext(List<Individual> pop) {
        return pop.remove(rand.nextInt(pop.size()));
//        return pop.remove(0);
    }

    private static void evolvegroup(Individual[] group,
                                    int actualSize,
                                    int transmissionProbability,
                                    int illnessDuration) {
        boolean[] infected = new boolean[actualSize];
        for (int i = 0; i < actualSize; i++) {
            Individual current = group[i];
            for (int j = i+1; j < actualSize; j++) {
                Individual partner = group[j];
                if (current.isContageous() &&
                        (rand.nextInt(100) < transmissionProbability))
                    infected[j] = true;
                if (partner.isContageous() &&
                        (rand.nextInt(100) < transmissionProbability))
                    infected[i] = true;
            }
        }
        for (int i = 0; i < infected.length; i++) {
            if (infected[i])
                group[i].infect(illnessDuration);
        }
    }

    public static void evolve(Individual[] population,
                              PopulationStatistics populationStatistics,
                              int maxGroupSize,
                              int congregationProbablity,
                              int transmissionProbability,
                              int illnessDuration) {
        LinkedList<Individual> congregators = new LinkedList<>();
        for (int i = 0; i < population.length; i++) {
            if (rand.nextInt(100) < congregationProbablity) {
                congregators.add(population[i]);
            }
        }
        int curr = 0;
        Individual[] group = new Individual[maxGroupSize];
        while (!congregators.isEmpty()) {
            if (curr == maxGroupSize) {
                evolvegroup(group, curr, transmissionProbability, illnessDuration);
                curr = 0;
            }
            group[curr++] = removeNext(congregators);
        }
        if (curr != 0) {
            evolvegroup(group, curr, transmissionProbability, illnessDuration);
            curr = 0;
        }
        for (int i = 0; i < population.length; i++) {
            Individual individual = population[i];
            if (individual.isContageous())
                populationStatistics.infectedCount++;
            individual.evolve();
        }
    }

    public static void main(String[] args) {
        Individual[] population = makeInitial(POPULATION_SIZE, INITIAL_INFECTION_RATE, ILLNESS_DURATION);
        PopulationStatistics populationStatistics = new PopulationStatistics();
        int dayCount = 0;
        for (;
             0==dayCount || populationStatistics.infectedCount != 0;
             ++dayCount) {
            populationStatistics.reset();
            evolve(population, populationStatistics,
                    MAX_GROUP_SIZE,
                    CONGREGATION_PROBABILITY,
                    TRANSMISSION_PROBABILITY,
                    ILLNESS_DURATION);
            System.out.printf("=== Day %d =======\n", dayCount);
            System.out.printf("\tInfected: %d\n", populationStatistics.infectedCount);
        }
        System.out.printf("=== Final Day %d =======\n", dayCount);
        int unaffected = 0;
        for (int i = 0; i < population.length; i++) {
            Individual current = population[i];
            if (current.infected == 0)
                ++unaffected;
        }
        System.out.printf("\tUnaffected: %d\n", unaffected);
    }
}
