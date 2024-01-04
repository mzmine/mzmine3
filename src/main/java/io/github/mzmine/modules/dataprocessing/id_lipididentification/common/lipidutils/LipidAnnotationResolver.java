package io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipidutils;

import com.google.common.collect.ComparisonChain;
import io.github.mzmine.datamodel.features.FeatureListRow;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipididentificationtools.matchedlipidannotations.MatchedLipid;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipididentificationtools.matchedlipidannotations.MatchedLipidStatus;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipididentificationtools.matchedlipidannotations.molecularspecieslevelidentities.MolecularSpeciesLevelAnnotation;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipididentificationtools.matchedlipidannotations.specieslevellipidmatches.SpeciesLevelAnnotation;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipids.ILipidAnnotation;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipids.LipidAnnotationLevel;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipids.lipidchain.ILipidChain;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class LipidAnnotationResolver {

  private final boolean keepIsobars;
  private final boolean keepIsomers;
  private final boolean addMissingSpeciesLevelAnnotation;

  private int maximumIdNumber;
  private static final LipidFactory LIPID_FACTORY = new LipidFactory();

  public LipidAnnotationResolver(boolean keepIsobars, boolean keepIsomers,
      boolean addMissingSpeciesLevelAnnotation) {
    this.keepIsobars = keepIsobars;
    this.keepIsomers = keepIsomers;
    this.addMissingSpeciesLevelAnnotation = addMissingSpeciesLevelAnnotation;
    this.maximumIdNumber = -1;

  }

  public LipidAnnotationResolver(boolean keepIsobars, boolean keepIsomers,
      boolean addMissingSpeciesLevelAnnotation, int maximumIdNumber) {
    this(keepIsobars, keepIsomers, addMissingSpeciesLevelAnnotation);
    this.maximumIdNumber = maximumIdNumber;
  }

  public List<MatchedLipid> resolveFeatureListRowMatchedLipids(FeatureListRow featureListRow,
      Set<MatchedLipid> matchedLipids) {
    List<MatchedLipid> resolvedMatchedLipidsList = removeDuplicates(matchedLipids);
    sortByMsMsScore(resolvedMatchedLipidsList);
    if (addMissingSpeciesLevelAnnotation) {
      estimateMissingSpeciesLevelAnnotations(resolvedMatchedLipidsList);
    }
    //TODO: Add Keep isobars functionality

    //TODO: Add keep isomers functionality

    //add to resolved list
    if (maximumIdNumber != -1 && resolvedMatchedLipidsList.size() > maximumIdNumber) {
      filterMaximumNumberOfId(resolvedMatchedLipidsList);
    }
    return resolvedMatchedLipidsList;
  }

  private List<MatchedLipid> removeDuplicates(Set<MatchedLipid> resolvedMatchedLipids) {
    return resolvedMatchedLipids.stream().collect(Collectors.collectingAndThen(
        Collectors.toCollection(() -> new TreeSet<>(comparatorMatchedLipids())), ArrayList::new));
  }

  private void estimateMissingSpeciesLevelAnnotations(
      List<MatchedLipid> resolvedMatchedLipidsList) {
    if (resolvedMatchedLipidsList.stream().noneMatch(
        matchedLipid -> matchedLipid.getLipidAnnotation().getLipidAnnotationLevel()
            .equals(LipidAnnotationLevel.SPECIES_LEVEL))) {
      Set<MatchedLipid> estimatedSpeciesLevelMatchedLipids = new HashSet<>();
      for (MatchedLipid lipid : resolvedMatchedLipidsList) {
        ILipidAnnotation estimatedSpeciesLevelAnnotation = convertMolecularSpeciesLevelToSpeciesLevel(
            (MolecularSpeciesLevelAnnotation) lipid.getLipidAnnotation());
        if (resolvedMatchedLipidsList.stream().noneMatch(
            matchedLipid -> matchedLipid.getLipidAnnotation().getAnnotation()
                .equals(estimatedSpeciesLevelAnnotation.getAnnotation()))) {
          if ((estimatedSpeciesLevelAnnotation != null
              && estimatedSpeciesLevelMatchedLipids.isEmpty()) || (
              estimatedSpeciesLevelAnnotation != null && estimatedSpeciesLevelMatchedLipids.stream()
                  .anyMatch(matchedLipid -> !Objects.equals(
                      matchedLipid.getLipidAnnotation().getAnnotation(),
                      estimatedSpeciesLevelAnnotation.getAnnotation())))) {
            MatchedLipid matchedLipidSpeciesLevel = new MatchedLipid(
                estimatedSpeciesLevelAnnotation, lipid.getAccurateMz(), lipid.getIonizationType(),
                new HashSet<>(lipid.getMatchedFragments()), 0.0, MatchedLipidStatus.ESTIMATED);
            matchedLipidSpeciesLevel.setComment(
                "Estimated annotation based on molecular species level fragments");
            estimatedSpeciesLevelMatchedLipids.add(matchedLipidSpeciesLevel);
          }
        }
      }
      if (!estimatedSpeciesLevelMatchedLipids.isEmpty()) {
        resolvedMatchedLipidsList.addAll(estimatedSpeciesLevelMatchedLipids);
      }
    }
  }

  private SpeciesLevelAnnotation convertMolecularSpeciesLevelToSpeciesLevel(
      MolecularSpeciesLevelAnnotation lipidAnnotation) {
    int numberOfCarbons = lipidAnnotation.getLipidChains().stream()
        .mapToInt(ILipidChain::getNumberOfCarbons).sum();
    int numberOfDBEs = lipidAnnotation.getLipidChains().stream()
        .mapToInt(ILipidChain::getNumberOfDBEs).sum();
    return LIPID_FACTORY.buildSpeciesLevelLipid(lipidAnnotation.getLipidClass(), numberOfCarbons,
        numberOfDBEs, 0);
  }

  private void filterMaximumNumberOfId(List<MatchedLipid> resolvedMatchedLipids) {
    Iterator<MatchedLipid> iterator = resolvedMatchedLipids.iterator();
    while (iterator.hasNext()) {
      MatchedLipid lipid = iterator.next();
      if (resolvedMatchedLipids.indexOf(lipid) > maximumIdNumber) {
        iterator.remove();
      }
    }
  }

  private static Comparator<MatchedLipid> comparatorMatchedLipids() {
    return (lipid1, lipid2) -> ComparisonChain.start()
        .compare(lipid1.getLipidAnnotation().getAnnotation(),
            lipid2.getLipidAnnotation().getAnnotation())
        .compare(lipid1.getMsMsScore(), lipid2.getMsMsScore())
        .compare(lipid1.getAccurateMz(), lipid2.getAccurateMz()).result();
  }

  private void sortByMsMsScore(List<MatchedLipid> matchedLipids) {
    matchedLipids.sort(Comparator.comparingDouble(MatchedLipid::getMsMsScore).reversed());
  }

}