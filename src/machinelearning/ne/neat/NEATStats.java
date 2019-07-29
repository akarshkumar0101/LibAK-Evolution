package machinelearning.ne.neat;

public class NEATStats {

	// similarity parameters for species calculation
	public double c1 = 1.0;
	public double c2 = 1.0;
	public double c3 = 0.4;
	public double deltaThreshold = 3.0;

	// general GA parameters
	public double percentPopulationToKill = 0.5;

	public double percentOffspringFromCrossover = 0.75;

	public double crossoverInterspeciesProbability = 0.001;

	// mutation parameters
	public double weightShiftStrengh = 0.02;
	public double weightRandomizeStrengh = 2.0;

	// mutation probabilities
	public double mutationProbability = 1.0;

	public double alterAllWeightsProbability = 0.8;

	public double weightRandomizeProbability = 0.10;

	public double addConnectionProbability = 0.05;
	public double addNodeProbability = 0.0;// 0.01;// 0.03;

	public double toggleConnectionProbability = 0.0;

	public double getC1(NEAT neat) {
		return this.c1;
	}

	public double getC2(NEAT neat) {
		return this.c2;
	}

	public double getC3(NEAT neat) {
		return this.c3;
	}

	public double getDeltaThreshold(NEAT neat) {
		return this.deltaThreshold;
	}

	public double getPercentPopulationToKill(NEAT neat) {
		return this.percentPopulationToKill;
	}

	public double getPercentOffspringFromCrossover(NEAT neat) {
		return this.percentOffspringFromCrossover;
	}

	public double getCrossoverInterspeciesProbability(NEAT neat) {
		return this.crossoverInterspeciesProbability;
	}

	public double getWeightShiftStrengh(NEAT neat) {
		return this.weightShiftStrengh;
	}

	public double getWeightRandomizeStrengh(NEAT neat) {
		return this.weightRandomizeStrengh;
	}

	public double getMutationProbability(NEAT neat) {
		return this.mutationProbability;
	}

	public double getAlterAllWeightsProbability(NEAT neat) {
		return this.alterAllWeightsProbability;
	}

	public double getWeightRandomizeProbability(NEAT neat) {
		return this.weightRandomizeProbability;
	}

	public double getAddConnectionProbability(NEAT neat) {
		return this.addConnectionProbability;
	}

	public double getAddNodeProbability(NEAT neat) {
		return this.addNodeProbability;
	}

	public double getToggleConnectionProbability(NEAT neat) {
		return this.toggleConnectionProbability;
	}

}
