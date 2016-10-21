/*      */ package metapiga.trees;
/*      */ 
/*      */ import com.google.common.collect.BiMap;
/*      */ import com.google.common.collect.HashBiMap;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.BitSet;
/*      */ import java.util.Collection;
/*      */ import java.util.Collections;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Set;
/*      */ import java.util.Stack;
/*      */ import java.util.TreeMap;
/*      */ import metapiga.RateParameter;
/*      */ import metapiga.modelization.Charset;
/*      */ import metapiga.modelization.Dataset;
/*      */ import metapiga.parameters.Parameters;
/*      */ import metapiga.parameters.Parameters.CPConsensus;
/*      */ import metapiga.trees.exceptions.BranchNotFoundException;
/*      */ import metapiga.trees.exceptions.NoInclusionException;
/*      */ import metapiga.trees.exceptions.NullAncestorException;
/*      */ import metapiga.trees.exceptions.TooManyNeighborsException;
/*      */ import metapiga.trees.exceptions.UncompatibleOutgroupException;
/*      */ import metapiga.trees.exceptions.UnknownTaxonException;
/*      */ import metapiga.trees.exceptions.UnrootableTreeException;
/*      */ import metapiga.utilities.Tools;
/*      */ 
/*      */ public class Consensus
/*      */ {
/*      */   private List<Map<String, BiPartition>> bipartitions;
/*      */   private Map<Node, Map<Node, BiPartition>> branches;
/*      */   private Map<String, Set<Tree>> bipartitionExistInTree;
/*      */   private Set<BiPartition> consensus;
/*      */   private int differentBipartitionCount;
/*      */   private int treeCount;
/*      */   private Map<String, Integer> taxonId;
/*      */   private Map<Integer, String> taxa;
/*      */   private final int numTaxa;
/*      */   private Map<Charset, Map<RateParameter, Double>> rateParameters;
/*      */   private Map<Charset, Double> evaluationDistributionShape;
/*      */   private Map<Charset, Double> evaluationPInv;
/*      */ 
/*      */   public Consensus(Tree tree, Dataset dataset)
/*      */   {
/*  384 */     this(Arrays.asList(new Tree[] { tree }), dataset);
/*      */   }
/*      */ 
/*      */   public Consensus(Tree tree1, Tree tree2, Dataset dataset) {
/*  388 */     this(Arrays.asList(new Tree[] { tree1, tree2 }), dataset);
/*      */   }
/*      */ 
/*      */   public Consensus(Collection<Tree> trees, Dataset dataset)
/*      */   {
/*  399 */     this.taxonId = new HashMap();
/*  400 */     this.taxa = new HashMap();
/*  401 */     for (int i = 0; i < dataset.getNTax(); i++) {
/*  402 */       this.taxonId.put(dataset.getTaxon(i), Integer.valueOf(i));
/*  403 */       this.taxa.put(Integer.valueOf(i), dataset.getTaxon(i));
/*      */     }
/*  405 */     this.numTaxa = this.taxa.size();
/*  406 */     this.differentBipartitionCount = 0;
/*  407 */     this.bipartitions = new ArrayList(this.numTaxa);
/*  408 */     for (int i = 0; i < this.numTaxa; i++) {
/*  409 */       this.bipartitions.add(new HashMap());
/*      */     }
/*  411 */     this.branches = new HashMap();
/*  412 */     this.bipartitionExistInTree = new HashMap();
/*  413 */     this.treeCount = trees.size();
/*  414 */     this.rateParameters = new TreeMap();
/*  415 */     this.evaluationDistributionShape = new TreeMap();
/*  416 */     this.evaluationPInv = new TreeMap();
/*  417 */     for (Charset c : dataset.getPartitionCharsets()) {
/*  418 */       Map param = new TreeMap();
/*  419 */       for (RateParameter r : ((Tree)trees.iterator().next()).getEvaluationRateParameters(c).keySet())
/*  420 */         param.put(r, Double.valueOf(0.0D));
/*  421 */       this.rateParameters.put(c, param);
/*  422 */       this.evaluationDistributionShape.put(c, Double.valueOf(0.0D));
/*  423 */       this.evaluationPInv.put(c, Double.valueOf(0.0D));
/*      */     }
/*      */     Charset c;
/*  425 */     for (??? = trees.iterator(); ???.hasNext(); 
/*  431 */       ???.hasNext())
/*      */     {
/*  425 */       Tree tree = (Tree)???.next();
/*  426 */       Node access = tree.getAccessNode();
/*  427 */       for (Node.Neighbor neighbor : access.getNeighborKeys()) {
/*  428 */         Branch b = new Branch(access, neighbor).getMirrorBranch();
/*  429 */         addBiPartition(buildBiPartition(tree, b), tree, b);
/*      */       }
/*  431 */       ??? = tree.getPartitions().iterator(); continue; c = (Charset)???.next();
/*  432 */       for (Map.Entry e : tree.getEvaluationRateParameters(c).entrySet()) {
/*  433 */         ((Map)this.rateParameters.get(c)).put((RateParameter)e.getKey(), Double.valueOf(((Double)((Map)this.rateParameters.get(c)).get(e.getKey())).doubleValue() + ((Double)e.getValue()).doubleValue() / this.treeCount));
/*      */       }
/*  435 */       this.evaluationDistributionShape.put(c, Double.valueOf(((Double)this.evaluationDistributionShape.get(c)).doubleValue() + tree.getEvaluationGammaShape(c) / this.treeCount));
/*  436 */       this.evaluationPInv.put(c, Double.valueOf(((Double)this.evaluationPInv.get(c)).doubleValue() + tree.getEvaluationPInv(c) / this.treeCount));
/*      */     }
/*      */ 
/*  439 */     for (??? = this.bipartitions.iterator(); ???.hasNext(); 
/*  440 */       c.hasNext())
/*      */     {
/*  439 */       Map map = (Map)???.next();
/*  440 */       c = map.values().iterator(); continue; BiPartition p = (BiPartition)c.next();
/*  441 */       p.strength = (p.count / this.treeCount);
/*  442 */       p.branchLength /= p.count;
/*      */     }
/*      */   }
/*      */ 
/*      */   public Consensus(Collection<Tree> trees, Dataset dataset, Parameters.CPConsensus consensusType)
/*      */   {
/*  455 */     this(trees, dataset);
/*  456 */     setConsensusType(consensusType);
/*      */   }
/*      */ 
/*      */   private void putBiPartitionInBranches(Branch branch, BiPartition bipartition)
/*      */   {
/*  464 */     Node nodeA = branch.getNode();
/*  465 */     Node nodeB = branch.getOtherNode();
/*  466 */     if (!this.branches.containsKey(nodeA)) {
/*  467 */       this.branches.put(nodeA, new HashMap());
/*      */     }
/*  469 */     Map subMap = (Map)this.branches.get(nodeA);
/*  470 */     subMap.put(nodeB, bipartition);
/*  471 */     if (!this.branches.containsKey(nodeB)) {
/*  472 */       this.branches.put(nodeB, new HashMap());
/*      */     }
/*  474 */     subMap = (Map)this.branches.get(nodeB);
/*  475 */     subMap.put(nodeA, bipartition);
/*      */   }
/*      */ 
/*      */   public synchronized BiPartition getBiPartition(Branch branch)
/*      */   {
/*  484 */     return (BiPartition)((Map)this.branches.get(branch.getNode())).get(branch.getOtherNode());
/*      */   }
/*      */ 
/*      */   private void putBiPartitionExistInTree(BiPartition bipartition, Tree tree) {
/*  488 */     String key = bipartition.toString();
/*  489 */     if (!this.bipartitionExistInTree.containsKey(key)) this.bipartitionExistInTree.put(key, new HashSet());
/*  490 */     ((Set)this.bipartitionExistInTree.get(key)).add(tree);
/*      */   }
/*      */ 
/*      */   public boolean isBiPartitionExistsInTree(BiPartition bipartition, Tree tree) {
/*  494 */     return ((Set)this.bipartitionExistInTree.get(bipartition.toString())).contains(tree);
/*      */   }
/*      */ 
/*      */   public void setConsensusType(Parameters.CPConsensus consensusType) {
/*  498 */     this.consensus = new HashSet();
/*      */     Iterator localIterator2;
/*  499 */     for (Iterator localIterator1 = this.bipartitions.iterator(); localIterator1.hasNext(); 
/*  500 */       localIterator2.hasNext())
/*      */     {
/*  499 */       Map map = (Map)localIterator1.next();
/*  500 */       localIterator2 = map.values().iterator(); continue; BiPartition p = (BiPartition)localIterator2.next();
/*  501 */       if (p.cardinality > 1)
/*  502 */         if (consensusType == Parameters.CPConsensus.STRICT) {
/*  503 */           if (p.strength == 1.0D) this.consensus.add(p); 
/*      */         }
/*  504 */         else if ((consensusType == Parameters.CPConsensus.STOCHASTIC) && 
/*  505 */           (p.count > 1) && (Math.random() < p.strength)) this.consensus.add(p);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean isInConsensus(Branch branch)
/*      */   {
/*  521 */     BiPartition p = getBiPartition(branch);
/*  522 */     for (BiPartition e : this.consensus) {
/*  523 */       if (e.equals(p)) return true;
/*      */     }
/*  525 */     return false;
/*      */   }
/*      */ 
/*      */   private BiPartition buildBiPartition(Tree tree, Branch branch)
/*      */   {
/*  537 */     if (branch.getNode().isLeaf()) {
/*  538 */       return new BiPartition(branch.getNode());
/*      */     }
/*  540 */     Set set = new HashSet();
/*  541 */     for (Branch b : branch.getAllNeighborBranches()) {
/*  542 */       BiPartition p = buildBiPartition(tree, b);
/*  543 */       addBiPartition(p, tree, b);
/*  544 */       set.add(p);
/*      */     }
/*  546 */     BiPartition p = new BiPartition(set, branch.getLength());
/*  547 */     return p;
/*      */   }
/*      */ 
/*      */   private void addBiPartition(BiPartition bipartition, Tree tree, Branch branch)
/*      */   {
/*      */     BiPartition p;
/*      */     BiPartition p;
/*  553 */     if (bipartition.cardinality > this.numTaxa / 2)
/*  554 */       p = bipartition.mirror();
/*      */     else {
/*  556 */       p = bipartition;
/*      */     }
/*  558 */     putBiPartitionExistInTree(p, tree);
/*  559 */     Map map = (Map)this.bipartitions.get(p.cardinality);
/*  560 */     if (map.containsKey(p.toString())) {
/*  561 */       BiPartition q = (BiPartition)map.get(p.toString());
/*  562 */       q.count += 1;
/*  563 */       q.branchLength += p.branchLength;
/*  564 */       putBiPartitionInBranches(branch, q);
/*  565 */     } else if ((p.cardinality == this.numTaxa / 2) && (map.containsKey(p.mirror().toString()))) {
/*  566 */       BiPartition q = (BiPartition)map.get(p.mirror().toString());
/*  567 */       q.count += 1;
/*  568 */       q.branchLength += p.branchLength;
/*  569 */       putBiPartitionInBranches(branch, q);
/*      */     } else {
/*  571 */       this.differentBipartitionCount += 1;
/*  572 */       putBiPartitionInBranches(branch, p);
/*  573 */       map.put(p.toString(), p);
/*      */     }
/*      */   }
/*      */ 
/*      */   public Tree getConsensusTree(Parameters parameters)
/*      */     throws UnknownTaxonException, TooManyNeighborsException, UncompatibleOutgroupException, NoInclusionException
/*      */   {
/*  587 */     Tree tree = new Tree("Consensus tree", parameters);
/*  588 */     TreeMap P = new TreeMap();
/*  589 */     List majoritaryP = new ArrayList();
/*  590 */     List minoritaryP = new ArrayList();
/*  591 */     for (int card = 0; card < this.numTaxa; card++) {
/*  592 */       minoritaryP.addAll(((Map)this.bipartitions.get(card)).values());
/*  593 */       Set setP = new HashSet();
/*  594 */       for (BiPartition bp : ((Map)this.bipartitions.get(card)).values()) {
/*  595 */         if ((bp.strength > 0.5D) || (bp.strength == 1.0D)) {
/*  596 */           setP.add(bp);
/*  597 */           majoritaryP.add(bp);
/*  598 */           minoritaryP.remove(bp);
/*      */         }
/*      */       }
/*  601 */       if (!setP.isEmpty()) P.put(Integer.valueOf(card), setP);
/*      */     }
/*      */ 
/*  604 */     Collections.sort(minoritaryP);
/*      */ 
/*  606 */     while (!minoritaryP.isEmpty()) {
/*  607 */       BiPartition a = (BiPartition)minoritaryP.remove(0);
/*      */ 
/*  609 */       boolean isCompatible = true;
/*  610 */       for (BiPartition p : majoritaryP) {
/*  611 */         if (!a.isCompatible(p)) {
/*  612 */           isCompatible = false;
/*  613 */           break;
/*      */         }
/*      */       }
/*  616 */       if (isCompatible)
/*      */       {
/*  618 */         if (!P.containsKey(Integer.valueOf(a.cardinality))) {
/*  619 */           Set set = new HashSet();
/*  620 */           P.put(Integer.valueOf(a.cardinality), set);
/*      */         }
/*  622 */         ((Set)P.get(Integer.valueOf(a.cardinality))).add(a);
/*      */ 
/*  624 */         for (Iterator it = minoritaryP.iterator(); it.hasNext(); ) {
/*  625 */           if (!a.isCompatible((BiPartition)it.next())) it.remove();
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  630 */     BiPartition p = (BiPartition)((Set)P.get(P.lastKey())).iterator().next();
/*  631 */     ((Set)P.get(P.lastKey())).remove(p);
/*  632 */     ConsensusNode A = new ConsensusNode(p);
/*  633 */     buildConsensusTree(tree, A, P, p);
/*  634 */     BiPartition q = p.mirror();
/*  635 */     ConsensusNode root = new ConsensusNode(q);
/*  636 */     Node.Neighbor neighbor = root.addNeighbor(A);
/*  637 */     root.setBranchLength(neighbor, p.branchLength);
/*  638 */     root.setBranchStrength(neighbor, p.strength);
/*  639 */     buildConsensusTree(tree, root, P, q);
/*  640 */     tree.addNode(A);
/*  641 */     tree.addNode(root);
/*  642 */     tree.setAccessNode(root);
/*  643 */     tree.setOutgroup(parameters.outgroup);
/*  644 */     for (Charset c : tree.getPartitions()) {
/*  645 */       for (Map.Entry e : ((Map)this.rateParameters.get(c)).entrySet()) {
/*  646 */         tree.setEvaluationRateParameter(c, (RateParameter)e.getKey(), ((Double)e.getValue()).doubleValue());
/*      */       }
/*  648 */       tree.setEvaluationDistributionShape(c, ((Double)this.evaluationDistributionShape.get(c)).doubleValue());
/*  649 */       tree.setEvaluationPInv(c, ((Double)this.evaluationPInv.get(c)).doubleValue());
/*      */     }
/*  651 */     return tree;
/*      */   }
/*      */ 
/*      */   private void buildConsensusTree(Tree tree, Node father, TreeMap<Integer, Set<BiPartition>> P, BiPartition p) throws UnknownTaxonException, TooManyNeighborsException, NoInclusionException
/*      */   {
/*  656 */     if (p.cardinality <= 1) return;
/*  657 */     int currentCardinality = p.cardinality;
/*      */ 
/*  659 */     while (currentCardinality > 0) {
/*  660 */       int key = ((Integer)P.lowerKey(Integer.valueOf(currentCardinality))).intValue();
/*  661 */       for (BiPartition q : (Set)P.get(Integer.valueOf(key))) {
/*  662 */         if (q.isIncludedIn(p)) {
/*  663 */           ((Set)P.get(Integer.valueOf(key))).remove(q);
/*  664 */           ConsensusNode bigChild = new ConsensusNode(q);
/*  665 */           Node.Neighbor neighbor = bigChild.addNeighbor(father);
/*  666 */           bigChild.setBranchLength(neighbor, q.branchLength);
/*  667 */           bigChild.setBranchStrength(neighbor, q.strength);
/*  668 */           if (q.cardinality > 1) buildConsensusTree(tree, bigChild, P, q);
/*  669 */           tree.addNode(bigChild);
/*  670 */           currentCardinality = p.cardinality - q.cardinality;
/*      */ 
/*  672 */           if (P.containsKey(Integer.valueOf(currentCardinality))) {
/*  673 */             for (BiPartition r : (Set)P.get(Integer.valueOf(currentCardinality))) {
/*  674 */               if (r.isIncludedIn(p)) {
/*  675 */                 ((Set)P.get(Integer.valueOf(currentCardinality))).remove(r);
/*  676 */                 ConsensusNode smallChild = new ConsensusNode(r);
/*  677 */                 neighbor = smallChild.addNeighbor(father);
/*  678 */                 smallChild.setBranchLength(neighbor, r.branchLength);
/*  679 */                 smallChild.setBranchStrength(neighbor, r.strength);
/*  680 */                 if (r.cardinality > 1) buildConsensusTree(tree, smallChild, P, r);
/*  681 */                 tree.addNode(smallChild);
/*  682 */                 return;
/*      */               }
/*      */ 
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*  690 */           BiPartition fictiveBipartition = p.complement(q);
/*  691 */           ConsensusNode fictiveChild = new ConsensusNode(fictiveBipartition);
/*  692 */           neighbor = fictiveChild.addNeighbor(father);
/*  693 */           fictiveChild.setBranchLength(neighbor, 0.0D);
/*  694 */           fictiveChild.setBranchStrength(neighbor, 0.0D);
/*  695 */           if (fictiveBipartition.cardinality > 1) buildConsensusTree(tree, fictiveChild, P, fictiveBipartition);
/*  696 */           tree.addNode(fictiveChild);
/*  697 */           return;
/*      */         }
/*      */       }
/*  700 */       currentCardinality = key;
/*      */     }
/*      */   }
/*      */ 
/*      */   public String showPartitions() {
/*  705 */     String s = "";
/*      */     Iterator localIterator2;
/*  706 */     for (Iterator localIterator1 = this.bipartitions.iterator(); localIterator1.hasNext(); 
/*  707 */       localIterator2.hasNext())
/*      */     {
/*  706 */       Map map = (Map)localIterator1.next();
/*  707 */       localIterator2 = map.values().iterator(); continue; BiPartition p = (BiPartition)localIterator2.next();
/*  708 */       s = s + p.toTaxa() + " - (" + p.cardinality + ") " + p.strength * 100.0D + "%" + "\n";
/*      */     }
/*      */ 
/*  711 */     return s;
/*      */   }
/*      */ 
/*      */   public String showConsensus() {
/*  715 */     String s = "";
/*  716 */     for (BiPartition p : this.consensus) {
/*  717 */       s = s + p.toTaxa() + " - (" + p.count + "/" + this.treeCount + ") " + p.strength * 100.0D + "%" + "\n";
/*      */     }
/*  719 */     return s;
/*      */   }
/*      */ 
/*      */   public double getConsensusCoverage()
/*      */   {
/*  727 */     double sum = 0.0D;
/*  728 */     for (BiPartition p : this.consensus) {
/*  729 */       sum += p.strength;
/*      */     }
/*  731 */     return sum / (this.numTaxa - 3);
/*      */   }
/*      */ 
/*      */   public boolean acceptNNI(Branch candidate)
/*      */   {
/*  741 */     return !isInConsensus(candidate);
/*      */   }
/*      */ 
/*      */   public List<Branch> getNNIValidCandidates(Tree T)
/*      */     throws NullAncestorException
/*      */   {
/*  753 */     List candidates = new ArrayList();
/*  754 */     for (Node inode : T.getIngroupInodes()) {
/*  755 */       if (inode != T.getRoot()) {
/*  756 */         Branch b = new Branch(inode, inode.getAncestorKey());
/*  757 */         if (!isInConsensus(b)) candidates.add(b);
/*      */       }
/*      */     }
/*  760 */     return candidates;
/*      */   }
/*      */ 
/*      */   public List<Branch> getSPRValidTargets(Tree T, Branch candidate)
/*      */   {
/*  772 */     List candidates = buildSPRValidTargets(T, candidate, !T.isInOutgroup(candidate.getNode()));
/*  773 */     for (Iterator it = candidates.iterator(); it.hasNext(); ) {
/*  774 */       Branch b = (Branch)it.next();
/*  775 */       for (Branch neigh : candidate.getAllNeighborBranches()) {
/*  776 */         if (b.equals(neigh)) {
/*  777 */           it.remove();
/*  778 */           break;
/*      */         }
/*      */       }
/*      */     }
/*  782 */     return candidates;
/*      */   }
/*      */ 
/*      */   private List<Branch> buildSPRValidTargets(Tree T, Branch currentBranch, boolean ingroup)
/*      */   {
/*  794 */     List candidates = new ArrayList();
/*  795 */     for (Branch b : currentBranch.getAllNeighborBranches()) {
/*  796 */       candidates.add(b);
/*  797 */       if (((!ingroup) || (!T.isInOutgroup(b.getNode()))) && (!isInConsensus(b))) {
/*  798 */         candidates.addAll(buildSPRValidTargets(T, b, ingroup));
/*      */       }
/*      */     }
/*  801 */     return candidates;
/*      */   }
/*      */ 
/*      */   public boolean acceptTXS(Tree T, Collection<Node> taxas)
/*      */   {
/*  811 */     TaxaSet txs = new TaxaSet(taxas);
/*  812 */     for (BiPartition p : this.consensus) {
/*  813 */       if ((isBiPartitionExistsInTree(p, T)) && 
/*  814 */         (!p.hasOnSamePartition(txs))) return false;
/*      */     }
/*      */ 
/*  817 */     return true;
/*      */   }
/*      */ 
/*      */   public List<Node> getTXSValidCandidates(Tree T, int txsParam, List<Node> availableLeaves)
/*      */     throws NoInclusionException, NullAncestorException
/*      */   {
/*  832 */     List nodes = new ArrayList();
/*  833 */     if (this.consensus.isEmpty()) {
/*  834 */       return availableLeaves;
/*      */     }
/*  836 */     List taxasets = new ArrayList();
/*  837 */     for (BiPartition p : this.consensus) {
/*  838 */       if (isBiPartitionExistsInTree(p, T)) {
/*  839 */         TaxaSet txs = p.getBiggestPartition();
/*  840 */         int pos = 0;
/*  841 */         while ((pos < taxasets.size()) && (txs.cardinality > ((TaxaSet)taxasets.get(pos)).cardinality)) {
/*  842 */           pos++;
/*      */         }
/*  844 */         taxasets.add(pos, txs);
/*  845 */         txs = p.getSmallestPartition();
/*  846 */         pos = 0;
/*  847 */         while ((pos < taxasets.size()) && (txs.cardinality > ((TaxaSet)taxasets.get(pos)).cardinality)) {
/*  848 */           pos++;
/*      */         }
/*  850 */         taxasets.add(pos, txs);
/*      */       }
/*      */     }
/*  853 */     for (int i = 0; i < taxasets.size(); i++) {
/*  854 */       TaxaSet txsi = (TaxaSet)taxasets.get(i);
/*  855 */       for (int j = i + 1; j < taxasets.size(); j++) {
/*  856 */         TaxaSet txsj = (TaxaSet)taxasets.get(j);
/*  857 */         if (txsi.isIncludedIn(txsj)) {
/*  858 */           taxasets.set(j, txsj.complement(txsi));
/*      */         }
/*      */       }
/*      */     }
/*  862 */     int numTaxa = 0;
/*  863 */     TaxaSet availableTaxas = new TaxaSet(availableLeaves);
/*  864 */     for (Iterator it = taxasets.iterator(); it.hasNext(); ) {
/*  865 */       TaxaSet txs = (TaxaSet)it.next();
/*  866 */       txs.keepTaxas(availableTaxas);
/*  867 */       if (txs.cardinality < txsParam) {
/*  868 */         it.remove();
/*  869 */       } else if (txs.cardinality == 2)
/*      */       {
/*  871 */         List l = txs.getTaxas(T);
/*  872 */         if (((Node)l.get(0)).getAncestorNode() == ((Node)l.get(1)).getAncestorNode())
/*  873 */           it.remove();
/*      */         else
/*  875 */           numTaxa += txs.cardinality;
/*      */       }
/*      */       else {
/*  878 */         numTaxa += txs.cardinality;
/*      */       }
/*      */     }
/*  881 */     if (taxasets.size() > 0) {
/*  882 */       int rand = Tools.randInt(numTaxa);
/*  883 */       int count = 0;
/*  884 */       for (TaxaSet txs : taxasets) {
/*  885 */         count += txs.cardinality;
/*  886 */         if (rand < count) return txs.getTaxas(T);
/*      */       }
/*  888 */       return ((TaxaSet)taxasets.get(Tools.randInt(taxasets.size()))).getTaxas(T);
/*      */     }
/*  890 */     return nodes;
/*      */   }
/*      */ 
/*      */   public boolean acceptSTS(Tree T, Collection<Node> inodes)
/*      */     throws UnrootableTreeException
/*      */   {
/*  904 */     Set taxas = new HashSet();
/*      */     Iterator localIterator2;
/*      */     Node node;
/*  905 */     for (Iterator localIterator1 = inodes.iterator(); localIterator1.hasNext(); 
/*  906 */       localIterator2.hasNext())
/*      */     {
/*  905 */       Node inode = (Node)localIterator1.next();
/*  906 */       localIterator2 = T.getPreorderTraversal(inode).iterator(); continue; node = (Node)localIterator2.next();
/*  907 */       if (node.isLeaf()) taxas.add(node);
/*      */     }
/*      */ 
/*  910 */     TaxaSet txs = new TaxaSet(taxas);
/*  911 */     for (BiPartition p : this.consensus) {
/*  912 */       if ((isBiPartitionExistsInTree(p, T)) && 
/*  913 */         (!p.isIncludedIn(txs)) && 
/*  914 */         (!p.hasOnSamePartition(txs))) {
/*  915 */         return false;
/*      */       }
/*      */     }
/*      */ 
/*  919 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean recombination(Tree T, Tree T2)
/*      */     throws BranchNotFoundException, UnrootableTreeException, NullAncestorException, TooManyNeighborsException
/*      */   {
/*  938 */     if (T != T2) {
/*  939 */       List branches = T.getBranches();
/*  940 */       Branch b = null;
/*  941 */       BiPartition P = null;
/*  942 */       while ((b == null) && (!branches.isEmpty())) {
/*  943 */         b = (Branch)branches.remove(Tools.randInt(branches.size()));
/*  944 */         if ((b.isTipBranch()) || (T.isInOutgroup(b.getNode())) || (T.isInOutgroup(b.getOtherNode()))) {
/*  945 */           b = null;
/*      */         } else {
/*  947 */           P = getBiPartition(b);
/*  948 */           if ((P.count < 2) || (P.cardinality < 3) || (!isBiPartitionExistsInTree(P, T2)))
/*  949 */             b = null;
/*      */         }
/*      */       }
/*  952 */       if (b != null) {
/*  953 */         recombine(T, T2, b, P);
/*  954 */         return true;
/*      */       }
/*      */     }
/*  957 */     return false;
/*      */   }
/*      */ 
/*      */   public String hybridization(Tree T, Tree[] population)
/*      */     throws BranchNotFoundException, UnrootableTreeException, NullAncestorException, TooManyNeighborsException
/*      */   {
/*  971 */     List branches = T.getBranches();
/*  972 */     Branch b = null;
/*  973 */     BiPartition P = null;
/*  974 */     Tree T2 = null;
/*      */     Iterator localIterator;
/*  975 */     label197: for (; (T2 == null) && (!branches.isEmpty()); 
/*  981 */       localIterator.hasNext())
/*      */     {
/*  976 */       b = (Branch)branches.remove(Tools.randInt(branches.size()));
/*  977 */       if ((b.isTipBranch()) || (T.isInOutgroup(b.getNode())) || (T.isInOutgroup(b.getOtherNode()))) break label197;
/*  978 */       P = getBiPartition(b);
/*  979 */       if ((P.count <= 1) || (P.cardinality <= 2)) break label197;
/*  980 */       Set trees = (Set)this.bipartitionExistInTree.get(P.toString());
/*  981 */       localIterator = trees.iterator(); continue; Tree tree = (Tree)localIterator.next();
/*  982 */       if (T != tree) {
/*  983 */         boolean insidePop = false;
/*  984 */         for (int i = 0; i < population.length; i++) {
/*  985 */           if (tree == population[i]) {
/*  986 */             insidePop = true;
/*  987 */             break;
/*      */           }
/*      */         }
/*  990 */         if (!insidePop) T2 = tree;
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  996 */     if (T2 != null) {
/*  997 */       recombine(T, T2, b, P);
/*  998 */       return "Recombination with " + T2.getName();
/*      */     }
/* 1000 */     return "No recombination possible";
/*      */   }
/*      */ 
/*      */   private void recombine(Tree T, Tree T2, Branch branchInT, BiPartition P)
/*      */     throws BranchNotFoundException, UnrootableTreeException, NullAncestorException, TooManyNeighborsException
/*      */   {
/* 1006 */     T.unroot();
/*      */ 
/* 1008 */     List branchesOfT2 = T2.getBranches();
/* 1009 */     Branch branchInT2 = null;
/* 1010 */     String Pstring = P.toString();
/* 1011 */     for (Branch b : branchesOfT2) {
/* 1012 */       if ((!b.isTipBranch()) && (((BiPartition)((Map)this.branches.get(b.getNode())).get(b.getOtherNode())).toString().equals(Pstring))) {
/* 1013 */         branchInT2 = b;
/* 1014 */         break;
/*      */       }
/*      */     }
/* 1017 */     if (branchInT2 == null) throw new BranchNotFoundException("Recombination", branchInT, T, T2);
/*      */ 
/* 1023 */     Set set1 = T.getAllNodesUnderNeighbor(branchInT.getNode(), branchInT.getNeighbor());
/* 1024 */     Set set2 = T.getAllNodesUnderNeighbor(branchInT.getOtherNode(), branchInT.getOtherNeighbor());
/*      */     Node outsideNodeT;
/*      */     Set nodesOfT;
/*      */     Node insideNodeT;
/*      */     Node outsideNodeT;
/* 1025 */     if (((!T.hasOutgroup()) && (set1.size() <= set2.size())) || (
/* 1026 */       (T.hasOutgroup()) && (!set1.containsAll(T.getOutgroupLeaves())))) {
/* 1027 */       Set nodesOfT = new HashSet(set1);
/* 1028 */       Node insideNodeT = branchInT.getOtherNode();
/* 1029 */       outsideNodeT = branchInT.getNode();
/*      */     } else {
/* 1031 */       nodesOfT = new HashSet(set2);
/* 1032 */       insideNodeT = branchInT.getNode();
/* 1033 */       outsideNodeT = branchInT.getOtherNode();
/*      */     }
/* 1035 */     set1.clear();
/* 1036 */     set2.clear();
/*      */ 
/* 1038 */     set1 = T2.getAllNodesUnderNeighbor(branchInT2.getNode(), branchInT2.getNeighbor());
/* 1039 */     set2 = T2.getAllNodesUnderNeighbor(branchInT2.getOtherNode(), branchInT2.getOtherNeighbor());
/*      */     Node outsideNodeT2;
/*      */     Set nodesOfT2;
/*      */     Node insideNodeT2;
/*      */     Node outsideNodeT2;
/* 1040 */     if (((!T.hasOutgroup()) && (set1.size() < set2.size())) || (
/* 1041 */       (T.hasOutgroup()) && (!set1.containsAll(T2.getOutgroupLeaves())))) {
/* 1042 */       Set nodesOfT2 = new HashSet(set1);
/* 1043 */       Node insideNodeT2 = branchInT2.getOtherNode();
/* 1044 */       outsideNodeT2 = branchInT2.getNode();
/*      */     }
/*      */     else
/*      */     {
/*      */       Node outsideNodeT2;
/* 1045 */       if ((!T.hasOutgroup()) && (set1.size() == set2.size())) {
/* 1046 */         Node leaf = null;
/* 1047 */         for (Node n : set1)
/* 1048 */           if (n.isLeaf()) {
/* 1049 */             leaf = n;
/* 1050 */             break;
/*      */           }
/*      */         Node outsideNodeT2;
/* 1053 */         if (nodesOfT.contains(T.getNode(leaf.label))) {
/* 1054 */           Set nodesOfT2 = new HashSet(set1);
/* 1055 */           Node insideNodeT2 = branchInT2.getOtherNode();
/* 1056 */           outsideNodeT2 = branchInT2.getNode();
/*      */         } else {
/* 1058 */           Set nodesOfT2 = new HashSet(set2);
/* 1059 */           Node insideNodeT2 = branchInT2.getNode();
/* 1060 */           outsideNodeT2 = branchInT2.getOtherNode();
/*      */         }
/*      */       } else {
/* 1063 */         nodesOfT2 = new HashSet(set2);
/* 1064 */         insideNodeT2 = branchInT2.getNode();
/* 1065 */         outsideNodeT2 = branchInT2.getOtherNode();
/*      */       }
/*      */     }
/* 1068 */     BiMap TxT2 = HashBiMap.create(nodesOfT2.size());
/* 1069 */     TxT2.put(outsideNodeT, outsideNodeT2);
/* 1070 */     TxT2.put(insideNodeT, insideNodeT2);
/* 1071 */     Stack stackT2 = new Stack();
/* 1072 */     for (Node n : nodesOfT2) {
/* 1073 */       if ((n.isInode()) && (n != insideNodeT2))
/* 1074 */         stackT2.push(n);
/*      */     }
/* 1076 */     for (Node n : nodesOfT) {
/* 1077 */       if (n.isLeaf())
/* 1078 */         TxT2.put(n, T2.getNode(n.label));
/* 1079 */       else if (n != insideNodeT) {
/* 1080 */         TxT2.put(n, (Node)stackT2.pop());
/*      */       }
/*      */     }
/*      */ 
/* 1084 */     insideNodeT.removeNeighborButKeepBranchLength(outsideNodeT);
/* 1085 */     for (Node n : nodesOfT)
/* 1086 */       n.removeAllNeighbors();
/*      */     int j;
/*      */     int i;
/* 1089 */     for (??? = nodesOfT2.iterator(); ???.hasNext(); 
/* 1095 */       i < j)
/*      */     {
/* 1089 */       Node nT2 = (Node)???.next();
/* 1090 */       Node nT = (Node)TxT2.inverse().get(nT2);
/* 1091 */       Set currentNeighborsT2 = new HashSet();
/* 1092 */       for (Node neighbor : nT.getNeighborNodes())
/* 1093 */         currentNeighborsT2.add((Node)TxT2.get(neighbor));
/*      */       Node.Neighbor[] arrayOfNeighbor;
/* 1095 */       j = (arrayOfNeighbor = Node.Neighbor.values()).length; i = 0; continue; Node.Neighbor neigh = arrayOfNeighbor[i];
/* 1096 */       if (nT2.hasNeighbor(neigh)) {
/* 1097 */         Node neighborNodeT2 = nT2.getNeighbor(neigh);
/* 1098 */         if (!currentNeighborsT2.contains(neighborNodeT2)) {
/* 1099 */           Node.Neighbor neighThis = nT.addNeighbor((Node)TxT2.inverse().get(neighborNodeT2));
/* 1100 */           nT.setBranchLength(neighThis, nT2.getBranchLength(neigh));
/*      */         }
/*      */       }
/* 1095 */       i++;
/*      */     }
/*      */ 
/* 1105 */     T.root();
/* 1106 */     for (Node n : nodesOfT)
/* 1107 */       T.markNodeToReEvaluate(n);
/*      */   }
/*      */ 
/*      */   public final class BiPartition
/*      */     implements Comparable<BiPartition>
/*      */   {
/*      */     private final BitSet bits;
/*      */     private int cardinality;
/*      */     private int count;
/*      */     private double strength;
/*      */     private double branchLength;
/*   79 */     private String stringRepresentation = null;
/*      */     private final int ntax;
/*      */ 
/*      */     public BiPartition()
/*      */     {
/*   86 */       this.ntax = Consensus.this.numTaxa;
/*   87 */       this.bits = new BitSet(this.ntax);
/*   88 */       this.cardinality = 0;
/*   89 */       this.strength = 0.0D;
/*   90 */       this.branchLength = 0.0D;
/*   91 */       this.count = 1;
/*      */     }
/*      */ 
/*      */     public BiPartition(Node leaf)
/*      */     {
/*   99 */       this.ntax = Consensus.this.numTaxa;
/*  100 */       this.bits = new BitSet(this.ntax);
/*  101 */       this.bits.set(((Integer)Consensus.this.taxonId.get(leaf.getLabel())).intValue());
/*  102 */       this.count = 1;
/*  103 */       this.strength = 1.0D;
/*  104 */       this.branchLength = leaf.getBranchLength((Node.Neighbor)leaf.getNeighborKeys().iterator().next());
/*  105 */       this.cardinality = 1;
/*      */     }
/*      */ 
/*      */     public BiPartition(double setToMerge)
/*      */     {
/*  114 */       this.ntax = Consensus.this.numTaxa;
/*  115 */       this.bits = new BitSet(this.ntax);
/*  116 */       for (BiPartition p : setToMerge) {
/*  117 */         this.bits.or(p.bits);
/*      */       }
/*  119 */       this.count = 1;
/*  120 */       this.strength = 1.0D;
/*  121 */       this.branchLength = branchLength;
/*  122 */       this.cardinality = this.bits.cardinality();
/*      */     }
/*      */ 
/*      */     public String toString() {
/*  126 */       return this.bits.toString();
/*      */     }
/*      */ 
/*      */     public int compareTo(BiPartition bp) {
/*  130 */       if (equals(bp)) return 0;
/*  131 */       if (bp.strength - this.strength < 0.0D) return -1;
/*  132 */       if (bp.strength - this.strength > 0.0D) return 1;
/*  133 */       return 0;
/*      */     }
/*      */ 
/*      */     public String toTaxa() {
/*  137 */       if (this.stringRepresentation == null) {
/*  138 */         StringBuilder A = new StringBuilder();
/*  139 */         StringBuilder B = new StringBuilder();
/*  140 */         for (Iterator localIterator = Consensus.this.taxa.keySet().iterator(); localIterator.hasNext(); ) { int i = ((Integer)localIterator.next()).intValue();
/*  141 */           if (this.bits.get(i)) A.append((String)Consensus.this.taxa.get(Integer.valueOf(i)) + " "); else
/*  142 */             B.append((String)Consensus.this.taxa.get(Integer.valueOf(i)) + " ");
/*      */         }
/*  144 */         this.stringRepresentation = (A.toString() + " versus " + B.toString());
/*      */       }
/*  146 */       return this.stringRepresentation;
/*      */     }
/*      */ 
/*      */     public int getCardinality() {
/*  150 */       return this.cardinality;
/*      */     }
/*      */ 
/*      */     public double getStrength() {
/*  154 */       return this.strength;
/*      */     }
/*      */ 
/*      */     public String getTaxa() {
/*  158 */       String s = "";
/*  159 */       for (Iterator localIterator = Consensus.this.taxa.keySet().iterator(); localIterator.hasNext(); ) { int i = ((Integer)localIterator.next()).intValue();
/*  160 */         if (this.bits.get(i)) {
/*  161 */           if (s.length() > 0) s = s + ", ";
/*  162 */           s = s + (String)Consensus.this.taxa.get(Integer.valueOf(i));
/*      */         }
/*      */       }
/*  165 */       return s;
/*      */     }
/*      */ 
/*      */     public boolean equals(Object obj) {
/*  169 */       BiPartition p = (BiPartition)obj;
/*  170 */       if (this.bits.equals(p.bits))
/*  171 */         return true;
/*  172 */       if ((this.cardinality == this.ntax / 2) && (this.cardinality == p.cardinality)) {
/*  173 */         BitSet comp = new BitSet(this.ntax);
/*  174 */         comp.flip(0, this.ntax);
/*  175 */         comp.andNot(((BiPartition)obj).bits);
/*  176 */         return this.bits.equals(comp);
/*      */       }
/*  178 */       return false;
/*      */     }
/*      */ 
/*      */     public int hashCode() {
/*  182 */       int hash = 42;
/*  183 */       hash = 31 * hash + this.cardinality;
/*  184 */       return hash;
/*      */     }
/*      */ 
/*      */     public boolean isIncludedIn(BiPartition p)
/*      */     {
/*  193 */       if (this.cardinality < p.cardinality) {
/*  194 */         for (int i = 0; i < this.ntax; i++) {
/*  195 */           if ((!p.bits.get(i)) && 
/*  196 */             (this.bits.get(i))) return false;
/*      */         }
/*      */ 
/*  199 */         return true;
/*      */       }
/*  201 */       return false;
/*      */     }
/*      */ 
/*      */     public boolean isCompatible(BiPartition p)
/*      */     {
/*  211 */       if (this.bits.intersects(p.bits)) {
/*  212 */         if (this.cardinality < p.cardinality) {
/*  213 */           return isIncludedIn(p);
/*      */         }
/*  215 */         return p.isIncludedIn(this);
/*      */       }
/*  217 */       return true;
/*      */     }
/*      */ 
/*      */     public Consensus.TaxaSet getSmallestPartition() {
/*  221 */       Consensus.TaxaSet txs = new Consensus.TaxaSet(Consensus.this);
/*  222 */       Consensus.TaxaSet.access$0(txs).or(this.bits);
/*  223 */       Consensus.TaxaSet.access$1(txs, Consensus.TaxaSet.access$0(txs).cardinality());
/*  224 */       return txs;
/*      */     }
/*      */ 
/*      */     public Consensus.TaxaSet getBiggestPartition() {
/*  228 */       Consensus.TaxaSet txs = new Consensus.TaxaSet(Consensus.this);
/*  229 */       Consensus.TaxaSet.access$0(txs).or(this.bits);
/*  230 */       Consensus.TaxaSet.access$0(txs).flip(0, this.ntax);
/*  231 */       Consensus.TaxaSet.access$1(txs, Consensus.TaxaSet.access$0(txs).cardinality());
/*  232 */       return txs;
/*      */     }
/*      */ 
/*      */     public boolean hasOnSamePartition(Consensus.TaxaSet txs)
/*      */     {
/*  241 */       if (this.bits.intersects(Consensus.TaxaSet.access$0(txs))) {
/*  242 */         for (int i = 0; i < this.ntax; i++) {
/*  243 */           if ((Consensus.TaxaSet.access$0(txs).get(i)) && 
/*  244 */             (!this.bits.get(i))) return false;
/*      */         }
/*      */       }
/*      */ 
/*  248 */       return true;
/*      */     }
/*      */ 
/*      */     public boolean isIncludedIn(Consensus.TaxaSet txs)
/*      */     {
/*  259 */       boolean included = true;
/*  260 */       for (int i = this.bits.nextSetBit(0); i != -1; i = this.bits.nextSetBit(i + 1)) {
/*  261 */         if (!Consensus.TaxaSet.access$0(txs).get(i)) {
/*  262 */           included = false;
/*  263 */           break;
/*      */         }
/*      */       }
/*  266 */       if ((!included) && (this.bits.nextClearBit(0) != -1)) {
/*  267 */         included = true;
/*  268 */         for (int i = this.bits.nextClearBit(0); i != -1; i = this.bits.nextClearBit(i + 1)) {
/*  269 */           if (!Consensus.TaxaSet.access$0(txs).get(i)) {
/*  270 */             included = false;
/*  271 */             break;
/*      */           }
/*      */         }
/*      */       }
/*  275 */       return included;
/*      */     }
/*      */ 
/*      */     public BiPartition complement(BiPartition p)
/*      */       throws NoInclusionException
/*      */     {
/*  286 */       if (!p.isIncludedIn(this)) throw new NoInclusionException(toTaxa(), p.toTaxa(), "Cannot take the complement");
/*  287 */       BiPartition q = new BiPartition(Consensus.this);
/*  288 */       q.bits.or(this.bits);
/*  289 */       q.bits.xor(p.bits);
/*  290 */       q.cardinality = q.bits.cardinality();
/*  291 */       return q;
/*      */     }
/*      */ 
/*      */     public BiPartition mirror() {
/*  295 */       BiPartition p = new BiPartition(Consensus.this);
/*  296 */       p.count = this.count;
/*  297 */       p.strength = this.strength;
/*  298 */       p.branchLength = this.branchLength;
/*  299 */       p.bits.or(this.bits);
/*  300 */       p.bits.flip(0, this.ntax);
/*  301 */       p.cardinality = p.bits.cardinality();
/*  302 */       return p;
/*      */     }
/*      */   }
/*      */   public class TaxaSet {
/*      */     private BitSet bits;
/*      */     private int cardinality;
/*      */     private final int nTax;
/*      */ 
/*  312 */     public TaxaSet() { this.nTax = Consensus.this.numTaxa;
/*  313 */       this.bits = new BitSet(this.nTax);
/*  314 */       this.cardinality = 0;
/*      */     }
/*      */ 
/*      */     public TaxaSet()
/*      */     {
/*  322 */       this.nTax = Consensus.this.numTaxa;
/*  323 */       this.bits = new BitSet(this.nTax);
/*  324 */       for (Node leaf : leaves) {
/*  325 */         this.bits.set(((Integer)Consensus.this.taxonId.get(leaf.getLabel())).intValue());
/*      */       }
/*  327 */       this.cardinality = this.bits.cardinality();
/*      */     }
/*      */ 
/*      */     public int cardinality() {
/*  331 */       return this.cardinality;
/*      */     }
/*      */ 
/*      */     public String toString() {
/*  335 */       String s = "";
/*  336 */       for (int i = 0; i < this.nTax; i++) {
/*  337 */         if (this.bits.get(i)) {
/*  338 */           if (s.length() > 0) s = s + ", ";
/*  339 */           s = s + (String)Consensus.this.taxa.get(Integer.valueOf(i));
/*      */         }
/*      */       }
/*  342 */       return s;
/*      */     }
/*      */ 
/*      */     public List<Node> getTaxas(Tree T) {
/*  346 */       List list = new ArrayList();
/*  347 */       for (int i = 0; i < this.nTax; i++) {
/*  348 */         if (this.bits.get(i)) {
/*  349 */           list.add(T.getNode((String)Consensus.this.taxa.get(Integer.valueOf(i))));
/*      */         }
/*      */       }
/*  352 */       return list;
/*      */     }
/*      */ 
/*      */     public boolean isIncludedIn(TaxaSet taxaset) {
/*  356 */       if (this.cardinality < taxaset.cardinality) {
/*  357 */         for (int i = 0; i < this.nTax; i++) {
/*  358 */           if ((!taxaset.bits.get(i)) && 
/*  359 */             (this.bits.get(i))) return false;
/*      */         }
/*      */ 
/*  362 */         return true;
/*      */       }
/*  364 */       return false;
/*      */     }
/*      */ 
/*      */     public TaxaSet complement(TaxaSet taxaset) throws NoInclusionException
/*      */     {
/*  369 */       if (!taxaset.isIncludedIn(this)) throw new NoInclusionException(toString(), taxaset.toString(), "Cannot make the complement");
/*  370 */       TaxaSet txs = new TaxaSet(Consensus.this);
/*  371 */       txs.bits.or(this.bits);
/*  372 */       txs.bits.xor(taxaset.bits);
/*  373 */       txs.cardinality = txs.bits.cardinality();
/*  374 */       return txs;
/*      */     }
/*      */ 
/*      */     public void keepTaxas(TaxaSet txs) {
/*  378 */       this.bits.and(txs.bits);
/*  379 */       this.cardinality = this.bits.cardinality();
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.Consensus
 * JD-Core Version:    0.6.2
 */