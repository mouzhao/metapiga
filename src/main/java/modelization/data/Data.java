package modelization.data;

import java.awt.Color;
import java.util.BitSet;

public abstract interface Data
{
  public abstract char toChar();

  public abstract BitSet toBits();

  public abstract int numOfStates();

  public abstract int getMaxStates();

  public abstract int getState();

  public abstract Color getColor();

  public abstract boolean isState(int paramInt);

  public abstract boolean isUndeterminate();
}

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.modelization.data.Data
 * JD-Core Version:    0.6.2
 */