/*
 * Copyright 2006-2021 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package io.github.mzmine.datamodel.msdk;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * <p>
 * DataPointSorter class.
 * </p>
 */
public class DataPointSorter {

  public enum SortingProperty {
    MZ, INTENSITY
  }

  public enum SortingDirection {
    ASCENDING, DESCENDING
  }

  private static class DataPointComparator implements Comparator<Integer> {

    private final double mzBuffer[];
    private final float intensityBuffer[];
    private final SortingProperty prop;
    private final SortingDirection dir;

    DataPointComparator(final double mzBuffer[], final float intensityBuffer[],
        SortingProperty prop, SortingDirection dir) {
      this.mzBuffer = mzBuffer;
      this.intensityBuffer = intensityBuffer;
      this.prop = prop;
      this.dir = dir;
    }

    @Override
    public int compare(Integer i1, Integer i2) {
      switch (prop) {
        case INTENSITY:
          if (dir == SortingDirection.ASCENDING)
            return Float.compare(intensityBuffer[i1], intensityBuffer[i2]);
          else
            return Float.compare(intensityBuffer[i2], intensityBuffer[i1]);
        case MZ:
          if (dir == SortingDirection.ASCENDING)
            return Double.compare(mzBuffer[i1], mzBuffer[i2]);
          else
            return Double.compare(mzBuffer[i2], mzBuffer[i1]);
      }
      return 0;
    }

  }

  /**
   * Sort the given data points by m/z order
   *
   * @param mzBuffer an array of double.
   * @param intensityBuffer an array of float.
   * @param size a int.
   * @param prop a {@link SortingProperty} object.
   * @param dir a {@link SortingDirection} object.
   */
  public static void sortDataPoints(final double mzBuffer[], final float intensityBuffer[],
      final int size, SortingProperty prop, SortingDirection dir) {

    // Use Collections.sort to obtain index mapping from old arrays to the
    // new sorted array
    final List<Integer> idx = new ArrayList<>(size);
    for (int i = 0; i < size; i++)
      idx.add(i);
    Comparator<Integer> comp = new DataPointComparator(mzBuffer, intensityBuffer, prop, dir);

    Collections.sort(idx, comp);
    // Remap the values according to the index map idx
    remapArray(mzBuffer, idx);
    remapArray(intensityBuffer, idx);

  }

  private static void remapArray(Object array, List<Integer> indices) {

    // Make a copy of indices, to prevent modifying the list
    List<Integer> idx = new ArrayList<>(indices);

    for (int i = 0; i < idx.size(); i++) {
      final int newIndex = idx.get(i);
      if (newIndex == i)
        continue;

      Object tmp = Array.get(array, i);
      Array.set(array, i, Array.get(array, newIndex));

      final int swapIndex = idx.indexOf(i);
      Array.set(array, newIndex, tmp);
      idx.set(swapIndex, newIndex);
      idx.set(i, i);
    }
  }

  /**
   * Sort the given data points by RT order
   *
   * @param rtBuffer an array
   * @param mzBuffer an array of double.
   * @param intensityBuffer an array of float.
   * @param size a int.
   */
  public static void sortDataPoints(final Float rtBuffer[], final double mzBuffer[],
      final float intensityBuffer[], final int size) {

    // Use Collections.sort to obtain index mapping from old arrays to the
    // new sorted array
    final List<Integer> idx = new ArrayList<>(size);
    for (int i = 0; i < size; i++)
      idx.add(i);
    Collections.sort(idx, new Comparator<Integer>() {
      public int compare(Integer i1, Integer i2) {
        return rtBuffer[i1].compareTo(rtBuffer[i2]);
      }
    });

    // Remap the values according to the index map idx
    remapArray(rtBuffer, idx);
    remapArray(mzBuffer, idx);
    remapArray(intensityBuffer, idx);

  }

}
