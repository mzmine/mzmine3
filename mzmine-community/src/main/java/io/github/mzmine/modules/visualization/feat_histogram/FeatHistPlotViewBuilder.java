/*
 * Copyright (c) 2004-2024 The MZmine Development Team
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

package io.github.mzmine.modules.visualization.feat_histogram;

import io.github.mzmine.datamodel.AbundanceMeasure;
import io.github.mzmine.datamodel.features.FeatureList;
import io.github.mzmine.datamodel.features.FeatureListRow;
import io.github.mzmine.datamodel.features.ModularFeatureList;
import io.github.mzmine.datamodel.features.types.numbers.abstr.NumberType;
import io.github.mzmine.gui.chartbasics.chartthemes.EStandardChartTheme;
import io.github.mzmine.gui.chartbasics.simplechart.SimpleChartUtility;
import io.github.mzmine.gui.chartbasics.simplechart.SimpleXYChart;
import io.github.mzmine.gui.chartbasics.simplechart.datasets.ColoredXYDataset;
import io.github.mzmine.gui.chartbasics.simplechart.providers.PlotXYDataProvider;
import io.github.mzmine.gui.chartbasics.simplechart.providers.XYItemObjectProvider;
import io.github.mzmine.gui.chartbasics.simplechart.renderers.ColoredXYShapeRenderer;
import io.github.mzmine.javafx.mvci.FxViewBuilder;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.dataanalysis.significance.RowSignificanceTest;
import io.github.mzmine.modules.dataanalysis.significance.RowSignificanceTestModules;
import io.github.mzmine.modules.dataanalysis.significance.RowSignificanceTestResult;
import io.github.mzmine.parameters.ValuePropertyComponent;
import io.github.mzmine.parameters.parametertypes.DoubleComponent;
import java.awt.Stroke;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Accordion;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.math.util.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.ui.Layer;
import org.jfree.data.xy.XYDataset;

public class FeatHistPlotViewBuilder extends FxViewBuilder<FeatHistPlotModel> {

  private static final Logger logger = Logger.getLogger(FeatHistPlotViewBuilder.class.getName());

  private final int space = 5;
  private final Stroke annotationStroke = EStandardChartTheme.DEFAULT_MARKER_STROKE;
  private SimpleXYChart<PlotXYDataProvider> chart;

  public FeatHistPlotViewBuilder(FeatHistPlotModel model) {
    super(model);
  }

  @Override
  public Region build() {

    final BorderPane mainPane = new BorderPane();
    chart = new SimpleXYChart<>("Feature histogram plot");
//        "log2(fold change)", "-log10(p-Value)");
    mainPane.setCenter(chart);

//    final HBox pValueBox = createPValueBox();
//    final HBox abundanceBox = createAbundanceBox();
    final HBox dataTypeBox = createDataTypeBox();
//    final VBox pAndAbundance = new VBox(space, pValueBox, abundanceBox);
    final VBox pAndAbundance = new VBox(space, dataTypeBox);
//    final Region testConfigPane = createTestParametersPane();

//    final TitledPane controls = new TitledPane("Settings",
//        createControlsPane(pAndAbundance, testConfigPane));
//    final Accordion accordion = new Accordion(controls);
//    accordion.setExpandedPane(controls);
//    mainPane.setBottom(accordion);
    mainPane.setBottom(pAndAbundance);

    chart.setDefaultRenderer(new ColoredXYShapeRenderer());
    model.datasetsProperty().addListener((_, _, n) -> {
      chart.applyWithNotifyChanges(false, () -> {
        final var neutralColor = MZmineCore.getConfiguration().getDefaultColorPalette()
            .getNeutralColorAWT();
        chart.removeAllDatasets();
        chart.getXYPlot().clearAnnotations();
        if (n == null) {
          return;
        }
        n.forEach(datasetAndRenderer -> chart.addDataset(datasetAndRenderer.dataset(),
            datasetAndRenderer.renderer()));

        // all markers are set to index 0, so we dont need to remove them if a value updates.
        // p-Value line
//        chart.getXYPlot().addRangeMarker(0,
//            new ValueMarker(-Math.log10(model.getpValue()), neutralColor, annotationStroke),
//            Layer.FOREGROUND);
//        // annotation for fold change (a/b) = 0.5 (half)
//        chart.getXYPlot().addDomainMarker(0,
//            new ValueMarker(MathUtils.log(2, 0.5), neutralColor, annotationStroke),
//            Layer.FOREGROUND);
//        // annotation for fold change (a/b) = 2 (double)
//        chart.getXYPlot().addDomainMarker(0, new ValueMarker(1, neutralColor, annotationStroke),
//            Layer.FOREGROUND);
      });
    });

    addChartValueListener();
    initializeExternalSelectedRowListener();
    return mainPane;
  }

  @NotNull
  private HBox createDataTypeBox() {
    final HBox dataTypeBox = new HBox(space);
    Label label = new Label("Data type: ");
    final ComboBox<NumberType> dataTypeCombo = new ComboBox<>(
        model.getTypeChoices()
    );
    dataTypeCombo.setValue(model.getDataType());
    model.dataTypeProperty().bindBidirectional(dataTypeCombo.valueProperty());
    dataTypeBox.getChildren().addAll(label, dataTypeCombo);
    return dataTypeBox;
  }
  @NotNull
  private HBox createFeatureListsBox() {
    final HBox dataTypeBox = new HBox(space);
    Label label = new Label("Feature list: ");
    final ComboBox<List<FeatureList>> featureListCombo = new ComboBox<>(
        FXCollections.observableList(model.getFlists())
    );
    featureListCombo.setValue(model.getFlists().getFirst());
    model.selectedRowsProperty().bindBidirectional(featureListCombo.valueProperty());
    dataTypeBox.getChildren().addAll(label, featureListCombo);
    return dataTypeBox;
  }

//  @NotNull
//  private HBox createPValueBox() {
//    final HBox pValueBox = new HBox(space);
//    Label label = new Label("p-Value:");
//    final DoubleComponent pValueComponent = new DoubleComponent(100, 0.0, 1d,
//        new DecimalFormat("0.###"), 0.05);
//    Bindings.bindBidirectional(pValueComponent.getTextField().textProperty(),
//        model.pValueProperty(), new DecimalFormat("0.###"));
//    pValueBox.getChildren().addAll(label, pValueComponent.getTextField());
//    return pValueBox;
//  }

  @NotNull
  private FlowPane createControlsPane(Region... panes) {
    final FlowPane controls = new FlowPane(Orientation.HORIZONTAL);
    controls.setHgap(space);
    controls.setVgap(space);
    controls.getChildren().addAll(panes);
    controls.setPadding(new Insets(space));
    controls.setAlignment(Pos.TOP_LEFT);
    return controls;
  }

//  @NotNull
//  private HBox createAbundanceBox() {
//    final ComboBox<AbundanceMeasure> abundanceCombo = new ComboBox<>(
//        FXCollections.observableList(List.of(AbundanceMeasure.values())));
//    abundanceCombo.setValue(model.getAbundanceMeasure());
//    model.abundanceMeasureProperty().bindBidirectional(abundanceCombo.valueProperty());
//
//    final HBox abundanceBox = new HBox(space);
//    abundanceBox.getChildren().addAll(new Label("Abundance measure:"), abundanceCombo);
//    return abundanceBox;
//  }

  private Region createTestParametersPane() {

    final ComboBox<RowSignificanceTestModules> testComboBox = new ComboBox<>(
        FXCollections.observableList(RowSignificanceTestModules.TWO_GROUP_TESTS));

    FlowPane controls = new FlowPane(Orientation.HORIZONTAL, space, space);
    controls.setRowValignment(VPos.CENTER);
    controls.getChildren().addAll(new Label("Test type:"), testComboBox);

    HBox testConfigPane = new HBox(5);
    controls.getChildren().add(testConfigPane);

    // the combo box allows selection of the test and creates a component to configure the test
    testComboBox.valueProperty().addListener((_, _, n) -> {
      testConfigPane.getChildren().clear();
      final Region component = n.getModule().createConfigurationComponent();
      testConfigPane.getChildren().add(component);

      ((ValuePropertyComponent<?>) component).valueProperty()
          .addListener((ChangeListener<Object>) (_, _, _) -> {
            try {
              // try to update the test
              final RowSignificanceTest instance = n.getModule()
                  .getInstance((ValuePropertyComponent) component);
//              model.setTest(instance);
            } catch (Exception e) {
//              model.setTest(null);
              logger.log(Level.WARNING, e.getMessage(), e);
            }
          });
    });

    // listen to external changes of the test and update the combo box accordingly
//    model.testProperty().addListener((_, _, newTest) -> {
//      if (newTest == null) {
//        return;
//      }
//      final RowSignificanceTestModules newTestModule = testComboBox.getItems().stream()
//          .filter(t -> t.getTestClass().isInstance(newTest)).findFirst().orElse(null);
//      if (newTestModule != null) {
//        testComboBox.setValue(newTestModule);
//      } else {
//        logger.warning("Selected test is invalid.");
//      }
//    });

    testComboBox.getSelectionModel().selectFirst();
    return controls;
  }

  private void addChartValueListener() {
    chart.cursorPositionProperty().addListener((obs, o, pos) -> {
      final XYDataset dataset = pos.getDataset();
      if (!(dataset instanceof ColoredXYDataset cds)) {
        return;
      }
      if (!(cds.getValueProvider() instanceof XYItemObjectProvider<?> provider)) {
        return;
      }
      final int index = pos.getValueIndex();
      final Object item = provider.getItemObject(index);
      if (item instanceof RowSignificanceTestResult testResult) {
        model.selectedRowsProperty().set(List.of(testResult.row()));
      }
    });
  }

  // todo: enable as soon as we merge the pr with mvci row listener interfaces.
  private void initializeExternalSelectedRowListener() {
    model.selectedRowsProperty().addListener((_, old, rows) -> {
      if (rows.isEmpty() || ListUtils.isEqualList(old, rows)) {
        return;
      }
      final FeatureListRow selectedRow = rows.getFirst();
      if (selectedRow == null) {
        return;
      }
      SimpleChartUtility.selectItemInChart(chart, selectedRow, o1 -> {
        if (!(o1 instanceof RowSignificanceTestResult result)) {
          return null;
        }
        return result.row();
      }, RowSignificanceTestResult.class);
    });
  }
}
