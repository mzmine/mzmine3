/*
 * Copyright (c) 2004-2022 The MZmine Development Team
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package util;

import com.google.common.collect.Range;
import io.github.mzmine.modules.io.import_rawdata_bruker_tdf.datamodel.sql.MaldiSpotInfo;
import io.github.mzmine.modules.tools.timstofmaldiacq.TimsTOFAcquisitionUtils;
import io.github.mzmine.modules.tools.timstofmaldiacq.imaging.ImagingSpot;
import io.github.mzmine.modules.tools.timstofmaldiacq.imaging.Ms2ImagingMode;
import io.github.mzmine.modules.tools.timstofmaldiacq.precursorselection.MaldiTimsPrecursor;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TimsTofAcquisitionUtilsTest {

  private static final Logger logger = Logger.getLogger(
      TimsTofAcquisitionUtilsTest.class.getName());

  final ImagingSpot spot1_1 = new ImagingSpot(new MaldiSpotInfo(0, 0, "", 1, 1, 1, 0, 0, 0),
      Ms2ImagingMode.SINGLE, 50d);
  final ImagingSpot spot25_25 = new ImagingSpot(new MaldiSpotInfo(0, 0, "", 1, 25, 25, 0, 0, 0),
      Ms2ImagingMode.SINGLE, 30d);
  final ImagingSpot spot50_50 = new ImagingSpot(new MaldiSpotInfo(0, 0, "", 1, 50, 50, 0, 0, 0),
      Ms2ImagingMode.SINGLE, 30d);
  final ImagingSpot spot50_0 = new ImagingSpot(new MaldiSpotInfo(0, 0, "", 1, 50, 0, 0, 0, 0),
      Ms2ImagingMode.SINGLE, 40d);
  final ImagingSpot spot0_50 = new ImagingSpot(new MaldiSpotInfo(0, 0, "", 1, 0, 50, 0, 0, 0),
      Ms2ImagingMode.SINGLE, 30d);
  final ImagingSpot spot23_34 = new ImagingSpot(new MaldiSpotInfo(0, 0, "", 1, 23, 34, 0, 0, 0),
      Ms2ImagingMode.SINGLE, 40d);
  final List<ImagingSpot> allSpots = List.of(spot1_1, spot25_25, spot50_50, spot50_0, spot0_50,
      spot23_34);

  @Test
  void testOffsets() {
    Assertions.assertTrue(Arrays.equals(new int[]{20, 0},
        TimsTOFAcquisitionUtils.getOffsetsForIncrementCounter(1, 4, 20, 20)));

    Assertions.assertTrue(Arrays.equals(new int[]{3 * 20, 1 * 20},
        TimsTOFAcquisitionUtils.getOffsetsForIncrementCounter(7, 4, 20, 20)));

    Assertions.assertTrue(Arrays.equals(new int[]{2 * 20, 2 * 20},
        TimsTOFAcquisitionUtils.getOffsetsForIncrementCounter(10, 4, 20, 20)));
  }

  @Test
  void testDistance() {
    Assertions.assertEquals(69.29646455628166,
        TimsTOFAcquisitionUtils.getDistanceForSpots(spot1_1.spotInfo(), spot50_50.spotInfo()));
    Assertions.assertEquals(33.94112549695428,
        TimsTOFAcquisitionUtils.getDistanceForSpots(spot1_1.spotInfo(), spot25_25.spotInfo()));

    Assertions.assertFalse(
        TimsTOFAcquisitionUtils.areSpotsWithinDistance(spot1_1.spotInfo(), spot50_50.spotInfo(),
            60));
    Assertions.assertTrue(
        TimsTOFAcquisitionUtils.areSpotsWithinDistance(spot1_1.spotInfo(), spot25_25.spotInfo(),
            60));

    Assertions.assertFalse(TimsTOFAcquisitionUtils.areSpotsWithinDistance(spot1_1.spotInfo(),
        List.of(spot25_25, spot50_50, spot50_0, spot0_50), 30));
    Assertions.assertTrue(TimsTOFAcquisitionUtils.areSpotsWithinDistance(spot1_1.spotInfo(),
        List.of(spot25_25, spot50_50, spot50_0, spot0_50), 35));
    Assertions.assertTrue(TimsTOFAcquisitionUtils.areSpotsWithinDistance(spot23_34.spotInfo(),
        List.of(spot1_1, spot25_25, spot50_50, spot50_0, spot0_50), 30));
  }

  @Test
  public void testGetSpotsWithinDistance() {
    final MaldiSpotInfo testSpot = new MaldiSpotInfo(0, 0, "", 0, 15, 40, 0, 0, 0);
    final List<ImagingSpot> spotsWithinDistance = TimsTOFAcquisitionUtils.getSpotsWithinDistance(30,
        allSpots, testSpot);
    Assertions.assertEquals(List.of(spot25_25, spot0_50, spot23_34), spotsWithinDistance);
  }

  @Test
  void testGetBestCollisionEnergyForSpot() {
    final List<Double> energies = List.of(30d, 40d, 50d);
    final MaldiTimsPrecursor precursor = new MaldiTimsPrecursor(null, 500d,
        Range.closed(0.85f, 0.88f), energies);

    spot50_0.addPrecursor(precursor);
    spot0_50.addPrecursor(precursor);
    precursor.incrementSpotCounterForCollisionEnergy(30);
    precursor.incrementSpotCounterForCollisionEnergy(50);
    precursor.incrementSpotCounterForCollisionEnergy(50);
    // CE usage: 30 -> 2, 40 -> 1, 50 -> 2

    final MaldiSpotInfo testSpot = new MaldiSpotInfo(0, 0, "", 0, 15, 40, 0, 0, 0);

    final List<ImagingSpot> spotsWithinDistance = TimsTOFAcquisitionUtils.getSpotsWithinDistance(30,
        allSpots, testSpot);
    final List<ImagingSpot> spotsOutsideDistance = allSpots.stream()
        .filter(s -> !spotsWithinDistance.contains(s)).toList();

    // test with all spots, only 50 is not used
    Assertions.assertEquals(50d,
        TimsTOFAcquisitionUtils.getBestCollisionEnergyForSpot(30, precursor, allSpots, testSpot,
            energies, 3));

    // spot outside must use lowest CE (= 40, only one spot)
    Assertions.assertEquals(40d,
        TimsTOFAcquisitionUtils.getBestCollisionEnergyForSpot(30, precursor, spotsOutsideDistance,
            testSpot, energies, 3));

    // Test with only spots in range, so get the CE that is not used in range
    Assertions.assertEquals(50d,
        TimsTOFAcquisitionUtils.getBestCollisionEnergyForSpot(30, precursor, spotsWithinDistance,
            testSpot, energies, 3));

    // all CEs used, must return null
    Assertions.assertEquals(null,
        TimsTOFAcquisitionUtils.getBestCollisionEnergyForSpot(70, precursor,
            List.of(spot0_50, spot50_0,
                new ImagingSpot(new MaldiSpotInfo(0, 0, "", 0, 10, 10, 0, 0, 0),
                    Ms2ImagingMode.SINGLE, 50)), testSpot, energies, 3));

    // no spot given, must return CE with fewest MS/MS
    Assertions.assertEquals(40d,
        TimsTOFAcquisitionUtils.getBestCollisionEnergyForSpot(30, precursor, List.of(), testSpot,
            energies, 3));
  }

  @Test
  void testGetPossibleCollisionEnergiesForSpot() {
    final List<Double> energies = List.of(30d, 40d, 50d);
    var usedSpots = List.of(spot50_0, spot0_50);

    final MaldiTimsPrecursor precursor = new MaldiTimsPrecursor(null, 500d,
        Range.closed(0.85f, 0.88f), energies);

    spot50_0.addPrecursor(precursor);
    spot0_50.addPrecursor(precursor);
    precursor.incrementSpotCounterForCollisionEnergy(30);
    precursor.incrementSpotCounterForCollisionEnergy(50);
    precursor.incrementSpotCounterForCollisionEnergy(50);

    final MaldiSpotInfo testSpot = new MaldiSpotInfo(0, 0, "", 0, 15, 40, 0, 0, 0);

    Assertions.assertEquals(List.of(40.0d, 50.0d),
        TimsTOFAcquisitionUtils.getPossibleCollisionEnergiesForSpot(30, precursor,
            List.of(spot25_25), testSpot, energies, 3));

    Assertions.assertEquals(List.of(50.0d, 30.0d),
        TimsTOFAcquisitionUtils.getPossibleCollisionEnergiesForSpot(30, precursor,
            List.of(spot23_34), testSpot, energies, 3));

    Assertions.assertEquals(List.of(50.0d),
        TimsTOFAcquisitionUtils.getPossibleCollisionEnergiesForSpot(30, precursor,
            List.of(spot23_34, spot25_25), testSpot, energies, 3));

    Assertions.assertEquals(List.of(),
        TimsTOFAcquisitionUtils.getPossibleCollisionEnergiesForSpot(60, precursor, allSpots,
            testSpot, energies, 3));
  }

  @Test
  void testIllegalCollisionEnergy() {
    MaldiTimsPrecursor precursor = new MaldiTimsPrecursor(null, 500d, Range.closed(3f, 4f),
        List.of(30d, 40d, 50d));

    Assertions.assertThrows(IllegalArgumentException.class,
        () -> precursor.incrementSpotCounterForCollisionEnergy(25));
  }
}