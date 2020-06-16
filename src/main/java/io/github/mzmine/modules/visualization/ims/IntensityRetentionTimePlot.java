package io.github.mzmine.modules.visualization.ims;

import io.github.mzmine.gui.chartbasics.gui.javafx.EChartViewer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.util.logging.Logger;

public class IntensityRetentionTimePlot extends EChartViewer {

  private XYPlot plot;
  private JFreeChart chart;
  private Logger logger = Logger.getLogger(this.getClass().getName());
  static final Font legendFont = new Font("SansSerif", Font.PLAIN, 12);

  public IntensityRetentionTimePlot(XYDataset dataset) {

    super(
        ChartFactory.createXYLineChart(
            null,
            "retention time",
            "intensity",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false));

    chart = getChart();

    plot = chart.getXYPlot();
    var renderer = new XYLineAndShapeRenderer();
    renderer.setSeriesPaint(0, Color.GREEN);
    renderer.setSeriesStroke(0, new BasicStroke(1.0f));

    plot.setRenderer(renderer);
    plot.setBackgroundPaint(Color.BLACK);
    plot.setRangeGridlinePaint(Color.RED);
    plot.setDomainGridlinePaint(Color.RED);
    plot.setOutlinePaint(Color.red);
    plot.setOutlineStroke(new BasicStroke(2.5f));

    chart.getLegend().setFrame(BlockBorder.NONE);
  }
}
