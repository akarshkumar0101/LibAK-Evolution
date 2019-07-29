package machinelearning.ne.neat.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import machinelearning.ne.neat.genome.ConnectionGene;
import machinelearning.ne.neat.genome.Genome;
import machinelearning.neuralnet.InputSource;

public class NeuralNetwork {

	private final List<Neuron> inputNeurons;
	private final List<Neuron> outputNeurons;

	private final List<Neuron> hiddenNeurons;

	private boolean hasBias;

	// uses input array when the constructor inputSource is null
	private final double[] inputs;
	private final InputSource inputSource;

	public final long networkID;

	public NeuralNetwork(Genome geno, InputSource inputSource) {
		this.inputNeurons = new ArrayList<>();
		this.outputNeurons = new ArrayList<>();
		this.hiddenNeurons = new ArrayList<>();

		if (inputSource == null) {
			this.inputs = new double[geno.getBaseTemplate().numInputNodes()];
			this.inputSource = inputIndex -> NeuralNetwork.this.inputs[inputIndex];
		} else {
			this.inputSource = inputSource;
			this.inputs = null;
		}

		this.buildFromGeno(geno);

		this.networkID = geno.ID;
	}

	public NeuralNetwork(Genome geno) {
		this(geno, null);
	}

	public void calculate() {
		this.invalidateAll();
		for (Neuron outputNeuron : this.outputNeurons) {
			outputNeuron.calculate();
		}
	}

	public void calculate(double... inputs) {
		System.arraycopy(inputs, 0, this.inputs, 0, this.inputs.length);
		this.calculate();
	}

	public void invalidateAll() {
		for (Neuron neuron : this.outputNeurons) {
			neuron.invalidate();
		}
		for (Neuron neuron : this.inputNeurons) {
			neuron.invalidate();
		}
		for (Neuron neuron : this.hiddenNeurons) {
			neuron.invalidate();
		}
	}

	public void buildFromGeno(Genome geno) {
		this.hasBias = geno.getBaseTemplate().hasBias();

		Map<Integer, Neuron> neurons = new HashMap<>();

		if (geno.getBaseTemplate().hasBias()) {
			// str += "\t{Node 0, Type: BIAS}\n";
			BiasNeuron biasNeuron = new BiasNeuron(this);
			neurons.put(0, biasNeuron);
			this.inputNeurons.add(biasNeuron);
		}
		for (int i = 1; i <= geno.getBaseTemplate().numInputNodes(); i++) {
			// str += "\t{Node " + i + ", Type: INPUT}\n";
			final int inputI = i - 1;
			NEATInputNeuron inputNeuron = new NEATInputNeuron(this) {
				@Override
				public double getInput() {
					return NeuralNetwork.this.inputSource.getInput(inputI);
				}
			};
			neurons.put(i, inputNeuron);
			this.inputNeurons.add(inputNeuron);
		}
		for (int i = geno.getBaseTemplate().numInputNodes() + 1; i <= geno.getBaseTemplate().numInputNodes()
				+ geno.getBaseTemplate().numOutputNodes(); i++) {
			// str += "\t{Node " + i + ", Type: OUTPUT}\n";
			Neuron outputNeuron = new Neuron(this);
			neurons.put(i, outputNeuron);
			this.outputNeurons.add(outputNeuron);
		}
		for (int i = geno.getBaseTemplate().numInputNodes() + geno.getBaseTemplate().numOutputNodes() + 1; i <= geno
				.getBaseTemplate().numInputNodes() + geno.getBaseTemplate().numOutputNodes()
				+ geno.getNumHiddenNodes(); i++) {
			// str += "\t{Node " + i + ", Type: HIDDEN}\n";
			Neuron hiddenNeuron = new Neuron(this);
			neurons.put(i, hiddenNeuron);
			this.hiddenNeurons.add(hiddenNeuron);
		}

		for (ConnectionGene cg : geno.getConnectionGenes()) {
			if (cg.isEnabled()) {
				Neuron outputNeuron = neurons.get(cg.getOutputNodeID());
				Neuron inputNeuron = neurons.get(cg.getInputNodeID());
				outputNeuron.addConnection(inputNeuron, cg.getConnectionWeight());
			}
		}
	}

	public boolean hasBias() {
		return this.hasBias;
	}

	public List<Neuron> getInputNeurons() {
		return this.inputNeurons;
	}

	public List<Neuron> getOutputNeurons() {
		return this.outputNeurons;
	}

	public List<Neuron> getHiddenNeurons() {
		return this.hiddenNeurons;
	}
}
