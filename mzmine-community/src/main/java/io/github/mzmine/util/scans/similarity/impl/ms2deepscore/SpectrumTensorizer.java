/*
 * Copyright (c) 2004-2024 The mzmine Development Team
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

package io.github.mzmine.util.scans.similarity.impl.ms2deepscore;


import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import io.github.mzmine.datamodel.PolarityType;
import io.github.mzmine.datamodel.Scan;
import org.jetbrains.annotations.NotNull;

/**
 * Bins spectra and returns two tensors, one for metadata and one for fragments
 */
public class SpectrumTensorizer {

  private final SettingsMS2Deepscore settings;
  private final int numBins;

  public SpectrumTensorizer(SettingsMS2Deepscore settings) {
    this.settings = settings;
    this.numBins = (int) ((settings.maximumMZ() - settings.minimumMZ() / settings.binWidth()));
  }

  public double[] tensorizeFragments(Scan spectrum) {
    double[] vector = new double[numBins];
    int numberOfDataPoints = spectrum.getNumberOfDataPoints();
    for (int i = 0; i < numberOfDataPoints; i++) {
      double mz = spectrum.getMzValue(i);
      double intensity = spectrum.getIntensityValue(i);
      if (settings.minimumMZ() <= mz && mz < settings.maximumMZ()) {
        int binIndex = (int) ((mz - settings.minimumMZ() / settings.binWidth()));
        vector[binIndex] = Math.max(vector[binIndex], intensity);
      }

    }
    return vector;
  }

  private double binarizePolarity(Scan scan) {
    @NotNull PolarityType polarity = scan.getPolarity();
    if (!(polarity == PolarityType.POSITIVE || polarity == PolarityType.NEGATIVE)) {
      throw new RuntimeException("The polarity has to be positive or negative");
    }
    if (polarity == PolarityType.POSITIVE) {
      return 1;
    } else {
      return 0;
    }
  }

  private double scalePrecursorMZ(Scan scan, double mean, double standardDeviation) {
//    @robin what happens if this is null, does that throw an exception?
    @NotNull double precursorMZ = scan.getPrecursorMz();
    return (precursorMZ - mean) / standardDeviation;
  }


  public double[] tensorizeMetadata(Scan scan) {
    double[] vector = new double[]{scalePrecursorMZ(scan, 0, 1000), binarizePolarity(scan)};
    return vector;
  }

  public TensorizedSpectra tensorizeSpectra(Scan[] scans) {

    double[][] metadataVectors = new double[scans.length][numBins];
    double[][] fragmentVectors = new double[scans.length][numBins];

    for (int i = 0; i < scans.length; i++) {
      metadataVectors[i] = tensorizeMetadata(scans[i]);
      fragmentVectors[i] = tensorizeFragments(scans[i]);
    }
    try (NDManager manager = NDManager.newBaseManager()) {
//      Create test input data
      // Convert input nested float array to NDArray
      NDArray metadataNDArray = manager.create(metadataVectors);
      NDArray fragmentsNDArray = manager.create(fragmentVectors);

      return new TensorizedSpectra(fragmentsNDArray, metadataNDArray);
    }
  }
}