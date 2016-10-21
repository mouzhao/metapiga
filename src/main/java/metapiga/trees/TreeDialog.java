/*     */ package metapiga.trees;
/*     */ 
/*     */ import java.awt.Color;
/*     */ import java.awt.Container;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.FileDialog;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Toolkit;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.print.PrinterJob;
/*     */ import java.io.File;
/*     */ import java.io.FileWriter;
/*     */ import java.util.Date;
/*     */ import java.util.List;
/*     */ import javax.imageio.ImageIO;
/*     */ import javax.swing.BorderFactory;
/*     */ import javax.swing.ImageIcon;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JList;
/*     */ import javax.swing.JOptionPane;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JTextPane;
/*     */ import javax.swing.JViewport;
/*     */ import javax.swing.event.ListSelectionEvent;
/*     */ import javax.swing.event.ListSelectionListener;
/*     */ import metapiga.MainFrame;
/*     */ import metapiga.MetaPIGA;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public class TreeDialog extends JFrame
/*     */ {
/*  45 */   TreePanel.Type currentType = TreePanel.Type.RECTANGULAR_CLADOGRAM;
/*  46 */   boolean showBranchLength = false;
/*  47 */   boolean showInodes = false;
/*     */   List<TreePanel> treePanels;
/*     */   final Parameters P;
/*     */   int currentTree;
/*     */   TreePanel treePanel;
/*  52 */   JPanel mainPanel = new JPanel();
/*  53 */   JScrollPane treeScrollPane = new JScrollPane();
/*  54 */   JPanel southPanel = new JPanel();
/*  55 */   JButton closeButton = new JButton();
/*  56 */   JButton slantedCladogramButton = new JButton();
/*  57 */   JButton radialTreeButton = new JButton();
/*  58 */   JButton rectCladogramButton = new JButton();
/*  59 */   JButton phylogramButton = new JButton();
/*  60 */   JButton exportButton = new JButton();
/*  61 */   JButton nextButton = new JButton();
/*  62 */   JButton precedentButton = new JButton();
/*  63 */   JButton printButton = new JButton();
/*  64 */   JButton saveOneButton = new JButton();
/*  65 */   JButton saveAllButton = new JButton();
/*  66 */   JButton showBlButton = new JButton();
/*  67 */   JButton showInodesButton = new JButton();
/*  68 */   JButton rootButton = new JButton();
/*  69 */   private JTextPane likelihoodTextPane = null;
/*  70 */   private JButton toViewerOneButton = null;
/*  71 */   private JButton toViewerAllButton = null;
/*     */   private JScrollPane likelihoodScrollPane;
/*     */ 
/*     */   public TreeDialog(List<TreePanel> treePanels, Parameters P)
/*     */   {
/*  75 */     super(((TreePanel)treePanels.get(0)).tree.getName());
/*  76 */     setIconImage(Tools.getScaledIcon(MainFrame.imageRectangularCladogram, 32).getImage());
/*  77 */     this.treePanels = treePanels;
/*  78 */     this.P = P;
/*  79 */     this.treePanel = ((TreePanel)treePanels.get(0));
/*  80 */     this.currentTree = 0;
/*     */     try {
/*  82 */       jbInit();
/*  83 */       pack();
/*     */     } catch (Exception ex) {
/*  85 */       ex.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   private void jbInit() throws Exception {
/*  90 */     this.closeButton.setToolTipText("Close window");
/*  91 */     this.closeButton.setContentAreaFilled(false);
/*  92 */     this.closeButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/*  94 */         TreeDialog.this.close();
/*     */       }
/*     */     });
/*  97 */     this.closeButton.setIcon(MainFrame.imageClose);
/*  98 */     this.closeButton.setBorder(BorderFactory.createRaisedBevelBorder());
/*  99 */     this.closeButton.setMaximumSize(new Dimension(40, 40));
/* 100 */     this.closeButton.setMinimumSize(new Dimension(30, 30));
/* 101 */     this.exportButton.setToolTipText("Export tree as an image");
/* 102 */     this.exportButton.setContentAreaFilled(false);
/* 103 */     this.exportButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 105 */         TreeDialog.this.export();
/*     */       }
/*     */     });
/* 108 */     this.exportButton.setIcon(MainFrame.imageImage);
/* 109 */     this.exportButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 110 */     this.exportButton.setMaximumSize(new Dimension(40, 40));
/* 111 */     this.exportButton.setMinimumSize(new Dimension(30, 30));
/* 112 */     this.printButton.setToolTipText("Print tree");
/* 113 */     this.printButton.setContentAreaFilled(false);
/* 114 */     this.printButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 116 */         TreeDialog.this.print();
/*     */       }
/*     */     });
/* 119 */     this.printButton.setIcon(MainFrame.imagePrinter);
/* 120 */     this.printButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 121 */     this.printButton.setMaximumSize(new Dimension(40, 40));
/* 122 */     this.printButton.setMinimumSize(new Dimension(30, 30));
/* 123 */     this.rootButton.setIcon(MainFrame.imageRoot);
/* 124 */     this.rootButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 125 */     this.rootButton.setMaximumSize(new Dimension(40, 40));
/* 126 */     this.rootButton.setMinimumSize(new Dimension(30, 30));
/* 127 */     this.rootButton.setToolTipText("Re-root tree");
/* 128 */     this.rootButton.setContentAreaFilled(false);
/* 129 */     this.rootButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 131 */         TreeDialog.this.reroot();
/*     */       }
/*     */     });
/* 134 */     this.showInodesButton.setIcon(MainFrame.imageInodes);
/* 135 */     this.showInodesButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 136 */     this.showInodesButton.setMaximumSize(new Dimension(40, 40));
/* 137 */     this.showInodesButton.setMinimumSize(new Dimension(30, 30));
/* 138 */     this.showInodesButton.setToolTipText("Show internal nodes labels");
/* 139 */     this.showInodesButton.setContentAreaFilled(false);
/* 140 */     this.showInodesButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 142 */         TreeDialog.this.showInodes();
/*     */       }
/*     */     });
/* 145 */     this.showBlButton.setIcon(MainFrame.imageBranchLength);
/* 146 */     this.showBlButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 147 */     this.showBlButton.setMaximumSize(new Dimension(40, 40));
/* 148 */     this.showBlButton.setMinimumSize(new Dimension(30, 30));
/* 149 */     this.showBlButton.setToolTipText("Show branch lengths");
/* 150 */     this.showBlButton.setContentAreaFilled(false);
/* 151 */     this.showBlButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 153 */         TreeDialog.this.showBranchLength();
/*     */       }
/*     */     });
/* 156 */     this.saveOneButton.setIcon(MainFrame.imageSaveOne);
/* 157 */     this.saveOneButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 158 */     this.saveOneButton.setMaximumSize(new Dimension(40, 40));
/* 159 */     this.saveOneButton.setMinimumSize(new Dimension(30, 30));
/* 160 */     this.saveOneButton.setToolTipText("Save only this tree to a file");
/* 161 */     this.saveOneButton.setContentAreaFilled(false);
/* 162 */     this.saveOneButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 164 */         TreeDialog.this.save1Tree();
/*     */       }
/*     */     });
/* 167 */     this.saveAllButton.setIcon(MainFrame.imageSaveAll);
/* 168 */     this.saveAllButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 169 */     this.saveAllButton.setMaximumSize(new Dimension(40, 40));
/* 170 */     this.saveAllButton.setMinimumSize(new Dimension(30, 30));
/* 171 */     this.saveAllButton.setToolTipText("Save all trees to a file");
/* 172 */     this.saveAllButton.setContentAreaFilled(false);
/* 173 */     this.saveAllButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 175 */         TreeDialog.this.saveAllTree();
/*     */       }
/*     */     });
/* 178 */     this.slantedCladogramButton.setToolTipText("Slanted cladogram");
/* 179 */     this.slantedCladogramButton.setBorderPainted(true);
/* 180 */     this.slantedCladogramButton.setContentAreaFilled(false);
/* 181 */     this.slantedCladogramButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 183 */         TreeDialog.this.slantedCladogram();
/*     */       }
/*     */     });
/* 186 */     this.slantedCladogramButton.setIcon(MainFrame.imageSlantedCladogram);
/* 187 */     this.slantedCladogramButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 188 */     this.slantedCladogramButton.setMaximumSize(new Dimension(40, 40));
/* 189 */     this.slantedCladogramButton.setMinimumSize(new Dimension(30, 30));
/* 190 */     this.radialTreeButton.setToolTipText("Radial tree");
/* 191 */     this.radialTreeButton.setBorderPainted(true);
/* 192 */     this.radialTreeButton.setContentAreaFilled(false);
/* 193 */     this.radialTreeButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 195 */         TreeDialog.this.radialTree();
/*     */       }
/*     */     });
/* 198 */     this.radialTreeButton.setIcon(MainFrame.imageRadialCladogram);
/* 199 */     this.radialTreeButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 200 */     this.radialTreeButton.setMaximumSize(new Dimension(40, 40));
/* 201 */     this.radialTreeButton.setMinimumSize(new Dimension(30, 30));
/* 202 */     this.rectCladogramButton.setToolTipText("Rectangular cladogram");
/* 203 */     this.rectCladogramButton.setContentAreaFilled(false);
/* 204 */     this.rectCladogramButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 206 */         TreeDialog.this.rectCladogram();
/*     */       }
/*     */     });
/* 209 */     this.rectCladogramButton.setIcon(MainFrame.imageRectangularCladogram);
/* 210 */     this.rectCladogramButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 211 */     this.rectCladogramButton.setMaximumSize(new Dimension(40, 40));
/* 212 */     this.rectCladogramButton.setMinimumSize(new Dimension(30, 30));
/* 213 */     this.phylogramButton.setToolTipText("Phylogram");
/* 214 */     this.phylogramButton.setContentAreaFilled(false);
/* 215 */     this.phylogramButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 217 */         TreeDialog.this.phylogram();
/*     */       }
/*     */     });
/* 220 */     this.phylogramButton.setIcon(MainFrame.imagePhylogram);
/* 221 */     this.phylogramButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 222 */     this.phylogramButton.setMaximumSize(new Dimension(40, 40));
/* 223 */     this.phylogramButton.setMinimumSize(new Dimension(30, 30));
/* 224 */     this.nextButton.setMinimumSize(new Dimension(30, 30));
/* 225 */     this.nextButton.setMaximumSize(new Dimension(40, 40));
/* 226 */     this.nextButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 227 */     this.nextButton.setIcon(MainFrame.imageNext);
/* 228 */     this.nextButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 230 */         TreeDialog.this.next();
/*     */       }
/*     */     });
/* 233 */     this.nextButton.setContentAreaFilled(false);
/* 234 */     this.nextButton.setBorderPainted(true);
/* 235 */     this.nextButton.setToolTipText("Next tree");
/* 236 */     if (this.treePanels.size() > 1)
/* 237 */       this.nextButton.setEnabled(true);
/*     */     else {
/* 239 */       this.nextButton.setEnabled(false);
/*     */     }
/* 241 */     this.precedentButton.setMinimumSize(new Dimension(30, 30));
/* 242 */     this.precedentButton.setMaximumSize(new Dimension(40, 40));
/* 243 */     this.precedentButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 244 */     this.precedentButton.setIcon(MainFrame.imagePrecedent);
/* 245 */     this.precedentButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 247 */         TreeDialog.this.precedent();
/*     */       }
/*     */     });
/* 250 */     this.precedentButton.setContentAreaFilled(false);
/* 251 */     this.precedentButton.setBorderPainted(true);
/* 252 */     this.precedentButton.setToolTipText("Precedent tree");
/* 253 */     this.precedentButton.setEnabled(false);
/* 254 */     GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
/* 255 */     gridBagConstraints2.gridx = 0;
/* 256 */     gridBagConstraints2.gridy = 1;
/* 257 */     gridBagConstraints2.fill = 1;
/* 258 */     gridBagConstraints2.weighty = 0.9D;
/* 259 */     gridBagConstraints2.weightx = 1.0D;
/* 260 */     GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
/* 261 */     gridBagConstraints3.gridx = 0;
/* 262 */     gridBagConstraints3.gridy = 2;
/* 263 */     gridBagConstraints3.fill = 2;
/* 264 */     gridBagConstraints3.weightx = 1.0D;
/* 265 */     GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
/* 266 */     gridBagConstraints4.gridx = 0;
/* 267 */     gridBagConstraints4.gridy = 0;
/* 268 */     gridBagConstraints4.fill = 1;
/* 269 */     gridBagConstraints4.weightx = 1.0D;
/* 270 */     gridBagConstraints4.weighty = 0.1D;
/* 271 */     this.mainPanel = new JPanel();
/* 272 */     this.mainPanel.setLayout(new GridBagLayout());
/* 273 */     this.mainPanel.add(this.treeScrollPane, gridBagConstraints2);
/* 274 */     this.mainPanel.add(this.southPanel, gridBagConstraints3);
/* 275 */     this.mainPanel.add(getLikelihoodScrollPane(), gridBagConstraints4);
/* 276 */     getContentPane().add(this.mainPanel);
/* 277 */     this.southPanel.add(this.precedentButton, null);
/* 278 */     this.southPanel.add(this.nextButton, null);
/* 279 */     this.southPanel.add(this.rectCladogramButton, null);
/* 280 */     this.southPanel.add(this.slantedCladogramButton, null);
/* 281 */     this.southPanel.add(this.radialTreeButton, null);
/* 282 */     this.southPanel.add(this.phylogramButton, null);
/* 283 */     this.southPanel.add(this.showInodesButton, null);
/* 284 */     this.southPanel.add(this.showBlButton, null);
/* 285 */     this.southPanel.add(this.rootButton, null);
/* 286 */     this.southPanel.add(getToViewerOneButton(), null);
/* 287 */     this.southPanel.add(getToViewerAllButton(), null);
/* 288 */     this.southPanel.add(this.saveOneButton, null);
/* 289 */     this.southPanel.add(this.saveAllButton, null);
/* 290 */     this.southPanel.add(this.exportButton);
/* 291 */     this.southPanel.add(this.printButton, null);
/* 292 */     this.southPanel.add(this.closeButton, null);
/* 293 */     this.treeScrollPane.setViewportView(this.treePanel);
/*     */   }
/*     */ 
/*     */   public void close() {
/* 297 */     dispose();
/*     */   }
/*     */ 
/*     */   public void export() {
/* 301 */     Object format = JOptionPane.showInputDialog(this, "Choose an image format: ", "Export tree to image file", 3, MainFrame.imageImage, ImageIO.getWriterFileSuffixes(), "png");
/* 302 */     if (format != null) {
/* 303 */       FileDialog chooser = new FileDialog(this, "Export tree to image", 1);
/* 304 */       chooser.setFile(((TreePanel)this.treePanels.get(this.currentTree)).tree.getName() + "." + format);
/* 305 */       Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/* 306 */       Dimension windowSize = chooser.getSize();
/* 307 */       chooser.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/* 308 */         Math.max(0, (screenSize.height - windowSize.height) / 2));
/* 309 */       chooser.setVisible(true);
/* 310 */       if (chooser.getFile() != null)
/*     */         try {
/* 312 */           String filename = chooser.getDirectory() + chooser.getFile();
/* 313 */           if (!filename.toLowerCase().endsWith("." + format.toString())) filename = filename + "." + format.toString();
/* 314 */           this.treePanel.exportToImage(new File(filename), format.toString());
/*     */         } catch (Exception e) {
/* 316 */           e.printStackTrace();
/* 317 */           JOptionPane.showMessageDialog(this, Tools.getErrorPanel("Error", e), 
/* 318 */             "Error in tree file exporting", 0);
/*     */         }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void print()
/*     */   {
/* 325 */     PrinterJob pj = PrinterJob.getPrinterJob();
/*     */ 
/* 328 */     if (pj.printDialog()) {
/* 329 */       pj.setPrintable(this.treePanel);
/*     */       try {
/* 331 */         pj.print();
/*     */       } catch (Exception PrintException) {
/* 333 */         PrintException.printStackTrace();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void precedent() {
/* 339 */     this.currentTree -= 1;
/* 340 */     if (this.currentTree == 0) {
/* 341 */       this.precedentButton.setEnabled(false);
/*     */     }
/* 343 */     if (this.currentTree < this.treePanels.size() - 1) {
/* 344 */       this.nextButton.setEnabled(true);
/*     */     }
/* 346 */     this.treeScrollPane.getViewport().remove(this.treePanel);
/* 347 */     this.treePanel = ((TreePanel)this.treePanels.get(this.currentTree));
/* 348 */     this.treePanel.treeType = this.currentType;
/* 349 */     this.treePanel.drawInodeLabel = this.showInodes;
/* 350 */     this.treePanel.drawBranchLength = this.showBranchLength;
/* 351 */     this.treeScrollPane.setViewportView(this.treePanel);
/* 352 */     setTitle(this.treePanel.tree.getName());
/* 353 */     getLikelihoodTextPane();
/*     */   }
/*     */ 
/*     */   public void next() {
/* 357 */     this.currentTree += 1;
/* 358 */     if (this.currentTree > 0) {
/* 359 */       this.precedentButton.setEnabled(true);
/*     */     }
/* 361 */     if (this.currentTree == this.treePanels.size() - 1) {
/* 362 */       this.nextButton.setEnabled(false);
/*     */     }
/* 364 */     this.treeScrollPane.getViewport().remove(this.treePanel);
/* 365 */     this.treePanel = ((TreePanel)this.treePanels.get(this.currentTree));
/* 366 */     this.treePanel.treeType = this.currentType;
/* 367 */     this.treePanel.drawInodeLabel = this.showInodes;
/* 368 */     this.treePanel.drawBranchLength = this.showBranchLength;
/* 369 */     this.treeScrollPane.setViewportView(this.treePanel);
/* 370 */     setTitle(this.treePanel.tree.getName());
/* 371 */     getLikelihoodTextPane();
/*     */   }
/*     */ 
/*     */   public void slantedCladogram() {
/* 375 */     this.currentType = TreePanel.Type.SLANTED_CLADOGRAM;
/* 376 */     this.treePanel.treeType = this.currentType;
/* 377 */     this.treePanel.repaint();
/*     */   }
/*     */ 
/*     */   public void radialTree() {
/* 381 */     this.currentType = TreePanel.Type.RADIAL_TREE;
/* 382 */     this.treePanel.treeType = this.currentType;
/* 383 */     this.treePanel.repaint();
/*     */   }
/*     */ 
/*     */   public void rectCladogram() {
/* 387 */     this.currentType = TreePanel.Type.RECTANGULAR_CLADOGRAM;
/* 388 */     this.treePanel.treeType = this.currentType;
/* 389 */     this.treePanel.repaint();
/*     */   }
/*     */ 
/*     */   public void phylogram() {
/* 393 */     this.currentType = TreePanel.Type.PHYLOGRAM;
/* 394 */     this.treePanel.treeType = this.currentType;
/* 395 */     this.treePanel.repaint();
/*     */   }
/*     */ 
/*     */   public void save1Tree() {
/* 399 */     FileDialog chooser = new FileDialog(this, "Save tree to file", 1);
/* 400 */     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/* 401 */     Dimension windowSize = chooser.getSize();
/* 402 */     chooser.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/* 403 */       Math.max(0, (screenSize.height - windowSize.height) / 2));
/* 404 */     chooser.setVisible(true);
/* 405 */     if (chooser.getFile() != null) {
/* 406 */       String filename = chooser.getDirectory() + chooser.getFile();
/* 407 */       if (!filename.endsWith(".tre")) filename = filename + ".tre";
/* 408 */       File output = new File(filename);
/*     */       try {
/* 410 */         FileWriter fw = new FileWriter(output);
/* 411 */         fw.write("#NEXUS\n");
/* 412 */         fw.write("\n");
/* 413 */         fw.write("Begin trees;  [Treefile saved " + new Date(System.currentTimeMillis()).toString() + "]\n");
/* 414 */         fw.write(this.treePanel.tree.toNewickLine(false, true) + "\n");
/* 415 */         fw.write("End;\n");
/* 416 */         fw.close();
/*     */       } catch (Exception e) {
/* 418 */         e.printStackTrace();
/* 419 */         JOptionPane.showMessageDialog(this, Tools.getErrorPanel("Error", e), 
/* 420 */           "Error in tree file saving", 0);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void saveAllTree() {
/* 426 */     FileDialog chooser = new FileDialog(this, "Save trees to file", 1);
/* 427 */     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/* 428 */     Dimension windowSize = chooser.getSize();
/* 429 */     chooser.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/* 430 */       Math.max(0, (screenSize.height - windowSize.height) / 2));
/* 431 */     chooser.setVisible(true);
/* 432 */     if (chooser.getFile() != null) {
/* 433 */       String filename = chooser.getDirectory() + chooser.getFile();
/* 434 */       if (!filename.endsWith(".tre")) filename = filename + ".tre";
/* 435 */       File output = new File(filename);
/*     */       try {
/* 437 */         FileWriter fw = new FileWriter(output);
/* 438 */         fw.write("#NEXUS\n");
/* 439 */         fw.write("\n");
/* 440 */         fw.write("Begin trees;  [Treefile saved " + new Date(System.currentTimeMillis()).toString() + "]\n");
/* 441 */         for (TreePanel tp : this.treePanels) {
/* 442 */           fw.write(tp.tree.toNewickLine(false, true) + "\n");
/*     */         }
/* 444 */         fw.write("End;\n");
/* 445 */         fw.close();
/*     */       } catch (Exception e) {
/* 447 */         e.printStackTrace();
/* 448 */         JOptionPane.showMessageDialog(this, Tools.getErrorPanel("Error", e), 
/* 449 */           "Error in tree file saving", 0);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void toViewer1Tree() {
/* 455 */     MetaPIGA.treeViewer.addTree(this.treePanel.tree, this.P);
/* 456 */     MetaPIGA.treeViewer.setVisible(true);
/*     */   }
/*     */ 
/*     */   public void toViewerAllTree() {
/* 460 */     for (TreePanel tp : this.treePanels) {
/* 461 */       MetaPIGA.treeViewer.addTree(tp.tree, this.P);
/*     */     }
/* 463 */     MetaPIGA.treeViewer.setVisible(true);
/*     */   }
/*     */ 
/*     */   public void showBranchLength() {
/* 467 */     this.showBranchLength = (!this.showBranchLength);
/* 468 */     this.treePanel.drawBranchLength = this.showBranchLength;
/* 469 */     this.treePanel.repaint();
/*     */   }
/*     */ 
/*     */   public void showInodes() {
/* 473 */     this.showInodes = (!this.showInodes);
/* 474 */     this.treePanel.drawInodeLabel = this.showInodes;
/* 475 */     this.treePanel.repaint();
/*     */   }
/*     */ 
/*     */   public void reroot() {
/* 479 */     InodeList list = new InodeList(this, this.treePanel.tree.getRoot());
/* 480 */     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/* 481 */     Dimension windowSize = list.getSize();
/* 482 */     list.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/* 483 */       Math.max(0, (screenSize.height - windowSize.height) / 2));
/* 484 */     list.setVisible(true);
/* 485 */     this.treePanel.tree.root(list.newRoot);
/* 486 */     this.treePanel.repaint();
/*     */   }
/*     */ 
/*     */   private JTextPane getLikelihoodTextPane()
/*     */   {
/* 524 */     if (this.likelihoodTextPane == null) {
/* 525 */       this.likelihoodTextPane = new JTextPane();
/* 526 */       this.likelihoodTextPane.setBackground(Color.BLACK);
/* 527 */       this.likelihoodTextPane.setForeground(Color.GREEN);
/* 528 */       this.likelihoodTextPane.setEditable(false);
/*     */     }
/*     */     try {
/* 531 */       this.likelihoodTextPane.setStyledDocument(((TreePanel)this.treePanels.get(this.currentTree)).tree.getEvaluationString());
/*     */     } catch (Exception e) {
/* 533 */       e.printStackTrace();
/* 534 */       this.likelihoodTextPane.setText("Cannot display likelihood of this tree (" + Tools.getErrorMessage(e) + ")");
/*     */     }
/* 536 */     return this.likelihoodTextPane;
/*     */   }
/*     */ 
/*     */   private JButton getToViewerOneButton()
/*     */   {
/* 545 */     if (this.toViewerOneButton == null) {
/* 546 */       this.toViewerOneButton = new JButton();
/* 547 */       this.toViewerOneButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 548 */       this.toViewerOneButton.setMinimumSize(new Dimension(30, 30));
/* 549 */       this.toViewerOneButton.setToolTipText("Add only this tree to the tree viewer");
/* 550 */       this.toViewerOneButton.setContentAreaFilled(false);
/* 551 */       this.toViewerOneButton.setIcon(MainFrame.imageTreeViewerOne);
/* 552 */       this.toViewerOneButton.setMaximumSize(new Dimension(40, 40));
/* 553 */       this.toViewerOneButton.addActionListener(new ActionListener() {
/*     */         public void actionPerformed(ActionEvent e) {
/* 555 */           TreeDialog.this.toViewer1Tree();
/*     */         }
/*     */       });
/*     */     }
/* 559 */     return this.toViewerOneButton;
/*     */   }
/*     */ 
/*     */   private JButton getToViewerAllButton()
/*     */   {
/* 568 */     if (this.toViewerAllButton == null) {
/* 569 */       this.toViewerAllButton = new JButton();
/* 570 */       this.toViewerAllButton.setBorder(BorderFactory.createRaisedBevelBorder());
/* 571 */       this.toViewerAllButton.setMinimumSize(new Dimension(30, 30));
/* 572 */       this.toViewerAllButton.setToolTipText("Add all trees to the tree viewer");
/* 573 */       this.toViewerAllButton.setContentAreaFilled(false);
/* 574 */       this.toViewerAllButton.setIcon(MainFrame.imageTreeViewerAll);
/* 575 */       this.toViewerAllButton.setMaximumSize(new Dimension(40, 40));
/* 576 */       this.toViewerAllButton.addActionListener(new ActionListener() {
/*     */         public void actionPerformed(ActionEvent e) {
/* 578 */           TreeDialog.this.toViewerAllTree();
/*     */         }
/*     */       });
/*     */     }
/* 582 */     return this.toViewerAllButton;
/*     */   }
/*     */ 
/*     */   private JScrollPane getLikelihoodScrollPane() {
/* 586 */     if (this.likelihoodScrollPane == null) {
/* 587 */       this.likelihoodScrollPane = new JScrollPane();
/* 588 */       this.likelihoodScrollPane.setHorizontalScrollBarPolicy(31);
/* 589 */       this.likelihoodScrollPane.setViewportView(getLikelihoodTextPane());
/*     */     }
/* 591 */     return this.likelihoodScrollPane;
/*     */   }
/*     */ 
/*     */   public class InodeList extends JDialog
/*     */   {
/*     */     JList list;
/*     */     JScrollPane scrollPane;
/*     */     Node newRoot;
/*     */ 
/*     */     public InodeList(JFrame parent, Node actualRoot)
/*     */     {
/* 494 */       super("Select the root", true);
/* 495 */       this.newRoot = actualRoot;
/*     */       try {
/* 497 */         this.list = new JList(TreeDialog.this.treePanel.tree.getInodes().toArray());
/* 498 */         this.list.setSelectedValue(actualRoot, true);
/* 499 */         this.list.setSelectionMode(0);
/* 500 */         this.scrollPane = new JScrollPane(this.list);
/* 501 */         this.list.addListSelectionListener(new ListSelectionListener() {
/*     */           public void valueChanged(ListSelectionEvent e) {
/* 503 */             TreeDialog.InodeList.this.newRoot = ((Node)TreeDialog.InodeList.this.list.getSelectedValue());
/* 504 */             TreeDialog.InodeList.this.dispose();
/*     */           }
/*     */         });
/* 507 */         getContentPane().add(this.scrollPane);
/* 508 */         pack();
/*     */       } catch (Exception e) {
/* 510 */         e.printStackTrace();
/* 511 */         JOptionPane.showMessageDialog(this, Tools.getErrorPanel("Error", e), 
/* 512 */           "Cannot show the list of internal nodes", 0);
/*     */       }
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.TreeDialog
 * JD-Core Version:    0.6.2
 */