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
/*     */ import java.util.Date;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ import java.util.concurrent.CountDownLatch;
/*     */ import java.util.concurrent.ExecutorService;
/*     */ import java.util.concurrent.Executors;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import java.util.concurrent.atomic.AtomicInteger;
/*     */ import javax.swing.DefaultListModel;
/*     */
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JList;
/*     */ import javax.swing.JOptionPane;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JProgressBar;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JSplitPane;
/*     */ import javax.swing.JTextArea;
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
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.trees.TreeViewer;
/*     */ import metapiga.utilities.Tools;
/*     */ import org.biojavax.bio.phylo.io.nexus.CharactersBlock;
/*     */ import org.biojavax.bio.phylo.io.nexus.MetapigaBlock;
/*     */ 
/*     */ public class SearchBatchGraphical extends JFrame
/*     */   implements Runnable
/*     */ {
/*     */   private ExecutorService executor;
/*     */   private int nJobs;
/*     */   int currentStep;
/*     */   private AtomicInteger replicatesDone;
/*     */   private AtomicInteger replicatesAvailable;
/*     */   private AtomicInteger replicatesTotal;
/*     */   private AtomicInteger countMRE;
/*     */   private DefaultListModel batch;
/*     */   private Parameters currentParameters;
/*  76 */   private List<Tree> allSolutionTrees = new Vector();
/*     */   private String runDirectory;
/*     */   public String dirPath;
/*     */   private String runLabel;
/*  80 */   private boolean stopAll = false;
/*  81 */   private boolean stopHeuristic = false;
/*     */   private SearchBatchGraphicalMonitor[] monitors;
/*     */   private SearchGraphicalGrid[] grids;
/*     */   private CountDownLatch latch;
/*  85 */   private ConsensusMRE consensusMRE = new ConsensusMRE();
/*  86 */   private GridConnection grid = null;
/*  87 */   private boolean stopGrid = false;
/*     */   private SearchGraphicalCloud[] clouds;
/*  90 */   private CloudConnection cloud = null;
/*  91 */   private boolean stopCloud = false;
/*     */   final JList batchList;
/*  94 */   final JButton stopSelectedButton = new JButton();
/*  95 */   final JButton stopAllButton = new JButton();
/*  96 */   final JProgressBar batchProgress = new JProgressBar();
/*  97 */   final JProgressBar replicatesProgressBar = new JProgressBar();
/*  98 */   final JLabel mreLabel = new JLabel();
/*  99 */   final JTextArea runLogTextArea = new JTextArea();
/* 100 */   final JTextArea batchLogTextArea = new JTextArea();
/* 101 */   final JPanel runInfoPanel = new JPanel();
/* 102 */   private final ScrollableFlowPanel gridsPanel = new ScrollableFlowPanel();
/*     */   public GridStatGraphical gridStatGraphical;
/* 104 */   private final JProgressBar memoryBar = new JProgressBar();
/*     */   public CloudStatGraphical cloudStatGraphical;
/*     */ 
/*     */   public SearchBatchGraphical(DefaultListModel parameters)
/*     */   {
/* 108 */     super("Batch : ");
/* 109 */     setIconImage(Tools.getScaledIcon(MainFrame.imageStartBatch, 32).getImage());
/* 110 */     this.batch = parameters;
/* 111 */     this.batchList = new JList(this.batch);
/*     */     try {
/* 113 */       jbInit();
/* 114 */       pack();
/*     */     }
/*     */     catch (Exception ex) {
/* 117 */       ex.printStackTrace();
/*     */     }
/* 119 */     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/* 120 */     Dimension windowSize = getSize();
/* 121 */     setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), Math.max(0, (screenSize.height - windowSize.height) / 2));
/* 122 */     setExtendedState(6);
/* 123 */     setDefaultCloseOperation(0);
/*     */   }
/*     */ 
/*     */   private void jbInit() {
/* 127 */     JSplitPane mainSplitPane = new JSplitPane();
/* 128 */     getContentPane().add(mainSplitPane, "Center");
/*     */ 
/* 130 */     JPanel batchListPanel = new JPanel();
/* 131 */     batchListPanel.setLayout(new BorderLayout());
/* 132 */     mainSplitPane.setLeftComponent(batchListPanel);
/*     */ 
/* 134 */     JScrollPane batchListScrollPane = new JScrollPane();
/* 135 */     batchListPanel.add(batchListScrollPane);
/*     */ 
/* 137 */     this.batchList.setBackground(Color.black);
/* 138 */     this.batchList.setFont(new Font("Geneva", 0, 14));
/* 139 */     this.batchList.setForeground(Color.green);
/* 140 */     this.batchList.setSelectionMode(0);
/* 141 */     this.batchList.setEnabled(false);
/* 142 */     batchListScrollPane.setViewportView(this.batchList);
/*     */ 
/* 144 */     JPanel stopPanel = new JPanel();
/* 145 */     stopPanel.setLayout(new GridBagLayout());
/* 146 */     batchListPanel.add(stopPanel, "South");
/*     */ 
/* 148 */     this.stopSelectedButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent arg0) {
/* 150 */         if (!SearchBatchGraphical.this.executor.isTerminated()) {
/* 151 */           SwingUtilities.invokeLater(new Runnable() {
/*     */             public void run() {
/* 153 */               SearchBatchGraphical.this.stopSelectedButton.setText("PLEASE WAIT ...");
/* 154 */               SearchBatchGraphical.this.stopSelectedButton.setEnabled(false);
/*     */             }
/*     */           });
/* 157 */           SearchBatchGraphical.this.replicatesAvailable = SearchBatchGraphical.this.replicatesTotal;
/* 158 */           SearchBatchGraphical.this.stopHeuristic = true;
/* 159 */           for (int i = 0; i < SearchBatchGraphical.this.nJobs; i++)
/* 160 */             if (SearchBatchGraphical.this.currentParameters.useGrid) SearchBatchGraphical.this.grids[i].stop();
/* 161 */             else if (SearchBatchGraphical.this.currentParameters.useCloud) SearchBatchGraphical.this.clouds[i].stop(); else
/* 162 */               SearchBatchGraphical.this.monitors[i].stop();
/*     */         }
/*     */       }
/*     */     });
/* 168 */     this.memoryBar.setPreferredSize(new Dimension(100, 18));
/* 169 */     this.memoryBar.setMaximum((int)(Runtime.getRuntime().maxMemory() / 1024L / 1024L));
/* 170 */     this.memoryBar.setValue(0);
/* 171 */     this.memoryBar.setString("used memory");
/* 172 */     this.memoryBar.setStringPainted(true);
/* 173 */     this.memoryBar.setToolTipText("Memory currently by MetaPIGA with bar length equal to allowed memory");
/* 174 */     GridBagConstraints gbc_memoryBar = new GridBagConstraints();
/* 175 */     gbc_memoryBar.insets = new Insets(5, 5, 0, 5);
/* 176 */     gbc_memoryBar.gridx = 0;
/* 177 */     gbc_memoryBar.gridy = 0;
/* 178 */     stopPanel.add(this.memoryBar, gbc_memoryBar);
/* 179 */     this.stopSelectedButton.setText("Stop current");
/* 180 */     GridBagConstraints gridBagConstraints = new GridBagConstraints();
/* 181 */     gridBagConstraints.insets = new Insets(5, 5, 0, 5);
/* 182 */     gridBagConstraints.gridy = 0;
/* 183 */     gridBagConstraints.gridx = 1;
/* 184 */     stopPanel.add(this.stopSelectedButton, gridBagConstraints);
/*     */ 
/* 186 */     this.stopAllButton.setText("Stop all");
/* 187 */     this.stopAllButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 189 */         if ((SearchBatchGraphical.this.executor != null) && (!SearchBatchGraphical.this.executor.isTerminated())) {
/* 190 */           SwingUtilities.invokeLater(new Runnable() {
/*     */             public void run() {
/* 192 */               SearchBatchGraphical.this.stopSelectedButton.setText("PLEASE WAIT ...");
/* 193 */               SearchBatchGraphical.this.stopSelectedButton.setEnabled(false);
/* 194 */               SearchBatchGraphical.this.stopAllButton.setText("PLEASE WAIT ...");
/* 195 */               SearchBatchGraphical.this.stopAllButton.setEnabled(false);
/*     */             }
/*     */           });
/* 197 */           SearchBatchGraphical.this.stopHeuristic = true;
/* 198 */           SearchBatchGraphical.this.stopAll = true;
/* 199 */           SearchBatchGraphical.this.replicatesAvailable = SearchBatchGraphical.this.replicatesTotal;
/* 200 */           if (SearchBatchGraphical.this.currentParameters.useGrid)
/* 201 */             SearchBatchGraphical.this.stopGrid();
/*     */           else
/* 203 */             for (int i = 0; i < SearchBatchGraphical.this.nJobs; i++)
/* 204 */               SearchBatchGraphical.this.monitors[i].stop();
/*     */         }
/*     */         else
/*     */         {
/* 208 */           SearchBatchGraphical.this.dispose();
/* 209 */           System.gc();
/*     */         }
/*     */       }
/*     */     });
/* 213 */     GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
/* 214 */     gridBagConstraints_1.insets = new Insets(5, 5, 0, 0);
/* 215 */     gridBagConstraints_1.gridy = 0;
/* 216 */     gridBagConstraints_1.gridx = 2;
/* 217 */     stopPanel.add(this.stopAllButton, gridBagConstraints_1);
/*     */ 
/* 219 */     JPanel progressPanel = new JPanel();
/* 220 */     progressPanel.setLayout(new BorderLayout());
/* 221 */     mainSplitPane.setRightComponent(progressPanel);
/*     */ 
/* 223 */     JSplitPane progressSplitPane = new JSplitPane();
/* 224 */     progressSplitPane.setResizeWeight(0.3D);
/* 225 */     progressSplitPane.setOrientation(0);
/* 226 */     progressPanel.add(progressSplitPane);
/*     */ 
/* 228 */     JPanel runPanel = new JPanel();
/* 229 */     runPanel.setLayout(new BorderLayout());
/* 230 */     progressSplitPane.setRightComponent(runPanel);
/*     */ 
/* 232 */     JScrollPane runLogScrollPane = new JScrollPane();
/* 233 */     runPanel.add(runLogScrollPane, "Center");
/*     */ 
/* 235 */     this.runLogTextArea.setBackground(Color.black);
/* 236 */     this.runLogTextArea.setForeground(Color.green);
/* 237 */     this.runLogTextArea.setEditable(false);
/* 238 */     this.runLogTextArea.setText("");
/* 239 */     this.runLogTextArea.setLineWrap(true);
/* 240 */     this.runLogTextArea.setWrapStyleWord(true);
/* 241 */     this.runLogTextArea.setFont(new Font("Geneva", 0, 12));
/* 242 */     runLogScrollPane.setViewportView(this.runLogTextArea);
/*     */ 
/* 244 */     this.runInfoPanel.setLayout(new GridBagLayout());
/* 245 */     runPanel.add(this.runInfoPanel, "South");
/*     */ 
/* 248 */     JPanel batchPanel = new JPanel();
/* 249 */     batchPanel.setLayout(new BorderLayout());
/* 250 */     progressSplitPane.setLeftComponent(batchPanel);
/*     */ 
/* 252 */     JScrollPane batchLogScrollPane = new JScrollPane();
/* 253 */     batchPanel.add(batchLogScrollPane);
/*     */ 
/* 255 */     this.batchLogTextArea.setBackground(Color.black);
/* 256 */     this.batchLogTextArea.setForeground(Color.green);
/* 257 */     this.batchLogTextArea.setEditable(false);
/* 258 */     this.batchLogTextArea.setText("");
/* 259 */     this.batchLogTextArea.setLineWrap(true);
/* 260 */     this.batchLogTextArea.setWrapStyleWord(true);
/* 261 */     this.batchLogTextArea.setFont(new Font("Geneva", 0, 12));
/* 262 */     batchLogScrollPane.setViewportView(this.batchLogTextArea);
/*     */ 
/* 264 */     JPanel batchProgressPanel = new JPanel();
/* 265 */     GridBagLayout gridBagLayout_1 = new GridBagLayout();
/* 266 */     gridBagLayout_1.columnWidths = new int[2];
/* 267 */     batchProgressPanel.setLayout(gridBagLayout_1);
/* 268 */     batchPanel.add(batchProgressPanel, "South");
/*     */ 
/* 270 */     JLabel batchProgressLabel = new JLabel();
/* 271 */     batchProgressLabel.setText("Batch progress :");
/* 272 */     GridBagConstraints gridBagConstraints_7 = new GridBagConstraints();
/* 273 */     gridBagConstraints_7.insets = new Insets(5, 5, 5, 5);
/* 274 */     gridBagConstraints_7.gridy = 0;
/* 275 */     gridBagConstraints_7.gridx = 0;
/* 276 */     batchProgressPanel.add(batchProgressLabel, gridBagConstraints_7);
/*     */ 
/* 278 */     GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
/* 279 */     gridBagConstraints_2.weightx = 1.0D;
/* 280 */     gridBagConstraints_2.fill = 1;
/* 281 */     gridBagConstraints_2.insets = new Insets(5, 5, 5, 25);
/* 282 */     gridBagConstraints_2.gridy = 0;
/* 283 */     gridBagConstraints_2.gridx = 1;
/* 284 */     batchProgressPanel.add(this.batchProgress, gridBagConstraints_2);
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/* 289 */     setVisible(true);
/* 290 */     new Thread(new Runnable() {
/*     */       public void run() {
/* 292 */         while (!SearchBatchGraphical.this.stopAll)
/*     */           try {
/* 294 */             final int usedMem = (int)((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024L);
/* 295 */             SwingUtilities.invokeLater(new Runnable() {
/*     */               public void run() {
/* 297 */                 SearchBatchGraphical.this.memoryBar.setValue(usedMem);
/* 298 */                 SearchBatchGraphical.this.memoryBar.setString(usedMem + " Mb");
/* 299 */                 SearchBatchGraphical.this.memoryBar.setStringPainted(true);
/*     */               }
/*     */             });
/* 302 */             Thread.sleep(1000L);
/*     */           } catch (InterruptedException ex) {
/* 304 */             ex.printStackTrace();
/*     */           }
/*     */       }
/*     */     }
/*     */     , "Used memory updater").start();
/* 309 */     this.batchProgress.setMinimum(0);
/* 310 */     this.batchProgress.setMaximum(this.batch.getSize());
/* 311 */     for (int i = 0; (i < this.batch.getSize()) && (!this.stopAll); i++) {
/* 312 */       this.stopHeuristic = false;
/* 313 */       this.allSolutionTrees.clear();
/* 314 */       this.stopSelectedButton.setText("Stop current");
/* 315 */       this.stopSelectedButton.setEnabled(true);
/* 316 */       this.currentParameters = ((Parameters)this.batch.get(i));
/* 317 */       DateFormat df = new SimpleDateFormat("yyyy-MM-dd - HH_mm_ss");
/* 318 */       this.runDirectory = this.currentParameters.outputDir;
/* 319 */       File dir = new File(this.runDirectory);
/* 320 */       if (!dir.exists()) {
/* 321 */         dir.mkdirs();
/*     */       }
/* 323 */       this.runLabel = (this.currentParameters.label + " - " + df.format(Long.valueOf(System.currentTimeMillis())));
/* 324 */       this.dirPath = (this.runDirectory + "/" + this.runLabel);
/* 325 */       dir = new File(this.dirPath);
/* 326 */       if (!dir.exists()) dir.mkdir();
/* 327 */       this.batchProgress.setValue(i + 1);
/* 328 */       this.batchProgress.setString(this.runLabel + " (" + (i + 1) + "/" + this.batch.getSize() + ")");
/* 329 */       this.batchProgress.setStringPainted(true);
/* 330 */       this.batchList.setSelectedIndex(i);
/* 331 */       switch ($SWITCH_TABLE$metapiga$parameters$Parameters$ReplicatesStopCondition()[this.currentParameters.replicatesStopCondition.ordinal()]) {
/*     */       case 1:
/* 333 */         this.replicatesTotal = new AtomicInteger(this.currentParameters.replicatesNumber);
/* 334 */         break;
/*     */       case 2:
/* 336 */         this.replicatesTotal = new AtomicInteger(this.currentParameters.replicatesMaximum);
/*     */       }
/*     */ 
/* 339 */       this.replicatesAvailable = new AtomicInteger();
/* 340 */       this.replicatesDone = new AtomicInteger();
/* 341 */       this.countMRE = new AtomicInteger();
/* 342 */       this.runInfoPanel.removeAll();
/* 343 */       GridBagConstraints gridBagConstraints_8 = new GridBagConstraints();
/* 344 */       gridBagConstraints_8.fill = 1;
/* 345 */       gridBagConstraints_8.gridx = 0;
/* 346 */       gridBagConstraints_8.gridy = 0;
/* 347 */       gridBagConstraints_8.weightx = 0.0D;
/* 348 */       gridBagConstraints_8.insets = new Insets(5, 15, 5, 5);
/* 349 */       this.runInfoPanel.add(this.mreLabel, gridBagConstraints_8);
/* 350 */       GridBagConstraints gridBagConstraints_88 = new GridBagConstraints();
/* 351 */       gridBagConstraints_88.fill = 1;
/* 352 */       gridBagConstraints_88.gridx = 1;
/* 353 */       gridBagConstraints_88.gridy = 0;
/* 354 */       gridBagConstraints_88.weightx = 1.0D;
/* 355 */       gridBagConstraints_88.insets = new Insets(5, 5, 5, 25);
/* 356 */       this.runInfoPanel.add(this.replicatesProgressBar, gridBagConstraints_88);
/* 357 */       if (this.currentParameters.useGrid) {
/* 358 */         this.nJobs = this.replicatesTotal.get();
/* 359 */         this.latch = new CountDownLatch(this.nJobs);
/* 360 */         this.grids = new SearchGraphicalGrid[this.nJobs];
/* 361 */         for (int j = 0; j < this.nJobs; j++) {
/* 362 */           this.grids[j] = new SearchGraphicalGrid(this, this.currentParameters, j + 1);
/*     */         }
/* 364 */         GridBagConstraints gridBagConstraints_99 = new GridBagConstraints();
/* 365 */         gridBagConstraints_99.fill = 1;
/* 366 */         gridBagConstraints_99.weightx = 1.0D;
/* 367 */         gridBagConstraints_99.weighty = 0.0D;
/* 368 */         gridBagConstraints_99.insets = new Insets(0, 5, 0, 5);
/* 369 */         gridBagConstraints_99.gridy = 1;
/* 370 */         gridBagConstraints_99.gridx = 0;
/* 371 */         gridBagConstraints_99.gridwidth = 2;
/* 372 */         this.gridStatGraphical = new GridStatGraphical(this.grids.length);
/* 373 */         this.gridStatGraphical.setPreferredSize(new Dimension(500, 100));
/* 374 */         this.runInfoPanel.add(this.gridStatGraphical, gridBagConstraints_99);
/* 375 */         GridBagConstraints gridBagConstraints_9 = new GridBagConstraints();
/* 376 */         gridBagConstraints_9.fill = 1;
/* 377 */         gridBagConstraints_9.weightx = 1.0D;
/* 378 */         gridBagConstraints_9.weighty = 1.0D;
/* 379 */         gridBagConstraints_9.insets = new Insets(0, 0, 0, 0);
/* 380 */         gridBagConstraints_9.gridy = 2;
/* 381 */         gridBagConstraints_9.gridx = 0;
/* 382 */         gridBagConstraints_9.gridwidth = 2;
/* 383 */         JScrollPane scrollpane = new JScrollPane();
/* 384 */         scrollpane.setPreferredSize(new Dimension(500, 100));
/* 385 */         scrollpane.setHorizontalScrollBarPolicy(31);
/* 386 */         this.runInfoPanel.add(scrollpane, gridBagConstraints_9);
/* 387 */         this.gridsPanel.setLayout(new FlowLayout(3));
/* 388 */         for (int j = 0; j < this.nJobs; j++) {
/* 389 */           this.gridsPanel.add(this.grids[j]);
/*     */         }
/* 391 */         scrollpane.setViewportView(this.gridsPanel);
/* 392 */       } else if (this.currentParameters.useCloud) {
/* 393 */         this.nJobs = this.replicatesTotal.get();
/* 394 */         this.latch = new CountDownLatch(this.nJobs);
/* 395 */         this.clouds = new SearchGraphicalCloud[this.nJobs];
/* 396 */         for (int j = 0; j < this.nJobs; j++) {
/* 397 */           this.clouds[j] = new SearchGraphicalCloud(this, this.currentParameters, j + 1);
/*     */         }
/* 399 */         GridBagConstraints gridBagConstraints_99 = new GridBagConstraints();
/* 400 */         gridBagConstraints_99.fill = 1;
/* 401 */         gridBagConstraints_99.weightx = 1.0D;
/* 402 */         gridBagConstraints_99.weighty = 0.0D;
/* 403 */         gridBagConstraints_99.insets = new Insets(0, 5, 0, 5);
/* 404 */         gridBagConstraints_99.gridy = 1;
/* 405 */         gridBagConstraints_99.gridx = 0;
/* 406 */         gridBagConstraints_99.gridwidth = 2;
/* 407 */         this.cloudStatGraphical = new CloudStatGraphical(this.clouds.length);
/* 408 */         this.cloudStatGraphical.setPreferredSize(new Dimension(500, 100));
/* 409 */         this.runInfoPanel.add(this.cloudStatGraphical, gridBagConstraints_99);
/* 410 */         GridBagConstraints gridBagConstraints_9 = new GridBagConstraints();
/* 411 */         gridBagConstraints_9.fill = 1;
/* 412 */         gridBagConstraints_9.weightx = 1.0D;
/* 413 */         gridBagConstraints_9.weighty = 1.0D;
/* 414 */         gridBagConstraints_9.insets = new Insets(0, 0, 0, 0);
/* 415 */         gridBagConstraints_9.gridy = 2;
/* 416 */         gridBagConstraints_9.gridx = 0;
/* 417 */         gridBagConstraints_9.gridwidth = 2;
/* 418 */         JScrollPane scrollpane = new JScrollPane();
/* 419 */         scrollpane.setPreferredSize(new Dimension(500, 100));
/* 420 */         scrollpane.setHorizontalScrollBarPolicy(31);
/* 421 */         this.runInfoPanel.add(scrollpane, gridBagConstraints_9);
/* 422 */         this.gridsPanel.setLayout(new FlowLayout(3));
/* 423 */         for (int j = 0; j < this.nJobs; j++) {
/* 424 */           this.gridsPanel.add(this.clouds[j]);
/*     */         }
/* 426 */         scrollpane.setViewportView(this.gridsPanel);
/*     */       } else {
/* 428 */         this.latch = null;
/* 429 */         this.nJobs = this.currentParameters.replicatesParallel;
/* 430 */         this.monitors = new SearchBatchGraphicalMonitor[this.nJobs];
/* 431 */         for (int j = 0; j < this.nJobs; j++) {
/* 432 */           this.monitors[j] = new SearchBatchGraphicalMonitor(this, this.currentParameters, this.dirPath);
/*     */ 
/* 434 */           GridBagConstraints gridBagConstraints_9 = new GridBagConstraints();
/* 435 */           gridBagConstraints_9.fill = 1;
/* 436 */           gridBagConstraints_9.weightx = 1.0D;
/* 437 */           gridBagConstraints_9.insets = new Insets(0, 5, 0, 25);
/* 438 */           gridBagConstraints_9.gridy = (j + 1);
/* 439 */           gridBagConstraints_9.gridx = 0;
/* 440 */           gridBagConstraints_9.gridwidth = 2;
/* 441 */           this.runInfoPanel.add(this.monitors[j], gridBagConstraints_9);
/*     */         }
/*     */       }
/*     */       try {
/* 445 */         long startTime = System.currentTimeMillis();
/* 446 */         showBatchText(this.runLabel + " started at " + new Date(startTime).toString() + "\n");
/* 447 */         showText("Running " + this.runLabel + "\n" + this.currentParameters.printParameters());
/* 448 */         if (this.replicatesTotal.get() <= 1) {
/* 449 */           this.replicatesProgressBar.setVisible(false);
/* 450 */           this.mreLabel.setVisible(false);
/*     */         } else {
/* 452 */           SwingUtilities.invokeLater(new Runnable() {
/*     */             public void run() {
/* 454 */               SearchBatchGraphical.this.replicatesProgressBar.setMaximum(SearchBatchGraphical.this.replicatesTotal.get());
/* 455 */               SearchBatchGraphical.this.replicatesProgressBar.setValue(SearchBatchGraphical.this.replicatesDone.get());
/* 456 */               SearchBatchGraphical.this.replicatesProgressBar.setString("Replicates done " + SearchBatchGraphical.this.replicatesDone.get() + "/" + SearchBatchGraphical.this.replicatesTotal);
/* 457 */               SearchBatchGraphical.this.replicatesProgressBar.setStringPainted(true);
/* 458 */               SearchBatchGraphical.this.setTitle("Batch : " + SearchBatchGraphical.this.runLabel + " : Replicates done " + SearchBatchGraphical.this.replicatesDone.get() + "/" + SearchBatchGraphical.this.replicatesTotal);
/*     */             }
/*     */           });
/*     */         }
/* 462 */         this.grid = (this.currentParameters.useGrid ? new GridConnection(this, this.currentParameters, this.dirPath) : null);
/* 463 */         this.cloud = (this.currentParameters.useCloud ? new CloudConnection(this, this.currentParameters, this.dirPath) : null);
/* 464 */         this.executor = Executors.newFixedThreadPool(this.currentParameters.useGrid ? Math.min(this.nJobs, 1000) : this.nJobs);
/* 465 */         for (int k = 0; k < this.nJobs; k++) {
/* 466 */           if (this.currentParameters.useGrid) {
/* 467 */             if (!this.stopHeuristic) {
/* 468 */               this.grids[k].initialize(this.grid, this.latch);
/* 469 */               this.executor.execute(this.grids[k]);
/* 470 */               while (this.grids[k].getMonitorStatus() == GridMonitor.JobStatus.POOLED)
/*     */                 try {
/* 472 */                   Thread.sleep(200L);
/*     */                 } catch (InterruptedException ex) {
/* 474 */                   ex.printStackTrace();
/*     */                 }
/*     */             }
/*     */             else {
/* 478 */               this.latch.countDown();
/*     */             }
/* 480 */           } else if (this.currentParameters.useCloud) {
/* 481 */             if (!this.stopHeuristic) {
/* 482 */               this.clouds[k].initialize(this.cloud, this.latch);
/* 483 */               this.executor.execute(this.clouds[k]);
/* 484 */               while (this.clouds[k].getMonitorStatus() == CloudMonitor.JobStatus.POOLED)
/*     */                 try {
/* 486 */                   Thread.sleep(200L);
/*     */                 } catch (InterruptedException ex) {
/* 488 */                   ex.printStackTrace();
/*     */                 }
/*     */             }
/*     */             else {
/* 492 */               this.latch.countDown();
/*     */             }
/*     */           }
/* 495 */           else this.executor.execute(this.monitors[k]);
/*     */         }
/*     */ 
/* 498 */         if (this.currentParameters.useGrid) this.latch.await();
/* 499 */         this.executor.shutdown();
/* 500 */         this.executor.awaitTermination(1000L, TimeUnit.DAYS);
/* 501 */         Thread.sleep(500L);
/*     */ 
/* 503 */         if (this.currentParameters.useGrid) stopGrid();
/* 504 */         end(startTime);
/*     */       } catch (Exception e) {
/* 506 */         endFromException(e);
/*     */       }
/* 508 */       showBatchText("");
/*     */     }
/* 510 */     this.stopSelectedButton.setEnabled(false);
/* 511 */     this.stopSelectedButton.setText("Finished");
/* 512 */     if (this.stopAll)
/* 513 */       showBatchText("Batch stopped by user.");
/*     */     else {
/* 515 */       showBatchText("Batch finished !");
/*     */     }
/* 517 */     setDefaultCloseOperation(2);
/* 518 */     this.stopAllButton.setText("CLOSE");
/* 519 */     this.stopAllButton.setEnabled(true);
/* 520 */     setState(0);
/* 521 */     MetaPIGA.treeViewer.setVisible(true);
/*     */   }
/*     */ 
/*     */   private void end(long startTime) {
/* 525 */     setTitle("Batch : " + this.runLabel + " : job finished");
/* 526 */     showText("");
/* 527 */     if (this.replicatesTotal.get() > 1) showText("All replicates done in " + Tools.doubletoString((System.currentTimeMillis() - startTime) / 60000.0D, 2) + " minutes");
/*     */ 
/* 529 */     Tree consensusTree = null;
/*     */     Tree optimizedConsensusTree;
/* 530 */     if (this.replicatesTotal.get() > 1) {
/*     */       try {
/* 532 */         Consensus consensus = new Consensus(this.allSolutionTrees, this.currentParameters.dataset);
/* 533 */         consensusTree = consensus.getConsensusTree(this.currentParameters);
/* 534 */         if (this.currentParameters.optimization == Parameters.Optimization.CONSENSUSTREE) {
/* 535 */           showText("\nOptimizing final consensus tree");
/* 536 */           optimizedConsensusTree = this.currentParameters.getOptimizer(consensusTree).getOptimizedTreeWithProgress(null, "Optimizing final consensus tree");
/* 537 */           consensusTree.cloneWithConsensus(optimizedConsensusTree);
/*     */         }
/*     */       } catch (Exception ex) {
/* 540 */         JOptionPane.showMessageDialog(null, Tools.getErrorPanel("Cannot display result tree(s)", ex), "Consensus tree Error", 0);
/* 541 */         System.out.println("Cannot build consensus tree : " + ex.getMessage());
/* 542 */         ex.printStackTrace();
/*     */       }
/*     */     }
/*     */ 
/* 546 */     if (consensusTree != null)
/* 547 */       consensusTree.setName(this.runLabel + " - " + consensusTree.getName() + " - " + this.replicatesDone + "_replicates");
/* 548 */     for (Tree t : this.allSolutionTrees) {
/* 549 */       t.setName(this.runLabel + " - " + t.getName());
/*     */     }
/* 551 */     if ((this.stopHeuristic) && (this.replicatesTotal.get() > 1)) {
/* 552 */       if (this.currentParameters.useGrid) showText("\nUnfinished replicates have been discarded"); else
/* 553 */         showText("\nLast replicate was interrupted and has been discarded");
/*     */     }
/* 555 */     showText("\n" + (this.stopHeuristic ? "JOB STOPPED BY USER" : "JOB DONE") + "\n");
/*     */ 
/* 557 */     if (consensusTree != null) MetaPIGA.treeViewer.addTree(consensusTree, this.currentParameters);
/* 558 */     for (Tree t : this.allSolutionTrees) {
/* 559 */       MetaPIGA.treeViewer.addTree(t, this.currentParameters);
/*     */     }
/*     */ 
/* 562 */     File output = new File(this.dirPath + "/" + "Results.nex");
/*     */     try {
/* 564 */       FileWriter fw = new FileWriter(output);
/* 565 */       BufferedWriter bw = new BufferedWriter(fw);
/* 566 */       bw.write("#NEXUS");
/* 567 */       bw.newLine();
/* 568 */       bw.newLine();
/* 569 */       this.currentParameters.getMetapigaBlock().writeObject(bw);
/* 570 */       bw.newLine();
/* 571 */       this.currentParameters.charactersBlock.writeObject(bw);
/* 572 */       bw.newLine();
/* 573 */       if (this.currentParameters.startingTreeGeneration == Parameters.StartingTreeGeneration.GIVEN) {
/* 574 */         this.currentParameters.writeTreeBlock(bw);
/* 575 */         bw.newLine();
/*     */       }
/* 577 */       bw.write("Begin trees;  [Result trees]");
/* 578 */       bw.newLine();
/* 579 */       if (consensusTree != null) {
/* 580 */         bw.write(consensusTree.toNewickLineWithML(consensusTree.getName(), false, true));
/* 581 */         bw.newLine();
/*     */       }
/* 583 */       for (Iterator localIterator = this.allSolutionTrees.iterator(); localIterator.hasNext(); ) { t = (Tree)localIterator.next();
/* 584 */         bw.write(t.toNewickLineWithML(t.getName(), false, true));
/* 585 */         bw.newLine();
/*     */       }
/* 587 */       bw.write("End;");
/* 588 */       bw.newLine();
/* 589 */       bw.close();
/* 590 */       fw.close();
/*     */     } catch (Exception e) {
/* 592 */       e.printStackTrace();
/* 593 */       showText("\n Error when writing results file");
/* 594 */       showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/*     */       StackTraceElement[] arrayOfStackTraceElement;
/* 595 */       Tree localTree1 = (arrayOfStackTraceElement = e.getStackTrace()).length; for (Tree t = 0; t < localTree1; t++) { StackTraceElement el = arrayOfStackTraceElement[t];
/* 596 */         showText("\tat " + el.toString()); }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addSolutionTree(List<Tree> trees)
/*     */   {
/* 602 */     if ((!this.stopHeuristic) || (this.replicatesTotal.get() <= 1)) {
/* 603 */       synchronized (this.allSolutionTrees) {
/* 604 */         this.allSolutionTrees.addAll(trees);
/*     */       }
/* 606 */       showReplicate();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void endFromException(Exception e) {
/* 611 */     e.printStackTrace();
/* 612 */     showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 613 */     for (StackTraceElement el : e.getStackTrace()) {
/* 614 */       showText("\tat " + el.toString());
/*     */     }
/* 616 */     showBatchText(this.runLabel + " stops after an exception : " + e.getMessage());
/*     */   }
/*     */ 
/*     */   public int getNextReplicate() {
/* 620 */     if (this.replicatesAvailable.get() < this.replicatesTotal.get()) {
/* 621 */       return this.replicatesAvailable.incrementAndGet();
/*     */     }
/* 623 */     return -1;
/*     */   }
/*     */ 
/*     */   public void showReplicate() {
/* 627 */     if (this.replicatesDone.incrementAndGet() > 1) {
/*     */       try
/*     */       {
/*     */         Consensus consensus;
/* 630 */         synchronized (this.allSolutionTrees) {
/* 631 */           consensus = new Consensus(this.allSolutionTrees, this.currentParameters.dataset);
/*     */         }
/* 633 */         synchronized (this.consensusMRE)
/*     */         {
/*     */           Consensus consensus;
/* 634 */           Tree consensusTree = consensus.getConsensusTree(this.currentParameters);
/* 635 */           consensusTree.setName("Consensus_tree_" + this.replicatesDone + "_replicates");
/* 636 */           this.consensusMRE.addConsensus(consensusTree, this.currentParameters, false);
/* 637 */           double mre = this.consensusMRE.meanRelativeError();
/* 638 */           consensusTree.setName("Consensus_tree_" + this.replicatesDone + "_replicates" + " [MRE: " + Tools.doubleToPercent(mre, 2) + "]");
/* 639 */           this.mreLabel.setText("Mean Relative Error of consensus tree: " + Tools.doubleToPercent(mre, 2));
/* 640 */           if ((this.replicatesDone.get() > this.currentParameters.replicatesMinimum) && (mre < this.currentParameters.replicatesMRE)) {
/* 641 */             this.countMRE.incrementAndGet();
/* 642 */             if ((this.currentParameters.replicatesStopCondition == Parameters.ReplicatesStopCondition.MRE) && (this.countMRE.get() >= this.currentParameters.replicatesInterval)) {
/* 643 */               this.replicatesAvailable = this.replicatesTotal;
/* 644 */               if (this.currentParameters.useGrid) {
/* 645 */                 stopGrid();
/*     */               }
/* 647 */               showText("MRE condition has been met, all remaining replicates have been cancelled.");
/*     */             }
/*     */           } else {
/* 650 */             this.countMRE.set(0);
/* 651 */             this.consensusMRE.addConsensus(consensusTree, this.currentParameters, true);
/*     */           }
/* 653 */           if (this.currentParameters.useGrid) this.grids[0].updateConsensusTree(consensusTree);
/* 654 */           else if (this.currentParameters.useCloud) this.clouds[0].updateConsensusTree(consensusTree); else
/* 655 */             this.monitors[0].updateConsensusTree(consensusTree);
/*     */         }
/*     */       } catch (Exception e) {
/* 658 */         e.printStackTrace();
/*     */       }
/*     */     }
/* 661 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 663 */         SearchBatchGraphical.this.replicatesProgressBar.setValue(SearchBatchGraphical.this.replicatesDone.get());
/* 664 */         SearchBatchGraphical.this.replicatesProgressBar.setString("Replicate " + SearchBatchGraphical.this.replicatesDone + "/" + SearchBatchGraphical.this.replicatesTotal);
/* 665 */         SearchBatchGraphical.this.replicatesProgressBar.setStringPainted(true);
/* 666 */         SearchBatchGraphical.this.setTitle("Batch : " + SearchBatchGraphical.this.runLabel + " : Replicate " + SearchBatchGraphical.this.replicatesDone + "/" + SearchBatchGraphical.this.replicatesTotal);
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void stopGrid() {
/* 672 */     if (!this.stopGrid) {
/* 673 */       this.stopGrid = true;
/* 674 */       new Thread(new Runnable() {
/*     */         public void run() {
/* 676 */           for (int i = 0; i < SearchBatchGraphical.this.nJobs; i++) {
/* 677 */             if (SearchBatchGraphical.this.grids[i].getMonitorStatus() == GridMonitor.JobStatus.NOT_INITIALIZED) SearchBatchGraphical.this.grids[i].stop();
/*     */           }
/* 679 */           for (int i = 0; i < SearchBatchGraphical.this.nJobs; i++) {
/* 680 */             if (SearchBatchGraphical.this.grids[i].getMonitorStatus() == GridMonitor.JobStatus.POOLED) SearchBatchGraphical.this.grids[i].stop();
/*     */           }
/* 682 */           for (int i = 0; i < SearchBatchGraphical.this.nJobs; i++) {
/* 683 */             if ((SearchBatchGraphical.this.grids[i].getMonitorStatus() != GridMonitor.JobStatus.NOT_INITIALIZED) && (SearchBatchGraphical.this.grids[i].getMonitorStatus() != GridMonitor.JobStatus.POOLED)) SearchBatchGraphical.this.grids[i].stop();
/*     */           }
/* 685 */           boolean allIsClosed = false;
/* 686 */           while (!allIsClosed) {
/* 687 */             allIsClosed = true;
/* 688 */             for (int i = 0; i < SearchBatchGraphical.this.nJobs; i++) {
/* 689 */               if (!SearchBatchGraphical.this.grids[i].isTerminated()) {
/* 690 */                 allIsClosed = false;
/*     */                 try {
/* 692 */                   Thread.sleep(500L);
/*     */                 } catch (Exception e) {
/* 694 */                   e.printStackTrace();
/*     */                 }
/* 696 */                 SearchBatchGraphical.this.grids[i].stop();
/*     */               }
/*     */             }
/* 699 */             if (!allIsClosed) {
/*     */               try {
/* 701 */                 Thread.sleep(1000L);
/*     */               } catch (Exception e) {
/* 703 */                 e.printStackTrace();
/*     */               }
/*     */             }
/*     */           }
/* 707 */           SearchBatchGraphical.this.showText("Ending GRID application  : " + (SearchBatchGraphical.this.grid.endApplication() ? "OK" : "FAILED"));
/* 708 */           SearchBatchGraphical.this.stopGrid = false;
/*     */         }
/*     */       }).start();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void stopCloud() {
/* 715 */     if (!this.stopCloud) {
/* 716 */       this.stopCloud = true;
/* 717 */       new Thread(new Runnable() {
/*     */         public void run() {
/* 719 */           for (int i = 0; i < SearchBatchGraphical.this.nJobs; i++) {
/* 720 */             if (SearchBatchGraphical.this.clouds[i].getMonitorStatus() == CloudMonitor.JobStatus.NOT_INITIALIZED) SearchBatchGraphical.this.clouds[i].stop();
/*     */           }
/* 722 */           for (int i = 0; i < SearchBatchGraphical.this.nJobs; i++) {
/* 723 */             if (SearchBatchGraphical.this.clouds[i].getMonitorStatus() == CloudMonitor.JobStatus.POOLED) SearchBatchGraphical.this.clouds[i].stop();
/*     */           }
/* 725 */           for (int i = 0; i < SearchBatchGraphical.this.nJobs; i++) {
/* 726 */             if ((SearchBatchGraphical.this.clouds[i].getMonitorStatus() != CloudMonitor.JobStatus.NOT_INITIALIZED) && 
/* 727 */               (SearchBatchGraphical.this.clouds[i].getMonitorStatus() != CloudMonitor.JobStatus.POOLED)) SearchBatchGraphical.this.clouds[i].stop();
/*     */           }
/* 729 */           boolean allIsClosed = false;
/* 730 */           while (!allIsClosed) {
/* 731 */             allIsClosed = true;
/* 732 */             for (int i = 0; i < SearchBatchGraphical.this.nJobs; i++) {
/* 733 */               if (!SearchBatchGraphical.this.clouds[i].isTerminated()) {
/* 734 */                 allIsClosed = false;
/*     */                 try {
/* 736 */                   Thread.sleep(500L);
/*     */                 } catch (Exception e) {
/* 738 */                   e.printStackTrace();
/*     */                 }
/* 740 */                 SearchBatchGraphical.this.clouds[i].stop();
/*     */               }
/*     */             }
/* 743 */             if (!allIsClosed) {
/*     */               try {
/* 745 */                 Thread.sleep(1000L);
/*     */               } catch (Exception e) {
/* 747 */                 e.printStackTrace();
/*     */               }
/*     */             }
/*     */           }
/* 751 */           SearchBatchGraphical.this.showText("Ending GRID application  : " + (SearchBatchGraphical.this.cloud.endApplication() ? "OK" : "FAILED"));
/* 752 */           SearchBatchGraphical.this.stopCloud = false;
/*     */         }
/*     */       }).start();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void showText(String text) {
/* 759 */     this.runLogTextArea.append(text + "\n");
/* 760 */     this.runLogTextArea.setCaretPosition(this.runLogTextArea.getText().length());
/*     */   }
/*     */ 
/*     */   public void showBatchText(String text) {
/* 764 */     this.batchLogTextArea.append(text + "\n");
/* 765 */     this.batchLogTextArea.setCaretPosition(this.batchLogTextArea.getText().length());
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.SearchBatchGraphical
 * JD-Core Version:    0.6.2
 */