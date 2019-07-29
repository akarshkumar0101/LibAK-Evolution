package machinelearning.geneticalgorithm;

import java.util.ArrayList;
import java.util.List;

import data.tuple.Tuple2D;
import math.AKRandom;

//G = Genotype (chromosonne) (the genetic encoding of solution)
public interface GeneticTrainer<G> {

	public abstract double calculateFitness(G a, GAEnvironment<G> env);

	public default List<Double> calculateFitness(List<G> as, GAEnvironment<G> env) {
		List<Double> fitnesses = new ArrayList<>(as.size());
		for (int i = 0; i < as.size(); i++) {
			fitnesses.add(this.calculateFitness(as.get(i), env));
		}
		return fitnesses;
	}

	public default double getMutationChance(G a, GAEnvironment<G> env) {
		return (double) env.getPopulation().indexOf(a) / env.getPopulation().size();
	}

	public abstract double getCrossoverChance(Tuple2D<G, G> partners, GAEnvironment<G> env);

	public abstract G generateRandom(GAEnvironment<G> env);

	public abstract G crossover(G a, G b, GAEnvironment<G> env);

	public abstract G mutate(G a, GAEnvironment<G> env);

	public abstract List<Tuple2D<G, G>> selectCrossoverPartners(List<G> population, GAEnvironment<G> env);

	public abstract List<G> killOff(List<G> population, int numToKill, GAEnvironment<G> env);

	// stochastic universal sampling
	public default List<Tuple2D<G, G>> selectCrossoverPartnersSUS(List<G> population, int numCrossovers,
			GAEnvironment<G> env) {

		double fitnessOffset = Double.MAX_VALUE;
		for (G c : population) {

			double fit = env.getFitnesses().get(c);
			if (fit < fitnessOffset) {
				fitnessOffset = fit;
			}
		}
		if (fitnessOffset < 0) {
			fitnessOffset *= -1;
		} else {
			fitnessOffset = 0;
		}
		fitnessOffset += 0.1;

		// calculate total fitness
		double totalFitness = 0;
		for (G c : population) {
			totalFitness += env.getFitnesses().get(c) + fitnessOffset;
		}

		List<Tuple2D<G, G>> partners = new ArrayList<>(numCrossovers);

		// System.out.println(fitnessOffset);

		for (int i = 0; i < numCrossovers; i++) {
			double pick1Fit = AKRandom.randomNumber(0, totalFitness);
			double pick2Fit = (pick1Fit + totalFitness / 2) % totalFitness;

			G a = null, b = null;
			double currentFitAt = 0;
			for (G c : population) {
				double fit = env.getFitnesses().get(c) + fitnessOffset;
				currentFitAt += fit;

				if (currentFitAt > pick1Fit && a == null) {
					a = c;
				}
				if (currentFitAt > pick2Fit && b == null) {
					b = c;
				}
				if (a != null && b != null) {
					break;
				}
			}
			if (a == null || b == null) {
				System.out.println("rip");
			}
			partners.add(new Tuple2D<>(a, b));
		}
		return partners;

	}

	public default List<Tuple2D<G, G>> selectCrossoverPartnersRandomly(List<G> population, int numCrossovers,
			GAEnvironment<G> env) {

		ArrayList<Tuple2D<G, G>> partners = new ArrayList<>(numCrossovers);

		for (int i = 0; i < numCrossovers; i++) {
			G a = population.get((int) AKRandom.randomNumber(0, population.size()));
			G b = population.get((int) AKRandom.randomNumber(0, population.size()));
			partners.add(new Tuple2D<>(a, b));
		}

		return partners;
	}

	public default List<G> killOffWorst(List<G> population, int numToKill, GAEnvironment<G> env) {
		List<G> killed = new ArrayList<>(numToKill);

		for (int i = 0; i < numToKill; i++) {
			G leastFitC = null;
			double leastfitness = Double.MAX_VALUE;
			for (G c : population) {
				if (!killed.contains(c)) {
					if (env.getFitnesses().containsKey(c)) {
						double fitness = env.getFitnesses().get(c);
						if (fitness < leastfitness) {
							leastfitness = fitness;
							leastFitC = c;
						}
					}
				}
			}
			killed.add(leastFitC);
		}

		return killed;

	}

}
