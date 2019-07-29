package machinelearning.geneticalgorithm;

import java.util.ArrayList;
import java.util.List;

//P = Phenotype (animal), G = Genotype (chromosonne) (the genetic encoding of solution)
public interface PhenotypeGeneticTrainer<P, G> extends GeneticTrainer<G> {

	public abstract G getGenotype(P pheno);

	public abstract P getPhenotype(G geno);

	public abstract double calculatePhenoFitness(P a, GAEnvironment<G> env);

	public default List<Double> calculatePhenoFitness(List<P> as, GAEnvironment<G> env) {
		List<Double> fitnesses = new ArrayList<>(as.size());
		for (int i = 0; i < as.size(); i++) {
			fitnesses.add(this.calculatePhenoFitness(as.get(i), env));
		}
		return fitnesses;
	}

	@Override
	public default double calculateFitness(G a, GAEnvironment<G> env) {
		P pheno = this.getPhenotype(a);
		return this.calculatePhenoFitness(pheno, env);
	}

	@Override
	public default List<Double> calculateFitness(List<G> as, GAEnvironment<G> env) {
		List<P> phenos = new ArrayList<>(as.size());
		for (G geno : as) {
			P pheno = this.getPhenotype(geno);
			phenos.add(pheno);
		}
		return this.calculatePhenoFitness(phenos, env);
	}

}
