package io.github.mzmine.modules.tools.timstofmaldiacq.imaging;

import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.tools.timstofmaldiacq.imaging.acquisitionwriters.MaldiMs2AcqusitionWriter;
import io.github.mzmine.modules.tools.timstofmaldiacq.imaging.acquisitionwriters.SingleSpotMs2Writer;
import io.github.mzmine.modules.tools.timstofmaldiacq.imaging.acquisitionwriters.TripleSpotMs2Writer;
import io.github.mzmine.parameters.Parameter;
import io.github.mzmine.parameters.impl.IonMobilitySupport;
import io.github.mzmine.parameters.impl.SimpleParameterSet;
import io.github.mzmine.parameters.parametertypes.BooleanParameter;
import io.github.mzmine.parameters.parametertypes.DoubleParameter;
import io.github.mzmine.parameters.parametertypes.IntegerParameter;
import io.github.mzmine.parameters.parametertypes.ModuleComboParameter;
import io.github.mzmine.parameters.parametertypes.filenames.DirectoryParameter;
import io.github.mzmine.parameters.parametertypes.filenames.FileNameParameter;
import io.github.mzmine.parameters.parametertypes.filenames.FileSelectionType;
import io.github.mzmine.parameters.parametertypes.selectors.FeatureListsParameter;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import javafx.stage.FileChooser.ExtensionFilter;
import org.jetbrains.annotations.NotNull;

public class TimsTOFImageMsMsParameters extends SimpleParameterSet {

  public static final SingleSpotMs2Writer single = MZmineCore.getModuleInstance(
      SingleSpotMs2Writer.class);

  public static final TripleSpotMs2Writer triple = MZmineCore.getModuleInstance(
      TripleSpotMs2Writer.class);

  public static final MaldiMs2AcqusitionWriter[] acquisitionModules = new MaldiMs2AcqusitionWriter[]{
      single, triple};

  public static final FeatureListsParameter flists = new FeatureListsParameter();

  public static final DoubleParameter minMobilityWidth = new DoubleParameter(
      "Minimum mobility window", "Minimum width of the mobility isolation window.",
      new DecimalFormat("0.000"), 0.005);

  public static final DoubleParameter maxMobilityWidth = new DoubleParameter(
      "Maximum mobility window", "Maximum width of the mobility isolation window.",
      new DecimalFormat("0.000"), 0.015);

  public static final DirectoryParameter savePathDir = new DirectoryParameter("Data location",
      "Path to where acquired measurements shall be saved.",
      "D:" + File.separator + "Data" + File.separator + "User" + File.separator + "MZmine_3");

  public static final FileNameParameter acquisitionControl = new FileNameParameter(
      "Path to msmsmaldi.exe", "", List.of(new ExtensionFilter("executable", "*.exe")),
      FileSelectionType.OPEN);

  public static final DoubleParameter isolationWidth = new DoubleParameter("Isolation width",
      "The isolation width for precursors", new DecimalFormat("0.0"), 1.5d);

  public static final IntegerParameter numMsMs = new IntegerParameter("Number of MS/MS spectra",
      "The number of MS/MS spectra to be acquired per feature.", 1, 1, Integer.MAX_VALUE);

  public static final IntegerParameter minimumDistance = new IntegerParameter(
      "Minimum distance of MS/MS spectra",
      "The number of MS/MS spectra to be acquired per feature.", 30);

  public static final DoubleParameter maximumChimerity = new DoubleParameter(
      "Minimum chimerity score", """
      Evaluates the chimerity of the isolation window and only selects pixels with low chimerity for fragmentation.
      The precursor intensity is divided by the summed intensity in the isolation window.
      This means that low values (e.g. 0.1) indicate high chimerity and high values (e.g. 0.9) indicate
      low chimerity.Thereby MS/MS spectra with higher quality are produced.""",
      new DecimalFormat("0.00"), 0.80, 0d, 1d);

  public static final DoubleParameter minimumIntensity = new DoubleParameter(
      "Minimum MS1 intensity", """
      Minimum intensity of a MS1 pixel to be eligible for an MS/MS spectrum.
      Note that every feature in this feature list will be fragmented.
      This parameter applies to individual pixels in the MS1 image.
      If that intensity is greater than the threshold, an MS2 may be acquired adjacent to that pixel.""",
      MZmineCore.getConfiguration().getIntensityFormat(), 1E4);

  public static final BooleanParameter exportOnly = new BooleanParameter("Export MS/MS lists only",
      "Will only export MS/MS lists and not start an acquisition.", false);

  public static final ModuleComboParameter<MaldiMs2AcqusitionWriter> ms2ImagingMode = new ModuleComboParameter<>(
      "MS2 acquisition mode", "", acquisitionModules, single);

  public TimsTOFImageMsMsParameters() {
    super(
        new Parameter[]{flists, minMobilityWidth, maxMobilityWidth, savePathDir, acquisitionControl,
            isolationWidth, numMsMs, minimumDistance, minimumIntensity, maximumChimerity,
            ms2ImagingMode, exportOnly});
  }

  @Override
  public @NotNull IonMobilitySupport getIonMobilitySupport() {
    return IonMobilitySupport.ONLY;
  }
}