package com.org.scraper_bkd.service;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.File;
import java.io.IOException;

public class GraphGenerator extends ApplicationFrame {

    private static final Logger logger = LoggerFactory.getLogger(GraphGenerator.class);

    //this only generates the graph in swing
    public GraphGenerator(String title, List<PriceEntry> priceEntries) {
        super(title);

        DefaultCategoryDataset dataset = createDataset(priceEntries);

        // Create the chart
        JFreeChart chart = ChartFactory.createLineChart(
                null, "Date", "Price",
                dataset, PlotOrientation.VERTICAL,
                false, false, false
        );

        CategoryPlot plot = chart.getCategoryPlot();

        // Set background and gridlines
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(new Color(150, 150, 150));

        // Customize X-axis (category axis)
        CategoryAxis xAxis = plot.getDomainAxis();
        xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45); // Rotate labels 45 degrees

        // Optionally skip labels if too many
        List<String> dates = priceEntries.stream().map(PriceEntry::getDate).toList();
        if (dates.size() > 20) {
            int step = Math.max(1, dates.size() / 12); // show at most 12 ticks
            for (int i = 0; i < dates.size(); i++) {
                if (i % step != 0) {
                    xAxis.setTickLabelPaint(dates.get(i), new Color(0, 0, 0, 0)); // hide some ticks
                }
            }
        }

        // Set custom renderer
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(30, 144, 255)); // DodgerBlue
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setDefaultShapesVisible(priceEntries.size() <= 30);
        plot.setRenderer(renderer);

        // Set custom Y-axis
       // List<Long> customTicks = Arrays.asList(14500L, 15000L, 15200L, 15500L, 15900L, 16500L);
        List<Long> customTicks=new ArrayList<>();
        for(PriceEntry entry : priceEntries ){
            if(!customTicks.contains(entry.getPrice())){
                customTicks.add(entry.getPrice());
            }
        }

        CustomTickAxis customAxis = new CustomTickAxis(customTicks);

        // Set Y-axis range to exactly cover those ticks
        long min = customTicks.stream().min(Long::compareTo).get();
        long max = customTicks.stream().max(Long::compareTo).get();
        long margin=getYMargin(max);

        customAxis.setRange(min - margin, max + margin);
        plot.setRangeAxis(customAxis);
        // Display the chart
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(800, 500));
        setContentPane(panel);
    }

    //it used to generate the graph image , and will save in src/main/resources/userGraphs/{imageName}
    public static String createChartImage(List<PriceEntry> priceEntries,
                                                 int width, int height,String imageName) throws IOException {
        try {
            // Create dataset
            DefaultCategoryDataset dataset = createDataset(priceEntries);

            // Create the chart
            JFreeChart chart = ChartFactory.createLineChart(
                    null, "Date", "Price",
                    dataset, PlotOrientation.VERTICAL,
                    false, false, false
            );

            CategoryPlot plot = chart.getCategoryPlot();

            // Set background and gridlines
            plot.setBackgroundPaint(Color.white);
            plot.setRangeGridlinePaint(new Color(150, 150, 150));

            // Customize X-axis (category axis)
            CategoryAxis xAxis = plot.getDomainAxis();
            xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45); // Rotate labels 45 degrees

            // Optionally skip labels if too many
            List<String> dates = priceEntries.stream().map(PriceEntry::getDate).toList();
            if (dates.size() > 20) {
                int step = Math.max(1, dates.size() / 12); // show at most 12 ticks
                for (int i = 0; i < dates.size(); i++) {
                    if (i % step != 0) {
                        xAxis.setTickLabelPaint(dates.get(i), new Color(0, 0, 0, 0)); // hide some ticks
                    }
                }


            }

            // Set custom renderer
            LineAndShapeRenderer renderer = new LineAndShapeRenderer();
            renderer.setSeriesPaint(0, new Color(30, 144, 255)); // DodgerBlue
            renderer.setSeriesStroke(0, new BasicStroke(2.5f));
            renderer.setDefaultShapesVisible(priceEntries.size() <= 30);

            plot.setRenderer(renderer);

            // Set custom Y-axis
            List<Long> customTicks = new ArrayList<>();
            for (PriceEntry entry : priceEntries) {
                if (!customTicks.contains(entry.getPrice())) {
                    customTicks.add(entry.getPrice());
                }
            }

            CustomTickAxis customAxis = new CustomTickAxis(customTicks);

            // Set Y-axis range
            long min = customTicks.stream().min(Long::compareTo).orElse(0L);
            long max = customTicks.stream().max(Long::compareTo).orElse(0L);
            long margin = getYMargin(max);

            customAxis.setRange(min - margin, max + margin);
            plot.setRangeAxis(customAxis);

            // Convert chart to image and return
            String outputDir = "userGraphs"; // Create this directory or make it configurable
            new File(outputDir).mkdirs(); // Ensure directory exists

            File image = new File(outputDir + "/" + imageName + ".png");
            ChartUtils.saveChartAsPNG(image, chart, width, height);

            return image.getPath();
        } catch (Exception e) {
            logger.error("An error occurred while creating image for graph : {}",e.getMessage());
            return null;
        }

    }

    // Make the dataset creation static
    private static DefaultCategoryDataset createDataset(List<PriceEntry> priceHistory) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (PriceEntry entry : priceHistory) {
            dataset.addValue(entry.getPrice(), "Price", entry.getDate());
        }

        return dataset;
    }

    // Custom axis class to restrict tick labels
    static class CustomTickAxis extends NumberAxis {
        private final List<Long> allowedTicks;

        public CustomTickAxis(List<Long> allowedTicks) {
            super("Price");
            this.allowedTicks = allowedTicks;
        }

        @Override
        protected List<Tick> refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea,
                                                  RectangleEdge edge) {
            List<Tick> ticks = new java.util.ArrayList<>();
            for (Long val : allowedTicks) {
                ticks.add(new NumberTick(val, val.toString(), TextAnchor.CENTER_RIGHT,
                        TextAnchor.CENTER_RIGHT, 0.0));
            }
            return ticks;
        }
    }

    //priceEntry model class
    static class PriceEntry{
        private final  Long price;
        private final String date;

        public PriceEntry(Long price, int date){
            this.price= price;
            this.date = String.format("%02d", date);
        }

        public Long getPrice(){
            return price;
        }
        public String getDate(){
            return date;
        }

        public String toString(){

            return "Price : "+price+", Date: "+date;
        }
    }

    //to get the y axis margin for min and max Y value
    static Long getYMargin(Long value){
        if(value==null || value==0){
            return 1L;
        }
        long absValue=Math.abs(value);
        int digits= (int)Math.log10(absValue)+1;

        switch (digits){
            case 1:
            case 2:
                return 1L;
            case 3:
                return 10L;
            default:
                return (long) ((long) 5 * Math.pow(10, digits - 3));
        }
    }

    public static void main(String[] args) throws IOException {

        List<PriceEntry> priceEntries = Arrays.asList(
                new PriceEntry(59999L, 1),  // Regular price $599.99
                new PriceEntry(59999L, 2),
                new PriceEntry(59999L, 3),
                new PriceEntry(57999L, 4),  // Small discount
                new PriceEntry(57999L, 5),
                new PriceEntry(54999L, 6),  // Weekend sale
                new PriceEntry(54999L, 7),
                new PriceEntry(59999L, 8),  // Back to regular price
                new PriceEntry(59999L, 9),
                new PriceEntry(59999L, 10),
                new PriceEntry(59999L, 11),
                new PriceEntry(62499L, 12), // Small price increase
                new PriceEntry(62499L, 13),
                new PriceEntry(62499L, 14),
                new PriceEntry(62499L, 15)
        );
        System.out.println(createChartImage(priceEntries,600,500,"userGraph2"));

        // enable this only if want to view the graph in swing GUI
//        SwingUtilities.invokeLater(() -> {
//            GraphGenerator demo = new GraphGenerator("Custom Y-Axis Chart",priceEntries);
//            demo.pack();
//            demo.setLocationRelativeTo(null);
//            demo.setVisible(true);
//        });
    }
}
