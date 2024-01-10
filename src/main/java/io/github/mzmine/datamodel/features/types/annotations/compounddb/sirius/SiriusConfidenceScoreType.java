package io.github.mzmine.datamodel.features.types.annotations.compounddb.sirius;

import io.github.mzmine.datamodel.features.types.numbers.abstr.ScoreType;
import org.jetbrains.annotations.NotNull;

public class SiriusConfidenceScoreType extends ScoreType {

  @Override
  public @NotNull String getUniqueID() {
    return "sirius_confidence_score";
  }

  @Override
  public @NotNull String getHeaderString() {
    return "Confidence (Sirius)";
  }
}
