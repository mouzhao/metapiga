/*     */ package metapiga.monitors;
/*     */ 
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */
/*     */ import java.awt.Dimension;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.Font;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.Toolkit;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.File;
/*     */ import java.io.FileWriter;
/*     */
/*     */ import java.text.DateFormat;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ import java.util.concurrent.CountDownLatch;
/*     */ import java.util.concurrent.ExecutorService;
/*     */ import java.util.concurrent.Executors;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import java.util.concurrent.atomic.AtomicInteger;
/*     */ import javax.swing.BorderFactory;
/*     */
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JOptionPane;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JProgressBar;
/*     */
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JSplitPane;
/*     */ import javax.swing.JTabbedPane;
/*     */ import javax.swing.JTextArea;
/*     */ import javax.swing.JToggleButton;
/*     */
/*     */ import javax.swing.SwingUtilities;
/*     */ import metapiga.MainFrame;
/*     */ import metapiga.MetaPIGA;
/*     */ import metapiga.ScrollableFlowPanel;
/*     */ import metapiga.cloud.CloudConnection;
/*     */
/*     */ import metapiga.cloud.CloudStatGraphical;
/*     */ import metapiga.cloud.SearchGraphicalCloud;
/*     */ import metapiga.grid.GridConnection;
/*     */
/*     */ import metapiga.grid.GridStatGraphical;
/*     */ import metapiga.grid.SearchGraphicalGrid;
/*     */
/*     */ import metapiga.parameters.Parameters;
/*     */
/*     */
/*     */
/*     */ import metapiga.trees.Consensus;
/*     */ import metapiga.trees.ConsensusMRE;
/*     */ import metapiga.trees.ConsensusNode;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.trees.TreePanel;
/*     */ import metapiga.trees.TreePanel.Type;
/*     */ import metapiga.trees.TreeViewer;
/*     */ import metapiga.utilities.Tools;
/*     */ import org.biojavax.bio.phylo.io.nexus.CharactersBlock;
/*     */ import org.biojavax.bio.phylo.io.nexus.MetapigaBlock;
/*     */ 
/*     */ public class SearchOnceGraphical extends JFrame
/*     */   implements Runnable
/*     */ {
/*     */   private ExecutorService executor;
/*     */   private int nJobs;
/*     */   private AtomicInteger replicatesDone;
/*     */   private AtomicInteger replicatesAvailable;
/*     */   private AtomicInteger replicatesTotal;
/*     */   private AtomicInteger countMRE;
/*     */   final Parameters parameters;
/*  78 */   private final List<Tree> allSolutionTrees = new Vector();
/*     */   public final String runLabel;
/*     */   private String runDirectory;
/*     */   public String dirPath;
/*  82 */   private double currentDisplayedEvaluation = 0.0D;
/*  83 */   private TreePanel.Type currentTreeType = TreePanel.Type.RECTANGULAR_CLADOGRAM;
/*  84 */   private boolean stopHeuristic = false;
/*     */   private SearchOnceGraphicalMonitor[] monitors;
/*     */   private SearchGraphicalGrid[] grids;
/*     */   private CountDownLatch latch;
/*     */   private Tree consensusTree;
/*  89 */   private ConsensusMRE consensusMRE = new ConsensusMRE();
/*  90 */   private GridConnection grid = null;
/*  91 */   private boolean stopGrid = false;
/*     */   private SearchGraphicalCloud[] clouds;
/*  93 */   private CloudConnection cloud = null;
/*  94 */   private boolean stopCloud = false;
/*     */ 
/*  96 */   private final JPanel mainPanel = new JPanel();
/*  97 */   private final BorderLayout borderLayout1 = new BorderLayout();
/*  98 */   private final JSplitPane leftSplitPane = new JSplitPane();
/*  99 */   private final JScrollPane logScrollPane = new JScrollPane();
/* 100 */   private final JTextArea logTextArea = new JTextArea();
/* 101 */   private final JPanel southPanel = new JPanel();
/* 102 */   private final JButton closeButton = new JButton();
/* 103 */   private final JToggleButton treeBranchButton = new JToggleButton();
/* 104 */   private final JSplitPane mainSplitPane = new JSplitPane();
/*     */   private TreePanel treePanel;
/* 106 */   private final JPanel rightPanel = new JPanel();
/* 107 */   private final JScrollPane treeScrollPane = new JScrollPane();
/* 108 */   private final GridBagLayout gridBagLayout3 = new GridBagLayout();
/* 109 */   private final GridBagLayout gridBagLayout4 = new GridBagLayout();
/* 110 */   private final JProgressBar replicatesProgressBar = new JProgressBar();
/* 111 */   private final JProgressBar memoryBar = new JProgressBar();
/* 112 */   private final JLabel mreLabel = new JLabel();
/* 113 */   private final JProgressBar mreProgress = new JProgressBar();
/* 114 */   private final JScrollPane graphsScrollPane = new JScrollPane();
/* 115 */   private final JTabbedPane graphsPanel = new JTabbedPane();
/* 116 */   private final ScrollableFlowPanel gridsPanel = new ScrollableFlowPanel();
/*     */   public GridStatGraphical gridStatGraphical;
/*     */   public CloudStatGraphical cloudStatGraphical;
/*     */ 
/*     */   public SearchOnceGraphical(Parameters parameters)
/*     */   {
/* 121 */     super(parameters.label + " : start search");
/* 122 */     setIconImage(Tools.getScaledIcon(MainFrame.imageStartRun, 32).getImage());
/* 123 */     this.parameters = parameters;
/* 124 */     DateFormat df = new SimpleDateFormat("yyyy-MM-dd - HH_mm_ss");
/* 125 */     this.runDirectory = parameters.outputDir;
/* 126 */     File dir = new File(this.runDirectory);
/* 127 */     if (!dir.exists()) {
/* 128 */       dir.mkdirs();
/*     */     }
/* 130 */     this.runLabel = (parameters.label + " - " + df.format(Long.valueOf(System.currentTimeMillis())));
/* 131 */     this.dirPath = (this.runDirectory + "/" + this.runLabel);
/* 132 */     dir = new File(this.dirPath);
/* 133 */     if (!dir.exists()) dir.mkdir(); try
/*     */     {
/* 135 */       switch ($SWITCH_TABLE$metapiga$parameters$Parameters$ReplicatesStopCondition()[parameters.replicatesStopCondition.ordinal()]) {
/*     */       case 1:
/* 137 */         this.replicatesTotal = new AtomicInteger(parameters.replicatesNumber);
/* 138 */         break;
/*     */       case 2:
/* 140 */         this.replicatesTotal = new AtomicInteger(parameters.replicatesMaximum);
/*     */       }
/*     */ 
/* 144 */       this.replicatesAvailable = new AtomicInteger();
/* 145 */       this.replicatesDone = new AtomicInteger();
/* 146 */       this.countMRE = new AtomicInteger();
/* 147 */       if (parameters.useGrid) {
/* 148 */         this.nJobs = this.replicatesTotal.get();
/* 149 */         this.latch = new CountDownLatch(this.nJobs);
/* 150 */         this.grids = new SearchGraphicalGrid[this.nJobs];
/* 151 */         for (int i = 0; i < this.nJobs; i++)
/* 152 */           this.grids[i] = new SearchGraphicalGrid(this, parameters, i + 1);
/*     */       }
/* 154 */       else if (parameters.useCloud) {
/* 155 */         this.nJobs = this.replicatesTotal.get();
/* 156 */         this.latch = new CountDownLatch(this.nJobs);
/* 157 */         this.clouds = new SearchGraphicalCloud[this.nJobs];
/* 158 */         for (int i = 0; i < this.nJobs; i++)
/* 159 */           this.clouds[i] = new SearchGraphicalCloud(this, parameters, i + 1);
/*     */       }
/*     */       else {
/* 162 */         this.latch = null;
/* 163 */         this.nJobs = parameters.replicatesParallel;
/* 164 */         this.monitors = new SearchOnceGraphicalMonitor[this.nJobs];
/* 165 */         for (int i = 0; i < this.nJobs; i++) {
/* 166 */           this.monitors[i] = new SearchOnceGraphicalMonitor(this, this.dirPath);
/*     */         }
/*     */       }
/*     */ 
/* 170 */       jbInit();
/* 171 */       pack();
/*     */     }
/*     */     catch (Exception ex) {
/* 174 */       ex.printStackTrace();
/*     */     }
/* 176 */     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/* 177 */     Dimension windowSize = getSize();
/* 178 */     setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), Math.max(0, (screenSize.height - windowSize.height) / 2));
/* 179 */     setExtendedState(6);
/* 180 */     setDefaultCloseOperation(0);
/*     */   }
/*     */ 
/*     */   private void jbInit() {
/* 184 */     this.mainPanel.setLayout(this.borderLayout1);
/* 185 */     this.rightPanel.setLayout(this.gridBagLayout3);
/* 186 */     this.southPanel.setLayout(this.gridBagLayout4);
/* 187 */     this.leftSplitPane.setOrientation(0);
/* 188 */     this.leftSplitPane.setResizeWeight(0.5D);
/* 189 */     this.logTextArea.setBackground(Color.black);
/* 190 */     this.logTextArea.setForeground(Color.green);
/* 191 */     this.logTextArea.setEditable(false);
/* 192 */     this.logTextArea.setText("");
/* 193 */     this.logTextArea.setLineWrap(true);
/* 194 */     this.logTextArea.setWrapStyleWord(true);
/* 195 */     this.logTextArea.setFont(new Font("Geneva", 0, 12));
/* 196 */     this.closeButton.setText("STOP");
/* 197 */     this.closeButton.addActionListener(new SearchDialog_closeButton_actionAdapter(this));
/* 198 */     this.treeBranchButton.setToolTipText("Show/Hide branch length");
/* 199 */     this.treeBranchButton.setContentAreaFilled(false);
/* 200 */     this.treeBranchButton.setIcon(MainFrame.imagePhylogram);
/* 201 */     this.treeBranchButton.setSelectedIcon(MainFrame.imageRectangularCladogram);
/* 202 */     this.treeBranchButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 203 */     this.treeBranchButton.setMaximumSize(new Dimension(30, 30));
/* 204 */     this.treeBranchButton.setMinimumSize(new Dimension(30, 30));
/* 205 */     this.treeBranchButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 207 */         if (SearchOnceGraphical.this.treeBranchButton.isSelected()) {
/* 208 */           SearchOnceGraphical.this.currentTreeType = TreePanel.Type.PHYLOGRAM;
/* 209 */           SearchOnceGraphical.this.treePanel.changeDrawingType(SearchOnceGraphical.this.currentTreeType);
/*     */         } else {
/* 211 */           SearchOnceGraphical.this.currentTreeType = TreePanel.Type.RECTANGULAR_CLADOGRAM;
/* 212 */           SearchOnceGraphical.this.treePanel.changeDrawingType(SearchOnceGraphical.this.currentTreeType);
/*     */         }
/*     */       }
/*     */     });
/* 216 */     this.logScrollPane.setHorizontalScrollBarPolicy(31);
/* 217 */     this.logScrollPane.setVerticalScrollBarPolicy(22);
/* 218 */     this.logScrollPane.setAutoscrolls(true);
/* 219 */     this.logScrollPane.setPreferredSize(new Dimension(600, 400));
/* 220 */     this.logScrollPane.setViewportView(this.logTextArea);
/* 221 */     this.mainSplitPane.setDividerSize(10);
/* 222 */     this.mainSplitPane.setLeftComponent(this.leftSplitPane);
/* 223 */     this.mainSplitPane.setRightComponent(this.rightPanel);
/* 224 */     this.mainSplitPane.setOneTouchExpandable(true);
/* 225 */     this.mainSplitPane.setResizeWeight(0.5D);
/* 226 */     this.graphsScrollPane.setHorizontalScrollBarPolicy(31);
/* 227 */     if (this.parameters.useGrid) {
/* 228 */       this.graphsScrollPane.setViewportView(this.gridsPanel);
/* 229 */       this.gridsPanel.setLayout(new FlowLayout(3));
/* 230 */       this.gridStatGraphical = new GridStatGraphical(this.grids.length);
/* 231 */       this.gridsPanel.add(this.gridStatGraphical);
/* 232 */       for (int i = 0; i < this.grids.length; i++)
/* 233 */         this.gridsPanel.add(this.grids[i]);
/*     */     }
/* 235 */     else if (this.parameters.useCloud) {
/* 236 */       this.graphsScrollPane.setViewportView(this.gridsPanel);
/* 237 */       this.gridsPanel.setLayout(new FlowLayout(3));
/* 238 */       this.cloudStatGraphical = new CloudStatGraphical(this.clouds.length);
/* 239 */       this.gridsPanel.add(this.cloudStatGraphical);
/* 240 */       for (int i = 0; i < this.clouds.length; i++)
/* 241 */         this.gridsPanel.add(this.clouds[i]);
/*     */     }
/*     */     else
/*     */     {
/* 245 */       this.graphsScrollPane.setViewportView(this.graphsPanel);
/* 246 */       this.graphsPanel.setTabLayoutPolicy(1);
/* 247 */       this.graphsPanel.setTabPlacement(1);
/* 248 */       for (int i = 0; i < this.monitors.length; i++) {
/* 249 */         this.monitors[i].setPreferredSize(new Dimension(100, 500));
/* 250 */         this.monitors[i].setMinimumSize(new Dimension(100, 500));
/* 251 */         this.monitors[i].setMaximumSize(new Dimension(100, 500));
/* 252 */         this.graphsPanel.add(this.monitors[i], "Core " + (i + 1));
/*     */       }
/*     */     }
/* 255 */     this.leftSplitPane.setTopComponent(this.logScrollPane);
/* 256 */     this.leftSplitPane.setBottomComponent(this.graphsScrollPane);
/* 257 */     this.treeScrollPane.getViewport().setBackground(Color.black);
/* 258 */     this.treeScrollPane.setForeground(Color.green);
/* 259 */     this.replicatesProgressBar.setPreferredSize(new Dimension(200, 18));
/* 260 */     this.replicatesProgressBar.setValue(this.replicatesDone.get());
/* 261 */     this.replicatesProgressBar.setString("Replicates done " + this.replicatesDone + "/" + this.replicatesTotal);
/* 262 */     this.replicatesProgressBar.setStringPainted(true);
/* 263 */     setTitle(this.runLabel + " : Replicates done " + this.replicatesDone + "/" + this.replicatesTotal);
/* 264 */     this.memoryBar.setPreferredSize(new Dimension(100, 18));
/* 265 */     this.memoryBar.setMaximum((int)(Runtime.getRuntime().maxMemory() / 1024L / 1024L));
/* 266 */     this.memoryBar.setValue(0);
/* 267 */     this.memoryBar.setString("used memory");
/* 268 */     this.memoryBar.setStringPainted(true);
/* 269 */     this.memoryBar.setToolTipText("Memory currently by MetaPIGA with bar length equal to allowed memory");
/* 270 */     getContentPane().add(this.mainPanel);
/* 271 */     this.mainPanel.add(this.southPanel, "South");
/*     */ 
/* 273 */     this.southPanel.add(this.memoryBar, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, 
/* 274 */       17, 0, new Insets(5, 15, 0, 5), 0, 0));
/* 275 */     this.southPanel.add(this.closeButton, new GridBagConstraints(2, 0, 1, 1, 1.0D, 0.0D, 
/* 276 */       10, 0, new Insets(5, 5, 0, 5), 0, 0));
/* 277 */     this.southPanel.add(this.treeBranchButton, new GridBagConstraints(4, 0, 1, 1, 0.0D, 0.0D, 
/* 278 */       13, 0, new Insets(5, 5, 0, 0), 0, 0));
/* 279 */     this.southPanel.add(this.replicatesProgressBar, new GridBagConstraints(1, 0, 1, 1, 0.0D, 0.0D, 
/* 280 */       17, 0, new Insets(5, 15, 0, 5), 0, 0));
/* 281 */     this.southPanel.add(this.mreLabel, new GridBagConstraints(3, 0, 1, 1, 0.0D, 0.0D, 
/* 282 */       17, 0, new Insets(5, 15, 0, 5), 0, 0));
/* 283 */     this.southPanel.add(this.mreProgress, new GridBagConstraints(3, 0, 1, 1, 0.0D, 0.0D, 
/* 284 */       17, 0, new Insets(5, 15, 0, 5), 0, 0));
/* 285 */     this.mainPanel.add(this.mainSplitPane, "Center");
/* 286 */     this.rightPanel.add(this.treeScrollPane, new GridBagConstraints(0, 0, 0, 0, 1.0D, 1.0D, 
/* 287 */       10, 1, new Insets(10, 10, 10, 10), 0, 0));
/* 288 */     if (this.replicatesTotal.get() > 1) {
/* 289 */       this.treePanel = new TreePanel("Consensus tree is waiting for at least 2 replicates to finish");
/* 290 */       this.treeScrollPane.setViewportView(this.treePanel);
/*     */     }
/* 292 */     this.mreLabel.setVisible(this.parameters.replicatesStopCondition == Parameters.ReplicatesStopCondition.NONE);
/* 293 */     this.mreProgress.setVisible(this.parameters.replicatesStopCondition == Parameters.ReplicatesStopCondition.MRE);
/* 294 */     this.mainSplitPane.setDividerLocation(0.5D);
/*     */   }
/*     */ 
/*     */   public void run() {
/* 298 */     setVisible(true);
/* 299 */     new Thread(new Runnable() {
/*     */       public void run() {
/* 301 */         while (!SearchOnceGraphical.this.stopHeuristic)
/*     */           try {
/* 303 */             final int usedMem = (int)((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024L);
/* 304 */             SwingUtilities.invokeLater(new Runnable() {
/*     */               public void run() {
/* 306 */                 SearchOnceGraphical.this.memoryBar.setValue(usedMem);
/* 307 */                 SearchOnceGraphical.this.memoryBar.setString(usedMem + " Mb");
/* 308 */                 SearchOnceGraphical.this.memoryBar.setStringPainted(true);
/*     */               }
/*     */             });
/* 311 */             Thread.sleep(1000L);
/*     */           } catch (InterruptedException ex) {
/* 313 */             ex.printStackTrace();
/*     */           }
/*     */       }
/*     */     }
/*     */     , "Used memory updater").start();
    /*     */     try {
/* 319 */       long startTime = System.currentTimeMillis();
/* 320 */       showText("Running " + this.runLabel + "\n" + this.parameters.printParameters());
/* 321 */       if (this.replicatesTotal.get() <= 1) {
/* 322 */         this.replicatesProgressBar.setVisible(false);
/* 323 */         this.mreLabel.setVisible(false);
/* 324 */         this.mreProgress.setVisible(false);
/*     */       } else {
/* 326 */         this.replicatesProgressBar.setMaximum(this.replicatesTotal.get());
/* 327 */         this.mreProgress.setMinimum(0);
/* 328 */         this.mreProgress.setValue(0);
/* 329 */         this.mreProgress.setMaximum(this.parameters.replicatesInterval);
/*     */       }
/* 331 */       this.grid = (this.parameters.useGrid ? new GridConnection(this, this.parameters, this.dirPath) : null);
/* 332 */       this.cloud = (this.parameters.useCloud ? new CloudConnection(this, this.parameters, this.dirPath) : null);
/* 333 */       this.executor = Executors.newFixedThreadPool(this.parameters.useGrid ? Math.min(this.nJobs, 1000) : this.nJobs);
/* 334 */       for (int i = 0; i < this.nJobs; i++) {
/* 335 */         if (this.parameters.useGrid) {
/* 336 */           if (!this.stopHeuristic) {
/* 337 */             this.grids[i].initialize(this.grid, this.latch);
/* 338 */             this.executor.execute(this.grids[i]);
/* 339 */             while (this.grids[i].getMonitorStatus() == GridMonitor.JobStatus.POOLED)
/*     */               try {
/* 341 */                 Thread.sleep(200L);
/*     */               } catch (InterruptedException ex) {
/* 343 */                 ex.printStackTrace();
/*     */               }
/*     */           }
/*     */           else {
/* 347 */             this.latch.countDown();
/*     */           }
/* 349 */         } else if (this.parameters.useCloud) {
/* 350 */           if (!this.stopHeuristic) {
/* 351 */             this.clouds[i].initialize(this.cloud, this.latch);
/* 352 */             this.executor.execute(this.clouds[i]);
/* 353 */             while (this.clouds[i].getMonitorStatus() == CloudMonitor.JobStatus.POOLED)
/*     */               try {
/* 355 */                 Thread.sleep(200L);
/*     */               } catch (InterruptedException ex) {
/* 357 */                 ex.printStackTrace();
/*     */               }
/*     */           }
/*     */           else {
/* 361 */             this.latch.countDown();
/*     */           }
/*     */         }
/* 364 */         else this.executor.execute(this.monitors[i]);
/*     */       }
/*     */ 
/* 367 */       if (this.parameters.useGrid) this.latch.await();
/* 368 */       this.executor.shutdown();
/* 369 */       this.executor.awaitTermination(1000L, TimeUnit.DAYS);
/* 370 */       Thread.sleep(500L);
/*     */ 
/* 372 */       if (this.parameters.useGrid) stopGrid();
/* 373 */       end(startTime);
/*     */     } catch (Exception e) {
/* 375 */       endFromException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   private void end(long startTime) {
/* 380 */     setTitle(this.runLabel + " : job finished");
/* 381 */     showText("");
/* 382 */     if (this.replicatesTotal.get() > 1) showText("All replicates done in " + Tools.doubletoString((System.currentTimeMillis() - startTime) / 60000.0D, 2) + " minutes");
/*     */ 
/* 384 */     Tree consensusTree = null;
/*     */     Tree optimizedConsensusTree;
/* 385 */     if (this.replicatesTotal.get() > 1) {
/*     */       try {
/* 387 */         Consensus consensus = new Consensus(this.allSolutionTrees, this.parameters.dataset);
/* 388 */         consensusTree = consensus.getConsensusTree(this.parameters);
/* 389 */         if (this.parameters.optimization == Parameters.Optimization.CONSENSUSTREE) {
/* 390 */           showText("\nOptimizing final consensus tree");
/* 391 */           optimizedConsensusTree = this.parameters.getOptimizer(consensusTree).getOptimizedTreeWithProgress(null, "Optimizing final consensus tree");
/* 392 */           consensusTree.cloneWithConsensus(optimizedConsensusTree);
/*     */         }
/*     */       } catch (Exception ex) {
/* 395 */         JOptionPane.showMessageDialog(null, Tools.getErrorPanel("Cannot build consensus tree", ex), "Consensus tree Error", 0);
/* 396 */         System.out.println("Cannot build consensus tree : " + ex.getMessage());
/* 397 */         ex.printStackTrace();
/*     */       }
/*     */     }
/*     */ 
/* 401 */     if (consensusTree != null) {
/* 402 */       consensusTree.setName(this.runLabel + " - " + consensusTree.getName() + " - " + this.replicatesDone + "_replicates");
/*     */     }
/* 404 */     for (Tree t : this.allSolutionTrees) {
/* 405 */       t.setName(this.runLabel + " - " + t.getName());
/*     */     }
/* 407 */     if ((this.stopHeuristic) && (this.replicatesTotal.get() > 1)) {
/* 408 */       if (this.parameters.useGrid) showText("\nUnfinished replicates have been discarded"); else
/* 409 */         showText("\nLast replicate was interrupted and has been discarded");  } showText("\n" + (this.stopHeuristic ? "JOB STOPPED BY USER" : "JOB DONE") + "\n");
/*     */ 
/* 413 */     File output = new File(this.dirPath + "/" + "Results.nex");
/*     */     Tree t;
/*     */     StackTraceElement[] arrayOfStackTraceElement;
/*     */     Tree localTree1;
/*     */     try { FileWriter fw = new FileWriter(output);
/* 416 */       BufferedWriter bw = new BufferedWriter(fw);
/* 417 */       bw.write("#NEXUS");
/* 418 */       bw.newLine();
/* 419 */       bw.newLine();
/* 420 */       this.parameters.getMetapigaBlock().writeObject(bw);
/* 421 */       bw.newLine();
/* 422 */       this.parameters.charactersBlock.writeObject(bw);
/* 423 */       bw.newLine();
/* 424 */       if (this.parameters.startingTreeGeneration == Parameters.StartingTreeGeneration.GIVEN) {
/* 425 */         this.parameters.writeTreeBlock(bw);
/* 426 */         bw.newLine();
/*     */       }
/* 428 */       bw.write("Begin trees;  [Result trees]");
/* 429 */       bw.newLine();
/* 430 */       if (consensusTree != null) {
/* 431 */         bw.write(consensusTree.toNewickLineWithML(consensusTree.getName(), false, true));
/* 432 */         bw.newLine();
/*     */       }
/* 434 */       for (Iterator localIterator = this.allSolutionTrees.iterator(); localIterator.hasNext(); ) { t = (Tree)localIterator.next();
/* 435 */         bw.write(t.toNewickLineWithML(t.getName(), false, true));
/* 436 */         bw.newLine();
/*     */       }
/* 438 */       bw.write("End;");
/* 439 */       bw.newLine();
/* 440 */       bw.close();
/* 441 */       fw.close();
/*     */     } catch (Exception e) {
/* 443 */       e.printStackTrace();
/* 444 */       showText("\n Error when writing results file");
/* 445 */       showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 446 */       localTree1 = (arrayOfStackTraceElement = e.getStackTrace()).length; t = 0; } for (; t < localTree1; t++) { StackTraceElement el = arrayOfStackTraceElement[t];
/* 447 */       showText("\tat " + el.toString());
/*     */     }
/*     */ 
/* 452 */     setDefaultCloseOperation(2);
/* 453 */     this.closeButton.setText("CLOSE");
/* 454 */     this.closeButton.setEnabled(true);
/* 455 */     setState(0);
/*     */   }
/*     */ 
/*     */   public void showText(final String text) {
/* 459 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 461 */         SearchOnceGraphical.this.logTextArea.append(text + "\n");
/* 462 */         SearchOnceGraphical.this.logTextArea.setCaretPosition(SearchOnceGraphical.this.logTextArea.getText().length());
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public int getNextReplicate()
/*     */   {
/* 469 */     if (this.replicatesAvailable.get() < this.replicatesTotal.get()) {
/* 470 */       return this.replicatesAvailable.incrementAndGet();
/*     */     }
/* 472 */     return -1;
/*     */   }
/*     */ 
/*     */   public void showReplicate()
/*     */   {
/* 477 */     final int repDone = this.replicatesDone.incrementAndGet();
/* 478 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 480 */         SearchOnceGraphical.this.replicatesProgressBar.setValue(repDone);
/* 481 */         SearchOnceGraphical.this.replicatesProgressBar.setString("Replicates done " + SearchOnceGraphical.this.replicatesDone + "/" + SearchOnceGraphical.this.replicatesTotal);
/* 482 */         SearchOnceGraphical.this.replicatesProgressBar.setStringPainted(true);
/* 483 */         SearchOnceGraphical.this.setTitle(SearchOnceGraphical.this.runLabel + " : Replicates done " + SearchOnceGraphical.this.replicatesDone + "/" + SearchOnceGraphical.this.replicatesTotal);
/*     */       }
/*     */     });
/* 486 */     if (repDone > 1)
/*     */       try {
/* 488 */         Consensus consensus = new Consensus(new ArrayList(this.allSolutionTrees), this.parameters.dataset);
/* 489 */         synchronized (this.consensusMRE) {
/* 490 */           this.consensusTree = consensus.getConsensusTree(this.parameters);
/* 491 */           this.consensusTree.setName("Consensus_tree_" + this.replicatesDone + "_replicates");
/* 492 */           this.consensusMRE.addConsensus(this.consensusTree, this.parameters, false);
/* 493 */           final double mre = this.consensusMRE.meanRelativeError();
/* 494 */           this.consensusTree.setName("Consensus_tree_" + this.replicatesDone + "_replicates" + " [MRE: " + Tools.doubleToPercent(mre, 2) + "]");
/* 495 */           SwingUtilities.invokeLater(new Runnable() {
/*     */             public void run() {
/* 497 */               SearchOnceGraphical.this.mreLabel.setText("Mean Relative Error of consensus tree: " + Tools.doubleToPercent(mre, 2));
/* 498 */               SearchOnceGraphical.this.mreProgress.setString("   Inter-replicates MRE: " + Tools.doubleToPercent(mre, 2) + "  ");
/* 499 */               SearchOnceGraphical.this.mreProgress.setStringPainted(true);
/*     */             }
/*     */           });
/* 502 */           if ((repDone > this.parameters.replicatesMinimum) && (mre < this.parameters.replicatesMRE)) {
/* 503 */             this.countMRE.incrementAndGet();
/* 504 */             if ((this.parameters.replicatesStopCondition == Parameters.ReplicatesStopCondition.MRE) && (this.countMRE.get() >= this.parameters.replicatesInterval)) {
/* 505 */               this.replicatesAvailable.set(this.replicatesTotal.get());
/* 506 */               if (this.parameters.useGrid) {
/* 507 */                 stopGrid();
/*     */               }
/* 509 */               showText("MRE condition has been met, all remaining replicates have been cancelled.");
/*     */             }
/*     */           } else {
/* 512 */             this.countMRE.set(0);
/* 513 */             this.consensusMRE.addConsensus(this.consensusTree, this.parameters, true);
/*     */           }
/* 515 */           this.mreProgress.setValue(this.countMRE.get());
/* 516 */           if (this.parameters.useGrid) this.grids[0].updateConsensusTree(this.consensusTree);
/* 517 */           else if (this.parameters.useCloud) this.clouds[0].updateConsensusTree(this.consensusTree); else
/* 518 */             this.monitors[0].updateConsensusTree(this.consensusTree);
/*     */         }
/* 520 */         SwingUtilities.invokeLater(new Runnable() {
/*     */           public void run() {
/* 522 */             int pos = SearchOnceGraphical.this.treeScrollPane.getVerticalScrollBar().getValue();
/* 523 */             SearchOnceGraphical.this.treeScrollPane.getViewport().remove(SearchOnceGraphical.this.treePanel);
/* 524 */             SearchOnceGraphical.this.treePanel = new TreePanel(SearchOnceGraphical.this.consensusTree, SearchOnceGraphical.this.currentTreeType, false, false);
/* 525 */             SearchOnceGraphical.this.treeScrollPane.setViewportView(SearchOnceGraphical.this.treePanel);
/* 526 */             SearchOnceGraphical.this.treeScrollPane.getVerticalScrollBar().setValue(pos);
/*     */           } } );
/*     */       }
/*     */       catch (Exception e) {
/* 530 */         e.printStackTrace();
/*     */       }
/*     */   }
/*     */ 
/*     */   public void showStartingTree(final Tree tree)
/*     */   {
/* 536 */     if (this.replicatesTotal.get() == 1) {
/* 537 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 539 */           SearchOnceGraphical.this.treePanel = new TreePanel(tree, SearchOnceGraphical.this.currentTreeType, false, false);
/* 540 */           SearchOnceGraphical.this.treeScrollPane.setViewportView(SearchOnceGraphical.this.treePanel);
/*     */         }
/*     */       });
/*     */       try {
/* 544 */         this.currentDisplayedEvaluation = tree.getEvaluation();
/*     */       } catch (Exception e) {
/* 546 */         e.printStackTrace();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean showCurrentTree(final Tree tree) {
/*     */     try {
/* 553 */       if ((this.replicatesTotal.get() == 1) && (this.currentDisplayedEvaluation != tree.getEvaluation())) {
/* 554 */         this.currentDisplayedEvaluation = tree.getEvaluation();
/* 555 */         SwingUtilities.invokeLater(new Runnable() {
/*     */           public void run() {
/* 557 */             int pos = SearchOnceGraphical.this.treeScrollPane.getVerticalScrollBar().getValue();
/* 558 */             SearchOnceGraphical.this.treeScrollPane.getViewport().remove(SearchOnceGraphical.this.treePanel);
/* 559 */             SearchOnceGraphical.this.treePanel = new TreePanel(tree.getAccessNode().getClass() == ConsensusNode.class ? tree : tree.clone(), SearchOnceGraphical.this.currentTreeType, false, false);
/* 560 */             SearchOnceGraphical.this.treeScrollPane.setViewportView(SearchOnceGraphical.this.treePanel);
/* 561 */             SearchOnceGraphical.this.treeScrollPane.getVerticalScrollBar().setValue(pos);
/*     */           }
/*     */         });
/*     */       }
/* 565 */       return true;
/*     */     } catch (Exception e) {
/* 567 */       e.printStackTrace();
/*     */     }
/* 569 */     return false;
/*     */   }
/*     */ 
/*     */   public void addSolutionTree(List<Tree> trees) {
/* 573 */     if ((!this.stopHeuristic) || (this.replicatesTotal.get() <= 1)) {
/* 574 */       synchronized (this.allSolutionTrees) {
/* 575 */         this.allSolutionTrees.addAll(trees);
/*     */       }
/* 577 */       showReplicate();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void endFromException(Exception e) {
/* 582 */     e.printStackTrace();
/* 583 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 585 */         SearchOnceGraphical.this.setState(0);
/* 586 */         SearchOnceGraphical.this.setDefaultCloseOperation(2);
/*     */       }
/*     */     });
/* 589 */     closeButton_actionPerformed(null);
/* 590 */     showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 591 */     for (StackTraceElement el : e.getStackTrace()) {
/* 592 */       showText("\tat " + el.toString());
/*     */     }
/* 594 */     JOptionPane.showMessageDialog(null, Tools.getErrorPanel("Error", e), "Error", 0);
/*     */   }
/*     */ 
/*     */   public void stopGrid() {
/* 598 */     if (!this.stopGrid) {
/* 599 */       this.stopGrid = true;
/* 600 */       new Thread(new Runnable() {
/*     */         public void run() {
/* 602 */           for (int i = 0; i < SearchOnceGraphical.this.nJobs; i++) {
/* 603 */             if (SearchOnceGraphical.this.grids[i].getMonitorStatus() == GridMonitor.JobStatus.NOT_INITIALIZED) SearchOnceGraphical.this.grids[i].stop();
/*     */           }
/* 605 */           for (int i = 0; i < SearchOnceGraphical.this.nJobs; i++) {
/* 606 */             if (SearchOnceGraphical.this.grids[i].getMonitorStatus() == GridMonitor.JobStatus.POOLED) SearchOnceGraphical.this.grids[i].stop();
/*     */           }
/* 608 */           for (int i = 0; i < SearchOnceGraphical.this.nJobs; i++) {
/* 609 */             if ((SearchOnceGraphical.this.grids[i].getMonitorStatus() != GridMonitor.JobStatus.NOT_INITIALIZED) && (SearchOnceGraphical.this.grids[i].getMonitorStatus() != GridMonitor.JobStatus.POOLED)) SearchOnceGraphical.this.grids[i].stop();
/*     */           }
/* 611 */           boolean allIsClosed = false;
/* 612 */           while (!allIsClosed) {
/* 613 */             allIsClosed = true;
/* 614 */             for (int i = 0; i < SearchOnceGraphical.this.nJobs; i++) {
/* 615 */               if (!SearchOnceGraphical.this.grids[i].isTerminated()) {
/* 616 */                 allIsClosed = false;
/*     */                 try {
/* 618 */                   Thread.sleep(500L);
/*     */                 } catch (Exception e) {
/* 620 */                   e.printStackTrace();
/*     */                 }
/* 622 */                 SearchOnceGraphical.this.grids[i].stop();
/*     */               }
/*     */             }
/* 625 */             if (!allIsClosed) {
/*     */               try {
/* 627 */                 Thread.sleep(1000L);
/*     */               } catch (Exception e) {
/* 629 */                 e.printStackTrace();
/*     */               }
/*     */             }
/*     */           }
/* 633 */           SearchOnceGraphical.this.showText("Ending GRID application  : " + (SearchOnceGraphical.this.grid.endApplication() ? "OK" : "FAILED"));
/* 634 */           SearchOnceGraphical.this.stopGrid = false;
/*     */         }
/*     */       }).start();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void stopCloud() {
/* 641 */     if (!this.stopCloud) {
/* 642 */       this.stopCloud = true;
/* 643 */       new Thread(new Runnable() {
/*     */         public void run() {
/* 645 */           for (int i = 0; i < SearchOnceGraphical.this.nJobs; i++) {
/* 646 */             if (SearchOnceGraphical.this.clouds[i].getMonitorStatus() == CloudMonitor.JobStatus.NOT_INITIALIZED) SearchOnceGraphical.this.clouds[i].stop();
/*     */           }
/* 648 */           for (int i = 0; i < SearchOnceGraphical.this.nJobs; i++) {
/* 649 */             if (SearchOnceGraphical.this.clouds[i].getMonitorStatus() == CloudMonitor.JobStatus.POOLED) SearchOnceGraphical.this.clouds[i].stop();
/*     */           }
/* 651 */           for (int i = 0; i < SearchOnceGraphical.this.nJobs; i++) {
/* 652 */             if ((SearchOnceGraphical.this.clouds[i].getMonitorStatus() != CloudMonitor.JobStatus.NOT_INITIALIZED) && 
/* 653 */               (SearchOnceGraphical.this.clouds[i].getMonitorStatus() != CloudMonitor.JobStatus.POOLED)) SearchOnceGraphical.this.clouds[i].stop();
/*     */           }
/* 655 */           boolean allIsClosed = false;
/* 656 */           while (!allIsClosed) {
/* 657 */             allIsClosed = true;
/* 658 */             for (int i = 0; i < SearchOnceGraphical.this.nJobs; i++) {
/* 659 */               if (!SearchOnceGraphical.this.clouds[i].isTerminated()) {
/* 660 */                 allIsClosed = false;
/*     */                 try {
/* 662 */                   Thread.sleep(500L);
/*     */                 } catch (Exception e) {
/* 664 */                   e.printStackTrace();
/*     */                 }
/* 666 */                 SearchOnceGraphical.this.clouds[i].stop();
/*     */               }
/*     */             }
/* 669 */             if (!allIsClosed) {
/*     */               try {
/* 671 */                 Thread.sleep(1000L);
/*     */               } catch (Exception e) {
/* 673 */                 e.printStackTrace();
/*     */               }
/*     */             }
/*     */           }
/* 677 */           SearchOnceGraphical.this.showText("Ending CLOUD application  : " + (SearchOnceGraphical.this.cloud.endApplication() ? "OK" : "FAILED"));
/* 678 */           SearchOnceGraphical.this.stopCloud = false;
/*     */         }
/*     */       }).start();
/*     */     }
/*     */   }
/*     */ 
/*     */   void closeButton_actionPerformed(ActionEvent e) {
/* 685 */     if ((this.executor != null) && (!this.executor.isTerminated())) {
/* 686 */       this.stopHeuristic = true;
/* 687 */       this.replicatesAvailable = this.replicatesTotal;
/* 688 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 690 */           SearchOnceGraphical.this.closeButton.setText("PLEASE WAIT ...");
/* 691 */           SearchOnceGraphical.this.closeButton.setEnabled(false);
/*     */         }
/*     */       });
/* 694 */       if (this.parameters.useGrid)
/* 695 */         stopGrid();
/* 696 */       else if (this.parameters.useCloud)
/* 697 */         stopCloud();
/*     */       else
/* 699 */         for (int i = 0; i < this.nJobs; i++)
/* 700 */           this.monitors[i].stop();
/*     */     }
/*     */     else
/*     */     {
/* 704 */       dispose();
/*     */       try
/*     */       {
/*     */         int optionType;
/*     */         String[] options;
/*     */         int optionType;
/* 723 */         if (this.consensusTree != null) {
/* 724 */           String[] options = new String[3];
/* 725 */           options[0] = "Consensus tree only";
/* 726 */           options[1] = "All best trees";
/* 727 */           options[2] = "None";
/* 728 */           optionType = 1;
/*     */         } else {
/* 730 */           options = new String[2];
/* 731 */           options[0] = (this.allSolutionTrees.size() == 1 ? "Best tree" : "All best trees");
/* 732 */           options[1] = "None";
/* 733 */           optionType = 0;
/*     */         }
/* 735 */         int op = JOptionPane.showOptionDialog(null, 
/* 736 */           "All your trees have been saved to " + this.dirPath + 
/* 737 */           "\nDo you want to send trees to the MetaPIGA 'Tree Viewer' for further manipulations ?" + 
/* 738 */           "\n(re-rooting, exporting, changing substitution model, optimizing model parameters, reconstructing ancestral states, etc)", 
/* 739 */           "Send result trees to the Tree Viewer", optionType, 3, Tools.getScaledIcon(MainFrame.imageTreeViewer, 64), options, null);
/* 740 */         if (optionType == 1)
/* 741 */           switch (op) {
/*     */           case 0:
/* 743 */             MetaPIGA.treeViewer.addTree(this.consensusTree, this.parameters);
/* 744 */             MetaPIGA.treeViewer.setSelectedTrees(this.consensusTree);
/* 745 */             MetaPIGA.treeViewer.setVisible(true);
/* 746 */             break;
/*     */           case 1:
/* 748 */             MetaPIGA.treeViewer.addTree(this.consensusTree, this.parameters);
/* 749 */             for (Tree t : this.allSolutionTrees) {
/* 750 */               MetaPIGA.treeViewer.addTree(t, this.parameters);
/*     */             }
/* 752 */             MetaPIGA.treeViewer.setSelectedTrees(this.consensusTree, this.allSolutionTrees);
/* 753 */             MetaPIGA.treeViewer.setVisible(true);
/* 754 */             break;
/*     */           case 2:
/*     */           default:
/* 757 */             break;
/*     */           }
/* 759 */         else switch (op) {
/*     */           case 0:
/* 761 */             for (Tree t : this.allSolutionTrees) {
/* 762 */               MetaPIGA.treeViewer.addTree(t, this.parameters);
/*     */             }
/* 764 */             MetaPIGA.treeViewer.setSelectedTrees(this.allSolutionTrees);
/* 765 */             MetaPIGA.treeViewer.setVisible(true);
/* 766 */             break;
/*     */           case 1:
/*     */           }
/*     */ 
/*     */ 
/* 771 */         System.gc();
/*     */       }
/*     */       catch (Exception ex) {
/* 774 */         System.out.println("Cannot display result tree(s) : " + ex.getMessage());
/* 775 */         ex.printStackTrace();
/*     */       }
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.SearchOnceGraphical
 * JD-Core Version:    0.6.2
 */