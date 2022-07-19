package org.sheinbergon.needle.jna.linux.structure;

import com.sun.jna.Structure;

import java.util.List;

@SuppressWarnings({"MemberName", "VisibilityModifier"})
public class CpuSet extends Structure {

  /**
   * sched.h derived CPU set-size constant.
   */
  private static final int CPU_SETSIZE = 1024;

  /**
   * sched.h derived CPU bitmask size.
   */
  private static final int NCPUBITS = Long.SIZE;

  /**
   * JNA structure 'bits' field.
   */
  public long[] __bits = new long[CPU_SETSIZE / NCPUBITS];

  /**
   * Instantiate this {@link Structure}, disable synchronization and alignment optimization.
   *
   * @see Structure#setAlignType(int)
   * @see Structure#setAutoSynch(boolean)
   */
  public CpuSet() {
    setAlignType(ALIGN_NONE);
    setAutoSynch(true);
  }

  /**
   * @return JNA {@link Structure} field older.
   */
  @Override
  protected List<String> getFieldOrder() {
    return List.of("__bits");
  }

  /**
   * Get the size of the cpu-set bitmask in bytes.
   *
   * @return the size of the cpu-set bitmask in bytes
   */
  public int bytes() {
    return Long.BYTES * __bits.length;
  }

  /**
   * Zero-fill the memory backing this {@link Structure}.
   */
  public void zero() {
    this.clear();
  }
}
