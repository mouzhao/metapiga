/*     */ package metapiga.trees;
/*     */ 
/*     */ import java.awt.Color;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Font;
/*     */ import java.awt.FontMetrics;
/*     */ import java.awt.Graphics;
/*     */ import java.awt.Graphics2D;
/*     */ import java.awt.Point;
/*     */ import java.awt.event.MouseAdapter;
/*     */ import java.awt.event.MouseEvent;
/*     */ import java.awt.event.MouseMotionAdapter;
/*     */ import java.awt.geom.Arc2D;
/*     */ import java.awt.geom.Arc2D.Double;
/*     */ import java.awt.geom.Rectangle2D;
/*     */ import java.awt.image.BufferedImage;
/*     */ import java.awt.print.PageFormat;
/*     */ import java.awt.print.Printable;
/*     */ import java.awt.print.PrinterException;
/*     */ import java.io.File;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import javax.imageio.ImageIO;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.RepaintManager;
/*     */ import metapiga.trees.exceptions.NullAncestorException;
/*     */ import metapiga.trees.exceptions.UnrootableTreeException;
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public class TreePanel extends JPanel
/*     */   implements Printable
/*     */ {
/*  40 */   public static final Color COLOR_BACKGROUND_SCREEN = Color.BLACK;
/*  41 */   public static final Color COLOR_TREE_SCREEN = Color.GREEN;
/*  42 */   public static final Color COLOR_BL_SCREEN = Color.RED;
/*  43 */   public static final Color COLOR_CONSENSUS_SCREEN = Color.YELLOW;
/*  44 */   public static final Color COLOR_SELECTION_SCREEN = Color.CYAN;
/*  45 */   public static final Color COLOR_BACKGROUND_PRINT = Color.WHITE;
/*  46 */   public static final Color COLOR_TREE_PRINT = Color.BLACK;
/*  47 */   public static final Color COLOR_BL_PRINT = Color.RED;
/*  48 */   public static final Color COLOR_CONSENSUS_PRINT = Color.BLUE;
/*  49 */   public static final Color COLOR_SELECTION_PRINT = Color.BLACK;
/*     */ 
/*  51 */   private Color COLOR_BACKGROUND = COLOR_BACKGROUND_SCREEN;
/*  52 */   private Color COLOR_TREE = COLOR_TREE_SCREEN;
/*  53 */   private Color COLOR_BL = COLOR_BL_SCREEN;
/*  54 */   private Color COLOR_CONSENSUS = COLOR_CONSENSUS_SCREEN;
/*  55 */   private Color COLOR_SELECTION = COLOR_SELECTION_SCREEN;
/*     */   public Tree tree;
/*     */   public Type treeType;
/*     */   public boolean drawBranchLength;
/*     */   public boolean drawInodeLabel;
/*  61 */   private String longestLeafLabel = "";
/*     */   private String message;
/*  63 */   private final Map<Node, NodeCoordinates> nodeCoordinates = new HashMap();
/*  64 */   private Node selectedNode = null;
/*     */   private Font baseFont;
/*     */   private Font bigFont;
/*  67 */   private boolean phylogramComputationDone = false;
/*  68 */   private double longestBranch = 0.0D;
/*  69 */   private double shortestBranch = 1.7976931348623157E+308D;
/*     */   private double[] leafPaths;
/*  72 */   private boolean printing = false;
/*     */ 
/*     */   public TreePanel(Tree tree, Type treeType, boolean drawInodeLabel, boolean drawBranchLength, AncestralStatesPanel ancestralStatesPanel)
/*     */   {
/* 136 */     this.tree = tree;
/*     */     try {
/* 138 */       for (Node n : tree.getNodes()) {
/* 139 */         this.nodeCoordinates.put(n, new NodeCoordinates(n));
/*     */       }
/* 141 */       if (!tree.isRooted()) tree.root();
/* 142 */       this.longestLeafLabel = tree.getLongestTaxon();
/* 143 */       this.leafPaths = new double[tree.getNumOfLeaves()];
/* 144 */       this.treeType = treeType;
/* 145 */       this.drawInodeLabel = drawInodeLabel;
/* 146 */       this.drawBranchLength = drawBranchLength;
/* 147 */       if ((getWidth() < tree.getNumOfLeaves() * 2) || 
/* 148 */         (getHeight() < tree.getNumOfLeaves() * 20)) {
/* 149 */         setPreferredSize(new Dimension(tree.getNumOfLeaves() * 2, tree.getNumOfLeaves() * 20));
/*     */       }
/* 151 */       if (ancestralStatesPanel != null) {
/* 152 */         final AncestralStatesPanel asp = ancestralStatesPanel;
/* 153 */         addMouseListener(new MouseAdapter() {
/*     */           public void mouseClicked(MouseEvent e) {
/* 155 */             for (TreePanel.NodeCoordinates nc : TreePanel.this.nodeCoordinates.values()) {
/* 156 */               if (nc.contains(e.getPoint())) {
/* 157 */                 asp.showMessage(AncestralStatesPanel.Message.CUSTOM, "Reconstructing ancestral states for node " + TreePanel.NodeCoordinates.access$0(nc).getLabel() + ", please wait ...", null);
/* 158 */                 asp.showAncestralStates(TreePanel.this.tree, TreePanel.NodeCoordinates.access$0(nc));
/* 159 */                 TreePanel.this.selectedNode = TreePanel.NodeCoordinates.access$0(nc);
/* 160 */                 break;
/*     */               }
/* 162 */               asp.showMessage(AncestralStatesPanel.Message.SELECT_NODE, null, null);
/* 163 */               TreePanel.this.selectedNode = null;
/*     */             }
/* 165 */             TreePanel.this.repaint();
/*     */           }
/*     */         });
/* 168 */         addMouseMotionListener(new MouseMotionAdapter() {
/*     */           public void mouseMoved(MouseEvent e) {
/* 170 */             for (TreePanel.NodeCoordinates nc : TreePanel.this.nodeCoordinates.values()) {
/* 171 */               if (nc.contains(e.getPoint()))
/* 172 */                 nc.setHovered(true);
/*     */               else {
/* 174 */                 nc.setHovered(false);
/*     */               }
/* 176 */               TreePanel.this.repaint();
/*     */             }
/*     */           }
/*     */         });
/*     */       }
/*     */     }
/*     */     catch (Exception e) {
/* 183 */       e.printStackTrace();
/* 184 */       add(new JLabel(Tools.getErrorMessage(e)));
/*     */     }
/*     */   }
/*     */ 
/*     */   public TreePanel(Tree tree, Type treeType, boolean drawInodeLabel, boolean drawBranchLength) {
/* 189 */     this(tree, treeType, drawInodeLabel, drawBranchLength, null);
/*     */   }
/*     */ 
/*     */   public TreePanel(Tree tree) {
/* 193 */     this(tree, Type.RECTANGULAR_CLADOGRAM, false, false, null);
/*     */   }
/*     */ 
/*     */   public TreePanel(String message)
/*     */   {
/* 198 */     this.message = message;
/*     */   }
/*     */ 
/*     */   public void changeDrawingType(Type treeType) {
/* 202 */     this.treeType = treeType;
/* 203 */     repaint();
/*     */   }
/*     */ 
/*     */   public void paintComponent(Graphics g) {
/* 207 */     Dimension dim = getSize();
/* 208 */     this.baseFont = g.getFont();
/* 209 */     this.bigFont = this.baseFont.deriveFont(1, this.baseFont.getSize() + 2);
/* 210 */     if (this.tree != null) {
/*     */       try {
/* 212 */         drawTree(g, (int)dim.getWidth(), (int)dim.getHeight(), this.treeType, this.drawInodeLabel);
/*     */       } catch (Exception e) {
/* 214 */         e.printStackTrace();
/*     */       }
/*     */     } else {
/* 217 */       int sl = (int)g.getFontMetrics().getStringBounds(this.message, g).getWidth() / 2;
/* 218 */       g.drawString(this.message, (int)dim.getWidth() / 2 - sl, (int)dim.getHeight() / 2);
/*     */     }
/*     */   }
/*     */ 
/*     */   private void drawTree(Graphics g, int width, int height, Type drawType, boolean drawInodeLabel) throws UnrootableTreeException, NullAncestorException
/*     */   {
/* 224 */     g.setColor(this.COLOR_BACKGROUND);
/* 225 */     g.fillRect(0, 0, width, height);
/* 226 */     g.setColor(this.COLOR_TREE);
/* 227 */     Node root = this.tree.getRoot();
/*     */ 
/* 230 */     int llll = (int)g.getFontMetrics().getStringBounds(this.longestLeafLabel, g).getWidth();
/* 231 */     switch (drawType) {
/*     */     case RECTANGULAR_CLADOGRAM:
/* 233 */       int x = 10;
/* 234 */       int y = 15;
/* 235 */       int baseWidth = (width - llll - x * 2) / this.tree.getLevel(this.tree.getRoot());
/* 236 */       int baseHeight = (height - y * 2) / (this.tree.getNumOfLeaves() - 1);
/* 237 */       if (this.printing) x += (width - llll - x * 2 - baseWidth * this.tree.getLevel(this.tree.getRoot())) / 2;
/* 238 */       if (this.printing) y += (height - y * 2 - baseHeight * (this.tree.getNumOfLeaves() - 1)) / 2;
/* 239 */       drawNode(root, g, x, y, baseWidth, baseHeight, drawInodeLabel);
/* 240 */       break;
/*     */     case RADIAL_TREE:
/* 242 */       int x = 10;
/* 243 */       int y = 15;
/* 244 */       int baseWidth = (width - llll - x * 2) / (this.tree.getNumOfLeaves() - 1);
/* 245 */       int baseHeight = (height - y * 2) / (this.tree.getNumOfLeaves() - 1);
/* 246 */       if (this.printing) x += (width - llll - x * 2 - baseWidth * (this.tree.getNumOfLeaves() - 1)) / 2;
/* 247 */       if (this.printing) y += (height - y * 2 - baseHeight * (this.tree.getNumOfLeaves() - 1)) / 2;
/* 248 */       drawNodeClad(root, g, x, y, baseWidth, baseHeight, drawInodeLabel);
/* 249 */       break;
/*     */     case SLANTED_CLADOGRAM:
/* 251 */       if (!this.phylogramComputationDone) {
/* 252 */         computeExtrema(this.tree.getRoot());
/* 253 */         computeLeafPaths();
/* 254 */         this.phylogramComputationDone = true;
/*     */       }
/* 256 */       int x = 10;
/* 257 */       int y = 10;
/* 258 */       int baseWidth = (int)((width - llll - x * 2) / getLongestPath());
/* 259 */       int baseHeight = (height - 50) / (this.tree.getNumOfLeaves() - 1);
/* 260 */       if (this.printing) x = (int)(x + (width - llll - x * 2 - baseWidth * getLongestPath()) / 2.0D);
/* 261 */       if (this.printing) y += (height - y * 2 - baseHeight * (this.tree.getNumOfLeaves() - 1)) / 2;
/* 262 */       drawNodeWithBranchLength(root, g, x, y, baseWidth, baseHeight, drawInodeLabel);
/* 263 */       break;
/*     */     case PHYLOGRAM:
/* 265 */       int x = 10 + (width - 20) / 2;
/* 266 */       int y = 15 + (height - 30) / 2;
/* 267 */       int baseRadius = (Math.min(width - 2 * llll, height - 2 * llll) / 2 - 10) / (this.tree.getLevel(this.tree.getRoot()) - 1);
/* 268 */       double baseAngle = 6.283185307179586D / this.tree.getNumOfLeaves();
/* 269 */       y -= (height - 40 - 2 * llll - 2 * ((this.tree.getLevel(this.tree.getRoot()) - 1) * baseRadius)) / 2;
/* 270 */       drawNodeRadial(root, 0, 0.0D, g, x, y, baseRadius, baseAngle, drawInodeLabel);
/*     */     }
/*     */   }
/*     */ 
/*     */   private int drawNode(Node node, Graphics g, int x, int y, int baseWidth, int baseHeight, boolean drawInodeLabel)
/*     */     throws UnrootableTreeException, NullAncestorException
/*     */   {
/* 277 */     if (node.isLeaf()) {
/* 278 */       ((NodeCoordinates)this.nodeCoordinates.get(node)).draw(g, x, y);
/*     */     } else {
/* 280 */       List children = node.getChildren();
/* 281 */       int[] childX = new int[children.size()];
/* 282 */       int[] childY = new int[children.size()];
/* 283 */       int childrenAlreadyDrawn = 0;
/* 284 */       for (int i = 0; i < children.size(); i++) {
/* 285 */         childX[i] = (x + baseWidth * (this.tree.getLevel(node) - this.tree.getLevel((Node)children.get(i))));
/* 286 */         childY[i] = drawNode((Node)children.get(i), g, childX[i], y + childrenAlreadyDrawn, baseWidth, baseHeight, drawInodeLabel);
/* 287 */         childrenAlreadyDrawn += this.tree.getNumOfLeavesUnder((Node)children.get(i)) * baseHeight;
/*     */       }
/* 289 */       y = childY[0] + (childY[(children.size() - 1)] - childY[0]) / 2;
/* 290 */       for (int i = 0; i < children.size(); i++) {
/* 291 */         g.drawLine(x, y, x, childY[i]);
/* 292 */         g.drawLine(x, childY[i], childX[i], childY[i]);
/* 293 */         if ((drawInodeLabel) && (((Node)children.get(i)).isInode())) {
/* 294 */           ((NodeCoordinates)this.nodeCoordinates.get(children.get(i))).draw(g, childX[i], childY[i]);
/*     */         }
/* 296 */         if (this.drawBranchLength) {
/* 297 */           g.setColor(this.COLOR_BL);
/* 298 */           String s = Tools.doubletoString(((Node)children.get(i)).getAncestorBranchLength(), 4);
/* 299 */           int sl = (int)g.getFontMetrics().getStringBounds(s, g).getWidth() / 2;
/* 300 */           g.drawString(s, childX[i] - (childX[i] - x) / 2 - sl, childY[i] - 2);
/* 301 */           g.setColor(this.COLOR_TREE);
/*     */         }
/* 303 */         if ((((Node)children.get(i)).isInode()) && (((Node)children.get(i)).getClass() == ConsensusNode.class)) {
/* 304 */           g.setColor(this.COLOR_CONSENSUS);
/* 305 */           String s = Tools.doubletoString(((ConsensusNode)children.get(i)).getAncestorBranchStrength() * 100.0D, 0) + "%";
/* 306 */           int sl = (int)g.getFontMetrics().getStringBounds(s, g).getWidth() / 2;
/* 307 */           int sh = (int)g.getFontMetrics().getStringBounds(s, g).getHeight();
/* 308 */           g.drawString(s, childX[i] - (childX[i] - x) / 2 - sl, childY[i] + sh);
/* 309 */           g.setColor(this.COLOR_TREE);
/*     */         }
/*     */       }
/* 312 */       if ((node == this.tree.getRoot()) && (drawInodeLabel)) {
/* 313 */         ((NodeCoordinates)this.nodeCoordinates.get(node)).draw(g, x, childY[1]);
/*     */       }
/*     */     }
/* 316 */     return y;
/*     */   }
/*     */ 
/*     */   private int drawNodeClad(Node node, Graphics g, int x, int y, int baseWidth, int baseHeight, boolean drawInodeLabel) throws UnrootableTreeException, NullAncestorException {
/* 320 */     if (node.isLeaf()) {
/* 321 */       ((NodeCoordinates)this.nodeCoordinates.get(node)).draw(g, x, y);
/*     */     } else {
/* 323 */       List children = node.getChildren();
/* 324 */       int[] childX = new int[children.size()];
/* 325 */       int[] childY = new int[children.size()];
/* 326 */       int childrenAlreadyDrawn = 0;
/* 327 */       for (int i = 0; i < children.size(); i++) {
/* 328 */         childX[i] = (x + baseWidth * (this.tree.getNumOfLeavesUnder(node) - this.tree.getNumOfLeavesUnder((Node)children.get(i))));
/* 329 */         childY[i] = drawNodeClad((Node)children.get(i), g, childX[i], y + childrenAlreadyDrawn, baseWidth, baseHeight, drawInodeLabel);
/* 330 */         childrenAlreadyDrawn += this.tree.getNumOfLeavesUnder((Node)children.get(i)) * baseHeight;
/*     */       }
/* 332 */       y = ((NodeCoordinates)this.nodeCoordinates.get(this.tree.getPostorderTraversal(node).get(0))).y + baseHeight * (this.tree.getNumOfLeavesUnder(node) - 1) / 2;
/* 333 */       for (int i = 0; i < children.size(); i++) {
/* 334 */         g.drawLine(x, y, childX[i], childY[i]);
/* 335 */         if ((drawInodeLabel) && (((Node)children.get(i)).isInode())) {
/* 336 */           ((NodeCoordinates)this.nodeCoordinates.get(children.get(i))).draw(g, childX[i], childY[i]);
/*     */         }
/* 338 */         if (this.drawBranchLength) {
/* 339 */           g.setColor(this.COLOR_BL);
/* 340 */           String s = Tools.doubletoString(((Node)children.get(i)).getAncestorBranchLength(), 4);
/* 341 */           int sl = (int)g.getFontMetrics().getStringBounds(s, g).getWidth() / 2;
/* 342 */           g.drawString(s, childX[i] - (childX[i] - x) / 2 - sl, childY[i] - (childY[i] - y) / 2 - 2);
/* 343 */           g.setColor(this.COLOR_TREE);
/*     */         }
/* 345 */         if ((((Node)children.get(i)).isInode()) && (((Node)children.get(i)).getClass() == ConsensusNode.class)) {
/* 346 */           g.setColor(this.COLOR_CONSENSUS);
/* 347 */           String s = Tools.doubletoString(((ConsensusNode)children.get(i)).getAncestorBranchStrength() * 100.0D, 0) + "%";
/* 348 */           int sl = (int)g.getFontMetrics().getStringBounds(s, g).getWidth() / 2;
/* 349 */           int sh = (int)g.getFontMetrics().getStringBounds(s, g).getHeight();
/* 350 */           g.drawString(s, childX[i] - (childX[i] - x) / 2 - sl, childY[i] + sh);
/* 351 */           g.setColor(this.COLOR_TREE);
/*     */         }
/*     */       }
/* 354 */       if ((node == this.tree.getRoot()) && (drawInodeLabel)) {
/* 355 */         ((NodeCoordinates)this.nodeCoordinates.get(node)).draw(g, x, y);
/*     */       }
/*     */     }
/* 358 */     return y;
/*     */   }
/*     */ 
/*     */   private int drawNodeWithBranchLength(Node node, Graphics g, int x, int y, int baseWidth, int baseHeight, boolean drawInodeLabel) throws UnrootableTreeException, NullAncestorException {
/* 362 */     if (node == this.tree.getRoot()) {
/* 363 */       g.drawLine(x, y, x + baseWidth / 10, y);
/* 364 */       g.drawString("0.1", x + baseWidth / 10 + 10, y);
/* 365 */       y += 20;
/*     */     }
/* 367 */     if (node.isLeaf()) {
/* 368 */       ((NodeCoordinates)this.nodeCoordinates.get(node)).draw(g, x, y);
/*     */     } else {
/* 370 */       List children = node.getChildren();
/* 371 */       int[] childX = new int[children.size()];
/* 372 */       int[] childY = new int[children.size()];
/* 373 */       int childrenAlreadyDrawn = 0;
/* 374 */       for (int i = 0; i < children.size(); i++) {
/* 375 */         childX[i] = (x + (int)(baseWidth * ((Node)children.get(i)).getAncestorBranchLength()));
/* 376 */         childY[i] = drawNodeWithBranchLength((Node)children.get(i), g, childX[i], y + childrenAlreadyDrawn, baseWidth, baseHeight, drawInodeLabel);
/* 377 */         childrenAlreadyDrawn += this.tree.getNumOfLeavesUnder((Node)children.get(i)) * baseHeight;
/*     */       }
/* 379 */       y = childY[0] + (childY[(children.size() - 1)] - childY[0]) / 2;
/* 380 */       for (int i = 0; i < children.size(); i++) {
/* 381 */         g.drawLine(x, y, x, childY[i]);
/* 382 */         g.drawLine(x, childY[i], childX[i], childY[i]);
/* 383 */         if ((drawInodeLabel) && (((Node)children.get(i)).isInode())) {
/* 384 */           ((NodeCoordinates)this.nodeCoordinates.get(children.get(i))).draw(g, childX[i], childY[i]);
/*     */         }
/* 386 */         if ((this.drawBranchLength) && (((Node)children.get(i)).getAncestorBranchLength() > 0.0001D)) {
/* 387 */           g.setColor(this.COLOR_BL);
/* 388 */           String s = Tools.doubletoString(((Node)children.get(i)).getAncestorBranchLength(), 4);
/* 389 */           int sl = (int)g.getFontMetrics().getStringBounds(s, g).getWidth() / 2;
/* 390 */           g.drawString(s, childX[i] - (childX[i] - x) / 2 - sl, childY[i] - 2);
/* 391 */           g.setColor(this.COLOR_TREE);
/*     */         }
/* 393 */         if ((((Node)children.get(i)).isInode()) && (((Node)children.get(i)).getClass() == ConsensusNode.class) && (((Node)children.get(i)).getAncestorBranchLength() > 0.0001D)) {
/* 394 */           g.setColor(this.COLOR_CONSENSUS);
/* 395 */           String s = Tools.doubletoString(((ConsensusNode)children.get(i)).getAncestorBranchStrength() * 100.0D, 0) + "%";
/* 396 */           int sl = (int)g.getFontMetrics().getStringBounds(s, g).getWidth() / 2;
/* 397 */           int sh = (int)g.getFontMetrics().getStringBounds(s, g).getHeight();
/* 398 */           g.drawString(s, childX[i] - (childX[i] - x) / 2 - sl, childY[i] + sh);
/* 399 */           g.setColor(this.COLOR_TREE);
/*     */         }
/*     */       }
/* 402 */       if ((node == this.tree.getRoot()) && (drawInodeLabel)) {
/* 403 */         ((NodeCoordinates)this.nodeCoordinates.get(node)).draw(g, x, childY[1]);
/*     */       }
/*     */     }
/* 406 */     return y;
/*     */   }
/*     */ 
/*     */   private double drawNodeRadial(Node node, int radius, double angle, Graphics g, int x0, int y0, int baseRadius, double baseAngle, boolean drawInodeLabel) throws UnrootableTreeException, NullAncestorException {
/* 410 */     int x = x0 + (int)(radius * Math.cos(angle));
/* 411 */     int y = y0 - (int)(radius * Math.sin(angle));
/* 412 */     if (node.isLeaf())
/*     */     {
/* 424 */       ((NodeCoordinates)this.nodeCoordinates.get(node)).draw(g, x, y, angle);
/*     */     } else {
/* 426 */       List children = node.getChildren();
/* 427 */       int[] childRadius = new int[children.size()];
/* 428 */       double[] childAngle = new double[children.size()];
/* 429 */       double childrenAlreadyDrawn = 0.0D;
/* 430 */       for (int i = 0; i < children.size(); i++) {
/* 431 */         childRadius[i] = (baseRadius * (this.tree.getLevel(this.tree.getRoot()) - this.tree.getLevel((Node)children.get(i))));
/* 432 */         childAngle[i] = drawNodeRadial((Node)children.get(i), childRadius[i], angle + childrenAlreadyDrawn, g, x0, y0, baseRadius, baseAngle, drawInodeLabel);
/* 433 */         childrenAlreadyDrawn += this.tree.getNumOfLeavesUnder((Node)children.get(i)) * baseAngle;
/*     */       }
/* 435 */       angle = childAngle[0] + (childAngle[(children.size() - 1)] - childAngle[0]) / 2.0D;
/* 436 */       Graphics2D g2 = (Graphics2D)g;
/* 437 */       Arc2D arc = new Arc2D.Double();
/* 438 */       arc.setArcByCenter(x0, y0, radius, Math.toDegrees(childAngle[0]), Math.toDegrees(childAngle[(children.size() - 1)] - childAngle[0]), 0);
/* 439 */       g2.draw(arc);
/* 440 */       for (int i = 0; i < children.size(); i++) {
/* 441 */         int x1 = x0 + (int)(childRadius[i] * Math.cos(childAngle[i]));
/* 442 */         int y1 = y0 - (int)(childRadius[i] * Math.sin(childAngle[i]));
/* 443 */         int x2 = x0 + (int)(radius * Math.cos(childAngle[i]));
/* 444 */         int y2 = y0 - (int)(radius * Math.sin(childAngle[i]));
/* 445 */         g.drawLine(x1, y1, x2, y2);
/* 446 */         if ((drawInodeLabel) && (((Node)children.get(i)).isInode())) {
/* 447 */           ((NodeCoordinates)this.nodeCoordinates.get(children.get(i))).draw(g, x1, y1);
/*     */         }
/* 449 */         if (this.drawBranchLength) {
/* 450 */           g.setColor(this.COLOR_BL);
/* 451 */           String s = Tools.doubletoString(((Node)children.get(i)).getAncestorBranchLength(), 4);
/* 452 */           int sl = (int)g.getFontMetrics().getStringBounds(s, g).getWidth() / 2;
/* 453 */           g.drawString(s, x2 + (x1 - x2) / 2 - sl, y2 - (y2 - y1) / 2 - 2);
/* 454 */           g.setColor(this.COLOR_TREE);
/*     */         }
/* 456 */         if ((((Node)children.get(i)).isInode()) && (((Node)children.get(i)).getClass() == ConsensusNode.class)) {
/* 457 */           g.setColor(this.COLOR_CONSENSUS);
/* 458 */           String s = Tools.doubletoString(((ConsensusNode)children.get(i)).getAncestorBranchStrength() * 100.0D, 0) + "%";
/* 459 */           int sl = (int)g.getFontMetrics().getStringBounds(s, g).getWidth() / 2;
/* 460 */           int sh = (int)g.getFontMetrics().getStringBounds(s, g).getHeight();
/* 461 */           g.drawString(s, x2 + (x1 - x2) / 2 - sl, y2 - (y2 - y1) / 2 + sh);
/* 462 */           g.setColor(this.COLOR_TREE);
/*     */         }
/*     */       }
/* 465 */       if ((node == this.tree.getRoot()) && (drawInodeLabel)) {
/* 466 */         ((NodeCoordinates)this.nodeCoordinates.get(node)).draw(g, x, y);
/*     */       }
/*     */     }
/* 469 */     return angle;
/*     */   }
/*     */ 
/*     */   public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException
/*     */   {
/* 474 */     RepaintManager.currentManager(this).setDoubleBufferingEnabled(false);
/*     */ 
/* 476 */     int totalNumPages = 1;
/* 477 */     if (pageIndex >= totalNumPages) {
/* 478 */       return 1;
/*     */     }
/*     */ 
/* 481 */     this.printing = true;
/* 482 */     this.COLOR_BACKGROUND = COLOR_BACKGROUND_PRINT;
/* 483 */     this.COLOR_TREE = COLOR_TREE_PRINT;
/* 484 */     this.COLOR_BL = COLOR_BL_PRINT;
/* 485 */     this.COLOR_CONSENSUS = COLOR_CONSENSUS_PRINT;
/* 486 */     this.COLOR_SELECTION = COLOR_SELECTION_PRINT;
/* 487 */     int fontSize = 12;
/* 488 */     if (this.tree.getNumOfLeaves() < 20)
/* 489 */       fontSize = 8;
/* 490 */     else if (this.tree.getNumOfLeaves() < 30)
/* 491 */       fontSize = 9;
/* 492 */     else if (this.tree.getNumOfLeaves() < 40)
/* 493 */       fontSize = 10;
/* 494 */     else if (this.tree.getNumOfLeaves() < 50)
/* 495 */       fontSize = 11;
/* 496 */     else if (this.tree.getNumOfLeaves() < 60)
/* 497 */       fontSize = 12;
/* 498 */     else if (this.tree.getNumOfLeaves() < 70)
/* 499 */       fontSize = 13;
/* 500 */     else if (this.tree.getNumOfLeaves() < 80)
/* 501 */       fontSize = 14;
/* 502 */     else if (this.tree.getNumOfLeaves() < 90)
/* 503 */       fontSize = 15;
/* 504 */     else if (this.tree.getNumOfLeaves() < 100)
/* 505 */       fontSize = 16;
/* 506 */     else if (this.tree.getNumOfLeaves() < 200)
/* 507 */       fontSize = 17;
/* 508 */     else if (this.tree.getNumOfLeaves() < 300)
/* 509 */       fontSize = 18;
/* 510 */     else if (this.tree.getNumOfLeaves() < 400)
/* 511 */       fontSize = 19;
/* 512 */     else if (this.tree.getNumOfLeaves() < 500)
/* 513 */       fontSize = 20;
/*     */     else {
/* 515 */       fontSize = 22;
/*     */     }
/* 517 */     this.baseFont = new Font("Arial", 0, fontSize);
/* 518 */     g.setFont(this.baseFont);
/* 519 */     this.bigFont = this.baseFont.deriveFont(1, this.baseFont.getSize() + 2);
/*     */ 
/* 521 */     double pageHeight = pageFormat.getImageableHeight() - 30.0D;
/* 522 */     double pageWidth = pageFormat.getImageableWidth() - 30.0D;
/*     */ 
/* 524 */     if (this.tree != null) {
/*     */       try {
/* 526 */         int width = 0;
/* 527 */         int height = 0;
/* 528 */         switch ($SWITCH_TABLE$metapiga$trees$TreePanel$Type()[this.treeType.ordinal()]) {
/*     */         case 3:
/* 530 */           width = this.tree.getLevel(this.tree.getRoot()) * 100;
/* 531 */           height = (this.tree.getNumOfLeaves() - 1) * 30;
/* 532 */           break;
/*     */         case 2:
/* 534 */           width = (this.tree.getNumOfLeaves() - 1) * 100;
/* 535 */           height = (this.tree.getNumOfLeaves() - 1) * 50;
/* 536 */           break;
/*     */         case 4:
/* 538 */           if (!this.phylogramComputationDone) {
/* 539 */             computeExtrema(this.tree.getRoot());
/* 540 */             computeLeafPaths();
/* 541 */             this.phylogramComputationDone = true;
/*     */           }
/* 543 */           width = (int)(getLongestPath() * 100.0D);
/* 544 */           height = (this.tree.getNumOfLeaves() - 1) * 50;
/* 545 */           break;
/*     */         case 1:
/* 547 */           width = (this.tree.getLevel(this.tree.getRoot()) - 1) * 100;
/* 548 */           height = (this.tree.getLevel(this.tree.getRoot()) - 1) * 100;
/*     */         }
/*     */ 
/* 552 */         Graphics2D g2 = (Graphics2D)g;
/* 553 */         g2.translate(30, 30);
/* 554 */         double scalingFactor = Math.min(pageWidth / width, pageHeight / height);
/* 555 */         g2.scale(scalingFactor, scalingFactor);
/* 556 */         drawTree(g2, width, height, this.treeType, this.drawInodeLabel);
/*     */       } catch (Exception e) {
/* 558 */         e.printStackTrace();
/*     */       }
/*     */     } else {
/* 561 */       int sl = (int)g.getFontMetrics().getStringBounds(this.message, g).getWidth() / 2;
/* 562 */       g.drawString(this.message, (int)pageWidth / 2 - sl, (int)pageHeight / 2);
/*     */     }
/* 564 */     g.setColor(Color.black);
/* 565 */     g.drawString("MetaPIGA 2 - Tree " + this.tree.getName(), 15, 20);
/*     */ 
/* 567 */     this.printing = false;
/* 568 */     this.COLOR_BACKGROUND = COLOR_BACKGROUND_SCREEN;
/* 569 */     this.COLOR_TREE = COLOR_TREE_SCREEN;
/* 570 */     this.COLOR_BL = COLOR_BL_SCREEN;
/* 571 */     this.COLOR_CONSENSUS = COLOR_CONSENSUS_SCREEN;
/* 572 */     this.COLOR_SELECTION = COLOR_SELECTION_SCREEN;
/*     */ 
/* 574 */     return 0;
/*     */   }
/*     */ 
/*     */   public void exportToImage(File file, String format) throws Exception {
/* 578 */     int width = 0;
/* 579 */     int height = 0;
/* 580 */     switch ($SWITCH_TABLE$metapiga$trees$TreePanel$Type()[this.treeType.ordinal()]) {
/*     */     case 3:
/* 582 */       width = this.tree.getLevel(this.tree.getRoot()) * 100;
/* 583 */       height = (this.tree.getNumOfLeaves() - 1) * 30;
/* 584 */       break;
/*     */     case 2:
/* 586 */       width = (this.tree.getNumOfLeaves() - 1) * 100;
/* 587 */       height = (this.tree.getNumOfLeaves() - 1) * 50;
/* 588 */       break;
/*     */     case 4:
/* 590 */       if (!this.phylogramComputationDone) {
/* 591 */         computeExtrema(this.tree.getRoot());
/* 592 */         computeLeafPaths();
/* 593 */         this.phylogramComputationDone = true;
/*     */       }
/* 595 */       width = (int)(getLongestPath() * 100.0D);
/* 596 */       height = (this.tree.getNumOfLeaves() - 1) * 50;
/* 597 */       break;
/*     */     case 1:
/* 599 */       width = (this.tree.getLevel(this.tree.getRoot()) - 1) * 100;
/* 600 */       height = (this.tree.getLevel(this.tree.getRoot()) - 1) * 100;
/*     */     }
/*     */ 
/* 604 */     BufferedImage image = new BufferedImage(width, height, 1);
/* 605 */     Graphics g = image.createGraphics();
/*     */ 
/* 607 */     this.printing = true;
/* 608 */     this.COLOR_BACKGROUND = COLOR_BACKGROUND_PRINT;
/* 609 */     this.COLOR_TREE = COLOR_TREE_PRINT;
/* 610 */     this.COLOR_BL = COLOR_BL_PRINT;
/* 611 */     this.COLOR_CONSENSUS = COLOR_CONSENSUS_PRINT;
/* 612 */     this.COLOR_SELECTION = COLOR_SELECTION_PRINT;
/* 613 */     this.baseFont = new Font("Arial", 0, 12);
/* 614 */     g.setFont(this.baseFont);
/* 615 */     this.bigFont = this.baseFont.deriveFont(1, this.baseFont.getSize() + 2);
/*     */ 
/* 617 */     drawTree(g, width, height, this.treeType, this.drawInodeLabel);
/* 618 */     ImageIO.write(image, format, file);
/*     */ 
/* 620 */     this.printing = false;
/* 621 */     this.COLOR_BACKGROUND = COLOR_BACKGROUND_SCREEN;
/* 622 */     this.COLOR_TREE = COLOR_TREE_SCREEN;
/* 623 */     this.COLOR_BL = COLOR_BL_SCREEN;
/* 624 */     this.COLOR_CONSENSUS = COLOR_CONSENSUS_SCREEN;
/* 625 */     this.COLOR_SELECTION = COLOR_SELECTION_SCREEN;
/*     */   }
/*     */ 
/*     */   private void computeExtrema(Node node) throws NullAncestorException {
/* 629 */     if (node != this.tree.getRoot()) {
/* 630 */       if (node.getAncestorBranchLength() > this.longestBranch) {
/* 631 */         this.longestBranch = node.getAncestorBranchLength();
/*     */       }
/* 633 */       if (node.getAncestorBranchLength() < this.shortestBranch) {
/* 634 */         this.shortestBranch = node.getAncestorBranchLength();
/*     */       }
/*     */     }
/* 637 */     if (!node.isLeaf())
/* 638 */       for (Node n : node.getChildren())
/* 639 */         computeExtrema(n);
/*     */   }
/*     */ 
/*     */   private void computeLeafPaths()
/*     */     throws NullAncestorException
/*     */   {
/* 645 */     int i = 0;
/* 646 */     for (Node n : this.tree.getLeaves()) {
/* 647 */       this.leafPaths[i] = 0.0D;
/* 648 */       while (n != this.tree.getRoot()) {
/* 649 */         this.leafPaths[i] += n.getAncestorBranchLength();
/* 650 */         n = n.getAncestorNode();
/*     */       }
/* 652 */       i++;
/*     */     }
/*     */   }
/*     */ 
/*     */   public double getLongestPath() {
/* 657 */     double res = 0.0D;
/* 658 */     for (double d : this.leafPaths) {
/* 659 */       if (d > res) {
/* 660 */         res = d;
/*     */       }
/*     */     }
/*     */ 
/* 664 */     return res;
/*     */   }
/*     */ 
/*     */   private class NodeCoordinates
/*     */   {
/*     */     private final Node node;
/*  76 */     private int x = 0; private int y = 0;
/*  77 */     private int lx = 0; private int rx = 0; private int ty = 0; private int by = 0;
/*  78 */     private boolean hovered = false;
/*     */ 
/*  80 */     public NodeCoordinates(Node node) { this.node = node; }
/*     */ 
/*     */     public void draw(Graphics g, int X, int Y) {
/*  83 */       draw(g, X, Y, 0.0D);
/*     */     }
/*     */ 
/*     */     public void draw(Graphics g, int X, int Y, double angle) {
/*  87 */       boolean sel = (this.hovered) || (this.node == TreePanel.this.selectedNode);
/*  88 */       if (TreePanel.this.printing) sel = false;
/*  89 */       this.x = X;
/*  90 */       this.y = Y;
/*  91 */       if (this.node.isLeaf()) {
/*  92 */         g.setFont(sel ? TreePanel.this.bigFont : TreePanel.this.baseFont);
/*  93 */         this.lx = (this.x + 10);
/*  94 */         this.rx = (this.lx + (int)g.getFontMetrics().getStringBounds(this.node.getLabel(), g).getWidth());
/*  95 */         this.by = (this.y + 4);
/*  96 */         this.ty = (this.by - (int)Math.round(g.getFontMetrics().getStringBounds(this.node.getLabel(), g).getHeight()));
/*  97 */         g.setColor(sel ? TreePanel.this.COLOR_SELECTION : TreePanel.this.COLOR_TREE);
/*  98 */         if (angle == 0.0D) {
/*  99 */           g.drawString(this.node.getLabel(), this.lx, this.by);
/*     */         } else {
/* 101 */           Graphics2D g2 = (Graphics2D)g;
/* 102 */           g2.rotate(-angle, this.x, this.y);
/* 103 */           g2.drawString(this.node.getLabel(), this.lx, this.by);
/* 104 */           g2.rotate(angle, this.x, this.y);
/*     */         }
/* 106 */         g.setColor(TreePanel.this.COLOR_TREE);
/* 107 */         g.setFont(TreePanel.this.baseFont);
/* 108 */       } else if ((this.node.isInode()) && (this.node.getLabel() != null)) {
/* 109 */         g.setFont(sel ? TreePanel.this.bigFont : TreePanel.this.baseFont);
/* 110 */         String cLab = this.node.getLabel();
/* 111 */         Integer.parseInt(cLab);
/* 112 */         int sl = (int)g.getFontMetrics().getStringBounds(cLab, g).getWidth() / 2 + 4;
/* 113 */         int sh = (int)Math.round(g.getFontMetrics().getStringBounds(cLab, g).getHeight() / 2.0D) + 1;
/* 114 */         this.lx = (sel ? this.x - sl - 3 : this.x - sl);
/* 115 */         this.rx = (sel ? this.x + sl + 3 : this.x + sl);
/* 116 */         this.ty = (sel ? this.y - sh - 3 : this.y - sh);
/* 117 */         this.by = (sel ? this.y + sh + 3 : this.y + sh);
/* 118 */         g.setColor(sel ? TreePanel.this.COLOR_SELECTION : TreePanel.this.COLOR_TREE);
/* 119 */         g.fillRect(this.lx, this.ty, this.rx - this.lx, this.by - this.ty);
/* 120 */         g.setColor(TreePanel.this.COLOR_BACKGROUND);
/* 121 */         g.drawString(cLab, this.lx + (sel ? 7 : 4), this.y + 4);
/* 122 */         g.setColor(TreePanel.this.COLOR_TREE);
/* 123 */         g.setFont(TreePanel.this.baseFont);
/*     */       }
/*     */     }
/*     */ 
/* 127 */     public boolean contains(Point p) { return (p.x >= this.lx) && (p.x <= this.rx) && (p.y >= this.ty) && (p.y <= this.by); }
/*     */ 
/*     */     public void setHovered(boolean isHovered) {
/* 130 */       this.hovered = isHovered;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static enum Type
/*     */   {
/*  39 */     RADIAL_TREE, SLANTED_CLADOGRAM, RECTANGULAR_CLADOGRAM, PHYLOGRAM;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.TreePanel
 * JD-Core Version:    0.6.2
 */