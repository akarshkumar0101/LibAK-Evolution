package machinelearning.ne;

import java.util.List;

import array.DoubleArrays;
import data.tuple.Tuple2D;
import machinelearning.geneticalgorithm.GAEnvironment;
import machinelearning.geneticalgorithm.GeneticTrainer;
import machinelearning.neuralnet.FCNeuralNetwork;
import math.AKRandom;

public interface NEFTTrainer extends GeneticTrainer<FCNeuralNetwork> {

	@Override
	public default double getCrossoverChance(Tuple2D<FCNeuralNetwork, FCNeuralNetwork> partners,
			GAEnvironment<FCNeuralNetwork> env) {
		return .7;
	}

	@Override
	public default FCNeuralNetwork crossover(FCNeuralNetwork a, FCNeuralNetwork b, GAEnvironment<FCNeuralNetwork> env) {
		double[][][] weights = DoubleArrays.deepCopy(a.weights);
		double[][] biases = DoubleArrays.deepCopy(a.biases);

		for (int x = 0; x < weights.length; x++) {
			for (int y = 0; y < weights[x].length; y++) {
				for (int z = 0; z < weights[x][y].length; z++) {
					if (AKRandom.randomChance(.5)) {
						weights[x][y][z] = b.weights[x][y][z];
					}
				}
			}
		}
		for (int x = 0; x < biases.length; x++) {
			for (int y = 0; y < biases[x].length; y++) {
				if (AKRandom.randomChance(.5)) {
					biases[x][y] = b.biases[x][y];
				}
			}
		}
		return new FCNeuralNetwork(weights, biases);

	}

	@Override
	public default FCNeuralNetwork mutate(FCNeuralNetwork a, GAEnvironment<FCNeuralNetwork> env) {
		double[][][] weights = DoubleArrays.deepCopy(a.weights);
		double[][] biases = DoubleArrays.deepCopy(a.biases);

		for (int x = 0; x < weights.length; x++) {
			for (int y = 0; y < weights[x].length; y++) {
				for (int z = 0; z < weights[x][y].length; z++) {
					if (AKRandom.randomChance(.05)) {
						weights[x][y][z] = AKRandom.randomNumber(-1, 1);
					}
				}
			}
		}
		for (int x = 0; x < biases.length; x++) {
			for (int y = 0; y < biases[x].length; y++) {
				biases[x][y] = AKRandom.randomNumber(-1, 1);
			}
		}
		return new FCNeuralNetwork(weights, biases);
	}

	@Override
	public default List<Tuple2D<FCNeuralNetwork, FCNeuralNetwork>> selectCrossoverPartners(List<FCNeuralNetwork> population,
			GAEnvironment<FCNeuralNetwork> env) {
		return this.selectCrossoverPartnersSUS(population, env.getPopulation().size() / 3, env);
	}

	@Override
	public default List<FCNeuralNetwork> killOff(List<FCNeuralNetwork> population, int numToKill,
			GAEnvironment<FCNeuralNetwork> env) {
		return this.killOffWorst(population, numToKill, env);
	}

}
