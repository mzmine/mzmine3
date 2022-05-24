package io.github.mzmine.modules.visualization.massvoltammogram;

import com.google.common.collect.Range;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.dataprocessing.id_ecmscalcpotential.EcmsUtils;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.taskcontrol.AbstractTask;
import io.github.mzmine.taskcontrol.TaskStatus;
import io.github.mzmine.util.ExitCode;
import io.github.mzmine.util.scans.ScanUtils;
import java.time.Instant;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jmol.script.T;

public class MassvoltammogramTask extends AbstractTask {

  /**
   * tubing length in mm
   */
  private final double tubingLength = MassvoltammogramParameters.tubingLengthMM.getValue();
  /**
   * tubing id in mm
   */
  private final double tubingId = MassvoltammogramParameters.tubingIdMM.getValue();
  /**
   * flow rate in uL/min
   */
  private final double flowRate = MassvoltammogramParameters.flowRateMicroLiterPerMin.getValue();
  /**
   * potential ramp speed in mV/s
   */
  private final double potentialRampSpeed = MassvoltammogramParameters.potentialRampSpeed.getValue();
  /**
   * step size between drawn spectra in mV
   */
  private final double stepSize = MassvoltammogramParameters.stepSize.getValue();
  /**
   * potential range of mass voltammogram in mV
   */
  private final Range<Double> potentialRange = MassvoltammogramParameters.potentialRange.getValue();
  /**
   * m/z range of drawn spectra
   */
  private final Range<Double> mzRange = MassvoltammogramParameters.mzRange.getValue();


  public MassvoltammogramTask(@NotNull ParameterSet parameters, @NotNull Instant moduleCallDate) {
    super(null, moduleCallDate);
  }

  @Override
  public String getTaskDescription() {
    return "Creating Massvoltammogram";
  }

  @Override
  public double getFinishedPercentage() {
    return 0;
  }

  @Override
  public void run() {
    setStatus(TaskStatus.PROCESSING);

    //Getting raw data file.
    final RawDataFile file = MassvoltammogramParameters.files.getValue()
        .getMatchingRawDataFiles()[0];

    //Calculating delay time between EC-cell and MS.
    final double tubingVolumeMicroL = EcmsUtils.getTubingVolume(tubingLength, tubingId);
    final double delayTimeMin = EcmsUtils.getDelayTime(flowRate, tubingVolumeMicroL);

    //Creating a list with all needed scans.
    final List<double[][]> scans = MassvoltamogramUtils.getScans(file, delayTimeMin, potentialRange,
        potentialRampSpeed, stepSize);

    //Checking weather the scans were extracted correctly.
    if(scans.size() == 0){
      setStatus(TaskStatus.ERROR);
      setErrorMessage("The entered parameters do not match the selected data file!\nThe massvolatamogarm cannot be created.\nCheck the entered parameters for plausibility.");
      return;
    }

    //Extracting all spectra within the given m/z-range.
    final List<double[][]> spectra = MassvoltamogramUtils.extractMZRangeFromScan(scans,
        mzRange);

    //Getting the maximal intensity from all spectra.
    final double maxIntensity = ScanUtils.getMaxIntensity(spectra);

    //Removing all datapoints with low intensity values.
    final List<double[][]> spectraWithoutNoise = MassvoltamogramUtils.removeNoise(spectra, maxIntensity);

    //Removing excess zeros from the dataset.
    final List<double[][]> spectraWithoutZeros = MassvoltamogramUtils.removeExcessZeros(spectraWithoutNoise);

    //Creating new 3D Plot.
    final ExtendedPlot3DPanel plot = new ExtendedPlot3DPanel();

    //Adding the data to the plot for later export.
    plot.addRawScans(scans);
    plot.addRawScansInMzRange(spectra);

    //Calculating the divisor needed to scale the z-axis.
    final double divisor = MassvoltamogramUtils.getDivisor(maxIntensity);

    //Adding all the spectra to the plot.
    MassvoltamogramUtils.addSpectraToPlot(spectraWithoutZeros, divisor, plot);

    //Setting up the plot correctly.
    plot.setAxisLabels("m/z", "Potential / mV",
        "Intensity / 10" + MassvoltamogramUtils.toSupercript((int) Math.log10(divisor))
            + " a.u.");
    plot.setFixedBounds(1, potentialRange.lowerEndpoint(), potentialRange.upperEndpoint());
    plot.setFixedBounds(0, mzRange.lowerEndpoint(), mzRange.upperEndpoint());

    //Adding the plot to a new MZmineTab.
    final MassvoltammogramTab mvTab = new MassvoltammogramTab("Massvoltammogram", plot);
    MZmineCore.getDesktop().addTab(mvTab);

    setStatus(TaskStatus.FINISHED);
  }
}
