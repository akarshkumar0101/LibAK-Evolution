package machinelearning.ne.neat;

import java.util.ArrayList;

import machinelearning.ne.neat.genome.Genome;

public class Species extends ArrayList<Genome> {

	private static final long serialVersionUID = -8114693581105898796L;

	private final NEAT neat;

	private Genome representative;

	public final long ID;

	public int age = 0;
	public int lastGenerationOfIncrease = 0;
	double maxFit = -1;

	public Species(long ID, NEAT neat) {
		this(ID, null, neat);
	}

	public Species(long ID, Genome geno, NEAT neat) {
		super();
		this.ID = ID;
		this.representative = geno;
		if (geno != null) {
			this.add(geno);
		}
		this.neat = neat;
	}

	public void assignNewRandomRepresentative() {
		this.representative = null;
		if (!this.isEmpty()) {
			int randIndex = (int) this.neat.akRandom.nextRandomNumber(this.size());
			this.representative = this.get(randIndex);
		}
	}

	public Genome getRepresentative() {
		return this.representative;
	}

	public void setRepresentative(Genome representative) {
		this.representative = representative;
	}

	public double calculateAverageFitness(NEAT neat) {
		double avg = 0;
		for (Genome geno : this) {
			avg += neat.getFitnesses().get(geno);
		}
		avg /= this.size();
		return avg;
	}

	public void sortByFitness(NEAT neat) {
		this.sort((o1, o2) -> {
			double ret = neat.getFitnesses().get(o2) - neat.getFitnesses().get(o1);
			return (int) Math.signum(ret);
		});
	}

	public Genome selectGenome() {
		double fitnessOffset = Double.MAX_VALUE;
		for (Genome c : this) {
			double fit = c.fitness;
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
		for (Genome c : this) {
			totalFitness += c.fitness + fitnessOffset;
		}

		// System.out.println(fitnessOffset);

		double pick1Fit = this.neat.akRandom.nextRandomNumber(0, totalFitness);

		Genome a = null;
		double currentFitAt = 0;
		for (Genome c : this) {
			double fit = c.fitness + fitnessOffset;
			currentFitAt += fit;

			if (currentFitAt > pick1Fit && a == null) {
				a = c;
			}

			if (a != null) {
				break;
			}
		}
		return a;
	}

	public Genome selectRandomGenome() {
		return this.get((int) this.neat.akRandom.nextRandomNumber(this.size()));
	}

	public Genome giveBaby(NEAT neat, NEATTrainer trainer) {
		Genome baby;
		if (neat.akRandom.nextRandomChance(0.25)) {// 25% of the time there is no crossover and the child is simply a
													// clone of a
			// random(ish) player
			Genome selected = this.selectGenome();
			baby = new Genome(neat.getNewGenomeID(), selected);
		} else {// 75% of the time do crossover

			// get 2 random(ish) parents
			Genome parent1 = this.selectGenome();
			Genome parent2 = this.selectGenome();

			// the crossover function expects the highest fitness parent to be the object
			// and the lowest as the argument
			baby = trainer.crossover(parent1, parent2, neat);
		}
		baby.cleanup();
		return baby;
	}

	@Override
	public Species clone() {
		Species spec = new Species(this.ID, this.neat);
		spec.addAll(this);
		spec.setRepresentative(this.getRepresentative());
		return spec;
	}

	@Override
	public boolean equals(Object another) {
		if (another instanceof Species) {
			if (((Species) another).ID == this.ID) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (int) this.ID;
	}

}
