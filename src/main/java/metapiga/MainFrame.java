package metapiga;/*      */
/*      */ 
/*      */ import com.install4j.api.Util;
/*      */ import com.install4j.api.windows.SpecialFolder;
/*      */ import com.install4j.api.windows.WinFileSystem;
/*      */ import java.awt.BorderLayout;
/*      */ import java.awt.CardLayout;
/*      */ import java.awt.Color;
/*      */ import java.awt.Component;
/*      */
/*      */ import java.awt.Dimension;
/*      */ import java.awt.FileDialog;
/*      */ import java.awt.Font;
/*      */ import java.awt.GridBagConstraints;
/*      */ import java.awt.GridBagLayout;
/*      */ import java.awt.Insets;
/*      */ import java.awt.Point;
/*      */ import java.awt.Toolkit;
/*      */ import java.awt.datatransfer.DataFlavor;
/*      */ import java.awt.datatransfer.Transferable;
/*      */ import java.awt.dnd.DropTarget;
/*      */ import java.awt.dnd.DropTargetDragEvent;
/*      */ import java.awt.dnd.DropTargetDropEvent;
/*      */ import java.awt.dnd.DropTargetEvent;
/*      */ import java.awt.dnd.DropTargetListener;
/*      */ import java.awt.event.ActionEvent;
/*      */ import java.awt.event.ActionListener;
/*      */ import java.awt.event.KeyEvent;
/*      */ import java.awt.event.MouseAdapter;
/*      */ import java.awt.event.MouseEvent;
/*      */ import java.awt.event.WindowEvent;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.File;
/*      */ import java.io.FileReader;
/*      */ import java.io.FileWriter;
/*      */
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Set;
/*      */ import java.util.concurrent.ExecutionException;
/*      */ import java.util.concurrent.ExecutorService;
/*      */ import java.util.concurrent.Executors;
/*      */ import java.util.regex.Matcher;
/*      */ import java.util.regex.Pattern;
/*      */ import javax.swing.BorderFactory;
/*      */ import javax.swing.DefaultListModel;
/*      */ import javax.swing.ImageIcon;
/*      */ import javax.swing.JButton;
/*      */ import javax.swing.JDialog;
/*      */ import javax.swing.JFrame;
/*      */ import javax.swing.JLabel;
/*      */ import javax.swing.JList;
/*      */ import javax.swing.JMenu;
/*      */ import javax.swing.JMenuBar;
/*      */ import javax.swing.JMenuItem;
/*      */ import javax.swing.JOptionPane;
/*      */ import javax.swing.JPanel;
/*      */ import javax.swing.JProgressBar;
/*      */ import javax.swing.JScrollPane;
/*      */ import javax.swing.JSlider;
/*      */ import javax.swing.JSplitPane;
/*      */ import javax.swing.JTabbedPane;
/*      */ import javax.swing.JTextPane;
/*      */ import javax.swing.JToolBar;
/*      */
/*      */ import javax.swing.KeyStroke;
/*      */ import javax.swing.SwingWorker;
/*      */ import javax.swing.border.Border;
/*      */ import javax.swing.border.LineBorder;
/*      */ import javax.swing.event.ChangeEvent;
/*      */ import javax.swing.event.ChangeListener;
/*      */ import javax.swing.event.HyperlinkEvent;
/*      */ import javax.swing.event.HyperlinkEvent.EventType;
/*      */ import javax.swing.event.HyperlinkListener;
/*      */ import javax.swing.event.ListSelectionEvent;
/*      */ import javax.swing.event.ListSelectionListener;
/*      */ import javax.swing.text.html.HTMLEditorKit;
/*      */ import metapiga.io.FastaReader;
/*      */ import metapiga.io.NexusReader;
/*      */ import metapiga.io.NexusWriter;
/*      */ import metapiga.modelization.ComputeDistancesFrame;
/*      */ import metapiga.modelization.DatasetTriming;
/*      */
/*      */ import metapiga.modelization.data.DataType;
/*      */ import metapiga.monitors.SearchBatchGraphical;
/*      */ import metapiga.monitors.SearchOnceGraphical;
/*      */ import metapiga.optimization.DFO;
/*      */ import metapiga.parameters.Parameters;
/*      */
/*      */
/*      */ import metapiga.settings.SettingsAnalysisDialog;
/*      */ import metapiga.settings.SettingsDatasetDialog;
/*      */ import metapiga.trees.ConsensusTreeFrame;
/*      */ import metapiga.trees.Tree;
/*      */ import metapiga.trees.TreeGenerator;
/*      */ import metapiga.trees.TreeViewer;
/*      */ import metapiga.utilities.Tools;
/*      */ import org.apache.commons.io.FileUtils;
/*      */ 
/*      */ public class MainFrame extends JFrame
/*      */   implements DropTargetListener
/*      */ {
/*      */   private final MetaPIGA metapiga;
/*  114 */   private Parameters currentParameters = null;
/*  115 */   private int currentParametersPosition = -1;
/*      */   private WaitingLogo waitingLogo;
/*  117 */   private boolean selectingRun = false;
/*  118 */   public boolean busy = false;
/*      */   private DropTarget dropTarget;
/*  121 */   JMenuBar menuBar = new JMenuBar();
/*  122 */   public JMenu jMenuFile = new JMenu();
/*  123 */   public JMenuItem jMenuFileLoad = new JMenuItem();
/*  124 */   public JMenuItem jMenuFileSave = new JMenuItem();
/*  125 */   public JMenuItem jMenuFileSaveModified = new JMenuItem();
/*  126 */   public JMenuItem jMenuFileRemove = new JMenuItem();
/*  127 */   public JMenuItem jMenuFileExit = new JMenuItem();
/*  128 */   public JMenu jMenuDataset = new JMenu();
/*  129 */   public JMenuItem jMenuDatasetCheckAmbiguities = new JMenuItem();
/*  130 */   public JMenuItem jMenuDatasetCheckRedundancy = new JMenuItem();
/*  131 */   public JMenuItem jMenuDatasetCheckSaturation = new JMenuItem();
/*  132 */   public JMenuItem jMenuDatasetTrimming = new JMenuItem();
/*  133 */   public JMenuItem jMenuDatasetSettings = new JMenuItem();
/*  134 */   public JMenu jMenuSearch = new JMenu();
/*  135 */   public JMenuItem jMenuSearchSettings = new JMenuItem();
/*  136 */   public JMenuItem jMenuSearchRun = new JMenuItem();
/*  137 */   public JMenu jMenuBatch = new JMenu();
/*  138 */   public JMenuItem jMenuBatchSave = new JMenuItem();
/*  139 */   public JMenuItem jMenuBatchClose = new JMenuItem();
/*  140 */   public JMenuItem jMenuBatchDuplicateRun = new JMenuItem();
/*  141 */   public JMenuItem jMenuBatchAssociateParameters = new JMenuItem();
/*  142 */   public JMenuItem jMenuBatchRun = new JMenuItem();
/*  143 */   public JMenu jMenuTools = new JMenu();
/*  144 */   public JMenuItem jMenuToolsTreeViewer = new JMenuItem();
/*  145 */   public JMenuItem jMenuToolsTreeGenerator = new JMenuItem();
/*  146 */   public JMenuItem jMenuToolsAncestralStates = new JMenuItem();
/*  147 */   public JMenuItem jMenuToolsConsensus = new JMenuItem();
/*  148 */   public JMenuItem jMenuToolsDistances = new JMenuItem();
/*  149 */   public JMenuItem jMenuToolsMemorySettings = new JMenuItem();
/*  150 */   public JMenuItem jMenuToolsTest = new JMenuItem();
/*  151 */   public JMenu jMenuHelp = new JMenu();
/*  152 */   public JMenuItem jMenuHelpAbout = new JMenuItem();
/*  153 */   public JMenuItem jMenuHelpChangeLog = new JMenuItem();
/*  154 */   public JMenuItem jMenuHelpSystemInfo = new JMenuItem();
/*  155 */   public JMenuItem jMenuHelpManual = new JMenuItem();
/*  156 */   public JMenuItem jMenuHelpSupport = new JMenuItem();
/*      */ 
/*  158 */   JToolBar toolBar = new JToolBar();
/*  159 */   public JButton loadButton = new JButton();
/*  160 */   public JButton saveButton = new JButton();
/*  161 */   public JButton removeButton = new JButton();
/*  162 */   public JButton batchSaveButton = new JButton();
/*  163 */   public JButton batchDuplicateButton = new JButton();
/*  164 */   public JButton batchAssociateButton = new JButton();
/*  165 */   public JButton datasetButton = new JButton();
/*  166 */   public JButton settingsButton = new JButton();
/*  167 */   public JButton startRunButton = new JButton();
/*  168 */   public JButton startBatchButton = new JButton();
/*  169 */   public JButton treeViewerButton = new JButton();
/*      */ 
/*  171 */   public static final ImageIcon imageAbout = new ImageIcon(MainFrame.class.getResource("metapiga/resources/about.png"));
/*  172 */   public static final ImageIcon imageAssociateSettings = new ImageIcon(MainFrame.class.getResource("metapiga/resources/associate.png"));
/*  173 */   public static final ImageIcon imageBranchLength = new ImageIcon(MainFrame.class.getResource("metapiga/resources/branchLength.png"));
/*  174 */   public static final ImageIcon imageClose = new ImageIcon(MainFrame.class.getResource("metapiga/resources/close.png"));
/*  175 */   public static final ImageIcon imageDataset = new ImageIcon(MainFrame.class.getResource("metapiga/resources/dataset.png"));
/*  176 */   public static final ImageIcon imageDuplicateRun = new ImageIcon(MainFrame.class.getResource("metapiga/resources/duplicate.png"));
/*  177 */   public static final ImageIcon imageTreeViewer = new ImageIcon(MainFrame.class.getResource("metapiga/resources/eye.png"));
/*  178 */   public static final ImageIcon imageTreeViewerAll = new ImageIcon(MainFrame.class.getResource("metapiga/resources/eyeAll.png"));
/*  179 */   public static final ImageIcon imageTreeViewerOne = new ImageIcon(MainFrame.class.getResource("metapiga/resources/eyeOne.png"));
/*  180 */   public static final ImageIcon imageHelp = new ImageIcon(MainFrame.class.getResource("metapiga/resources/help.png"));
/*  181 */   public static final ImageIcon imageHistoCumulative = new ImageIcon(MainFrame.class.getResource("metapiga/resources/histo_cumulative.png"));
/*  182 */   public static final ImageIcon imageHistoOverlapping = new ImageIcon(MainFrame.class.getResource("metapiga/resources/histo_overlapping.png"));
/*  183 */   public static final ImageIcon imageImage = new ImageIcon(MainFrame.class.getResource("metapiga/resources/image.png"));
/*  184 */   public static final ImageIcon imageInodes = new ImageIcon(MainFrame.class.getResource("metapiga/resources/inodes.png"));
/*  185 */   public static final ImageIcon imageLANE = new ImageIcon(MainFrame.class.getResource("metapiga/resources/lane_40.png"));
/*  186 */   public static final ImageIcon imageMatrix = new ImageIcon(MainFrame.class.getResource("metapiga/resources/matrix_99.png"));
/*  187 */   public static final ImageIcon imageMemory = new ImageIcon(MetaPIGA.class.getResource("metapiga/resources/memory.png"));
/*  188 */   public static final ImageIcon imageMetapiga = new ImageIcon(MetaPIGA.class.getResource("metapiga/resources/metapiga.png"));
/*  189 */   public static final ImageIcon imageNext = new ImageIcon(MetaPIGA.class.getResource("metapiga/resources/next.png"));
/*  190 */   public static final ImageIcon imageOpenFile = new ImageIcon(MainFrame.class.getResource("metapiga/resources/openFile.png"));
/*  191 */   public static final ImageIcon imagePhylogram = new ImageIcon(MetaPIGA.class.getResource("metapiga/resources/phylo.png"));
/*  192 */   public static final ImageIcon imagePrecedent = new ImageIcon(MetaPIGA.class.getResource("metapiga/resources/precedent.png"));
/*  193 */   public static final ImageIcon imagePrinter = new ImageIcon(MainFrame.class.getResource("metapiga/resources/printer.png"));
/*  194 */   public static final ImageIcon imageRadialCladogram = new ImageIcon(MetaPIGA.class.getResource("metapiga/resources/radialClad.png"));
/*  195 */   public static final ImageIcon imageRectangularCladogram = new ImageIcon(MetaPIGA.class.getResource("metapiga/resources/rectClad.png"));
/*  196 */   public static final ImageIcon imageRoot = new ImageIcon(MetaPIGA.class.getResource("metapiga/resources/root.png"));
/*  197 */   public static final ImageIcon imageSaveAll = new ImageIcon(MainFrame.class.getResource("metapiga/resources/saveAll.png"));
/*  198 */   public static final ImageIcon imageSaveBatch = new ImageIcon(MainFrame.class.getResource("metapiga/resources/saveBatch.png"));
/*  199 */   public static final ImageIcon imageSaveFile = new ImageIcon(MainFrame.class.getResource("metapiga/resources/saveFile.png"));
/*  200 */   public static final ImageIcon imageSaveOne = new ImageIcon(MainFrame.class.getResource("metapiga/resources/saveOne.png"));
/*  201 */   public static final ImageIcon imageSettings = new ImageIcon(MainFrame.class.getResource("metapiga/resources/settings.png"));
/*  202 */   public static final ImageIcon imageSlantedCladogram = new ImageIcon(MetaPIGA.class.getResource("metapiga/resources/slantedClad.png"));
/*  203 */   public static final ImageIcon imageStartRun = new ImageIcon(MainFrame.class.getResource("metapiga/resources/start.png"));
/*  204 */   public static final ImageIcon imageStartBatch = new ImageIcon(MainFrame.class.getResource("metapiga/resources/startBatch.png"));
/*  205 */   public static final ImageIcon imageSupport = new ImageIcon(MainFrame.class.getResource("metapiga/resources/support.png"));
/*  206 */   public static final ImageIcon imageTrimal = new ImageIcon(MainFrame.class.getResource("metapiga/resources/trimal_80.png"));
/*  207 */   public static final ImageIcon imageUpdater = new ImageIcon(MainFrame.class.getResource("metapiga/resources/updater_32.png"));
/*      */   JPanel contentPane;
/*  210 */   public static JLabel statusBar = new JLabel();
/*  211 */   private static JProgressBar progressBar = new JProgressBar();
/*  212 */   BorderLayout borderLayout1 = new BorderLayout();
/*  213 */   JPanel nexusNoWordWrapPanel = new JPanel();
/*  214 */   CardLayout nexusNoWordWrapLayout = new CardLayout();
/*  215 */   JPanel dataNoWordWrapPanel = new JPanel();
/*  216 */   CardLayout dataNoWordWrapLayout = new CardLayout();
/*  217 */   private Map<Parameters, String> availableCardLayouts = new HashMap();
/*  218 */   private int layoutIndex = 0;
/*  219 */   JPanel southPanel = new JPanel();
/*      */   Border border1;
/*  221 */   GridBagLayout gridBagLayout1 = new GridBagLayout();
/*  222 */   private final JPanel bottomSplitPanel = new JPanel();
/*  223 */   private final JButton checkDatasetButton = new JButton();
/*  224 */   JTabbedPane datasetPanel = new JTabbedPane();
/*  225 */   JScrollPane nexusScrollPane = new JScrollPane();
/*  226 */   JTextPane nexusTextPane = new JTextPane();
/*  227 */   JScrollPane dataScrollPane = new JScrollPane();
/*  228 */   JTextPane dataTextPane = new JTextPane();
/*  229 */   JLabel laneLabel = new JLabel();
/*  230 */   JPanel northPanel = new JPanel();
/*  231 */   GridBagLayout gridBagLayout2 = new GridBagLayout();
/*  232 */   JSplitPane datasetSplitPane = new JSplitPane();
/*  233 */   JScrollPane settingsScrollPane = new JScrollPane();
/*  234 */   JScrollPane batchListScrollPane = new JScrollPane();
/*  235 */   JTextPane settingsTextPane = new JTextPane();
/*      */   JList batchList;
/*  237 */   JPanel westPanel = new JPanel();
/*  238 */   JPanel listControlPanel = new JPanel();
/*  239 */   public static JPanel splashPanel = new JPanel();
/*  240 */   CardLayout splashLayout = new CardLayout();
/*  241 */   JLabel splashLabel = new JLabel();
/*  242 */   JSlider positionSlider = new JSlider();
/*  243 */   JLabel positionLabel = new JLabel("Use this slider to change the selected dataset priority in a batch run");
/*      */ 
/*      */   public MainFrame(MetaPIGA metapiga)
/*      */   {
/*  247 */     this.metapiga = metapiga;
/*  248 */     this.batchList = new JList(metapiga.parameters);
/*  249 */     MetaPIGA.progressHandling = new ProgressHandling(progressBar);
/*  250 */     enableEvents(64L);
/*      */     try {
/*  252 */       setIconImage(Tools.getScaledIcon(imageMetapiga, 32).getImage());
/*  253 */       if (this.dropTarget == null) this.dropTarget = new DropTarget(this, this);
/*  254 */       jbInit();
/*      */     } catch (Exception e) {
/*  256 */       e.printStackTrace();
/*      */     }
/*      */   }
/*      */ 
/*      */   private void jbInit() throws Exception
/*      */   {
/*  262 */     this.jMenuFile.setText("File");
/*  263 */     this.jMenuFile.setMnemonic(70);
/*  264 */     this.jMenuFileLoad.setText("Load a data file (Nexus or Fasta format)");
/*  265 */     this.jMenuFileLoad.setIcon(Tools.getScaledIcon(imageOpenFile, 16));
/*  266 */     this.jMenuFileLoad.setMnemonic(76);
/*  267 */     this.jMenuFileLoad.setAccelerator(KeyStroke.getKeyStroke(
/*  268 */       76, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
/*  269 */     this.jMenuFileSave.setText("Save selected dataset to Nexus");
/*  270 */     this.jMenuFileSave.setIcon(Tools.getScaledIcon(imageSaveFile, 16));
/*  271 */     this.jMenuFileSave.setMnemonic(83);
/*  272 */     this.jMenuFileSave.setAccelerator(KeyStroke.getKeyStroke(
/*  273 */       83, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
/*  274 */     this.jMenuFileSave.setEnabled(false);
/*  275 */     this.jMenuFileSaveModified.setText("Save modified dataset to Nexus");
/*  276 */     this.jMenuFileSaveModified.setIcon(Tools.getScaledIcon(imageSaveFile, 16));
/*  277 */     this.jMenuFileSaveModified.setMnemonic(77);
/*  278 */     this.jMenuFileSaveModified.setAccelerator(KeyStroke.getKeyStroke(
/*  279 */       77, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
/*  280 */     this.jMenuFileSaveModified.setEnabled(false);
/*  281 */     this.jMenuFileRemove.setText("Close selected dataset");
/*  282 */     this.jMenuFileRemove.setMnemonic(67);
/*  283 */     this.jMenuFileRemove.setIcon(Tools.getScaledIcon(imageClose, 16));
/*  284 */     this.jMenuFileRemove.setAccelerator(KeyStroke.getKeyStroke(
/*  285 */       87, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
/*  286 */     this.jMenuFileRemove.setEnabled(false);
/*  287 */     this.jMenuFileExit.setText("Exit");
/*  288 */     this.jMenuFileExit.setMnemonic(69);
/*  289 */     this.jMenuDataset.setText("Dataset");
/*  290 */     this.jMenuDataset.setMnemonic(68);
/*  291 */     this.jMenuDatasetCheckAmbiguities.setText("Check for Ambiguous sequences");
/*  292 */     this.jMenuDatasetCheckAmbiguities.setMnemonic(65);
/*  293 */     this.jMenuDatasetCheckAmbiguities.setEnabled(false);
/*  294 */     this.jMenuDatasetCheckRedundancy.setText("Check for Redundancy");
/*  295 */     this.jMenuDatasetCheckRedundancy.setMnemonic(82);
/*  296 */     this.jMenuDatasetCheckRedundancy.setEnabled(false);
/*  297 */     this.jMenuDatasetCheckSaturation.setText("Check for Saturation");
/*  298 */     this.jMenuDatasetCheckSaturation.setMnemonic(83);
/*  299 */     this.jMenuDatasetCheckSaturation.setEnabled(false);
/*  300 */     this.jMenuDatasetTrimming.setText("Trimming");
/*  301 */     this.jMenuDatasetTrimming.setMnemonic(84);
/*  302 */     this.jMenuDatasetTrimming.setIcon(Tools.getScaledIcon(imageTrimal, 16));
/*  303 */     this.jMenuDatasetTrimming.setEnabled(false);
/*  304 */     this.jMenuDatasetSettings.setText("Dataset settings");
/*  305 */     this.jMenuDatasetSettings.setMnemonic(68);
/*  306 */     this.jMenuDatasetSettings.setIcon(Tools.getScaledIcon(imageDataset, 16));
/*  307 */     this.jMenuDatasetSettings.setAccelerator(KeyStroke.getKeyStroke(
/*  308 */       68, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
/*  309 */     this.jMenuDatasetSettings.setEnabled(false);
/*  310 */     this.jMenuSearch.setText("Search");
/*  311 */     this.jMenuSearch.setMnemonic(83);
/*  312 */     this.jMenuSearchSettings.setText("Analysis settings");
/*  313 */     this.jMenuSearchSettings.setMnemonic(65);
/*  314 */     this.jMenuSearchSettings.setIcon(Tools.getScaledIcon(imageSettings, 16));
/*  315 */     this.jMenuSearchSettings.setAccelerator(KeyStroke.getKeyStroke(
/*  316 */       65, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
/*  317 */     this.jMenuSearchSettings.setEnabled(false);
/*  318 */     this.jMenuSearchRun.setText("Run");
/*  319 */     this.jMenuSearchRun.setMnemonic(82);
/*  320 */     this.jMenuSearchRun.setIcon(Tools.getScaledIcon(imageStartRun, 16));
/*  321 */     this.jMenuSearchRun.setAccelerator(KeyStroke.getKeyStroke(
/*  322 */       82, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
/*  323 */     this.jMenuSearchRun.setEnabled(false);
/*  324 */     this.jMenuBatch.setText("Batch");
/*  325 */     this.jMenuBatch.setMnemonic(66);
/*  326 */     this.jMenuBatchSave.setText("Save all datasets to a batch Nexus file");
/*  327 */     this.jMenuBatchSave.setMnemonic(83);
/*  328 */     this.jMenuBatchSave.setIcon(Tools.getScaledIcon(imageSaveBatch, 16));
/*  329 */     this.jMenuBatchSave.setAccelerator(KeyStroke.getKeyStroke(
/*  330 */       83, 1 + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
/*  331 */     this.jMenuBatchSave.setEnabled(false);
/*  332 */     this.jMenuBatchClose.setText("Close all datasets");
/*  333 */     this.jMenuBatchClose.setMnemonic(67);
/*  334 */     this.jMenuBatchClose.setAccelerator(KeyStroke.getKeyStroke(
/*  335 */       87, 1 + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
/*  336 */     this.jMenuBatchClose.setEnabled(false);
/*  337 */     this.jMenuBatchDuplicateRun.setText("Duplicate selected dataset");
/*  338 */     this.jMenuBatchDuplicateRun.setMnemonic(68);
/*  339 */     this.jMenuBatchDuplicateRun.setIcon(Tools.getScaledIcon(imageDuplicateRun, 16));
/*  340 */     this.jMenuBatchDuplicateRun.setAccelerator(KeyStroke.getKeyStroke(
/*  341 */       85, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
/*  342 */     this.jMenuBatchDuplicateRun.setEnabled(false);
/*  343 */     this.jMenuBatchAssociateParameters.setText("Associate selected dataset analysis settings");
/*  344 */     this.jMenuBatchAssociateParameters.setMnemonic(65);
/*  345 */     this.jMenuBatchAssociateParameters.setIcon(Tools.getScaledIcon(imageAssociateSettings, 16));
/*  346 */     this.jMenuBatchAssociateParameters.setAccelerator(KeyStroke.getKeyStroke(
/*  347 */       79, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
/*  348 */     this.jMenuBatchAssociateParameters.setEnabled(false);
/*  349 */     this.jMenuBatchRun.setText("Run all datasets in a batch");
/*  350 */     this.jMenuBatchRun.setMnemonic(82);
/*  351 */     this.jMenuBatchRun.setIcon(Tools.getScaledIcon(imageStartBatch, 16));
/*  352 */     this.jMenuBatchRun.setAccelerator(KeyStroke.getKeyStroke(
/*  353 */       82, 1 + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
/*  354 */     this.jMenuBatchRun.setEnabled(false);
/*  355 */     this.jMenuTools.setText("Tools");
/*  356 */     this.jMenuTools.setMnemonic(84);
/*  357 */     this.jMenuToolsMemorySettings.setText("Memory settings");
/*  358 */     this.jMenuToolsMemorySettings.setMnemonic(77);
/*  359 */     this.jMenuToolsMemorySettings.setIcon(Tools.getScaledIcon(imageMemory, 16));
/*  360 */     this.jMenuToolsMemorySettings.setAccelerator(KeyStroke.getKeyStroke(
/*  361 */       77, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
/*  362 */     this.jMenuToolsTreeViewer.setText("Tree Viewer");
/*  363 */     this.jMenuToolsTreeViewer.setMnemonic(86);
/*  364 */     this.jMenuToolsTreeViewer.setIcon(Tools.getScaledIcon(imageTreeViewer, 16));
/*  365 */     this.jMenuToolsTreeViewer.setAccelerator(KeyStroke.getKeyStroke(
/*  366 */       84, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
/*  367 */     this.jMenuToolsTreeGenerator.setText("Tree Generator");
/*  368 */     this.jMenuToolsTreeGenerator.setMnemonic(71);
/*  369 */     this.jMenuToolsTreeGenerator.setIcon(Tools.getScaledIcon(imageRectangularCladogram, 16));
/*  370 */     this.jMenuToolsAncestralStates.setText("Ancestral states reconstruction");
/*  371 */     this.jMenuToolsAncestralStates.setMnemonic(65);
/*  372 */     this.jMenuToolsAncestralStates.setIcon(Tools.getScaledIcon(imageHistoCumulative, 16));
/*  373 */     this.jMenuToolsConsensus.setText("Build consensus tree");
/*  374 */     this.jMenuToolsConsensus.setMnemonic(66);
/*  375 */     this.jMenuToolsConsensus.setIcon(Tools.getScaledIcon(imageRectangularCladogram, 16));
/*  376 */     this.jMenuToolsDistances.setText("Compute distances");
/*  377 */     this.jMenuToolsDistances.setMnemonic(67);
/*  378 */     this.jMenuToolsDistances.setIcon(Tools.getScaledIcon(imageMatrix, 16));
/*  379 */     this.jMenuToolsTest.setText("Test");
/*  380 */     this.jMenuHelp.setText("Help");
/*  381 */     this.jMenuHelp.setMnemonic(72);
/*  382 */     this.jMenuHelpAbout.setText("About MetaPIGA");
/*  383 */     this.jMenuHelpAbout.setMnemonic(65);
/*  384 */     this.jMenuHelpAbout.setIcon(Tools.getScaledIcon(imageMetapiga, 16));
/*  385 */     this.jMenuHelpChangeLog.setText("Change log");
/*  386 */     this.jMenuHelpChangeLog.setMnemonic(67);
/*  387 */     this.jMenuHelpChangeLog.setIcon(Tools.getScaledIcon(imageUpdater, 16));
/*  388 */     this.jMenuHelpSystemInfo.setText("System information");
/*  389 */     this.jMenuHelpSystemInfo.setMnemonic(83);
/*  390 */     this.jMenuHelpSystemInfo.setIcon(Tools.getScaledIcon(imageAbout, 16));
/*  391 */     this.jMenuHelpManual.setText("User manual");
/*  392 */     this.jMenuHelpManual.setMnemonic(85);
/*  393 */     this.jMenuHelpManual.setIcon(Tools.getScaledIcon(imageHelp, 16));
/*  394 */     this.jMenuHelpSupport.setText("Online support");
/*  395 */     this.jMenuHelpSupport.setMnemonic(79);
/*  396 */     this.jMenuHelpSupport.setIcon(Tools.getScaledIcon(imageSupport, 16));
/*      */ 
/*  398 */     this.southPanel.setMinimumSize(new Dimension(100, 15));
/*  399 */     statusBar.setMaximumSize(new Dimension(10000, 10000));
/*  400 */     statusBar.setPreferredSize(new Dimension(124, 19));
/*  401 */     this.nexusTextPane.setBackground(Color.black);
/*  402 */     this.nexusTextPane.setFont(new Font("Courier New", 0, 12));
/*  403 */     this.nexusTextPane.setForeground(Color.green);
/*  404 */     this.nexusTextPane.setOpaque(true);
/*  405 */     this.nexusTextPane.setRequestFocusEnabled(true);
/*  406 */     this.nexusTextPane.setCaretColor(Color.black);
/*      */ 
/*  408 */     this.nexusTextPane.setEditable(false);
/*  409 */     this.dataTextPane.setBackground(Color.black);
/*  410 */     this.dataTextPane.setFont(new Font("Courier New", 0, 12));
/*  411 */     this.dataTextPane.setForeground(Color.green);
/*  412 */     this.dataTextPane.setOpaque(true);
/*  413 */     this.dataTextPane.setRequestFocusEnabled(true);
/*  414 */     this.dataTextPane.setCaretColor(Color.black);
/*      */ 
/*  416 */     this.dataTextPane.setEditable(false);
/*  417 */     this.nexusScrollPane.setHorizontalScrollBarPolicy(32);
/*  418 */     this.nexusScrollPane.setVerticalScrollBarPolicy(22);
/*  419 */     this.dataScrollPane.setHorizontalScrollBarPolicy(32);
/*  420 */     this.dataScrollPane.setVerticalScrollBarPolicy(22);
/*  421 */     this.datasetPanel.setTabPlacement(3);
/*  422 */     this.datasetPanel.addTab("Nexus data matrix", this.nexusScrollPane);
/*  423 */     this.datasetPanel.addTab("MetaPIGA data matrix", this.dataScrollPane);
/*  424 */     this.northPanel.setLayout(this.gridBagLayout2);
/*  425 */     this.settingsTextPane.setEditorKit(new HTMLEditorKit());
/*  426 */     this.settingsTextPane.setBackground(Color.black);
/*  427 */     this.settingsTextPane.setFont(new Font("Geneva", 0, 14));
/*  428 */     this.settingsTextPane.setForeground(Color.green);
/*  429 */     this.settingsTextPane.setOpaque(true);
/*  430 */     this.settingsTextPane.setCaretColor(Color.black);
/*  431 */     this.settingsTextPane.setEditable(false);
/*  432 */     this.settingsTextPane.addHyperlinkListener(new HyperlinkListener() {
/*      */       public void hyperlinkUpdate(HyperlinkEvent e) {
/*  434 */         if (e.getEventType() == EventType.ACTIVATED) {
/*  435 */           Object newLabel = JOptionPane.showInputDialog(null, "New label for current selection", "Change run label", 3, Tools.getScaledIcon(MainFrame.imageDataset, 64), null, MainFrame.this.currentParameters.label);
/*  436 */           if (newLabel != null) {
/*  437 */             MainFrame.this.currentParameters.label = newLabel.toString();
/*  438 */             MainFrame.this.checkRunLabels();
/*  439 */             MainFrame.this.settingsTextPane.setText(MainFrame.this.getSettingsHTMLText());
/*  440 */             MainFrame.this.settingsTextPane.setCaretPosition(0);
/*  441 */             MainFrame.this.batchList.repaint();
/*      */           }
/*      */         }
/*      */       }
/*      */     });
/*  446 */     this.batchList.setBackground(Color.black);
/*  447 */     this.batchList.setFont(new Font("Geneva", 0, 14));
/*  448 */     this.batchList.setForeground(Color.green);
/*  449 */     this.batchList.setSelectionMode(0);
/*  450 */     this.batchList.addListSelectionListener(new ListSelectionListener() {
/*      */       public void valueChanged(ListSelectionEvent e) {
/*  452 */         if (!e.getValueIsAdjusting())
/*  453 */           MainFrame.this.showRun();
/*      */       }
/*      */     });
/*  457 */     this.settingsScrollPane.setHorizontalScrollBarPolicy(32);
/*  458 */     this.settingsScrollPane.setVerticalScrollBarPolicy(22);
/*  459 */     this.batchListScrollPane.setHorizontalScrollBarPolicy(31);
/*  460 */     this.batchListScrollPane.setBorder(new LineBorder(Color.black, 1, false));
/*  461 */     this.batchListScrollPane.setVerticalScrollBarPolicy(20);
/*  462 */     this.settingsScrollPane.getViewport().add(this.settingsTextPane);
/*  463 */     this.batchListScrollPane.getViewport().add(this.batchList);
/*  464 */     this.westPanel.setLayout(new BorderLayout());
/*  465 */     GridBagLayout gridBagLayout = new GridBagLayout();
/*  466 */     this.listControlPanel.setLayout(gridBagLayout);
/*  467 */     this.westPanel.add(this.listControlPanel, "North");
/*  468 */     this.positionSlider.setMinimum(0);
/*  469 */     this.positionSlider.setMaximum(0);
/*  470 */     this.positionSlider.setMinorTickSpacing(1);
/*  471 */     this.positionSlider.setPaintLabels(false);
/*  472 */     this.positionSlider.setPaintTicks(true);
/*  473 */     this.positionSlider.setSnapToTicks(true);
/*  474 */     this.positionSlider.setEnabled(false);
/*  475 */     this.positionSlider.addChangeListener(new ChangeListener() {
/*      */       public void stateChanged(ChangeEvent e) {
/*  477 */         JSlider source = (JSlider)e.getSource();
/*  478 */         if ((!source.getValueIsAdjusting()) && (!MainFrame.this.selectingRun))
/*  479 */           MainFrame.this.moveRun(MainFrame.this.positionSlider.getValue());
/*      */       }
/*      */     });
/*  483 */     GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
/*  484 */     gridBagConstraints_2.insets = new Insets(0, 5, 0, 0);
/*  485 */     gridBagConstraints_2.weighty = 0.0D;
/*  486 */     gridBagConstraints_2.weightx = 1.0D;
/*  487 */     gridBagConstraints_2.fill = 1;
/*  488 */     gridBagConstraints_2.gridy = 1;
/*  489 */     gridBagConstraints_2.gridx = 0;
/*  490 */     this.listControlPanel.add(this.positionSlider, gridBagConstraints_2);
/*  491 */     GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
/*  492 */     gridBagConstraints_1.insets = new Insets(0, 5, 0, 0);
/*  493 */     gridBagConstraints_1.weighty = 0.0D;
/*  494 */     gridBagConstraints_1.weightx = 1.0D;
/*  495 */     gridBagConstraints_1.fill = 1;
/*  496 */     gridBagConstraints_1.gridy = 0;
/*  497 */     gridBagConstraints_1.gridx = 0;
/*  498 */     this.listControlPanel.add(this.positionLabel, gridBagConstraints_1);
/*  499 */     splashPanel.setLayout(this.splashLayout);
/*  500 */     splashPanel.setBackground(Color.BLACK);
/*  501 */     this.splashLabel.setIcon(WaitingLogo.imageRest);
/*  502 */     splashPanel.add(this.splashLabel, "0");
/*  503 */     this.westPanel.add(splashPanel, "South");
/*  504 */     this.westPanel.add(this.batchListScrollPane, "Center");
/*  505 */     this.datasetSplitPane.setOrientation(0);
/*  506 */     this.datasetSplitPane.add(this.settingsScrollPane, "top");
/*  507 */     this.datasetSplitPane.setResizeWeight(0.4D);
/*  508 */     this.datasetSplitPane.add(this.bottomSplitPanel, "bottom");
/*  509 */     this.checkDatasetButton.setText("Test your dataset for redundancy and/or saturation problems, and apply automated trimming");
/*  510 */     this.checkDatasetButton.setIcon(Tools.getScaledIcon(imageTrimal, 16));
/*  511 */     this.checkDatasetButton.setFont(new Font("Tahoma", 1, 12));
/*  512 */     this.checkDatasetButton.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent arg0) {
/*  514 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  516 */             MainFrame.this.checkDataset(DatasetTriming.DatasetCheck.ALL);
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  521 */     JPanel checkDatasetPanel = new JPanel();
/*  522 */     checkDatasetPanel.setBackground(Color.BLACK);
/*  523 */     this.checkDatasetButton.setBackground(Color.BLACK);
/*  524 */     checkDatasetPanel.add(this.checkDatasetButton);
/*  525 */     this.bottomSplitPanel.setLayout(new BorderLayout());
/*  526 */     this.bottomSplitPanel.add(checkDatasetPanel, "North");
/*  527 */     this.bottomSplitPanel.add(this.datasetPanel, "Center");
/*  528 */     this.jMenuFile.add(this.jMenuFileLoad);
/*  529 */     this.jMenuFile.add(this.jMenuFileSave);
/*  530 */     this.jMenuFile.add(this.jMenuFileSaveModified);
/*  531 */     this.jMenuFile.add(this.jMenuFileRemove);
/*  532 */     this.jMenuFile.add(this.jMenuFileExit);
/*  533 */     this.jMenuDataset.add(this.jMenuDatasetCheckAmbiguities);
/*  534 */     this.jMenuDataset.add(this.jMenuDatasetCheckRedundancy);
/*  535 */     this.jMenuDataset.add(this.jMenuDatasetCheckSaturation);
/*  536 */     this.jMenuDataset.add(this.jMenuDatasetTrimming);
/*  537 */     this.jMenuDataset.add(this.jMenuDatasetSettings);
/*  538 */     this.jMenuSearch.add(this.jMenuSearchSettings);
/*  539 */     this.jMenuSearch.add(this.jMenuSearchRun);
/*  540 */     this.jMenuBatch.add(this.jMenuBatchSave);
/*  541 */     this.jMenuBatch.add(this.jMenuBatchClose);
/*  542 */     this.jMenuBatch.add(this.jMenuBatchDuplicateRun);
/*  543 */     this.jMenuBatch.add(this.jMenuBatchAssociateParameters);
/*  544 */     this.jMenuBatch.add(this.jMenuBatchRun);
/*  545 */     this.jMenuTools.add(this.jMenuToolsTreeViewer);
/*  546 */     this.jMenuTools.add(this.jMenuToolsTreeGenerator);
/*  547 */     this.jMenuTools.add(this.jMenuToolsAncestralStates);
/*  548 */     this.jMenuTools.add(this.jMenuToolsConsensus);
/*  549 */     this.jMenuTools.add(this.jMenuToolsDistances);
/*  550 */     this.jMenuTools.add(this.jMenuToolsMemorySettings);
/*      */ 
/*  552 */     this.jMenuHelp.add(this.jMenuHelpAbout);
/*  553 */     this.jMenuHelp.add(this.jMenuHelpChangeLog);
/*  554 */     this.jMenuHelp.add(this.jMenuHelpSystemInfo);
/*  555 */     this.jMenuHelp.add(this.jMenuHelpManual);
/*      */ 
/*  557 */     this.menuBar.add(this.jMenuFile);
/*  558 */     this.menuBar.add(this.jMenuDataset);
/*  559 */     this.menuBar.add(this.jMenuSearch);
/*  560 */     this.menuBar.add(this.jMenuBatch);
/*  561 */     this.menuBar.add(this.jMenuTools);
/*  562 */     this.menuBar.add(this.jMenuHelp);
/*  563 */     setJMenuBar(this.menuBar);
/*      */ 
/*  565 */     this.jMenuFileLoad.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  567 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  569 */             MainFrame.this.loadDataFile();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  575 */     this.jMenuFileSave.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  577 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  579 */             MainFrame.this.saveNexusFile(false);
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  585 */     this.jMenuFileSaveModified.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  587 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  589 */             MainFrame.this.saveNexusFile(true);
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  595 */     this.jMenuFileRemove.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  597 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  599 */             MainFrame.this.closeNexusFile();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  605 */     this.jMenuFileExit.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  607 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  609 */             MainFrame.this.exit();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  615 */     this.jMenuDatasetCheckAmbiguities.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  617 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  619 */             MainFrame.this.checkDataset(DatasetTriming.DatasetCheck.AMBIGUITIES);
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  625 */     this.jMenuDatasetCheckRedundancy.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  627 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  629 */             MainFrame.this.checkDataset(DatasetTriming.DatasetCheck.REDUNDANCY);
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  635 */     this.jMenuDatasetCheckSaturation.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  637 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  639 */             MainFrame.this.checkDataset(DatasetTriming.DatasetCheck.SATURATION);
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  645 */     this.jMenuDatasetTrimming.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  647 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  649 */             MainFrame.this.checkDataset(DatasetTriming.DatasetCheck.TRIMMING);
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  655 */     this.jMenuDatasetSettings.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  657 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  659 */             MainFrame.this.datasetSettings();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  665 */     this.jMenuSearchSettings.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  667 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  669 */             MainFrame.this.analysisSettings();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  675 */     this.jMenuSearchRun.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  677 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  679 */             MainFrame.this.runSelection();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  685 */     this.jMenuBatchSave.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  687 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  689 */             MainFrame.this.saveBatch();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  695 */     this.jMenuBatchClose.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  697 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  699 */             MainFrame.this.closeBatch();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  705 */     this.jMenuBatchDuplicateRun.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  707 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  709 */             MainFrame.this.duplicateRun();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  715 */     this.jMenuBatchAssociateParameters.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  717 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  719 */             MainFrame.this.associateParameters();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  725 */     this.jMenuBatchRun.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  727 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  729 */             MainFrame.this.runBatch();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  735 */     this.jMenuToolsMemorySettings.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  737 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  739 */             MainFrame.this.memorySettings();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  745 */     this.jMenuToolsTreeViewer.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  747 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  749 */             MainFrame.this.treeViewer();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  755 */     this.jMenuToolsTreeGenerator.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  757 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  759 */             MainFrame.this.treeGenerator();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  765 */     this.jMenuToolsAncestralStates.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  767 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  769 */             MetaPIGA.treeViewer.showAncestralStates(true);
/*  770 */             MainFrame.this.treeViewer();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  776 */     this.jMenuToolsConsensus.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  778 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  780 */             MainFrame.this.consensusTree();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  786 */     this.jMenuToolsDistances.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  788 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  790 */             MainFrame.this.computeDistances();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  796 */     this.jMenuToolsTest.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  798 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  800 */             MainFrame.this.test();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  806 */     this.jMenuHelpAbout.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  808 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  810 */             MainFrame.this.about();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  816 */     this.jMenuHelpChangeLog.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  818 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  820 */             MainFrame.this.changelog();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  826 */     this.jMenuHelpSystemInfo.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  828 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  830 */             MainFrame.this.sysInfo();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  836 */     this.jMenuHelpManual.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  838 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  840 */             MainFrame.this.showManual();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  846 */     this.jMenuHelpSupport.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  848 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  850 */             MainFrame.this.showSupport();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  856 */     this.laneLabel.addMouseListener(new MouseAdapter()
/*      */     {
/*      */       public void mouseClicked(MouseEvent arg0) {
/*  859 */         Tools.openURL("http://www.lanevol.org");
/*      */       }
/*      */     });
/*  862 */     this.laneLabel.setIcon(imageLANE);
/*      */ 
/*  864 */     this.loadButton.setIcon(imageOpenFile);
/*  865 */     this.loadButton.setBorder(BorderFactory.createRaisedBevelBorder());
/*  866 */     this.loadButton.setMaximumSize(new Dimension(40, 40));
/*  867 */     this.loadButton.setMinimumSize(new Dimension(30, 30));
/*  868 */     this.loadButton.setToolTipText("Load data file (" + KeyEvent.getKeyModifiersText(Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) + "+L)");
/*  869 */     this.loadButton.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  871 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  873 */             MainFrame.this.loadDataFile();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  878 */     this.saveButton.setIcon(imageSaveFile);
/*  879 */     this.saveButton.setBorder(BorderFactory.createRaisedBevelBorder());
/*  880 */     this.saveButton.setMaximumSize(new Dimension(40, 40));
/*  881 */     this.saveButton.setMinimumSize(new Dimension(30, 30));
/*  882 */     this.saveButton.setToolTipText("Save selected file (" + KeyEvent.getKeyModifiersText(Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) + "+S)");
/*  883 */     this.saveButton.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  885 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  887 */             MainFrame.this.saveNexusFile(false);
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  892 */     this.removeButton.setIcon(imageClose);
/*  893 */     this.removeButton.setBorder(BorderFactory.createRaisedBevelBorder());
/*  894 */     this.removeButton.setMaximumSize(new Dimension(40, 40));
/*  895 */     this.removeButton.setMinimumSize(new Dimension(30, 30));
/*  896 */     this.removeButton.setToolTipText("Close selected file (" + KeyEvent.getKeyModifiersText(Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) + "+W)");
/*  897 */     this.removeButton.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  899 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  901 */             MainFrame.this.closeNexusFile();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  906 */     this.datasetButton.setIcon(Tools.getScaledIcon(imageDataset, 32));
/*  907 */     this.datasetButton.setBorder(BorderFactory.createRaisedBevelBorder());
/*  908 */     this.datasetButton.setMaximumSize(new Dimension(40, 40));
/*  909 */     this.datasetButton.setMinimumSize(new Dimension(30, 30));
/*  910 */     this.datasetButton.setToolTipText("Dataset settings (" + KeyEvent.getKeyModifiersText(Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) + "+D)");
/*  911 */     this.datasetButton.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  913 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  915 */             MainFrame.this.datasetSettings();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  920 */     this.settingsButton.setIcon(Tools.getScaledIcon(imageSettings, 32));
/*  921 */     this.settingsButton.setBorder(BorderFactory.createRaisedBevelBorder());
/*  922 */     this.settingsButton.setMaximumSize(new Dimension(40, 40));
/*  923 */     this.settingsButton.setMinimumSize(new Dimension(30, 30));
/*  924 */     this.settingsButton.setToolTipText("Analysis settings (" + KeyEvent.getKeyModifiersText(Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) + "+A)");
/*  925 */     this.settingsButton.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  927 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  929 */             MainFrame.this.analysisSettings();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  934 */     this.startRunButton.setIcon(Tools.getScaledIcon(imageStartRun, 32));
/*  935 */     this.startRunButton.setBorder(BorderFactory.createRaisedBevelBorder());
/*  936 */     this.startRunButton.setMaximumSize(new Dimension(40, 40));
/*  937 */     this.startRunButton.setMinimumSize(new Dimension(30, 30));
/*  938 */     this.startRunButton.setToolTipText("Run heuristic search (" + KeyEvent.getKeyModifiersText(Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) + "+R)");
/*  939 */     this.startRunButton.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  941 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  943 */             MainFrame.this.runSelection();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  948 */     this.batchSaveButton.setIcon(imageSaveBatch);
/*  949 */     this.batchSaveButton.setBorder(BorderFactory.createRaisedBevelBorder());
/*  950 */     this.batchSaveButton.setMaximumSize(new Dimension(40, 40));
/*  951 */     this.batchSaveButton.setMinimumSize(new Dimension(30, 30));
/*  952 */     this.batchSaveButton.setToolTipText("Save all files in a Nexus batch (" + KeyEvent.getKeyModifiersText(1 + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) + "+S)");
/*  953 */     this.batchSaveButton.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  955 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  957 */             MainFrame.this.saveBatch();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  962 */     this.batchDuplicateButton.setIcon(imageDuplicateRun);
/*  963 */     this.batchDuplicateButton.setBorder(BorderFactory.createRaisedBevelBorder());
/*  964 */     this.batchDuplicateButton.setMaximumSize(new Dimension(40, 40));
/*  965 */     this.batchDuplicateButton.setMinimumSize(new Dimension(30, 30));
/*  966 */     this.batchDuplicateButton.setToolTipText("Duplicate selected dataset (" + KeyEvent.getKeyModifiersText(Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) + "+U)");
/*  967 */     this.batchDuplicateButton.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  969 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  971 */             MainFrame.this.duplicateRun();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  976 */     this.batchAssociateButton.setIcon(imageAssociateSettings);
/*  977 */     this.batchAssociateButton.setBorder(BorderFactory.createRaisedBevelBorder());
/*  978 */     this.batchAssociateButton.setMaximumSize(new Dimension(40, 40));
/*  979 */     this.batchAssociateButton.setMinimumSize(new Dimension(30, 30));
/*  980 */     this.batchAssociateButton.setToolTipText("Associate settings (" + KeyEvent.getKeyModifiersText(Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) + "+O)");
/*  981 */     this.batchAssociateButton.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  983 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  985 */             MainFrame.this.associateParameters();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/*  990 */     this.startBatchButton.setIcon(imageStartBatch);
/*  991 */     this.startBatchButton.setBorder(BorderFactory.createRaisedBevelBorder());
/*  992 */     this.startBatchButton.setMaximumSize(new Dimension(40, 40));
/*  993 */     this.startBatchButton.setMinimumSize(new Dimension(30, 30));
/*  994 */     this.startBatchButton.setToolTipText("Run batch (" + KeyEvent.getKeyModifiersText(1 + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) + "+R)");
/*  995 */     this.startBatchButton.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/*  997 */         new Thread(new Runnable() {
/*      */           public void run() {
/*  999 */             MainFrame.this.runBatch();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/* 1004 */     this.treeViewerButton.setIcon(Tools.getScaledIcon(imageTreeViewer, 32));
/* 1005 */     this.treeViewerButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 1006 */     this.treeViewerButton.setMaximumSize(new Dimension(40, 40));
/* 1007 */     this.treeViewerButton.setMinimumSize(new Dimension(30, 30));
/* 1008 */     this.treeViewerButton.setToolTipText("Tree viewer (" + KeyEvent.getKeyModifiersText(Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) + "+T)");
/* 1009 */     this.treeViewerButton.addActionListener(new ActionListener() {
/*      */       public void actionPerformed(ActionEvent e) {
/* 1011 */         new Thread(new Runnable() {
/*      */           public void run() {
/* 1013 */             MainFrame.this.treeViewer();
/*      */           }
/*      */         }).start();
/*      */       }
/*      */     });
/* 1019 */     this.removeButton.setEnabled(false);
/* 1020 */     this.saveButton.setEnabled(false);
/* 1021 */     this.datasetButton.setEnabled(false);
/* 1022 */     this.settingsButton.setEnabled(false);
/* 1023 */     this.startRunButton.setEnabled(false);
/* 1024 */     this.batchSaveButton.setEnabled(false);
/* 1025 */     this.batchAssociateButton.setEnabled(false);
/* 1026 */     this.batchDuplicateButton.setEnabled(false);
/* 1027 */     this.startBatchButton.setEnabled(false);
/*      */ 
/* 1029 */     this.toolBar.add(this.loadButton);
/* 1030 */     this.toolBar.add(this.saveButton);
/* 1031 */     this.toolBar.add(this.removeButton);
/* 1032 */     this.toolBar.addSeparator();
/* 1033 */     this.toolBar.add(this.datasetButton);
/* 1034 */     this.toolBar.add(this.settingsButton);
/* 1035 */     this.toolBar.add(this.startRunButton);
/* 1036 */     this.toolBar.addSeparator();
/* 1037 */     this.toolBar.add(this.batchDuplicateButton);
/* 1038 */     this.toolBar.add(this.batchAssociateButton);
/* 1039 */     this.toolBar.add(this.batchSaveButton);
/* 1040 */     this.toolBar.add(this.startBatchButton);
/* 1041 */     this.toolBar.addSeparator();
/* 1042 */     this.toolBar.add(this.treeViewerButton);
/* 1043 */     this.contentPane = ((JPanel)getContentPane());
/* 1044 */     this.border1 = BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151));
/* 1045 */     this.contentPane.setLayout(this.borderLayout1);
/* 1046 */     setSize(new Dimension(1024, 768));
/* 1047 */     setTitle("Metapiga 3.1");
/* 1048 */     statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
/* 1049 */     statusBar.setText("Welcome in Metapiga 3.1");
/* 1050 */     this.southPanel.setLayout(this.gridBagLayout1);
/* 1051 */     this.toolBar.setBorder(null);
/* 1052 */     this.southPanel.setBorder(null);
/* 1053 */     this.nexusNoWordWrapPanel.setLayout(this.nexusNoWordWrapLayout);
/* 1054 */     this.dataNoWordWrapPanel.setLayout(this.dataNoWordWrapLayout);
/* 1055 */     this.nexusNoWordWrapPanel.add(this.nexusTextPane, "nexusTextPane");
/* 1056 */     this.dataNoWordWrapPanel.add(this.dataTextPane, "dataTextPane");
/* 1057 */     this.nexusScrollPane.getViewport().add(this.nexusNoWordWrapPanel, null);
/* 1058 */     this.dataScrollPane.getViewport().add(this.dataNoWordWrapPanel, null);
/* 1059 */     this.contentPane.add(this.northPanel, "North");
/* 1060 */     this.contentPane.add(this.southPanel, "South");
/* 1061 */     this.contentPane.add(this.westPanel, "West");
/* 1062 */     this.contentPane.add(this.datasetSplitPane, "Center");
/* 1063 */     this.northPanel.add(this.laneLabel, new GridBagConstraints(2, 0, 1, 1, 0.0D, 0.0D, 
/* 1064 */       10, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 1065 */     this.northPanel.add(this.toolBar, new GridBagConstraints(0, 0, 1, 1, 1.0D, 0.0D, 
/* 1066 */       10, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 1067 */     this.southPanel.add(statusBar, new GridBagConstraints(0, 0, 1, 1, 1.0D, 0.0D, 
/* 1068 */       10, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 1069 */     this.southPanel.add(progressBar, new GridBagConstraints(0, 0, 1, 1, 1.0D, 0.0D, 
/* 1070 */       10, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 1071 */     progressBar.setVisible(false);
/*      */   }
/*      */ 
/*      */   public void setAllEnabled(Component parentComponent, WaitingLogo.Status status) {
/* 1075 */     if (parentComponent == null) parentComponent = this;
/* 1076 */     if (!status.enable) {
/* 1077 */       this.busy = true;
/* 1078 */       this.waitingLogo = new WaitingLogo(parentComponent, status);
/* 1079 */       new Thread(this.waitingLogo, "WaitingLogo-Animation").start();
/*      */     }
/* 1081 */     this.jMenuFileLoad.setEnabled(status.enable);
/* 1082 */     this.loadButton.setEnabled(status.enable);
/* 1083 */     this.jMenuFileSave.setEnabled(status.enable);
/* 1084 */     this.jMenuFileSaveModified.setEnabled(this.currentParameters != null ? this.currentParameters.isDatasetModified() : false);
/* 1085 */     this.saveButton.setEnabled(status.enable);
/* 1086 */     this.jMenuFileRemove.setEnabled(status.enable);
/* 1087 */     this.removeButton.setEnabled(status.enable);
/* 1088 */     this.jMenuDatasetCheckAmbiguities.setEnabled(status.enable);
/* 1089 */     this.jMenuDatasetCheckRedundancy.setEnabled(status.enable);
/* 1090 */     this.jMenuDatasetCheckSaturation.setEnabled(status.enable);
/* 1091 */     this.jMenuDatasetTrimming.setEnabled(status.enable);
/* 1092 */     this.jMenuDatasetSettings.setEnabled(status.enable);
/* 1093 */     this.jMenuSearchSettings.setEnabled(status.enable);
/* 1094 */     this.datasetButton.setEnabled(status.enable);
/* 1095 */     this.settingsButton.setEnabled(status.enable);
/* 1096 */     this.jMenuSearchRun.setEnabled(status.enable);
/* 1097 */     this.startRunButton.setEnabled(status.enable);
/* 1098 */     this.jMenuBatchSave.setEnabled(status.enable);
/* 1099 */     this.batchSaveButton.setEnabled(status.enable);
/* 1100 */     this.jMenuBatchAssociateParameters.setEnabled(status.enable);
/* 1101 */     this.batchAssociateButton.setEnabled(status.enable);
/* 1102 */     this.jMenuBatchDuplicateRun.setEnabled(status.enable);
/* 1103 */     this.batchDuplicateButton.setEnabled(status.enable);
/* 1104 */     this.jMenuBatchRun.setEnabled(status.enable);
/* 1105 */     this.startBatchButton.setEnabled(status.enable);
/* 1106 */     this.jMenuBatchClose.setEnabled(status.enable);
/* 1107 */     switch (status) {
/*      */     case CHECK_DATASET:
/* 1109 */       break;
/*      */     case COMPUTING_DISTANCES:
/* 1111 */       checkNoFileLoaded();
/* 1112 */       Tools.ShowErrorMessage(this, status.text, "Error when reading data file");
/* 1113 */       break;
/*      */     case CHECK_DATASET_DONE:
/* 1115 */       this.positionSlider.setMaximum(this.metapiga.parameters.getSize() - 1);
/* 1116 */       checkRunLabels();
/* 1117 */       MetaPIGA.treeViewer.setAvailbleParameters(this.metapiga.parameters);
/* 1118 */       updateMatrixTextPanes();
/* 1119 */       this.batchList.setSelectedIndex(this.metapiga.parameters.getSize() - 1);
/* 1120 */       System.gc();
/* 1121 */       break;
/*      */     case CHECK_DATASET_NOT_DONE:
/* 1123 */       this.positionSlider.setMaximum(this.metapiga.parameters.getSize() - 1);
/* 1124 */       checkRunLabels();
/* 1125 */       MetaPIGA.treeViewer.setAvailbleParameters(this.metapiga.parameters);
/* 1126 */       updateMatrixTextPanes();
/* 1127 */       System.gc();
/* 1128 */       break;
/*      */     case COMPUTING_DISTANCES_DONE:
/* 1130 */       break;
/*      */     case DATA_FILE_LOADED:
/* 1132 */       Tools.ShowErrorMessage(this, status.text, "Error when writing Nexus file");
/* 1133 */       break;
/*      */     case DATA_BATCH_LOADED:
/* 1135 */       break;
/*      */     case DATA_FILE_NOT_LOADED:
/* 1137 */       break;
/*      */     case DUPLICATION_DONE:
/* 1139 */       showRun();
/* 1140 */       this.batchList.repaint();
/* 1141 */       break;
/*      */     case DUPLICATION:
/* 1143 */       break;
/*      */     case DUPLICATION_NOT_DONE:
/* 1145 */       break;
/*      */     case LOAD_DATA_FILE:
/* 1147 */       MetaPIGA.treeViewer.setAvailbleParameters(this.metapiga.parameters);
/* 1148 */       break;
/*      */     case NEXUS_FILE_NOT_SAVED:
/* 1150 */       Tools.ShowErrorMessage(this, status.text, "Error when duplicating Nexus file");
/* 1151 */       break;
/*      */     case NEXUS_FILE_SAVED:
/* 1153 */       break;
/*      */     case PARAMETERS_NOT_SAVED:
/* 1155 */       break;
/*      */     case PARAMETERS_SAVED:
/* 1157 */       Tools.ShowErrorMessage(this, status.text, "Error when testing dataset");
/* 1158 */       break;
/*      */     case SAVE_NEXUS_FILE:
/* 1160 */       break;
/*      */     case SAVING_PARAMETERS:
/* 1162 */       break;
/*      */     case TREE_GENERATION:
/* 1164 */       break;
/*      */     case TREE_GENERATION_DONE:
/*      */     }
/*      */ 
/* 1169 */     if (status.enable)
/*      */     {
/* 1170 */       this.waitingLogo.stop(status);
/* 1171 */       showRun();
/* 1172 */       this.busy = false;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void updateButtonVisibility(Parameters params) {
/* 1177 */     if (params != null) {
/* 1178 */       DataType dataType = params.evaluationModel.getDataType();
/* 1179 */       if (dataType == DataType.CODON)
/* 1180 */         this.checkDatasetButton.setEnabled(false);
/*      */       else
/* 1182 */         this.checkDatasetButton.setEnabled(true);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void updateMatrixTextPanes() {
/* 1187 */     updateMatrixTextPanes(null);
/*      */   }
/*      */ 
/*      */   public void updateMatrixTextPanes(Parameters forceUpdateP) {
/* 1191 */     for (int i = 0; i < this.metapiga.parameters.size(); i++) {
/* 1192 */       Parameters p = (Parameters)this.metapiga.parameters.get(i);
/* 1193 */       if ((!this.availableCardLayouts.containsKey(p)) || ((forceUpdateP != null) && (p.label.equals(forceUpdateP.label)))) { JTextPane newNexusTextPane = new JTextPane();
/* 1195 */         JTextPane newDataTextPane = new JTextPane();
/* 1196 */         newNexusTextPane.setBackground(Color.black);
/* 1197 */         newNexusTextPane.setFont(new Font("Courier New", 0, 12));
/* 1198 */         newNexusTextPane.setForeground(Color.green);
/* 1199 */         newNexusTextPane.setOpaque(true);
/* 1200 */         newNexusTextPane.setRequestFocusEnabled(true);
/* 1201 */         newNexusTextPane.setCaretColor(Color.black);
/* 1202 */         newNexusTextPane.setEditable(false);
/* 1203 */         newDataTextPane.setBackground(Color.black);
/* 1204 */         newDataTextPane.setFont(new Font("Courier New", 0, 12));
/* 1205 */         newDataTextPane.setForeground(Color.green);
/* 1206 */         newDataTextPane.setOpaque(true);
/* 1207 */         newDataTextPane.setRequestFocusEnabled(true);
/* 1208 */         newDataTextPane.setCaretColor(Color.black);
/* 1209 */         newDataTextPane.setEditable(false);
/*      */         StackTraceElement[] arrayOfStackTraceElement;
/*      */         int j;
/*      */         int i;
/*      */         try { newNexusTextPane.setStyledDocument(p.showNexusDataMatrix());
/*      */         } catch (Exception e) {
/* 1213 */           newNexusTextPane.setText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 1214 */           j = (arrayOfStackTraceElement = e.getStackTrace()).length; i = 0; } for (; i < j; i++) { StackTraceElement el = arrayOfStackTraceElement[i];
/* 1215 */           newNexusTextPane.setText("\tat " + el.toString());
/*      */         }
/*      */         try
/*      */         {
/* 1219 */           newDataTextPane.setStyledDocument(p.showDataset());
/*      */         } catch (Exception e) {
/* 1221 */           newDataTextPane.setText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 1222 */           j = (arrayOfStackTraceElement = e.getStackTrace()).length; i = 0; } for (; i < j; i++) { StackTraceElement el = arrayOfStackTraceElement[i];
/* 1223 */           newDataTextPane.setText("\tat " + el.toString());
/*      */         }
/*      */ 
/* 1226 */         String index = this.layoutIndex++;
/* 1227 */         this.availableCardLayouts.put(p, index);
/* 1228 */         this.nexusNoWordWrapPanel.add(newNexusTextPane, index);
/* 1229 */         this.dataNoWordWrapPanel.add(newDataTextPane, index); }
/*      */     }
/*      */   }
/*      */ 
/*      */   private void checkNoFileLoaded()
/*      */   {
/* 1235 */     boolean enable = !this.metapiga.parameters.isEmpty();
/* 1236 */     this.jMenuBatchSave.setEnabled(enable);
/* 1237 */     this.batchSaveButton.setEnabled(enable);
/* 1238 */     this.jMenuBatchRun.setEnabled(enable);
/* 1239 */     this.startBatchButton.setEnabled(enable);
/* 1240 */     this.jMenuBatchClose.setEnabled(enable);
/*      */   }
/*      */ 
/*      */   private void checkRunLabels()
/*      */   {
/* 1245 */     Set usedNames = new HashSet();
/* 1246 */     for (int i = 0; i < this.metapiga.parameters.getSize(); i++) {
/* 1247 */       Parameters p = (Parameters)this.metapiga.parameters.get(i);
/* 1248 */       int num = 1;
/* 1249 */       String label = p.label;
/* 1250 */       Pattern pattern = Pattern.compile("(_\\d+)$");
/* 1251 */       while (usedNames.contains(p.label.toUpperCase())) {
/* 1252 */         Matcher matcher = pattern.matcher(p.label);
/* 1253 */         if (matcher.find()) {
/* 1254 */           int cut = p.label.lastIndexOf("_");
/* 1255 */           num = Integer.parseInt(p.label.substring(cut + 1)) + 1;
/* 1256 */           label = p.label.substring(0, cut);
/*      */         }
/* 1258 */         p.label = (label + "_" + num);
/* 1259 */         num++;
/*      */       }
/* 1261 */       usedNames.add(p.label.toUpperCase());
/*      */     }
/* 1263 */     this.batchList.repaint();
/*      */   }
/*      */ 
/*      */   private void showRun() {
/* 1267 */     boolean enable = false;
/*      */     try {
/* 1269 */       if (this.batchList.isSelectionEmpty()) {
/* 1270 */         this.currentParameters = null;
/* 1271 */         this.currentParametersPosition = -1;
/* 1272 */         this.positionSlider.setValue(0);
/* 1273 */         this.positionSlider.setEnabled(false);
/* 1274 */         this.settingsTextPane.setText("");
/* 1275 */         this.nexusNoWordWrapLayout.show(this.nexusNoWordWrapPanel, "nexusTextPane");
/* 1276 */         this.dataNoWordWrapLayout.show(this.dataNoWordWrapPanel, "dataTextPane");
/*      */       } else {
/* 1278 */         enable = true;
/* 1279 */         this.selectingRun = true;
/* 1280 */         this.currentParameters = ((Parameters)this.batchList.getSelectedValue());
/* 1281 */         this.currentParametersPosition = this.batchList.getSelectedIndex();
/* 1282 */         this.positionSlider.setEnabled(true);
/* 1283 */         this.positionSlider.setValue(this.currentParametersPosition);
/* 1284 */         this.settingsTextPane.setText("");
/* 1285 */         this.settingsTextPane.setText(getSettingsHTMLText());
/* 1286 */         this.settingsTextPane.setCaretPosition(0);
/* 1287 */         this.nexusNoWordWrapLayout.show(this.nexusNoWordWrapPanel, (String)this.availableCardLayouts.get(this.currentParameters));
/* 1288 */         this.dataNoWordWrapLayout.show(this.dataNoWordWrapPanel, (String)this.availableCardLayouts.get(this.currentParameters));
/* 1289 */         MetaPIGA.treeViewer.setParameters(this.currentParameters);
/* 1290 */         this.datasetButton.setEnabled(true);
/* 1291 */         this.settingsButton.setEnabled(true);
/* 1292 */         this.jMenuDatasetCheckAmbiguities.setEnabled(true);
/* 1293 */         this.jMenuDatasetCheckRedundancy.setEnabled(true);
/* 1294 */         this.jMenuDatasetCheckSaturation.setEnabled(true);
/* 1295 */         this.jMenuDatasetTrimming.setEnabled(true);
/* 1296 */         this.jMenuDatasetSettings.setEnabled(true);
/* 1297 */         this.jMenuSearchSettings.setEnabled(true);
/* 1298 */         this.selectingRun = false;
/*      */       }
/*      */     } catch (Exception e) {
/* 1301 */       e.printStackTrace();
/* 1302 */       JOptionPane.showMessageDialog(this, Tools.getErrorPanel("Error when displaying dataset", e), "Displaying dataset", 0);
/*      */     }
/* 1304 */     this.jMenuFileSave.setEnabled(enable);
/* 1305 */     this.jMenuFileSaveModified.setEnabled(this.currentParameters != null ? this.currentParameters.isDatasetModified() : false);
/* 1306 */     this.saveButton.setEnabled(enable);
/* 1307 */     this.jMenuFileRemove.setEnabled(enable);
/* 1308 */     this.removeButton.setEnabled(enable);
/* 1309 */     this.jMenuDatasetCheckAmbiguities.setEnabled(enable);
/* 1310 */     this.jMenuDatasetCheckRedundancy.setEnabled(enable);
/* 1311 */     this.jMenuDatasetCheckSaturation.setEnabled(enable);
/* 1312 */     this.jMenuDatasetTrimming.setEnabled(enable);
/* 1313 */     this.jMenuDatasetSettings.setEnabled(enable);
/* 1314 */     this.jMenuSearchSettings.setEnabled(enable);
/* 1315 */     this.datasetButton.setEnabled(enable);
/* 1316 */     this.settingsButton.setEnabled(enable);
/* 1317 */     this.jMenuSearchRun.setEnabled(enable);
/* 1318 */     this.startRunButton.setEnabled(enable);
/* 1319 */     this.jMenuBatchAssociateParameters.setEnabled(enable);
/* 1320 */     this.batchAssociateButton.setEnabled(enable);
/* 1321 */     this.jMenuBatchDuplicateRun.setEnabled(enable);
/* 1322 */     this.batchDuplicateButton.setEnabled(enable);
/* 1323 */     updateButtonVisibility(this.currentParameters);
/*      */   }
/*      */ 
/*      */   private void moveRun(int newPosition) {
/* 1327 */     if (this.currentParametersPosition > -1) {
/* 1328 */       this.currentParameters = ((Parameters)this.metapiga.parameters.remove(this.currentParametersPosition));
/* 1329 */       this.metapiga.parameters.add(newPosition, this.currentParameters);
/* 1330 */       this.batchList.setSelectedIndex(newPosition);
/*      */     }
/*      */   }
/*      */ 
/*      */   private String getSettingsHTMLText() {
/* 1335 */     String text = "<BODY style=\"font-family:geneva; color:#00FF00; font-size:10px\">Parameters of run ";
/* 1336 */     text = text + "<a href=\"" + this.currentParameters.label + "\" style=\"color:yellow\">" + this.currentParameters.label + "</a> :<br><br>";
/* 1337 */     long memNeeded = Tools.estimateNecessaryMemory(this.currentParameters);
/* 1338 */     long memSystem = Tools.getMaxPhysicalMemory();
/* 1339 */     long memAllowed = Runtime.getRuntime().maxMemory() / 1024L / 1024L;
/* 1340 */     long memUsed = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024L;
/*      */ 
/* 1342 */     if (memNeeded > memAllowed) {
/* 1343 */       text = text + "<div style=\"color:red\">Estimated amount of memory necessary : " + Tools.doubletoString(memNeeded, 0) + " Mb.";
/* 1344 */       if (memUsed + memNeeded < memSystem)
/* 1345 */         text = text + "<br>You have not assigned enough memory to MetaPIGA. Please use the menu \"Tools > Memory settings\" for assigning more memory, and relaunch MetaPIGA, or use other parameters (you can try reducing the number of parallel replicates, changing the selection scheme, removing rate heterogeneity, using another heuristic, etc).";
/*      */       else {
/* 1347 */         text = text + "<br>Your system memory seems limited to " + Tools.doubletoString(memSystem, 0) + " Mb, which is not enough for running this dataset with these parameters. Hence, either add more RAM to your machine or use other parameters (you can try reducing the number of parallel replicates, changing the selection scheme, removing rate heterogeneity, using another heuristic, etc).";
/*      */       }
/* 1349 */       text = text + "<br>You can try to reduce memory needs by changing settings (like reducing the number of parallel replicates, changing selection scheme, removing rate heterogeneity or using another heuristic).";
/* 1350 */       text = text + "</div>";
/*      */     } else {
/* 1352 */       text = text + "Estimated amount of memory necessary : " + Tools.doubletoString(memNeeded, 0) + " Mb.<br>";
/*      */     }
/* 1354 */     text = text + this.currentParameters.printParameters().replace("\n", "<br>");
/* 1355 */     text = text + "</BODY>";
/* 1356 */     return text;
/*      */   }
/*      */ 
/*      */   public void checkDataset(DatasetTriming.DatasetCheck check) {
/* 1360 */     setAllEnabled(this, WaitingLogo.Status.CHECK_DATASET);
/* 1361 */     DatasetTriming checkDataset = new DatasetTriming(this, this.currentParameters, check);
/* 1362 */     checkDataset.execute();
/*      */   }
/*      */ 
/*      */   public void exit()
/*      */   {
/* 1367 */     System.exit(0);
/*      */   }
/*      */ 
/*      */   public void loadDataFile()
/*      */   {
/* 1372 */     FileDialog chooser = new FileDialog(this, "Open data File in Nexus or Fasta format", 0);
/* 1373 */     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/* 1374 */     Dimension windowSize = chooser.getSize();
/* 1375 */     chooser.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/* 1376 */       Math.max(0, (screenSize.height - windowSize.height) / 2));
/* 1377 */     chooser.setVisible(true);
/* 1378 */     if (chooser.getFile() != null)
/* 1379 */       loadDataFile(new File(chooser.getDirectory() + chooser.getFile()));
/*      */   }
/*      */ 
/*      */   public void loadDataFile(File file)
/*      */   {
/* 1412 */     setAllEnabled(this, WaitingLogo.Status.LOAD_DATA_FILE);
/* 1413 */     Parameters.FileFormat format = Parameters.FileFormat.NEXUS;
/*      */     try {
/* 1415 */       FileReader fr = new FileReader(file);
/* 1416 */       BufferedReader br = new BufferedReader(fr);
/*      */       String line;
/* 1418 */       while ((line = br.readLine()) != null)
/*      */       {
/*      */         String line;
/* 1419 */         if ((line.length() > 0) && 
/* 1420 */           (line.startsWith(">"))) {
/* 1421 */           format = Parameters.FileFormat.FASTA;
/* 1422 */           break;
/*      */         }
/*      */       }
/*      */ 
/* 1426 */       br.close();
/* 1427 */       fr.close();
/*      */     } catch (Exception e) {
/* 1429 */       e.printStackTrace();
/*      */     }
/* 1431 */     switch (format) {
/*      */     case NEXUS:
/* 1433 */       FastaReader fastaReader = new FastaReader(file, this.metapiga);
/* 1434 */       fastaReader.execute();
/* 1435 */       break;
/*      */     case FASTA:
/*      */     default:
/* 1438 */       NexusReader nexusReader = new NexusReader(file, this.metapiga);
/* 1439 */       nexusReader.execute();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void saveNexusFile(boolean saveModified)
/*      */   {
/* 1446 */     if (this.currentParameters != null) {
/* 1447 */       FileDialog chooser = new FileDialog(this, "Save Nexus File", 1);
/* 1448 */       Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/* 1449 */       Dimension windowSize = chooser.getSize();
/* 1450 */       chooser.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/* 1451 */         Math.max(0, (screenSize.height - windowSize.height) / 2));
/* 1452 */       chooser.setFile(this.currentParameters.label + ".nex");
/* 1453 */       chooser.setVisible(true);
/* 1454 */       if (chooser.getFile() != null) {
/* 1455 */         setAllEnabled(this, WaitingLogo.Status.SAVE_NEXUS_FILE);
/* 1456 */         String filename = chooser.getDirectory();
/* 1457 */         if ((filename.endsWith("/")) || (filename.endsWith("\\"))) filename = filename + chooser.getFile();
/* 1458 */         if (!filename.endsWith(".nex")) filename = filename + ".nex";
/* 1459 */         NexusWriter nw = new NexusWriter(filename, this.currentParameters, saveModified, this);
/* 1460 */         nw.execute();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void closeNexusFile()
/*      */   {
/* 1467 */     this.selectingRun = true;
/*      */ 
/* 1469 */     this.metapiga.parameters.removeElement(this.currentParameters);
/* 1470 */     this.positionSlider.setMaximum(Math.max(this.metapiga.parameters.size() - 1, 0));
/* 1471 */     checkNoFileLoaded();
/* 1472 */     MetaPIGA.treeViewer.setAvailbleParameters(this.metapiga.parameters);
/* 1473 */     this.selectingRun = false;
/*      */   }
/*      */ 
/*      */   public void datasetSettings()
/*      */   {
/* 1478 */     JFrame settingsFrame = new JFrame();
/* 1479 */     settingsFrame.setIconImage(Tools.getScaledIcon(imageDataset, 32).getImage());
/* 1480 */     SettingsDatasetDialog settingsDialog = new SettingsDatasetDialog(settingsFrame, "Dataset settings", true, this, this.currentParameters);
/* 1481 */     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/* 1482 */     int width = screenSize.width - 100;
/* 1483 */     int height = screenSize.height - 100;
/* 1484 */     if (screenSize.width > 1100) width = 1100;
/* 1485 */     if (screenSize.height > 750) height = 750;
/* 1486 */     settingsDialog.setSize(new Dimension(width, height));
/* 1487 */     Dimension windowSize = settingsDialog.getSize();
/* 1488 */     settingsDialog.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/* 1489 */       Math.max(0, (screenSize.height - windowSize.height) / 2));
/* 1490 */     settingsDialog.setVisible(true);
/*      */   }
/*      */ 
/*      */   public void analysisSettings()
/*      */   {
/* 1495 */     JFrame settingsFrame = new JFrame();
/* 1496 */     settingsFrame.setIconImage(Tools.getScaledIcon(imageSettings, 32).getImage());
/* 1497 */     SettingsAnalysisDialog settingsDialog = new SettingsAnalysisDialog(settingsFrame, "Analysis settings", true, this, this.currentParameters);
/* 1498 */     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/* 1499 */     int width = screenSize.width - 100;
/* 1500 */     int height = screenSize.height - 100;
/* 1501 */     if (screenSize.width > 1100) width = 1100;
/* 1502 */     if (screenSize.height > 750) height = 750;
/* 1503 */     settingsDialog.setSize(new Dimension(width, height));
/* 1504 */     Dimension windowSize = settingsDialog.getSize();
/* 1505 */     settingsDialog.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/* 1506 */       Math.max(0, (screenSize.height - windowSize.height) / 2));
/* 1507 */     settingsDialog.setVisible(true);
/*      */   }
/*      */ 
/*      */   public void runSelection()
/*      */   {
/* 1512 */     if (this.currentParameters != null)
/*      */     {
/* 1514 */       ExecutorService executor = Executors.newSingleThreadExecutor();
/* 1515 */       SearchOnceGraphical searchDialog = new SearchOnceGraphical(this.currentParameters);
/* 1516 */       executor.execute(searchDialog);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void saveBatch()
/*      */   {
/* 1522 */     FileDialog chooser = new FileDialog(this, "Save batch file", 1);
/* 1523 */     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/* 1524 */     Dimension windowSize = chooser.getSize();
/* 1525 */     chooser.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/* 1526 */       Math.max(0, (screenSize.height - windowSize.height) / 2));
/* 1527 */     chooser.setVisible(true);
/* 1528 */     if (chooser.getFile() != null) {
/* 1529 */       setAllEnabled(this, WaitingLogo.Status.SAVE_NEXUS_FILE);
/* 1530 */       String filename = chooser.getDirectory() + chooser.getFile();
/* 1531 */       if (!filename.endsWith(".nex")) filename = filename + ".nex";
/* 1532 */       NexusWriter nw = new NexusWriter(filename, this.metapiga.parameters, this);
/* 1533 */       nw.execute();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void closeBatch()
/*      */   {
/* 1539 */     this.selectingRun = true;
/* 1540 */     this.metapiga.parameters.removeAllElements();
/* 1541 */     this.positionSlider.setMaximum(0);
/* 1542 */     checkNoFileLoaded();
/* 1543 */     MetaPIGA.treeViewer.setAvailbleParameters(this.metapiga.parameters);
/* 1544 */     this.selectingRun = false;
/*      */   }
/*      */ 
/*      */   public void duplicateRun()
/*      */   {
/* 1549 */     if (this.currentParameters != null) {
/* 1550 */       setAllEnabled(this, WaitingLogo.Status.DUPLICATION);
/* 1551 */       DuplicateRun dup = new DuplicateRun(null);
/* 1552 */       dup.execute();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void associateParameters()
/*      */   {
/* 1583 */     if (this.currentParameters != null) {
/* 1584 */       RunList list = new RunList(this);
/* 1585 */       list.setLocation(getMousePosition());
/* 1586 */       list.setVisible(true);
/* 1587 */       setAllEnabled(this, WaitingLogo.Status.DUPLICATION);
/* 1588 */       AssociateParameters dup = new AssociateParameters(this.currentParameters, list.to);
/* 1589 */       dup.execute();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void runBatch()
/*      */   {
/* 1662 */     ExecutorService executor = Executors.newSingleThreadExecutor();
/* 1663 */     SearchBatchGraphical batchFrame = new SearchBatchGraphical(this.metapiga.parameters);
/* 1664 */     executor.execute(batchFrame);
/*      */   }
/*      */ 
/*      */   public void treeViewer()
/*      */   {
/* 1669 */     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/* 1670 */     Dimension windowSize = MetaPIGA.treeViewer.getSize();
/* 1671 */     MetaPIGA.treeViewer.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/* 1672 */       Math.max(0, (screenSize.height - windowSize.height) / 2));
/* 1673 */     MetaPIGA.treeViewer.setVisible(true);
/*      */   }
/*      */ 
/*      */   public void treeGenerator()
/*      */   {
/* 1678 */     TreeGenerator frame = new TreeGenerator(this, this.currentParameters);
/* 1679 */     frame.setIconImage(Tools.getScaledIcon(imageRectangularCladogram, 32).getImage());
/* 1680 */     frame.setSize(new Dimension(700, 700));
/* 1681 */     frame.pack();
/* 1682 */     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/* 1683 */     Dimension windowSize = frame.getSize();
/* 1684 */     frame.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/* 1685 */       Math.max(0, (screenSize.height - windowSize.height) / 2));
/* 1686 */     frame.setVisible(true);
/*      */   }
/*      */ 
/*      */   public void consensusTree()
/*      */   {
/* 1691 */     ConsensusTreeFrame frame = new ConsensusTreeFrame();
/* 1692 */     frame.setIconImage(Tools.getScaledIcon(imageRectangularCladogram, 32).getImage());
/* 1693 */     frame.setSize(new Dimension(700, 600));
/* 1694 */     frame.pack();
/* 1695 */     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/* 1696 */     Dimension windowSize = frame.getSize();
/* 1697 */     frame.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/* 1698 */       Math.max(0, (screenSize.height - windowSize.height) / 2));
/* 1699 */     frame.setVisible(true);
/*      */   }
/*      */ 
/*      */   public void computeDistances()
/*      */   {
/* 1704 */     ComputeDistancesFrame frame = new ComputeDistancesFrame(this, this.currentParameters);
/* 1705 */     frame.setIconImage(Tools.getScaledIcon(imageMatrix, 32).getImage());
/* 1706 */     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/*      */ 
/* 1711 */     frame.setSize(new Dimension((int)(screenSize.width * 0.75D), (int)(screenSize.height * 0.75D)));
/* 1712 */     frame.setLocation(0, 0);
/* 1713 */     frame.setVisible(true);
/*      */   }
/*      */ 
/*      */   public void memorySettings()
/*      */   {
/*      */     try {
/* 1719 */       String appData = System.getProperty("user.home");
/* 1720 */       if (Util.isWindows())
/* 1721 */         appData = WinFileSystem.getSpecialFolder(SpecialFolder.APPDATA, false) + "\\MetaPIGA\\";
/* 1722 */       else if (Util.isMacOS())
/* 1723 */         appData = System.getProperty("user.home") + "/Library/Application Support/MetaPIGA/";
/*      */       else {
/* 1725 */         appData = System.getProperty("user.dir") + "/";
/*      */       }
/* 1727 */       String currentSetting = "";
/* 1728 */       List vmoptions = new ArrayList();
/* 1729 */       if (Util.isMacOS()) {
/* 1730 */         vmoptions.add(new File("MetaPIGA.app/Contents/Info.plist"));
/* 1731 */         vmoptions.add(new File("mp_console.vmoptions"));
/*      */       } else {
/* 1733 */         vmoptions.add(new File(appData + "mp.vmoptions"));
/*      */       }
/* 1735 */       int[] start = new int[vmoptions.size()];
/* 1736 */       int[] end = new int[vmoptions.size()];
/* 1737 */       for (int i = 0; i < vmoptions.size(); i++) {
/* 1738 */         FileReader fr = new FileReader((File)vmoptions.get(i));
/* 1739 */         BufferedReader br = new BufferedReader(fr);
/*      */ 
/* 1741 */         start[i] = 0;
/* 1742 */         end[i] = 0;
/*      */         String line;
/* 1743 */         while ((line = br.readLine()) != null)
/*      */         {
/*      */           String line;
/* 1744 */           if (line.indexOf("Xmx") >= 0) {
/* 1745 */             start[i] = (line.indexOf("Xmx") + 3);
/* 1746 */             end[i] = start[i];
/* 1747 */             while (line.charAt(end[i]) != 'm') {
/* 1748 */               if (i == 0) currentSetting = currentSetting + line.charAt(end[i]);
/* 1749 */               end[i] += 1;
/*      */             }
/*      */           }
/*      */         }
/* 1753 */         br.close();
/* 1754 */         fr.close();
/*      */       }
/* 1756 */       List availableAmounts = new ArrayList();
/* 1757 */       for (int val = 256; val < Tools.getMaxPhysicalMemory() - 500L; val += 256) {
/* 1758 */         availableAmounts.add(val);
/*      */       }
/* 1760 */       String[] memoryOptions = (String[])availableAmounts.toArray(new String[0]);
/* 1761 */       Object newValue = JOptionPane.showInputDialog(this, "Set maximum memory allowed to MetaPIGA : ", "Memory settings", 
/* 1762 */         3, Tools.getScaledIcon(imageMemory, 128), memoryOptions, currentSetting);
/* 1763 */       if (newValue != null) {
/* 1764 */         for (int i = 0; i < vmoptions.size(); i++) {
/* 1765 */           File tempOutput = File.createTempFile("metapiga/settings", ".temp");
/* 1766 */           tempOutput.deleteOnExit();
/* 1767 */           FileReader fr = new FileReader((File)vmoptions.get(i));
/* 1768 */           BufferedReader br = new BufferedReader(fr);
/* 1769 */           FileWriter fw = new FileWriter(tempOutput);
/*      */           String line;
/* 1771 */           while ((line = br.readLine()) != null)
/*      */           {
/*      */             String line;
/* 1772 */             if (line.indexOf("Xmx") >= 0) {
/* 1773 */               fw.write(line.substring(0, start[i]));
/* 1774 */               fw.write(newValue.toString());
/* 1775 */               fw.write(line.substring(end[i]) + "\n");
/*      */             } else {
/* 1777 */               fw.write(line + "\n");
/*      */             }
/*      */           }
/* 1780 */           br.close();
/* 1781 */           fr.close();
/* 1782 */           fw.close();
/* 1783 */           ((File)vmoptions.get(i)).delete();
/* 1784 */           FileUtils.moveFile(tempOutput, (File)vmoptions.get(i));
/*      */         }
/* 1786 */         String text = "Memory settings changed, you must restart MetaPIGA before it can take effect.";
/* 1787 */         JOptionPane.showMessageDialog(this, text, "Memory settings", 1, Tools.getScaledIcon(imageMemory, 128));
/*      */       }
/*      */     } catch (Exception ex) {
/* 1790 */       ex.printStackTrace();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void about()
/*      */   {
/* 1796 */     JFrame aboutFrame = new JFrame();
/* 1797 */     aboutFrame.setIconImage(Tools.getScaledIcon(imageAbout, 32).getImage());
/* 1798 */     AboutBox dlg = new AboutBox(aboutFrame);
/* 1799 */     Dimension dlgSize = dlg.getPreferredSize();
/* 1800 */     Dimension frmSize = getSize();
/* 1801 */     Point loc = getLocation();
/* 1802 */     dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
/* 1803 */     dlg.setModal(true);
/* 1804 */     dlg.pack();
/* 1805 */     dlg.setVisible(true);
/*      */   }
/*      */ 
/*      */   public void changelog()
/*      */   {
/* 1810 */     JFrame changelogFrame = new JFrame();
/* 1811 */     changelogFrame.setIconImage(Tools.getScaledIcon(imageUpdater, 32).getImage());
/* 1812 */     ChangelogDialog dlg = new ChangelogDialog(changelogFrame);
/* 1813 */     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/* 1814 */     Dimension windowSize = dlg.getSize();
/* 1815 */     dlg.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/* 1816 */       Math.max(0, (screenSize.height - windowSize.height) / 2));
/* 1817 */     dlg.setModal(true);
/* 1818 */     dlg.setVisible(true);
/*      */   }
/*      */ 
/*      */   public void sysInfo()
/*      */   {
/* 1823 */     String info = "";
/* 1824 */     info = info + "Available processors : " + Tools.doubletoString(Runtime.getRuntime().availableProcessors(), 0);
/* 1825 */     info = info + "\nSystem available memory : " + Tools.doubletoString(Tools.getMaxPhysicalMemory(), 0) + " Mb";
/* 1826 */     info = info + "\nMemory allocated to MetaPIGA : " + Tools.doubletoString(Runtime.getRuntime().maxMemory() / 1024L / 1024L, 0) + " Mb";
/* 1827 */     info = info + "\nMemory currently used by MetaPIGA : " + Tools.doubletoString((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024L, 0) + " Mb";
/* 1828 */     JOptionPane.showMessageDialog(this, info, "System information", 1, imageAbout);
/*      */   }
/*      */ 
/*      */   public void showManual()
/*      */   {
/* 1833 */     Tools.openURL("http://www.metapiga.org/files/MetaPIGA_manual.pdf");
/*      */   }
/*      */ 
/*      */   public void showSupport()
/*      */   {
/* 1838 */     Tools.openURL("http://sourceforge.net/projects/metapiga2/support");
/*      */   }
/*      */ 
/*      */   protected void processWindowEvent(WindowEvent e)
/*      */   {
/* 1843 */     super.processWindowEvent(e);
/* 1844 */     if (e.getID() == 201)
/* 1845 */       exit();
/*      */   }
/*      */ 
/*      */   public void dragEnter(DropTargetDragEvent dtde)
/*      */   {
/*      */   }
/*      */ 
/*      */   public void dragExit(DropTargetEvent dte)
/*      */   {
/*      */   }
/*      */ 
/*      */   public void dragOver(DropTargetDragEvent dtde)
/*      */   {
/*      */   }
/*      */ 
/*      */   public void dropActionChanged(DropTargetDragEvent dtde)
/*      */   {
/*      */   }
/*      */ 
/*      */   public void drop(DropTargetDropEvent dtde)
/*      */   {
/*      */     try
/*      */     {
/* 1870 */       Transferable tr = dtde.getTransferable();
/* 1871 */       DataFlavor[] flavors = tr.getTransferDataFlavors();
/* 1872 */       for (int i = 0; i < flavors.length; i++) {
/* 1873 */         if (flavors[i].isFlavorJavaFileListType()) {
/* 1874 */           dtde.acceptDrop(1);
/*      */ 
/* 1876 */           List list = (List)tr.getTransferData(flavors[i]);
/* 1877 */           final List files = new ArrayList();
/* 1878 */           for (int j = 0; j < list.size(); j++) {
/* 1879 */             File nexus = new File(list.get(j).toString());
/* 1880 */             if (nexus.exists()) files.add(nexus);
/*      */           }
/* 1882 */           dtde.dropComplete(true);
/* 1883 */           new Thread(new Runnable() {
/*      */             public void run() {
/* 1885 */               for (File file : files) {
/* 1886 */                 MainFrame.this.setAllEnabled(MainFrame.this, WaitingLogo.Status.LOAD_DATA_FILE);
/* 1887 */                 Parameters.FileFormat format = Parameters.FileFormat.NEXUS;
/*      */                 try {
/* 1889 */                   FileReader fr = new FileReader(file);
/* 1890 */                   BufferedReader br = new BufferedReader(fr);
/*      */                   String line;
/* 1892 */                   while ((line = br.readLine()) != null)
/*      */                   {
/*      */                     String line;
/* 1893 */                     if ((line.length() > 0) && 
/* 1894 */                       (line.startsWith(">"))) {
/* 1895 */                       format = Parameters.FileFormat.FASTA;
/* 1896 */                       break;
/*      */                     }
/*      */                   }
/*      */ 
/* 1900 */                   br.close();
/* 1901 */                   fr.close();
/*      */                 } catch (Exception e) {
/* 1903 */                   e.printStackTrace();
/*      */                 }
/* 1905 */                 switch (format) {
/*      */                 case NEXUS:
/* 1907 */                   FastaReader fastaReader = new FastaReader(file, MainFrame.this.metapiga);
/* 1908 */                   fastaReader.execute();
/*      */                   try {
/* 1910 */                     fastaReader.get();
/* 1911 */                     Thread.sleep(500L);
/*      */                   } catch (Exception e) {
/* 1913 */                     e.printStackTrace();
/*      */                   }
/*      */ 
/*      */                 case FASTA:
/*      */                 default:
/* 1918 */                   NexusReader nexusReader = new NexusReader(file, MainFrame.this.metapiga);
/* 1919 */                   nexusReader.execute();
/*      */                   try {
/* 1921 */                     nexusReader.get();
/* 1922 */                     Thread.sleep(500L);
/*      */                   } catch (Exception e) {
/* 1924 */                     e.printStackTrace();
/*      */                   }
/*      */                 }
/*      */               }
/*      */             }
/*      */           }).start();
/* 1931 */           return;
/*      */         }
/*      */       }
/* 1934 */       dtde.rejectDrop();
/*      */     } catch (Exception e) {
/* 1936 */       e.printStackTrace();
/* 1937 */       dtde.rejectDrop();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void test()
/*      */   {
/* 1962 */     testDFO();
/*      */   }
/*      */ 
/*      */   void testDFO()
/*      */   {
/* 1967 */     new DFO();
/*      */   }
/*      */ 
/*      */   private class AssociateParameters extends SwingWorker<WaitingLogo.Status, Object>
/*      */   {
/*      */     Parameters from;
/*      */     List<Parameters> to;
/*      */ 
/*      */     public AssociateParameters(List<Parameters> from)
/*      */     {
/* 1634 */       this.from = from;
/* 1635 */       this.to = to;
/*      */     }
/*      */     public WaitingLogo.Status doInBackground() {
/*      */       try {
/* 1639 */         for (Parameters p : this.to)
/* 1640 */           this.from.applyParametersTo(p);
/* 1641 */         return WaitingLogo.Status.DUPLICATION_DONE;
/*      */       } catch (Exception e) {
/* 1643 */         e.printStackTrace();
/* 1644 */       }return WaitingLogo.Status.DUPLICATION_NOT_DONE;
/*      */     }
/*      */ 
/*      */     public void done()
/*      */     {
/*      */       try {
/* 1650 */         MainFrame.this.setAllEnabled(null, (WaitingLogo.Status)get());
/*      */       } catch (ExecutionException e) {
/* 1652 */         e.getCause().printStackTrace();
/*      */       } catch (InterruptedException e) {
/* 1654 */         e.printStackTrace();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private class DuplicateRun extends SwingWorker<WaitingLogo.Status, Object>
/*      */   {
/*      */     private DuplicateRun()
/*      */     {
/*      */     }
/*      */ 
/*      */     public WaitingLogo.Status doInBackground()
/*      */     {
/*      */       try
/*      */       {
/* 1559 */         MainFrame.this.metapiga.parameters.addElement(MainFrame.this.currentParameters.duplicate());
/* 1560 */         return WaitingLogo.Status.DUPLICATION_DONE;
/*      */       } catch (Exception e) {
/* 1562 */         e.printStackTrace();
/* 1563 */       }return WaitingLogo.Status.DUPLICATION_NOT_DONE;
/*      */     }
/*      */ 
/*      */     public void done()
/*      */     {
/*      */       try {
/* 1569 */         MainFrame.this.checkRunLabels();
/* 1570 */         MainFrame.this.setAllEnabled(null, (WaitingLogo.Status)get());
/* 1571 */         MainFrame.this.positionSlider.setMaximum(MainFrame.this.metapiga.parameters.getSize() - 1);
/*      */       } catch (ExecutionException e) {
/* 1573 */         e.getCause().printStackTrace();
/*      */       } catch (InterruptedException e) {
/* 1575 */         e.printStackTrace();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private class RunList extends JDialog
/*      */   {
/*      */     JList list;
/*      */     JScrollPane scrollPane;
/*      */     JButton ok;
/* 1597 */     List<Parameters> to = new ArrayList();
/*      */ 
/* 1599 */     public RunList(JFrame parent) { super("Tranfer parameters to ...", true);
/*      */       try {
/* 1601 */         setSize(new Dimension(300, 200));
/* 1602 */         model = new DefaultListModel();
/* 1603 */         for (int i = 0; i < MainFrame.this.metapiga.parameters.getSize(); i++) {
/* 1604 */           if (i != MainFrame.this.currentParametersPosition) model.addElement(MainFrame.this.metapiga.parameters.get(i));
/*      */         }
/* 1606 */         this.list = new JList(model);
/* 1607 */         this.list.setSelectionMode(2);
/* 1608 */         this.list.setBackground(Color.black);
/* 1609 */         this.list.setFont(new Font("Geneva", 0, 14));
/* 1610 */         this.list.setForeground(Color.green);
/* 1611 */         this.scrollPane = new JScrollPane(this.list);
/* 1612 */         this.ok = new JButton("Associate");
/* 1613 */         this.ok.addActionListener(new ActionListener() {
/*      */           public void actionPerformed(ActionEvent e) {
/* 1615 */             for (Object o : RunList.this.list.getSelectedValues())
/* 1616 */               RunList.this.to.add((Parameters)o);
/* 1617 */             RunList.this.dispose();
/*      */           }
/*      */         });
/* 1620 */         getContentPane().add(this.scrollPane, "Center");
/* 1621 */         getContentPane().add(this.ok, "South");
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*      */         DefaultListModel model;
/* 1623 */         e.printStackTrace();
/* 1624 */         JOptionPane.showMessageDialog(this, Tools.getErrorPanel("Error", e), 
/* 1625 */           "Cannot show the list of Nexus files", 0);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public class Test
/*      */     implements Runnable
/*      */   {
/*      */     public Test()
/*      */     {
/*      */     }
/*      */ 
/*      */     public void run()
/*      */     {
/*      */       try
/*      */       {
/* 1944 */         Parameters P = (Parameters)MainFrame.this.metapiga.parameters.get(0);
/* 1945 */         Tree T = ((Tree)P.startingTrees.get(0)).clone();
/* 1946 */         Tree T1 = T.clone();
/* 1947 */         T.getEvaluation();
/* 1948 */         long fulltime = System.nanoTime();
/* 1949 */         for (int i = 0; i < 5000; i++) {
/* 1950 */           T1.clone(T);
/* 1951 */           T1.markAllNodesToReEvaluate();
/* 1952 */           T1.getEvaluation();
/*      */         }
/* 1954 */         System.out.println("T : " + Tools.doubletoString(System.nanoTime() - fulltime, 0) + " (" + Tools.doubletoString((System.nanoTime() - fulltime) / 60000000000.0D, 2) + " min)");
/*      */       } catch (Exception e) {
/* 1956 */         e.printStackTrace();
/*      */       }
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.MainFrame
 * JD-Core Version:    0.6.2
 */