/*      */ package metapiga.trees;
/*      */ 
/*      */ import java.awt.BorderLayout;
/*      */ import java.awt.Color;
/*      */ import java.awt.Container;
/*      */ import java.awt.Dimension;
/*      */ import java.awt.FileDialog;
/*      */ import java.awt.FlowLayout;
/*      */ import java.awt.Font;
/*      */ import java.awt.Frame;
/*      */ import java.awt.GridBagConstraints;
/*      */ import java.awt.GridBagLayout;
/*      */ import java.awt.Insets;
/*      */ import java.awt.Toolkit;
/*      */ import java.awt.event.ActionEvent;
/*      */ import java.awt.event.ActionListener;
/*      */ import java.awt.event.ItemEvent;
/*      */ import java.awt.event.ItemListener;
/*      */ import java.awt.event.WindowAdapter;
/*      */ import java.awt.event.WindowEvent;
/*      */ import java.awt.print.PrinterJob;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.File;
/*      */ import java.io.FileReader;
/*      */ import java.io.FileWriter;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Collection;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.Iterator;
/*      */ import java.util.LinkedHashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Set;
/*      */ import java.util.TreeMap;
/*      */ import java.util.regex.Matcher;
/*      */ import java.util.regex.Pattern;
/*      */ import javax.imageio.ImageIO;
/*      */ import javax.swing.BorderFactory;
/*      */ import javax.swing.DefaultListModel;
/*      */ import javax.swing.ImageIcon;
/*      */ import javax.swing.JButton;
/*      */ import javax.swing.JComboBox;
/*      */ import javax.swing.JDialog;
/*      */ import javax.swing.JFrame;
/*      */ import javax.swing.JLabel;
/*      */ import javax.swing.JList;
/*      */ import javax.swing.JOptionPane;
/*      */ import javax.swing.JPanel;
/*      */ import javax.swing.JProgressBar;
/*      */ import javax.swing.JScrollPane;
/*      */ import javax.swing.JSplitPane;
/*      */ import javax.swing.JTextArea;
/*      */ import javax.swing.JTextField;
/*      */ import javax.swing.JTextPane;
/*      */ import javax.swing.JViewport;
/*      */ import javax.swing.border.TitledBorder;
/*      */ import javax.swing.event.ListSelectionEvent;
/*      */ import javax.swing.event.ListSelectionListener;
/*      */ import metapiga.MainFrame;
/*      */ import metapiga.io.NewickReader;
/*      */ import metapiga.parameters.Parameters;
/*      */ import metapiga.settings.EvaluationDialog;
/*      */ import metapiga.utilities.Tools;
/*      */ import org.biojavax.bio.phylo.io.nexus.MyNexusFileBuilder;
/*      */ import org.biojavax.bio.phylo.io.nexus.NexusBlock;
/*      */ import org.biojavax.bio.phylo.io.nexus.NexusFile;
/*      */ import org.biojavax.bio.phylo.io.nexus.NexusFileFormat;
/*      */ import org.biojavax.bio.phylo.io.nexus.TreesBlock;
/*      */ import org.biojavax.bio.phylo.io.nexus.TreesBlock.NewickTreeString;
/*      */ 
/*      */ public class TreeViewer extends JFrame
/*      */ {
/*   83 */   private Map<String, Parameters> availableParameters = new TreeMap();
/*      */   private Parameters currentParameters;
/*   85 */   private Map<String, Map<String, Tree>> loadedTrees = new TreeMap();
/*   86 */   private boolean treeLoaded = false;
/*   87 */   private boolean showAncestralStates = false;
/*   88 */   private TreePanel.Type currentType = TreePanel.Type.RECTANGULAR_CLADOGRAM;
/*   89 */   private boolean showBranchLength = false;
/*   90 */   private boolean showInodes = true;
/*   91 */   private boolean isAdjustingDatasetCombobox = false;
/*      */ 
/*   93 */   private JSplitPane mainSplitPane = null;
/*   94 */   private JScrollPane treeScrollPane = null;
/*   95 */   private JPanel treePanel = null;
/*   96 */   private JSplitPane leftSplitPane = null;
/*   97 */   private JPanel newickPanel = null;
/*   98 */   private JPanel filePanel = null;
/*   99 */   private JTextArea newickTextArea = null;
/*  100 */   private JButton fileButton = null;
/*  101 */   private DefaultListModel treeListModel = new DefaultListModel();
/*  102 */   private JList treeList = null;
/*  103 */   private JLabel newickLabel = null;
/*  104 */   private JButton addToListButton = null;
/*  105 */   private JScrollPane treeListScrollPane = null;
/*  106 */   private JScrollPane newickScrollPane = null;
/*  107 */   private JButton clearButton = null;
/*  108 */   private JPanel rightPanel = null;
/*  109 */   private JPanel treeButtonPanel = null;
/*  110 */   private JButton slantedCladogramButton = null;
/*  111 */   private JButton radialTreeButton = null;
/*  112 */   private JButton rectCladogramButton = null;
/*  113 */   private JButton phylogramButton = null;
/*  114 */   private JButton showInodesButton = null;
/*  115 */   private JButton showBlButton = null;
/*  116 */   private JButton rootButton = null;
/*  117 */   private JButton saveButton = null;
/*  118 */   private JButton printButton = null;
/*  119 */   private JTextField nameTextField = null;
/*  120 */   private JScrollPane likelihoodScrollPane = null;
/*  121 */   private JTextPane likelihoodTextPane = null;
/*  122 */   private JButton exportButton = null;
/*  123 */   private JButton removeButton = null;
/*  124 */   private JButton renameButton = null;
/*  125 */   private JComboBox datasetComboBox = null;
/*  126 */   private JPanel southPanel = null;
/*  127 */   private JButton btnAncestralStatesReconstruction = null;
/*  128 */   private AncestralStatesPanel ancestralStatesPanel = null;
/*      */   private JScrollPane treeButtonScroll;
/*      */ 
/*      */   public TreeViewer()
/*      */   {
/*  132 */     setIconImage(Tools.getScaledIcon(MainFrame.imageTreeViewer, 32).getImage());
/*  133 */     setTitle("Tree viewer");
/*  134 */     addWindowListener(new WindowAdapter()
/*      */     {
/*      */       public void windowClosing(WindowEvent e)
/*      */       {
/*      */         Iterator localIterator2;
/*  136 */         for (Iterator localIterator1 = TreeViewer.this.loadedTrees.values().iterator(); localIterator1.hasNext(); 
/*  137 */           localIterator2.hasNext())
/*      */         {
/*  136 */           Map trees = (Map)localIterator1.next();
/*  137 */           localIterator2 = trees.values().iterator(); continue; Tree tree = (Tree)localIterator2.next();
/*  138 */           tree.deleteLikelihoodComputation();
/*      */         }
/*      */       }
/*      */     });
/*  143 */     initialize();
/*      */   }
/*      */ 
/*      */   public void setAvailbleParameters(DefaultListModel parameters) {
/*  147 */     this.isAdjustingDatasetCombobox = true;
/*  148 */     this.datasetComboBox.removeAllItems();
/*  149 */     this.availableParameters.clear();
/*  150 */     for (int i = 0; i < parameters.size(); i++) {
/*  151 */       Parameters p = (Parameters)parameters.get(i);
/*  152 */       this.availableParameters.put(p.label, p);
/*  153 */       if (!this.loadedTrees.containsKey(p.label)) this.loadedTrees.put(p.label, new LinkedHashMap());
/*  154 */       this.datasetComboBox.addItem(p.label);
/*      */     }
/*  156 */     this.datasetComboBox.repaint();
/*      */     String dataset;
/*  157 */     for (Iterator it = this.loadedTrees.keySet().iterator(); it.hasNext(); ) {
/*  158 */       dataset = (String)it.next();
/*  159 */       if (!this.availableParameters.containsKey(dataset)) {
/*  160 */         it.remove();
/*      */       }
/*      */     }
/*  163 */     this.treeListModel.clear();
/*  164 */     if (this.datasetComboBox.getItemCount() > 0) {
/*  165 */       this.currentParameters = ((Parameters)this.availableParameters.get(this.datasetComboBox.getSelectedItem()));
/*  166 */       for (String treeName : ((Map)this.loadedTrees.get(this.currentParameters.label)).keySet()) {
/*  167 */         this.treeListModel.addElement(treeName);
/*      */       }
/*      */     }
/*  170 */     this.isAdjustingDatasetCombobox = false;
/*      */   }
/*      */ 
/*      */   public void setParameters(Parameters parameters) {
/*  174 */     this.isAdjustingDatasetCombobox = true;
/*  175 */     this.currentParameters = ((Parameters)this.availableParameters.get(parameters.label));
/*  176 */     this.isAdjustingDatasetCombobox = false;
/*  177 */     this.datasetComboBox.setSelectedItem(parameters.label);
/*      */   }
/*      */ 
/*      */   public void addTree(Tree tree, Parameters p) {
/*  181 */     if (tree.getNumOfNodes() > 1) {
/*  182 */       tree.deleteLikelihoodComputation();
/*  183 */       String name = tree.getName();
/*  184 */       String dataset = p.label;
/*  185 */       int generic = 1;
/*  186 */       Pattern pattern = Pattern.compile("(_\\d+)$");
/*  187 */       while (((Map)this.loadedTrees.get(dataset)).containsKey(tree.getName())) {
/*  188 */         Matcher matcher = pattern.matcher(tree.getName());
/*  189 */         if (matcher.find()) {
/*  190 */           int cut = tree.getName().lastIndexOf("_");
/*  191 */           generic = Integer.parseInt(tree.getName().substring(cut + 1)) + 1;
/*  192 */           name = tree.getName().substring(0, cut);
/*      */         }
/*  194 */         tree.setName(name + "_" + generic);
/*  195 */         generic++;
/*      */       }
/*  197 */       ((Map)this.loadedTrees.get(dataset)).put(tree.getName(), tree);
/*  198 */       if (this.datasetComboBox.getSelectedItem().toString().equals(dataset)) this.treeListModel.addElement(tree.getName());
/*  199 */       this.datasetComboBox.setSelectedItem(dataset);
/*      */     } else {
/*  201 */       JOptionPane.showMessageDialog(this, "Tree " + tree.getName() + " has less than 2 nodes", 
/*  202 */         "Cannot add tree", 0);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setSelectedTrees(List<Tree> trees) {
/*  207 */     int[] indices = new int[trees.size()];
/*  208 */     for (int i = 0; i < trees.size(); i++) {
/*  209 */       Tree tree = (Tree)trees.get(i);
/*  210 */       indices[i] = this.treeListModel.indexOf(tree.getName());
/*      */     }
/*  212 */     this.treeList.setSelectedIndices(indices);
/*      */   }
/*      */ 
/*      */   public void setSelectedTrees(Tree tree) {
/*  216 */     List trees = new ArrayList();
/*  217 */     trees.add(tree);
/*  218 */     setSelectedTrees(trees);
/*      */   }
/*      */ 
/*      */   public void setSelectedTrees(Tree tree, List<Tree> trees) {
/*  222 */     List allTrees = new ArrayList();
/*  223 */     allTrees.add(tree);
/*  224 */     allTrees.addAll(trees);
/*  225 */     setSelectedTrees(allTrees);
/*      */   }
/*      */ 
/*      */   public Map<String, Tree> getLoadedTrees(Parameters p) {
/*  229 */     if (!this.loadedTrees.containsKey(p.label)) return new HashMap();
/*  230 */     return new TreeMap((Map)this.loadedTrees.get(p.label));
/*      */   }
/*      */ 
/*      */   public Map<String, Parameters> getAvailableParameters() {
/*  234 */     return new TreeMap(this.availableParameters);
/*      */   }
/*      */ 
/*      */   public String getSelectedDataset() {
/*  238 */     return this.currentParameters.label;
/*      */   }
/*      */ 
/*      */   void setTreeLoaded(boolean treeIsLoaded) {
/*  242 */     this.treeLoaded = treeIsLoaded;
/*  243 */     if (treeIsLoaded) {
/*  244 */       getSlantedCladogramButton().setEnabled(true);
/*  245 */       getRadialTreeButton().setEnabled(true);
/*  246 */       getRectCladogramButton().setEnabled(true);
/*  247 */       getPhylogramButton().setEnabled(true);
/*  248 */       getShowInodesButton().setEnabled(!this.showAncestralStates);
/*  249 */       getShowBlButton().setEnabled(true);
/*  250 */       getRootButton().setEnabled(true);
/*  251 */       getSaveOneButton().setEnabled(true);
/*  252 */       getSaveAllButton().setEnabled(true);
/*  253 */       getPrintButton().setEnabled(true);
/*  254 */       getLikelihoodTextPane();
/*  255 */       getAncestralStatesPanel().showMessage(AncestralStatesPanel.Message.SELECT_NODE, null, (Tree)((Map)this.loadedTrees.get(this.currentParameters.label)).get(this.treeList.getSelectedValue()));
/*      */     } else {
/*  257 */       getSlantedCladogramButton().setEnabled(false);
/*  258 */       getRadialTreeButton().setEnabled(false);
/*  259 */       getRectCladogramButton().setEnabled(false);
/*  260 */       getPhylogramButton().setEnabled(false);
/*  261 */       getShowInodesButton().setEnabled(false);
/*  262 */       getShowBlButton().setEnabled(false);
/*  263 */       getRootButton().setEnabled(false);
/*  264 */       getSaveOneButton().setEnabled(false);
/*  265 */       getSaveAllButton().setEnabled(false);
/*  266 */       getPrintButton().setEnabled(false);
/*  267 */       getLikelihoodTextPane();
/*  268 */       getAncestralStatesPanel().showMessage(AncestralStatesPanel.Message.SELECT_TREE, null, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   void changeModel() {
/*  273 */     if (this.treeList.getSelectedIndex() > -1) {
/*  274 */       String selection = this.treeList.getSelectedValue().toString();
/*  275 */       Tree tree = (Tree)((Map)this.loadedTrees.get(this.currentParameters.label)).get(selection);
/*  276 */       EvaluationDialog settingsDialog = new EvaluationDialog(this, "Evaluation settings of selected tree", true, tree);
/*  277 */       settingsDialog.pack();
/*  278 */       Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/*  279 */       Dimension windowSize = settingsDialog.getSize();
/*  280 */       settingsDialog.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/*  281 */         Math.max(0, (screenSize.height - windowSize.height) / 2));
/*  282 */       settingsDialog.setVisible(true);
/*  283 */       getAncestralStatesPanel().update();
/*  284 */       getLikelihoodTextPane();
/*      */     }
/*      */   }
/*      */ 
/*      */   void slantedCladogram()
/*      */   {
/*  290 */     this.currentType = TreePanel.Type.SLANTED_CLADOGRAM;
/*  291 */     ((TreePanel)this.treePanel).treeType = this.currentType;
/*  292 */     this.treePanel.repaint();
/*      */   }
/*      */ 
/*      */   void radialTree() {
/*  296 */     this.currentType = TreePanel.Type.RADIAL_TREE;
/*  297 */     ((TreePanel)this.treePanel).treeType = this.currentType;
/*  298 */     this.treePanel.repaint();
/*      */   }
/*      */ 
/*      */   void rectCladogram() {
/*  302 */     this.currentType = TreePanel.Type.RECTANGULAR_CLADOGRAM;
/*  303 */     ((TreePanel)this.treePanel).treeType = this.currentType;
/*  304 */     this.treePanel.repaint();
/*      */   }
/*      */ 
/*      */   void phylogram() {
/*  308 */     this.currentType = TreePanel.Type.PHYLOGRAM;
/*  309 */     ((TreePanel)this.treePanel).treeType = this.currentType;
/*  310 */     this.treePanel.repaint();
/*      */   }
/*      */ 
/*      */   void showBranchLength() {
/*  314 */     this.showBranchLength = (!this.showBranchLength);
/*  315 */     ((TreePanel)this.treePanel).drawBranchLength = this.showBranchLength;
/*  316 */     this.treePanel.repaint();
/*      */   }
/*      */ 
/*      */   void showInodes() {
/*  320 */     this.showInodes = (!this.showInodes);
/*  321 */     ((TreePanel)this.treePanel).drawInodeLabel = this.showInodes;
/*  322 */     this.treePanel.repaint();
/*      */   }
/*      */ 
/*      */   void reroot() {
/*  326 */     InodeList list = new InodeList(this, ((TreePanel)this.treePanel).tree.getRoot());
/*  327 */     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/*  328 */     Dimension windowSize = list.getSize();
/*  329 */     list.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/*  330 */       Math.max(0, (screenSize.height - windowSize.height) / 2));
/*  331 */     list.setVisible(true);
/*  332 */     ((TreePanel)this.treePanel).tree.root(list.newRoot);
/*  333 */     this.treePanel.repaint();
/*      */   }
/*      */ 
/*      */   void saveSelectedTrees() {
/*  337 */     FileDialog chooser = new FileDialog(this, "Save tree(s) in Newick format to file", 1);
/*  338 */     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/*  339 */     Dimension windowSize = chooser.getSize();
/*  340 */     chooser.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/*  341 */       Math.max(0, (screenSize.height - windowSize.height) / 2));
/*  342 */     chooser.setVisible(true);
/*  343 */     if (chooser.getFile() != null) {
/*  344 */       String filename = chooser.getDirectory() + chooser.getFile();
/*  345 */       if (!filename.endsWith(".tre")) filename = filename + ".tre";
/*  346 */       File output = new File(filename);
/*      */       try {
/*  348 */         FileWriter fw = new FileWriter(output);
/*  349 */         fw.write("#NEXUS\n");
/*  350 */         fw.write("\n");
/*  351 */         fw.write("Begin trees;  [Treefile saved " + new Date(System.currentTimeMillis()).toString() + "]\n");
/*  352 */         for (Object selection : this.treeList.getSelectedValues()) {
/*  353 */           Tree tree = (Tree)((Map)this.loadedTrees.get(this.currentParameters.label)).get(selection);
/*  354 */           fw.write(tree.toNewickLine(false, true) + "\n");
/*      */         }
/*  356 */         fw.write("End;\n");
/*  357 */         fw.close();
/*      */       } catch (Exception e) {
/*  359 */         e.printStackTrace();
/*  360 */         JOptionPane.showMessageDialog(this, Tools.getErrorPanel("Error", e), 
/*  361 */           "Error in tree file saving", 0);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   void exportSelectedTrees() {
/*  367 */     Object format = JOptionPane.showInputDialog(this, "Choose an image format: ", "Export tree(s) to images", 3, MainFrame.imageImage, ImageIO.getWriterFileSuffixes(), "png");
/*  368 */     if (format != null) {
/*  369 */       FileDialog chooser = new FileDialog(this, this.treeList.getSelectedValues().length == 1 ? "Export tree to image" : "Choose a directory and a file prefix", 1);
/*  370 */       if (this.treeList.getSelectedValues().length == 1)
/*  371 */         chooser.setFile(((Tree)((Map)this.loadedTrees.get(this.currentParameters.label)).get(this.treeList.getSelectedValue())).getName() + "." + format);
/*      */       else {
/*  373 */         chooser.setFile(this.currentParameters.toString());
/*      */       }
/*  375 */       Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/*  376 */       Dimension windowSize = chooser.getSize();
/*  377 */       chooser.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/*  378 */         Math.max(0, (screenSize.height - windowSize.height) / 2));
/*  379 */       chooser.setVisible(true);
/*  380 */       if (chooser.getFile() != null)
/*      */         try {
/*  382 */           if (this.treeList.getSelectedValues().length == 1) {
/*  383 */             String filename = chooser.getDirectory() + chooser.getFile();
/*  384 */             if (!filename.toLowerCase().endsWith("." + format.toString())) filename = filename + "." + format.toString();
/*  385 */             ((TreePanel)this.treePanel).exportToImage(new File(filename), format.toString());
/*      */           } else {
/*  387 */             for (Object selection : this.treeList.getSelectedValues()) {
/*  388 */               Tree tree = (Tree)((Map)this.loadedTrees.get(this.currentParameters.label)).get(selection);
/*  389 */               String filename = chooser.getDirectory() + chooser.getFile() + "_" + tree.getName() + "." + format.toString();
/*  390 */               new TreePanel(tree, this.currentType, this.showInodes, this.showBranchLength, getAncestralStatesPanel()).exportToImage(new File(filename), format.toString());
/*      */             }
/*      */           }
/*      */         } catch (Exception e) {
/*  394 */           e.printStackTrace();
/*  395 */           JOptionPane.showMessageDialog(this, Tools.getErrorPanel("Error", e), 
/*  396 */             "Error in tree file exporting", 0);
/*      */         }
/*      */     }
/*      */   }
/*      */ 
/*      */   void print()
/*      */   {
/*  403 */     PrinterJob pj = PrinterJob.getPrinterJob();
/*      */ 
/*  406 */     if (pj.printDialog())
/*  407 */       for (Object selection : this.treeList.getSelectedValues()) {
/*  408 */         pj.setPrintable(new TreePanel((Tree)((Map)this.loadedTrees.get(this.currentParameters.label)).get(selection), this.currentType, this.showInodes, this.showBranchLength, getAncestralStatesPanel()));
/*      */         try {
/*  410 */           pj.print();
/*      */         } catch (Exception PrintException) {
/*  412 */           PrintException.printStackTrace();
/*      */         }
/*      */       }
/*      */   }
/*      */ 
/*      */   public void showAncestralStates(boolean show)
/*      */   {
/*  447 */     if (show) {
/*  448 */       this.showAncestralStates = true;
/*  449 */       getAncestralStatesPanel().setVisible(true);
/*  450 */       this.btnAncestralStatesReconstruction.setText("[-] Hide ancestral states reconstruction [-]");
/*  451 */       if (!this.showInodes) showInodes();
/*  452 */       getShowInodesButton().setEnabled(false);
/*      */     } else {
/*  454 */       this.showAncestralStates = false;
/*  455 */       getAncestralStatesPanel().setVisible(false);
/*  456 */       this.btnAncestralStatesReconstruction.setText("[+] Show ancestral states reconstruction [+]");
/*  457 */       getShowInodesButton().setEnabled(this.treeLoaded);
/*      */     }
/*      */   }
/*      */ 
/*      */   private void initialize()
/*      */   {
/*  466 */     GridBagLayout gridBagLayout = new GridBagLayout();
/*  467 */     getContentPane().setLayout(gridBagLayout);
/*  468 */     GridBagConstraints gbc_mainSplitPane = new GridBagConstraints();
/*  469 */     gbc_mainSplitPane.weighty = 1.0D;
/*  470 */     gbc_mainSplitPane.weightx = 1.0D;
/*  471 */     gbc_mainSplitPane.fill = 1;
/*  472 */     gbc_mainSplitPane.insets = new Insets(0, 0, 5, 0);
/*  473 */     gbc_mainSplitPane.gridx = 0;
/*  474 */     gbc_mainSplitPane.gridy = 0;
/*  475 */     getContentPane().add(getMainSplitPane(), gbc_mainSplitPane);
/*  476 */     GridBagConstraints gbc_southPanel = new GridBagConstraints();
/*  477 */     gbc_southPanel.weightx = 1.0D;
/*  478 */     gbc_southPanel.anchor = 15;
/*  479 */     gbc_southPanel.fill = 1;
/*  480 */     gbc_southPanel.gridx = 0;
/*  481 */     gbc_southPanel.gridy = 1;
/*  482 */     getContentPane().add(getSouthPanel(), gbc_southPanel);
/*      */   }
/*      */ 
/*      */   private JSplitPane getMainSplitPane()
/*      */   {
/*  491 */     if (this.mainSplitPane == null) {
/*  492 */       this.mainSplitPane = new JSplitPane();
/*  493 */       this.mainSplitPane.setLeftComponent(getLeftPanel());
/*  494 */       this.mainSplitPane.setRightComponent(getRightPanel());
/*      */     }
/*  496 */     return this.mainSplitPane;
/*      */   }
/*      */ 
/*      */   private JScrollPane getTreeScrollPane()
/*      */   {
/*  506 */     if (this.treeScrollPane == null) {
/*  507 */       this.treeScrollPane = new JScrollPane();
/*  508 */       this.treeScrollPane.setViewportView(getTreePanel());
/*      */     }
/*  510 */     return this.treeScrollPane;
/*      */   }
/*      */ 
/*      */   private JPanel getTreePanel()
/*      */   {
/*  520 */     if (this.treePanel == null) {
/*  521 */       this.treePanel = new JPanel();
/*  522 */       this.treePanel.setBackground(Color.BLACK);
/*  523 */       this.treePanel.setLayout(new GridBagLayout());
/*  524 */       setTreeLoaded(false);
/*      */     }
/*  526 */     return this.treePanel;
/*      */   }
/*      */ 
/*      */   private JSplitPane getLeftPanel()
/*      */   {
/*  536 */     if (this.leftSplitPane == null) {
/*  537 */       this.leftSplitPane = new JSplitPane();
/*  538 */       this.leftSplitPane.setOrientation(0);
/*  539 */       this.leftSplitPane.setResizeWeight(0.5D);
/*  540 */       this.leftSplitPane.setBottomComponent(getNewickPanel());
/*  541 */       this.leftSplitPane.setTopComponent(getFilePanel());
/*      */     }
/*  543 */     return this.leftSplitPane;
/*      */   }
/*      */ 
/*      */   private JPanel getNewickPanel()
/*      */   {
/*  553 */     if (this.newickPanel == null) {
/*  554 */       GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
/*  555 */       gridBagConstraints11.fill = 1;
/*  556 */       gridBagConstraints11.gridy = 0;
/*  557 */       gridBagConstraints11.weightx = 1.0D;
/*  558 */       gridBagConstraints11.insets = new Insets(5, 0, 5, 5);
/*  559 */       gridBagConstraints11.gridx = 1;
/*  560 */       GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
/*  561 */       gridBagConstraints7.fill = 1;
/*  562 */       gridBagConstraints7.gridy = 1;
/*  563 */       gridBagConstraints7.weightx = 1.0D;
/*  564 */       gridBagConstraints7.weighty = 1.0D;
/*  565 */       gridBagConstraints7.gridwidth = 3;
/*  566 */       gridBagConstraints7.insets = new Insets(0, 5, 5, 5);
/*  567 */       gridBagConstraints7.gridx = 0;
/*  568 */       GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
/*  569 */       gridBagConstraints51.gridx = 2;
/*  570 */       gridBagConstraints51.insets = new Insets(5, 5, 5, 5);
/*  571 */       gridBagConstraints51.anchor = 13;
/*  572 */       gridBagConstraints51.gridy = 0;
/*  573 */       this.newickLabel = new JLabel();
/*  574 */       this.newickLabel.setText("Tree Name");
/*  575 */       GridBagConstraints gridBagConstraints = new GridBagConstraints();
/*  576 */       gridBagConstraints.gridx = 0;
/*  577 */       gridBagConstraints.gridy = 0;
/*  578 */       gridBagConstraints.fill = 0;
/*  579 */       gridBagConstraints.weighty = 0.0D;
/*  580 */       gridBagConstraints.insets = new Insets(5, 5, 5, 5);
/*  581 */       gridBagConstraints.anchor = 10;
/*  582 */       gridBagConstraints.weightx = 0.0D;
/*  583 */       this.newickPanel = new JPanel();
/*  584 */       this.newickPanel.setLayout(new GridBagLayout());
/*  585 */       this.newickPanel.setBorder(BorderFactory.createEtchedBorder(1));
/*  586 */       this.newickPanel.add(this.newickLabel, gridBagConstraints);
/*  587 */       this.newickPanel.add(getAddToListButton(), gridBagConstraints51);
/*  588 */       this.newickPanel.add(getNewickScrollPane(), gridBagConstraints7);
/*  589 */       this.newickPanel.add(getNameTextField(), gridBagConstraints11);
/*      */     }
/*  591 */     return this.newickPanel;
/*      */   }
/*      */ 
/*      */   private JPanel getFilePanel()
/*      */   {
/*  601 */     if (this.filePanel == null) {
/*  602 */       GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
/*  603 */       gridBagConstraints6.gridx = 0;
/*  604 */       gridBagConstraints6.insets = new Insets(5, 5, 5, 5);
/*  605 */       gridBagConstraints6.fill = 2;
/*  606 */       gridBagConstraints6.weightx = 1.0D;
/*  607 */       gridBagConstraints6.gridy = 3;
/*  608 */       GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
/*  609 */       gridBagConstraints5.gridx = 1;
/*  610 */       gridBagConstraints5.insets = new Insets(5, 5, 5, 5);
/*  611 */       gridBagConstraints5.fill = 2;
/*  612 */       gridBagConstraints5.weightx = 1.0D;
/*  613 */       gridBagConstraints5.gridy = 3;
/*  614 */       GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
/*  615 */       gridBagConstraints61.fill = 1;
/*  616 */       gridBagConstraints61.gridy = 5;
/*  617 */       gridBagConstraints61.weightx = 1.0D;
/*  618 */       gridBagConstraints61.weighty = 1.0D;
/*  619 */       gridBagConstraints61.insets = new Insets(5, 5, 5, 5);
/*  620 */       gridBagConstraints61.gridwidth = 3;
/*  621 */       gridBagConstraints61.gridx = 0;
/*  622 */       GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
/*  623 */       gridBagConstraints21.gridwidth = 2;
/*  624 */       gridBagConstraints21.gridx = 0;
/*  625 */       gridBagConstraints21.insets = new Insets(5, 5, 5, 5);
/*  626 */       gridBagConstraints21.anchor = 10;
/*  627 */       gridBagConstraints21.fill = 2;
/*  628 */       gridBagConstraints21.weightx = 1.0D;
/*  629 */       gridBagConstraints21.gridy = 1;
/*  630 */       this.filePanel = new JPanel();
/*  631 */       GridBagLayout gridBagLayout = new GridBagLayout();
/*  632 */       this.filePanel.setLayout(gridBagLayout);
/*  633 */       this.filePanel.setBorder(BorderFactory.createEtchedBorder(1));
/*  634 */       this.filePanel.add(getFileButton(), gridBagConstraints21);
/*  635 */       this.filePanel.add(getTreeListScrollPane(), gridBagConstraints61);
/*  636 */       this.filePanel.add(getRemoveButton(), gridBagConstraints5);
/*      */ 
/*  638 */       JLabel datasetLabel = new JLabel();
/*  639 */       datasetLabel.setText("Current dataset");
/*  640 */       GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
/*  641 */       gridBagConstraints_1.insets = new Insets(5, 5, 5, 5);
/*  642 */       gridBagConstraints_1.gridy = 2;
/*  643 */       gridBagConstraints_1.gridx = 0;
/*  644 */       this.filePanel.add(datasetLabel, gridBagConstraints_1);
/*  645 */       this.datasetComboBox = new JComboBox();
/*  646 */       this.datasetComboBox.addItemListener(new ItemListener() {
/*      */         public void itemStateChanged(ItemEvent arg0) {
/*  648 */           if ((!TreeViewer.this.isAdjustingDatasetCombobox) && (arg0.getStateChange() == 1)) {
/*  649 */             TreeViewer.this.currentParameters = ((Parameters)TreeViewer.this.availableParameters.get(TreeViewer.this.datasetComboBox.getSelectedItem()));
/*  650 */             TreeViewer.this.treeListModel.clear();
/*  651 */             for (String treeName : ((Map)TreeViewer.this.loadedTrees.get(TreeViewer.this.currentParameters.label)).keySet())
/*  652 */               TreeViewer.this.treeListModel.addElement(treeName);
/*      */           }
/*      */         }
/*      */       });
/*  657 */       GridBagConstraints gridBagConstraints = new GridBagConstraints();
/*  658 */       gridBagConstraints.insets = new Insets(5, 0, 5, 5);
/*  659 */       gridBagConstraints.fill = 2;
/*  660 */       gridBagConstraints.gridwidth = 2;
/*  661 */       gridBagConstraints.gridy = 2;
/*  662 */       gridBagConstraints.gridx = 1;
/*  663 */       this.filePanel.add(this.datasetComboBox, gridBagConstraints);
/*  664 */       this.filePanel.add(getRenameButton(), gridBagConstraints6);
/*      */     }
/*  666 */     return this.filePanel;
/*      */   }
/*      */ 
/*      */   private JTextArea getNewickTextArea()
/*      */   {
/*  675 */     if (this.newickTextArea == null) {
/*  676 */       this.newickTextArea = new JTextArea();
/*  677 */       this.newickTextArea.setWrapStyleWord(true);
/*  678 */       this.newickTextArea.setLineWrap(true);
/*      */     }
/*  680 */     return this.newickTextArea;
/*      */   }
/*      */ 
/*      */   private JButton getFileButton()
/*      */   {
/*  690 */     if (this.fileButton == null) {
/*  691 */       this.fileButton = new JButton();
/*  692 */       this.fileButton.setText("Add tree from file");
/*  693 */       this.fileButton.addActionListener(new ActionListener()
/*      */       {
/*      */         public void actionPerformed(ActionEvent ev) {
/*  696 */           new Thread(new Runnable() {
/*      */             public void run() {
/*  698 */               Frame frame = new Frame();
/*  699 */               FileDialog chooser = new FileDialog(frame, "Choose a file containing tree(s) in newick format", 0);
/*  700 */               Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/*  701 */               Dimension windowSize = chooser.getSize();
/*  702 */               chooser.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/*  703 */                 Math.max(0, (screenSize.height - windowSize.height) / 2));
/*  704 */               chooser.setVisible(true);
/*  705 */               if (chooser.getFile() != null)
/*      */                 try {
/*  707 */                   File nexus = new File(chooser.getDirectory() + chooser.getFile());
/*  708 */                   FileReader fr = new FileReader(nexus);
/*  709 */                   BufferedReader br = new BufferedReader(fr);
/*  710 */                   String line = br.readLine();
/*      */ 
/*  712 */                   JDialog progressDialog = new JDialog(TreeViewer.this, false);
/*  713 */                   JProgressBar progressBar = new JProgressBar();
/*  714 */                   progressBar.setIndeterminate(true);
/*  715 */                   progressBar.setString("Parsing trees");
/*  716 */                   progressBar.setStringPainted(true);
/*  717 */                   progressDialog.getContentPane().setLayout(new BorderLayout());
/*  718 */                   progressDialog.getContentPane().add(progressBar, "Center");
/*  719 */                   progressDialog.setIconImage(Tools.getScaledIcon(MainFrame.imageMetapiga, 32).getImage());
/*  720 */                   progressDialog.setDefaultCloseOperation(0);
/*  721 */                   progressDialog.setResizable(false);
/*  722 */                   progressDialog.setUndecorated(true);
/*  723 */                   screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/*  724 */                   progressDialog.setSize((int)screenSize.getWidth() / 2, (int)screenSize.getHeight() / 10);
/*  725 */                   windowSize = progressDialog.getSize();
/*  726 */                   progressDialog.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), Math.max(0, (screenSize.height - windowSize.height) / 2));
/*  727 */                   progressDialog.setTitle("Parsing trees");
/*  728 */                   progressDialog.setVisible(true);
/*      */                   try {
/*  730 */                     if (line.trim().toUpperCase().startsWith("#NEXUS"))
/*      */                     {
/*  737 */                       MyNexusFileBuilder builder = new MyNexusFileBuilder();
/*  738 */                       NexusFileFormat.parseFile(builder, nexus);
/*  739 */                       NexusFile file = builder.getNexusFile();
/*  740 */                       for (Iterator it = file.blockIterator(); it.hasNext(); ) {
/*  741 */                         NexusBlock block = (NexusBlock)it.next();
/*  742 */                         if (block.getBlockName().equals("TREES")) {
/*  743 */                           TreesBlock tb = (TreesBlock)block;
/*  744 */                           progressBar.setIndeterminate(false);
/*  745 */                           progressBar.setMaximum(tb.getTrees().size());
/*  746 */                           int p = 0;
/*  747 */                           for (Iterator tr = tb.getTrees().entrySet().iterator(); tr.hasNext(); ) {
/*  748 */                             progressBar.setValue(++p);
/*  749 */                             Map.Entry e = (Map.Entry)tr.next();
/*  750 */                             NewickReader nr = new NewickReader(TreeViewer.this.currentParameters, (String)e.getKey(), ((TreesBlock.NewickTreeString)e.getValue()).getTreeString(), tb.getTranslations());
/*  751 */                             TreeViewer.this.addTree(nr.parseNewick(), TreeViewer.this.currentParameters);
/*      */                           }
/*      */                         }
/*      */                       }
/*      */ 
/*      */                     }
/*      */                     else
/*      */                     {
/*  759 */                       progressBar.setIndeterminate(false);
/*  760 */                       progressBar.setMaximum((int)nexus.length());
/*  761 */                       int p = 0;
/*  762 */                       while ((line = br.readLine()) != null) {
/*  763 */                         p += line.length();
/*  764 */                         progressBar.setValue(p);
/*  765 */                         line = line.trim();
/*  766 */                         if (line.toUpperCase().startsWith("TREE")) {
/*  767 */                           NewickReader nr = new NewickReader(TreeViewer.this.currentParameters, line);
/*  768 */                           TreeViewer.this.addTree(nr.parseNewick(), TreeViewer.this.currentParameters);
/*      */                         }
/*      */                       }
/*      */                     }
/*      */                   } catch (Exception e) {
/*  773 */                     e.printStackTrace();
/*  774 */                     JOptionPane.showMessageDialog(TreeViewer.this, Tools.getErrorPanel("Error when parsing trees", e), "Error", 0);
/*      */                   } finally {
/*  776 */                     progressDialog.setVisible(false);
/*      */                   }
/*      */ 
/*  779 */                   br.close();
/*  780 */                   fr.close();
/*      */                 }
/*      */                 catch (Exception ex) {
/*  783 */                   JOptionPane.showMessageDialog(frame, Tools.getErrorPanel("Error", ex), "Error when loading tree", 0);
/*  784 */                   ex.printStackTrace();
/*      */                 }
/*      */             }
/*      */           }).start();
/*      */         }
/*      */       });
/*      */     }
/*  792 */     return this.fileButton;
/*      */   }
/*      */ 
/*      */   private JList getTreeList()
/*      */   {
/*  802 */     if (this.treeList == null) {
/*  803 */       this.treeList = new JList(this.treeListModel);
/*  804 */       this.treeList.setSelectionMode(2);
/*  805 */       this.treeList
/*  806 */         .addListSelectionListener(new ListSelectionListener() {
/*      */         public void valueChanged(ListSelectionEvent e) {
/*      */           try {
/*  809 */             if (!e.getValueIsAdjusting()) {
/*  810 */               TreeViewer.this.treeScrollPane.getViewport().remove(TreeViewer.this.treePanel);
/*  811 */               if (TreeViewer.this.treeList.getSelectedValue() != null) {
/*  812 */                 TreeViewer.this.treePanel = new TreePanel((Tree)((Map)TreeViewer.this.loadedTrees.get(TreeViewer.this.currentParameters.label)).get(TreeViewer.this.treeList.getSelectedValue()), TreeViewer.this.currentType, TreeViewer.this.showInodes, TreeViewer.this.showBranchLength, TreeViewer.this.getAncestralStatesPanel());
/*  813 */                 TreeViewer.this.setTreeLoaded(true);
/*      */               } else {
/*  815 */                 TreeViewer.this.treePanel = new JPanel();
/*  816 */                 TreeViewer.this.treePanel.setBackground(Color.BLACK);
/*  817 */                 TreeViewer.this.treePanel.setLayout(new GridBagLayout());
/*  818 */                 TreeViewer.this.setTreeLoaded(false);
/*      */               }
/*  820 */               TreeViewer.this.treeScrollPane.setViewportView(TreeViewer.this.treePanel);
/*      */             }
/*      */           } catch (Exception ex) {
/*  823 */             JOptionPane.showMessageDialog(null, Tools.getErrorPanel("Error", ex), "Error in loaded tree", 0);
/*  824 */             ex.printStackTrace();
/*      */           }
/*      */         }
/*      */       });
/*      */     }
/*  829 */     return this.treeList;
/*      */   }
/*      */ 
/*      */   private JButton getAddToListButton()
/*      */   {
/*  839 */     if (this.addToListButton == null) {
/*  840 */       this.addToListButton = new JButton();
/*  841 */       this.addToListButton.setText("Add to list");
/*  842 */       this.addToListButton.addActionListener(new ActionListener() {
/*      */         public void actionPerformed(ActionEvent e) {
/*      */           try {
/*  845 */             if (TreeViewer.this.newickTextArea.getText().length() > 0) {
/*  846 */               NewickReader nr = new NewickReader(TreeViewer.this.currentParameters, TreeViewer.this.newickTextArea.getText());
/*  847 */               Tree tree = nr.parseNewick();
/*  848 */               String treeName = TreeViewer.this.getNameTextField().getText();
/*  849 */               if (treeName.length() < 1) {
/*  850 */                 treeName = tree.getName();
/*      */               }
/*  852 */               tree.setName(treeName);
/*  853 */               TreeViewer.this.addTree(tree, TreeViewer.this.currentParameters);
/*  854 */               TreeViewer.this.treeList.setSelectedValue(tree.getName(), true);
/*  855 */               TreeViewer.this.newickTextArea.setText("");
/*  856 */               TreeViewer.this.getNameTextField().setText("");
/*      */             }
/*      */           } catch (Exception ex) {
/*  859 */             JOptionPane.showMessageDialog(null, Tools.getErrorPanel("Error", ex), "Error in your Newick tree", 0);
/*  860 */             ex.printStackTrace();
/*      */           }
/*      */         }
/*      */       });
/*      */     }
/*  865 */     return this.addToListButton;
/*      */   }
/*      */ 
/*      */   private JScrollPane getTreeListScrollPane()
/*      */   {
/*  875 */     if (this.treeListScrollPane == null)
/*      */     {
/*  877 */       JButton modelButton = new JButton();
/*  878 */       modelButton.addActionListener(new ActionListener() {
/*      */         public void actionPerformed(ActionEvent arg0) {
/*  880 */           TreeViewer.this.changeModel();
/*      */         }
/*      */       });
/*  883 */       modelButton.setText("Model");
/*  884 */       GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
/*  885 */       gridBagConstraints_1.weightx = 1.0D;
/*  886 */       gridBagConstraints_1.fill = 2;
/*  887 */       gridBagConstraints_1.insets = new Insets(5, 5, 5, 5);
/*  888 */       gridBagConstraints_1.gridy = 3;
/*  889 */       gridBagConstraints_1.gridx = 2;
/*  890 */       this.filePanel.add(modelButton, gridBagConstraints_1);
/*  891 */       this.treeListScrollPane = new JScrollPane();
/*  892 */       this.treeListScrollPane.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), "Available trees"));
/*  893 */       this.treeListScrollPane.setViewportView(getTreeList());
/*      */     }
/*  895 */     return this.treeListScrollPane;
/*      */   }
/*      */ 
/*      */   private JScrollPane getNewickScrollPane()
/*      */   {
/*  905 */     if (this.newickScrollPane == null) {
/*  906 */       this.newickScrollPane = new JScrollPane();
/*  907 */       this.newickScrollPane.setViewportView(getNewickTextArea());
/*  908 */       this.newickScrollPane.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), "Tree in Newick format"));
/*      */     }
/*  910 */     return this.newickScrollPane;
/*      */   }
/*      */ 
/*      */   private JButton getClearButton()
/*      */   {
/*  920 */     if (this.clearButton == null) {
/*  921 */       this.clearButton = new JButton();
/*  922 */       this.clearButton.setText("Clear list");
/*  923 */       this.clearButton.addActionListener(new ActionListener() {
/*      */         public void actionPerformed(ActionEvent e) {
/*  925 */           int yesno = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete ALL trees from the Tree Viewer for the selected dataset ?", "Clearing dataset", 0, 3, Tools.getScaledIcon(MainFrame.imageTreeViewer, 64));
/*  926 */           if (yesno == 0) {
/*  927 */             ((Map)TreeViewer.this.loadedTrees.get(TreeViewer.this.currentParameters.label)).clear();
/*  928 */             TreeViewer.this.treeListModel.clear();
/*      */           }
/*      */         }
/*      */       });
/*      */     }
/*  933 */     return this.clearButton;
/*      */   }
/*      */ 
/*      */   private JPanel getRightPanel()
/*      */   {
/*  943 */     if (this.rightPanel == null) {
/*  944 */       GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
/*  945 */       gridBagConstraints2.gridx = 0;
/*  946 */       gridBagConstraints2.gridy = 1;
/*  947 */       gridBagConstraints2.fill = 1;
/*  948 */       gridBagConstraints2.weighty = 0.9D;
/*  949 */       gridBagConstraints2.weightx = 1.0D;
/*  950 */       GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
/*  951 */       gridBagConstraints3.gridx = 0;
/*  952 */       gridBagConstraints3.gridy = 2;
/*  953 */       gridBagConstraints3.fill = 2;
/*  954 */       gridBagConstraints3.weightx = 1.0D;
/*  955 */       GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
/*  956 */       gridBagConstraints4.gridx = 0;
/*  957 */       gridBagConstraints4.gridy = 0;
/*  958 */       gridBagConstraints4.fill = 1;
/*  959 */       gridBagConstraints4.weightx = 1.0D;
/*  960 */       gridBagConstraints4.weighty = 0.1D;
/*  961 */       this.rightPanel = new JPanel();
/*  962 */       this.rightPanel.setLayout(new GridBagLayout());
/*  963 */       this.rightPanel.add(getTreeScrollPane(), gridBagConstraints2);
/*  964 */       this.rightPanel.add(getTreeButtonScrollButton(), gridBagConstraints3);
/*  965 */       this.rightPanel.add(getLikelihoodScrollPane(), gridBagConstraints4);
/*      */     }
/*  967 */     return this.rightPanel;
/*      */   }
/*      */ 
/*      */   private JPanel getTreeButtonScrollButton()
/*      */   {
/*  977 */     if (this.treeButtonPanel == null) {
/*  978 */       this.treeButtonPanel = new JPanel();
/*  979 */       this.treeButtonScroll = new JScrollPane();
/*  980 */       this.treeButtonPanel.setLayout(new FlowLayout());
/*  981 */       this.treeButtonPanel.add(getRectCladogramButton(), null);
/*  982 */       this.treeButtonPanel.add(getSlantedCladogramButton(), null);
/*  983 */       this.treeButtonPanel.add(getRadialTreeButton(), null);
/*  984 */       this.treeButtonPanel.add(getPhylogramButton(), null);
/*  985 */       this.treeButtonPanel.add(getShowInodesButton(), null);
/*  986 */       this.treeButtonPanel.add(getShowBlButton(), null);
/*  987 */       this.treeButtonPanel.add(getRootButton(), null);
/*  988 */       this.treeButtonPanel.add(getSaveOneButton(), null);
/*  989 */       this.treeButtonPanel.add(getSaveAllButton(), null);
/*  990 */       this.treeButtonPanel.add(getPrintButton(), null);
/*  991 */       this.treeButtonScroll.setViewportView(this.treeButtonPanel);
/*      */     }
/*  993 */     return this.treeButtonPanel;
/*      */   }
/*      */ 
/*      */   private JButton getSlantedCladogramButton()
/*      */   {
/* 1003 */     if (this.slantedCladogramButton == null) {
/* 1004 */       this.slantedCladogramButton = new JButton();
/* 1005 */       this.slantedCladogramButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 1006 */       this.slantedCladogramButton.setMinimumSize(new Dimension(37, 37));
/* 1007 */       this.slantedCladogramButton.setToolTipText("Slanted cladogram");
/* 1008 */       this.slantedCladogramButton.setBorderPainted(true);
/* 1009 */       this.slantedCladogramButton.setContentAreaFilled(false);
/* 1010 */       this.slantedCladogramButton.setIcon(MainFrame.imageSlantedCladogram);
/* 1011 */       this.slantedCladogramButton.setMaximumSize(new Dimension(40, 40));
/* 1012 */       this.slantedCladogramButton.addActionListener(new ActionListener() {
/*      */         public void actionPerformed(ActionEvent e) {
/* 1014 */           TreeViewer.this.slantedCladogram();
/*      */         }
/*      */       });
/*      */     }
/* 1018 */     return this.slantedCladogramButton;
/*      */   }
/*      */ 
/*      */   private JButton getRadialTreeButton()
/*      */   {
/* 1027 */     if (this.radialTreeButton == null) {
/* 1028 */       this.radialTreeButton = new JButton();
/* 1029 */       this.radialTreeButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 1030 */       this.radialTreeButton.setMinimumSize(new Dimension(37, 37));
/* 1031 */       this.radialTreeButton.setToolTipText("Radial tree");
/* 1032 */       this.radialTreeButton.setBorderPainted(true);
/* 1033 */       this.radialTreeButton.setContentAreaFilled(false);
/* 1034 */       this.radialTreeButton.setIcon(MainFrame.imageRadialCladogram);
/* 1035 */       this.radialTreeButton.setMaximumSize(new Dimension(40, 40));
/* 1036 */       this.radialTreeButton.addActionListener(new ActionListener() {
/*      */         public void actionPerformed(ActionEvent e) {
/* 1038 */           TreeViewer.this.radialTree();
/*      */         }
/*      */       });
/*      */     }
/* 1042 */     return this.radialTreeButton;
/*      */   }
/*      */ 
/*      */   private JButton getRectCladogramButton()
/*      */   {
/* 1052 */     if (this.rectCladogramButton == null) {
/* 1053 */       this.rectCladogramButton = new JButton();
/* 1054 */       this.rectCladogramButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 1055 */       this.rectCladogramButton.setMinimumSize(new Dimension(37, 37));
/* 1056 */       this.rectCladogramButton.setToolTipText("Rectangular cladogram");
/* 1057 */       this.rectCladogramButton.setContentAreaFilled(false);
/* 1058 */       this.rectCladogramButton.setIcon(MainFrame.imageRectangularCladogram);
/* 1059 */       this.rectCladogramButton.setMaximumSize(new Dimension(40, 40));
/* 1060 */       this.rectCladogramButton.addActionListener(new ActionListener() {
/*      */         public void actionPerformed(ActionEvent e) {
/* 1062 */           TreeViewer.this.rectCladogram();
/*      */         }
/*      */       });
/*      */     }
/* 1066 */     return this.rectCladogramButton;
/*      */   }
/*      */ 
/*      */   private JButton getPhylogramButton()
/*      */   {
/* 1076 */     if (this.phylogramButton == null) {
/* 1077 */       this.phylogramButton = new JButton();
/* 1078 */       this.phylogramButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 1079 */       this.phylogramButton.setMinimumSize(new Dimension(37, 37));
/* 1080 */       this.phylogramButton.setToolTipText("Phylogram");
/* 1081 */       this.phylogramButton.setContentAreaFilled(false);
/* 1082 */       this.phylogramButton.setIcon(MainFrame.imagePhylogram);
/* 1083 */       this.phylogramButton.setMaximumSize(new Dimension(40, 40));
/* 1084 */       this.phylogramButton.addActionListener(new ActionListener() {
/*      */         public void actionPerformed(ActionEvent e) {
/* 1086 */           TreeViewer.this.phylogram();
/*      */         }
/*      */       });
/*      */     }
/* 1090 */     return this.phylogramButton;
/*      */   }
/*      */ 
/*      */   private JButton getShowInodesButton()
/*      */   {
/* 1100 */     if (this.showInodesButton == null) {
/* 1101 */       this.showInodesButton = new JButton();
/* 1102 */       this.showInodesButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 1103 */       this.showInodesButton.setMinimumSize(new Dimension(37, 37));
/* 1104 */       this.showInodesButton.setToolTipText("Show internal nodes labels");
/* 1105 */       this.showInodesButton.setContentAreaFilled(false);
/* 1106 */       this.showInodesButton.setIcon(MainFrame.imageInodes);
/* 1107 */       this.showInodesButton.setMaximumSize(new Dimension(40, 40));
/* 1108 */       this.showInodesButton.addActionListener(new ActionListener() {
/*      */         public void actionPerformed(ActionEvent e) {
/* 1110 */           TreeViewer.this.showInodes();
/*      */         }
/*      */       });
/*      */     }
/* 1114 */     return this.showInodesButton;
/*      */   }
/*      */ 
/*      */   private JButton getShowBlButton()
/*      */   {
/* 1124 */     if (this.showBlButton == null) {
/* 1125 */       this.showBlButton = new JButton();
/* 1126 */       this.showBlButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 1127 */       this.showBlButton.setMinimumSize(new Dimension(37, 37));
/* 1128 */       this.showBlButton.setToolTipText("Show branch lengths");
/* 1129 */       this.showBlButton.setContentAreaFilled(false);
/* 1130 */       this.showBlButton.setIcon(MainFrame.imageBranchLength);
/* 1131 */       this.showBlButton.setMaximumSize(new Dimension(40, 40));
/* 1132 */       this.showBlButton.addActionListener(new ActionListener() {
/*      */         public void actionPerformed(ActionEvent e) {
/* 1134 */           TreeViewer.this.showBranchLength();
/*      */         }
/*      */       });
/*      */     }
/* 1138 */     return this.showBlButton;
/*      */   }
/*      */ 
/*      */   private JButton getRootButton()
/*      */   {
/* 1148 */     if (this.rootButton == null) {
/* 1149 */       this.rootButton = new JButton();
/* 1150 */       this.rootButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 1151 */       this.rootButton.setMinimumSize(new Dimension(37, 37));
/* 1152 */       this.rootButton.setToolTipText("Re-root tree");
/* 1153 */       this.rootButton.setContentAreaFilled(false);
/* 1154 */       this.rootButton.setIcon(MainFrame.imageRoot);
/* 1155 */       this.rootButton.setMaximumSize(new Dimension(40, 40));
/* 1156 */       this.rootButton.addActionListener(new ActionListener() {
/*      */         public void actionPerformed(ActionEvent e) {
/* 1158 */           TreeViewer.this.reroot();
/*      */         }
/*      */       });
/*      */     }
/* 1162 */     return this.rootButton;
/*      */   }
/*      */ 
/*      */   private JButton getSaveOneButton()
/*      */   {
/* 1172 */     if (this.saveButton == null) {
/* 1173 */       this.saveButton = new JButton();
/* 1174 */       this.saveButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 1175 */       this.saveButton.setMinimumSize(new Dimension(37, 37));
/* 1176 */       this.saveButton.setToolTipText("Save selected tree(s) in Newick format to a file");
/* 1177 */       this.saveButton.setContentAreaFilled(false);
/* 1178 */       this.saveButton.setIcon(MainFrame.imageSaveFile);
/* 1179 */       this.saveButton.setMaximumSize(new Dimension(40, 40));
/* 1180 */       this.saveButton.addActionListener(new ActionListener() {
/*      */         public void actionPerformed(ActionEvent e) {
/* 1182 */           TreeViewer.this.saveSelectedTrees();
/*      */         }
/*      */       });
/*      */     }
/* 1186 */     return this.saveButton;
/*      */   }
/*      */ 
/*      */   private JButton getPrintButton()
/*      */   {
/* 1196 */     if (this.printButton == null) {
/* 1197 */       this.printButton = new JButton();
/* 1198 */       this.printButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 1199 */       this.printButton.setMinimumSize(new Dimension(37, 37));
/* 1200 */       this.printButton.setToolTipText("Print selected tree(s)");
/* 1201 */       this.printButton.setContentAreaFilled(false);
/* 1202 */       this.printButton.setIcon(MainFrame.imagePrinter);
/* 1203 */       this.printButton.setMaximumSize(new Dimension(40, 40));
/* 1204 */       this.printButton.addActionListener(new ActionListener() {
/*      */         public void actionPerformed(ActionEvent e) {
/* 1206 */           TreeViewer.this.print();
/*      */         }
/*      */       });
/*      */     }
/* 1210 */     return this.printButton;
/*      */   }
/*      */ 
/*      */   private JTextField getNameTextField()
/*      */   {
/* 1219 */     if (this.nameTextField == null) {
/* 1220 */       this.nameTextField = new JTextField();
/*      */     }
/* 1222 */     return this.nameTextField;
/*      */   }
/*      */ 
/*      */   private JScrollPane getLikelihoodScrollPane()
/*      */   {
/* 1231 */     if (this.likelihoodScrollPane == null) {
/* 1232 */       this.likelihoodScrollPane = new JScrollPane();
/* 1233 */       this.likelihoodScrollPane.setHorizontalScrollBarPolicy(31);
/* 1234 */       this.likelihoodScrollPane.setViewportView(getLikelihoodTextPane());
/*      */     }
/* 1236 */     return this.likelihoodScrollPane;
/*      */   }
/*      */ 
/*      */   private JTextPane getLikelihoodTextPane()
/*      */   {
/* 1245 */     if (this.likelihoodTextPane == null) {
/* 1246 */       this.likelihoodTextPane = new JTextPane();
/* 1247 */       this.likelihoodTextPane.setBackground(Color.BLACK);
/* 1248 */       this.likelihoodTextPane.setForeground(Color.GREEN);
/* 1249 */       this.likelihoodTextPane.setEditable(false);
/*      */     }
/*      */     try {
/* 1252 */       if (this.treeLoaded) {
/* 1253 */         this.likelihoodTextPane.setStyledDocument(((TreePanel)this.treePanel).tree.getEvaluationString());
/* 1254 */         ((TreePanel)this.treePanel).tree.deleteLikelihoodComputation();
/*      */       } else {
/* 1256 */         this.likelihoodTextPane.setText("");
/*      */       }
/*      */     } catch (Exception e) {
/* 1259 */       e.printStackTrace();
/* 1260 */       this.likelihoodTextPane.setText("Cannot display likelihood of this tree (" + Tools.getErrorMessage(e) + ")");
/*      */     }
/* 1262 */     return this.likelihoodTextPane;
/*      */   }
/*      */ 
/*      */   private JButton getSaveAllButton()
/*      */   {
/* 1271 */     if (this.exportButton == null) {
/* 1272 */       this.exportButton = new JButton();
/* 1273 */       this.exportButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 1274 */       this.exportButton.setMinimumSize(new Dimension(37, 37));
/* 1275 */       this.exportButton.setToolTipText("Export selected tree(s) as image file(s)");
/* 1276 */       this.exportButton.setContentAreaFilled(false);
/* 1277 */       this.exportButton.setIcon(MainFrame.imageImage);
/* 1278 */       this.exportButton.setMaximumSize(new Dimension(40, 40));
/* 1279 */       this.exportButton.addActionListener(new ActionListener() {
/*      */         public void actionPerformed(ActionEvent e) {
/* 1281 */           TreeViewer.this.exportSelectedTrees();
/*      */         }
/*      */       });
/*      */     }
/* 1285 */     return this.exportButton;
/*      */   }
/*      */ 
/*      */   private JButton getRemoveButton()
/*      */   {
/* 1294 */     if (this.removeButton == null) {
/* 1295 */       this.removeButton = new JButton();
/* 1296 */       this.removeButton.setText("Remove");
/* 1297 */       this.removeButton.addActionListener(new ActionListener() {
/*      */         public void actionPerformed(ActionEvent e) {
/* 1299 */           if (TreeViewer.this.treeList.getSelectedIndex() > -1) {
/* 1300 */             for (Object selection : TreeViewer.this.treeList.getSelectedValues()) {
/* 1301 */               ((Map)TreeViewer.this.loadedTrees.get(TreeViewer.this.currentParameters.label)).remove(selection);
/* 1302 */               TreeViewer.this.treeListModel.removeElement(selection);
/*      */             }
/*      */           }
/*      */         }
/*      */       });
/*      */     }
/* 1308 */     return this.removeButton;
/*      */   }
/*      */ 
/*      */   private JButton getRenameButton()
/*      */   {
/* 1317 */     if (this.renameButton == null) {
/* 1318 */       GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
/* 1319 */       gridBagConstraints1.gridx = 2;
/* 1320 */       gridBagConstraints1.fill = 2;
/* 1321 */       gridBagConstraints1.insets = new Insets(5, 5, 5, 5);
/* 1322 */       gridBagConstraints1.weightx = 1.0D;
/* 1323 */       gridBagConstraints1.gridy = 1;
/* 1324 */       this.filePanel.add(getClearButton(), gridBagConstraints1);
/* 1325 */       this.renameButton = new JButton();
/* 1326 */       this.renameButton.setText("Rename");
/* 1327 */       this.renameButton.addActionListener(new ActionListener() {
/*      */         public void actionPerformed(ActionEvent e) {
/* 1329 */           if (TreeViewer.this.treeList.getSelectedIndex() > -1) {
/* 1330 */             String selection = TreeViewer.this.treeList.getSelectedValue().toString();
/* 1331 */             Object newName = JOptionPane.showInputDialog("Rename tree :", selection);
/* 1332 */             if (newName != null) {
/* 1333 */               Tree tree = (Tree)((Map)TreeViewer.this.loadedTrees.get(TreeViewer.this.currentParameters.label)).get(selection);
/* 1334 */               tree.setName(newName.toString());
/* 1335 */               ((Map)TreeViewer.this.loadedTrees.get(TreeViewer.this.currentParameters.label)).remove(selection);
/* 1336 */               ((Map)TreeViewer.this.loadedTrees.get(TreeViewer.this.currentParameters.label)).put(newName.toString(), tree);
/* 1337 */               int pos = TreeViewer.this.treeListModel.indexOf(selection);
/* 1338 */               TreeViewer.this.treeListModel.removeElement(selection);
/* 1339 */               TreeViewer.this.treeListModel.add(pos, newName);
/* 1340 */               TreeViewer.this.treeList.setSelectedValue(newName, true);
/*      */             }
/*      */           }
/*      */         }
/*      */       });
/*      */     }
/* 1346 */     return this.renameButton;
/*      */   }
/*      */   private JPanel getSouthPanel() {
/* 1349 */     if (this.southPanel == null) {
/* 1350 */       this.southPanel = new JPanel();
/* 1351 */       this.southPanel.setLayout(new BorderLayout(0, 0));
/* 1352 */       this.southPanel.add(getBtnAncestralStatesReconstruction(), "North");
/* 1353 */       this.southPanel.add(getAncestralStatesPanel(), "Center");
/* 1354 */       getAncestralStatesPanel().setVisible(false);
/*      */     }
/* 1356 */     return this.southPanel;
/*      */   }
/*      */ 
/*      */   private JButton getBtnAncestralStatesReconstruction() {
/* 1360 */     if (this.btnAncestralStatesReconstruction == null) {
/* 1361 */       this.btnAncestralStatesReconstruction = new JButton("[+] Show ancestral states reconstruction [+]");
/* 1362 */       this.btnAncestralStatesReconstruction.setFont(new Font("Tahoma", 1, 12));
/* 1363 */       this.btnAncestralStatesReconstruction.addActionListener(new ActionListener() {
/*      */         public void actionPerformed(ActionEvent arg0) {
/* 1365 */           TreeViewer.this.showAncestralStates(!TreeViewer.this.showAncestralStates);
/*      */         }
/*      */       });
/*      */     }
/* 1369 */     return this.btnAncestralStatesReconstruction;
/*      */   }
/*      */ 
/*      */   private AncestralStatesPanel getAncestralStatesPanel() {
/* 1373 */     if (this.ancestralStatesPanel == null) {
/* 1374 */       this.ancestralStatesPanel = new AncestralStatesPanel();
/*      */     }
/* 1376 */     return this.ancestralStatesPanel;
/*      */   }
/*      */ 
/*      */   public class InodeList extends JDialog
/*      */   {
/*      */     JList list;
/*      */     JScrollPane scrollPane;
/*      */     Node newRoot;
/*      */ 
/*      */     public InodeList(JFrame parent, Node actualRoot)
/*      */     {
/*  423 */       super("Select the root", true);
/*  424 */       this.newRoot = actualRoot;
/*      */       try {
/*  426 */         this.list = new JList(((TreePanel)TreeViewer.this.treePanel).tree.getInodes().toArray());
/*  427 */         this.list.setSelectedValue(actualRoot, true);
/*  428 */         this.list.setSelectionMode(0);
/*  429 */         this.scrollPane = new JScrollPane(this.list);
/*  430 */         this.list.addListSelectionListener(new ListSelectionListener() {
/*      */           public void valueChanged(ListSelectionEvent e) {
/*  432 */             TreeViewer.InodeList.this.newRoot = ((Node)TreeViewer.InodeList.this.list.getSelectedValue());
/*  433 */             TreeViewer.InodeList.this.dispose();
/*      */           }
/*      */         });
/*  436 */         getContentPane().add(this.scrollPane);
/*  437 */         pack();
/*      */       } catch (Exception e) {
/*  439 */         e.printStackTrace();
/*  440 */         JOptionPane.showMessageDialog(this, Tools.getErrorPanel("Error", e), 
/*  441 */           "Cannot show the list of internal nodes", 0);
/*      */       }
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.TreeViewer
 * JD-Core Version:    0.6.2
 */