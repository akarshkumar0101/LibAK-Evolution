package machinelearning.ne.neat.network;

import java.util.HashMap;
import java.util.Map;

public class Neuron {
	protected double activation;

	protected boolean calculated;

	protected final Map<Neuron, Double> prevConnections;

	protected final NeuralNetwork network;

	Neuron(NeuralNetwork network) {
		this.network = network;
		this.calculated = false;

		this.prevConnections = new HashMap<>();
	}

	public void addConnection(Neuron prevNeuron, double connectionWeight) {
		this.prevConnections.put(prevNeuron, connectionWeight);
	}

	public void calculate() {
		if (this.calculated) {
			return;
		}
		this.calculated = true;
		double input = 0;

		for (Neuron neuron : this.prevConnections.keySet()) {
			double connectionWeight = this.prevConnections.get(neuron);
			if (!neuron.calculated) {
				neuron.calculate();
			}
			input += neuron.activation * connectionWeight;
		}
		this.activation = input;

		this.activationFunc();
	}

	public void invalidate() {
		this.calculated = false;
	}

	public static double sigmoidFunc(double input) {
		return 1 / (1 + Math.exp(-input));
	}

	public static double steepSigmoidFunc(double input) {
		return 1 / (1 + Math.exp(-5 * input));
	}

	public static double ReLU(double input) {
		return Math.max(0.0, input);
	}

	public static double linearActivation(double input, double scale) {
		return input * scale;
	}

	public void activationFunc() {
		this.activation = Neuron.steepSigmoidFunc(this.activation);
	}

	public double getActivation() {
		return this.activation;
	}

	public void setActivation(double activation) {
		this.activation = activation;
	}

	public boolean isCalculated() {
		return this.calculated;
	}

	public void setCalculated(boolean calculated) {
		this.calculated = calculated;
	}

	public Map<Neuron, Double> getPrevConnections() {
		return this.prevConnections;
	}

	public NeuralNetwork getNetwork() {
		return this.network;
	}
}

abstract class NEATInputNeuron extends Neuron {
	public NEATInputNeuron(NeuralNetwork network) {
		super(network);
	}

	@Override
	public void calculate() {
		this.activation = this.getInput();
		this.activationFunc();
	}

	public abstract double getInput();
}

class BiasNeuron extends NEATInputNeuron {

	public BiasNeuron(NeuralNetwork network) {
		super(network);
	}

	@Override
	public void calculate() {
		this.activation = 1.0;
	}

	@Override
	public double getInput() {
		return 0;
	}

}
