/*
 * Copyright 2006-2018 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MZmine 2; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.mzmine.datamodel.data.types.numbers;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.annotation.Nonnull;
import com.google.common.collect.Range;
import io.github.mzmine.main.MZmineCore;

public class IntensityRangeType extends NumberRangeType<Float> {
  // only used in cases where the mzmine config has no format
  private static final NumberFormat DEFAULT_FORMAT = new DecimalFormat("0.0E00");

  public IntensityRangeType(Range<Float> value) {
    super(value);
  }

  @Override
  public NumberFormat getFormatter() {
    try {
      return MZmineCore.getConfiguration().getIntensityFormat();
    } catch (NullPointerException e) {
      // only happens if types are used without initializing the MZmineCore
      return DEFAULT_FORMAT;
    }
  }

  @Override
  @Nonnull
  public String getHeaderString() {
    return "Intensity Range";
  }

}
