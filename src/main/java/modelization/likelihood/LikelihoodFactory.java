/*    */ package modelization.likelihood;
/*    */ 
/*    */ import java.util.Map;
/*    */ import metapiga.RateParameter;
/*    */ import metapiga.modelization.Dataset.Partition;
/*    */ import metapiga.modelization.data.DataType;
/*    */ import metapiga.parameters.Parameters.EvaluationDistribution;
/*    */ import metapiga.parameters.Parameters.EvaluationModel;
/*    */ import metapiga.parameters.Parameters.EvaluationRate;
/*    */ import metapiga.parameters.Parameters.EvaluationStateFrequencies;
/*    */ import metapiga.trees.Tree;
/*    */ import metapiga.trees.exceptions.UnrootableTreeException;
/*    */ 
/*    */ public class LikelihoodFactory
/*    */ {
/*    */   public static Likelihood makeLikelihoodGpu(Dataset.Partition partition, Parameters.EvaluationRate rate, Parameters.EvaluationModel model, Parameters.EvaluationDistribution distribution, double distributionShape, double pinv, double apRate, Map<RateParameter, Double> rateParameters, Parameters.EvaluationStateFrequencies stateFreq, Tree tree, int numSubsets)
/*    */     throws UnrootableTreeException
/*    */   {
/* 19 */     int numNodes = tree.getNumOfNodes();
/* 20 */     int numCategories = distribution == Parameters.EvaluationDistribution.NONE ? 1 : numSubsets;
/* 21 */     int numCharacters = partition.getCompressedNChar();
/* 22 */     int numStates = partition.getDataType().numOfStates();
/*    */ 
/* 24 */     SequenceLinearArray seq = new SequenceLinearArray(numNodes, numCategories, numCharacters, numStates, 32);
/* 25 */     return new LikelihoodGpu(partition, rate, model, distribution, distributionShape, pinv, apRate, rateParameters, stateFreq, tree, numSubsets, seq);
/*    */   }
/*    */ 
/*    */   public static Likelihood makeLikelihoodClassic(Dataset.Partition partition, Parameters.EvaluationRate rate, Parameters.EvaluationModel model, Parameters.EvaluationDistribution distribution, double distributionShape, double pinv, double apRate, Map<RateParameter, Double> rateParameters, Parameters.EvaluationStateFrequencies stateFreq, Tree tree, int numSubsets)
/*    */     throws UnrootableTreeException
/*    */   {
/* 33 */     int numNodes = tree.getNumOfNodes();
/* 34 */     int numCategories = distribution == Parameters.EvaluationDistribution.NONE ? 1 : numSubsets;
/* 35 */     int numCharacters = partition.getCompressedNChar();
/* 36 */     int numStates = partition.getDataType().numOfStates();
/* 37 */     SequenceArrays4Dimension seq = new SequenceArrays4Dimension(numNodes, numCategories, numCharacters, numStates);
/* 38 */     return new LikelihoodClassic(partition, rate, model, distribution, distributionShape, pinv, apRate, rateParameters, stateFreq, tree, numSubsets, seq);
/*    */   }
/*    */   public static Likelihood makeLikelihoodCopy(Likelihood L, Tree tree) throws UnrootableTreeException {
/* 41 */     if ((L instanceof LikelihoodGpu))
/* 42 */       return new LikelihoodGpu((LikelihoodGpu)L, tree);
/* 43 */     if ((L instanceof LikelihoodClassic))
/*    */     {
/* 45 */       return new LikelihoodClassic((LikelihoodClassic)L, tree);
/*    */     }
/* 47 */     return null;
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.modelization.likelihood.LikelihoodFactory
 * JD-Core Version:    0.6.2
 */