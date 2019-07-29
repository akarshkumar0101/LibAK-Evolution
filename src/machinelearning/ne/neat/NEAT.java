package machinelearning.ne.neat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import data.tuple.Tuple2D;
import machinelearning.ne.neat.genome.Genome;
import math.AKRandom;

public class NEAT implements Iterable<Genome> {

	// private Map<Genome, List<Genome>> species;
	public final List<Species> species;

	private final Map<Genome, Double> fitnesses;

	private NEATTrainer trainer;

	private int preferredPopulationSize;

	private int currentGenerationFinished;

	private NEATStats neatStats;

	private List<Genome> population;

	public final AKRandom akRandom = new AKRandom();

	public NEAT(int preferredPopulationSize, NEATTrainer trainer, NEATStats neatStats) {
		this.setTrainer(trainer);

		this.species = new ArrayList<>();
		this.fitnesses = new HashMap<>();

		this.preferredPopulationSize = preferredPopulationSize;
		this.currentGenerationFinished = -1;
		this.currentInnovationNumber = 0;

		this.setNeatStats(neatStats);

		this.population = null;
	}

	// runs the first generation (generation 0)
	public void initialize() {
		this.population = new ArrayList<>(this.preferredPopulationSize);
		for (int i = 0; i < this.preferredPopulationSize; i++) {
			this.population.add(this.trainer.generateRandom(this));
		}
		this.calculateFitnesses(this.population);
		this.putIntoSpecies(this.population);
		this.currentGenerationFinished = 0;
	}

	// AVERAGE FITNESS OF A SPECIES IS SAME AS THE TOTAL (SUM OF) ADJUSTED FITNESSES

	// this will get the current population, find fitness and sort population into
	// species.
	// then it will use the current population to calculate the next generation
	// population.
	public void runGeneration() {

		// WE NEED TO NOT LET HALF OF THE SPECIES REPRODUCE, KILL THEM OFF OR SOMETHING
		for (Species spec : this.species) {
			// sort by fitness
			spec.sortByFitness(this);

			// kill off half
			// midPoint is second mid index if size is even, and the real mid index when odd
			int midPoint = spec.size() / 2 + 1;

			for (int i = spec.size() - 1; i >= midPoint; i--) {
				spec.remove(i);
			}
		}

		// NOW CONSTRUCTING NEXT GEN

		// NOTE: shouldn't you only extinct species if they have survived 15 generations
		// and still not increased in size?

		// put best genomes from each species into next generation

		// this is the new population now.
		this.assignNextGenerationFromSpecies();

		// our generation is population variable
		// previous generation is in species so clear them

		for (Species spec : this.species) {
			spec.assignNewRandomRepresentative();
			spec.clear();
		}

		// this.fitnesses.clear();

		this.calculateFitnesses(this.population);

		// Place genomes into species
		this.putIntoSpecies(this.population);

		// Remove unused empty species
		this.removeExtinctSpecies();

		this.currentGenerationFinished++;
		this.generationalInnovations.clear();

	}

	public void assignNextGenerationFromSpecies() {
		this.population.clear();

		for (Species spec : this.species) {
			if (spec.size() > 0) {
				Genome bestInSpec = spec.get(0);
				this.population.add(bestInSpec);
			}
		}
		System.out.println("Copied " + this.population.size() + " champions from previous species.");

		int numOffspringNeeded = this.preferredPopulationSize - this.population.size();

		// construct avgFitnessInSpecies
		Map<Species, Double> avgFitnessInSpecies = new HashMap<>(this.species.size());
		double sumAvgFitnessInSpecies = 0.0;
		for (Species spec : this.species) {
			double avgFitness = spec.calculateAverageFitness(this);
			avgFitnessInSpecies.put(spec, avgFitness);

			sumAvgFitnessInSpecies += avgFitness;
		}

		this.species.sort((o1, o2) -> (int) Math.signum(avgFitnessInSpecies.get(o2) - avgFitnessInSpecies.get(o1)));

		for (Species spec : this.species) {
			double percentFitnessMakeup = avgFitnessInSpecies.get(spec) / sumAvgFitnessInSpecies;

			// round down always
			int numOffspring = (int) (percentFitnessMakeup * numOffspringNeeded);

			if (spec.lastGenerationOfIncrease < this.currentGenerationFinished + 15) {
				// numOffspring = 0;
			}

			// System.out.println("Species " + spec.id + " had average fitness of " +
			// avgFitnessInSpecies.get(spec)
			// + " and was granted " + numOffspring + " children");

			List<Genome> speciesOffSpring = this.getOffspringForSpecies(spec, numOffspring, avgFitnessInSpecies);

			this.mutateGroup(speciesOffSpring);

			this.population.addAll(speciesOffSpring);
		}

		// Fill rest of the population with brand new organisms
		while (this.population.size() < this.preferredPopulationSize) {
			this.population.add(this.trainer.generateRandom(this));
		}
	}

	public List<Genome> getOffspringForSpecies(Species spec, int numOffspring,
			Map<Species, Double> avgFitnessInSpecies) {
		List<Genome> offspring = new ArrayList<>(numOffspring);

		// 25% of offspring are clones
		// 75% of offspring are crosses
		int crossAmount = (int) (this.neatStats.getPercentOffspringFromCrossover(this) * numOffspring);

		for (int i = 0; i < crossAmount; i++) {
			Genome p1 = this.getWeightedRandom(spec, this.fitnesses);
			p1 = spec.get((int) this.akRandom.nextRandomNumber(spec.size()));

			Species secondParentSpec = spec;
			// interspecies mating rate
			if (this.akRandom.nextRandomChance(this.neatStats.getCrossoverInterspeciesProbability(this))) {
				secondParentSpec = this.getWeightedRandom(this.species, avgFitnessInSpecies);
			}
			Genome p2 = this.getWeightedRandom(secondParentSpec, this.fitnesses);

			p2 = spec.get((int) this.akRandom.nextRandomNumber(spec.size()));

			Genome child = null;
			// use adjusted fitness here
			if (this.fitnesses.get(p1) >= this.fitnesses.get(p2)) {
				child = this.trainer.crossover(p1, p2, this);
			} else {
				child = this.trainer.crossover(p2, p1, this);
			}
			offspring.add(child);
		}

		while (offspring.size() < numOffspring) {
			Genome selected = this.getWeightedRandom(spec, this.fitnesses);

			// geno = spec.get((int) akRandom.nextRandomNumber(spec.size()));

			Genome newGeno = new Genome(this.getNewGenomeID(), selected);
			offspring.add(newGeno);
		}

		return offspring;
	}

	private <T> T getWeightedRandom(List<T> list, Map<T, Double> fitnesses) {
		double totalWeight = 0.0;
		for (T t : list) {
			totalWeight += fitnesses.get(t);
		}

		double rand = this.akRandom.nextRandomNumber(totalWeight);

		double currentWeight = 0.0;
		for (T t : list) {
			currentWeight += fitnesses.get(t);
			if (currentWeight >= rand) {
				return t;
			}
		}
		throw new RuntimeException("There was an error calculating the fitnesses (probably had negative fitnesses)");
	}

	private void putIntoSpecies(Genome geno) {
		for (Species spec : this.species) {
			if (this.trainer.areSimilar(geno, spec.getRepresentative(), this)) {
				// found it
				spec.add(geno);
				if (geno.fitness > spec.maxFit) {
					spec.lastGenerationOfIncrease = this.currentGenerationFinished;
					spec.maxFit = geno.fitness;
				}
				return;
			}
		}
		Species newSpec = new Species(this.getNewSpeciesID(), geno, this);
		newSpec.lastGenerationOfIncrease = this.currentGenerationFinished;
		newSpec.maxFit = geno.fitness;
		System.err.println("NEW SPECIES: " + newSpec.ID);
		this.species.add(newSpec);
	}

	private void putIntoSpecies(List<Genome> genos) {
		for (Genome geno : genos) {
			this.putIntoSpecies(geno);
		}
		for (Species spec : this.species) {
			spec.age++;
		}
	}

	private void removeExtinctSpecies() {
		this.species.removeIf(spec -> {
			if (spec.isEmpty()) {
				System.err.println("DEAD SPECIES: " + spec.ID);
			}
			return spec.isEmpty();
		});
	}

	private void calculateFitnesses(List<Genome> population) {
		List<Double> fitnessesList = this.trainer.calculateFitness(population, this);

		for (int i = 0; i < population.size(); i++) {
			Genome geno = population.get(i);
			double fitness = fitnessesList.get(i);
			this.fitnesses.put(geno, fitness);
			geno.fitness = fitness;

//			if (fitness > highestScore) {
//				highestScore = fitness;
//				fittestGenome = geno;
//			}
		}
	}

	private void mutateGroup(List<Genome> group) {
		for (Genome geno : group) {
			if (this.akRandom.nextRandomChance(this.neatStats.getMutationProbability(this))) {
				this.trainer.mutate(geno, this);
			}
		}
	}

	public void setTrainer(NEATTrainer trainer) {
		this.trainer = trainer;
	}

	public int getPreferredPopulationSize() {
		return this.preferredPopulationSize;
	}

	public List<Genome> getPopulation() {
		return this.population;
	}

	public Map<Genome, Double> getFitnesses() {
		return this.fitnesses;
	}

	public int getCurrentGenerationFinished() {
		return this.currentGenerationFinished;
	}

	public int currentInnovationNumber = 0;
	private final HashMap<Tuple2D<Integer, Integer>, Integer> generationalInnovations = new HashMap<>();

	// public int samemutation = 0, uniquemutation = 0;

	public int getInnovationNumberForConnectionMutation(int inputNodeID, int outputNodeID) {
		for (Tuple2D<Integer, Integer> structure : this.generationalInnovations.keySet()) {
			if (structure.getA() == inputNodeID && structure.getB() == outputNodeID) {
//				System.out.println("found same mutation in generation!");
//				samemutation++;
//				System.out.println("distributed: " + this.generationalInnovations.get(structure));
				// found structure already in generation
				return this.generationalInnovations.get(structure);
			}
		}
		int innov = this.currentInnovationNumber++;

		this.generationalInnovations.put(new Tuple2D<>(inputNodeID, outputNodeID), innov);
//		uniquemutation++;
//		System.out.println("distributed: " + innov);
		return innov;
	}

	private long currentGenomeID = 0;

	public long getNewGenomeID() {
		return this.currentGenomeID++;
	}

	private long currentSpeciesID = 0;

	public long getNewSpeciesID() {
		return this.currentSpeciesID++;
	}

	public NEATStats getNeatStats() {
		return this.neatStats;
	}

	public void setNeatStats(NEATStats neatStats) {
		this.neatStats = neatStats;
	}

	public int size() {
		int size = 0;
		for (Species spec : this.species) {
			size += spec.size();
		}

		return size;
	}

	public boolean contains(Genome g) {
		for (Species spec : this.species) {
			if (spec.contains(g)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Iterator<Genome> iterator() {
		// List<Iterable<Genome>> iterables = new LinkedList<>();
		// iterables.addAll(this.species);
		// return new CombinedIterator<>(iterables);

		return this.population.iterator();
	}
}
