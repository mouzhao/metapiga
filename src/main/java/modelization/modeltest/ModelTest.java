package modelization.modeltest;

import java.util.Map;
import java.util.Set;
import metapiga.RateParameter;
import metapiga.modelization.Charset;
import metapiga.parameters.Parameters.EvaluationDistribution;
import metapiga.parameters.Parameters.EvaluationModel;
import metapiga.parameters.Parameters.EvaluationStateFrequencies;

public abstract interface ModelTest
{
  public abstract void testModels(int paramInt, Set<Parameters.EvaluationModel> paramSet)
    throws Exception;

  public abstract String getResults();

  public abstract void stop();

  public abstract Parameters.EvaluationModel getBestModel();

  public abstract Map<RateParameter, Double> getBestRateParameters(Charset paramCharset)
    throws Exception;

  public abstract Parameters.EvaluationDistribution getBestDistribution();

  public abstract double getBestDistributionShape(Charset paramCharset)
    throws Exception;

  public abstract double getBestInvariant(Charset paramCharset)
    throws Exception;

  public abstract boolean hasBestInvariant();

  public abstract Parameters.EvaluationStateFrequencies getBestStateFrequencies();
}

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.modelization.modeltest.ModelTest
 * JD-Core Version:    0.6.2
 */