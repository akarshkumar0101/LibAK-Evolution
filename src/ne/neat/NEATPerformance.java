package machinelearning.ne.neat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import machinelearning.ne.neat.genome.Genome;
import math.stat.Statistic;

public class NEATPerformance {

	public int numGenerations;
	public Map<Integer, GenerationalPerformance> performances;

	public NEATPerformance() {
		this.performances = new HashMap<>();
	}

	public static double averageFitness(NEAT neat) {
		double avg = 0;
		for (Genome geno : neat) {
			avg += neat.getFitnesses().get(geno);
		}
		avg /= neat.size();
		return avg;
	}

	public static Genome bestGenome(NEAT neat) {
		double maxFitness = -Double.MAX_VALUE;
		Genome bestGeno = null;
		for (Genome geno : neat) {
			double fit = neat.getFitnesses().get(geno);

			if (fit > maxFitness) {
				maxFitness = fit;
				bestGeno = geno;
			}
		}
		return bestGeno;
	}

	public static double averageHiddenNodes(NEAT neat) {
		double hiddenNodes = 0;
		for (Genome geno : neat) {
			hiddenNodes += geno.getNumHiddenNodes();
		}
		hiddenNodes /= neat.size();
		return hiddenNodes;
	}

	public void registerPerformance(NEAT neat) {
		int gen = neat.getCurrentGenerationFinished();

		GenerationalPerformance performance = new GenerationalPerformance(neat);
		this.performances.put(gen, performance);
		this.numGenerations++;
	}

	public List<Genome> getPopulationSnapshot(int generation) {
		return this.performances.get(generation).populationSnapshot;
	}

	public List<Species> getSpeciesSnapshot(int generation) {
		return this.performances.get(generation).speciesSnapshot;
	}

	public Statistic<Genome> getOverallperformance(int generation) {
		return this.performances.get(generation).overallFitnessPerformance;
	}

	public List<Species> speciesForGeneration(int generation) {
		return this.performances.get(generation).speciesSnapshot;
	}

}

class GenerationalPerformance {

	List<Genome> populationSnapshot;
	List<Species> speciesSnapshot;

	Statistic<Genome> overallFitnessPerformance;
	Statistic<Species> speciesSizePerformance;

	// Map<Species, Statistic<Genome>> speciesIndvidualPerformance;

	public GenerationalPerformance(NEAT neat) {
		this.populationSnapshot = new ArrayList<>(neat.getPopulation());

		this.speciesSnapshot = new ArrayList<>(neat.species.size());

		for (Species spec : neat.species) {
			// have to clone species because species will be changing
			this.speciesSnapshot.add(spec.clone());
		}

		this.overallFitnessPerformance = new Statistic<>(this.populationSnapshot, a -> a.fitness);

		this.speciesSizePerformance = new Statistic<>(this.speciesSnapshot, spec -> (double) spec.size());
	}

}
