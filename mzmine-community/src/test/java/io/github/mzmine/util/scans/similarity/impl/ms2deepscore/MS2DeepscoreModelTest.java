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

import static org.junit.jupiter.api.Assertions.assertEquals;

import ai.djl.MalformedModelException;
import ai.djl.ndarray.NDArray;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.translate.TranslateException;
import io.github.mzmine.datamodel.MassSpectrumType;
import io.github.mzmine.datamodel.PolarityType;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.impl.DDAMsMsInfoImpl;
import io.github.mzmine.datamodel.impl.SimpleScan;
import io.github.mzmine.project.impl.RawDataFileImpl;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MS2DeepscoreModelTest {

  private static MS2DeepscoreModel model;
  private static SimpleScan[] testSpectra;

  @BeforeAll
  static void setUp()
      throws URISyntaxException, ModelNotFoundException, MalformedModelException, IOException {
    // load model and setup objects that are shared with all tests
    URI modelFilePath = MS2DeepscoreModelTest.class.getClassLoader()
        .getResource("models/java_embeddings_ms2deepscore_model.pt").toURI();
    URI settingsFilePath = MS2DeepscoreModelTest.class.getClassLoader()
        .getResource("models/ms2deepscore_model_settings.json").toURI();
    model = new MS2DeepscoreModel(modelFilePath, settingsFilePath);

    RawDataFile dummyFile = new RawDataFileImpl("testfile", null, null,
        javafx.scene.paint.Color.BLACK);
    testSpectra = new SimpleScan[]{
        new SimpleScan(dummyFile, -1, 2, 0.1F, new DDAMsMsInfoImpl(200.0, 1, 2),
            new double[]{5, 12, 12.1, 14., 14.3}, new double[]{100, 200, 400, 200, 100},
            MassSpectrumType.ANY, PolarityType.POSITIVE, "Pseudo", null),
        new SimpleScan(dummyFile, -1, 2, 0.1F, new DDAMsMsInfoImpl(200.0, 1, 2),
            new double[]{5, 12, 12.1, 14., 14.3}, new double[]{100, 200, 400, 200, 100},
            MassSpectrumType.ANY, PolarityType.POSITIVE, "Pseudo", null)};
  }


  @AfterAll
  static void tearDown() {
  }

  // Method to generate a list of 1000 random values
  private float[][] generateRandomList(int listLength, int numberOfArrays) {
    float[][] listOfList = new float[numberOfArrays][listLength];
    for (int i = 0; i < numberOfArrays; i++) {
      Random random = new Random();
      for (int j = 0; j < listLength; j++) {
        float randomNumber = random.nextFloat();
        listOfList[i][j] = randomNumber;
      }
    }

    return listOfList;
  }

  private float[][] generateNestedArray(int listLength, float[] listValues) {

    float[][] listOfList = new float[listValues.length][listLength];
    for (int j = 0; j < listValues.length; j++) {
      for (int i = 0; i < listLength; i++) {
        listOfList[j][i] = listValues[j];
      }
    }
    return listOfList;
  }

  @Test
  void testCorrectPrediction() throws TranslateException {
//      Create test input data
    float[][] spectrumArray = generateNestedArray(990, new float[]{0.1F, 0.2F});
    float[][] metadataArray = generateNestedArray(2, new float[]{0.0F, 1.0F});

    NDArray predictions = model.predictEmbeddingFromTensors(
        new TensorizedSpectra(spectrumArray, metadataArray));
    Assertions.assertArrayEquals(new long[]{2, 50}, predictions.getShape().getShape());
//      Test that the first number in the embedding is correct for the first test spectrum
    assertEquals(predictions.get(0).getFloat(0), -0.046006925, 0.0001);
//      Test that the first number in the embedding is correct for the second spectrum
    assertEquals(predictions.get(1).getFloat(0), -0.03738583, 0.0001);

  }

  @Test
  void testCreateEmbeddingFromScan() {
    NDArray embeddings = null;
    try {
      embeddings = model.predictEmbeddingFromSpectra(testSpectra);
    } catch (TranslateException e) {
      throw new RuntimeException(e);
    }
    System.out.println(embeddings);
  }
}

