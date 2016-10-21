/*     */ package metapiga.modelization.data.codons.tables;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.EnumSet;
/*     */ import java.util.Set;
/*     */ import metapiga.modelization.data.Codon;
/*     */ 
/*     */ public class UniversalCodonTransitionTable extends CodonTransitionTable
/*     */ {
/*     */   protected Set<Codon> stopCodons;
/*     */   protected Set<Codon> phenylalanine;
/*     */   protected Set<Codon> leucine;
/*     */   protected Set<Codon> isoleucine;
/*     */   protected Set<Codon> methionine;
/*     */   protected Set<Codon> valine;
/*     */   protected Set<Codon> serine;
/*     */   protected Set<Codon> proline;
/*     */   protected Set<Codon> threonine;
/*     */   protected Set<Codon> alanine;
/*     */   protected Set<Codon> tyrosine;
/*     */   protected Set<Codon> histidine;
/*     */   protected Set<Codon> glutamine;
/*     */   protected Set<Codon> asparagine;
/*     */   protected Set<Codon> lysine;
/*     */   protected Set<Codon> asparticAcid;
/*     */   protected Set<Codon> glutamicAcid;
/*     */   protected Set<Codon> cysteine;
/*     */   protected Set<Codon> tryptophan;
/*     */   protected Set<Codon> arginine;
/*     */   protected Set<Codon> glycine;
/*     */   protected Set<Codon> opal;
/*     */   protected Set<Codon> ochre;
/*     */   protected Set<Codon> amber;
/*     */   protected ArrayList<Set<Codon>> aminoacids;
/*     */ 
/*     */   public UniversalCodonTransitionTable()
/*     */   {
/*  10 */     this.stopCodons = EnumSet.of(Codon.TGA, Codon.TAA, Codon.TAG);
/*     */ 
/*  43 */     this.aminoacids = new ArrayList();
/*     */ 
/*  46 */     this.phenylalanine = EnumSet.of(Codon.TTT, Codon.TTC);
/*  47 */     this.leucine = EnumSet.of(Codon.CTT, Codon.CTC, Codon.CTA, Codon.CTG);
/*  48 */     this.leucine.add(Codon.TTA);
/*  49 */     this.leucine.add(Codon.TTG);
/*  50 */     this.isoleucine = EnumSet.of(Codon.ATT, Codon.ATC, Codon.ATA);
/*  51 */     this.methionine = EnumSet.of(Codon.ATG);
/*  52 */     this.valine = EnumSet.of(Codon.GTT, Codon.GTC, Codon.GTA, Codon.GTG);
/*  53 */     this.serine = EnumSet.of(Codon.TCT, Codon.TCC, Codon.TCA, Codon.TCG);
/*  54 */     this.proline = EnumSet.of(Codon.CCT, Codon.CCA, Codon.CCC, Codon.CCG);
/*  55 */     this.threonine = EnumSet.of(Codon.ACT, Codon.ACC, Codon.ACA, Codon.ACG);
/*  56 */     this.alanine = EnumSet.of(Codon.GCT, Codon.GCC, Codon.GCA, Codon.GCG);
/*  57 */     this.tyrosine = EnumSet.of(Codon.TAT, Codon.TAC);
/*  58 */     this.ochre = EnumSet.of(Codon.TAA);
/*  59 */     this.amber = EnumSet.of(Codon.TAG);
/*  60 */     this.histidine = EnumSet.of(Codon.CAT, Codon.CAC);
/*  61 */     this.glutamine = EnumSet.of(Codon.CAA, Codon.CAG);
/*  62 */     this.asparagine = EnumSet.of(Codon.AAT, Codon.AAC);
/*  63 */     this.lysine = EnumSet.of(Codon.AAA, Codon.AAG);
/*  64 */     this.asparticAcid = EnumSet.of(Codon.GAT, Codon.GAC);
/*  65 */     this.glutamicAcid = EnumSet.of(Codon.GAA, Codon.GAG);
/*  66 */     this.cysteine = EnumSet.of(Codon.TGT, Codon.TGC);
/*  67 */     this.opal = EnumSet.of(Codon.TGA);
/*  68 */     this.tryptophan = EnumSet.of(Codon.TGG);
/*  69 */     this.arginine = EnumSet.of(Codon.CGT, Codon.CGC, Codon.CGA, Codon.CGG);
/*  70 */     this.serine.add(Codon.AGT);
/*  71 */     this.serine.add(Codon.AGC);
/*  72 */     this.arginine.add(Codon.AGA);
/*  73 */     this.arginine.add(Codon.AGG);
/*  74 */     this.glycine = EnumSet.of(Codon.GGT, Codon.GGC, Codon.GGA, Codon.GGG);
/*     */ 
/*  76 */     this.aminoacids.add(this.alanine);
/*  77 */     this.aminoacids.add(this.arginine);
/*  78 */     this.aminoacids.add(this.asparagine);
/*  79 */     this.aminoacids.add(this.cysteine);
/*  80 */     this.aminoacids.add(this.glutamine);
/*  81 */     this.aminoacids.add(this.glycine);
/*  82 */     this.aminoacids.add(this.histidine);
/*  83 */     this.aminoacids.add(this.isoleucine);
/*  84 */     this.aminoacids.add(this.leucine);
/*  85 */     this.aminoacids.add(this.lysine);
/*  86 */     this.aminoacids.add(this.methionine);
/*  87 */     this.aminoacids.add(this.phenylalanine);
/*  88 */     this.aminoacids.add(this.proline);
/*  89 */     this.aminoacids.add(this.serine);
/*  90 */     this.aminoacids.add(this.threonine);
/*  91 */     this.aminoacids.add(this.tyrosine);
/*  92 */     this.aminoacids.add(this.valine);
/*  93 */     this.aminoacids.add(this.asparticAcid);
/*  94 */     this.aminoacids.add(this.glutamicAcid);
/*  95 */     this.aminoacids.add(this.tryptophan);
/*     */   }
/*     */ 
/*     */   public boolean isSynonymous(Codon fromCodon, Codon toCodon)
/*     */   {
/* 100 */     for (Set aminoacid : this.aminoacids) {
/* 101 */       if ((aminoacid.contains(fromCodon)) && (aminoacid.contains(toCodon)))
/* 102 */         return true;
/*     */     }
/* 104 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean isStopCodon(Codon codon)
/*     */   {
/* 110 */     if (this.stopCodons.contains(codon))
/* 111 */       return true;
/* 112 */     return false;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.UniversalCodonTransitionTable
 * JD-Core Version:    0.6.2
 */