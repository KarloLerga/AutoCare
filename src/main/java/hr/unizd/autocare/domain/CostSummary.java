package hr.unizd.autocare.domain;

import java.math.BigDecimal;
import java.util.Collection;

/** Poznati zbroj i broj nepoznatih cijena. Nepoznato nije nula. */
public final class CostSummary {
  private final BigDecimal knownTotal;
  private final long unknownCount;

  public CostSummary(BigDecimal knownTotal, long unknownCount) {
    this.knownTotal = knownTotal == null ? BigDecimal.ZERO.setScale(2) : knownTotal;
    this.unknownCount = unknownCount;
  }

  public static CostSummary of(Collection<BigDecimal> prices) {
    BigDecimal sum = BigDecimal.ZERO.setScale(2);
    long unknown = 0;
    for (BigDecimal p : prices) {
      if (p == null) {
        unknown++;
      } else {
        sum = sum.add(p);
      }
    }
    return new CostSummary(sum, unknown);
  }

  public BigDecimal getKnownTotal() {
    return knownTotal;
  }

  public long getUnknownCount() {
    return unknownCount;
  }
}
