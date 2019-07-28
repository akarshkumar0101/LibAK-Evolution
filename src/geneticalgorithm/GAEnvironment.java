package machinelearning.geneticalgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import data.tuple.Tuple2D;
import math.AKRandom;

//P = Phenotype (animal), G = Genotype (chromosonne) (the genetic encoding of solution)
public class GAEnvironment<G> {

	private GeneticTrainer<G> trainer;

	private List<G> population;

	private HashMap<G, Double> fitnesses;

	private int preferredPopulationSize;

	private int currentGeneration;

	public GAEnvironment(int preferredPopulationSize, GeneticTrainer<G> trainer) {

		this.preferredPopulationSize = preferredPopulationSize;
		this.setTrainer(trainer);

		this.population = new ArrayList<>(this.preferredPopulationSize);
		this.fitnesses = new HashMap<>();

		this.populateRest();

		this.currentGeneration = 0;
	}

	public void setTrainer(GeneticTrainer<G> trainer) {
		this.trainer = trainer;
	}

	public int getPreferredPopulationSize() {
		return this.preferredPopulationSize;
	}

	public List<G> getPopulation() {
		return this.population;
	}

	public HashMap<G, Double> getFitnesses() {
		return this.fitnesses;
	}

	private void populateRest() {
		for (int i = this.population.size(); i < this.preferredPopulationSize; i++) {
			this.population.add(this.trainer.generateRandom(this));
		}
	}

	public void runGeneration() {

		this.calculateFitnesses();

		this.sortPopulation();

		this.selectSurvivors();

		this.cleanupFitness();

		List<G> offspring = this.crossPopulation();// new members added

		this.mutatePopulation();

		this.population.addAll(offspring);

		this.currentGeneration++;
	}

	private void cleanupFitness() {
		this.fitnesses.entrySet().removeIf(e -> !GAEnvironment.this.population.contains(e.getKey()));
	}

	public void calculateFitnesses() {
		List<Double> fits = this.trainer.calculateFitness(this.population, this);

		for (int i = 0; i < this.population.size(); i++) {
			this.fitnesses.put(this.population.get(i), fits.get(i));
		}
	}

	public void sortPopulation() {
		Collections.sort(this.population, (o1, o2) -> {
			double dec = GAEnvironment.this.fitnesses.get(o2) - GAEnvironment.this.fitnesses.get(o1);
			return dec == 0 ? 0 : dec > 0 ? 1 : -1;
		});
	}

	private List<G> crossPopulation() {
		List<Tuple2D<G, G>> crossoverPartners = this.trainer.selectCrossoverPartners(this.population, this);

		List<G> offspring = new ArrayList<>(crossoverPartners.size());

		for (Tuple2D<G, G> partners : crossoverPartners) {
			if (AKRandom.randomChance(this.trainer.getCrossoverChance(partners, this))) {
				G geno = this.trainer.crossover(partners.getA(), partners.getB(), this);
				offspring.add(geno);
			}
		}
		return offspring;
	}

	private void mutatePopulation() {

		for (int i = 0; i < this.population.size(); i++) {
			G geno = this.population.get(i);

			if (AKRandom.randomChance(this.trainer.getMutationChance(geno, this))) {
				G newgeno = this.trainer.mutate(geno, this);
				this.population.remove(i);
				this.population.add(i, newgeno);
			}
		}
		// System.out.println("Mutated " + toAdd.size() + " members");

	}

	private void selectSurvivors() {
		int numShouldBeKilled = this.population.size() - this.preferredPopulationSize;
		List<G> killed = this.trainer.killOff(this.population, numShouldBeKilled, this);
		for (G geno : killed) {
			this.population.remove(geno);
		}
	}

}
