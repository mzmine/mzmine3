package io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipidutils;

import io.github.mzmine.datamodel.IonizationType;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipididentificationtools.LipidFragmentationRule;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipididentificationtools.matchedlipidannotations.specieslevellipidmatches.SpeciesLevelAnnotation;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipids.ILipidClass;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipids.LipidClassDescription;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipids.LipidClasses;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.lipidannotationmodules.glyceroandglycerophospholipids.GlyceroAndGlycerophospholipidAnnotationChainParameters;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.lipidannotationmodules.glyceroandglycerophospholipids.GlyceroAndGlycerophospholipidAnnotationParameters;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.parameters.parametertypes.tolerances.MZTolerance;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

public class LipidDatabaseCalculator {

  private static final LipidFactory LIPID_FACTORY = new LipidFactory();

  private final int minChainLength;
  private final int maxChainLength;
  private final int minDoubleBonds;
  private final int maxDoubleBonds;
  private final Boolean onlySearchForEvenChains;
  private final MZTolerance mzTolerance;
  private final LipidClasses[] selectedLipids;

  private final ObservableList<LipidClassDescription> tableData = FXCollections.observableArrayList();

  public LipidDatabaseCalculator(ParameterSet parameters, LipidClasses[] selectedLipids) {
    this.minChainLength = parameters.getParameter(
            GlyceroAndGlycerophospholipidAnnotationParameters.lipidChainParameters)
        .getEmbeddedParameters()
        .getParameter(GlyceroAndGlycerophospholipidAnnotationChainParameters.minChainLength)
        .getValue();
    this.maxChainLength = parameters.getParameter(
            GlyceroAndGlycerophospholipidAnnotationParameters.lipidChainParameters)
        .getEmbeddedParameters()
        .getParameter(GlyceroAndGlycerophospholipidAnnotationChainParameters.maxChainLength)
        .getValue();
    this.minDoubleBonds = parameters.getParameter(
            GlyceroAndGlycerophospholipidAnnotationParameters.lipidChainParameters)
        .getEmbeddedParameters()
        .getParameter(GlyceroAndGlycerophospholipidAnnotationChainParameters.minDBEs).getValue();
    this.maxDoubleBonds = parameters.getParameter(
            GlyceroAndGlycerophospholipidAnnotationParameters.lipidChainParameters)
        .getEmbeddedParameters()
        .getParameter(GlyceroAndGlycerophospholipidAnnotationChainParameters.maxDBEs).getValue();
    this.onlySearchForEvenChains = parameters.getParameter(
            GlyceroAndGlycerophospholipidAnnotationParameters.lipidChainParameters)
        .getEmbeddedParameters().getParameter(
            GlyceroAndGlycerophospholipidAnnotationChainParameters.onlySearchForEvenChainLength)
        .getValue();

    this.mzTolerance = parameters.getParameter(
        GlyceroAndGlycerophospholipidAnnotationParameters.mzTolerance).getValue();
    this.selectedLipids = selectedLipids;
  }

  public void createTableData() {
    int id = 1;
    for (ILipidClass selectedLipid : selectedLipids) {
      // TODO starting point to extend for better oxidized lipid support
      int numberOfAdditionalOxygens = 0;
      int minTotalChainLength = minChainLength * selectedLipid.getChainTypes().length;
      int maxTotalChainLength = maxChainLength * selectedLipid.getChainTypes().length;
      int minTotalDoubleBonds = minDoubleBonds * selectedLipid.getChainTypes().length;
      int maxTotalDoubleBonds = maxDoubleBonds * selectedLipid.getChainTypes().length;
      for (int chainLength = minTotalChainLength; chainLength <= maxTotalChainLength;
          chainLength++) {
        if (onlySearchForEvenChains && chainLength % 2 != 0) {
          continue;
        }
        for (int chainDoubleBonds = minTotalDoubleBonds; chainDoubleBonds <= maxTotalDoubleBonds;
            chainDoubleBonds++) {

          if (chainLength / 2 < chainDoubleBonds || chainLength == 0) {
            continue;
          }
          // Prepare a lipid instance
          SpeciesLevelAnnotation lipid = LIPID_FACTORY.buildSpeciesLevelLipid(selectedLipid,
              chainLength, chainDoubleBonds, numberOfAdditionalOxygens);
          if (lipid == null) {
            continue;
          }
          List<LipidFragmentationRule> fragmentationRules = Arrays.asList(
              selectedLipid.getFragmentationRules());
          StringBuilder fragmentationRuleSB = new StringBuilder();
          fragmentationRules.stream().forEach(rule -> {
            fragmentationRuleSB.append(rule.toString()).append("\n");
          });
          StringBuilder exactMassSB = new StringBuilder();
          Set<IonizationType> ionizationTypes = fragmentationRules.stream()
              .map(LipidFragmentationRule::getIonizationType).collect(Collectors.toSet());
          for (IonizationType ionizationType : ionizationTypes) {
            double mz = MolecularFormulaManipulator.getMass(lipid.getMolecularFormula(),
                AtomContainerManipulator.MonoIsotopic) + ionizationType.getAddedMass();
            exactMassSB.append(ionizationType.getAdductName()).append(" ")
                .append(MZmineCore.getConfiguration().getMZFormat().format(mz)).append("\n");
          }
          tableData.add(new LipidClassDescription(String.valueOf(id), // id
              selectedLipid.getName(), // lipid class
              MolecularFormulaManipulator.getString(lipid.getMolecularFormula()), // molecular
              // formula
              lipid.getAnnotation(),
              // abbr
              exactMassSB.toString(), // exact mass
              // mass
              "", // info
              "", // status
              fragmentationRuleSB.toString())); // msms fragments
          id++;
        }
      }
    }
  }

  public void checkInterferencesOld() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < tableData.size(); i++) {
      Map<String, Double> ionSpecificMzValues = extractIonNotationMzValuesFromTable(
          tableData.get(i));
      for (Entry<String, Double> entry : ionSpecificMzValues.entrySet()) {
        for (int j = 0; j < tableData.size(); j++) {
          sb.setLength(0);
          Map<String, Double> ionSpecificMzValuesCompare = extractIonNotationMzValuesFromTable(
              tableData.get(j));
          for (Entry<String, Double> entryCompare : ionSpecificMzValuesCompare.entrySet()) {
            double valueOne = entry.getValue();
            double valueTwo = entryCompare.getValue();
            if (valueOne == valueTwo && j != i && isSamePolarity(entry.getKey(),
                entryCompare.getKey())) {
              if (!sb.isEmpty()) {
                sb.append("\n");
              }
              sb.append(entryCompare.getKey()).append(" interference with ")
                  .append(tableData.get(i).getAbbreviation()).append(" ").append(entry.getKey());
            } else if (mzTolerance.checkWithinTolerance(valueOne, valueTwo) && j != i
                && isSamePolarity(entry.getKey(), entryCompare.getKey())) {
              double delta = valueOne - valueTwo;
              if (!sb.isEmpty()) {
                sb.append("\n");
              }
              sb.append(entryCompare.getKey()).append(" possible interference with ")
                  .append(tableData.get(i).getAbbreviation()).append(" ").append(entry.getKey())
                  .append(" \u0394 ")
                  .append(MZmineCore.getConfiguration().getMZFormat().format(delta));
            }
          }
          if (!sb.isEmpty()) {
            tableData.get(j).setInfo(tableData.get(j).getInfo() + "\n" + sb);
          }
        }
      }
    }
  }

  public void checkInterferences() {
    for (int i = 0; i < tableData.size(); i++) {
      LipidClassDescription lipidClassDescription = tableData.get(i);
      Map<String, Double> ionSpecificMzValues = extractIonNotationMzValuesFromTable(
          lipidClassDescription);
      for (Entry<String, Double> entry : ionSpecificMzValues.entrySet()) {
        for (int j = 0; j < tableData.size(); j++) {
          if (i == j) {
            continue;
          }
          StringBuilder sb = new StringBuilder();
          LipidClassDescription lipidClassDescriptionCompare = tableData.get(j);
          Map<String, Double> ionSpecificMzValuesCompare = extractIonNotationMzValuesFromTable(
              lipidClassDescriptionCompare);
          for (Entry<String, Double> entryCompare : ionSpecificMzValuesCompare.entrySet()) {
            double valueOne = entry.getValue();
            double valueTwo = entryCompare.getValue();
            if (valueOne == valueTwo && isSamePolarity(entry.getKey(), entryCompare.getKey())) {
              if (!sb.isEmpty()) {
                sb.append("\n");
              }
              sb.append(entryCompare.getKey()).append(" interference with ")
                  .append(lipidClassDescription.getAbbreviation()).append(" ")
                  .append(entry.getKey());
            } else if (mzTolerance.checkWithinTolerance(valueOne, valueTwo) && isSamePolarity(
                entry.getKey(), entryCompare.getKey())) {
              double delta = valueOne - valueTwo;
              if (!sb.isEmpty()) {
                sb.append("\n");
              }
              sb.append(entryCompare.getKey()).append(" possible interference with ")
                  .append(lipidClassDescription.getAbbreviation()).append(" ")
                  .append(entry.getKey()).append(" \u0394 ")
                  .append(MZmineCore.getConfiguration().getMZFormat().format(delta));
            }
          }
          if (!sb.isEmpty()) {
            lipidClassDescriptionCompare.setInfo(
                lipidClassDescriptionCompare.getInfo() + "\n" + sb);
          }
        }
      }
    }
  }

  private boolean isSamePolarity(String key, String key2) {
    return ((key.contains("]+") && key2.contains("]+")) || (key.contains("]-") && key2.contains(
        "]-")));
  }

  private Map<String, Double> extractIonNotationMzValuesFromTable(
      LipidClassDescription lipidClassDescription) {
    Map<String, Double> ionSpecificMzValues = new HashMap<>();
    String allPairs = lipidClassDescription.getExactMass();
    String[] pairs = allPairs.split("\n");
    for (int i = 0; i < pairs.length; i++) {
      String[] pair = pairs[i].split(" ");
      if (pair != null && pair.length > 1) {
        ionSpecificMzValues.put(pair[0], Double.parseDouble(pair[1]));
      }
    }
    return ionSpecificMzValues;
  }

  public ObservableList<LipidClassDescription> getTableData() {
    return tableData;
  }

  public MZTolerance getMzTolerance() {
    return mzTolerance;
  }
}