/*      */ package metapiga.modelization;
/*      */ 
/*      */ import cern.jet.random.Exponential;
/*      */ import java.awt.Point;
/*      */
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.BitSet;
/*      */ import java.util.Collection;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.Iterator;
/*      */ import java.util.LinkedHashMap;
/*      */ import java.util.LinkedList;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Set;
/*      */ import java.util.TreeMap;
/*      */ import java.util.TreeSet;
/*      */ import javax.swing.JOptionPane;
/*      */ import javax.swing.JProgressBar;
/*      */ import metapiga.MetaPIGA;
/*      */ import metapiga.MetaPIGA.UI;
/*      */ import metapiga.ProgressHandling;
/*      */ import metapiga.exceptions.IncompatibleDataException;
/*      */ import metapiga.exceptions.NexusInconsistencyException;
/*      */ import metapiga.exceptions.OutgroupTooBigException;
/*      */ import metapiga.exceptions.UnknownDataException;
/*      */ import metapiga.modelization.data.Data;
/*      */ import metapiga.modelization.data.DataType;
/*      */ import metapiga.modelization.data.codons.tables.CodonTransitionTable;
/*      */ import metapiga.monitors.Monitor;
/*      */ import metapiga.parameters.Parameters;
/*      */
/*      */
/*      */
/*      */
/*      */
/*      */
/*      */ import metapiga.trees.Node;
/*      */ import metapiga.trees.Node.Neighbor;
/*      */ import metapiga.trees.Tree;
/*      */ import metapiga.trees.exceptions.TooManyNeighborsException;
/*      */ import metapiga.trees.exceptions.UncompatibleOutgroupException;
/*      */ import metapiga.trees.exceptions.UnknownTaxonException;
/*      */ import metapiga.utilities.Tools;
/*      */ import org.biojavax.bio.phylo.io.nexus.CharactersBlock;
/*      */ 
/*      */ public class Dataset
/*      */ {
/*   66 */   private boolean hasSaturation = false;
/*      */   private final DataType dataType;
/*      */   private final Map<Charset, Partition> data;
/*      */   private final List<String> taxas;
/*      */   private final String longestTaxon;
/*      */   private final BitSet gaps;
/*      */   private final BitSet ngaps;
/*      */   private final int fullNChar;
/*      */   private DistanceMatrix D;
/*      */   Parameters.DistanceModel DMmodel;
/*      */   Parameters.StartingTreeDistribution DMdistribution;
/*      */   double DMdistributionShape;
/*      */   double DMpinv;
/*      */   Parameters.StartingTreePInvPi DMpi;
/*      */ 
/*      */   public Dataset(CharactersBlock block, Set<String> deletedTaxa, Set<Charset> excludedCharsets, Set<Charset> partitions, Parameters.ColumnRemoval columnRemoval)
/*      */     throws UnknownDataException, NexusInconsistencyException
/*      */   {
/*  291 */     this.data = new LinkedHashMap();
/*  292 */     this.taxas = new ArrayList();
/*  293 */     this.D = null;
/*      */ 
/*  296 */     if (block.getDataType().toUpperCase().equals("NUCLEOTIDES")) {
/*  297 */       this.dataType = DataType.DNA; } else {
/*  298 */       if (block.getDataType().toUpperCase().equals("RNA")) {
/*  299 */         throw new UnknownDataException("RNA");
/*      */       }
/*  301 */       this.dataType = DataType.valueOf(block.getDataType().toUpperCase());
/*      */     }
/*  303 */     this.fullNChar = block.getDimensionsNChar();
/*  304 */     String matchSymbol = block.getMatchChar() == null ? "." : block.getMatchChar();
/*  305 */     String missingSymbol = block.getMissing() == null ? "." : block.getMissing();
/*  306 */     String gapSymbol = block.getGap() == null ? "." : block.getGap();
/*  307 */     Map matrix = new LinkedHashMap();
/*  308 */     this.gaps = new BitSet();
/*  309 */     this.ngaps = new BitSet();
/*  310 */     String firstTaxon = null;
/*      */     List seq;
/*  311 */     for (Iterator localIterator1 = block.getMatrixLabels().iterator(); localIterator1.hasNext(); ) { Object taxon = localIterator1.next();
/*  312 */       seq = new LinkedList();
/*  313 */       if (firstTaxon == null) firstTaxon = taxon.toString();
/*  314 */       for (Iterator localIterator2 = block.getMatrixData(taxon.toString()).iterator(); localIterator2.hasNext(); ) { Object obj = localIterator2.next();
/*  315 */         String nucl = obj.toString();
/*  316 */         if (nucl.length() > 0) {
/*  317 */           if (nucl.length() > 1) {
/*  318 */             BitSet bitSet = new BitSet(this.dataType.numOfStates());
/*  319 */             for (char c : nucl.toCharArray()) {
/*  320 */               bitSet.set(this.dataType.getStateOf(c));
/*      */             }
/*  322 */             seq.add(this.dataType.getData(bitSet));
/*      */           }
/*  324 */           else if ((nucl.equals(matchSymbol)) || ((block.isRespectCase()) && (nucl.equalsIgnoreCase(matchSymbol)))) {
/*  325 */             if (firstTaxon == null) throw new NexusInconsistencyException("You cannot use MATCHCHAR symbol on first line !");
/*  326 */             Data d = (Data)((List)matrix.get(firstTaxon)).get(seq.size());
/*  327 */             if (d.isUndeterminate()) this.ngaps.set(seq.size());
/*  328 */             seq.add(d);
/*  329 */           } else if ((nucl.equals(missingSymbol)) || ((block.isRespectCase()) && (nucl.equalsIgnoreCase(missingSymbol)))) {
/*  330 */             this.ngaps.set(seq.size());
/*  331 */             seq.add(this.dataType.getUndeterminateData());
/*  332 */           } else if ((nucl.equals(gapSymbol)) || ((block.isRespectCase()) && (nucl.equalsIgnoreCase(gapSymbol)))) {
/*  333 */             this.gaps.set(seq.size());
/*  334 */             this.ngaps.set(seq.size());
/*  335 */             seq.add(this.dataType.getUndeterminateData());
/*      */           } else {
/*      */             try {
/*  338 */               Data d = this.dataType.getData(nucl.toUpperCase());
/*  339 */               if (d.isUndeterminate()) this.ngaps.set(seq.size());
/*  340 */               seq.add(d);
/*      */             } catch (Exception e) {
/*  342 */               e.printStackTrace();
/*  343 */               throw new UnknownDataException(nucl, taxon.toString());
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  349 */       matrix.put(taxon.toString(), seq);
/*  350 */       if (((List)matrix.get(firstTaxon)).size() != seq.size()) {
/*  351 */         throw new NexusInconsistencyException("Line " + taxon + " has a size of " + seq.size() + ", and should have " + ((List)matrix.get(firstTaxon)).size() + " as first line");
/*      */       }
/*      */     }
/*      */ 
/*  355 */     handleDataPartitioning(deletedTaxa, excludedCharsets, partitions, 
/*  356 */       columnRemoval, matrix);
/*      */ 
/*  359 */     String temp = "";
/*  360 */     for (String taxa : this.taxas) {
/*  361 */       if (taxa.length() > temp.length()) {
/*  362 */         temp = taxa;
/*      */       }
/*      */     }
/*  365 */     this.longestTaxon = temp;
/*      */   }
/*      */ 
/*      */   private void handleDataPartitioning(Set<String> deletedTaxa, Set<Charset> excludedCharsets, Set<Charset> partitions, Parameters.ColumnRemoval columnRemoval, Map<String, List<Data>> matrix)
/*      */   {
/*  379 */     for (Charset charset : partitions) {
/*  380 */       Map temp = new LinkedHashMap();
/*  381 */       boolean needMapping = true;
/*  382 */       List mapping = new LinkedList();
/*  383 */       for (Entry e : matrix.entrySet()) {
/*  384 */         String taxa = (String)e.getKey();
/*  385 */         if (!deletedTaxa.contains(taxa)) {
/*  386 */           List seq = new LinkedList();
/*  387 */           for (int i = 0; i < ((List)e.getValue()).size(); i++) {
/*  388 */             boolean exclude = false;
/*  389 */             if (columnRemoval == Parameters.ColumnRemoval.GAP)
/*  390 */               exclude = this.gaps.get(i);
/*  391 */             else if (columnRemoval == Parameters.ColumnRemoval.NGAP) {
/*  392 */               exclude = this.ngaps.get(i);
/*      */             }
/*  394 */             if (!exclude) {
/*  395 */               for (Charset ch : excludedCharsets) {
/*  396 */                 if (ch.isInCharset(i + 1)) {
/*  397 */                   exclude = true;
/*  398 */                   break;
/*      */                 }
/*      */               }
/*      */             }
/*  402 */             if ((!exclude) && (charset.isInCharset(i + 1))) {
/*  403 */               seq.add((Data)((List)e.getValue()).get(i));
/*  404 */               if (needMapping) mapping.add(Integer.valueOf(i));
/*      */             }
/*      */           }
/*  407 */           temp.put(taxa, seq);
/*  408 */           if (!this.taxas.contains(taxa)) {
/*  409 */             this.taxas.add(taxa);
/*      */           }
/*      */         }
/*      */       }
/*  413 */       this.data.put(charset, new Partition(this.dataType, this.taxas, temp, mapping));
/*      */     }
/*      */   }
/*      */ 
/*      */   public Dataset(CodonCharactersBlock block, Set<String> deletedTaxa, Set<Charset> excludedCharsets, Set<Charset> partitions, Parameters.ColumnRemoval columnRemoval, CodonTransitionTable transTable) throws UnknownDataException, NexusInconsistencyException
/*      */   {
/*  419 */     this.data = new LinkedHashMap();
/*  420 */     this.taxas = new ArrayList();
/*  421 */     this.dataType = DataType.CODON;
/*  422 */     this.fullNChar = block.getDimensionsNChar();
/*  423 */     this.gaps = new BitSet();
/*  424 */     this.ngaps = new BitSet();
/*      */ 
/*  431 */     handleDataPartitioning(deletedTaxa, excludedCharsets, partitions, 
/*  432 */       columnRemoval, block.matrix);
/*      */ 
/*  435 */     String temp = "";
/*  436 */     for (String taxa : this.taxas) {
/*  437 */       if (taxa.length() > temp.length()) {
/*  438 */         temp = taxa;
/*      */       }
/*      */     }
/*  441 */     this.longestTaxon = temp;
/*      */   }
/*      */ 
/*      */   public Dataset(Dataset D)
/*      */   {
/*  446 */     this.dataType = D.dataType;
/*  447 */     this.data = new LinkedHashMap();
/*  448 */     for (Entry e : D.data.entrySet()) {
/*  449 */       this.data.put((Charset)e.getKey(), new Partition((Partition)e.getValue()));
/*      */     }
/*  451 */     this.taxas = new ArrayList();
/*  452 */     this.taxas.addAll(D.taxas);
/*  453 */     this.longestTaxon = D.longestTaxon;
/*  454 */     this.gaps = new BitSet(D.gaps.length());
/*  455 */     this.gaps.or(D.gaps);
/*  456 */     this.ngaps = new BitSet(D.ngaps.length());
/*  457 */     this.ngaps.or(D.ngaps);
/*  458 */     this.fullNChar = D.fullNChar;
/*      */   }
/*      */ 
/*      */   private Dataset(Dataset D, int firstCodonPosition, int lastCodonPostition)
/*      */     throws IncompatibleDataException
/*      */   {
/*  469 */     if (D.dataType != DataType.DNA) {
/*  470 */       throw new IncompatibleDataException(DataType.DNA, D.dataType);
/*      */     }
/*  472 */     this.dataType = DataType.CODON;
/*  473 */     this.taxas = new ArrayList();
/*  474 */     this.taxas.addAll(D.taxas);
/*  475 */     this.longestTaxon = D.longestTaxon;
/*  476 */     this.gaps = new BitSet(D.gaps.length());
/*  477 */     this.gaps.or(D.gaps);
/*  478 */     this.ngaps = new BitSet(D.ngaps.length());
/*  479 */     this.ngaps.or(D.ngaps);
/*  480 */     this.fullNChar = D.fullNChar;
/*      */ 
/*  483 */     this.data = new LinkedHashMap();
/*      */   }
/*      */ 
/*      */   public Dataset makeCodonDataset(Parameters.CodonDomainDefinition codonDefinition) {
/*      */     try {
/*  488 */       return new Dataset(this, codonDefinition.getStartCodonDomainPosition(), codonDefinition.getEndCodonDomainPosition());
/*      */     } catch (IncompatibleDataException e) {
/*  490 */       e.printStackTrace();
/*  491 */       System.exit(-1);
/*  492 */     }return null;
/*      */   }
/*      */ 
/*      */   public DataType getDataType()
/*      */   {
/*  497 */     return this.dataType;
/*      */   }
/*      */ 
/*      */   public String getTaxon(int index) {
/*  501 */     return (String)this.taxas.get(index);
/*      */   }
/*      */ 
/*      */   public String getLongestTaxon() {
/*  505 */     return this.longestTaxon;
/*      */   }
/*      */ 
/*      */   public boolean hasGapAtPos(int pos) {
/*  509 */     return this.gaps.get(pos);
/*      */   }
/*      */ 
/*      */   public boolean hasGapOrNAtPos(int pos) {
/*  513 */     return this.ngaps.get(pos);
/*      */   }
/*      */ 
/*      */   public List<String> getTaxa() {
/*  517 */     return this.taxas;
/*      */   }
/*      */ 
/*      */   public int getNTax() {
/*  521 */     return this.taxas.size();
/*      */   }
/*      */ 
/*      */   public Partition getPartition(Charset partition) {
/*  525 */     return (Partition)this.data.get(partition);
/*      */   }
/*      */ 
/*      */   public Collection<Partition> getPartitions() {
/*  529 */     return this.data.values();
/*      */   }
/*      */ 
/*      */   public Set<Charset> getPartitionCharsets() {
/*  533 */     return this.data.keySet();
/*      */   }
/*      */ 
/*      */   public Charset getCharset(String label) throws Exception {
/*  537 */     for (Charset c : this.data.keySet()) {
/*  538 */       if (c.getLabel().equals(label))
/*  539 */         return c;
/*      */     }
/*  541 */     throw new Exception("Unknown charset: " + label);
/*      */   }
/*      */ 
/*      */   public int getFullNChar()
/*      */   {
/*  551 */     return this.fullNChar;
/*      */   }
/*      */ 
/*      */   public int getNChar()
/*      */   {
/*  561 */     int nchar = 0;
/*  562 */     for (Partition p : this.data.values()) {
/*  563 */       nchar += p.getNChar();
/*      */     }
/*  565 */     return nchar;
/*      */   }
/*      */ 
/*      */   public int getCompressedNChar()
/*      */   {
/*  574 */     int nchar = 0;
/*  575 */     for (Partition p : this.data.values()) {
/*  576 */       nchar += p.getCompressedNChar();
/*      */     }
/*  578 */     return nchar;
/*      */   }
/*      */ 
/*      */   public double[] getDataFrequencies() {
/*  582 */     double[] frequencies = new double[this.dataType.numOfStates()];
/*  583 */     for (int i = 0; i < this.dataType.numOfStates(); i++) {
/*  584 */       frequencies[i] = 0.0D;
/*      */     }
/*  586 */     int nchar = getNChar();
/*      */     int i;
/*  587 */     for (Iterator localIterator = this.data.values().iterator(); localIterator.hasNext(); 
/*  589 */       i < this.dataType.numOfStates())
/*      */     {
/*  587 */       Partition p = (Partition)localIterator.next();
/*  588 */       double[] charsetFreq = p.getDataFrequencies();
/*  589 */       i = 0; continue;
/*  590 */       frequencies[i] += charsetFreq[i] * p.nchar / nchar;
/*      */ 
/*  589 */       i++;
/*      */     }
/*      */ 
/*  593 */     return frequencies;
/*      */   }
/*      */ 
/*      */   public String getDataFrequenciesToString() throws UnknownDataException {
/*  597 */     double[] freq = getDataFrequencies();
/*  598 */     String s = "";
/*  599 */     for (int i = 0; i < this.dataType.numOfStates(); i++) {
/*  600 */       s = s + this.dataType.getDataWithState(i).toString() + "[" + Tools.doubleToPercent(freq[i], 2) + "]";
/*  601 */       if (i < this.dataType.numOfStates() - 1) s = s + ", ";
/*      */     }
/*  603 */     return s;
/*      */   }
/*      */ 
/*      */   public double getSequenceQuality(String taxon)
/*      */   {
/*  616 */     double ambiguities = getNChar();
/*      */     Partition P;
/*      */     int pos;
/*  617 */     for (Iterator localIterator = getPartitions().iterator(); localIterator.hasNext(); 
/*  618 */       pos < P.getCompressedNChar())
/*      */     {
/*  617 */       P = (Partition)localIterator.next();
/*  618 */       pos = 0; continue;
/*  619 */       Data d = P.getData(taxon, pos);
/*  620 */       if (d.numOfStates() > 1)
/*  621 */         ambiguities -= d.numOfStates() / d.getMaxStates() * P.getWeight(pos);
/*  618 */       pos++;
/*      */     }
/*      */ 
/*  625 */     ambiguities /= getNChar();
/*  626 */     return ambiguities;
/*      */   }
/*      */ 
/*      */   public Map<String, Double> getAmbiguousSequences(double threshold)
/*      */   {
/*  636 */     ProgressHandling progress = MetaPIGA.progressHandling;
/*  637 */     progress.newSingleProgress(0, getNTax() * getCompressedNChar(), "Testing for ambiguities");
/*  638 */     int p = 0;
/*  639 */     Map ambigous = new TreeMap();
/*  640 */     for (String taxon : getTaxa()) {
/*  641 */       double ambiguities = 0.0D;
/*      */       Partition P;
/*      */       int pos;
/*  642 */       for (Iterator localIterator2 = getPartitions().iterator(); localIterator2.hasNext(); 
/*  643 */         pos < P.getCompressedNChar())
/*      */       {
/*  642 */         P = (Partition)localIterator2.next();
/*  643 */         pos = 0; continue;
/*  644 */         progress.setValue(++p);
/*  645 */         if (P.getData(taxon, pos).numOfStates() > 1)
/*  646 */           ambiguities += P.getWeight(pos);
/*  643 */         pos++;
/*      */       }
/*      */ 
/*  650 */       ambiguities /= getNChar();
/*  651 */       if (ambiguities >= threshold) {
/*  652 */         ambigous.put(taxon, Double.valueOf(ambiguities));
/*      */       }
/*      */     }
/*  655 */     return ambigous;
/*      */   }
/*      */ 
/*      */   public Set<Set<String>> getIdenticalSequences() {
/*  659 */     DistanceMatrix DM = getDistanceMatrix(Parameters.DistanceModel.ABSOLUTE, Parameters.StartingTreeDistribution.NONE, 0.5D, 0.0D, Parameters.StartingTreePInvPi.CONSTANT);
/*  660 */     for (int i = 0; i < DM.ntax; i++) {
/*  661 */       for (int j = 0; j < i; j++) {
/*  662 */         DM.set(i, j, DM.get(j, i));
/*      */       }
/*      */     }
/*  665 */     Set idSeq = new HashSet();
/*  666 */     ProgressHandling progress = MetaPIGA.progressHandling;
/*  667 */     int T = DM.ntax;
/*  668 */     progress.newSingleProgress(0, T * (T - 1) / 2 + T, "Testing for identical sequences");
/*  669 */     int p = 0;
/*      */     Set set;
/*      */     boolean equivalent;
/*  670 */     for (int i = 0; i < DM.ntax; i++) {
/*  671 */       for (int j = 0; j < i; j++) {
/*  672 */         progress.setValue(++p);
/*  673 */         if (DM.get(j, i) == 0.0D) {
/*  674 */           set = new HashSet();
/*      */ 
/*  676 */           for (Iterator it = idSeq.iterator(); it.hasNext(); ) {
/*  677 */             Set idSet = (Set)it.next();
/*  678 */             if ((idSet.contains(Integer.valueOf(i))) || (idSet.contains(Integer.valueOf(j))))
/*      */             {
/*  680 */               equivalent = true;
/*  681 */               for (Iterator localIterator1 = idSet.iterator(); localIterator1.hasNext(); ) { int k = ((Integer)localIterator1.next()).intValue();
/*  682 */                 if ((k != i) && 
/*  683 */                   (DM.get(k, i) != 0.0D)) {
/*  684 */                   equivalent = false;
/*  685 */                   break;
/*      */                 }
/*      */ 
/*  688 */                 if ((k != j) && 
/*  689 */                   (DM.get(k, j) != 0.0D)) {
/*  690 */                   equivalent = false;
/*  691 */                   break;
/*      */                 }
/*      */               }
/*      */ 
/*  695 */               if (equivalent) {
/*  696 */                 for (localIterator1 = idSet.iterator(); localIterator1.hasNext(); ) { int k = ((Integer)localIterator1.next()).intValue();
/*  697 */                   set.add(Integer.valueOf(k));
/*      */                 }
/*  699 */                 it.remove();
/*      */               }
/*      */             }
/*      */           }
/*  703 */           set.add(Integer.valueOf(i));
/*  704 */           set.add(Integer.valueOf(j));
/*  705 */           idSeq.add(set);
/*      */         }
/*      */       }
/*      */     }
/*  709 */     Set result = new HashSet();
/*  710 */     for (Set set : idSeq) {
/*  711 */       Set s = new HashSet();
/*  712 */       for (equivalent = set.iterator(); equivalent.hasNext(); ) { int id = ((Integer)equivalent.next()).intValue();
/*  713 */         s.add((String)this.taxas.get(id));
/*      */       }
/*  715 */       result.add(s);
/*      */     }
/*  717 */     return result;
/*      */   }
/*      */ 
/*      */   public DistanceMatrix getDistanceMatrix(Parameters.DistanceModel model, Parameters.StartingTreeDistribution distribution, double distributionShape, double pinv, Parameters.StartingTreePInvPi pi)
/*      */   {
/*  722 */     if ((this.D == null) || (model != this.DMmodel) || (distribution != this.DMdistribution) || 
/*  723 */       (distributionShape != this.DMdistributionShape) || (pinv != this.DMpinv) || (pi != this.DMpi)) {
/*  724 */       this.D = new DistanceMatrix(this.dataType, new HashSet(getPartitions()), getTaxa(), model, distribution, distributionShape, pinv, pi);
/*  725 */       this.DMmodel = model;
/*  726 */       this.DMdistribution = distribution;
/*  727 */       this.DMdistributionShape = distributionShape;
/*  728 */       this.DMpinv = pinv;
/*  729 */       this.DMpi = pi;
/*  730 */       this.hasSaturation = this.D.hasSaturation();
/*      */     }
/*  732 */     return this.D;
/*      */   }
/*      */ 
/*      */   public Tree generateTree(Set<String> outgroup, Parameters.StartingTreeGeneration generation, double startingTreeRange, Parameters.DistanceModel model, Parameters.StartingTreeDistribution distribution, double gammaShape, double pinv, Parameters.StartingTreePInvPi pi, Parameters datasetAndEvaluationParam, Monitor monitor)
/*      */     throws OutgroupTooBigException, UncompatibleOutgroupException, UnknownTaxonException, TooManyNeighborsException
/*      */   {
/*  758 */     if (outgroup.size() > this.taxas.size() - 2) {
/*  759 */       throw new OutgroupTooBigException();
/*      */     }
/*  761 */     Tree tree = new Tree(generation.toString(), datasetAndEvaluationParam);
/*  762 */     int treeRange = (int)(getNTax() * (getNTax() - 1) / 2 * startingTreeRange);
/*  763 */     if (treeRange < 1) treeRange = 1;
/*  764 */     switch (generation) {
/*      */     case NJ:
/*  766 */       List inTaxa = new ArrayList(this.taxas);
/*  767 */       Node root = new Node();
/*  768 */       tree.addNode(root);
/*  769 */       if (outgroup.size() == 1) {
/*  770 */         String name = (String)outgroup.iterator().next();
/*  771 */         inTaxa.remove(name);
/*  772 */         Node leaf = new Node(name);
/*  773 */         Node.Neighbor key = root.addNeighbor(leaf);
/*  774 */         root.setBranchLength(key, Exponential.staticNextDouble(1.0D) + 0.001D);
/*  775 */         tree.addNode(leaf);
/*  776 */       } else if (outgroup.size() > 1) {
/*  777 */         List outTaxa = new ArrayList(outgroup);
/*  778 */         inTaxa.removeAll(outgroup);
/*  779 */         Node outRoot = new Node();
/*  780 */         tree.addNode(outRoot);
/*  781 */         Node.Neighbor key = root.addNeighbor(outRoot);
/*  782 */         root.setBranchLength(key, Exponential.staticNextDouble(1.0D) + 0.001D);
/*  783 */         generateRandomSubTree(tree, outRoot, outTaxa);
/*      */       }
/*  785 */       generateRandomSubTree(tree, root, inTaxa);
/*  786 */       tree.setAccessNode(root);
/*  787 */       tree.setOutgroup(outgroup);
/*  788 */       tree.setAccessNodeToOutgroupRoot();
/*  789 */       break;
/*      */     case GIVEN:
/*  791 */       treeRange = 1;
/*      */       try {
/*  793 */         generateNeighborJoiningTree(tree, treeRange, new HashSet(), model, distribution, gammaShape, pinv, pi, monitor);
/*  794 */         tree.setOutgroup(outgroup);
/*      */       } catch (UncompatibleOutgroupException ex) {
/*  796 */         System.out.println(ex.getMessage());
/*  797 */         JOptionPane.showMessageDialog(null, ex.getMessage() + "\nA loosing neighbor joining tree will be used instead, with a topology compatible with your outgroup.");
/*  798 */         tree = new Tree(generation.toString(), datasetAndEvaluationParam);
/*  799 */         generateNeighborJoiningTree(tree, treeRange, outgroup, model, distribution, gammaShape, pinv, pi, monitor);
/*  800 */         tree.setOutgroup(outgroup);
/*      */       }
/*  802 */       tree.setAccessNodeToOutgroupRoot();
/*  803 */       break;
/*      */     case LNJ:
/*      */     default:
/*  806 */       generateNeighborJoiningTree(tree, treeRange, outgroup, model, distribution, gammaShape, pinv, pi, monitor);
/*  807 */       tree.setOutgroup(outgroup);
/*  808 */       tree.setAccessNodeToOutgroupRoot();
/*      */     }
/*      */ 
/*  811 */     return tree;
/*      */   }
/*      */ 
/*      */   private void generateRandomSubTree(Tree tree, Node root, List<String> taxaList) throws UnknownTaxonException, TooManyNeighborsException {
/*  815 */     List remainingTaxa = new ArrayList(taxaList);
/*  816 */     int nInodes = taxaList.size() - 3 + root.getNeighborNodes().size();
/*  817 */     int freeSlots = 3 - root.getNeighborNodes().size();
/*  818 */     List inodes = new ArrayList();
/*  819 */     inodes.add(root);
/*  820 */     while (inodes.size() > 0) {
/*  821 */       Node current = (Node)inodes.get(0);
/*  822 */       inodes.remove(0);
/*  823 */       while (current.getNeighborNodes().size() < 3)
/*      */       {
/*      */         boolean addLeaf;
/*      */         boolean addLeaf;
/*  825 */         if ((freeSlots == 1) && (nInodes > 0)) {
/*  826 */           addLeaf = false;
/*      */         }
/*      */         else
/*      */         {
/*      */           boolean addLeaf;
/*  827 */           if (nInodes == 0)
/*  828 */             addLeaf = true;
/*      */           else
/*  830 */             addLeaf = Math.random() < 0.5D;
/*      */         }
/*  832 */         if (addLeaf)
/*      */         {
/*  834 */           int rand = Tools.randInt(remainingTaxa.size());
/*  835 */           String name = (String)remainingTaxa.get(rand);
/*  836 */           remainingTaxa.remove(rand);
/*  837 */           Node leaf = new Node(name);
/*  838 */           Node.Neighbor key = current.addNeighbor(leaf);
/*  839 */           current.setBranchLength(key, Exponential.staticNextDouble(1.0D) + 0.001D);
/*  840 */           tree.addNode(leaf);
/*  841 */           freeSlots--;
/*      */         }
/*      */         else {
/*  844 */           Node inode = new Node();
/*  845 */           Node.Neighbor key = current.addNeighbor(inode);
/*  846 */           current.setBranchLength(key, Exponential.staticNextDouble(1.0D) + 0.001D);
/*  847 */           inodes.add(inode);
/*  848 */           freeSlots++;
/*  849 */           nInodes--;
/*      */         }
/*      */       }
/*  852 */       tree.addNode(current);
/*      */     }
/*      */   }
/*      */ 
/*      */   private void generateNeighborJoiningTree(Tree tree, int randomRange, Set<String> outgroup, Parameters.DistanceModel model, Parameters.StartingTreeDistribution distribution, double distributionShape, double pinv, Parameters.StartingTreePInvPi pi, Monitor monitor)
/*      */     throws UnknownTaxonException, TooManyNeighborsException
/*      */   {
/*  860 */     int taxaNumber = getNTax();
/*  861 */     int matLength = 2 * taxaNumber - 2;
/*      */ 
/*  863 */     double[][] DM = new double[matLength][matLength];
/*  864 */     for (int i = 0; i < DM.length; i++) {
/*  865 */       for (int j = 0; j < DM[i].length; j++) {
/*  866 */         if ((i < taxaNumber) && (j < taxaNumber))
/*  867 */           DM[i][j] = 0.0D;
/*      */         else {
/*  869 */           DM[i][j] = 1.7976931348623157E+308D;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  874 */     if ((this.D == null) || (model != this.DMmodel) || (distribution != this.DMdistribution) || (distributionShape != this.DMdistributionShape) || (pinv != this.DMpinv) || (pi != this.DMpi)) {
/*  875 */       this.D = new DistanceMatrix(this.dataType, new HashSet(getPartitions()), getTaxa(), model, distribution, distributionShape, pinv, pi);
/*  876 */       this.DMmodel = model;
/*  877 */       this.DMdistribution = distribution;
/*  878 */       this.DMdistributionShape = distributionShape;
/*  879 */       this.DMpinv = pinv;
/*  880 */       this.DMpi = pi;
/*  881 */       this.hasSaturation = this.D.hasSaturation();
/*  882 */       if (this.hasSaturation) {
/*  883 */         monitor.showText("\n");
/*  884 */         monitor.showText("-------------------------------------------------------------------------------------------------------------------");
/*  885 */         monitor.showText("-------------------------------------------------------------------------------------------------------------------");
/*  886 */         monitor.showText("-------------------------------------------------------------------------------------------------------------------");
/*  887 */         monitor.showText("WARNING: MetaPIGA has encountered saturations in ML distances.");
/*  888 */         monitor.showText("This can generate artifacts in your final result or cause ML computation to fail, hence metaPIGA to crash.");
/*  889 */         monitor.showText("We suggest you remove the highly divergent sequence(s) and realign the remaining sequences.");
/*  890 */         monitor.showText("-------------------------------------------------------------------------------------------------------------------");
/*  891 */         monitor.showText("-------------------------------------------------------------------------------------------------------------------");
/*  892 */         monitor.showText("-------------------------------------------------------------------------------------------------------------------");
/*  893 */         monitor.showText("\n");
/*      */       }
/*  895 */       if (monitor.trackDistances()) monitor.printDistanceMatrix(this.D);
/*      */     }
/*  897 */     for (int x = 0; x < taxaNumber; x++) {
/*  898 */       for (int y = x + 1; y < taxaNumber; y++)
/*      */       {
/*      */         double tmp411_408 = this.D.get(x, y); DM[y][x] = tmp411_408; DM[x][y] = tmp411_408;
/*      */       }
/*      */     }
/*      */ 
/*  903 */     BitSet mask = new BitSet(matLength);
/*  904 */     BitSet selectable = new BitSet(matLength);
/*  905 */     int N = taxaNumber;
/*  906 */     double[] U = new double[matLength];
/*  907 */     Node[] nodes = new Node[matLength];
/*  908 */     for (int k = 0; k < matLength; k++) {
/*  909 */       U[k] = 1.7976931348623157E+308D;
/*  910 */       if (k < taxaNumber) {
/*  911 */         nodes[k] = new Node(getTaxon(k));
/*  912 */         mask.set(k);
/*  913 */         if (!outgroup.contains(getTaxon(k))) selectable.set(k); 
/*      */       }
/*  915 */       else { nodes[k] = new Node(); }
/*      */ 
/*      */     }
/*      */ 
/*  919 */     for (int k = taxaNumber; k < matLength; k++) {
/*  920 */       for (int i = 0; i < matLength; i++) {
/*  921 */         if (mask.get(i)) {
/*  922 */           U[i] = 0.0D;
/*  923 */           for (int j = 0; j < matLength; j++) {
/*  924 */             if ((mask.get(j)) && (i != j)) {
/*  925 */               U[i] += DM[i][j];
/*      */             }
/*      */           }
/*  928 */           U[i] /= (N - 2);
/*      */         }
/*      */       }
/*      */ 
/*  932 */       Node node1 = null; Node node2 = null;
/*  933 */       int pos1 = -1; int pos2 = -1;
/*  934 */       List mins = new ArrayList();
/*  935 */       for (int i = 0; i < matLength; i++) {
/*  936 */         if ((mask.get(i)) && (selectable.get(i))) {
/*  937 */           for (int j = i + 1; j < matLength; j++) {
/*  938 */             if ((mask.get(j)) && (selectable.get(j))) {
/*  939 */               double thisVal = DM[i][j] - U[i] - U[j];
/*  940 */               boolean add = false;
/*  941 */               if (mins.size() < randomRange) {
/*  942 */                 add = true;
/*      */               } else {
/*  944 */                 Point p = (Point)mins.get(mins.size() - 1);
/*  945 */                 double val = DM[p.x][p.y] - U[p.x] - U[p.y];
/*  946 */                 if (thisVal < val) {
/*  947 */                   mins.remove(mins.size() - 1);
/*  948 */                   add = true;
/*      */                 }
/*      */               }
/*  951 */               if (add) {
/*  952 */                 int pos = 0;
/*  953 */                 for (int m = 0; m < mins.size(); m++) {
/*  954 */                   Point p = (Point)mins.get(m);
/*  955 */                   double val = DM[p.x][p.y] - U[p.x] - U[p.y];
/*  956 */                   if (thisVal < val) break;
/*  957 */                   pos++;
/*      */                 }
/*  959 */                 mins.add(pos, new Point(i, j));
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*  965 */       int rand = mins.size() == 1 ? 0 : Tools.randInt(mins.size());
/*  966 */       Point p = (Point)mins.get(rand);
/*  967 */       node1 = nodes[p.x];
/*  968 */       pos1 = p.x;
/*  969 */       node2 = nodes[p.y];
/*  970 */       pos2 = p.y;
/*      */ 
/*  972 */       Node inode = nodes[k];
/*  973 */       Node.Neighbor key = inode.addNeighbor(node1);
/*  974 */       inode.setBranchLength(key, DM[pos1][pos2] / 2.0D + (U[pos1] - U[pos2]) / 2.0D);
/*  975 */       key = inode.addNeighbor(node2);
/*  976 */       inode.setBranchLength(key, DM[pos1][pos2] / 2.0D + (U[pos2] - U[pos1]) / 2.0D);
/*  977 */       tree.addNode(node1);
/*  978 */       tree.addNode(node2);
/*      */ 
/*  980 */       for (int i = 0; i < k; i++) {
/*  981 */         if ((i != pos1) && (i != pos2)) {
/*  982 */           DM[k][i] = ((DM[pos1][i] + DM[pos2][i] - DM[pos1][pos2]) / 2.0D);
/*  983 */           DM[i][k] = DM[k][i];
/*      */         }
/*      */       }
/*      */ 
/*  987 */       mask.clear(pos1);
/*  988 */       selectable.clear(pos1);
/*  989 */       mask.clear(pos2);
/*  990 */       selectable.clear(pos2);
/*  991 */       mask.set(k);
/*  992 */       selectable.set(k);
/*  993 */       N--;
/*  994 */       if ((selectable.cardinality() == 1) && (N > 2)) {
/*  995 */         for (int taxa = 0; taxa < taxaNumber; taxa++) {
/*  996 */           if (outgroup.contains(getTaxon(taxa))) {
/*  997 */             selectable.set(taxa);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1003 */     for (int i = 0; i < matLength; i++)
/* 1004 */       if (mask.get(i)) {
/* 1005 */         Node.Neighbor key = nodes[(matLength - 1)].addNeighbor(nodes[i]);
/* 1006 */         nodes[(matLength - 1)].setBranchLength(key, DM[i][(matLength - 1)]);
/* 1007 */         tree.addNode(nodes[i]);
/* 1008 */         tree.addNode(nodes[(matLength - 1)]);
/* 1009 */         tree.setAccessNode(nodes[(matLength - 1)]);
/* 1010 */         break;
/*      */       }
/*      */   }
/*      */ 
/*      */   public Dataset randomSampling()
/*      */   {
/* 1016 */     Dataset D = new Dataset(this);
/*      */     Partition P;
/*      */     int j;
/* 1017 */     for (Iterator localIterator = D.data.values().iterator(); localIterator.hasNext(); 
/* 1027 */       j < P.nchar)
/*      */     {
/* 1017 */       P = (Partition)localIterator.next();
/* 1018 */       int[] pos = new int[P.nchar];
/* 1019 */       int i = 0;
/* 1020 */       for (int p = 0; p < P.weights.length; p++) {
/* 1021 */         for (int j = 0; j < P.weights[p]; j++) {
/* 1022 */           pos[i] = p;
/* 1023 */           i++;
/*      */         }
/*      */       }
/* 1026 */       Arrays.fill(P.weights, 0);
/* 1027 */       j = 0; continue;
/* 1028 */       P.weights[pos[Tools.randInt(P.nchar)]] += 1;
/*      */ 
/* 1027 */       j++;
/*      */     }
/*      */ 
/* 1031 */     return D;
/*      */   }
/*      */ 
/*      */   public String toString()
/*      */   {
/* 1036 */     String endl = "\n";
/* 1037 */     StringBuilder doc = new StringBuilder();
/* 1038 */     int longestTaxon = 0;
/* 1039 */     for (String taxa : getTaxa()) {
/* 1040 */       if (taxa.length() > longestTaxon) {
/* 1041 */         longestTaxon = taxa.length();
/*      */       }
/*      */     }
/* 1044 */     if ("Weights".toString().length() > longestTaxon) {
/* 1045 */       longestTaxon = "Weights".toString().length();
/*      */     }
/* 1047 */     doc.append("Character matrices used in MetaPIGA :" + endl + endl);
/* 1048 */     doc.append("Your Nexus matrix has been compressed, you can see the weight of each column on the last line." + endl);
/* 1049 */     doc.append(getNTax() + " taxa where kept." + endl);
/* 1050 */     doc.append(getDataType().verbose() + " frequencies : ");
/*      */     try {
/* 1052 */       doc.append(getDataFrequenciesToString());
/*      */     } catch (UnknownDataException ex) {
/* 1054 */       doc.append(ex.getMessage());
/*      */     }
/* 1056 */     doc.append(endl);
/* 1057 */     if (!this.data.isEmpty()) {
/* 1058 */       doc.append("Partitions (each one is used separatly during computation) : " + endl);
/* 1059 */       for (Entry e : this.data.entrySet()) {
/* 1060 */         doc.append(((Charset)e.getKey()).toString());
/* 1061 */         doc.append(" : " + getPartition((Charset)e.getKey()).getNChar() + 
/* 1062 */           " characters (" + getPartition((Charset)e.getKey()).getCompression() + " compression giving " + 
/* 1063 */           getPartition((Charset)e.getKey()).getCompressedNChar() + " characters)" + " - Frequencies : ");
/*      */         try {
/* 1065 */           doc.append(getPartition((Charset)e.getKey()).getDataFrequenciesToString());
/*      */         } catch (UnknownDataException ex) {
/* 1067 */           doc.append(ex.getMessage());
/*      */         }
/* 1069 */         doc.append(endl);
/*      */       }
/* 1071 */       doc.append(endl);
/*      */     }
/* 1073 */     doc.append(endl);
/* 1074 */     for (String taxa : getTaxa()) {
/* 1075 */       int spaces = longestTaxon - taxa.toString().length();
/* 1076 */       String stax = taxa;
/* 1077 */       for (int j = 0; j < spaces; j++) {
/* 1078 */         stax = stax + " ";
/*      */       }
/* 1080 */       stax = stax + "    ";
/* 1081 */       doc.append(stax);
/* 1082 */       for (Charset ch : getPartitionCharsets()) {
/* 1083 */         for (Data data : getPartition(ch).getAllData(taxa)) {
/* 1084 */           doc.append(data.toString());
/*      */         }
/* 1086 */         doc.append(" ");
/*      */       }
/* 1088 */       doc.append(endl);
/*      */     }
/* 1090 */     doc.append(endl);
/* 1091 */     boolean nextLine = false;
/* 1092 */     int line = 0;
/* 1093 */     String ws = "Weights";
/* 1094 */     int spaces = longestTaxon - ws.toString().length();
/* 1095 */     for (int j = 0; j < spaces; j++) {
/* 1096 */       ws = ws + " ";
/*      */     }
/* 1098 */     ws = ws + "    ";
/* 1099 */     doc.append(ws);
/*      */     int w;
/*      */     String s;
/* 1100 */     for (Charset ch : getPartitionCharsets()) {
/* 1101 */       for (w : getPartition(ch).getAllWeights()) {
/* 1102 */         s = w;
/* 1103 */         if (s.length() > line + 1) {
/* 1104 */           s = s.charAt(line);
/* 1105 */           nextLine = true;
/*      */         }
/* 1107 */         doc.append(s);
/*      */       }
/* 1109 */       doc.append(" ");
/*      */     }
/* 1111 */     doc.append(endl);
/* 1112 */     String empty = "";
/* 1113 */     for (int i = 0; i < ws.length(); i++) empty = empty + " ";
/* 1114 */     while (nextLine) {
/* 1115 */       line++;
/* 1116 */       nextLine = false;
/* 1117 */       doc.append(empty);
/* 1118 */       for (Charset ch : getPartitionCharsets()) {
/* 1119 */         for (int w : getPartition(ch).getAllWeights()) {
/* 1120 */           String s = w;
/* 1121 */           if (s.length() < line + 1) {
/* 1122 */             s = " ";
/* 1123 */           } else if (s.length() == line + 1) {
/* 1124 */             s = s.charAt(line);
/* 1125 */           } else if (s.length() > line + 1) {
/* 1126 */             s = s.charAt(line);
/* 1127 */             nextLine = true;
/*      */           }
/* 1129 */           doc.append(s);
/*      */         }
/* 1131 */         doc.append(" ");
/*      */       }
/* 1133 */       doc.append(endl);
/*      */     }
/* 1135 */     return doc.toString();
/*      */   }
/*      */ 
/*      */   public static final class Partition
/*      */   {
/*      */     private final DataType dataType;
/*      */     private final Map<String, List<Data>> data;
/*      */     private final List<String> taxa;
/*      */     private final int nchar;
/*      */     private final int[] weights;
/*      */     private final List<Set<Integer>> datasetMapping;
/*      */ 
/*      */     public Partition(DataType dataType, List<String> taxas, Map<String, List<Data>> seq, List<Integer> mapping)
/*      */     {
/*   77 */       this.dataType = dataType;
/*   78 */       this.taxa = taxas;
/*   79 */       this.nchar = ((List)seq.get(taxas.get(0))).size();
/*   80 */       ProgressHandling progress = MetaPIGA.progressHandling;
/*      */ 
/*   86 */       if (progress == null) {
/*   87 */         progress = new ProgressHandling(new JProgressBar());
/*   88 */         progress.setUI(MetaPIGA.UI.SILENT);
/*      */       }
/*      */ 
/*   91 */       progress.newSingleProgress(0, this.nchar, "Compressing dataset");
/*      */ 
/*   95 */       List tempList = new LinkedList();
/*   96 */       for (int i = 0; i < this.nchar; i++) {
/*   97 */         tempList.add(Integer.valueOf(1));
/*      */       }
/*      */ 
/*  100 */       Map difmap = new HashMap();
/*  101 */       Map tempMapping = new TreeMap();
/*      */       String site;
/*      */       int j;
/*  102 */       for (int i = 0; i < this.nchar; i++) {
/*  103 */         progress.setValue(i);
/*  104 */         site = "";
/*  105 */         for (String taxa : taxas) {
/*  106 */           site = site + ((List)seq.get(taxa)).get(i);
/*      */         }
/*  108 */         if (!difmap.containsKey(site)) {
/*  109 */           difmap.put(site, Integer.valueOf(i));
/*  110 */           Set set = new TreeSet();
/*  111 */           set.add((Integer)mapping.get(i));
/*  112 */           tempMapping.put(Integer.valueOf(i), set);
/*      */         } else {
/*  114 */           j = ((Integer)difmap.get(site)).intValue();
/*  115 */           tempList.set(j, Integer.valueOf(((Integer)tempList.get(j)).intValue() + 1));
/*  116 */           tempList.set(i, Integer.valueOf(0));
/*  117 */           ((Set)tempMapping.get(Integer.valueOf(j))).add((Integer)mapping.get(i));
/*      */         }
/*      */       }
/*      */ 
/*  121 */       this.data = new LinkedHashMap();
/*  122 */       for (String taxa : taxas) {
/*  123 */         this.data.put(taxa, new LinkedList());
/*      */       }
/*  125 */       progress.newSingleProgress(0, this.nchar, "Building data matrix");
/*  126 */       for (int i = 0; i < this.nchar; i++) {
/*  127 */         progress.setValue(i);
/*  128 */         if (((Integer)tempList.get(i)).intValue() > 0) {
/*  129 */           for (String taxa : taxas) {
/*  130 */             ((List)this.data.get(taxa)).add((Data)((List)seq.get(taxa)).get(i));
/*      */           }
/*      */         }
/*      */       }
/*  134 */       List weightsList = new LinkedList();
/*  135 */       this.datasetMapping = new LinkedList();
/*  136 */       for (int i = 0; i < this.nchar; i++) {
/*  137 */         if (((Integer)tempList.get(i)).intValue() > 0) {
/*  138 */           weightsList.add((Integer)tempList.get(i));
/*  139 */           this.datasetMapping.add((Set)tempMapping.get(Integer.valueOf(i)));
/*      */         }
/*      */       }
/*  142 */       int size = weightsList.size();
/*  143 */       this.weights = new int[size];
/*  144 */       for (int i = 0; i < size; i++)
/*  145 */         this.weights[i] = ((Integer)weightsList.get(i)).intValue();
/*      */     }
/*      */ 
/*      */     public Partition(Partition P)
/*      */     {
/*  150 */       this.dataType = P.dataType;
/*  151 */       this.data = new LinkedHashMap();
/*  152 */       for (String taxa : P.data.keySet()) {
/*  153 */         this.data.put(taxa, new LinkedList((Collection)P.data.get(taxa)));
/*      */       }
/*  155 */       this.taxa = new ArrayList(P.taxa);
/*  156 */       this.nchar = P.nchar;
/*  157 */       this.weights = new int[P.weights.length];
/*  158 */       System.arraycopy(P.weights, 0, this.weights, 0, P.weights.length);
/*  159 */       this.datasetMapping = new LinkedList();
/*  160 */       for (Set set : P.datasetMapping)
/*  161 */         this.datasetMapping.add(new TreeSet(set));
/*      */     }
/*      */ 
/*      */     public DataType getDataType()
/*      */     {
/*  166 */       return this.dataType;
/*      */     }
/*      */ 
/*      */     public String getTaxon(int index) {
/*  170 */       return (String)this.taxa.get(index);
/*      */     }
/*      */ 
/*      */     public List<String> getTaxa() {
/*  174 */       return this.taxa;
/*      */     }
/*      */ 
/*      */     public int getNTax() {
/*  178 */       return this.taxa.size();
/*      */     }
/*      */ 
/*      */     public int getNChar()
/*      */     {
/*  187 */       return this.nchar;
/*      */     }
/*      */ 
/*      */     public int getCompressedNChar()
/*      */     {
/*  195 */       return this.weights.length;
/*      */     }
/*      */ 
/*      */     public Data getData(String taxon, int position) {
/*  199 */       return (Data)((List)this.data.get(taxon)).get(position);
/*      */     }
/*      */ 
/*      */     public Data getData(int taxaIndex, int position) {
/*  203 */       return (Data)((List)this.data.get(this.taxa.get(taxaIndex))).get(position);
/*      */     }
/*      */ 
/*      */     public List<Data> getAllData(String taxon) {
/*  207 */       return (List)this.data.get(taxon);
/*      */     }
/*      */ 
/*      */     public int getWeight(int position) {
/*  211 */       return this.weights[position];
/*      */     }
/*      */ 
/*      */     public int[] getAllWeights() {
/*  215 */       return this.weights;
/*      */     }
/*      */ 
/*      */     public String getCompression() {
/*  219 */       return 100 - (int)(getCompressedNChar() / this.nchar * 100.0D) + "%";
/*      */     }
/*      */ 
/*      */     public Set<Integer> getDatasetPosition(int position)
/*      */     {
/*  229 */       return (Set)this.datasetMapping.get(position);
/*      */     }
/*      */ 
/*      */     public double[] getDataFrequencies() {
/*  233 */       double[] frequencies = new double[this.dataType.numOfStates()];
/*      */       List list;
/*      */       int k;
/*  234 */       for (Iterator localIterator = this.data.values().iterator(); localIterator.hasNext(); 
/*  235 */         k < list.size())
/*      */       {
/*  234 */         list = (List)localIterator.next();
/*  235 */         k = 0; continue;
/*  236 */         Data d = (Data)list.get(k);
/*  237 */         for (int i = 0; i < this.dataType.numOfStates(); i++)
/*  238 */           frequencies[i] += (d.isState(i) ? this.weights[k] / d.numOfStates() : 0.0D);
/*  235 */         k++;
/*      */       }
/*      */ 
/*  242 */       for (int i = 0; i < frequencies.length; i++) {
/*  243 */         frequencies[i] /= getNChar() * getNTax();
/*      */       }
/*  245 */       return frequencies;
/*      */     }
/*      */ 
/*      */     public String getDataFrequenciesToString() throws UnknownDataException {
/*  249 */       double[] freq = getDataFrequencies();
/*  250 */       String s = "";
/*  251 */       for (int i = 0; i < this.dataType.numOfStates(); i++) {
/*  252 */         s = s + this.dataType.getDataWithState(i).toString() + "[" + Tools.doubleToPercent(freq[i], 2) + "]";
/*  253 */         if (i < this.dataType.numOfStates() - 1) s = s + ", ";
/*      */       }
/*  255 */       return s;
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.Dataset
 * JD-Core Version:    0.6.2
 */