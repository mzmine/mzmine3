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

package io.github.mzmine.modules.dataprocessing.id_lipididentification.lipidannotationmodules.glyceroandglycerophospholipids;

import io.github.mzmine.datamodel.IonizationType;
import io.github.mzmine.datamodel.features.FeatureList;
import io.github.mzmine.datamodel.features.FeatureListRow;
import io.github.mzmine.datamodel.features.ModularFeatureList;
import io.github.mzmine.datamodel.features.SimpleFeatureListAppliedMethod;
import io.github.mzmine.datamodel.features.types.annotations.LipidMatchListType;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipididentificationtools.MSMSLipidTools;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipids.ILipidAnnotation;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipids.LipidCategories;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipids.LipidClasses;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipids.customlipidclass.CustomLipidClass;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.lipidannotationmodules.LipidAnnotationChainParameters;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.lipidannotationmodules.LipidAnnotationUtils;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.parameters.parametertypes.tolerances.MZTolerance;
import io.github.mzmine.taskcontrol.AbstractTask;
import io.github.mzmine.taskcontrol.TaskStatus;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Task to search and annotate lipids in feature list
 *
 * @author Ansgar Korf (ansgar.korf@uni-muenster.de)
 */
public class GlyceroAndGlycerophospholipidAnnotationTask extends AbstractTask {

  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private double finishedSteps;
  private double totalSteps;
  private final FeatureList featureList;
  private final LipidClasses[] selectedLipids;
  private CustomLipidClass[] customLipidClasses;
  private final int minChainLength;
  private final int maxChainLength;
  private final int maxDoubleBonds;
  private final int minDoubleBonds;
  private final Boolean onlySearchForEvenChains;
  private final MZTolerance mzTolerance;
  private MZTolerance mzToleranceMS2;
  private final Boolean searchForMSMSFragments;
  private final Boolean keepUnconfirmedAnnotations;
  private double minMsMsScore;
  private final IonizationType[] ionizationTypesToIgnore;


  private final ParameterSet parameters;

  private static final MSMSLipidTools MSMS_LIPID_TOOLS = new MSMSLipidTools();

  public GlyceroAndGlycerophospholipidAnnotationTask(ParameterSet parameters,
      FeatureList featureList, @NotNull Instant moduleCallDate) {
    super(null, moduleCallDate);
    this.featureList = featureList;
    this.parameters = parameters;

    this.minChainLength = parameters.getParameter(
            GlyceroAndGlycerophospholipidAnnotationParameters.lipidChainParameters)
        .getEmbeddedParameters().getParameter(LipidAnnotationChainParameters.minChainLength)
        .getValue();
    this.maxChainLength = parameters.getParameter(
            GlyceroAndGlycerophospholipidAnnotationParameters.lipidChainParameters)
        .getEmbeddedParameters().getParameter(LipidAnnotationChainParameters.maxChainLength)
        .getValue();
    this.minDoubleBonds = parameters.getParameter(
            GlyceroAndGlycerophospholipidAnnotationParameters.lipidChainParameters)
        .getEmbeddedParameters().getParameter(LipidAnnotationChainParameters.minDBEs).getValue();
    this.maxDoubleBonds = parameters.getParameter(
            GlyceroAndGlycerophospholipidAnnotationParameters.lipidChainParameters)
        .getEmbeddedParameters().getParameter(LipidAnnotationChainParameters.maxDBEs).getValue();
    this.onlySearchForEvenChains = parameters.getParameter(
            GlyceroAndGlycerophospholipidAnnotationParameters.lipidChainParameters)
        .getEmbeddedParameters()
        .getParameter(LipidAnnotationChainParameters.onlySearchForEvenChainLength).getValue();
    this.mzToleranceMS2 = parameters.getParameter(
            GlyceroAndGlycerophospholipidAnnotationParameters.searchForMSMSFragments)
        .getEmbeddedParameters()
        .getParameter(GlyceroAndGlycerophospholipidAnnotationMSMSParameters.mzToleranceMS2)
        .getValue();
    this.mzTolerance = parameters.getParameter(
        GlyceroAndGlycerophospholipidAnnotationParameters.mzTolerance).getValue();
    Object[] selectedObjects = parameters.getParameter(
        GlyceroAndGlycerophospholipidAnnotationParameters.lipidClasses).getValue();
    this.searchForMSMSFragments = parameters.getParameter(
        GlyceroAndGlycerophospholipidAnnotationParameters.searchForMSMSFragments).getValue();
    if (searchForMSMSFragments.booleanValue()) {
      this.mzToleranceMS2 = parameters.getParameter(
              GlyceroAndGlycerophospholipidAnnotationParameters.searchForMSMSFragments)
          .getEmbeddedParameters()
          .getParameter(GlyceroAndGlycerophospholipidAnnotationMSMSParameters.mzToleranceMS2)
          .getValue();
      this.keepUnconfirmedAnnotations = parameters.getParameter(
              GlyceroAndGlycerophospholipidAnnotationParameters.searchForMSMSFragments)
          .getEmbeddedParameters().getParameter(
              GlyceroAndGlycerophospholipidAnnotationMSMSParameters.keepUnconfirmedAnnotations)
          .getValue();
      this.minMsMsScore = parameters.getParameter(
              GlyceroAndGlycerophospholipidAnnotationParameters.searchForMSMSFragments)
          .getEmbeddedParameters()
          .getParameter(GlyceroAndGlycerophospholipidAnnotationMSMSParameters.minimumMsMsScore)
          .getValue();
    } else {
      this.keepUnconfirmedAnnotations = true;
    }
    Boolean searchForCustomLipidClasses = parameters.getParameter(
        GlyceroAndGlycerophospholipidAnnotationParameters.customLipidClasses).getValue();
    if (searchForCustomLipidClasses.booleanValue()) {
      this.customLipidClasses = GlyceroAndGlycerophospholipidAnnotationParameters.customLipidClasses.getEmbeddedParameter()
          .getChoices();
    }
    // Convert Objects to LipidClasses
    this.selectedLipids = Arrays.stream(selectedObjects).filter(o -> o instanceof LipidClasses)
        .map(o -> (LipidClasses) o).toArray(LipidClasses[]::new);

    if (parameters.getParameter(GlyceroAndGlycerophospholipidAnnotationParameters.advanced)
        .getValue()) {
      this.ionizationTypesToIgnore = parameters.getParameter(
              GlyceroAndGlycerophospholipidAnnotationParameters.advanced.getEmbeddedParameters()
                  .getParameter(
                      AdvancedGlyceroAndGlycerophospholipidAnnotationParameters.IONS_TO_IGNORE))
          .getValue();
    } else {
      ionizationTypesToIgnore = null;
    }
  }

  /**
   * @see io.github.mzmine.taskcontrol.Task#getFinishedPercentage()
   */
  @Override
  public double getFinishedPercentage() {
    if (totalSteps == 0) {
      return 0;
    }
    return (finishedSteps) / totalSteps;
  }

  /**
   * @see io.github.mzmine.taskcontrol.Task#getTaskDescription()
   */
  @Override
  public String getTaskDescription() {
    return "Find Glycero- and Glycerophospholipids in " + featureList;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    setStatus(TaskStatus.PROCESSING);

    logger.info("Starting Glycero- and Glycerophospholipid annotation in " + featureList);

    List<FeatureListRow> rows = featureList.getRows();
    if (featureList instanceof ModularFeatureList) {
      featureList.addRowType(new LipidMatchListType());
    }
    totalSteps = rows.size();

    // build lipid species database
    Set<ILipidAnnotation> lipidDatabase = LipidAnnotationUtils.buildLipidDatabase(selectedLipids,
        minChainLength, maxChainLength, minDoubleBonds, maxDoubleBonds, onlySearchForEvenChains);

    // start lipid annotation
    rows.parallelStream().forEach(row -> {
      for (ILipidAnnotation lipidAnnotation : lipidDatabase) {
        if (isCanceled()) {
          return;
        }
        LipidAnnotationUtils.findPossibleLipid(lipidAnnotation, row, parameters,
            ionizationTypesToIgnore, mzTolerance, mzToleranceMS2, searchForMSMSFragments,
            minMsMsScore, keepUnconfirmedAnnotations, LipidCategories.GLYCEROPHOSPHOLIPIDS);
      }
      finishedSteps++;
    });

    // Add task description to featureList
    (featureList).addDescriptionOfAppliedTask(
        new SimpleFeatureListAppliedMethod("Glycero- and Glycerophospholipid annotation",
            GlyceroAndGlycerophospholipidAnnotationModule.class, parameters, getModuleCallDate()));

    setStatus(TaskStatus.FINISHED);

    logger.info("Finished Glycero- and Glycerophospholipids task for " + featureList);
  }

}