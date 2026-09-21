package hr.unizd.autocare.domain;

import java.math.BigDecimal;
import java.util.Collection;

/** Poznati zbroj i broj nepoznatih cijena. */
public final class CostSummary {
  private final BigDecimal knownTotal;
  private final long unknownCount;

  private CostSummary(BigDecimal knownTotal, long unknownCount) {
    this.knownTotal = knownTotal;
    this.unknownCount = unknownCount;
  }

  public static CostSummary of(Collection<BigDecimal> prices) {
    BigDecimal sum = BigDecimal.ZERO.setScale(2);
    long unknown = 0;

    for (BigDecimal price : prices) {
      if (price == null) {
        unknown++;
      } else {
        sum = sum.add(price);
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
