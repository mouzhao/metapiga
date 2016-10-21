/*      */ package modelization;
/*      */ 
/*      */ import java.awt.BorderLayout;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.BitSet;
/*      */ import java.util.Collection;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Set;
/*      */ import java.util.concurrent.ExecutionException;
/*      */ import javax.swing.JLabel;
/*      */ import javax.swing.JOptionPane;
/*      */ import javax.swing.JPanel;
/*      */ import javax.swing.JScrollPane;
/*      */ import javax.swing.JTextArea;
/*      */ import javax.swing.SwingWorker;
/*      */ import metapiga.MainFrame;
/*      */ import metapiga.MetaPIGA;
/*      */ import metapiga.ProgressHandling;
/*      */ import metapiga.WaitingLogo.Status;
/*      */ import metapiga.exceptions.UnknownDataException;
/*      */ import metapiga.modelization.data.Data;
/*      */ import metapiga.modelization.data.DataType;
/*      */ import metapiga.modelization.modeltest.ModelSampling;
/*      */ import metapiga.parameters.CharsetsContainer;
/*      */ import metapiga.parameters.Parameters;
/*      */ import metapiga.parameters.Parameters.DistanceModel;
/*      */ import metapiga.parameters.Parameters.StartingTreeDistribution;
/*      */ import metapiga.parameters.Parameters.StartingTreePInvPi;
/*      */ import metapiga.utilities.Tools;
/*      */ import org.biojavax.bio.phylo.io.nexus.CharactersBlock;
/*      */ 
/*      */ public class DatasetTriming extends SwingWorker<WaitingLogo.Status, Object>
/*      */ {
/*      */   private static final double ambiguitiesThreshold = 0.4D;
/*      */   private final Parameters P;
/*      */   private final MainFrame mainFrame;
/*      */   private final DatasetCheck check;
/*      */   private double divergenceThreshold;
/*      */   private int nchar;
/*      */   private int columns;
/*      */   private int rows;
/*      */   private char indet;
/*      */   private char[][] alignmentMatrix;
/*      */   private int[] nexusPositions;
/*      */   private SimilarityMatrix simMatrix;
/*      */   private int[] gapsInColumn;
/*      */   private int[] aminosXInColumn;
/*      */   private int[] numColumnsWithGaps;
/*      */   private int[] gapsWindow;
/*      */   int maxGaps;
/*      */   private float[] Q;
/*      */   private float[] MDK;
/*      */   private float[] MDK_Window;
/*      */   private float[][] matrixIdentity;
/*      */ 
/*      */   public DatasetTriming(MainFrame mainFrame, Parameters currentParameters, DatasetCheck check)
/*      */   {
/*   74 */     this.mainFrame = mainFrame;
/*   75 */     this.P = currentParameters;
/*   76 */     this.check = check;
/*      */   }
/*      */ 
/*      */   public WaitingLogo.Status doInBackground() {
/*      */     try {
/*   81 */       StringBuilder sb = new StringBuilder();
/*   82 */       switch ($SWITCH_TABLE$metapiga$modelization$DatasetTriming$DatasetCheck()[this.check.ordinal()]) {
/*      */       case 2:
/*   84 */         if (checkAmbiguities(0.4D)) {
/*   85 */           sb.append("Your dataset has been tested for ambiguities.");
/*   86 */           sb.append("\n");
/*   87 */           sb.append("All sequences have less than " + Tools.doubleToPercent(0.4D, 0) + " of ambiguous positions.");
/*   88 */           sb.append("\n");
/*      */         }
/*   90 */         break;
/*      */       case 3:
/*   92 */         if (checkRedundancy()) {
/*   93 */           sb.append("Your dataset has been tested for redundancy.");
/*   94 */           sb.append("\n");
/*   95 */           sb.append("There are no identical sequences.");
/*   96 */           sb.append("\n");
/*      */         }
/*   98 */         break;
/*      */       case 4:
/*  100 */         if (checkSaturation()) {
/*  101 */           sb.append("Your dataset has been tested for saturation problems.");
/*  102 */           sb.append("\n");
/*  103 */           sb.append("Divergence among your sequences does not show obvious signs of saturation.");
/*  104 */           sb.append("\n");
/*      */         }
/*  106 */         break;
/*      */       case 5:
/*  108 */         Charset trim = trimmming();
/*  109 */         sb.append(trim.getLabel() + " as been applyed to your dataset.");
/*  110 */         sb.append("\n");
/*  111 */         sb.append("\n");
/*  112 */         if (trim.isEmpty()) {
/*  113 */           sb.append("No character has to be removed.");
/*  114 */           sb.append("\n");
/*      */         } else {
/*  116 */           sb.append("A 'trimming' charset has been created excluding the following characters:");
/*  117 */           String[] ranges = trim.getAllRanges().split(" ");
/*  118 */           for (int i = 0; i < ranges.length; i++) {
/*  119 */             if (i % 8 == 0) sb.append("\n");
/*  120 */             sb.append(ranges[i] + " ");
/*      */           }
/*  122 */           sb.append("\n");
/*      */         }
/*  124 */         sb.append("\n");
/*  125 */         sb.append("Trimming method has been selected using automated1 heuristic from trimAl.");
/*  126 */         sb.append("\n");
/*  127 */         sb.append("For the description of trimAl heuristic and methods, see:");
/*  128 */         sb.append("\n");
/*  129 */         sb.append("  trimAl: a tool for automated alignment trimming in large-scale phylogenetic analyses.");
/*  130 */         sb.append("\n");
/*  131 */         sb.append("  Capella-Gutierrez, Silla-Martinez & Gabaldon. Bioinformatics 2009 25: 1972-1973.");
/*  132 */         sb.append("\n");
/*  133 */         sb.append("  http://trimal.cgenomics.org");
/*  134 */         sb.append("\n");
/*  135 */         break;
/*      */       case 1:
/*      */       default:
/*  138 */         sb.append("Your dataset has been fully tested.");
/*  139 */         sb.append("\n");
/*  140 */         if (checkAmbiguities(0.4D)) {
/*  141 */           sb.append("\n");
/*  142 */           sb.append("All sequences have less than " + Tools.doubleToPercent(0.4D, 0) + " of ambiguous positions.");
/*  143 */           sb.append("\n");
/*      */         } else {
/*  145 */           sb.append("\n");
/*  146 */           sb.append("WARNING : some sequences have more than " + Tools.doubleToPercent(0.4D, 0) + " of ambiguous positions.");
/*  147 */           sb.append("\n");
/*      */         }
/*  149 */         if (checkRedundancy()) {
/*  150 */           sb.append("\n");
/*  151 */           sb.append("There are no identical sequences.");
/*  152 */           sb.append("\n");
/*      */         } else {
/*  154 */           sb.append("\n");
/*  155 */           sb.append("WARNING : some sequences are identical.");
/*  156 */           sb.append("\n");
/*      */         }
/*  158 */         if (checkSaturation()) {
/*  159 */           sb.append("\n");
/*  160 */           sb.append("Divergence among your sequences does not show obvious signs of saturation.");
/*  161 */           sb.append("\n");
/*      */         } else {
/*  163 */           sb.append("\n");
/*  164 */           sb.append("WARNING : some pairwise uncorrected distances are higher than " + Tools.doubleToPercent(this.divergenceThreshold, 0) + ".");
/*  165 */           sb.append("\n");
/*  166 */           sb.append(" This means that divergence among some of your sequences is close or at saturation and are likely to generate problems during likelihood computation.");
/*  167 */           sb.append("\n");
/*      */         }
/*  169 */         Charset trim2 = trimmming();
/*  170 */         sb.append("\n");
/*  171 */         if (trim2.isEmpty()) {
/*  172 */           sb.append(trim2.getLabel() + " as been applyed to your dataset and no character has to be removed.");
/*  173 */           sb.append("\n");
/*      */         } else {
/*  175 */           sb.append("A '" + trim2.getLabel() + "' charset has been created excluding the following characters:");
/*  176 */           String[] ranges = trim2.getAllRanges().split(" ");
/*  177 */           for (int i = 0; i < ranges.length; i++) {
/*  178 */             if (i % 8 == 0) sb.append("\n");
/*  179 */             sb.append(ranges[i] + " ");
/*      */           }
/*  181 */           sb.append("\n");
/*      */         }
/*  183 */         sb.append("\n");
/*  184 */         sb.append("Trimming method has been selected using automated1 heuristic from trimAl.");
/*  185 */         sb.append("\n");
/*  186 */         sb.append("For the description of trimAl heuristic and methods, see:");
/*  187 */         sb.append("\n");
/*  188 */         sb.append("  \"trimAl: a tool for automated alignment trimming in large-scale phylogenetic analyses.");
/*  189 */         sb.append("\n");
/*  190 */         sb.append("  Capella-Gutierrez, Silla-Martinez & Gabaldon. Bioinformatics 2009 25: 1972-1973.");
/*  191 */         sb.append("\n");
/*  192 */         sb.append("  http://trimal.cgenomics.org\"");
/*  193 */         sb.append("\n");
/*      */       }
/*      */ 
/*  196 */       if (sb.length() > 0) {
/*  197 */         JOptionPane.showMessageDialog(this.mainFrame, sb.toString(), "Dataset testing", 1, MainFrame.imageMetapiga);
/*      */       }
/*  199 */       return WaitingLogo.Status.CHECK_DATASET_DONE;
/*      */     } catch (Exception e) {
/*  201 */       e.printStackTrace();
/*  202 */     }return WaitingLogo.Status.CHECK_DATASET_NOT_DONE;
/*      */   }
/*      */ 
/*      */   public void done()
/*      */   {
/*      */     try {
/*  208 */       this.mainFrame.setAllEnabled(null, (WaitingLogo.Status)get());
/*      */     } catch (ExecutionException e) {
/*  210 */       e.getCause().printStackTrace();
/*      */     } catch (InterruptedException e) {
/*  212 */       e.printStackTrace();
/*      */     }
/*      */   }
/*      */ 
/*      */   private boolean checkAmbiguities(double threshold)
/*      */     throws Exception
/*      */   {
/*  224 */     Map ambigous = this.P.dataset.getAmbiguousSequences(threshold);
/*  225 */     if (!ambigous.isEmpty()) {
/*  226 */       StringBuilder sb = new StringBuilder();
/*  227 */       for (Entry e : ambigous.entrySet()) {
/*  228 */         sb.append((String)e.getKey() + "\t- " + Tools.doubleToPercent(((Double)e.getValue()).doubleValue(), 0) + "\n");
/*      */       }
/*  230 */       JTextArea area = new JTextArea(sb.toString());
/*  231 */       area.setRows(10);
/*  232 */       area.setColumns(70);
/*  233 */       area.setLineWrap(true);
/*  234 */       JScrollPane scrollPane = new JScrollPane(area);
/*  235 */       JPanel pane = new JPanel(new BorderLayout());
/*  236 */       pane.add(scrollPane, "Center");
/*  237 */       pane.add(new JLabel("Warning: the following sequences have more than " + Tools.doubleToPercent(threshold, 0) + " of ambiguous positions."), "North");
/*  238 */       JPanel paneSouth = new JPanel(new BorderLayout());
/*  239 */       paneSouth.add(new JLabel("This can generate serious artifacts during phylogeny inference."), "North");
/*  240 */       paneSouth.add(new JLabel("Do you want MetaPIGA to remove the sequences for the analysis ?"), "South");
/*  241 */       pane.add(paneSouth, "South");
/*  242 */       int res = JOptionPane.showConfirmDialog(this.mainFrame, pane, "Test for ambiguities in dataset", 0, 3, MainFrame.imageMetapiga);
/*  243 */       if (res == 0) {
/*  244 */         for (String taxon : ambigous.keySet()) {
/*  245 */           this.P.deletedTaxa.add(taxon);
/*      */         }
/*  247 */         this.P.buildDataset();
/*  248 */         this.mainFrame.updateMatrixTextPanes(this.P);
/*  249 */         this.P.modelSampling.clear();
/*      */       }
/*  251 */       return false;
/*      */     }
/*  253 */     return true;
/*      */   }
/*      */ 
/*      */   private boolean checkRedundancy()
/*      */     throws Exception
/*      */   {
/*  264 */     MetaPIGA.progressHandling.newIndeterminateProgress("Testing for identical sequences - Computing absolute distances");
/*  265 */     Set idSeq = this.P.dataset.getIdenticalSequences();
/*  266 */     if (!idSeq.isEmpty()) {
/*  267 */       StringBuilder sb = new StringBuilder();
/*  268 */       int grpId = 1;
/*  269 */       for (Set set : idSeq) {
/*  270 */         sb.append("Group " + grpId++ + " : ");
/*  271 */         for (String taxon : set) {
/*  272 */           sb.append(taxon + ", ");
/*      */         }
/*  274 */         sb.deleteCharAt(sb.length() - 1);
/*  275 */         sb.deleteCharAt(sb.length() - 1);
/*  276 */         sb.append("\n");
/*      */       }
/*  278 */       JTextArea area = new JTextArea(sb.toString());
/*  279 */       area.setRows(10);
/*  280 */       area.setColumns(70);
/*  281 */       area.setLineWrap(true);
/*  282 */       JScrollPane scrollPane = new JScrollPane(area);
/*  283 */       JPanel pane = new JPanel(new BorderLayout());
/*  284 */       pane.add(scrollPane, "Center");
/*  285 */       pane.add(new JLabel("Warning: MetaPIGA has detected one or several groups of two or multiple sequences that are identical."), "North");
/*  286 */       pane.add(new JLabel("MetaPIGA will continue but will keep, for each group of identical sequences, only one sequence (the one with the lowest number of ambiguities)."), "South");
/*  287 */       JOptionPane.showMessageDialog(this.mainFrame, pane, "Test for identical sequences in dataset", 2, MainFrame.imageMetapiga);
/*      */       Iterator localIterator4;
/*  288 */       for (Iterator localIterator3 = idSeq.iterator(); localIterator3.hasNext(); 
/*  297 */         localIterator4.hasNext())
/*      */       {
/*  288 */         Set set = (Set)localIterator3.next();
/*  289 */         Iterator it = set.iterator();
/*  290 */         String bestSequence = (String)it.next();
/*  291 */         while (it.hasNext()) {
/*  292 */           String seq = (String)it.next();
/*  293 */           if (this.P.dataset.getSequenceQuality(seq) > this.P.dataset.getSequenceQuality(bestSequence)) {
/*  294 */             bestSequence = seq;
/*      */           }
/*      */         }
/*  297 */         localIterator4 = set.iterator(); continue; String taxon = (String)localIterator4.next();
/*  298 */         if (!taxon.equals(bestSequence)) {
/*  299 */           this.P.deletedTaxa.add(taxon);
/*      */         }
/*      */       }
/*  302 */       this.P.buildDataset();
/*  303 */       this.mainFrame.updateMatrixTextPanes(this.P);
/*  304 */       this.P.modelSampling.clear();
/*  305 */       return false;
/*      */     }
/*  307 */     return true;
/*      */   }
/*      */ 
/*      */   private boolean checkSaturation()
/*      */   {
/*  317 */     MetaPIGA.progressHandling.newIndeterminateProgress("Testing for saturation - Computing uncorrected distances");
/*  318 */     DistanceMatrix DM = this.P.dataset.getDistanceMatrix(Parameters.DistanceModel.UNCORRECTED, Parameters.StartingTreeDistribution.NONE, 0.5D, 0.0D, Parameters.StartingTreePInvPi.CONSTANT);
/*  319 */     this.divergenceThreshold = DM.saturationThreshold;
/*  320 */     if (DM.hasSaturation()) {
/*  321 */       double prop = 0.0D;
/*  322 */       int max = DM.ntax;
/*  323 */       max = max * (max - 1) / 2 + max;
/*  324 */       for (int i = 0; i < DM.ntax; i++) {
/*  325 */         for (int j = 0; j < i; j++) {
/*  326 */           if (DM.get(j, i) > this.divergenceThreshold) {
/*  327 */             prop += 1.0D / max;
/*      */           }
/*      */         }
/*      */       }
/*  331 */       StringBuilder sb = new StringBuilder();
/*  332 */       sb.append("Warning: " + Tools.doubleToPercent(prop, 0) + " of the pairwise uncorrected distances are higher than " + (int)Math.ceil(this.divergenceThreshold * 100.0D) + "%.");
/*  333 */       sb.append("\n");
/*  334 */       sb.append("This means that divergence among some of your sequences is close or at saturation and are likely to generate problems during likelihood computation.");
/*  335 */       sb.append("\n");
/*  336 */       sb.append("We strongly advise you to generate the pairwise distance matrix (in 'Tools --> Compute distances')  with no distance correction, \nidentify the taxa that generate excessively divergent distances, remove these sequences from your dataset, and re-align the remaining sequences before performing phylogenetic analyses.");
/*  337 */       JOptionPane.showMessageDialog(this.mainFrame, sb.toString(), "Test for saturation in dataset", 2, MainFrame.imageMetapiga);
/*  338 */       return false;
/*      */     }
/*  340 */     return true;
/*      */   }
/*      */ 
/*      */   private Charset trimmming()
/*      */     throws Exception
/*      */   {
/*  370 */     buildAlignmentMatrix();
/*  371 */     calculateGapsStats();
/*  372 */     Charset trim = calculateSeqIdentity();
/*  373 */     if (!trim.isEmpty()) {
/*  374 */       this.P.charsets.addCharset(trim.getLabel(), trim);
/*  375 */       this.P.charsets.excludeCharset(trim.getLabel());
/*  376 */       this.P.buildDataset();
/*  377 */       this.mainFrame.updateMatrixTextPanes(this.P);
/*  378 */       this.P.modelSampling.clear();
/*      */     }
/*  380 */     return trim;
/*      */   }
/*      */ 
/*      */   private Charset calculateSeqIdentity()
/*      */     throws Exception
/*      */   {
/*  388 */     MetaPIGA.progressHandling.newIndeterminateProgress("Dataset trimming - Calculating sequences identity");
/*  389 */     float[][] values = new float[this.rows][this.rows];
/*  390 */     float maxSeq = 0.0F; float avgSeq = 0.0F;
/*  391 */     for (int i = 0; i < this.rows; i++) {
/*  392 */       for (int k = 0; k < i; k++)
/*  393 */         values[i][k] = values[k][i];
/*  394 */       values[i][i] = 0.0F;
/*      */ 
/*  396 */       for (int k = i + 1; k < this.rows; k++) {
/*  397 */         int hit = 0; int dst = 0;
/*  398 */         for (int j = 0; j < this.columns; j++) {
/*  399 */           if (((this.alignmentMatrix[i][j] != this.indet) && (this.alignmentMatrix[i][j] != '-')) || (
/*  400 */             (this.alignmentMatrix[k][j] != this.indet) && (this.alignmentMatrix[k][j] != '-'))) {
/*  401 */             dst++;
/*  402 */             if (this.alignmentMatrix[i][j] == this.alignmentMatrix[k][j])
/*  403 */               hit++;
/*      */           }
/*      */         }
/*  406 */         values[i][k] = (hit / dst);
/*      */       }
/*      */ 
/*  409 */       float mx = 0.0F; float avg = 0.0F;
/*  410 */       for (int k = 0; k < this.rows; k++) {
/*  411 */         if (i != k) {
/*  412 */           mx = mx < values[i][k] ? values[i][k] : mx;
/*  413 */           avg += values[i][k];
/*      */         }
/*      */       }
/*      */ 
/*  417 */       avgSeq += avg / (this.rows - 1);
/*  418 */       maxSeq += mx;
/*      */     }
/*      */ 
/*  421 */     avgSeq /= this.rows;
/*  422 */     maxSeq /= this.rows;
/*      */ 
/*  424 */     if (avgSeq >= 0.55D) {
/*  425 */       return clean2ndSlope();
/*      */     }
/*  427 */     if (avgSeq <= 0.38D) {
/*  428 */       return cleanCombMethods(false);
/*      */     }
/*      */ 
/*  432 */     if (this.rows <= 20) {
/*  433 */       return clean2ndSlope();
/*      */     }
/*      */ 
/*  437 */     if ((maxSeq >= 0.5D) && (maxSeq <= 0.65D)) {
/*  438 */       return clean2ndSlope();
/*      */     }
/*      */ 
/*  441 */     return cleanCombMethods(false);
/*      */   }
/*      */ 
/*      */   private void buildAlignmentMatrix()
/*      */   {
/*  453 */     MetaPIGA.progressHandling.newIndeterminateProgress("Dataset trimming - Building alignment matrix");
/*  454 */     CharactersBlock block = this.P.charactersBlock;
/*  455 */     int ntax = block.getMatrixLabels().size();
/*  456 */     this.nchar = block.getDimensionsNChar();
/*  457 */     List keepedRows = new ArrayList();
/*  458 */     List deletedCharacters = new ArrayList();
/*  459 */     for (Iterator iterator = this.P.charsets.getExcludedCharsetIterator(); iterator.hasNext(); ) {
/*  460 */       Charset charset = (Charset)iterator.next();
/*  461 */       deletedCharacters.addAll(charset.getCharacters());
/*      */     }
/*  463 */     char[][] data = new char[ntax][this.nchar];
/*      */     DataType dataType;
/*      */     DataType dataType;
/*  465 */     if (block.getDataType().toUpperCase().equals("NUCLEOTIDES"))
/*  466 */       dataType = DataType.DNA;
/*      */     else
/*  468 */       dataType = DataType.valueOf(block.getDataType().toUpperCase());
/*      */     try
/*      */     {
/*  471 */       this.indet = dataType.getUndeterminateData().toChar();
/*      */     } catch (UnknownDataException e) {
/*  473 */       this.indet = 'X';
/*      */     }
/*  475 */     String matchSymbol = block.getMatchChar() == null ? "." : block.getMatchChar();
/*  476 */     String missingSymbol = block.getMissing() == null ? "?" : block.getMissing();
/*  477 */     String gapSymbol = block.getGap() == null ? "-" : block.getGap();
/*  478 */     int currentRow = 0;
/*      */     String nucl;
/*  479 */     for (Iterator localIterator1 = block.getMatrixLabels().iterator(); localIterator1.hasNext(); ) { Object taxa = localIterator1.next();
/*  480 */       if (!this.P.deletedTaxa.contains(taxa.toString())) {
/*  481 */         keepedRows.add(Integer.valueOf(currentRow));
/*      */       }
/*  483 */       int currentCol = 0;
/*  484 */       for (Iterator localIterator2 = block.getMatrixData(taxa.toString()).iterator(); localIterator2.hasNext(); ) { Object obj = localIterator2.next();
/*  485 */         nucl = obj.toString();
/*  486 */         if (nucl.length() > 0) {
/*  487 */           if (nucl.length() > 1) {
/*  488 */             BitSet bitSet = new BitSet(dataType.numOfStates());
/*  489 */             for (char c : nucl.toCharArray())
/*      */               try {
/*  491 */                 bitSet.set(dataType.getStateOf(c));
/*      */               } catch (UnknownDataException e) {
/*  493 */                 e.printStackTrace();
/*      */               }
/*      */             try
/*      */             {
/*  497 */               if (dataType.getData(bitSet).numOfStates() > 1)
/*  498 */                 data[currentRow][currentCol] = this.indet;
/*      */               else
/*  500 */                 data[currentRow][currentCol] = dataType.getData(bitSet).toChar();
/*      */             }
/*      */             catch (Exception e) {
/*  503 */               e.printStackTrace();
/*      */             }
/*      */           }
/*  506 */           else if ((nucl.equals(matchSymbol)) || ((block.isRespectCase()) && (nucl.equalsIgnoreCase(matchSymbol)))) {
/*  507 */             data[currentRow][currentCol] = (currentRow > 0 ? data[(currentRow - 1)][currentCol] : 45);
/*  508 */           } else if ((nucl.equals(missingSymbol)) || ((block.isRespectCase()) && (nucl.equalsIgnoreCase(missingSymbol)))) {
/*  509 */             data[currentRow][currentCol] = 45;
/*  510 */           } else if ((nucl.equals(gapSymbol)) || ((block.isRespectCase()) && (nucl.equalsIgnoreCase(gapSymbol)))) {
/*  511 */             data[currentRow][currentCol] = 45;
/*      */           } else {
/*      */             try {
/*  514 */               if (dataType.getData(nucl.toUpperCase()).numOfStates() > 1)
/*  515 */                 data[currentRow][currentCol] = this.indet;
/*      */               else
/*  517 */                 data[currentRow][currentCol] = dataType.getData(nucl.toUpperCase()).toChar();
/*      */             }
/*      */             catch (Exception e) {
/*  520 */               e.printStackTrace();
/*      */             }
/*      */           }
/*      */ 
/*  524 */           currentCol++;
/*      */         }
/*      */       }
/*  527 */       currentRow++;
/*      */     }
/*  529 */     this.rows = keepedRows.size();
/*  530 */     this.columns = (this.nchar - deletedCharacters.size());
/*  531 */     this.alignmentMatrix = new char[this.rows][this.columns];
/*  532 */     this.nexusPositions = new int[this.columns];
/*  533 */     int curCol = 0;
/*  534 */     for (int c = 0; c < this.nchar; c++) {
/*  535 */       int character = c + 1;
/*  536 */       if (!deletedCharacters.contains(Integer.valueOf(character))) {
/*  537 */         this.nexusPositions[curCol] = character;
/*  538 */         int curRow = 0;
/*  539 */         for (nucl = keepedRows.iterator(); nucl.hasNext(); ) { int r = ((Integer)nucl.next()).intValue();
/*  540 */           this.alignmentMatrix[curRow][curCol] = data[r][c];
/*  541 */           curRow++;
/*      */         }
/*  543 */         curCol++;
/*      */       }
/*      */     }
/*      */ 
/*  547 */     this.simMatrix = new SimilarityMatrix(dataType);
/*      */   }
/*      */ 
/*      */   private Charset clean2ndSlope()
/*      */   {
/*  556 */     MetaPIGA.progressHandling.newIndeterminateProgress("Dataset trimming - computing gappyout trimming");
/*  557 */     int cut = calcCutPoint2ndSlope();
/*  558 */     boolean[] removedChars = new boolean[this.nchar + 1];
/*  559 */     for (int i = 0; i < this.columns; i++) {
/*  560 */       if (this.gapsWindow[i] > cut) {
/*  561 */         removedChars[this.nexusPositions[i]] = true;
/*      */       }
/*      */     }
/*  564 */     Charset charset = new Charset("gappyout trimming");
/*  565 */     int start = 0;
/*  566 */     while (start < this.columns) {
/*  567 */       while ((start < this.columns) && (removedChars[start] == 0)) start++;
/*  568 */       int end = start + 1;
/*  569 */       while ((end < this.columns) && (removedChars[end] != 0)) end++;
/*  570 */       end--;
/*  571 */       if (start < this.nchar) charset.addRange(start, end);
/*  572 */       start = end + 1;
/*      */     }
/*  574 */     return charset;
/*      */   }
/*      */ 
/*      */   private Charset cleanCombMethods(boolean variable)
/*      */     throws Exception
/*      */   {
/*  587 */     MetaPIGA.progressHandling.newIndeterminateProgress("Dataset trimming - computing strict" + (variable ? "plus" : "") + " trimming");
/*      */ 
/*  590 */     int gapCut = calcCutPoint2ndSlope();
/*  591 */     calculateConservationStats();
/*      */ 
/*  595 */     calculateVectors();
/*  596 */     this.MDK_Window = this.MDK;
/*  597 */     float[] simil = this.MDK_Window;
/*      */ 
/*  600 */     int[] positions = new int[this.columns];
/*  601 */     Arrays.fill(positions, -1);
/*      */ 
/*  605 */     int acm = 0;
/*  606 */     for (int i = 0; i < this.columns; i++) {
/*  607 */       if (this.gapsWindow[i] <= gapCut) {
/*  608 */         positions[i] = i;
/*  609 */         acm++;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  614 */     float[] vectAux = new float[acm];
/*  615 */     int i = 0; for (int j = 0; i < this.columns; i++) {
/*  616 */       if (positions[i] != -1) {
/*  617 */         vectAux[(j++)] = simil[i];
/*      */       }
/*      */     }
/*  620 */     Arrays.sort(vectAux, 0, acm - 1);
/*      */ 
/*  623 */     float first20Point = 0.0F;
/*  624 */     float last80Point = 0.0F;
/*      */ 
/*  626 */     int i = acm - 1; for (int j = 1; i >= 0; j++) {
/*  627 */       if (j / acm * 100.0D <= 20.0D)
/*  628 */         first20Point = vectAux[i];
/*  629 */       if (j / acm * 100.0D <= 80.0D)
/*  630 */         last80Point = vectAux[i];
/*  626 */       i--;
/*      */     }
/*      */ 
/*  635 */     double inic = Math.log10(first20Point);
/*  636 */     double fin = Math.log10(last80Point);
/*  637 */     double vlr = (inic - fin) / 10.0D + fin;
/*  638 */     float simCut = (float)Math.pow(10.0D, vlr);
/*      */ 
/*  641 */     return cleanStrictPlus(gapCut, simCut, variable);
/*      */   }
/*      */ 
/*      */   private void calculateGapsStats()
/*      */   {
/*  648 */     MetaPIGA.progressHandling.newIndeterminateProgress("Dataset trimming - calculate gaps statistics");
/*  649 */     this.gapsInColumn = new int[this.columns];
/*  650 */     this.aminosXInColumn = new int[this.columns];
/*  651 */     this.gapsWindow = new int[this.columns];
/*  652 */     this.numColumnsWithGaps = new int[this.rows + 1];
/*  653 */     this.maxGaps = 0;
/*  654 */     for (int i = 0; i < this.columns; i++) {
/*  655 */       for (int j = 0; j < this.rows; j++) {
/*  656 */         if (this.alignmentMatrix[j][i] == '-')
/*  657 */           this.gapsInColumn[i] += 1;
/*  658 */         else if (this.alignmentMatrix[j][i] == this.indet) {
/*  659 */           this.aminosXInColumn[i] += 1;
/*      */         }
/*      */       }
/*      */ 
/*  663 */       this.numColumnsWithGaps[this.gapsInColumn[i]] += 1;
/*  664 */       this.gapsWindow[i] = this.gapsInColumn[i];
/*  665 */       if (this.gapsWindow[i] > this.maxGaps) this.maxGaps = this.gapsWindow[i];
/*      */     }
/*      */   }
/*      */ 
/*      */   private void calculateConservationStats()
/*      */   {
/*  673 */     MetaPIGA.progressHandling.newIndeterminateProgress("Dataset trimming - calculate conservation statistics");
/*  674 */     this.Q = new float[this.columns];
/*  675 */     this.MDK = new float[this.columns];
/*  676 */     this.MDK_Window = new float[this.columns];
/*  677 */     this.matrixIdentity = new float[this.rows][this.rows];
/*      */ 
/*  679 */     for (int i = 0; i < this.rows; i++)
/*  680 */       for (int j = i + 1; j < this.rows; j++)
/*      */       {
/*  682 */         int sum = 0; int length = 0;
/*      */ 
/*  684 */         for (int k = 0; k < this.columns; k++)
/*      */         {
/*  687 */           if ((this.alignmentMatrix[i][k] != '-') && (this.alignmentMatrix[i][k] != this.indet))
/*      */           {
/*  690 */             if ((this.alignmentMatrix[j][k] != '-') && (this.alignmentMatrix[j][k] != this.indet))
/*      */             {
/*  693 */               if (this.alignmentMatrix[j][k] == this.alignmentMatrix[i][k]) {
/*  694 */                 sum++;
/*      */               }
/*      */             }
/*  697 */             length++;
/*      */           }
/*  702 */           else if ((this.alignmentMatrix[j][k] != '-') && (this.alignmentMatrix[j][k] != this.indet)) {
/*  703 */             length++;
/*      */           }
/*      */         }
/*      */ 
/*  707 */         this.matrixIdentity[j][i] = (100.0F - sum / length * 100.0F);
/*  708 */         this.matrixIdentity[i][j] = this.matrixIdentity[j][i];
/*      */       }
/*      */   }
/*      */ 
/*      */   private int calcCutPoint2ndSlope()
/*      */   {
/*  721 */     MetaPIGA.progressHandling.newIndeterminateProgress("Dataset trimming - calculate cut point for gappyout trimming");
/*  722 */     float maxSlope = -1.0F;
/*  723 */     int act = 0; int max = 0;
/*      */ 
/*  726 */     float[] secondSlopeVector = new float[this.maxGaps + 1];
/*  727 */     Arrays.fill(secondSlopeVector, -1.0F);
/*  728 */     int maxIter = this.maxGaps + 1;
/*      */ 
/*  730 */     while (act < maxIter)
/*      */     {
/*  733 */       while (this.numColumnsWithGaps[act] == 0) act++;
/*  734 */       int pprev = act; if (act + 1 >= maxIter)
/*      */         break;
/*      */       do
/*  737 */         act++; while (this.numColumnsWithGaps[act] == 0);
/*  738 */       int prev = act; if (act + 1 >= maxIter)
/*      */         break;
/*      */       do
/*  741 */         act++; while (this.numColumnsWithGaps[act] == 0);
/*  742 */       if (act >= maxIter) {
/*      */         break;
/*      */       }
/*  745 */       secondSlopeVector[act] = ((act - pprev) / this.rows);
/*  746 */       secondSlopeVector[act] /= (this.numColumnsWithGaps[act] + this.numColumnsWithGaps[prev]) / this.columns;
/*      */ 
/*  749 */       if (secondSlopeVector[pprev] != -1.0F) {
/*  750 */         if (secondSlopeVector[act] / secondSlopeVector[pprev] > maxSlope) {
/*  751 */           maxSlope = secondSlopeVector[act] / secondSlopeVector[pprev];
/*  752 */           max = pprev;
/*      */         }
/*  754 */       } else if ((secondSlopeVector[prev] != -1.0F) && 
/*  755 */         (secondSlopeVector[act] / secondSlopeVector[prev] > maxSlope)) {
/*  756 */         maxSlope = secondSlopeVector[act] / secondSlopeVector[prev];
/*  757 */         max = pprev;
/*      */       }
/*      */ 
/*  760 */       act = prev;
/*      */     }
/*      */ 
/*  764 */     return max;
/*      */   }
/*      */ 
/*      */   private void calculateVectors() throws Exception {
/*  768 */     MetaPIGA.progressHandling.newIndeterminateProgress("Dataset trimming - building structures for strict trimming");
/*      */ 
/*  770 */     for (int i = 0; i < this.columns; i++)
/*      */     {
/*  772 */       float num = 0.0F; float den = 0.0F;
/*  773 */       for (int j = 0; j < this.rows; j++)
/*      */       {
/*  775 */         if ((this.alignmentMatrix[j][i] != '-') && (this.alignmentMatrix[j][i] != this.indet)) {
/*  776 */           for (int k = j + 1; k < this.rows; k++)
/*      */           {
/*  778 */             if ((this.alignmentMatrix[k][i] != '-') && (this.alignmentMatrix[k][i] != this.indet))
/*      */             {
/*  780 */               num += this.matrixIdentity[j][k] * this.simMatrix.getDistance(this.alignmentMatrix[j][i], this.alignmentMatrix[k][i]);
/*  781 */               den += this.matrixIdentity[j][k];
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*  786 */       this.Q[i] = (den == 0.0F ? 0.0F : num / den);
/*  787 */       this.MDK[i] = ((float)Math.exp(-this.Q[i]));
/*      */ 
/*  790 */       if (this.gapsWindow[i] / this.rows >= 0.8D) this.MDK[i] = 0.0F;
/*      */ 
/*  793 */       if (this.MDK[i] > 1.0F) this.MDK[i] = 1.0F;
/*      */     }
/*      */   }
/*      */ 
/*      */   private Charset cleanStrictPlus(int gapCut, float simCut, boolean variable)
/*      */   {
/*  810 */     MetaPIGA.progressHandling.newIndeterminateProgress("Dataset trimming - building charset with removed character of strict" + (variable ? "plus" : "") + " trimming");
/*  811 */     int[] saveResidues = new int[this.columns];
/*      */ 
/*  813 */     for (int i = 0; i < this.columns; i++) {
/*  814 */       if (this.gapsWindow[i] > gapCut) {
/*  815 */         saveResidues[i] = -1;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  820 */     for (int i = 0; i < this.columns; i++) {
/*  821 */       if (this.MDK_Window[i] < simCut) {
/*  822 */         saveResidues[i] = -1;
/*      */       }
/*      */     }
/*      */ 
/*  826 */     if ((saveResidues[0] != -1) && (saveResidues[2] != -1) && (saveResidues[3] != -1))
/*  827 */       saveResidues[1] = 1;
/*      */     else {
/*  829 */       saveResidues[1] = -1;
/*      */     }
/*      */ 
/*  832 */     if ((saveResidues[(this.columns - 1)] != -1) && (saveResidues[(this.columns - 3)] != -1) && (saveResidues[(this.columns - 4)] != -1))
/*  833 */       saveResidues[(this.columns - 2)] = (this.columns - 2);
/*      */     else {
/*  835 */       saveResidues[(this.columns - 2)] = -1;
/*      */     }
/*      */ 
/*  838 */     int i = 2; for (int num = 0; i < this.columns - 2; num = 0) {
/*  839 */       if (saveResidues[i] == -1) {
/*  840 */         if (saveResidues[(i - 2)] != -1) num++;
/*  841 */         if (saveResidues[(i - 1)] != -1) num++;
/*  842 */         if (saveResidues[(i + 1)] != -1) num++;
/*  843 */         if (saveResidues[(i + 2)] != -1) num++;
/*  844 */         if (num >= 3) saveResidues[i] = i;
/*      */       }
/*  838 */       i++;
/*      */     }
/*      */     int lenBlock;
/*      */     int lenBlock;
/*  851 */     if (!variable) {
/*  852 */       lenBlock = 5;
/*      */     } else {
/*  854 */       lenBlock = Math.round(this.columns * 0.01D) > 3L ? (int)Math.round(this.columns * 0.01D) : 3;
/*  855 */       lenBlock = lenBlock < 12 ? lenBlock : 12;
/*      */     }
/*      */ 
/*  859 */     for (int i = 0; i < this.columns; i++) {
/*  860 */       if (saveResidues[i] != -1)
/*      */       {
/*  862 */         for (int j = i + 1; (j < this.columns) && (saveResidues[j] != -1); j++);
/*  863 */         if (j - i < lenBlock)
/*  864 */           for (int k = i; k < j; k++)
/*  865 */             saveResidues[k] = -1;
/*  866 */         i = j;
/*      */       }
/*      */     }
/*      */ 
/*  870 */     boolean[] removedChars = new boolean[this.nchar + 1];
/*  871 */     for (int i = 0; i < this.columns; i++) {
/*  872 */       if (saveResidues[i] == -1) {
/*  873 */         removedChars[this.nexusPositions[i]] = true;
/*      */       }
/*      */     }
/*  876 */     Charset charset = new Charset("strict" + (variable ? "plus" : "") + " trimming");
/*  877 */     int start = 0;
/*  878 */     while (start < this.columns) {
/*  879 */       while ((start < this.columns) && (removedChars[start] == 0)) start++;
/*  880 */       int end = start + 1;
/*  881 */       while ((end < this.columns) && (removedChars[end] != 0)) end++;
/*  882 */       end--;
/*  883 */       if (start < this.nchar) charset.addRange(start, end);
/*  884 */       start = end + 1;
/*      */     }
/*  886 */     return charset;
/*      */   }
/*      */ 
/*      */   public static enum DatasetCheck
/*      */   {
/*   45 */     ALL, AMBIGUITIES, REDUNDANCY, SATURATION, TRIMMING;
/*      */   }
/*      */ 
/*      */   private class SimilarityMatrix
/*      */   {
/*  890 */     private final char[] listAASym = "ARNDCQEGHILKMFPSTWYV".toCharArray();
/*      */ 
/*  893 */     private final float[][] defaultAAMatrix = { 
/*  894 */       { 4.0F, -1.0F, -2.0F, -2.0F, 0.0F, -1.0F, -1.0F, 0.0F, -2.0F, -1.0F, -1.0F, -1.0F, -1.0F, -2.0F, -1.0F, 1.0F, 0.0F, -3.0F, -2.0F, 0.0F }, 
/*  895 */       { -1.0F, 5.0F, 0.0F, -2.0F, -3.0F, 1.0F, 0.0F, -2.0F, 0.0F, -3.0F, -2.0F, 2.0F, -1.0F, -3.0F, -2.0F, -1.0F, -1.0F, -3.0F, -2.0F, -3.0F }, 
/*  896 */       { -2.0F, 0.0F, 6.0F, 1.0F, -3.0F, 0.0F, 0.0F, 0.0F, 1.0F, -3.0F, -3.0F, 0.0F, -2.0F, -3.0F, -2.0F, 1.0F, 0.0F, -4.0F, -2.0F, -3.0F }, 
/*  897 */       { -2.0F, -2.0F, 1.0F, 6.0F, -3.0F, 0.0F, 2.0F, -1.0F, -1.0F, -3.0F, -4.0F, -1.0F, -3.0F, -3.0F, -1.0F, 0.0F, -1.0F, -4.0F, -3.0F, -3.0F }, 
/*  898 */       { 0.0F, -3.0F, -3.0F, -3.0F, 9.0F, -3.0F, -4.0F, -3.0F, -3.0F, -1.0F, -1.0F, -3.0F, -1.0F, -2.0F, -3.0F, -1.0F, -1.0F, -2.0F, -2.0F, -1.0F }, 
/*  899 */       { -1.0F, 1.0F, 0.0F, 0.0F, -3.0F, 5.0F, 2.0F, -2.0F, 0.0F, -3.0F, -2.0F, 1.0F, 0.0F, -3.0F, -1.0F, 0.0F, -1.0F, -2.0F, -1.0F, -2.0F }, 
/*  900 */       { -1.0F, 0.0F, 0.0F, 2.0F, -4.0F, 2.0F, 5.0F, -2.0F, 0.0F, -3.0F, -3.0F, 1.0F, -2.0F, -3.0F, -1.0F, 0.0F, -1.0F, -3.0F, -2.0F, -2.0F }, 
/*  901 */       { 0.0F, -2.0F, 0.0F, -1.0F, -3.0F, -2.0F, -2.0F, 6.0F, -2.0F, -4.0F, -4.0F, -2.0F, -3.0F, -3.0F, -2.0F, 0.0F, -2.0F, -2.0F, -3.0F, -3.0F }, 
/*  902 */       { -2.0F, 0.0F, 1.0F, -1.0F, -3.0F, 0.0F, 0.0F, -2.0F, 8.0F, -3.0F, -3.0F, -1.0F, -2.0F, -1.0F, -2.0F, -1.0F, -2.0F, -2.0F, 2.0F, -3.0F }, 
/*  903 */       { -1.0F, -3.0F, -3.0F, -3.0F, -1.0F, -3.0F, -3.0F, -4.0F, -3.0F, 4.0F, 2.0F, -3.0F, 1.0F, 0.0F, -3.0F, -2.0F, -1.0F, -3.0F, -1.0F, 3.0F }, 
/*  904 */       { -1.0F, -2.0F, -3.0F, -4.0F, -1.0F, -2.0F, -3.0F, -4.0F, -3.0F, 2.0F, 4.0F, -2.0F, 2.0F, 0.0F, -3.0F, -2.0F, -1.0F, -2.0F, -1.0F, 1.0F }, 
/*  905 */       { -1.0F, 2.0F, 0.0F, -1.0F, -3.0F, 1.0F, 1.0F, -2.0F, -1.0F, -3.0F, -2.0F, 5.0F, -1.0F, -3.0F, -1.0F, 0.0F, -1.0F, -3.0F, -2.0F, -2.0F }, 
/*  906 */       { -1.0F, -1.0F, -2.0F, -3.0F, -1.0F, 0.0F, -2.0F, -3.0F, -2.0F, 1.0F, 2.0F, -1.0F, 5.0F, 0.0F, -2.0F, -1.0F, -1.0F, -1.0F, -1.0F, 1.0F }, 
/*  907 */       { -2.0F, -3.0F, -3.0F, -3.0F, -2.0F, -3.0F, -3.0F, -3.0F, -1.0F, 0.0F, 0.0F, -3.0F, 0.0F, 6.0F, -4.0F, -2.0F, -2.0F, 1.0F, 3.0F, -1.0F }, 
/*  908 */       { -1.0F, -2.0F, -2.0F, -1.0F, -3.0F, -1.0F, -1.0F, -2.0F, -2.0F, -3.0F, -3.0F, -1.0F, -2.0F, -4.0F, 7.0F, -1.0F, -1.0F, -4.0F, -3.0F, -2.0F }, 
/*  909 */       { 1.0F, -1.0F, 1.0F, 0.0F, -1.0F, 0.0F, 0.0F, 0.0F, -1.0F, -2.0F, -2.0F, 0.0F, -1.0F, -2.0F, -1.0F, 4.0F, 1.0F, -3.0F, -2.0F, -2.0F }, 
/*  910 */       { 0.0F, -1.0F, 0.0F, -1.0F, -1.0F, -1.0F, -1.0F, -2.0F, -2.0F, -1.0F, -1.0F, -1.0F, -1.0F, -2.0F, -1.0F, 1.0F, 5.0F, -2.0F, -2.0F, 0.0F }, 
/*  911 */       { -3.0F, -3.0F, -4.0F, -4.0F, -2.0F, -2.0F, -3.0F, -2.0F, -2.0F, -3.0F, -2.0F, -3.0F, -1.0F, 1.0F, -4.0F, -3.0F, -2.0F, 11.0F, 2.0F, -3.0F }, 
/*  912 */       { -2.0F, -2.0F, -2.0F, -3.0F, -2.0F, -1.0F, -2.0F, -3.0F, 2.0F, -1.0F, -1.0F, -2.0F, -1.0F, 3.0F, -3.0F, -2.0F, -2.0F, 2.0F, 7.0F, -1.0F }, 
/*  913 */       { 0.0F, -3.0F, -3.0F, -3.0F, -1.0F, -2.0F, -2.0F, -3.0F, -3.0F, 3.0F, 1.0F, -2.0F, 1.0F, -1.0F, -2.0F, -2.0F, 0.0F, -3.0F, -1.0F, 4.0F } };
/*      */ 
/*  916 */     private final char[] listNTSym = "ACGT".toCharArray();
/*      */ 
/*  918 */     private final float[][] defaultNTMatrix = { 
/*  919 */       { 1.0F, 0.0F, 0.0F, 0.0F }, 
/*  920 */       { 0.0F, 1.0F, 0.0F, 0.0F }, 
/*  921 */       { 0.0F, 0.0F, 1.0F, 0.0F }, 
/*  922 */       { 0.0F, 0.0F, 0.0F, 1.0F } };
/*      */ 
/*  925 */     private final char[] listBSSym = "01".toCharArray();
/*      */ 
/*  927 */     private final float[][] defaultBSMatrix = { 
/*  928 */       { 1.0F, 0.0F }, 
/*  929 */       { 0.0F, 1.0F } };
/*      */     private static final int TAMABC = 45;
/*      */     private final int[] vhash;
/*      */     private final float[][] simMat;
/*      */     private final float[][] distMat;
/*      */     private final int numPositions;
/*      */ 
/*      */     public SimilarityMatrix(DataType dataType)
/*      */     {
/*  940 */       char[] listSym = null;
/*  941 */       float[][] defaultMatrix = null;
/*  942 */       switch (dataType) {
/*      */       case PROTEIN:
/*  944 */         listSym = this.listBSSym;
/*  945 */         defaultMatrix = this.defaultBSMatrix;
/*  946 */         this.numPositions = 2;
/*  947 */         break;
/*      */       case DNA:
/*  949 */         listSym = this.listAASym;
/*  950 */         defaultMatrix = this.defaultAAMatrix;
/*  951 */         this.numPositions = 20;
/*  952 */         break;
/*      */       case CODON:
/*      */       default:
/*  955 */         listSym = this.listNTSym;
/*  956 */         defaultMatrix = this.defaultNTMatrix;
/*  957 */         this.numPositions = 4;
/*      */       }
/*      */ 
/*  961 */       this.vhash = new int[45];
/*  962 */       this.simMat = new float[this.numPositions][this.numPositions];
/*  963 */       this.distMat = new float[this.numPositions][this.numPositions];
/*      */ 
/*  965 */       for (int i = 0; i < 45; i++) {
/*  966 */         this.vhash[i] = -1;
/*      */       }
/*      */ 
/*  969 */       for (int i = 0; i < this.numPositions; i++) {
/*  970 */         this.vhash[(listSym[i] - '0')] = i;
/*      */       }
/*  972 */       for (int i = 0; i < this.numPositions; i++) {
/*  973 */         for (int j = 0; j < this.numPositions; j++) {
/*  974 */           this.simMat[i][j] = defaultMatrix[i][j];
/*      */         }
/*      */       }
/*      */ 
/*  978 */       for (int j = 0; j < this.numPositions; j++)
/*  979 */         for (int i = 0; i < this.numPositions; i++)
/*  980 */           if ((i != j) && (this.distMat[i][j] == 0.0F)) {
/*  981 */             float sum = 0.0F;
/*  982 */             for (int k = 0; k < this.numPositions; k++)
/*  983 */               sum += (this.simMat[k][j] - this.simMat[k][i]) * (this.simMat[k][j] - this.simMat[k][i]);
/*  984 */             sum = (float)Math.sqrt(sum);
/*  985 */             this.distMat[i][j] = sum;
/*  986 */             this.distMat[j][i] = sum;
/*      */           }
/*      */     }
/*      */ 
/*      */     public float getDistance(char a, char b)
/*      */       throws Exception
/*      */     {
/* 1005 */       int numa;
/* 1005 */       if ((a >= '0') && (a <= 'Z')) numa = this.vhash[(a - '0')]; else
/* 1006 */         throw new Exception("Error: the symbol '" + a + "' is incorrect");
/* 1009 */       int numa;
/*      */       int numb;
/* 1009 */       if ((b >= '0') && (b <= 'Z')) numb = this.vhash[(b - '0')]; else
/* 1010 */         throw new Exception("Error: the symbol '" + b + "' is incorrect");
/*      */       int numb;
/* 1013 */       if (numa == -1) {
/* 1014 */         throw new Exception("Error: the symbol '" + a + "' accesing the matrix is not defined in this object");
/*      */       }
/*      */ 
/* 1017 */       if (numb == -1) {
/* 1018 */         throw new Exception("Error: the symbol '" + b + "' accesing the matrix is not defined in this object");
/*      */       }
/*      */ 
/* 1022 */       return this.distMat[numa][numb];
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.modelization.DatasetTriming
 * JD-Core Version:    0.6.2
 */