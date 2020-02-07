package roboterroridentifier;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import roboterroridentifier.LoggerFilter.LogList;

/**
 * A class that allows for graph generation and display.
 */
public class GraphManager {
    public enum GraphType {
        LINE(new Line()), BAR(new Bar()), PIE(new Pie()), MULTILINE(new MultiLine()), AREA(new Area());

        public GraphDraw getGraph() {
            return graph;
        }

        private final GraphDraw graph;

        GraphType(final GraphDraw graph) {
            this.graph = graph;
        }
    }

    public interface GraphDraw {
        void draw(ArrayList<LogList> data, double[] bounds);
    }

    public static void addGraph(final GraphType type, final ArrayList<LogList> dataSet, final double[] bounds) {
        type.getGraph().draw(dataSet, bounds);
    }

    public static int maxSec(final LogList lldata) {
        return lldata.timeStamps.size() < 1 ? 0
                : (int) (Double.parseDouble(lldata.timeStamps.get(lldata.timeStamps.size() - 1)));
    }

    public static int[] getInterval(final LogList data) {
        final int[] intervals = new int[2];
        final int maxSize = data.timeStamps.size();
        intervals[0] = maxSize / 4;
        try {
            intervals[1] = maxSize / intervals[0];
        } catch (final ArithmeticException e) {
            intervals[1] = 0;
        }
        return intervals;
    }

    public static int[] getInterval(final ArrayList<LogList> data) {
        final int[] intervals = new int[2];
        double maxSize = 0;
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < data.get(i).timeStamps.size(); j++) {
                if (Double.valueOf(data.get(i).timeStamps.get(j)) > maxSize) {
                    maxSize = Double.valueOf(data.get(i).timeStamps.get(j));
                }
            }
        }
        intervals[0] = (int) maxSize / 4;
        try {
            intervals[1] = (int) maxSize / intervals[0];
        } catch (final ArithmeticException e) {
            intervals[1] = 0;
        }
        return intervals;
    }

    public static class Bar implements GraphDraw {
        @Override
        public void draw(final ArrayList<LogList> data, final double[] bounds) {
            final DefaultCategoryDataset objDataset = new DefaultCategoryDataset();
            final String[] labels = LoggerFilter.TYPE_KEYS;
            final int[] sizesInRange = new int[LoggerFilter.TYPE_KEYS.length];

            for (int i = 0; i < labels.length; i++) {
                for (int j = 0; j < data.get(i).timeStamps.size(); j++) {
                    if (Double.valueOf(data.get(i).timeStamps.get(j)) > bounds[0]
                            && Double.valueOf(data.get(i).timeStamps.get(j)) < bounds[1]) {
                        sizesInRange[i]++;
                    }
                }

                objDataset.setValue(sizesInRange[i], labels[i], labels[i]);
            }

            final JFreeChart objChart = ChartFactory.createBarChart("Type Message Bar Graph", // Chart title
                    "Time", // Domain axis label
                    "Number of Messages", // Range axis label
                    objDataset, // Chart Data
                    PlotOrientation.VERTICAL, // orientation
                    true, // include legend?
                    true, // include tooltips?
                    false // include URLs?
            );

            final ChartFrame frame = new ChartFrame("SubsystemBar", objChart);
            frame.pack();
            frame.setVisible(true);
        }
    }

    public static class Line implements GraphDraw {
        @Override
        public void draw(final ArrayList<LogList> data, final double[] bounds) {
            final XYSeries objDataset = new XYSeries("All Messages Amount");

            final ArrayList<LogList> dataInRange = new ArrayList<>();
            dataInRange.add(new LogList());

            for (int j = 0; j < data.get(0).timeStamps.size(); j++) {
                if (Double.valueOf(data.get(0).timeStamps.get(j)) > bounds[0]
                        && Double.valueOf(data.get(0).timeStamps.get(j)) < bounds[1]) {
                    dataInRange.get(0).timeStamps.add(data.get(0).timeStamps.get(j));
                    dataInRange.get(0).messages.add(data.get(0).messages.get(j));
                }
            }
            final int maxSec = maxSec(dataInRange.get(0));
            final int[] summedData = new int[maxSec + 1];
            for (int i = 0; i < summedData.length; i++) {
                final int bottomBound = i;
                final int topBound = i + 1;
                for (int j = 0; j < dataInRange.get(0).timeStamps.size(); j++) {
                    final double ts = Double.parseDouble(dataInRange.get(0).timeStamps.get(j));
                    if (ts > bottomBound && ts <= topBound) {
                        summedData[i]++;
                    }
                }
            }
            for (int i = 0; i < summedData.length; i++) {
                objDataset.add(i, summedData[i]);
            }
            final XYSeriesCollection xydata = new XYSeriesCollection(objDataset);
            final JFreeChart objChart = ChartFactory.createXYStepChart("All Messages Line Graph", // Chart title
                    "Time", // Domain axis label
                    "Number of Messages", // Range axis label
                    xydata, // Chart Data
                    PlotOrientation.VERTICAL, // orientation
                    true, // include legend?
                    true, // include tooltips?
                    false // include URLs?
            );
            final NumberAxis xAxis = new NumberAxis();
            xAxis.setTickUnit(new NumberTickUnit(objDataset.getMaxX() > 100 ? 5 : 1));
            final NumberAxis yAxis = new NumberAxis();
            yAxis.setTickUnit(new NumberTickUnit(1));
            final XYPlot plot = (XYPlot) objChart.getPlot();
            plot.setDomainAxis(xAxis);
            plot.setRangeAxis(yAxis);
            final ChartPanel cPanel = new ChartPanel(objChart);

            cPanel.setMouseZoomable(true);

            final JFrame frame = new JFrame("All Messages Line Graph");
            final JScrollPane chartScroll = new JScrollPane(cPanel);
            frame.getContentPane().add(chartScroll);
            frame.pack();
            frame.setVisible(true);
        }
    }

    public static class MultiLine implements GraphDraw {
        @Override
        public void draw(final ArrayList<LogList> data, final double[] bounds) {
            final ArrayList<LogList> dataInRange = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                dataInRange.add(new LogList());
            }
            for (int i = 0; i < data.size(); i++) {
                final XYSeries objDataset = new XYSeries(
                        "Messages in " + LoggerFilter.SUBSYSTEM_KEYS[i] + " by Amount");
                for (int j = 0; j < data.get(i).timeStamps.size(); j++) {
                    if (Double.valueOf(data.get(i).timeStamps.get(j)) > bounds[0]
                            && Double.valueOf(data.get(i).timeStamps.get(j)) < bounds[1]) {
                        dataInRange.get(i).timeStamps.add(data.get(i).timeStamps.get(j));
                        dataInRange.get(i).messages.add(data.get(i).messages.get(j));
                    }
                }
                if (dataInRange.get(i).timeStamps.size() != 0) {
                    final int maxSec = maxSec(dataInRange.get(i));
                    final int[] summedData = new int[maxSec + 1];
                    for (int j = 0; j < summedData.length; j++) {
                        final int bottomBound = j;
                        final int topBound = j + 1;
                        for (int k = 0; k < dataInRange.get(i).timeStamps.size(); k++) {
                            final double ts = Double.parseDouble(dataInRange.get(i).timeStamps.get(k));
                            if (ts > bottomBound && ts <= topBound) {
                                summedData[j]++;
                            }
                        }
                    }
                    for (int j = 0; j < summedData.length; j++) {
                        objDataset.add(j, summedData[j]);
                    }
                    final XYSeriesCollection xydata = new XYSeriesCollection(objDataset);
                    final JFreeChart objChart = ChartFactory.createXYStepChart(
                            "Messages in " + LoggerFilter.SUBSYSTEM_KEYS[i] + " by Amount", // Chart title
                            "Time", // Domain axis label
                            "Number of Messages", // Range axis label
                            xydata, // Chart Data
                            PlotOrientation.VERTICAL, // orientation
                            true, // include legend?
                            true, // include tooltips?
                            false // include URLs?
                    );
                    final NumberAxis xAxis = new NumberAxis();
                    xAxis.setTickUnit(new NumberTickUnit(objDataset.getMaxX() > 100 ? 5 : 1));
                    final NumberAxis yAxis = new NumberAxis();
                    yAxis.setTickUnit(new NumberTickUnit(1));
                    final XYPlot plot = (XYPlot) objChart.getPlot();
                    plot.setDomainAxis(xAxis);
                    plot.setRangeAxis(yAxis);
                    final ChartPanel cPanel = new ChartPanel(objChart);

                    cPanel.setMouseZoomable(true);

                    final JFrame frame = new JFrame("Subsystem Messages Multiline Graph");
                    final JScrollPane chartScroll = new JScrollPane(cPanel);
                    frame.getContentPane().add(chartScroll);
                    frame.pack();
                    frame.setVisible(true);
                }
            }

        }
    }

    public static class Pie implements GraphDraw {
        @Override
        public void draw(final ArrayList<LogList> data, final double[] bounds) {
            final DefaultPieDataset objDataset = new DefaultPieDataset();
            final String[] labels = LoggerFilter.SUBSYSTEM_KEYS;

            final int[] sizesInRange = new int[labels.length];

            for (int i = 0; i < labels.length; i++) {
                for (int j = 0; j < data.get(i).timeStamps.size(); j++) {
                    if (Double.valueOf(data.get(i).timeStamps.get(j)) > bounds[0]
                            && Double.valueOf(data.get(i).timeStamps.get(j)) < bounds[1]) {
                        sizesInRange[i]++;
                    }
                }
            }
            for (int i = 0; i < data.size(); i++) {
                objDataset.setValue(labels[i], sizesInRange[i]);
            }

            final JFreeChart pieChart = ChartFactory.createPieChart("Subsystem Type Messages by Count", // Chart
                                                                                                        // title
                    objDataset, // Chart Data
                    true, // include legend?
                    true, // include tooltips?
                    false // include URLs?
            );

            final ChartFrame frame = new ChartFrame("TypePie", pieChart);
            frame.pack();
            frame.setVisible(true);
            LoggerGUI.printToFrame("Pie graph of message types constructed successfully");
        }
    }

    public static class Area implements GraphDraw {
        @Override
        public void draw(final ArrayList<LogList> data, final double[] bounds) {
            final DefaultCategoryDataset objDataset = new DefaultCategoryDataset();
            final String[] labels = LoggerFilter.SUBSYSTEM_KEYS;

            final ArrayList<LogList> dataInRange = new ArrayList<>();
            int maxIndex = 0;
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).timeStamps.size() > data.get(maxIndex).timeStamps.size()) {
                    maxIndex = i;
                }
                dataInRange.add(new LogList());
                for (int j = (int) bounds[0]; j < data.get(i).timeStamps.size(); j++) {
                    if (Double.valueOf(data.get(i).timeStamps.get(j)) < bounds[1]) {
                        dataInRange.get(i).timeStamps.add(data.get(i).timeStamps.get(j));
                    }
                }
            }

            final ArrayList<int[]> errors = new ArrayList<>(dataInRange.size());
            for (int i = 0; i < dataInRange.size(); i++) {
                errors.add(new int[getInterval(dataInRange)[1]]);
            }
            for (int i = 0; i < errors.size(); i++) {
                int timestampIndex = 0;
                int nullIndex = 0;
                for (int j = 0; j < errors.get(i).length; j++) {
                    for (int k = 0; k < getInterval(dataInRange)[0]; k++) {
                        try {
                            if (Double.valueOf(dataInRange.get(i).timeStamps.get(timestampIndex)) == nullIndex) {
                                errors.get(i)[j] += 1;
                                timestampIndex++;
                            }
                            nullIndex++;
                        } catch (final IndexOutOfBoundsException e) {

                        }
                    }
                }
            }
            for (int i = 0; i < errors.size(); i++) {
                for (int j = 0; j < errors.get(i).length; j++) {
                    objDataset.addValue(errors.get(i)[j], labels[i], "" + (getInterval(dataInRange)[0]) * (j + 1));
                }
            }

            // System.out.println(errors.size());
            final JFreeChart objChart = ChartFactory.createAreaChart("Subsystem Message Area Graph", // Chart title
                    "Subsystem", // Domain axis label
                    "Number of Messages", // Range axis label
                    objDataset, // Chart Data
                    PlotOrientation.VERTICAL, // orientation
                    true, // include legend?
                    true, // include tooltips?
                    false // include URLs?
            );

            final ChartFrame frame = new ChartFrame("SubsystemArea", objChart);
            frame.pack();
            frame.setVisible(true);
        }
    }
}