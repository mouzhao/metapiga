/*     */ package monitors;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.util.Collection;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ import javax.swing.text.DefaultEditorKit;
/*     */ import javax.swing.text.DefaultStyledDocument;
/*     */ import metapiga.modelization.DistanceMatrix;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.parameters.Parameters.Heuristic;
/*     */ import metapiga.parameters.Parameters.LogFile;
/*     */ import metapiga.parameters.Parameters.Operator;
/*     */ import metapiga.trees.Consensus;
/*     */ import metapiga.trees.Node;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public class PrintMonitor
/*     */ {
/*     */   public static final String tab = "\t";
/*     */   public static final String endl = "\n";
/*     */   private Monitor monitor;
/*     */   Parameters parameters;
/*     */   String dirPath;
/*  39 */   boolean DATAwritten = false;
/*  40 */   boolean DISTwritten = false;
/*     */   FileWriter treeOpWriter;
/*     */   FileWriter statOpWriter;
/*     */   FileWriter searchLogWriter;
/*     */   FileWriter searchTreeWriter;
/*     */   FileWriter consensusWriter;
/*     */   FileWriter ancseqWriter;
/*     */   FileWriter perfWriter;
/*     */   private Map<String, Long> performanceTracking;
/*     */   private Map<String, String> operatorDetailsTracking;
/*     */   private Map<Integer, String> cpDetailsTracking;
/*     */ 
/*     */   public PrintMonitor(Monitor monitor, String dirPath)
/*     */   {
/*  53 */     this.monitor = monitor;
/*  54 */     this.dirPath = dirPath;
/*     */   }
/*     */ 
/*     */   public void setParameters(Parameters parameters) {
/*  58 */     this.parameters = parameters;
/*  59 */     if (this.monitor.trackPerformances()) this.performanceTracking = new HashMap();
/*  60 */     if (this.monitor.trackOperators()) this.operatorDetailsTracking = new HashMap();
/*  61 */     if (this.monitor.trackHeuristic()) this.cpDetailsTracking = new HashMap(); 
/*     */   }
/*     */ 
/*     */   public void initLogFiles(int currentReplicate)
/*     */   {
/*  65 */     if (!this.parameters.logFiles.isEmpty()) {
/*  66 */       File dir = new File(this.dirPath);
/*  67 */       if (!dir.exists()) dir.mkdir();
/*  68 */       if (this.parameters.hasManyReplicates()) {
/*  69 */         dir = new File(this.dirPath + "/" + "Replicate " + currentReplicate);
/*  70 */         dir.mkdir();
/*     */       }
/*     */       try {
/*  73 */         if (this.parameters.logFiles.contains(Parameters.LogFile.OPDETAILS)) {
/*  74 */           if (this.treeOpWriter != null) this.treeOpWriter.close();
/*  75 */           this.treeOpWriter = new FileWriter(createOutputFile(Parameters.LogFile.OPDETAILS, currentReplicate));
/*     */         }
/*  77 */         if (this.parameters.logFiles.contains(Parameters.LogFile.OPSTATS)) {
/*  78 */           if (this.statOpWriter != null) this.statOpWriter.close();
/*  79 */           this.statOpWriter = new FileWriter(createOutputFile(Parameters.LogFile.OPSTATS, currentReplicate));
/*     */         }
/*  81 */         if (this.parameters.logFiles.contains(Parameters.LogFile.HEUR)) {
/*  82 */           if (this.searchLogWriter != null) this.searchLogWriter.close();
/*  83 */           this.searchLogWriter = new FileWriter(createOutputFile(Parameters.LogFile.HEUR, currentReplicate));
/*  84 */           switch ($SWITCH_TABLE$metapiga$parameters$Parameters$Heuristic()[this.parameters.heuristic.ordinal()]) {
/*     */           case 1:
/*  86 */             this.searchLogWriter.write("Step\tBest likelihood\tCurrent likelihood\tOperator\tImprovement\n");
/*  87 */             break;
/*     */           case 2:
/*  89 */             this.searchLogWriter.write("Step\tBest likelihood\tS0 likelihood\tCurrent likelihood\tOperator\tStatus\tTemperature acceptance\tTemperature\tSteps with same T°\tSuccesses\tFailures\tNbr of decrements without reheating\n");
/*     */ 
/*  92 */             break;
/*     */           case 3:
/*  94 */             this.searchLogWriter.write("Step\t");
/*  95 */             for (int i = 0; i < this.parameters.gaIndNum; i++) {
/*  96 */               this.searchLogWriter.write("Mutated individual " + i + "\t" + "Operator used on ind " + i + "\t");
/*     */             }
/*  98 */             this.searchLogWriter.write("Selection details\t");
/*  99 */             for (int i = 0; i < this.parameters.gaIndNum; i++) {
/* 100 */               this.searchLogWriter.write("Selected individual " + i + "\t");
/*     */             }
/* 102 */             this.searchLogWriter.write("Best likelihood\n");
/* 103 */             break;
/*     */           case 4:
/* 105 */             this.searchLogWriter.write("Step\t");
/* 106 */             for (int p = 0; p < this.parameters.cpPopNum; p++) {
/* 107 */               for (int i = 0; i < this.parameters.cpIndNum; i++) {
/* 108 */                 this.searchLogWriter.write("Mutated ind " + i + " of pop " + p + "\t" + "Operator used on ind " + i + " of pop " + p + "\t");
/*     */               }
/*     */             }
/* 111 */             for (int p = 0; p < this.parameters.cpPopNum; p++) {
/* 112 */               this.searchLogWriter.write("Selection details on pop " + p + "\t");
/* 113 */               for (int i = 0; i < this.parameters.cpIndNum; i++) {
/* 114 */                 this.searchLogWriter.write("Selected ind " + i + " of pop " + p + "\t");
/*     */               }
/*     */             }
/* 117 */             this.searchLogWriter.write("Best likelihood\n");
/*     */           }
/*     */         }
/*     */ 
/* 121 */         if (this.parameters.logFiles.contains(Parameters.LogFile.TREEHEUR)) {
/* 122 */           if (this.searchTreeWriter != null) this.searchTreeWriter.close();
/* 123 */           this.searchTreeWriter = new FileWriter(createOutputFile(Parameters.LogFile.TREEHEUR, currentReplicate));
/* 124 */           this.searchTreeWriter.write("#NEXUS\n");
/* 125 */           this.searchTreeWriter.write("\n");
/* 126 */           this.searchTreeWriter.write("Begin trees;  [" + this.parameters.heuristic.name() + " started " + new Date(System.currentTimeMillis()).toString() + "]" + "\n");
/*     */         }
/* 128 */         if (this.parameters.logFiles.contains(Parameters.LogFile.CONSENSUS)) {
/* 129 */           if (this.consensusWriter != null) this.consensusWriter.close();
/* 130 */           this.consensusWriter = new FileWriter(createOutputFile(Parameters.LogFile.CONSENSUS, currentReplicate));
/*     */         }
/* 132 */         if (this.parameters.logFiles.contains(Parameters.LogFile.PERF)) {
/* 133 */           if (this.perfWriter != null) this.perfWriter.close();
/* 134 */           this.perfWriter = new FileWriter(createOutputFile(Parameters.LogFile.PERF, currentReplicate));
/* 135 */           this.perfWriter.write("Performances (expressed in nanoseconds)\n\n");
/*     */         }
/* 137 */         if (this.parameters.logFiles.contains(Parameters.LogFile.ANCSEQ)) {
/* 138 */           if (this.ancseqWriter != null) this.ancseqWriter.close();
/* 139 */           this.ancseqWriter = new FileWriter(createOutputFile(Parameters.LogFile.ANCSEQ, currentReplicate));
/*     */         }
/*     */       } catch (Exception ex) {
/* 142 */         ex.printStackTrace();
/* 143 */         this.monitor.showText("\n Error when creating log files for replicate " + currentReplicate);
/* 144 */         this.monitor.showText("\n Java exception : " + ex.getCause() + " (" + ex.getMessage() + ")");
/* 145 */         for (StackTraceElement el : ex.getStackTrace())
/* 146 */           this.monitor.showText("\tat " + el.toString());
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   private File createOutputFile(Parameters.LogFile logFile, int currentReplicate)
/*     */   {
/* 154 */     String name = "";
/* 155 */     switch (logFile) {
/*     */     case OPSTATS:
/* 157 */       name = "OperatorsDetails.log";
/* 158 */       break;
/*     */     case PERF:
/* 160 */       name = "OperatorsStatistics.log";
/* 161 */       break;
/*     */     case DIST:
/* 163 */       name = "Heuristic.log";
/* 164 */       break;
/*     */     case HEUR:
/* 166 */       name = "Heuristic.tre";
/* 167 */       break;
/*     */     case OPDETAILS:
/* 169 */       name = "Consensus.log";
/* 170 */       break;
/*     */     case TREESTART:
/* 172 */       name = "Performances.log";
/* 173 */       break;
/*     */     case TREEHEUR:
/* 175 */       name = "AncestralSequences.log";
/*     */     }
/*     */     File output;
/*     */     File output;
/* 178 */     if (this.parameters.hasManyReplicates())
/* 179 */       output = new File(this.dirPath + "/" + "Replicate " + currentReplicate + "/" + name);
/*     */     else {
/* 181 */       output = new File(this.dirPath + "/" + name);
/*     */     }
/* 183 */     return output;
/*     */   }
/*     */ 
/*     */   public void closeOutputFiles() throws IOException {
/* 187 */     if (this.treeOpWriter != null) this.treeOpWriter.close();
/* 188 */     if (this.statOpWriter != null) this.statOpWriter.close();
/* 189 */     if (this.searchLogWriter != null) this.searchLogWriter.close();
/* 190 */     if (this.searchTreeWriter != null) this.searchTreeWriter.close();
/* 191 */     if (this.consensusWriter != null) this.consensusWriter.close();
/* 192 */     if (this.perfWriter != null) this.perfWriter.close();
/* 193 */     if (this.ancseqWriter != null) this.ancseqWriter.close(); 
/*     */   }
/*     */ 
/*     */   public void printDataMatrix()
/*     */   {
/* 197 */     if (!this.DATAwritten)
/*     */       try {
/* 199 */         File output = new File(this.dirPath);
/* 200 */         if (!output.exists()) output.mkdir();
/* 201 */         output = new File(this.dirPath + "/" + "Dataset.log");
/* 202 */         FileWriter fw = new FileWriter(output);
/* 203 */         kit = new DefaultEditorKit();
/* 204 */         doc = this.parameters.showDataset();
/* 205 */         kit.write(fw, doc, 0, doc.getLength());
/* 206 */         fw.close();
/* 207 */         this.DATAwritten = true;
/*     */       } catch (Exception e) {
/* 209 */         e.printStackTrace();
/* 210 */         this.monitor.showText("\n Error when writing file Dataset.log");
/* 211 */         this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/*     */         StackTraceElement[] arrayOfStackTraceElement;
/* 212 */         DefaultStyledDocument doc = (arrayOfStackTraceElement = e.getStackTrace()).length; for (DefaultEditorKit kit = 0; kit < doc; kit++) { StackTraceElement el = arrayOfStackTraceElement[kit];
/* 213 */           this.monitor.showText("\tat " + el.toString());
/*     */         }
/*     */       }
/*     */   }
/*     */ 
/*     */   public void printDistanceMatrix(DistanceMatrix dm)
/*     */   {
/* 220 */     if (!this.DISTwritten)
/*     */       try {
/* 222 */         File dir = new File(this.dirPath);
/* 223 */         if (!dir.exists()) dir.mkdir();
/* 224 */         File output = new File(this.dirPath + "/" + "Distances.log");
/* 225 */         fw = new FileWriter(output);
/* 226 */         kit = new DefaultEditorKit();
/* 227 */         doc = dm.show();
/* 228 */         kit.write(fw, doc, 0, doc.getLength());
/* 229 */         fw.close();
/* 230 */         this.DISTwritten = true;
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 235 */         DefaultStyledDocument doc;
/* 232 */         e.printStackTrace();
/* 233 */         this.monitor.showText("\n Error when writing file Distances.log");
/* 234 */         this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 235 */         DefaultEditorKit kit = (doc = e.getStackTrace()).length; for (FileWriter fw = 0; fw < kit; fw++) { StackTraceElement el = doc[fw];
/* 236 */           this.monitor.showText("\tat " + el.toString()); }
/*     */       }
/*     */   }
/*     */ 
/*     */   public void printStartingTrees(List<Tree> startingTrees, int currentReplicate)
/*     */   {
/*     */     try
/*     */     {
/* 244 */       File output = new File(this.dirPath);
/* 245 */       if (!output.exists()) output.mkdir();
/* 246 */       if (this.parameters.hasManyReplicates()) {
/* 247 */         output = new File(this.dirPath + "/" + "Replicate " + currentReplicate);
/* 248 */         if (!output.exists()) output.mkdir();
/* 249 */         output = new File(this.dirPath + "/" + "Replicate " + currentReplicate + "/" + "StartingTrees.tre");
/*     */       } else {
/* 251 */         output = new File(this.dirPath + "/" + "StartingTrees.tre");
/*     */       }
/* 253 */       FileWriter fw = new FileWriter(output);
/* 254 */       fw.write("#NEXUS\n");
/* 255 */       fw.write("\n");
/* 256 */       fw.write("Begin trees;  [Treefile created " + new Date(System.currentTimeMillis()).toString() + "]" + "\n");
/* 257 */       for (Iterator localIterator = startingTrees.iterator(); localIterator.hasNext(); ) { tree = (Tree)localIterator.next();
/* 258 */         fw.write(tree.toNewickLine(false, false) + "\n");
/*     */       }
/* 260 */       fw.write("End;\n");
/* 261 */       fw.close();
/*     */     } catch (Exception e) {
/* 263 */       e.printStackTrace();
/* 264 */       this.monitor.showText("\n Error when writing file StartingTrees.tre");
/* 265 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/*     */       StackTraceElement[] arrayOfStackTraceElement;
/* 266 */       Tree localTree1 = (arrayOfStackTraceElement = e.getStackTrace()).length; for (Tree tree = 0; tree < localTree1; tree++) { StackTraceElement el = arrayOfStackTraceElement[tree];
/* 267 */         this.monitor.showText("\tat " + el.toString()); }
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void printTreeBeforeOperator(Tree tree, Parameters.Operator operator, boolean consensus)
/*     */   {
/*     */     try {
/* 274 */       StringBuilder s = new StringBuilder();
/* 275 */       s.append("----------------------------------------------------------\n");
/* 276 */       s.append(tree.getName() + "\n");
/* 277 */       s.append("Tree before operator " + operator + (consensus ? " respecting consensus" : " without consensus") + " :" + "\n");
/* 278 */       s.append(tree.toNewickLineWithML(
/* 279 */         new StringBuilder(String.valueOf(tree.getName().replace(this.parameters.heuristic.verbose(), this.parameters.heuristic.toString()).replace("population", "pop").replace("individual", "ind")))
/* 280 */         .append(" bef").append(operator).toString(), 
/* 280 */         true, false) + "\n");
/* 281 */       this.operatorDetailsTracking.put(tree.getName(), s.toString());
/*     */     } catch (Exception e) {
/* 283 */       e.printStackTrace();
/* 284 */       this.monitor.showText("\n Error when writing in file OperatorsDetails.log");
/* 285 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 286 */       for (StackTraceElement el : e.getStackTrace())
/* 287 */         this.monitor.showText("\tat " + el.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void printOperatorInfos(Tree tree, String infos)
/*     */   {
/* 293 */     String key = tree.getName();
/* 294 */     String value = (String)this.operatorDetailsTracking.get(key);
/* 295 */     this.operatorDetailsTracking.put(key, value + infos + "\n");
/*     */   }
/*     */ 
/*     */   public synchronized void printOperatorInfos(Tree tree, String infos, Consensus consensus) {
/* 299 */     String key = tree.getName();
/* 300 */     String value = (String)this.operatorDetailsTracking.get(key);
/* 301 */     value = value + infos + "\n";
/* 302 */     value = value + "Consensus :\n" + consensus.showConsensus() + "\n";
/* 303 */     this.operatorDetailsTracking.put(key, value);
/*     */   }
/*     */ 
/*     */   public synchronized void printTreeAfterOperator(Tree tree, Parameters.Operator operator, boolean consensus) {
/*     */     try {
/* 308 */       this.treeOpWriter.write((String)this.operatorDetailsTracking.remove(tree.getName()));
/* 309 */       this.treeOpWriter.write("Tree after operator " + operator + (consensus ? " respecting consensus" : " without consensus") + " :" + "\n");
/* 310 */       this.treeOpWriter.write(tree.toNewickLineWithML(
/* 311 */         new StringBuilder(String.valueOf(tree.getName().replace(this.parameters.heuristic.verbose(), this.parameters.heuristic.toString()).replace("population", "pop").replace("individual", "ind")))
/* 312 */         .append(" aft").append(operator).toString(), 
/* 312 */         true, false) + "\n");
/*     */     } catch (Exception e) {
/* 314 */       e.printStackTrace();
/* 315 */       this.monitor.showText("\n Error when writing in file OperatorsDetails.log");
/* 316 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 317 */       for (StackTraceElement el : e.getStackTrace())
/* 318 */         this.monitor.showText("\tat " + el.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void printOperatorFrequenciesUpdate(int currentStep, Map<Parameters.Operator, Integer> use, Map<Parameters.Operator, Double> scoreImprovements, Map<Parameters.Operator, Long> performances)
/*     */   {
/*     */     try {
/* 325 */       this.statOpWriter.write("Likelihood improvements at step " + currentStep + " : " + "\n");
/* 326 */       this.statOpWriter.write("-------------------------------------\n");
/* 327 */       this.statOpWriter.write("Operator\tnbrUse\timprove ML\timprove rescaled" + (this.monitor.trackPerformances() ? "\tmean execution time (nanosec)" : "") + "\n" + "\n");
/* 328 */       for (Parameters.Operator op : use.keySet())
/* 329 */         this.statOpWriter.write(op + "\t" + use.get(op) + "\t" + Tools.doubletoString(((Double)scoreImprovements.get(op)).doubleValue(), 4) + 
/* 330 */           "\t" + Tools.doubletoString(((Double)scoreImprovements.get(op)).doubleValue() / ((Integer)use.get(op)).intValue(), 4) + (this.monitor.trackPerformances() ? "\t" + Math.round(((Long)performances.get(op)).longValue() / ((Integer)use.get(op)).intValue()) : "") + "\n");
/*     */     }
/*     */     catch (Exception e) {
/* 333 */       e.printStackTrace();
/* 334 */       this.monitor.showText("\n Error when writing in file OperatorsStatistics.log");
/* 335 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 336 */       for (StackTraceElement el : e.getStackTrace())
/* 337 */         this.monitor.showText("\tat " + el.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void printOperatorFrequenciesUpdate(Map<Parameters.Operator, Double> frequencies)
/*     */   {
/*     */     try {
/* 344 */       this.statOpWriter.write("New Frequencies : \n");
/* 345 */       this.statOpWriter.write("------------------\n");
/* 346 */       for (Entry e : frequencies.entrySet()) {
/* 347 */         this.statOpWriter.write(e.getKey() + " : " + Tools.doubletoString(((Double)e.getValue()).doubleValue() * 100.0D, 2) + "%" + "\n");
/*     */       }
/* 349 */       this.statOpWriter.write("----------------------------------------------------------\n");
/*     */     } catch (Exception e) {
/* 351 */       e.printStackTrace();
/* 352 */       this.monitor.showText("\n Error when writing in file OperatorsStatistics.log");
/* 353 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 354 */       for (StackTraceElement el : e.getStackTrace())
/* 355 */         this.monitor.showText("\tat " + el.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void printOperatorStatistics(int numStep, Map<Parameters.Operator, Integer> use, Map<Parameters.Operator, Double> scoreImprovements, Map<Parameters.Operator, Long> performances, int outgroupTargeted, int ingroupTargeted, Map<Parameters.Operator, Map<Integer, Integer>> cancelByConsensus)
/*     */   {
/*     */     try
/*     */     {
/* 363 */       this.statOpWriter.write("Likelihood improvements at end (" + numStep + " steps) : " + "\n");
/* 364 */       this.statOpWriter.write("---------------------------------------------\n");
/* 365 */       this.statOpWriter.write("Operator\tnbrUse\timprove ML\timprove rescaled" + (this.monitor.trackPerformances() ? "\tmean execution time (nanosec)" : "") + "\n" + "\n");
/* 366 */       for (Parameters.Operator op : use.keySet()) {
/* 367 */         this.statOpWriter.write(op + "\t" + use.get(op) + "\t" + Tools.doubletoString(((Double)scoreImprovements.get(op)).doubleValue(), 4) + 
/* 368 */           "\t" + Tools.doubletoString(((Double)scoreImprovements.get(op)).doubleValue() / ((Integer)use.get(op)).intValue(), 4) + (this.monitor.trackPerformances() ? "\t" + Math.round(((Long)performances.get(op)).longValue() / ((Integer)use.get(op)).intValue()) : "") + "\t" + "\n");
/*     */       }
/* 370 */       this.statOpWriter.write("\n");
/* 371 */       this.statOpWriter.write("Number of times ingroup was targeted by an operator : " + ingroupTargeted + "\n");
/* 372 */       this.statOpWriter.write("Number of times outgroup was targeted by an operator : " + outgroupTargeted + "\n");
/* 373 */       this.statOpWriter.write("\n");
/* 374 */       this.statOpWriter.write("Number of mutations cancelled by consensus : \n");
/* 375 */       this.statOpWriter.write("---------------------------------------------\n");
/* 376 */       this.statOpWriter.write("Operator : \ttotal");
/* 377 */       Set todo = new HashSet();
/* 378 */       for (Map map : cancelByConsensus.values()) {
/* 379 */         todo.addAll(map.keySet());
/*     */       }
/* 381 */       for (int i = 0; !todo.isEmpty(); i++) {
/* 382 */         todo.remove(Integer.valueOf(i));
/* 383 */         this.statOpWriter.write("\t" + i * 100 + "-" + ((i + 1) * 100 - 1));
/*     */       }
/* 385 */       this.statOpWriter.write("\n");
/* 386 */       for (Parameters.Operator operator : cancelByConsensus.keySet()) {
/* 387 */         this.statOpWriter.write(operator + " : " + "\t");
/* 388 */         sum = 0;
/* 389 */         for (Iterator localIterator3 = ((Map)cancelByConsensus.get(operator)).values().iterator(); localIterator3.hasNext(); ) { int c = ((Integer)localIterator3.next()).intValue();
/* 390 */           sum += c;
/*     */         }
/* 392 */         this.statOpWriter.write("\t" + sum);
/* 393 */         todo = new HashSet(((Map)cancelByConsensus.get(operator)).keySet());
/* 394 */         for (i = 0; !todo.isEmpty(); i++) {
/* 395 */           if (todo.remove(Integer.valueOf(i)))
/* 396 */             this.statOpWriter.write("\t" + ((Map)cancelByConsensus.get(operator)).get(Integer.valueOf(i)));
/*     */           else {
/* 398 */             this.statOpWriter.write("\t0");
/*     */           }
/*     */         }
/* 401 */         this.statOpWriter.write("\n");
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */       int sum;
/*     */       int i;
/* 404 */       e.printStackTrace();
/* 405 */       this.monitor.showText("\n Error when writing in file OperatorsStatistics.log");
/* 406 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 407 */       for (StackTraceElement el : e.getStackTrace())
/* 408 */         this.monitor.showText("\tat " + el.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void printDetailsHC(int step, double bestLikelihood, double currentLikelihood, Parameters.Operator operator, boolean improvement)
/*     */   {
/*     */     try {
/* 415 */       this.searchLogWriter.write(step + "\t" + Tools.doubletoString(bestLikelihood, 4) + "\t" + 
/* 416 */         Tools.doubletoString(currentLikelihood, 4) + "\t" + operator + "\t" + (improvement ? "YES" : "NO") + "\n");
/*     */     } catch (Exception e) {
/* 418 */       e.printStackTrace();
/* 419 */       this.monitor.showText("\n Error when writing in file Heuristic.log");
/* 420 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 421 */       for (StackTraceElement el : e.getStackTrace())
/* 422 */         this.monitor.showText("\tat " + el.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void printDetailsSA(int step, double bestLikelihood, double S0Likelihood, double currentLikelihood, Parameters.Operator operator, String status, double tempAcceptance, double temperature, int coolingSteps, int successes, int failures, int reheatingDecrements)
/*     */   {
/*     */     try
/*     */     {
/* 430 */       this.searchLogWriter.write(step + "\t" + Tools.doubletoString(bestLikelihood, 4) + "\t" + 
/* 431 */         Tools.doubletoString(S0Likelihood, 4) + "\t" + Tools.doubletoString(currentLikelihood, 4) + "\t" + 
/* 432 */         operator + "\t" + status + "\t" + Tools.doubletoString(tempAcceptance * 100.0D, 2) + "%" + "\t" + Tools.doubletoString(temperature, 4) + 
/* 433 */         "\t" + coolingSteps + "\t" + successes + "\t" + failures + "\t" + reheatingDecrements + "\n");
/*     */     } catch (Exception e) {
/* 435 */       e.printStackTrace();
/* 436 */       this.monitor.showText("\n Error when writing in file Heuristic.log");
/* 437 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 438 */       for (StackTraceElement el : e.getStackTrace())
/* 439 */         this.monitor.showText("\tat " + el.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void printDetailsGA(int step, Tree[] mutantML, Parameters.Operator[] operator)
/*     */   {
/*     */     try {
/* 446 */       this.searchLogWriter.write(step + "\t");
/* 447 */       for (int i = 0; i < mutantML.length; i++)
/* 448 */         this.searchLogWriter.write(Tools.doubletoString(mutantML[i].getEvaluation(), 4) + "\t" + (
/* 449 */           operator[i] == null ? "No mutation" : operator[i]) + "\t");
/*     */     }
/*     */     catch (Exception e) {
/* 452 */       e.printStackTrace();
/* 453 */       this.monitor.showText("\n Error when writing in file Heuristic.log");
/* 454 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 455 */       for (StackTraceElement el : e.getStackTrace())
/* 456 */         this.monitor.showText("\tat " + el.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void printDetailsGA(int step, String selectionDetails, Tree[] selectedML, double bestLikelihood)
/*     */   {
/*     */     try {
/* 463 */       this.searchLogWriter.write(selectionDetails + "\t");
/* 464 */       for (int i = 0; i < selectedML.length; i++) {
/* 465 */         this.searchLogWriter.write(Tools.doubletoString(selectedML[i].getEvaluation(), 4) + "\t");
/*     */       }
/* 467 */       this.searchLogWriter.write(Tools.doubletoString(bestLikelihood, 4) + "\n");
/*     */     } catch (Exception e) {
/* 469 */       e.printStackTrace();
/* 470 */       this.monitor.showText("\n Error when writing in file Heuristic.log");
/* 471 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 472 */       for (StackTraceElement el : e.getStackTrace())
/* 473 */         this.monitor.showText("\tat " + el.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void printDetailsCP(Tree[] mutantML, Parameters.Operator[] operator, int currentPop)
/*     */   {
/*     */     try {
/* 480 */       StringBuilder s = new StringBuilder();
/* 481 */       for (int i = 0; i < mutantML.length; i++) {
/* 482 */         s.append(Tools.doubletoString(mutantML[i].getEvaluation(), 4) + "\t" + (operator[i] == null ? "No mutation" : operator[i]) + "\t");
/*     */       }
/* 484 */       this.cpDetailsTracking.put(Integer.valueOf(currentPop), s.toString());
/*     */     } catch (Exception e) {
/* 486 */       e.printStackTrace();
/* 487 */       this.monitor.showText("\n Error when writing in file Heuristic.log");
/* 488 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 489 */       for (StackTraceElement el : e.getStackTrace())
/* 490 */         this.monitor.showText("\tat " + el.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void printDetailsCP(Tree[] hybridML, String[] parents, int currentPop)
/*     */   {
/*     */     try {
/* 497 */       StringBuilder s = new StringBuilder();
/* 498 */       for (int i = 0; i < hybridML.length; i++) {
/* 499 */         s.append(Tools.doubletoString(hybridML[i].getEvaluation(), 4) + "\t" + (parents[i] == null ? "No recombination" : parents[i]) + "\t");
/*     */       }
/* 501 */       this.cpDetailsTracking.put(Integer.valueOf(currentPop), s.toString());
/*     */     } catch (Exception e) {
/* 503 */       e.printStackTrace();
/* 504 */       this.monitor.showText("\n Error when writing in file Heuristic.log");
/* 505 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 506 */       for (StackTraceElement el : e.getStackTrace())
/* 507 */         this.monitor.showText("\tat " + el.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void printDetailsCP(int step, String[] selectionDetails, Tree[][] selectedML, double bestLikelihood)
/*     */   {
/*     */     try {
/* 514 */       this.searchLogWriter.write(step + "\t");
/* 515 */       for (int p = 0; p < selectionDetails.length; p++) {
/* 516 */         this.searchLogWriter.write((String)this.cpDetailsTracking.remove(Integer.valueOf(p)));
/*     */       }
/* 518 */       for (int p = 0; p < selectionDetails.length; p++) {
/* 519 */         this.searchLogWriter.write(selectionDetails[p] + "\t");
/* 520 */         for (int i = 0; i < selectedML[p].length; i++) {
/* 521 */           this.searchLogWriter.write(Tools.doubletoString(selectedML[p][i].getEvaluation(), 4) + "\t");
/*     */         }
/*     */       }
/* 524 */       this.searchLogWriter.write(Tools.doubletoString(bestLikelihood, 4) + "\n");
/*     */     } catch (Exception e) {
/* 526 */       e.printStackTrace();
/* 527 */       this.monitor.showText("\n Error when writing in file Heuristic.log");
/* 528 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 529 */       for (StackTraceElement el : e.getStackTrace())
/* 530 */         this.monitor.showText("\tat " + el.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void printTreesHC(int step, Tree bestTree, Tree currentTree)
/*     */   {
/*     */     try {
/* 537 */       this.searchTreeWriter.write(bestTree.toNewickLineWithML(new StringBuilder("HC_best_step").append(step).toString(), false, true) + "\n");
/* 538 */       this.searchTreeWriter.write(currentTree.toNewickLineWithML(new StringBuilder("HC_current_step").append(step).toString(), false, true) + "\n");
/*     */     } catch (Exception e) {
/* 540 */       e.printStackTrace();
/* 541 */       this.monitor.showText("\n Error when writing in file Heuristic.tre");
/* 542 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 543 */       for (StackTraceElement el : e.getStackTrace())
/* 544 */         this.monitor.showText("\tat " + el.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void printTreesSA(int step, Tree bestTree, Tree S0Tree, Tree currentTree)
/*     */   {
/*     */     try {
/* 551 */       this.searchTreeWriter.write(bestTree.toNewickLineWithML(new StringBuilder("SA_best_step").append(step).toString(), false, true) + "\n");
/* 552 */       this.searchTreeWriter.write(currentTree.toNewickLineWithML(new StringBuilder("SA_S0_step").append(step).toString(), false, true) + "\n");
/* 553 */       this.searchTreeWriter.write(currentTree.toNewickLineWithML(new StringBuilder("SA_current_step").append(step).toString(), false, true) + "\n");
/*     */     } catch (Exception e) {
/* 555 */       e.printStackTrace();
/* 556 */       this.monitor.showText("\n Error when writing in file Heuristic.tre");
/* 557 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 558 */       for (StackTraceElement el : e.getStackTrace())
/* 559 */         this.monitor.showText("\tat " + el.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void printTreesGA(int step, Tree[] trees, boolean selectionDone)
/*     */   {
/*     */     try {
/* 566 */       String type = selectionDone ? "_selected" : "_mutant";
/* 567 */       for (int i = 0; i < trees.length; i++)
/* 568 */         this.searchTreeWriter.write(trees[i].toNewickLineWithML(new StringBuilder("GA_step").append(step).append(type).append(i).toString(), false, true) + "\n");
/*     */     } catch (Exception e) {
/* 570 */       e.printStackTrace();
/* 571 */       this.monitor.showText("\n Error when writing in file Heuristic.tre");
/* 572 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 573 */       for (StackTraceElement el : e.getStackTrace())
/* 574 */         this.monitor.showText("\tat " + el.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void printTreesCP(int step, Tree[] trees, int pop, boolean recombined)
/*     */   {
/*     */     try {
/* 581 */       for (int i = 0; i < trees.length; i++)
/* 582 */         this.searchTreeWriter.write(trees[i].toNewickLineWithML(new StringBuilder("CP_step").append(step).append("_pop").append(pop).append("_").append(recombined ? "hybrid" : "mutant").append(i).toString(), false, true) + "\n");
/*     */     }
/*     */     catch (Exception e) {
/* 585 */       e.printStackTrace();
/* 586 */       this.monitor.showText("\n Error when writing in file Heuristic.tre");
/* 587 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 588 */       for (StackTraceElement el : e.getStackTrace())
/* 589 */         this.monitor.showText("\tat " + el.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void printTreesCP(int step, Tree[][] trees)
/*     */   {
/*     */     try {
/* 596 */       for (int p = 0; p < trees.length; p++)
/* 597 */         for (int i = 0; i < trees[p].length; i++)
/* 598 */           this.searchTreeWriter.write(trees[p][i].toNewickLineWithML(new StringBuilder("CP_step").append(step).append("_pop").append(p).append("_selected").append(i).toString(), false, true) + "\n");
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 602 */       e.printStackTrace();
/* 603 */       this.monitor.showText("\n Error when writing in file Heuristic.tre");
/* 604 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 605 */       for (StackTraceElement el : e.getStackTrace())
/* 606 */         this.monitor.showText("\tat " + el.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void printEndTreesHeuristic()
/*     */   {
/*     */     try {
/* 613 */       this.searchTreeWriter.write("End;  [Heuristic ended " + new Date(System.currentTimeMillis()).toString() + "]" + "\n");
/*     */     } catch (Exception e) {
/* 615 */       e.printStackTrace();
/* 616 */       this.monitor.showText("\n Error when writing in file Heuristic.tre");
/* 617 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 618 */       for (StackTraceElement el : e.getStackTrace())
/* 619 */         this.monitor.showText("\tat " + el.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void printConsensus(int step, Consensus consensus)
/*     */   {
/*     */     try {
/* 626 */       this.consensusWriter.write("Step " + step + " consensus partitions : " + "\n");
/* 627 */       this.consensusWriter.write(consensus.showConsensus() + "\n");
/* 628 */       this.consensusWriter.write("----------------------------------------------------------\n");
/*     */     } catch (Exception e) {
/* 630 */       e.printStackTrace();
/* 631 */       this.monitor.showText("\n Error when writing in file Consensus.log");
/* 632 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 633 */       for (StackTraceElement el : e.getStackTrace())
/* 634 */         this.monitor.showText("\tat " + el.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void trackPerformances(String action, int level)
/*     */   {
/* 640 */     if (this.performanceTracking.containsKey(action)) {
/*     */       try {
/* 642 */         switch (level) {
/*     */         case 0:
/* 644 */           this.perfWriter.write(action + "\t" + "\t" + "\t" + (System.nanoTime() - ((Long)this.performanceTracking.remove(action)).longValue()) + "\n");
/* 645 */           break;
/*     */         case 1:
/* 647 */           this.perfWriter.write("\t" + action + "\t" + (System.nanoTime() - ((Long)this.performanceTracking.remove(action)).longValue()) + "\t" + "\n");
/* 648 */           break;
/*     */         default:
/* 650 */           this.monitor.showText("Performance of level " + level + " is not supported (action = " + action + ").");
/*     */         }
/*     */       } catch (Exception e) {
/* 653 */         e.printStackTrace();
/* 654 */         this.monitor.showText("\n Error when writing in file Performances.log");
/* 655 */         this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 656 */         for (StackTraceElement el : e.getStackTrace())
/* 657 */           this.monitor.showText("\tat " + el.toString());
/*     */       }
/*     */     }
/*     */     else
/* 661 */       this.performanceTracking.put(action, Long.valueOf(System.nanoTime()));
/*     */   }
/*     */ 
/*     */   public void printAncestralSequences(List<Tree> trees)
/*     */   {
/*     */     try
/*     */     {
/*     */       Iterator localIterator2;
/* 667 */       for (Iterator localIterator1 = trees.iterator(); localIterator1.hasNext(); 
/* 672 */         localIterator2.hasNext())
/*     */       {
/* 667 */         Tree tree = (Tree)localIterator1.next();
/* 668 */         this.ancseqWriter.write("Ancestral sequences reconstruction for tree '" + tree.getName() + "':" + "\n");
/* 669 */         this.ancseqWriter.write("----------------------------------------------------------\n");
/* 670 */         this.ancseqWriter.write("Tree in Newick format with internal nodes labels : \n");
/* 671 */         this.ancseqWriter.write(tree.toNewickLine(true, false) + "\n" + "\n");
/* 672 */         localIterator2 = tree.getInodes().iterator(); continue; node = (Node)localIterator2.next();
/* 673 */         kit = new DefaultEditorKit();
/* 674 */         DefaultStyledDocument doc = tree.printAncestralStates(node);
/* 675 */         kit.write(this.ancseqWriter, doc, 0, doc.getLength());
/* 676 */         this.ancseqWriter.write("\n");
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 683 */       DefaultEditorKit kit;
/* 680 */       e.printStackTrace();
/* 681 */       this.monitor.showText("\n Error when writing in file AncestralSequences.log");
/* 682 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 683 */       Node localNode1 = (kit = e.getStackTrace()).length; for (Node node = 0; node < localNode1; node++) { StackTraceElement el = kit[node];
/* 684 */         this.monitor.showText("\tat " + el.toString()); }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateConsensusTree(Tree consensusTree)
/*     */   {
/*     */     try {
/* 691 */       File output = new File(this.dirPath);
/* 692 */       if (!output.exists()) output.mkdir();
/* 693 */       output = new File(this.dirPath + "/" + "ConsensusTree.tre");
/* 694 */       FileWriter fw = new FileWriter(output);
/* 695 */       fw.write("#NEXUS\n");
/* 696 */       fw.write("\n");
/* 697 */       fw.write("Begin trees;  [Treefile created " + new Date(System.currentTimeMillis()).toString() + "]" + "\n");
/* 698 */       fw.write(consensusTree.toNewickLine(false, true) + "\n");
/* 699 */       fw.write("End;\n");
/* 700 */       fw.close();
/*     */     } catch (Exception e) {
/* 702 */       e.printStackTrace();
/* 703 */       this.monitor.showText("\n Error when writing file ConsensusTree.tre");
/* 704 */       this.monitor.showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 705 */       for (StackTraceElement el : e.getStackTrace())
/* 706 */         this.monitor.showText("\tat " + el.toString());
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.monitors.PrintMonitor
 * JD-Core Version:    0.6.2
 */