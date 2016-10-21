/*     */ package metapiga.modelization.data.codons.tables;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.EnumSet;
/*     */ import java.util.Set;
/*     */ import metapiga.modelization.data.Codon;
/*     */ 
/*     */ public class VertebrateMitochondrialCode extends CodonTransitionTable
/*     */ {
/*  11 */   protected Set<Codon> stopCodons = EnumSet.of(Codon.TAA, Codon.TAG, Codon.AGG, Codon.AGA);
/*     */ 
/*  47 */   protected Set<Codon> phenylalanine = EnumSet.of(Codon.TTT, Codon.TTC);
/*  48 */   protected Set<Codon> leucine = EnumSet.of(Codon.CTT, Codon.CTC, Codon.CTA, Codon.CTG);
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
/*  44 */   protected ArrayList<Set<Codon>> aminoacids = new ArrayList();
/*     */ 
/*     */   public VertebrateMitochondrialCode()
/*     */   {
/*  49 */     this.leucine.add(Codon.TTA);
/*  50 */     this.leucine.add(Codon.TTG);
/*  51 */     this.isoleucine = EnumSet.of(Codon.ATT, Codon.ATC);
/*  52 */     this.methionine = EnumSet.of(Codon.ATG, Codon.ATA);
/*  53 */     this.valine = EnumSet.of(Codon.GTT, Codon.GTC, Codon.GTA, Codon.GTG);
/*  54 */     this.serine = EnumSet.of(Codon.TCT, Codon.TCC, Codon.TCA, Codon.TCG);
/*  55 */     this.proline = EnumSet.of(Codon.CCT, Codon.CCA, Codon.CCC, Codon.CCG);
/*  56 */     this.threonine = EnumSet.of(Codon.ACT, Codon.ACC, Codon.ACA, Codon.ACG);
/*  57 */     this.alanine = EnumSet.of(Codon.GCT, Codon.GCC, Codon.GCA, Codon.GCG);
/*  58 */     this.tyrosine = EnumSet.of(Codon.TAT, Codon.TAC);
/*  59 */     this.histidine = EnumSet.of(Codon.CAT, Codon.CAC);
/*  60 */     this.glutamine = EnumSet.of(Codon.CAA, Codon.CAG);
/*  61 */     this.asparagine = EnumSet.of(Codon.AAT, Codon.AAC);
/*  62 */     this.lysine = EnumSet.of(Codon.AAA, Codon.AAG);
/*  63 */     this.asparticAcid = EnumSet.of(Codon.GAT, Codon.GAC);
/*  64 */     this.glutamicAcid = EnumSet.of(Codon.GAA, Codon.GAG);
/*  65 */     this.cysteine = EnumSet.of(Codon.TGT, Codon.TGC);
/*  66 */     this.tryptophan = EnumSet.of(Codon.TGG, Codon.TGA);
/*  67 */     this.arginine = EnumSet.of(Codon.CGT, Codon.CGC, Codon.CGA, Codon.CGG);
/*  68 */     this.serine.add(Codon.AGT);
/*  69 */     this.serine.add(Codon.AGC);
/*  70 */     this.glycine = EnumSet.of(Codon.GGT, Codon.GGC, Codon.GGA, Codon.GGG);
/*     */ 
/*  72 */     this.ochre = EnumSet.of(Codon.TAA);
/*  73 */     this.amber = EnumSet.of(Codon.TAG);
/*     */ 
/*  75 */     this.aminoacids.add(this.alanine);
/*  76 */     this.aminoacids.add(this.arginine);
/*  77 */     this.aminoacids.add(this.asparagine);
/*  78 */     this.aminoacids.add(this.cysteine);
/*  79 */     this.aminoacids.add(this.glutamine);
/*  80 */     this.aminoacids.add(this.glycine);
/*  81 */     this.aminoacids.add(this.histidine);
/*  82 */     this.aminoacids.add(this.isoleucine);
/*  83 */     this.aminoacids.add(this.leucine);
/*  84 */     this.aminoacids.add(this.lysine);
/*  85 */     this.aminoacids.add(this.methionine);
/*  86 */     this.aminoacids.add(this.phenylalanine);
/*  87 */     this.aminoacids.add(this.proline);
/*  88 */     this.aminoacids.add(this.serine);
/*  89 */     this.aminoacids.add(this.threonine);
/*  90 */     this.aminoacids.add(this.tyrosine);
/*  91 */     this.aminoacids.add(this.valine);
/*  92 */     this.aminoacids.add(this.asparticAcid);
/*  93 */     this.aminoacids.add(this.glutamicAcid);
/*  94 */     this.aminoacids.add(this.tryptophan);
/*     */   }
/*     */ 
/*     */   public boolean isSynonymous(Codon fromCodon, Codon toCodon)
/*     */   {
/* 103 */     for (Set aminoacid : this.aminoacids) {
/* 104 */       if ((aminoacid.contains(fromCodon)) && (aminoacid.contains(toCodon)))
/* 105 */         return true;
/*     */     }
/* 107 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean isStopCodon(Codon codon)
/*     */   {
/* 113 */     if (this.stopCodons.contains(codon))
/* 114 */       return true;
/* 115 */     return false;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.VertebrateMitochondrialCode
 * JD-Core Version:    0.6.2
 */