/*      */ package metapiga.trees;
/*      */ 
/*      */ import java.io.PrintStream;
/*      */ import java.util.ArrayList;
/*      */ import java.util.BitSet;
/*      */ import java.util.Collection;
/*      */ import java.util.Collections;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Set;
/*      */ import java.util.Stack;
/*      */ import java.util.TreeMap;
/*      */ import javax.swing.text.AttributeSet;
/*      */ import javax.swing.text.BadLocationException;
/*      */ import javax.swing.text.DefaultStyledDocument;
/*      */ import javax.swing.text.SimpleAttributeSet;
/*      */ import javax.swing.text.StyleConstants;
/*      */ import metapiga.RateParameter;
/*      */ import metapiga.modelization.Charset;
/*      */ import metapiga.modelization.Dataset;
/*      */ import metapiga.modelization.Dataset.Partition;
/*      */ import metapiga.modelization.data.DataType;
/*      */ import metapiga.modelization.likelihood.Likelihood;
/*      */ import metapiga.modelization.likelihood.LikelihoodFactory;
/*      */ import metapiga.modelization.likelihood.LikelihoodGpu;
/*      */ import metapiga.parameters.Parameters;
/*      */ import metapiga.parameters.Parameters.EvaluationDistribution;
/*      */ import metapiga.parameters.Parameters.EvaluationModel;
/*      */ import metapiga.parameters.Parameters.EvaluationRate;
/*      */ import metapiga.parameters.Parameters.EvaluationStateFrequencies;
/*      */ import metapiga.parameters.Parameters.LikelihoodCalculationType;
/*      */ import metapiga.trees.exceptions.BranchNotFoundException;
/*      */ import metapiga.trees.exceptions.NullAncestorException;
/*      */ import metapiga.trees.exceptions.TooManyNeighborsException;
/*      */ import metapiga.trees.exceptions.UncompatibleOutgroupException;
/*      */ import metapiga.trees.exceptions.UnknownTaxonException;
/*      */ import metapiga.trees.exceptions.UnrootableTreeException;
/*      */ import metapiga.utilities.Tools;
/*      */ import metapiga.videoCard.VideocardContext;
/*      */ import org.biojavax.bio.phylo.io.nexus.TreesBlock.NewickTreeString;
/*      */ 
/*      */ public class Tree
/*      */   implements Cloneable
/*      */ {
/*      */   private String name;
/*      */   private Node root;
/*      */   private Node outgroupRoot;
/*      */   private Node accessNode;
/*   61 */   private List<Node> nodes = new ArrayList();
/*   62 */   private List<Node> outgroup = new ArrayList();
/*   63 */   private List<Node> ingroup = new ArrayList();
/*   64 */   private List<Node> inodes = new ArrayList();
/*   65 */   private List<Node> leaves = new ArrayList();
/*   66 */   private List<Node> outInodes = new ArrayList();
/*   67 */   private List<Node> inInodes = new ArrayList();
/*   68 */   private List<Node> outLeaves = new ArrayList();
/*   69 */   private List<Node> inLeaves = new ArrayList();
/*   70 */   private Map<String, Node> labels = new HashMap();
/*   71 */   private final Map<Node, Integer> level = new HashMap();
/*   72 */   private final Map<Node, Integer> nbrOfLeaves = new HashMap();
/*   73 */   private Parameters.LikelihoodCalculationType likelihoodType = Parameters.LikelihoodCalculationType.CLASSIC;
/*      */   private double likelihoodValue;
/*      */   private Map<Charset, Likelihood> likelihood;
/*      */   private Dataset dataset;
/*      */   private Parameters.EvaluationRate evaluationRate;
/*      */   private Parameters.EvaluationModel evaluationModel;
/*      */   private Parameters.EvaluationStateFrequencies evaluationStateFrequencies;
/*   83 */   private final Map<Charset, Map<RateParameter, Double>> rateParameters = new TreeMap();
/*      */   private Parameters.EvaluationDistribution evaluationDistribution;
/*      */   private int evaluationDistributionSubsets;
/*   86 */   private final Map<Charset, Double> evaluationDistributionShape = new TreeMap();
/*   87 */   private final Map<Charset, Double> evaluationPInv = new TreeMap();
/*   88 */   private final Map<Charset, Double> evaluationAPRate = new TreeMap();
/*      */ 
/*   93 */   private boolean isUsingOneTimeGraphicMemory = false;
/*      */   private VideocardContext videocard;
/*      */   public Parameters parameters;
/*      */ 
/*      */   public Tree()
/*      */   {
/*  100 */     resetLikelihoodValue();
/*      */   }
/*      */ 
/*      */   public Tree(String name, Parameters parameters) {
/*  104 */     this.name = name;
/*  105 */     this.dataset = parameters.dataset;
/*  106 */     this.evaluationRate = parameters.evaluationRate;
/*  107 */     this.evaluationModel = parameters.evaluationModel;
/*  108 */     this.evaluationStateFrequencies = parameters.evaluationStateFrequencies;
/*  109 */     this.evaluationDistribution = parameters.evaluationDistribution;
/*  110 */     this.evaluationDistributionSubsets = parameters.evaluationDistributionSubsets;
/*  111 */     for (Charset c : this.dataset.getPartitionCharsets()) {
/*  112 */       Map map = new TreeMap();
/*  113 */       for (RateParameter r : RateParameter.getParametersOfModel(this.evaluationModel)) {
/*  114 */         map.put(r, (Double)parameters.getRateParameters(c).get(r));
/*      */       }
/*  116 */       this.rateParameters.put(c, map);
/*  117 */       this.evaluationDistributionShape.put(c, Double.valueOf(parameters.getEvaluationDistributionShape(c)));
/*  118 */       this.evaluationPInv.put(c, Double.valueOf(parameters.getEvaluationPInv(c)));
/*  119 */       this.evaluationAPRate.put(c, Double.valueOf(1.0D));
/*      */     }
/*  121 */     this.root = null;
/*  122 */     this.outgroupRoot = null;
/*  123 */     this.accessNode = null;
/*  124 */     resetLikelihoodValue();
/*  125 */     this.likelihoodType = parameters.getLikelihoodCalculationType();
/*  126 */     this.parameters = parameters;
/*      */   }
/*      */ 
/*      */   public Tree clone()
/*      */   {
/*  131 */     Tree T = new Tree();
/*  132 */     T.name = this.name;
/*  133 */     T.parameters = this.parameters;
/*  134 */     for (Node node : this.nodes) {
/*  135 */       Node nodeClone = new Node(node.label);
/*  136 */       T.nodes.add(nodeClone);
/*  137 */       if (this.outgroup.contains(node)) T.outgroup.add(nodeClone);
/*  138 */       if (this.ingroup.contains(node)) T.ingroup.add(nodeClone);
/*  139 */       if (this.inodes.contains(node)) T.inodes.add(nodeClone);
/*  140 */       if (this.leaves.contains(node)) T.leaves.add(nodeClone);
/*  141 */       if (this.outInodes.contains(node)) T.outInodes.add(nodeClone);
/*  142 */       if (this.inInodes.contains(node)) T.inInodes.add(nodeClone);
/*  143 */       if (this.outLeaves.contains(node)) T.outLeaves.add(nodeClone);
/*  144 */       if (this.inLeaves.contains(node)) T.inLeaves.add(nodeClone);
/*  145 */       T.labels.put(nodeClone.label, nodeClone);
/*      */     }
/*  147 */     T.clone(this);
/*  148 */     return T;
/*      */   }
/*      */ 
/*      */   public void clone(Tree T)
/*      */   {
/*  160 */     this.root = null;
/*  161 */     this.level.clear();
/*  162 */     this.nbrOfLeaves.clear();
/*      */ 
/*  164 */     for (Node n : this.nodes)
/*  165 */       n.removeAllNeighbors();
/*      */     int j;
/*      */     int i;
/*  168 */     for (??? = T.nodes.iterator(); ???.hasNext(); 
/*  174 */       i < j)
/*      */     {
/*  168 */       Node n = (Node)???.next();
/*  169 */       Node m = (Node)this.labels.get(n.getLabel());
/*  170 */       Set currentNeighbors = new HashSet();
/*  171 */       for (Node neighbor : m.getNeighborNodes())
/*  172 */         currentNeighbors.add(neighbor.getLabel());
/*      */       Node.Neighbor[] arrayOfNeighbor;
/*  174 */       j = (arrayOfNeighbor = Node.Neighbor.values()).length; i = 0; continue; Node.Neighbor neigh = arrayOfNeighbor[i];
/*  175 */       if (n.hasNeighbor(neigh)) {
/*  176 */         String l = n.getNeighbor(neigh).getLabel();
/*  177 */         if (!currentNeighbors.contains(l))
/*      */           try {
/*  179 */             Node.Neighbor neighThis = m.addNeighbor((Node)this.labels.get(l));
/*  180 */             m.setBranchLength(neighThis, n.getBranchLength(neigh));
/*      */           } catch (TooManyNeighborsException e) {
/*  182 */             System.out.println("Error in cloning of tree " + this.name + ": node " + l + " was NOT added as a neighbor of " + m);
/*  183 */             e.printStackTrace();
/*      */           }
/*      */       }
/*  174 */       i++;
/*      */     }
/*      */ 
/*  190 */     if (T.accessNode != null)
/*  191 */       this.accessNode = ((Node)this.labels.get(T.accessNode.getLabel()));
/*      */     else {
/*  193 */       this.accessNode = null;
/*      */     }
/*  195 */     if (T.outgroupRoot != null)
/*  196 */       this.outgroupRoot = ((Node)this.labels.get(T.outgroupRoot.getLabel()));
/*      */     else {
/*  198 */       this.outgroupRoot = null;
/*      */     }
/*  200 */     if (T.isRooted()) {
/*  201 */       root((Node)this.labels.get(T.getRoot().getLabel()));
/*      */     }
/*      */ 
/*  204 */     this.likelihoodValue = T.likelihoodValue;
/*  205 */     this.likelihoodType = T.likelihoodType;
/*  206 */     this.dataset = T.dataset;
/*  207 */     this.evaluationRate = T.evaluationRate;
/*  208 */     this.evaluationModel = T.evaluationModel;
/*  209 */     this.evaluationStateFrequencies = T.evaluationStateFrequencies;
/*  210 */     this.evaluationDistribution = T.evaluationDistribution;
/*  211 */     this.evaluationDistributionSubsets = T.evaluationDistributionSubsets;
/*  212 */     this.evaluationDistributionShape.clear();
/*  213 */     this.evaluationDistributionShape.putAll(T.evaluationDistributionShape);
/*  214 */     this.evaluationPInv.clear();
/*  215 */     this.evaluationPInv.putAll(T.evaluationPInv);
/*  216 */     this.evaluationAPRate.clear();
/*  217 */     this.evaluationAPRate.putAll(T.evaluationAPRate);
/*  218 */     this.rateParameters.clear();
/*  219 */     for (Charset c : this.dataset.getPartitionCharsets()) {
/*  220 */       Map map = new TreeMap();
/*  221 */       map.putAll((Map)T.rateParameters.get(c));
/*  222 */       this.rateParameters.put(c, map);
/*      */     }
/*  224 */     if (T.likelihood != null)
/*      */       try {
/*  226 */         if (this.likelihood != null) {
/*  227 */           for (??? = T.likelihood.entrySet().iterator(); ???.hasNext(); ) { Map.Entry e = (Map.Entry)???.next();
/*  228 */             ((Likelihood)this.likelihood.get(e.getKey())).clone((Likelihood)e.getValue()); }
/*      */         }
/*      */         else {
/*  231 */           this.likelihood = new HashMap();
/*  232 */           for (??? = T.likelihood.keySet().iterator(); ???.hasNext(); ) { Charset c = (Charset)???.next();
/*  233 */             Likelihood l = LikelihoodFactory.makeLikelihoodCopy((Likelihood)T.likelihood.get(c), this);
/*  234 */             if (((l instanceof LikelihoodGpu)) && (this.videocard != null)) {
/*  235 */               ((LikelihoodGpu)l).addGPUMemory(this.videocard);
/*      */             }
/*  237 */             this.likelihood.put(c, l); }
/*      */         }
/*      */       }
/*      */       catch (UnrootableTreeException e) {
/*  241 */         System.out.println("Error in cloning of tree " + this.name + ": clone is not root, CANNOT clone likelihood calculation");
/*  242 */         e.printStackTrace();
/*  243 */         this.likelihood = null;
/*      */       }
/*      */   }
/*      */ 
/*      */   public void cloneWithConsensus(Tree T)
/*      */   {
/*      */     int j;
/*      */     int i;
/*  260 */     for (Iterator localIterator1 = T.nodes.iterator(); localIterator1.hasNext(); 
/*  266 */       i < j)
/*      */     {
/*  260 */       Node n = (Node)localIterator1.next();
/*  261 */       Node m = (Node)this.labels.get(n.getLabel());
/*  262 */       Set currentNeighbors = new HashSet();
/*  263 */       for (Node neighbor : m.getNeighborNodes())
/*  264 */         currentNeighbors.add(neighbor.getLabel());
/*      */       Node.Neighbor[] arrayOfNeighbor;
/*  266 */       j = (arrayOfNeighbor = Node.Neighbor.values()).length; i = 0; continue; Node.Neighbor neigh = arrayOfNeighbor[i];
/*  267 */       if (n.hasNeighbor(neigh)) {
/*  268 */         String l = n.getNeighbor(neigh).getLabel();
/*  269 */         Node.Neighbor neighThis = null;
/*  270 */         for (Node.Neighbor neighThis2 : m.getNeighborKeys()) {
/*  271 */           if (m.getNeighbor(neighThis2).getLabel().equals(l)) {
/*  272 */             neighThis = neighThis2;
/*  273 */             break;
/*      */           }
/*      */         }
/*  276 */         if (neighThis != null)
/*  277 */           m.setBranchLength(neighThis, n.getBranchLength(neigh));
/*      */         else
/*  279 */           System.out.println("Error in consensus cloning of tree " + this.name + ": branch length between node " + l + " and " + m + " was not changed !");
/*      */       }
/*  266 */       i++;
/*      */     }
/*      */ 
/*  285 */     this.likelihoodValue = T.likelihoodValue;
/*  286 */     this.dataset = T.dataset;
/*  287 */     this.evaluationRate = T.evaluationRate;
/*  288 */     this.evaluationModel = T.evaluationModel;
/*  289 */     this.evaluationStateFrequencies = T.evaluationStateFrequencies;
/*  290 */     this.evaluationDistribution = T.evaluationDistribution;
/*  291 */     this.evaluationDistributionSubsets = T.evaluationDistributionSubsets;
/*  292 */     this.evaluationDistributionShape.clear();
/*  293 */     this.evaluationDistributionShape.putAll(T.evaluationDistributionShape);
/*  294 */     this.evaluationPInv.clear();
/*  295 */     this.evaluationPInv.putAll(T.evaluationPInv);
/*  296 */     this.evaluationAPRate.clear();
/*  297 */     this.evaluationAPRate.putAll(T.evaluationAPRate);
/*  298 */     this.rateParameters.clear();
/*  299 */     for (Charset c : this.dataset.getPartitionCharsets()) {
/*  300 */       Map map = new TreeMap();
/*  301 */       map.putAll((Map)T.rateParameters.get(c));
/*  302 */       this.rateParameters.put(c, map);
/*      */     }
/*  304 */     if (T.likelihood != null)
/*      */       try {
/*  306 */         if (this.likelihood != null) {
/*  307 */           for (Map.Entry e : T.likelihood.entrySet())
/*  308 */             ((Likelihood)this.likelihood.get(e.getKey())).clone((Likelihood)e.getValue());
/*      */         }
/*      */         else {
/*  311 */           this.likelihood = new HashMap();
/*  312 */           for (Charset c : T.likelihood.keySet())
/*  313 */             this.likelihood.put(c, LikelihoodFactory.makeLikelihoodCopy((Likelihood)T.likelihood.get(c), this));
/*      */         }
/*      */       }
/*      */       catch (UnrootableTreeException e) {
/*  317 */         System.out.println("Error in consensus cloning of tree " + this.name + ": clone is not rooted, CANNOT clone likelihood calculation");
/*  318 */         e.printStackTrace();
/*  319 */         this.likelihood = null;
/*      */       }
/*      */   }
/*      */ 
/*      */   public void setName(String treeName)
/*      */   {
/*  325 */     this.name = treeName;
/*      */   }
/*      */ 
/*      */   public String getName() {
/*  329 */     return this.name;
/*      */   }
/*      */ 
/*      */   public Node getRoot() {
/*  333 */     return this.root;
/*      */   }
/*      */ 
/*      */   public boolean isRooted() {
/*  337 */     return this.root != null;
/*      */   }
/*      */ 
/*      */   public void root() throws UnrootableTreeException {
/*  341 */     if (hasOutgroup()) root(this.outgroupRoot);
/*  342 */     else if (this.accessNode != null) root(this.accessNode); else
/*  343 */       throw new UnrootableTreeException();
/*      */   }
/*      */ 
/*      */   public void root(Node node) {
/*  347 */     unroot();
/*  348 */     node.setToRoot();
/*  349 */     this.root = node;
/*      */   }
/*      */ 
/*      */   public void setAccessNode(Node node) {
/*  353 */     this.accessNode = node;
/*      */   }
/*      */ 
/*      */   public void setAccessNodeToOutgroupRoot() {
/*  357 */     if (this.outgroupRoot != null) this.accessNode = this.outgroupRoot; 
/*      */   }
/*      */ 
/*      */   public Node getAccessNode()
/*      */   {
/*  361 */     return this.accessNode;
/*      */   }
/*      */ 
/*      */   public boolean hasOutgroup() {
/*  365 */     return this.outgroupRoot != null;
/*      */   }
/*      */ 
/*      */   public boolean isCompatibleWithDataset() {
/*  369 */     List list = new ArrayList();
/*  370 */     for (Node n : this.leaves) {
/*  371 */       list.add(n.getLabel());
/*      */     }
/*  373 */     return (this.dataset.getTaxa().containsAll(list)) && (list.containsAll(this.dataset.getTaxa()));
/*      */   }
/*      */ 
/*      */   public boolean isCompatibleWithOutgroup(Set<String> outgroupToTest) {
/*  377 */     if (outgroupToTest.size() > 0) {
/*  378 */       Set outRoot = new HashSet();
/*  379 */       for (String taxa : outgroupToTest) {
/*  380 */         Node leaf = getNode(taxa);
/*  381 */         outRoot.add(leaf); } 
/*      */ Object tempOutgroup = new ArrayList();
/*      */       boolean loop;
/*      */       int count;
/*      */       do { Set temp = new HashSet();
/*      */         Node n;
/*  388 */         for (Iterator localIterator2 = outRoot.iterator(); localIterator2.hasNext(); temp.addAll(n.getNeighborNodes())) n = (Node)localIterator2.next();
/*  389 */         temp.removeAll((Collection)tempOutgroup);
/*  390 */         temp.removeAll(outRoot);
/*  391 */         ((List)tempOutgroup).addAll(outRoot);
/*  392 */         outRoot.clear();
/*  393 */         outRoot.addAll(temp);
/*  394 */         loop = true;
/*  395 */         if (outRoot.size() <= 1) {
/*      */           try {
/*  397 */             tempOutgroupRoot = (Node)outRoot.iterator().next();
/*      */           }
/*      */           catch (Exception ex)
/*      */           {
/*      */             Node tempOutgroupRoot;
/*  399 */             return false;
/*      */           }
/*      */           Node tempOutgroupRoot;
/*  401 */           count = 0;
/*  402 */           for (Node n : tempOutgroupRoot.getNeighborNodes()) {
/*  403 */             if (((List)tempOutgroup).contains(n)) count++;
/*      */           }
/*  405 */           if (count == 1) loop = false;
/*      */         }
/*      */       }
/*  386 */       while (
/*  407 */         loop);
/*  408 */       for (Node n : (List)tempOutgroup) {
/*  409 */         if ((n.isLeaf()) && (!outgroupToTest.contains(n.getLabel()))) {
/*  410 */           return false;
/*      */         }
/*      */       }
/*      */     }
/*  414 */     return true;
/*      */   }
/*      */ 
/*      */   public void setOutgroup(Set<String> taxasInOutgroup)
/*      */     throws UncompatibleOutgroupException
/*      */   {
/*  475 */     if (!taxasInOutgroup.isEmpty()) {
/*  476 */       for (String taxa : taxasInOutgroup) {
/*  477 */         leaf = getNode(taxa);
/*  478 */         this.outgroup.add(leaf);
/*      */       }
/*  480 */       InOutMap iom = new InOutMap();
/*  481 */       for (Node leaf = this.inodes.iterator(); leaf.hasNext(); ) { inode = (Node)leaf.next();
/*  482 */         iom.assignGroup((Node)inode);
/*      */       }
/*      */     }
/*  485 */     for (Object inode = this.nodes.iterator(); ((Iterator)inode).hasNext(); ) { Node n = (Node)((Iterator)inode).next();
/*  486 */       if (!this.outgroup.contains(n))
/*  487 */         this.ingroup.add(n);
/*      */     }
/*  489 */     this.outInodes = new ArrayList(this.outgroup);
/*  490 */     this.outLeaves = new ArrayList(this.outgroup);
/*  491 */     this.outInodes.retainAll(this.inodes);
/*  492 */     this.outLeaves.retainAll(this.leaves);
/*  493 */     this.inInodes = new ArrayList(this.inodes);
/*  494 */     this.inLeaves = new ArrayList(this.leaves);
/*  495 */     this.inInodes.removeAll(this.outgroup);
/*  496 */     this.inLeaves.removeAll(this.outgroup);
/*  497 */     labelizeTree();
/*  498 */     this.nodes = Collections.unmodifiableList(this.nodes);
/*  499 */     this.labels = Collections.unmodifiableMap(this.labels);
/*  500 */     this.outgroup = Collections.unmodifiableList(this.outgroup);
/*  501 */     this.ingroup = Collections.unmodifiableList(this.ingroup);
/*  502 */     this.inodes = Collections.unmodifiableList(this.inodes);
/*  503 */     this.leaves = Collections.unmodifiableList(this.leaves);
/*  504 */     this.outInodes = Collections.unmodifiableList(this.outInodes);
/*  505 */     this.outLeaves = Collections.unmodifiableList(this.outLeaves);
/*  506 */     this.inInodes = Collections.unmodifiableList(this.inInodes);
/*  507 */     this.inLeaves = Collections.unmodifiableList(this.inLeaves);
/*      */   }
/*      */ 
/*      */   public void labelizeTree() {
/*  511 */     if (this.outgroup.isEmpty()) {
/*  512 */       int i = 0;
/*  513 */       if (this.accessNode.getLabel() == null) {
/*  514 */         while (this.labels.containsKey(i)) i++;
/*  515 */         this.accessNode.setLabel(i);
/*  516 */         this.labels.put(this.accessNode.getLabel(), this.accessNode);
/*      */       }
/*  518 */       i++;
/*  519 */       for (Node n : this.leaves) {
/*  520 */         if (n.getLabel() == null) {
/*  521 */           while (this.labels.containsKey(i)) i++;
/*  522 */           n.setLabel(i);
/*  523 */           this.labels.put(n.getLabel(), n);
/*  524 */           i++;
/*      */         }
/*      */       }
/*  527 */       for (Node n : this.inodes)
/*  528 */         if (n.getLabel() == null) {
/*  529 */           while (this.labels.containsKey(i)) i++;
/*  530 */           n.setLabel(i);
/*  531 */           this.labels.put(n.getLabel(), n);
/*  532 */           i++;
/*      */         }
/*      */     }
/*      */     else {
/*  536 */       this.labels.clear();
/*  537 */       int i = 0;
/*  538 */       if (this.outgroupRoot.getLabel() == null) {
/*  539 */         while (this.labels.containsKey(i)) i++;
/*  540 */         this.outgroupRoot.setLabel(i);
/*  541 */         this.labels.put(this.outgroupRoot.getLabel(), this.outgroupRoot);
/*      */       }
/*  543 */       i++;
/*  544 */       for (Node n : this.outLeaves) {
/*  545 */         if (n.getLabel() == null) {
/*  546 */           while (this.labels.containsKey(i)) i++;
/*  547 */           n.setLabel(i);
/*  548 */           i++;
/*      */         }
/*  550 */         this.labels.put(n.getLabel(), n);
/*      */       }
/*  552 */       for (Node n : this.inLeaves) {
/*  553 */         if (n.getLabel() == null) {
/*  554 */           while (this.labels.containsKey(i)) i++;
/*  555 */           n.setLabel(i);
/*  556 */           i++;
/*      */         }
/*  558 */         this.labels.put(n.getLabel(), n);
/*      */       }
/*  560 */       for (Node n : this.outInodes) {
/*  561 */         if (n.getLabel() == null) {
/*  562 */           while (this.labels.containsKey(i)) i++;
/*  563 */           n.setLabel(i);
/*  564 */           this.labels.put(n.getLabel(), n);
/*  565 */           i++;
/*      */         }
/*      */       }
/*  568 */       for (Node n : this.inInodes)
/*  569 */         if (n.getLabel() == null) {
/*  570 */           while (this.labels.containsKey(i)) i++;
/*  571 */           n.setLabel(i);
/*  572 */           this.labels.put(n.getLabel(), n);
/*  573 */           i++;
/*      */         }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void unroot()
/*      */   {
/*  580 */     if (this.root == null)
/*      */       return;
/*  581 */     Node n;
/*  581 */     for (Iterator localIterator = this.root.getNeighborNodes().iterator(); localIterator.hasNext(); n.removeAncestor()) n = (Node)localIterator.next();
/*  582 */     this.root = null;
/*  583 */     this.level.clear();
/*  584 */     this.nbrOfLeaves.clear();
/*      */   }
/*      */ 
/*      */   public void addNode(Node node) throws UnknownTaxonException {
/*  588 */     if ((node.isLeaf()) && (!this.dataset.getTaxa().contains(node.getLabel())))
/*  589 */       throw new UnknownTaxonException(node.getLabel());
/*  590 */     if (!this.nodes.contains(node)) {
/*  591 */       this.nodes.add(node);
/*  592 */       if (node.isLeaf()) this.leaves.add(node); else
/*  593 */         this.inodes.add(node);
/*  594 */       if (!this.labels.containsKey(node.getLabel()))
/*  595 */         this.labels.put(node.getLabel(), node);
/*      */       else
/*  597 */         node.setLabel(null);
/*      */     }
/*      */   }
/*      */ 
/*      */   public final List<Node> getNodes() {
/*  602 */     return this.nodes;
/*      */   }
/*      */ 
/*      */   public List<Branch> getBranches() {
/*  606 */     Set checkedNodes = new HashSet();
/*  607 */     List branches = new ArrayList();
/*      */     Iterator localIterator2;
/*  608 */     for (Iterator localIterator1 = this.inInodes.iterator(); localIterator1.hasNext(); 
/*  610 */       localIterator2.hasNext())
/*      */     {
/*  608 */       Node node = (Node)localIterator1.next();
/*  609 */       checkedNodes.add(node);
/*  610 */       localIterator2 = node.getBranches().iterator(); continue; Branch b = (Branch)localIterator2.next();
/*  611 */       if (!checkedNodes.contains(b.getOtherNode())) {
/*  612 */         branches.add(b);
/*      */       }
/*      */     }
/*      */ 
/*  616 */     return branches;
/*      */   }
/*      */ 
/*      */   public Branch getBranch(Node node, Node otherNode) throws BranchNotFoundException {
/*  620 */     Node.Neighbor neighbor = null;
/*  621 */     for (Node.Neighbor n : node.getNeighborKeys()) {
/*  622 */       if (node.getNeighbor(n) == otherNode) {
/*  623 */         neighbor = n;
/*  624 */         break;
/*      */       }
/*      */     }
/*  627 */     if (neighbor != null) {
/*  628 */       return new Branch(node, neighbor);
/*      */     }
/*  630 */     throw new BranchNotFoundException(node, otherNode);
/*      */   }
/*      */ 
/*      */   public final int getNumOfNodes() {
/*  634 */     return this.nodes.size();
/*      */   }
/*      */ 
/*      */   public final List<Node> getInodes() {
/*  638 */     return this.inodes;
/*      */   }
/*      */ 
/*      */   public final int getNumOfInodes() {
/*  642 */     return this.inodes.size();
/*      */   }
/*      */ 
/*      */   public final List<Node> getLeaves() {
/*  646 */     return this.leaves;
/*      */   }
/*      */ 
/*      */   public final int getNumOfLeaves() {
/*  650 */     return this.leaves.size();
/*      */   }
/*      */ 
/*      */   public Node getNode(String label) {
/*  654 */     return (Node)this.labels.get(label);
/*      */   }
/*      */ 
/*      */   public boolean isInOutgroup(Node node) {
/*  658 */     return this.outgroup.contains(node);
/*      */   }
/*      */ 
/*      */   public final int getOutgroupSize() {
/*  662 */     return this.outgroup.size();
/*      */   }
/*      */ 
/*      */   public final int getIngroupSize() {
/*  666 */     return this.ingroup.size();
/*      */   }
/*      */ 
/*      */   public final List<Node> getOutgroupInodes() {
/*  670 */     return this.outInodes;
/*      */   }
/*      */ 
/*      */   public final int getNumOfOutgroupInodes() {
/*  674 */     return this.outInodes.size();
/*      */   }
/*      */ 
/*      */   public final List<Node> getOutgroupLeaves() {
/*  678 */     return this.outLeaves;
/*      */   }
/*      */ 
/*      */   public final int getNumOfOutgroupLeaves() {
/*  682 */     return this.outLeaves.size();
/*      */   }
/*      */ 
/*      */   public final List<Node> getIngroupInodes() {
/*  686 */     return this.inInodes;
/*      */   }
/*      */ 
/*      */   public final int getNumOfIngroupInodes() {
/*  690 */     return this.inInodes.size();
/*      */   }
/*      */ 
/*      */   public final List<Node> getIngroupLeaves() {
/*  694 */     return this.inLeaves;
/*      */   }
/*      */ 
/*      */   public final int getNumOfIngroupLeaves() {
/*  698 */     return this.inLeaves.size();
/*      */   }
/*      */ 
/*      */   private void initLikelihood() throws UnrootableTreeException {
/*  702 */     this.likelihood = new HashMap();
/*  703 */     for (Charset c : this.dataset.getPartitionCharsets()) {
/*  704 */       Likelihood l = null;
/*  705 */       if (this.likelihoodType == Parameters.LikelihoodCalculationType.CLASSIC)
/*  706 */         l = LikelihoodFactory.makeLikelihoodClassic(this.dataset.getPartition(c), this.evaluationRate, this.evaluationModel, 
/*  707 */           this.evaluationDistribution, ((Double)this.evaluationDistributionShape.get(c)).doubleValue(), ((Double)this.evaluationPInv.get(c)).doubleValue(), 
/*  708 */           ((Double)this.evaluationAPRate.get(c)).doubleValue(), (Map)this.rateParameters.get(c), this.evaluationStateFrequencies, this, this.evaluationDistributionSubsets);
/*  709 */       else if (this.likelihoodType == Parameters.LikelihoodCalculationType.GPU) {
/*  710 */         l = LikelihoodFactory.makeLikelihoodGpu(this.dataset.getPartition(c), this.evaluationRate, this.evaluationModel, 
/*  711 */           this.evaluationDistribution, ((Double)this.evaluationDistributionShape.get(c)).doubleValue(), ((Double)this.evaluationPInv.get(c)).doubleValue(), 
/*  712 */           ((Double)this.evaluationAPRate.get(c)).doubleValue(), (Map)this.rateParameters.get(c), this.evaluationStateFrequencies, this, this.evaluationDistributionSubsets);
/*      */       }
/*  714 */       if (((l instanceof LikelihoodGpu)) && (this.videocard != null) && (!this.videocard.isDisposed())) {
/*  715 */         ((LikelihoodGpu)l).addGPUMemory(this.videocard);
/*  716 */       } else if (((l instanceof LikelihoodGpu)) && ((this.videocard == null) || (this.videocard.isDisposed()))) {
/*  717 */         this.isUsingOneTimeGraphicMemory = true;
/*  718 */         if ((this.videocard == null) || (this.videocard.isDisposed())) {
/*  719 */           this.videocard = VideocardContext.getVCcontext(this.parameters);
/*      */         }
/*  721 */         ((LikelihoodGpu)l).addGPUMemory(this.videocard);
/*      */       }
/*  723 */       this.likelihood.put(c, l);
/*      */     }
/*      */   }
/*      */ 
/*      */   private void createOneTimeGraphicMemeory() {
/*  728 */     if (this.parameters.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.CLASSIC) {
/*  729 */       return;
/*      */     }
/*  731 */     if ((this.videocard != null) && (this.isUsingOneTimeGraphicMemory)) {
/*  732 */       this.videocard.freeMemory();
/*  733 */       this.videocard = null;
/*      */     }
/*  735 */     if ((this.videocard == null) || (this.videocard.isDisposed())) {
/*  736 */       this.isUsingOneTimeGraphicMemory = true;
/*  737 */       this.videocard = VideocardContext.getVCcontext(this.parameters);
/*      */     }
/*  739 */     for (Likelihood l : this.likelihood.values())
/*  740 */       if ((l instanceof LikelihoodGpu))
/*  741 */         ((LikelihoodGpu)l).addGPUMemory(this.videocard);
/*      */   }
/*      */ 
/*      */   public synchronized double getEvaluation()
/*      */     throws UnrootableTreeException, NullAncestorException
/*      */   {
/*  747 */     if (!hasLikelihoodValue()) {
/*  748 */       if (this.likelihood == null) initLikelihood();
/*  749 */       this.likelihoodValue = 0.0D;
/*  750 */       for (Likelihood l : this.likelihood.values()) {
/*  751 */         this.likelihoodValue += l.getLikelihoodValue();
/*      */       }
/*      */ 
/*  754 */       this.likelihoodValue = (-this.likelihoodValue);
/*      */     }
/*  756 */     if (this.isUsingOneTimeGraphicMemory) {
/*  757 */       this.isUsingOneTimeGraphicMemory = false;
/*  758 */       this.videocard.freeMemory();
/*  759 */       this.videocard = null;
/*      */     }
/*  761 */     return this.likelihoodValue;
/*      */   }
/*      */ 
/*      */   public void deleteLikelihoodComputation() {
/*  765 */     this.likelihood = null;
/*      */   }
/*      */ 
/*      */   private void resetLikelihoodValue() {
/*  769 */     this.likelihoodValue = -1.0D;
/*      */   }
/*      */ 
/*      */   private boolean hasLikelihoodValue() {
/*  773 */     return this.likelihoodValue != -1.0D;
/*      */   }
/*      */ 
/*      */   public boolean isBetterThan(Tree t)
/*      */     throws UnrootableTreeException, NullAncestorException
/*      */   {
/*  786 */     return getEvaluation() < t.getEvaluation();
/*      */   }
/*      */ 
/*      */   public boolean isBetterThan(double evaluation)
/*      */     throws UnrootableTreeException, NullAncestorException
/*      */   {
/*  799 */     return getEvaluation() < evaluation;
/*      */   }
/*      */ 
/*      */   public void markAllNodesToReEvaluate() {
/*  803 */     resetLikelihoodValue();
/*  804 */     if (this.likelihood != null)
/*  805 */       for (Likelihood l : this.likelihood.values())
/*  806 */         l.markAllInodesToUpdate();
/*      */   }
/*      */ 
/*      */   public void markNodeToReEvaluate(Node node)
/*      */     throws NullAncestorException
/*      */   {
/*  812 */     resetLikelihoodValue();
/*  813 */     if (this.likelihood != null) {
/*  814 */       for (Likelihood l : this.likelihood.values()) {
/*  815 */         l.markInodeToUpdate(node);
/*      */       }
/*  817 */       for (; node != this.root; 
/*  819 */         ???.hasNext())
/*      */       {
/*  818 */         node = node.getAncestorNode();
/*  819 */         ??? = this.likelihood.values().iterator(); continue; Likelihood l = (Likelihood)???.next();
/*  820 */         l.markInodeToUpdate(node);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public List<Charset> getPartitions()
/*      */   {
/*  827 */     return new ArrayList(this.dataset.getPartitionCharsets());
/*      */   }
/*      */ 
/*      */   public void setEvaluationModel(Parameters.EvaluationModel model) {
/*  831 */     this.evaluationModel = model;
/*  832 */     for (Charset c : this.dataset.getPartitionCharsets()) {
/*  833 */       ((Map)this.rateParameters.get(c)).clear();
/*      */     }
/*  835 */     resetLikelihoodValue();
/*  836 */     this.likelihood = null;
/*      */   }
/*      */ 
/*      */   public void setEvaluationStateFrequencies(Parameters.EvaluationStateFrequencies freq) {
/*  840 */     this.evaluationStateFrequencies = freq;
/*  841 */     resetLikelihoodValue();
/*  842 */     this.likelihood = null;
/*      */   }
/*      */ 
/*      */   public void setEvaluationRateParameter(Charset c, RateParameter param, double newValue) {
/*  846 */     resetLikelihoodValue();
/*  847 */     if (this.likelihood != null) {
/*  848 */       ((Likelihood)this.likelihood.get(c)).updateRateParameter(param, newValue);
/*      */     }
/*  850 */     ((Map)this.rateParameters.get(c)).put(param, Double.valueOf(newValue));
/*      */   }
/*      */ 
/*      */   public void setEvaluationRate(Parameters.EvaluationRate rate) {
/*  854 */     this.evaluationRate = rate;
/*  855 */     resetLikelihoodValue();
/*  856 */     this.likelihood = null;
/*      */   }
/*      */ 
/*      */   public void setEvaluationDistribution(Parameters.EvaluationDistribution distribution) {
/*  860 */     this.evaluationDistribution = distribution;
/*  861 */     resetLikelihoodValue();
/*  862 */     this.likelihood = null;
/*      */   }
/*      */ 
/*      */   public void setEvaluationDistributionSubsets(int nbrSubsets) {
/*  866 */     this.evaluationDistributionSubsets = nbrSubsets;
/*  867 */     resetLikelihoodValue();
/*  868 */     this.likelihood = null;
/*      */   }
/*      */ 
/*      */   public void setEvaluationDistributionShape(Charset c, double shape) {
/*  872 */     resetLikelihoodValue();
/*  873 */     if (this.likelihood != null) {
/*  874 */       ((Likelihood)this.likelihood.get(c)).updateGammaDistribution(shape);
/*      */     }
/*  876 */     this.evaluationDistributionShape.put(c, Double.valueOf(shape));
/*      */   }
/*      */ 
/*      */   public void setEvaluationPInv(Charset c, double pInv) {
/*  880 */     resetLikelihoodValue();
/*  881 */     if (this.likelihood != null) {
/*  882 */       ((Likelihood)this.likelihood.get(c)).updateInvariant(pInv);
/*      */     }
/*  884 */     this.evaluationPInv.put(c, Double.valueOf(pInv));
/*      */   }
/*      */ 
/*      */   public void setEvaluationAmongPartitionRate(Charset partition1, double apRate1, Charset partition2)
/*      */   {
/*  896 */     double mean = 1.0D;
/*  897 */     double nchar = this.dataset.getNChar();
/*  898 */     if (this.evaluationAPRate.size() > 2) {
/*  899 */       for (Map.Entry e : this.evaluationAPRate.entrySet()) {
/*  900 */         if ((!((Charset)e.getKey()).toString().equals(partition1.toString())) && (!((Charset)e.getKey()).toString().equals(partition2.toString())))
/*  901 */           mean -= this.dataset.getPartition((Charset)e.getKey()).getNChar() / nchar * ((Double)e.getValue()).doubleValue();
/*      */       }
/*      */     }
/*  904 */     mean -= this.dataset.getPartition(partition1).getNChar() / nchar * apRate1;
/*  905 */     double apRate2 = mean / (this.dataset.getPartition(partition2).getNChar() / nchar);
/*  906 */     resetLikelihoodValue();
/*  907 */     if (this.likelihood != null) {
/*  908 */       ((Likelihood)this.likelihood.get(partition1)).updateAmongPartitionRate(apRate1);
/*  909 */       ((Likelihood)this.likelihood.get(partition2)).updateAmongPartitionRate(apRate2);
/*      */     }
/*  911 */     this.evaluationAPRate.put(partition1, Double.valueOf(apRate1));
/*  912 */     this.evaluationAPRate.put(partition2, Double.valueOf(apRate2));
/*      */   }
/*      */ 
/*      */   public boolean setEvaluationAmongPartitionRate(Map<Charset, Double> apr)
/*      */   {
/*  925 */     double mean = 0.0D;
/*  926 */     double nchar = this.dataset.getNChar();
/*  927 */     for (Map.Entry e : apr.entrySet()) {
/*  928 */       mean += this.dataset.getPartition((Charset)e.getKey()).getNChar() / nchar * ((Double)e.getValue()).doubleValue();
/*      */     }
/*  930 */     if (mean == 1.0D) {
/*  931 */       resetLikelihoodValue();
/*  932 */       if (this.likelihood != null) {
/*  933 */         for (Map.Entry e : apr.entrySet()) {
/*  934 */           ((Likelihood)this.likelihood.get(e.getKey())).updateAmongPartitionRate(((Double)e.getValue()).doubleValue());
/*      */         }
/*      */       }
/*  937 */       this.evaluationAPRate.putAll(apr);
/*  938 */       return true;
/*      */     }
/*  940 */     return false;
/*      */   }
/*      */ 
/*      */   public void setEvaluationParameters(Parameters parameters) {
/*  944 */     this.dataset = parameters.dataset;
/*  945 */     this.evaluationRate = parameters.evaluationRate;
/*  946 */     this.evaluationModel = parameters.evaluationModel;
/*  947 */     this.evaluationStateFrequencies = parameters.evaluationStateFrequencies;
/*  948 */     this.evaluationDistribution = parameters.evaluationDistribution;
/*  949 */     this.evaluationDistributionSubsets = parameters.evaluationDistributionSubsets;
/*  950 */     this.rateParameters.clear();
/*  951 */     this.evaluationDistributionShape.clear();
/*  952 */     this.evaluationPInv.clear();
/*  953 */     this.evaluationAPRate.clear();
/*  954 */     for (Charset c : this.dataset.getPartitionCharsets()) {
/*  955 */       Map map = new HashMap();
/*  956 */       for (RateParameter r : RateParameter.getParametersOfModel(this.evaluationModel)) {
/*  957 */         map.put(r, (Double)parameters.getRateParameters(c).get(r));
/*      */       }
/*  959 */       this.rateParameters.put(c, map);
/*  960 */       this.evaluationDistributionShape.put(c, Double.valueOf(parameters.getEvaluationDistributionShape(c)));
/*  961 */       this.evaluationPInv.put(c, Double.valueOf(parameters.getEvaluationPInv(c)));
/*  962 */       this.evaluationAPRate.put(c, Double.valueOf(1.0D));
/*      */     }
/*  964 */     resetLikelihoodValue();
/*  965 */     this.likelihood = null;
/*      */   }
/*      */ 
/*      */   public Parameters.EvaluationModel getEvaluationModel() {
/*  969 */     return this.evaluationModel;
/*      */   }
/*      */ 
/*      */   public Parameters.EvaluationStateFrequencies getEvaluationStateFrequencies() {
/*  973 */     return this.evaluationStateFrequencies;
/*      */   }
/*      */ 
/*      */   public Parameters.EvaluationRate getEvaluationRate() {
/*  977 */     return this.evaluationRate;
/*      */   }
/*      */ 
/*      */   public Map<RateParameter, Double> getEvaluationRateParameters(Charset c) {
/*  981 */     return (Map)this.rateParameters.get(c);
/*      */   }
/*      */ 
/*      */   public Parameters.EvaluationDistribution getEvaluationDistribution() {
/*  985 */     return this.evaluationDistribution;
/*      */   }
/*      */ 
/*      */   public int getEvaluationDistributionSubsets() {
/*  989 */     return this.evaluationDistributionSubsets;
/*      */   }
/*      */ 
/*      */   public double getEvaluationGammaShape(Charset c) {
/*  993 */     return ((Double)this.evaluationDistributionShape.get(c)).doubleValue();
/*      */   }
/*      */ 
/*      */   public double getEvaluationPInv(Charset c) {
/*  997 */     return ((Double)this.evaluationPInv.get(c)).doubleValue();
/*      */   }
/*      */ 
/*      */   public double getEvaluationAmongPartitionRate(Charset c) {
/* 1001 */     return ((Double)this.evaluationAPRate.get(c)).doubleValue();
/*      */   }
/*      */ 
/*      */   public Dataset getDataset() {
/* 1005 */     return this.dataset;
/*      */   }
/*      */ 
/*      */   private void computeLevelAndNbrOfLeaves() throws UnrootableTreeException, NullAncestorException
/*      */   {
/* 1010 */     if (!isRooted()) root();
/*      */     Node n;
/* 1011 */     for (Iterator localIterator = this.leaves.iterator(); localIterator.hasNext(); 
/* 1014 */       n != null)
/*      */     {
/* 1011 */       Node leaf = (Node)localIterator.next();
/* 1012 */       n = leaf;
/* 1013 */       int currentLevel = 1;
/* 1014 */       continue;
/* 1015 */       if (!this.nbrOfLeaves.containsKey(n)) this.nbrOfLeaves.put(n, Integer.valueOf(1)); else
/* 1016 */         this.nbrOfLeaves.put(n, Integer.valueOf(((Integer)this.nbrOfLeaves.get(n)).intValue() + 1));
/* 1017 */       if (!this.level.containsKey(n)) this.level.put(n, Integer.valueOf(currentLevel));
/* 1018 */       else if (((Integer)this.level.get(n)).intValue() < currentLevel) this.level.put(n, Integer.valueOf(currentLevel));
/* 1019 */       if (n == this.root) { n = null;
/*      */       } else {
/* 1021 */         if (!isRooted()) root();
/* 1022 */         n = n.getAncestorNode();
/*      */       }
/* 1024 */       currentLevel++;
/*      */     }
/*      */   }
/*      */ 
/*      */   public String getLongestTaxon()
/*      */   {
/* 1030 */     return this.dataset.getLongestTaxon();
/*      */   }
/*      */ 
/*      */   void fireInodeStructureChange() {
/* 1034 */     this.level.clear();
/* 1035 */     this.nbrOfLeaves.clear();
/*      */ 
/* 1037 */     if (this.outgroupRoot != null) {
/* 1038 */       boolean hasOutgroupNeighbor = false;
/* 1039 */       for (Node n : this.outgroupRoot.getNeighborNodes()) {
/* 1040 */         if (this.outgroup.contains(n)) {
/* 1041 */           hasOutgroupNeighbor = true;
/* 1042 */           break;
/*      */         }
/*      */       }
/* 1045 */       if (!hasOutgroupNeighbor)
/*      */       {
/*      */         Iterator localIterator2;
/* 1046 */         if (this.outInodes.size() > 0) {
/* 1047 */           for (??? = this.outInodes.iterator(); ???.hasNext(); 
/* 1048 */             localIterator2.hasNext())
/*      */           {
/* 1047 */             Node n = (Node)???.next();
/* 1048 */             localIterator2 = n.getNeighborNodes().iterator(); continue; Node m = (Node)localIterator2.next();
/* 1049 */             if (!this.outgroup.contains(m)) {
/* 1050 */               this.outgroupRoot = m;
/* 1051 */               markAllNodesToReEvaluate();
/*      */               try {
/* 1053 */                 root();
/*      */               } catch (Exception e) {
/* 1055 */                 e.printStackTrace();
/*      */               }
/* 1057 */               return;
/*      */             }
/*      */           }
/*      */         }
/*      */         else
/* 1062 */           for (??? = this.outgroup.iterator(); ???.hasNext(); 
/* 1063 */             localIterator2.hasNext())
/*      */           {
/* 1062 */             Node n = (Node)???.next();
/* 1063 */             localIterator2 = n.getNeighborNodes().iterator(); continue; Node m = (Node)localIterator2.next();
/* 1064 */             if (!this.outgroup.contains(m)) {
/* 1065 */               this.outgroupRoot = m;
/* 1066 */               markAllNodesToReEvaluate();
/*      */               try {
/* 1068 */                 root();
/*      */               } catch (Exception e) {
/* 1070 */                 e.printStackTrace();
/*      */               }
/* 1072 */               return;
/*      */             }
/*      */           }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public Parameters.LikelihoodCalculationType getLikelihoodCalculationType()
/*      */   {
/* 1086 */     return this.likelihoodType;
/*      */   }
/*      */ 
/*      */   public int getLevel(Node node)
/*      */     throws UnrootableTreeException, NullAncestorException
/*      */   {
/* 1098 */     if (this.level.isEmpty()) computeLevelAndNbrOfLeaves();
/* 1099 */     return ((Integer)this.level.get(node)).intValue();
/*      */   }
/*      */ 
/*      */   public Map<Integer, List<Node>> getNodesInLevels()
/*      */     throws UnrootableTreeException, NullAncestorException
/*      */   {
/* 1113 */     Map levelsOfNodes = new HashMap();
/* 1114 */     for (Node node : this.nodes) {
/* 1115 */       int nodeLevel = getLevel(node);
/* 1116 */       if (!levelsOfNodes.containsKey(Integer.valueOf(nodeLevel))) {
/* 1117 */         List levelList = new ArrayList();
/* 1118 */         levelList.add(node);
/* 1119 */         levelsOfNodes.put(Integer.valueOf(nodeLevel), levelList);
/*      */       } else {
/* 1121 */         ((List)levelsOfNodes.get(Integer.valueOf(nodeLevel))).add(node);
/*      */       }
/*      */     }
/* 1124 */     return levelsOfNodes;
/*      */   }
/*      */ 
/*      */   public int getNumOfLeavesUnder(Node node)
/*      */     throws UnrootableTreeException, NullAncestorException
/*      */   {
/* 1136 */     if (this.nbrOfLeaves.isEmpty()) computeLevelAndNbrOfLeaves();
/* 1137 */     return ((Integer)this.nbrOfLeaves.get(node)).intValue();
/*      */   }
/*      */ 
/*      */   public Set<Node> getAllNodesUnderNeighbor(Node node, Node.Neighbor neighbor)
/*      */   {
/* 1149 */     Set set = new HashSet();
/* 1150 */     Stack stack = new Stack();
/* 1151 */     Set excludedNeighbors = new HashSet();
/* 1152 */     excludedNeighbors.add(node);
/* 1153 */     stack.add(node.getNeighbor(neighbor));
/*      */     do {
/* 1155 */       Node current = (Node)stack.pop();
/* 1156 */       if (!current.isLeaf()) {
/* 1157 */         for (Node n : current.getNeighborNodes()) {
/* 1158 */           if (!excludedNeighbors.contains(n)) {
/* 1159 */             stack.push(n);
/*      */           }
/*      */         }
/*      */       }
/* 1163 */       set.add(current);
/* 1164 */       excludedNeighbors.add(current);
/*      */     }
/* 1154 */     while (!
/* 1165 */       stack.isEmpty());
/* 1166 */     return set;
/*      */   }
/*      */ 
/*      */   public List<Node> getPreorderTraversal(Node node)
/*      */     throws UnrootableTreeException
/*      */   {
/* 1177 */     if (!isRooted()) root();
/* 1178 */     List traversal = new ArrayList();
/* 1179 */     Stack stack = new Stack();
/* 1180 */     stack.add(node);
/*      */     do {
/* 1182 */       Node current = (Node)stack.pop();
/* 1183 */       if (!current.isLeaf()) {
/* 1184 */         for (Node n : current.getChildren()) {
/* 1185 */           stack.push(n);
/*      */         }
/*      */       }
/* 1188 */       traversal.add(current);
/*      */     }
/* 1181 */     while (!
/* 1189 */       stack.isEmpty());
/* 1190 */     return traversal;
/*      */   }
/*      */ 
/*      */   public List<Node> getPostorderTraversal(Node node)
/*      */     throws UnrootableTreeException
/*      */   {
/* 1201 */     if (!isRooted()) root();
/* 1202 */     List traversal = new ArrayList();
/* 1203 */     Stack stack = new Stack();
/* 1204 */     stack.add(node);
/*      */     do {
/* 1206 */       Node current = (Node)stack.peek();
/* 1207 */       if (!current.isLeaf()) {
/* 1208 */         List children = current.getChildren();
/* 1209 */         if ((children.size() > 0) && (!traversal.contains(children.get(0)))) {
/* 1210 */           stack.push((Node)children.get(0));
/* 1211 */         } else if ((children.size() > 1) && (!traversal.contains(children.get(1)))) {
/* 1212 */           stack.push((Node)children.get(1));
/* 1213 */         } else if ((children.size() > 2) && (!traversal.contains(children.get(2)))) {
/* 1214 */           stack.push((Node)children.get(2));
/*      */         } else {
/* 1216 */           traversal.add(current);
/* 1217 */           stack.pop();
/*      */         }
/*      */       } else {
/* 1220 */         traversal.add(current);
/* 1221 */         stack.pop();
/*      */       }
/*      */     }
/* 1205 */     while (!
/* 1223 */       stack.isEmpty());
/* 1224 */     return traversal;
/*      */   }
/*      */ 
/*      */   public TreesBlock.NewickTreeString toNewick(boolean printINodes, boolean printSupportValues) throws UnrootableTreeException, NullAncestorException {
/* 1228 */     TreesBlock.NewickTreeString newick = new TreesBlock.NewickTreeString();
/* 1229 */     root();
/* 1230 */     newick.setTreeString(generateNewick(this.root, printINodes, printSupportValues));
/* 1231 */     newick.setRootType("R");
/* 1232 */     newick.setStarred(false);
/* 1233 */     return newick;
/*      */   }
/*      */ 
/*      */   public String toNewickLine(boolean printINodes, boolean printSupportValues) throws UnrootableTreeException, NullAncestorException {
/* 1237 */     String newick = "TREE '" + this.name + "' = [&R] ";
/* 1238 */     root();
/* 1239 */     if (this.root.getClass() == ConsensusNode.class) newick = newick + "[&C] ";
/* 1240 */     newick = newick + generateNewick(this.root, printINodes, printSupportValues) + ";";
/* 1241 */     return newick;
/*      */   }
/*      */ 
/*      */   public String toNewickLineWithML(String treeName, boolean printINodes, boolean printSupportValues) throws UnrootableTreeException, NullAncestorException, BadLocationException {
/* 1245 */     DefaultStyledDocument doc = getEvaluationString();
/* 1246 */     String newick = "TREE '" + treeName + "' [" + doc.getText(0, doc.getLength()) + "]" + " = [&R] ";
/* 1247 */     root();
/* 1248 */     if (this.root.getClass() == ConsensusNode.class) newick = newick + "[&C] ";
/* 1249 */     newick = newick + generateNewick(this.root, printINodes, printSupportValues) + ";";
/* 1250 */     return newick;
/*      */   }
/*      */ 
/*      */   public DefaultStyledDocument getEvaluationString() throws BadLocationException, UnrootableTreeException, NullAncestorException {
/* 1254 */     String NORMAL = "Normal";
/* 1255 */     String ITALIC = "Italic";
/* 1256 */     String BOLD = "Bold";
/* 1257 */     Hashtable paraStyles = new Hashtable();
/* 1258 */     SimpleAttributeSet attr = new SimpleAttributeSet();
/* 1259 */     paraStyles.put("Normal", attr);
/* 1260 */     attr = new SimpleAttributeSet();
/* 1261 */     StyleConstants.setItalic(attr, true);
/* 1262 */     paraStyles.put("Italic", attr);
/* 1263 */     attr = new SimpleAttributeSet();
/* 1264 */     StyleConstants.setBold(attr, true);
/* 1265 */     paraStyles.put("Bold", attr);
/* 1266 */     AttributeSet defaultStyle = (AttributeSet)paraStyles.get("Normal");
/*      */ 
/* 1268 */     AttributeSet boldStyle = (AttributeSet)paraStyles.get("Bold");
/* 1269 */     DefaultStyledDocument doc = new DefaultStyledDocument();
/* 1270 */     boolean multipart = this.dataset.getPartitionCharsets().size() > 1;
/*      */ 
/* 1272 */     doc.insertString(doc.getLength(), "Likelihood of ", defaultStyle);
/* 1273 */     doc.insertString(doc.getLength(), Tools.doubletoString(getEvaluation(), 4), boldStyle);
/* 1274 */     doc.insertString(doc.getLength(), " computed using ", defaultStyle);
/* 1275 */     doc.insertString(doc.getLength(), this.evaluationModel + " model", boldStyle);
/* 1276 */     if (this.evaluationModel.isEmpirical()) {
/* 1277 */       doc.insertString(doc.getLength(), " (with " + this.evaluationStateFrequencies + " equilibrium amino acid frequencies)", boldStyle);
/*      */     }
/* 1279 */     doc.insertString(doc.getLength(), " with a R matrix for ", defaultStyle);
/* 1280 */     doc.insertString(doc.getLength(), getEvaluationRate(), boldStyle);
/* 1281 */     if (this.evaluationDistribution == Parameters.EvaluationDistribution.NONE) {
/* 1282 */       doc.insertString(doc.getLength(), ", without rate heterogeneity", defaultStyle);
/*      */     } else {
/* 1284 */       doc.insertString(doc.getLength(), ", with ", defaultStyle);
/* 1285 */       doc.insertString(doc.getLength(), getEvaluationDistribution() + " distribution", boldStyle);
/* 1286 */       if (this.evaluationDistribution == Parameters.EvaluationDistribution.GAMMA) {
/* 1287 */         doc.insertString(doc.getLength(), " (" + this.evaluationDistributionSubsets + " subsets,", defaultStyle);
/* 1288 */         for (Charset C : this.dataset.getPartitionCharsets()) {
/* 1289 */           doc.insertString(doc.getLength(), " shape" + (multipart ? " in " + C.getLabel() : "") + " = " + Tools.doubletoString(((Double)this.evaluationDistributionShape.get(C)).doubleValue(), 4), defaultStyle);
/* 1290 */           doc.insertString(doc.getLength(), ",", defaultStyle);
/*      */         }
/* 1292 */         doc.remove(doc.getLength() - 1, 1);
/* 1293 */         doc.insertString(doc.getLength(), ")", defaultStyle);
/*      */       }
/*      */     }
/* 1296 */     doc.insertString(doc.getLength(), " and", defaultStyle);
/* 1297 */     for (Charset C : this.dataset.getPartitionCharsets()) {
/* 1298 */       if (((Double)this.evaluationPInv.get(C)).doubleValue() < 0.0001D) {
/* 1299 */         doc.insertString(doc.getLength(), " without invariable sites" + (multipart ? " for " + C.getLabel() : ""), defaultStyle);
/*      */       } else {
/* 1301 */         doc.insertString(doc.getLength(), " with ", defaultStyle);
/* 1302 */         doc.insertString(doc.getLength(), Tools.doubletoString(((Double)this.evaluationPInv.get(C)).doubleValue() * 100.0D, 2) + "% P-Invariant", boldStyle);
/* 1303 */         doc.insertString(doc.getLength(), multipart ? " for " + C.getLabel() : "", defaultStyle);
/*      */       }
/* 1305 */       doc.insertString(doc.getLength(), ",", defaultStyle);
/*      */     }
/* 1307 */     doc.remove(doc.getLength() - 1, 1);
/* 1308 */     if ((this.evaluationModel.getNumRateParameters() > 0) && (!this.evaluationModel.isEmpirical())) {
/* 1309 */       doc.insertString(doc.getLength(), ". Model parameters :", boldStyle);
/*      */       Iterator localIterator2;
/* 1310 */       for (??? = this.dataset.getPartitionCharsets().iterator(); ???.hasNext(); 
/* 1311 */         localIterator2.hasNext())
/*      */       {
/* 1310 */         Charset C = (Charset)???.next();
/* 1311 */         localIterator2 = ((Map)this.rateParameters.get(C)).keySet().iterator(); continue; RateParameter R = (RateParameter)localIterator2.next();
/* 1312 */         doc.insertString(doc.getLength(), " " + R.verbose() + (multipart ? " in " + C.getLabel() : "") + " = " + Tools.doubletoString(((Double)((Map)this.rateParameters.get(C)).get(R)).doubleValue(), 4), defaultStyle);
/* 1313 */         doc.insertString(doc.getLength(), ",", defaultStyle);
/*      */       }
/*      */ 
/* 1316 */       doc.remove(doc.getLength() - 1, 1);
/*      */     }
/* 1318 */     if (this.dataset.getPartitionCharsets().size() > 1) {
/* 1319 */       doc.insertString(doc.getLength(), ". Among-Partition rate variation :", defaultStyle);
/* 1320 */       for (Charset C : this.dataset.getPartitionCharsets()) {
/* 1321 */         doc.insertString(doc.getLength(), " rate of ", defaultStyle);
/* 1322 */         doc.insertString(doc.getLength(), Tools.doubletoString(((Double)this.evaluationAPRate.get(C)).doubleValue(), 2), boldStyle);
/* 1323 */         doc.insertString(doc.getLength(), multipart ? " for " + C.getLabel() : "", defaultStyle);
/* 1324 */         doc.insertString(doc.getLength(), ",", defaultStyle);
/*      */       }
/* 1326 */       doc.remove(doc.getLength() - 1, 1);
/*      */     }
/* 1328 */     return doc;
/*      */   }
/*      */ 
/*      */   public void parseEvaluationString(String comment)
/*      */     throws Exception
/*      */   {
/* 1336 */     boolean multipart = this.dataset.getPartitionCharsets().size() > 1;
/* 1337 */     Charset C = (Charset)this.dataset.getPartitionCharsets().iterator().next();
/* 1338 */     String sub = comment.substring(comment.indexOf("Likelihood of ") + "Likelihood of ".length(), comment.indexOf(" computed using "));
/* 1339 */     this.likelihoodValue = Tools.parseDouble(sub);
/* 1340 */     sub = comment.substring(comment.indexOf(" computed using ") + " computed using ".length(), comment.indexOf(" model"));
/* 1341 */     this.evaluationModel = Parameters.EvaluationModel.valueOf(sub);
/* 1342 */     if (comment.indexOf(" equilibrium amino acid frequencies)") > 0) {
/* 1343 */       sub = comment.substring(comment.indexOf(" model (with ") + " model (with ".length(), comment.indexOf(" equilibrium amino acid frequencies)"));
/* 1344 */       this.evaluationStateFrequencies = Parameters.EvaluationStateFrequencies.valueOf(sub);
/*      */     }
/* 1346 */     sub = comment.substring(comment.indexOf(" with a R matrix for ") + " with a R matrix for ".length(), comment.indexOf(", "));
/* 1347 */     this.evaluationRate = Parameters.EvaluationRate.valueOf(sub);
/* 1348 */     comment = comment.substring(comment.indexOf(", "));
/*      */     String str1;
/* 1349 */     if (comment.startsWith(", without rate heterogeneity")) {
/* 1350 */       this.evaluationDistribution = Parameters.EvaluationDistribution.NONE;
/*      */     } else {
/* 1352 */       sub = comment.substring(comment.indexOf(", with ") + ", with ".length(), comment.indexOf(" distribution"));
/* 1353 */       this.evaluationDistribution = Parameters.EvaluationDistribution.valueOf(sub);
/* 1354 */       if (this.evaluationDistribution == Parameters.EvaluationDistribution.GAMMA) {
/* 1355 */         String gamma = comment.substring(comment.indexOf(" (") + " (".length(), comment.indexOf(")"));
/* 1356 */         sub = gamma.substring(0, gamma.indexOf(" subsets,"));
/* 1357 */         this.evaluationDistributionSubsets = Integer.parseInt(sub);
/* 1358 */         gamma = gamma.substring(gamma.indexOf(" subsets,") + " subsets,".length());
/* 1359 */         String[] gammaParams = gamma.split(",");
/*      */         String[] arrayOfString2;
/* 1360 */         int i = (arrayOfString2 = gammaParams).length; for (str1 = 0; str1 < i; str1++) { p = arrayOfString2[str1];
/* 1361 */           sub = p.substring(p.indexOf(" = ") + " = ".length());
/* 1362 */           if (multipart) C = this.dataset.getCharset(p.substring(p.indexOf(" in ") + " in ".length(), p.indexOf(" = ")));
/* 1363 */           this.evaluationDistributionShape.put(C, Double.valueOf(Tools.parseDouble(sub)));
/*      */         }
/*      */       }
/*      */     }
/* 1367 */     comment = comment.substring(comment.indexOf(" and") + " and".length());
/*      */     String[] arrayOfString1;
/* 1368 */     if (comment.contains(". Among-Partition rate variation :")) {
/* 1369 */       String[] arps = comment.substring(comment.indexOf(". Among-Partition rate variation :") + ". Among-Partition rate variation :".length()).split(",");
/* 1370 */       comment = comment.substring(0, comment.indexOf(". Among-Partition rate variation :"));
/* 1371 */       str1 = (arrayOfString1 = arps).length; for (p = 0; p < str1; p++) { String p = arrayOfString1[p];
/* 1372 */         if (multipart) {
/* 1373 */           sub = p.substring(" rate of ".length(), p.indexOf(" for "));
/* 1374 */           C = this.dataset.getCharset(p.substring(p.indexOf(" for ") + " for ".length()));
/*      */         } else {
/* 1376 */           sub = p.substring(" rate of ".length());
/*      */         }
/* 1378 */         this.evaluationAPRate.put(C, Double.valueOf(Tools.parseDouble(sub)));
/*      */       }
/*      */     }
/* 1381 */     if (comment.contains(". Model parameters :")) {
/* 1382 */       String[] models = comment.substring(comment.indexOf(". Model parameters :") + ". Model parameters :".length()).split(",");
/* 1383 */       comment = comment.substring(0, comment.indexOf(". Model parameters :"));
/* 1384 */       String str2 = (arrayOfString1 = models).length; for (p = 0; p < str2; p++) { String p = arrayOfString1[p];
/* 1385 */         sub = p.substring(p.indexOf(" = ") + " = ".length());
/* 1386 */         RateParameter R = RateParameter.verboseValueOf(p.substring(1, p.indexOf(multipart ? " in " : " = ")));
/* 1387 */         if (multipart) C = this.dataset.getCharset(p.substring(p.indexOf(" in ") + " in ".length(), p.indexOf(" = ")));
/* 1388 */         ((Map)this.rateParameters.get(C)).put(R, Double.valueOf(Tools.parseDouble(sub)));
/*      */       }
/*      */     }
/* 1391 */     String[] pinvs = comment.split(",");
/* 1392 */     String str3 = (arrayOfString1 = pinvs).length; for (String p = 0; p < str3; p++) { String p = arrayOfString1[p];
/*      */       double pinv;
/* 1394 */       if (p.startsWith(" without invariable sites")) {
/* 1395 */         double pinv = 0.0D;
/* 1396 */         if (multipart) C = this.dataset.getCharset(p.substring(p.indexOf(" for ") + " for ".length())); 
/*      */       }
/* 1398 */       else { sub = p.substring(" with ".length(), p.indexOf("% P-Invariant"));
/* 1399 */         pinv = Tools.parseDouble(sub);
/* 1400 */         pinv /= 100.0D;
/* 1401 */         if (multipart) C = this.dataset.getCharset(p.substring(p.indexOf(" for ") + " for ".length()));
/*      */       }
/* 1403 */       this.evaluationPInv.put(C, Double.valueOf(pinv));
/*      */     }
/*      */   }
/*      */ 
/*      */   private String generateNewick(Node node, boolean internalLabels, boolean supportValues)
/*      */     throws NullAncestorException
/*      */   {
/* 1423 */     String newick = "";
/* 1424 */     if (node.isLeaf()) {
/* 1425 */       newick = newick + node.getLabel().replace(' ', '_');
/* 1426 */       if (node.getAncestorBranchLength() > 0.0D) {
/* 1427 */         newick = newick + ":" + node.getAncestorBranchLength();
/*      */       }
/* 1429 */       return newick;
/*      */     }
/* 1431 */     List children = node.getChildren();
/* 1432 */     for (int i = 0; i < children.size(); i++) {
/* 1433 */       if (i == 0) {
/* 1434 */         newick = newick + "(";
/*      */       }
/* 1436 */       newick = newick + generateNewick((Node)children.get(i), internalLabels, supportValues);
/* 1437 */       if (i < children.size() - 1) {
/* 1438 */         newick = newick + ",";
/*      */       }
/*      */     }
/* 1441 */     newick = newick + ")";
/* 1442 */     if (internalLabels) {
/* 1443 */       if (node.getLabel() != null) newick = newick + node.getLabel().replace(' ', '_'); 
/*      */     }
/* 1444 */     else if ((supportValues) && 
/* 1445 */       (node != this.root) && (node.getClass() == ConsensusNode.class)) {
/* 1446 */       newick = newick + Tools.doubletoString(((ConsensusNode)node).getAncestorBranchStrength(), 2);
/*      */     }
/*      */ 
/* 1449 */     if ((node != this.root) && (node.getAncestorBranchLength() > 0.0D)) {
/* 1450 */       newick = newick + ":" + node.getAncestorBranchLength();
/*      */     }
/* 1452 */     if ((internalLabels) && (supportValues) && (node != this.root) && (node.getClass() == ConsensusNode.class)) {
/* 1453 */       newick = newick + "[C=" + Tools.doubleToPercent(((ConsensusNode)node).getAncestorBranchStrength(), 0) + "]";
/*      */     }
/* 1455 */     return newick;
/*      */   }
/*      */ 
/*      */   public double[][] getAncestralStates(Node node) throws UnrootableTreeException, NullAncestorException
/*      */   {
/* 1460 */     if (this.likelihood == null) initLikelihood();
/* 1461 */     createOneTimeGraphicMemeory();
/* 1462 */     int numOfStates = this.dataset.getDataType().numOfStates();
/* 1463 */     double[][] ancestralStates = new double[this.dataset.getFullNChar()][numOfStates];
/* 1464 */     BitSet existingPosition = new BitSet(this.dataset.getFullNChar());
/*      */     double[][] asc;
/*      */     int site;
/* 1465 */     for (Iterator localIterator1 = this.dataset.getPartitionCharsets().iterator(); localIterator1.hasNext(); 
/* 1468 */       site < asc.length)
/*      */     {
/* 1465 */       Charset c = (Charset)localIterator1.next();
/* 1466 */       Dataset.Partition p = this.dataset.getPartition(c);
/* 1467 */       asc = ((Likelihood)this.likelihood.get(c)).getAncestralStates(node);
/* 1468 */       site = 0; continue;
/* 1469 */       for (int state = 0; state < asc[site].length; state++)
/* 1470 */         for (Iterator localIterator2 = p.getDatasetPosition(site).iterator(); localIterator2.hasNext(); ) { int position = ((Integer)localIterator2.next()).intValue();
/* 1471 */           ancestralStates[position][state] = asc[site][state];
/* 1472 */           existingPosition.set(position);
/*      */         }
/* 1468 */       site++;
/*      */     }
/*      */ 
/* 1477 */     double[][] result = new double[existingPosition.cardinality()][numOfStates];
/* 1478 */     int i = 0; for (int j = 0; i < ancestralStates.length; i++) {
/* 1479 */       if (existingPosition.get(i)) {
/* 1480 */         System.arraycopy(ancestralStates[i], 0, result[j], 0, numOfStates);
/* 1481 */         j++;
/*      */       }
/*      */     }
/*      */ 
/* 1485 */     if (this.isUsingOneTimeGraphicMemory) {
/* 1486 */       this.isUsingOneTimeGraphicMemory = false;
/* 1487 */       this.videocard.freeMemory();
/* 1488 */       this.videocard = null;
/*      */     }
/*      */ 
/* 1491 */     return result;
/*      */   }
/*      */ 
/*      */   public String getMostProbableAncestralSequence(Node node) throws Exception {
/* 1495 */     double[][] as = getAncestralStates(node);
/* 1496 */     DataType dataType = this.dataset.getDataType();
/* 1497 */     StringBuilder sb = new StringBuilder();
/* 1498 */     for (int site = 0; site < as.length; site++) {
/* 1499 */       sb.append(dataType.getMostProbableData(as[site]).toString());
/*      */     }
/* 1501 */     return sb.toString();
/*      */   }
/*      */ 
/*      */   public DefaultStyledDocument printAncestralStates(Node node) throws Exception {
/* 1505 */     double[][] as = getAncestralStates(node);
/* 1506 */     DataType dataType = this.dataset.getDataType();
/* 1507 */     String NORMAL = "Normal";
/* 1508 */     String ITALIC = "Italic";
/* 1509 */     String BOLD = "Bold";
/* 1510 */     DefaultStyledDocument doc = new DefaultStyledDocument();
/* 1511 */     Hashtable paraStyles = new Hashtable();
/* 1512 */     SimpleAttributeSet attr = new SimpleAttributeSet();
/* 1513 */     paraStyles.put("Normal", attr);
/* 1514 */     attr = new SimpleAttributeSet();
/* 1515 */     StyleConstants.setItalic(attr, true);
/* 1516 */     paraStyles.put("Italic", attr);
/* 1517 */     attr = new SimpleAttributeSet();
/* 1518 */     StyleConstants.setBold(attr, true);
/* 1519 */     paraStyles.put("Bold", attr);
/* 1520 */     AttributeSet defaultStyle = (AttributeSet)paraStyles.get("Normal");
/*      */ 
/* 1522 */     AttributeSet boldStyle = (AttributeSet)paraStyles.get("Bold");
/* 1523 */     doc.insertString(doc.getLength(), "Most probable sequence for node " + node.getLabel() + " :\n\n", boldStyle);
/* 1524 */     for (int site = 0; site < as.length; site++) {
/* 1525 */       doc.insertString(doc.getLength(), dataType.getMostProbableData(as[site]).toString(), defaultStyle);
/*      */     }
/* 1527 */     doc.insertString(doc.getLength(), "\n\nConditional likelihood distribution by site for node " + node.getLabel() + " :\n\n", boldStyle);
/* 1528 */     doc.insertString(doc.getLength(), "Site", boldStyle);
/* 1529 */     doc.insertString(doc.getLength(), "\tProbable ancestral state", boldStyle);
/* 1530 */     for (int state = 0; state < as[0].length; state++) {
/* 1531 */       doc.insertString(doc.getLength(), "\t" + dataType.getDataWithState(state) + " probability", boldStyle);
/*      */     }
/* 1533 */     for (int state = 0; state < as[0].length; state++) {
/* 1534 */       doc.insertString(doc.getLength(), "\t" + dataType.getDataWithState(state) + " conditional likelihood", boldStyle);
/*      */     }
/* 1536 */     doc.insertString(doc.getLength(), "\n", boldStyle);
/* 1537 */     for (int site = 0; site < as.length; site++) {
/* 1538 */       doc.insertString(doc.getLength(), site + 1, defaultStyle);
/* 1539 */       doc.insertString(doc.getLength(), "\t" + dataType.getMostProbableData(as[site]), defaultStyle);
/* 1540 */       double sum = 0.0D;
/* 1541 */       for (int state = 0; state < as[site].length; state++) {
/* 1542 */         sum += as[site][state];
/*      */       }
/* 1544 */       for (int state = 0; state < as[site].length; state++) {
/* 1545 */         doc.insertString(doc.getLength(), "\t" + Tools.doubleToPercent(as[site][state] / sum, 2), boldStyle);
/*      */       }
/* 1547 */       for (int state = 0; state < as[site].length; state++) {
/* 1548 */         doc.insertString(doc.getLength(), "\t" + Tools.doubletoString(as[site][state], 2), boldStyle);
/*      */       }
/* 1550 */       doc.insertString(doc.getLength(), "\n", boldStyle);
/*      */     }
/* 1552 */     return doc;
/*      */   }
/*      */ 
/*      */   public void addMemGPUchunk(VideocardContext vc) {
/* 1556 */     this.videocard = vc;
/* 1557 */     if (this.likelihood != null)
/* 1558 */       for (Likelihood l : this.likelihood.values())
/* 1559 */         if ((l instanceof LikelihoodGpu))
/* 1560 */           ((LikelihoodGpu)l).addGPUMemory(this.videocard);
/*      */   }
/*      */ 
/*      */   private class InOutMap
/*      */   {
/*      */     Map<Node, Map<Node.Neighbor, Tree.Status>> status;
/*      */ 
/*      */     public InOutMap()
/*      */     {
/*  421 */       this.status = new HashMap();
/*  422 */       for (Node inode : Tree.this.inodes)
/*  423 */         this.status.put(inode, new HashMap());
/*      */       Iterator localIterator2;
/*  425 */       for (??? = Tree.this.inodes.iterator(); ???.hasNext(); 
/*  427 */         localIterator2.hasNext())
/*      */       {
/*  425 */         Node inode = (Node)???.next();
/*  426 */         Map map = (Map)this.status.get(inode);
/*  427 */         localIterator2 = inode.getNeighborKeys().iterator(); continue; Node.Neighbor nei = (Node.Neighbor)localIterator2.next();
/*  428 */         if (!map.containsKey(nei))
/*  429 */           map.put(nei, getNeighborsStatus(inode, nei));
/*      */       }
/*      */     }
/*      */ 
/*      */     private Tree.Status getNeighborsStatus(Node nodeFrom, Node.Neighbor neiFrom)
/*      */     {
/*  436 */       Node node = nodeFrom.getNeighbor(neiFrom);
/*  437 */       if (node.isLeaf()) {
/*  438 */         if (Tree.this.outgroup.contains(node)) return Tree.Status.OUT;
/*  439 */         return Tree.Status.IN;
/*      */       }
/*  441 */       Node.Neighbor n1 = nodeFrom.getNeighborKey(neiFrom);
/*  442 */       Map map = (Map)this.status.get(node);
/*  443 */       Node.Neighbor n2 = n1 != Node.Neighbor.A ? Node.Neighbor.A : Node.Neighbor.C;
/*  444 */       Node.Neighbor n3 = n1 != Node.Neighbor.B ? Node.Neighbor.B : Node.Neighbor.C;
/*  445 */       if (!map.containsKey(n2)) map.put(n2, getNeighborsStatus(node, n2));
/*  446 */       if (!map.containsKey(n3)) map.put(n3, getNeighborsStatus(node, n3));
/*  447 */       if (map.get(n2) != map.get(n3)) return Tree.Status.MIX;
/*  448 */       return (Tree.Status)map.get(n2);
/*      */     }
/*      */ 
/*      */     public void assignGroup(Node inode) throws UncompatibleOutgroupException
/*      */     {
/*  453 */       int i = 0; int o = 0; int m = 0;
/*  454 */       for (Tree.Status s : ((Map)this.status.get(inode)).values()) {
/*  455 */         switch (s) {
/*      */         case IN:
/*  457 */           i++;
/*  458 */           break;
/*      */         case MIX:
/*  460 */           o++;
/*  461 */           break;
/*      */         case OUT:
/*  463 */           m++;
/*      */         }
/*      */       }
/*      */ 
/*  467 */       if (o == 2) { Tree.this.outgroup.add(inode); } else {
/*  468 */         if ((m == 2) || ((i == 1) && (o == 1) && (m == 1))) throw new UncompatibleOutgroupException(Tree.this.name);
/*  469 */         if ((i == 2) && (o == 1)) Tree.this.outgroupRoot = inode;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private static enum Status
/*      */   {
/*   55 */     IN, OUT, MIX;
/*      */   }
/*      */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.Tree
 * JD-Core Version:    0.6.2
 */