import React, { useMemo, useRef } from "react";
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import html2canvas from 'html2canvas';

const ProductDataChart = ({ product }) => {
  const chartRef = useRef(null);
  
  // Sample data for development/testing purposes
  const tempPriceHistory = [
    { date: "2023-01-01", price: 100 },
    { date: "2023-01-02", price: 100 },
    { date: "2023-01-03", price: 100 },
    { date: "2023-01-04", price: 110 },
    { date: "2023-01-05", price: 110 },
    { date: "2023-01-06", price: 120 },
    { date: "2023-01-07", price: 120 }
  ];

  // Aggregate data by day (simplified default)
  const aggregateData = (data) => {
    const aggregated = {};

    data.forEach((item) => {
      const key = new Date(item.date).toISOString().split("T")[0];
      if (!aggregated[key]) {
        aggregated[key] = {
          sum: item.price,
          count: 1,
          min: item.price,
          max: item.price,
          date: key,
        };
      } else {
        aggregated[key].sum += item.price;
        aggregated[key].count += 1;
        aggregated[key].min = Math.min(aggregated[key].min, item.price);
        aggregated[key].max = Math.max(aggregated[key].max, item.price);
      }
    });

    return Object.values(aggregated)
      .map((group) => ({
        date: group.date,
        price: group.min,
        minPrice: group.min,
        maxPrice: group.max,
      }))
      .sort((a, b) => new Date(a.date) - new Date(b.date));
  };

  const processedData = useMemo(() => {
    // Use product's price history if available, otherwise use temp data
    const priceData = (product?.priceHistory && product.priceHistory.length > 0) 
      ? product.priceHistory 
      : tempPriceHistory;
    
    return aggregateData(priceData);
  }, [product?.priceHistory]);

  const formatDate = (date) => {
    const d = new Date(date);
    return d.toLocaleDateString("en-US", { month: "short", day: "numeric" });
  };

  // Improved screenshot function that captures just the chart area
  const takeScreenshot = () => {
    if (chartRef.current) {
      const chartWidth = 400; // Match the width defined in ResponsiveContainer
      const chartHeight = 300; // Match the height defined in ResponsiveContainer
      
      html2canvas(chartRef.current, {
        width: chartWidth,
        height: chartHeight,
        backgroundColor: null,
        useCORS: true,
        // Only include the content within these dimensions
        ignoreElements: (element) => {
          // Ignore elements outside the chart area
          const rect = element.getBoundingClientRect();
          const chartRect = chartRef.current.getBoundingClientRect();
          return (rect.right > chartRect.right || 
                 rect.bottom > chartRect.bottom ||
                 rect.left < chartRect.left ||
                 rect.top < chartRect.top);
        }
      }).then(canvas => {
        const image = canvas.toDataURL("image/png");
        const link = document.createElement('a');
        link.download = `price-history-chart-${new Date().toISOString().slice(0, 10)}.png`;
        link.href = image;
        link.click();
      });
    }
  };

  return (
    <div>
      {/* Container with explicit width to match chart dimensions */}
      <div 
        className="chart" 
        ref={chartRef} 
        style={{ 
          width: "400px", // Match the ResponsiveContainer width
          height: "300px", // Match the ResponsiveContainer height
          margin: "0 auto" // Center the chart if parent is wider
        }}
      >
        <ResponsiveContainer width={400} height={300}>
          <LineChart data={processedData}>
            <XAxis
              dataKey="date"
              tickFormatter={formatDate}
              stroke="var(--text-muted)"
              minTickGap={30}
            />
            <YAxis
              domain={["auto", "auto"]}
              stroke="var(--text-muted)"
            />
            {/* <Tooltip content={<CustomTooltip />} /> */}
            <Line
              type="monotone"
              dataKey="price"
              stroke="var(--primary)"
              strokeWidth={2}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
      
      <button 
        onClick={takeScreenshot}
        style={{
          marginTop: "10px",
          padding: "8px 16px",
          backgroundColor: "var(--primary)",
          color: "white",
          border: "none",
          borderRadius: "4px",
          cursor: "pointer",
          display: "flex",
          alignItems: "center",
          gap: "6px"
        }}
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M19 14V22H5V14" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
          <path d="M12 15L12 3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
          <path d="M7 8L12 3L17 8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
        Download Chart
      </button>
    </div>
  );
};

export default ProductDataChart;