package io.github.mzmine.modules.dataprocessing.id_sirius_cli.sirius_bridge;

import io.github.mzmine.datamodel.features.FeatureList;
import io.github.mzmine.datamodel.features.FeatureListRow;
import io.github.mzmine.datamodel.features.ModularFeatureList;
import io.github.mzmine.datamodel.features.compoundannotations.CompoundDBAnnotation;
import io.github.mzmine.modules.dataprocessing.id_sirius_cli.SiriusExecutionUtil;
import io.github.mzmine.modules.dataprocessing.id_sirius_cli.sirius_import.ImportOption;
import io.github.mzmine.modules.dataprocessing.id_sirius_cli.sirius_import.SiriusResultsImportParameters;
import io.github.mzmine.modules.dataprocessing.id_sirius_cli.sirius_import.SiriusResultsImportTask;
import io.github.mzmine.modules.io.export_features_sirius.SiriusExportParameters;
import io.github.mzmine.modules.io.export_features_sirius.SiriusExportTask;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.parameters.parametertypes.selectors.FeatureListsSelection;
import io.github.mzmine.taskcontrol.AbstractTask;
import io.github.mzmine.taskcontrol.TaskStatus;
import io.github.mzmine.util.MemoryMapStorage;
import java.io.File;
import java.time.Instant;
import java.util.Map;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SiriusRatingTask extends AbstractTask {

  private static final Logger logger = Logger.getLogger(SiriusRatingTask.class.getName());

  private String desc = "Sirius";
  private double progress = 0;
  private final ParameterSet parameters;

  public SiriusRatingTask(@Nullable MemoryMapStorage storage, @NotNull Instant moduleCallDate,
      ParameterSet parameters) {
    super(storage, moduleCallDate);

    this.parameters = parameters;
  }

  @Override
  public String getTaskDescription() {
    return desc;
  }

  @Override
  public double getFinishedPercentage() {
    return progress;
  }

  @Override
  public void run() {
    setStatus(TaskStatus.PROCESSING);

    final FeatureList flist = parameters.getParameter(SiriusRatingParameters.flist).getValue()
        .getMatchingFeatureLists()[0];
    final String cleanFlistName = flist.getName().replaceAll("[^a-zA-Z0-9.-]", "_");

    final File pathToSirius = parameters.getValue(SiriusRatingParameters.siriusPath);
    final File outputDir = parameters.getValue(SiriusRatingParameters.siriusProject);
    final File tsvDatabase = new File(outputDir, cleanFlistName + ".tsv");
    final File mgfFile = new File(outputDir, cleanFlistName + ".mgf");
    final File projectDirSiriusDb = new File(outputDir, "project-sirius");
    final File projectDirCompounds = new File(outputDir, "project-compounds");
    final AlgorithmProfile presets = parameters.getValue(SiriusRatingParameters.presets);
    final SiriusDatabaseType dbType = parameters.getValue(SiriusRatingParameters.databases);
    final File databaseFolder;

    logger.finest(() -> "Exporting rows to mgf.");
    desc = "Exporting rows to mgf.";
    exportDatabaseAnnotatedRowsToMgf(flist,
        parameters.getParameter(SiriusRatingParameters.siriusExportParam).getEmbeddedParameters(),
        mgfFile);
    progress = 0.05;

    if (dbType == SiriusDatabaseType.COMBINED || dbType == SiriusDatabaseType.ONLY_COMPOUNDS) {
      desc = "Compiling custom database.";
      logger.finest(() -> "Compiling custom database.");
      final Map<String, CompoundDBAnnotation> smilesMap = SiriusExecutionUtil.compileDatabase(flist,
          null);
      final boolean databaseSuccess = SiriusExecutionUtil.writeCustomDatabase(smilesMap,
          tsvDatabase);
      if (!databaseSuccess) {
        logger.severe("Cannot write data base file.");
        setErrorMessage("Cannot write custom database file for sirius.");
        setStatus(TaskStatus.ERROR);
        return;
      }

      progress = 0.1;
      desc = "Predicting custom database for " + flist.getName();
      logger.finest(() -> desc);
      databaseFolder = SiriusExecutionUtil.generateCustomDatabase(tsvDatabase, pathToSirius);
    } else {
      databaseFolder = null;
    }

    progress = 0.3;
    desc = "Running CSI:FingerID with custom database.";
    logger.finest(desc);

    // if dbtype == COMBINED, then run first with only online db and then with only expected
    // compounds, so we see how the expected ones rank compared to other suggestions
    if (dbType == SiriusDatabaseType.ALL_SIRIUS || dbType == SiriusDatabaseType.COMBINED
        || dbType == SiriusDatabaseType.PUBCHEM) {
      SiriusExecutionUtil.runFingerId(mgfFile, presets, projectDirSiriusDb, pathToSirius, null,
          dbType);
    }
    if ((dbType == SiriusDatabaseType.COMBINED || dbType == SiriusDatabaseType.ONLY_COMPOUNDS)
        && databaseFolder.exists()) {
      SiriusExecutionUtil.runFingerId(mgfFile, presets, projectDirCompounds, pathToSirius,
          databaseFolder, SiriusDatabaseType.ONLY_COMPOUNDS);
    }

    progress = 0.8;
    desc = "Importing Sirius results from project " + outputDir;
    logger.finest(() -> desc);
    if (projectDirCompounds.exists()) {
      importResults(projectDirCompounds, flist, true);
    }
    if (projectDirSiriusDb.exists()) {
      importResults(projectDirSiriusDb, flist, projectDirCompounds.exists() ? false : true);
    }

    setStatus(TaskStatus.FINISHED);
  }

  public void exportDatabaseAnnotatedRowsToMgf(@NotNull final FeatureList flist,
      ParameterSet siriusExportParameters, final File mgfFile) {

    final ParameterSet exportParam = new SiriusExportParameters(false).cloneParameterSet();
    exportParam.setParameter(SiriusExportParameters.MERGE_PARAMETER,
        siriusExportParameters.getValue(SiriusExportParameters.MERGE_PARAMETER));
    exportParam.setParameter(SiriusExportParameters.EXCLUDE_MULTICHARGE,
        siriusExportParameters.getValue(SiriusExportParameters.EXCLUDE_MULTICHARGE));
    exportParam.setParameter(SiriusExportParameters.NEED_ANNOTATION,
        siriusExportParameters.getValue(SiriusExportParameters.NEED_ANNOTATION));
    exportParam.setParameter(SiriusExportParameters.EXCLUDE_EMPTY_MSMS,
        siriusExportParameters.getValue(SiriusExportParameters.EXCLUDE_EMPTY_MSMS));
    exportParam.setParameter(SiriusExportParameters.EXCLUDE_MULTIMERS,
        siriusExportParameters.getValue(SiriusExportParameters.EXCLUDE_MULTIMERS));

    exportParam.setParameter(SiriusExportParameters.FEATURE_LISTS,
        new FeatureListsSelection((ModularFeatureList) flist));
    exportParam.setParameter(SiriusExportParameters.FILENAME, mgfFile);
    exportParam.setParameter(SiriusExportParameters.RENUMBER_ID, false);

    desc = "Exporting eligible rows to mgf.";
    final SiriusExportTask export = new SiriusExportTask(exportParam, Instant.now());
    for (final FeatureListRow row : flist.getRows()) {
      if (row.getCompoundAnnotations().isEmpty() || !row.hasMs2Fragmentation()) {
        continue;
      }
      export.runSingleRow(row);
    }
  }

  public void importResults(final File dir, final FeatureList flist, boolean replaceAnnotations) {
    final ParameterSet param = new SiriusResultsImportParameters().cloneParameterSet();
    param.setParameter(SiriusResultsImportParameters.importOption, ImportOption.BEST);
    param.setParameter(SiriusResultsImportParameters.flist,
        new FeatureListsSelection((ModularFeatureList) flist));
    param.setParameter(SiriusResultsImportParameters.projectDir, dir);
    param.setParameter(SiriusResultsImportParameters.replaceOldAnnotations, replaceAnnotations);
    final SiriusResultsImportTask importTask = new SiriusResultsImportTask(parameters, null,
        Instant.now());

    importTask.run(dir, (ModularFeatureList) flist, ImportOption.BEST, replaceAnnotations);
  }
}
