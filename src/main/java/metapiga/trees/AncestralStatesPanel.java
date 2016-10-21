/*     */ package metapiga.trees;
/*     */ 
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.FileDialog;
/*     */ import java.awt.Font;
/*     */ import java.awt.FontMetrics;
/*     */ import java.awt.Frame;
/*     */ import java.awt.Graphics;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Toolkit;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.geom.Rectangle2D;
/*     */ import java.io.File;
/*     */ import java.io.FileWriter;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import javax.swing.BorderFactory;
/*     */ import javax.swing.GroupLayout;
/*     */ import javax.swing.GroupLayout.Alignment;
/*     */ import javax.swing.GroupLayout.ParallelGroup;
/*     */ import javax.swing.GroupLayout.SequentialGroup;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JOptionPane;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JViewport;
/*     */ import javax.swing.LayoutStyle.ComponentPlacement;
/*     */ import javax.swing.text.DefaultEditorKit;
/*     */ import javax.swing.text.DefaultStyledDocument;
/*     */ import metapiga.MainFrame;
/*     */ import metapiga.modelization.Dataset;
/*     */ import metapiga.modelization.data.Data;
/*     */ import metapiga.modelization.data.DataType;
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public class AncestralStatesPanel extends JPanel
/*     */ {
/*     */   private static final long serialVersionUID = -4232202860479166844L;
/*  48 */   private HistoType histoType = HistoType.OVERLAPPING;
/*  49 */   private Tree currentTree = null;
/*  50 */   private Node currentNode = null;
/*     */   private JPanel emptyPanel;
/*     */   private JPanel ancestralPanel;
/*     */   private AncestralHistoPanel ancestralHistoPanel;
/*     */   private JButton buttonExportNode;
/*     */   private JButton buttonExportTree;
/*     */   private JLabel labelEmpty;
/*     */   private JButton buttonCumulativeHisto;
/*     */   private JButton buttonOverlappingHisto;
/*     */   private JScrollPane scrollPane;
/*     */ 
/*     */   public AncestralStatesPanel()
/*     */   {
/*  61 */     setForeground(Color.GREEN);
/*  62 */     setBackground(Color.BLACK);
/*  63 */     GridBagLayout gridBagLayout = new GridBagLayout();
/*  64 */     setLayout(gridBagLayout);
/*     */ 
/*  66 */     JPanel ButtonPanel = new JPanel();
/*  67 */     GridBagConstraints gbc_ButtonPanel = new GridBagConstraints();
/*  68 */     gbc_ButtonPanel.fill = 3;
/*  69 */     gbc_ButtonPanel.weighty = 1.0D;
/*  70 */     gbc_ButtonPanel.gridx = 0;
/*  71 */     gbc_ButtonPanel.gridy = 0;
/*  72 */     add(ButtonPanel, gbc_ButtonPanel);
/*     */ 
/*  74 */     this.buttonOverlappingHisto = new JButton();
/*  75 */     this.buttonOverlappingHisto.setBorder(BorderFactory.createRaisedBevelBorder());
/*  76 */     this.buttonOverlappingHisto.setMinimumSize(new Dimension(37, 37));
/*  77 */     this.buttonOverlappingHisto.setToolTipText("Show overlapping histograms");
/*  78 */     this.buttonOverlappingHisto.setContentAreaFilled(false);
/*  79 */     this.buttonOverlappingHisto.setIcon(MainFrame.imageHistoOverlapping);
/*  80 */     this.buttonOverlappingHisto.setMaximumSize(new Dimension(40, 40));
/*  81 */     this.buttonOverlappingHisto.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/*  83 */         new Thread(new Runnable() {
/*     */           public void run() {
/*  85 */             AncestralStatesPanel.this.showHistograms(AncestralStatesPanel.HistoType.OVERLAPPING);
/*     */           }
/*     */         }).start();
/*     */       }
/*     */     });
/*  91 */     this.buttonCumulativeHisto = new JButton();
/*  92 */     this.buttonCumulativeHisto.setBorder(BorderFactory.createRaisedBevelBorder());
/*  93 */     this.buttonCumulativeHisto.setMinimumSize(new Dimension(37, 37));
/*  94 */     this.buttonCumulativeHisto.setToolTipText("Show cumulative histograms");
/*  95 */     this.buttonCumulativeHisto.setContentAreaFilled(false);
/*  96 */     this.buttonCumulativeHisto.setIcon(MainFrame.imageHistoCumulative);
/*  97 */     this.buttonCumulativeHisto.setMaximumSize(new Dimension(40, 40));
/*  98 */     this.buttonCumulativeHisto.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 100 */         new Thread(new Runnable() {
/*     */           public void run() {
/* 102 */             AncestralStatesPanel.this.showHistograms(AncestralStatesPanel.HistoType.CUMULATIVE);
/*     */           }
/*     */         }).start();
/*     */       }
/*     */     });
/* 108 */     this.buttonExportNode = new JButton();
/* 109 */     this.buttonExportNode.setBorder(BorderFactory.createRaisedBevelBorder());
/* 110 */     this.buttonExportNode.setMinimumSize(new Dimension(37, 37));
/* 111 */     this.buttonExportNode.setToolTipText("Export ancestral states for selected node");
/* 112 */     this.buttonExportNode.setContentAreaFilled(false);
/* 113 */     this.buttonExportNode.setIcon(MainFrame.imageSaveOne);
/* 114 */     this.buttonExportNode.setMaximumSize(new Dimension(40, 40));
/* 115 */     this.buttonExportNode.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 117 */         new Thread(new Runnable() {
/*     */           public void run() {
/* 119 */             AncestralStatesPanel.this.export(AncestralStatesPanel.this.currentTree, AncestralStatesPanel.this.currentNode);
/*     */           }
/*     */         }).start();
/*     */       }
/*     */     });
/* 125 */     this.buttonExportTree = new JButton();
/* 126 */     this.buttonExportTree.setBorder(BorderFactory.createRaisedBevelBorder());
/* 127 */     this.buttonExportTree.setMinimumSize(new Dimension(37, 37));
/* 128 */     this.buttonExportTree.setToolTipText("Export ancestral states for all nodes of selected tree");
/* 129 */     this.buttonExportTree.setContentAreaFilled(false);
/* 130 */     this.buttonExportTree.setIcon(MainFrame.imageSaveAll);
/* 131 */     this.buttonExportTree.setMaximumSize(new Dimension(40, 40));
/* 132 */     this.buttonExportTree.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 134 */         new Thread(new Runnable() {
/*     */           public void run() {
/* 136 */             AncestralStatesPanel.this.export(AncestralStatesPanel.this.currentTree, null);
/*     */           }
/*     */         }).start();
/*     */       }
/*     */     });
/* 141 */     GroupLayout gl_ButtonPanel = new GroupLayout(ButtonPanel);
/* 142 */     gl_ButtonPanel.setHorizontalGroup(
/* 143 */       gl_ButtonPanel.createParallelGroup(GroupLayout.Alignment.LEADING)
/* 144 */       .addGroup(gl_ButtonPanel.createSequentialGroup()
/* 145 */       .addGroup(gl_ButtonPanel.createParallelGroup(GroupLayout.Alignment.LEADING)
/* 146 */       .addGroup(gl_ButtonPanel.createSequentialGroup()
/* 147 */       .addGap(5)
/* 148 */       .addGroup(gl_ButtonPanel.createParallelGroup(GroupLayout.Alignment.LEADING)
/* 149 */       .addComponent(this.buttonOverlappingHisto, -2, 37, -2)
/* 150 */       .addComponent(this.buttonCumulativeHisto, -2, -1, -2)
/* 151 */       .addComponent(this.buttonExportNode, -2, -1, -2)))
/* 152 */       .addGroup(gl_ButtonPanel.createSequentialGroup()
/* 153 */       .addGap(5)
/* 154 */       .addComponent(this.buttonExportTree, -2, -1, -2)))
/* 155 */       .addContainerGap(5, 32767)));
/*     */ 
/* 157 */     gl_ButtonPanel.setVerticalGroup(
/* 158 */       gl_ButtonPanel.createParallelGroup(GroupLayout.Alignment.LEADING)
/* 159 */       .addGroup(gl_ButtonPanel.createSequentialGroup()
/* 160 */       .addGap(5)
/* 161 */       .addComponent(this.buttonOverlappingHisto, -2, -1, -2)
/* 162 */       .addGap(5)
/* 163 */       .addComponent(this.buttonCumulativeHisto, -2, -1, -2)
/* 164 */       .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
/* 165 */       .addComponent(this.buttonExportNode, -2, -1, -2)
/* 166 */       .addGap(5)
/* 167 */       .addComponent(this.buttonExportTree, -2, -1, -2)
/* 168 */       .addGap(5)));
/*     */ 
/* 170 */     ButtonPanel.setLayout(gl_ButtonPanel);
/*     */ 
/* 172 */     this.ancestralPanel = new JPanel();
/* 173 */     this.ancestralPanel.setBackground(Color.BLACK);
/* 174 */     this.ancestralPanel.setForeground(Color.GREEN);
/* 175 */     GridBagConstraints gbc_ancestralPanel = new GridBagConstraints();
/* 176 */     gbc_ancestralPanel.weightx = 1.0D;
/* 177 */     gbc_ancestralPanel.fill = 1;
/* 178 */     gbc_ancestralPanel.gridx = 1;
/* 179 */     gbc_ancestralPanel.gridy = 0;
/* 180 */     add(this.ancestralPanel, gbc_ancestralPanel);
/* 181 */     this.ancestralPanel.setLayout(new BorderLayout(0, 0));
/*     */ 
/* 183 */     this.ancestralHistoPanel = new AncestralHistoPanel(null);
/*     */ 
/* 185 */     this.scrollPane = new JScrollPane();
/* 186 */     this.scrollPane.setBackground(Color.BLACK);
/* 187 */     this.ancestralPanel.add(this.scrollPane, "Center");
/*     */ 
/* 189 */     this.emptyPanel = new JPanel();
/* 190 */     this.emptyPanel.setBackground(Color.BLACK);
/* 191 */     this.emptyPanel.setForeground(Color.GREEN);
/* 192 */     GridBagConstraints gbc_emptyPanel = new GridBagConstraints();
/* 193 */     gbc_emptyPanel.weightx = 1.0D;
/* 194 */     gbc_emptyPanel.fill = 1;
/* 195 */     gbc_emptyPanel.gridx = 1;
/* 196 */     gbc_emptyPanel.gridy = 0;
/* 197 */     add(this.emptyPanel, gbc_emptyPanel);
/* 198 */     this.emptyPanel.setLayout(new BorderLayout(0, 0));
/*     */ 
/* 200 */     this.labelEmpty = new JLabel();
/* 201 */     this.labelEmpty.setHorizontalAlignment(0);
/* 202 */     this.labelEmpty.setForeground(Color.GREEN);
/* 203 */     this.emptyPanel.add(this.labelEmpty, "Center");
/*     */ 
/* 205 */     showMessage(Message.SELECT_TREE, null, null);
/*     */   }
/*     */ 
/*     */   private void showHistograms(HistoType histoType) {
/* 209 */     this.histoType = histoType;
/* 210 */     repaint();
/*     */   }
/*     */ 
/*     */   private void export(Tree tree, Node node) {
/* 214 */     FileDialog chooser = new FileDialog(new Frame(), "Export ancestral reconstruction to file", 1);
/* 215 */     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/* 216 */     Dimension windowSize = chooser.getSize();
/* 217 */     chooser.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/* 218 */       Math.max(0, (screenSize.height - windowSize.height) / 2));
/* 219 */     chooser.setVisible(true);
/* 220 */     if (chooser.getFile() != null) {
/* 221 */       String filename = chooser.getDirectory() + chooser.getFile();
/* 222 */       if (!filename.endsWith(".txt")) filename = filename + ".txt";
/* 223 */       File output = new File(filename);
/*     */       try {
/* 225 */         FileWriter fw = new FileWriter(output);
/* 226 */         String endl = "\n";
/* 227 */         fw.write("Ancestral sequences reconstruction for tree '" + tree.getName() + "':" + endl);
/* 228 */         fw.write("----------------------------------------------------------" + endl);
/* 229 */         fw.write("Tree in Newick format with internal nodes labels : " + endl);
/* 230 */         fw.write(tree.toNewickLine(true, false) + endl + endl);
/* 231 */         List list = new ArrayList();
/* 232 */         if (node == null) list = tree.getInodes(); else
/* 233 */           list.add(node);
/* 234 */         if (node == null) {
/* 235 */           fw.write("Most probable sequences : " + endl);
/* 236 */           for (Node n : list) {
/* 237 */             fw.write(n.getLabel() + "\t" + tree.getMostProbableAncestralSequence(n) + endl);
/*     */           }
/* 239 */           fw.write(endl);
/*     */         }
/* 241 */         for (Node n : list) {
/* 242 */           DefaultEditorKit kit = new DefaultEditorKit();
/* 243 */           DefaultStyledDocument doc = tree.printAncestralStates(n);
/* 244 */           kit.write(fw, doc, 0, doc.getLength());
/* 245 */           fw.write(endl);
/*     */         }
/* 247 */         fw.close();
/*     */       } catch (Exception e) {
/* 249 */         e.printStackTrace();
/* 250 */         JOptionPane.showMessageDialog(this, Tools.getErrorPanel("Error", e), 
/* 251 */           "Error in distance file saving", 0);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void showAncestralStates(Tree tree, Node node) {
/* 257 */     this.emptyPanel.setVisible(false);
/* 258 */     this.ancestralPanel.setVisible(true);
/* 259 */     this.buttonExportNode.setEnabled(true);
/* 260 */     this.buttonExportTree.setEnabled(true);
/* 261 */     this.buttonOverlappingHisto.setEnabled(true);
/* 262 */     this.buttonCumulativeHisto.setEnabled(true);
/* 263 */     this.currentTree = tree;
/* 264 */     this.currentNode = node;
/* 265 */     this.ancestralHistoPanel.setSource(tree, node);
/*     */   }
/*     */ 
/*     */   public void update() {
/* 269 */     if (this.currentNode != null) this.ancestralHistoPanel.setSource(this.currentTree, this.currentNode); 
/*     */   }
/*     */ 
/*     */   public void showMessage(Message message, String text, Tree tree)
/*     */   {
/* 273 */     switch (message) {
/*     */     case CUSTOM:
/* 275 */       this.labelEmpty.setText("Select a tree in the list to display or export ancestral states");
/* 276 */       this.buttonExportTree.setEnabled(false);
/* 277 */       break;
/*     */     case ERROR:
/* 279 */       this.labelEmpty.setText("Select a node on the tree to display ancestral states");
/* 280 */       this.buttonExportTree.setEnabled(true);
/* 281 */       if (this.currentTree != null) this.currentTree.deleteLikelihoodComputation();
/* 282 */       this.currentTree = tree;
/* 283 */       break;
/*     */     case SELECT_NODE:
/* 285 */       this.labelEmpty.setText(text);
/* 286 */       break;
/*     */     case SELECT_TREE:
/* 288 */       this.labelEmpty.setText(text);
/*     */     }
/*     */ 
/* 291 */     this.emptyPanel.setVisible(true);
/* 292 */     this.ancestralPanel.setVisible(false);
/* 293 */     this.buttonExportNode.setEnabled(false);
/* 294 */     this.buttonOverlappingHisto.setEnabled(false);
/* 295 */     this.buttonCumulativeHisto.setEnabled(false);
/*     */   }
/*     */ 
/*     */   private class AncestralHistoPanel extends JPanel
/*     */   {
/*     */     private static final long serialVersionUID = 5472059197194507218L;
/* 300 */     private int COL_WIDTH = 12;
/* 301 */     private final int SCALE_WIDTH = 20;
/* 302 */     private final int SEQ_HEIGHT = 20;
/* 303 */     private int HISTO_HEIGHT = 60;
/* 304 */     private final int EMPTY_HEIGHT = 10;
/* 305 */     private final Color FOREGROUND = Color.GREEN;
/* 306 */     private final Color BACKGROUND = Color.BLACK;
/*     */ 
/* 308 */     Tree tree = null;
/* 309 */     Node node = null;
/* 310 */     double[][] as = null;
/* 311 */     DataType dt = DataType.DNA;
/*     */     int width;
/*     */     int height;
/* 313 */     int startX = 10;
/* 314 */     int startY = 20;
/*     */ 
/*     */     private AncestralHistoPanel() {  } 
/* 317 */     public void setSource(Tree tree, Node node) { this.tree = tree;
/* 318 */       this.node = node;
/*     */       try {
/* 320 */         this.dt = tree.getDataset().getDataType();
/* 321 */         switch ($SWITCH_TABLE$metapiga$modelization$data$DataType()[this.dt.ordinal()]) {
/*     */         case 1:
/* 323 */           this.COL_WIDTH = 12;
/* 324 */           this.HISTO_HEIGHT = 60;
/* 325 */           break;
/*     */         case 2:
/* 327 */           this.COL_WIDTH = 20;
/* 328 */           this.HISTO_HEIGHT = 80;
/* 329 */           break;
/*     */         case 4:
/* 331 */           this.COL_WIDTH = 60;
/* 332 */           this.HISTO_HEIGHT = 160;
/* 333 */           break;
/*     */         case 3:
/* 335 */           this.COL_WIDTH = 12;
/* 336 */           this.HISTO_HEIGHT = 60;
/*     */         }
/*     */ 
/* 339 */         this.as = tree.getAncestralStates(node);
/* 340 */         this.width = (this.startX + 20 + this.as.length * (this.COL_WIDTH + 1) + this.COL_WIDTH);
/* 341 */         this.height = (this.startY + 40 + 10 + this.HISTO_HEIGHT + 20);
/* 342 */         setSize(this.width, this.height);
/* 343 */         setPreferredSize(new Dimension(this.width, this.height));
/* 344 */         AncestralStatesPanel.this.scrollPane.getViewport().remove(this);
/* 345 */         repaint();
/* 346 */         AncestralStatesPanel.this.scrollPane.getViewport().add(this, null);
/*     */       } catch (Exception e) {
/* 348 */         e.printStackTrace();
/* 349 */         AncestralStatesPanel.this.showMessage(AncestralStatesPanel.Message.ERROR, Tools.getErrorMessage(e).replace("\n", " ; "), null);
/*     */       } }
/*     */ 
/*     */     public void paintComponent(Graphics g) {
/* 353 */       if ((this.tree != null) && (this.node != null) && (this.as != null)) {
/* 354 */         int x = this.startX;
/* 355 */         int y = this.startY;
/*     */         try {
/* 357 */           Font baseFont = g.getFont();
/* 358 */           Font tinyFont = new Font("Dialog", 0, 9);
/* 359 */           g.setColor(this.BACKGROUND);
/* 360 */           g.fillRect(0, 0, this.width, this.height);
/* 361 */           g.setColor(this.FOREGROUND);
/* 362 */           g.drawString("Ancestral state reconstruction for node " + this.node.getLabel(), x, y);
/* 363 */           y += 20;
/* 364 */           String s = "Conditional likelihood proportion (cL%) of ";
/* 365 */           int curX = x;
/* 366 */           g.drawString(s, curX, y);
/* 367 */           curX += (int)g.getFontMetrics().getStringBounds(s, g).getWidth();
/* 368 */           for (int state = 0; state < this.dt.numOfStates(); state++) {
/* 369 */             Data data = this.dt.getDataWithState(state);
/* 370 */             s = data + " ";
/* 371 */             g.setColor(data.getColor());
/* 372 */             g.drawString(s, curX, y);
/* 373 */             curX += (int)g.getFontMetrics().getStringBounds(s, g).getWidth();
/*     */           }
/* 375 */           g.setColor(this.FOREGROUND);
/* 376 */           if (AncestralStatesPanel.this.histoType == AncestralStatesPanel.HistoType.OVERLAPPING)
/* 377 */             g.drawString("(for each site, a colored bar represents the cL% of the corresponding " + this.dt.verbose().toLowerCase() + " cL%. " + this.dt.verbose() + " with lower cL% are in front of those with higher cL%.)", curX, y);
/* 378 */           else if (AncestralStatesPanel.this.histoType == AncestralStatesPanel.HistoType.CUMULATIVE) {
/* 379 */             g.drawString("(for each site, a column represent the total sum of cL% and is divided proportionnaly to each " + this.dt.verbose().toLowerCase() + " cL%. " + this.dt.verbose() + " with higher cL% are on top of those with lower cL%.)", curX, y);
/*     */           }
/* 381 */           if (AncestralStatesPanel.this.histoType == AncestralStatesPanel.HistoType.OVERLAPPING) {
/* 382 */             g.setFont(tinyFont);
/* 383 */             int TOP_HEIGHT = this.startY + 40 + 10;
/* 384 */             for (int h = 0; h <= 5; h++) {
/* 385 */               s = Tools.doubletoString(1.0D - h * 0.2D, 1);
/* 386 */               int sl = (int)(g.getFontMetrics().getStringBounds(s, g).getHeight() / 3.0D);
/* 387 */               g.drawString(s, this.startX, TOP_HEIGHT + h * this.HISTO_HEIGHT / 5 + sl);
/* 388 */               g.drawLine(this.startX + 20 - 4, TOP_HEIGHT + h * this.HISTO_HEIGHT / 5, this.startX + 20 - 2, TOP_HEIGHT + h * this.HISTO_HEIGHT / 5);
/*     */             }
/* 390 */             g.setColor(Color.DARK_GRAY);
/* 391 */             g.drawLine(this.startX + 20, TOP_HEIGHT + this.HISTO_HEIGHT, this.startX + 20 + this.as.length * this.COL_WIDTH, TOP_HEIGHT + this.HISTO_HEIGHT);
/* 392 */             g.setColor(this.FOREGROUND);
/* 393 */             g.setFont(baseFont);
/*     */           }
/* 395 */           x += 20;
/* 396 */           for (int site = 0; site < this.as.length; site++) {
/* 397 */             y = this.startY + 40;
/* 398 */             Data probState = this.dt.getMostProbableData(this.as[site]);
/* 399 */             s = probState != null ? probState.toString() : "-";
/* 400 */             g.setColor(probState.getColor());
/* 401 */             g.drawString(s, x + (this.COL_WIDTH - 6) / 2, y);
/* 402 */             y += 10;
/* 403 */             double sum = 0.0D;
/* 404 */             for (int state = 0; state < this.as[site].length; state++) {
/* 405 */               sum += this.as[site][state];
/*     */             }
/* 407 */             if (sum > 1.0D) sum = 1.0D;
/* 408 */             if (AncestralStatesPanel.this.histoType == AncestralStatesPanel.HistoType.CUMULATIVE)
/*     */             {
/* 410 */               List sorted = new ArrayList();
/* 411 */               sorted.add(Integer.valueOf(0));
/* 412 */               for (int state = 1; state < this.as[site].length; state++)
/*     */               {
/* 414 */                 for (int pos = 0; pos < sorted.size(); pos++) {
/* 415 */                   if (this.as[site][((Integer)sorted.get(pos)).intValue()] > this.as[site][state]) {
/* 416 */                     sorted.add(pos, Integer.valueOf(state));
/* 417 */                     break;
/*     */                   }
/*     */                 }
/* 420 */                 if (pos == sorted.size()) sorted.add(Integer.valueOf(state));
/*     */               }
/* 422 */               int curY = y + this.HISTO_HEIGHT;
/* 423 */               for (Iterator localIterator = sorted.iterator(); localIterator.hasNext(); ) { int state = ((Integer)localIterator.next()).intValue();
/* 424 */                 int height = (int)(this.as[site][state] / sum * this.HISTO_HEIGHT);
/*     */ 
/* 427 */                 curY -= height;
/* 428 */                 g.setColor(this.dt.getDataWithState(state).getColor());
/* 429 */                 g.fillRect(x, curY, this.COL_WIDTH, height);
/*     */               }
/* 431 */               g.fillRect(x, y, this.COL_WIDTH, curY - y);
/* 432 */             } else if (AncestralStatesPanel.this.histoType == AncestralStatesPanel.HistoType.OVERLAPPING)
/*     */             {
/* 434 */               List sorted = new ArrayList();
/* 435 */               sorted.add(Integer.valueOf(0));
/* 436 */               for (int state = 1; state < this.as[site].length; state++)
/*     */               {
/* 438 */                 for (int pos = 0; pos < sorted.size(); pos++) {
/* 439 */                   if (this.as[site][((Integer)sorted.get(pos)).intValue()] < this.as[site][state]) {
/* 440 */                     sorted.add(pos, Integer.valueOf(state));
/* 441 */                     break;
/*     */                   }
/*     */                 }
/* 444 */                 if (pos == sorted.size()) sorted.add(Integer.valueOf(state));
/*     */               }
/* 446 */               int nos = probState.numOfStates();
/* 447 */               for (int pos = 0; pos < sorted.size(); pos++) {
/* 448 */                 int state = ((Integer)sorted.get(pos)).intValue();
/* 449 */                 int height = (int)(this.as[site][state] / sum * this.HISTO_HEIGHT);
/*     */ 
/* 451 */                 g.setColor(this.dt.getDataWithState(state).getColor());
/* 452 */                 if (pos < nos)
/* 453 */                   g.fillRect(x + pos * (this.COL_WIDTH / nos), y + (this.HISTO_HEIGHT - height), this.COL_WIDTH / nos, height);
/*     */                 else {
/* 455 */                   g.fillRect(x, y + (this.HISTO_HEIGHT - height), this.COL_WIDTH, height);
/*     */                 }
/*     */               }
/*     */             }
/* 459 */             x += this.COL_WIDTH + 1;
/*     */           }
/* 461 */           g.setColor(this.FOREGROUND);
/*     */         } catch (Exception e) {
/* 463 */           e.printStackTrace();
/* 464 */           g.drawString(Tools.getErrorMessage(e).replace("\n", " ; "), 10, y + 20);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static enum HistoType
/*     */   {
/*  46 */     OVERLAPPING, CUMULATIVE;
/*     */   }
/*     */ 
/*     */   public static enum Message
/*     */   {
/*  45 */     SELECT_TREE, SELECT_NODE, ERROR, CUSTOM;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.AncestralStatesPanel
 * JD-Core Version:    0.6.2
 */