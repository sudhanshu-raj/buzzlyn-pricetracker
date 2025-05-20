import React, { useState, useMemo } from "react";
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import styles from "./ProductDataChart.module.css";
import { formatPrice } from "../utils/helperFunctions";

const ProductDataChart = ({ product }) => {
  // State variables for chart controls
  const [selectedPeriod, setSelectedPeriod] = useState("all");
  const [aggregationLevel, setAggregationLevel] = useState("day");

  // Helper functions for data filtering and formatting
  const filterDataByPeriod = (data, period) => {
    if (period === "all") return data;

    const now = new Date();
    let cutoffDate = new Date();

    switch (period) {
      case "week":
        cutoffDate.setDate(now.getDate() - 7);
        break;
      case "month":
        cutoffDate.setMonth(now.getMonth() - 1);
        break;
      case "6month":
        cutoffDate.setMonth(now.getMonth() - 6);
        break;
      case "year":
        cutoffDate.setFullYear(now.getFullYear() - 1);
        break;
      default:
        return data;
    }

    return data.filter((item) => new Date(item.date) >= cutoffDate);
  };

  const formatDateByPeriod = (date, period) => {
    const d = new Date(date);

    switch (period) {
      case "day":
        return d.toLocaleDateString("en-US", { month: "short", day: "numeric" });
      case "week":
        return d.toLocaleDateString("en-US", { month: "short", day: "numeric" });
      case "month":
        return d.toLocaleDateString("en-US", { month: "short", year: "2-digit" });
      case "year":
        return d.getFullYear().toString();
      default:
        return d.toLocaleDateString();
    }
  };

  const formatDateForTooltip = (date) => {
    return new Date(date).toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  // Function to format date for the price history table
  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    }).format(date);
  };

  // Aggregate data for different time periods
  const aggregateDataByPeriod = (data, period) => {
    if (!data || data.length === 0) return [];

    const aggregated = {};

    data.forEach((item) => {
      let key;
      const date = new Date(item.date);

      switch (period) {
        case "day":
          key = new Date(date).toISOString().split("T")[0]; // YYYY-MM-DD
          break;
        case "week":
          // Get start of week (Sunday)
          const startOfWeek = new Date(date);
          startOfWeek.setDate(date.getDate() - date.getDay());
          key = startOfWeek.toISOString().split("T")[0];
          break;
        case "month":
          key = `${date.getFullYear()}-${(date.getMonth() + 1)
            .toString()
            .padStart(2, "0")}`;
          break;
        case "year":
          key = `${date.getFullYear()}`;
          break;
        default:
          key = new Date(date).toISOString().split("T")[0];
      }

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
        price: group.min, // CHANGED: Using minimum price instead of average
        avgPrice: group.sum / group.count, // Keep average for reference
        minPrice: group.min,
        maxPrice: group.max,
      }))
      .sort((a, b) => new Date(a.date) - new Date(b.date));
  };

  // Process data based on period and aggregation level
  const processedData = useMemo(() => {
    if (!product || !product.priceHistory) return [];
    
    const filteredData = filterDataByPeriod(
      product.priceHistory,
      selectedPeriod
    );
    return aggregateDataByPeriod(filteredData, aggregationLevel);
  }, [product?.priceHistory, selectedPeriod, aggregationLevel]);

  // Filter the table data based on selected period
  const filteredTableData = useMemo(() => {
    if (!product || !product.priceHistory) return [];
    return filterDataByPeriod(product.priceHistory, selectedPeriod)
      .sort((a, b) => new Date(b.date) - new Date(a.date)); // Sort newest first
  }, [product?.priceHistory, selectedPeriod]);

  // If no product or price history data
  if (!product || !product.priceHistory || product.priceHistory.length === 0) {
    return (
      <div className={styles.priceHistoryContainer}>
        <h4 className={styles.sectionTitle}>Price History</h4>
        <div className={styles.noData}>No price history available</div>
      </div>
    );
  }

// Custom tooltip component to show min/max prices
const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      const data = payload[0].payload;
      return (
        <div className={styles.customTooltip}>
          <p className={styles.tooltipDate}>{formatDateForTooltip(label)}</p>
          {data.minPrice !== data.maxPrice ? (
            <>
              <p className={styles.tooltipPrice}>
                <span className={styles.tooltipLabel}>Lowest Price:</span> 
                {formatPrice(data.minPrice, 'INR')}
              </p>
              <p className={styles.tooltipPrice}>
                <span className={styles.tooltipLabel}>Highest Price:</span> 
                {formatPrice(data.maxPrice, 'INR')}
              </p>
            </>
          ) : (
            <p className={styles.tooltipPrice}>
              <span className={styles.tooltipLabel}>Price:</span> 
              {formatPrice(data.price, 'INR')}
            </p>
          )}
        </div>
      );
    }
    return null;
  };

  return (
    <div className={styles.priceHistoryContainer}>
      <h4 className={styles.sectionTitle}>Price History</h4>

      <div className={styles.chartControls}>
        <select
          className={styles.periodSelector}
          value={selectedPeriod}
          onChange={(e) => setSelectedPeriod(e.target.value)}
        >
          <option value="all">All Time</option>
          <option value="year">Past Year</option>
          <option value="6month">Past 6 Months</option>
          <option value="month">Past Month</option>
          <option value="week">Past Week</option>
        </select>

        <select
          className={styles.aggregationSelector}
          value={aggregationLevel}
          onChange={(e) => setAggregationLevel(e.target.value)}
        >
          <option value="day">Daily</option>
          <option value="week">Weekly</option>
          <option value="month">Monthly</option>
          {selectedPeriod === "all" && <option value="year">Yearly</option>}
        </select>
      </div>

      <div className={styles.priceHistoryChart}>
        <ResponsiveContainer width="100%" height="100%">
          <LineChart
            data={processedData}
            margin={{ top: 5, right: 5, bottom: 5, left: 5 }}
          >
            <XAxis
              dataKey="date"
              tickFormatter={(date) => formatDateByPeriod(date, aggregationLevel)}
              tick={{ fontSize: 12 }}
              stroke="var(--text-muted)"
              interval="preserveStartEnd"
              minTickGap={30}
            />
            <YAxis
              domain={["auto", "auto"]}
              tick={{ fontSize: 12 }}
              stroke="var(--text-muted)"
            />
            <Tooltip content={<CustomTooltip />} />
            <Line
              type="monotone"
              dataKey="price"
              stroke="var(--primary)"
              strokeWidth={2}
              dot={
                processedData.length > 50
                  ? false
                  : {
                      fill: "var(--primary)",
                      strokeWidth: 2,
                      r: 4,
                    }
              }
              activeDot={{ r: 6, fill: "var(--primary)" }}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
      
      <div className={styles.priceHistoryTable}>
        <div className={styles.tableHeader}>
          <div className={styles.tableCell}>Date</div>
          <div className={styles.tableCell}>Price</div>
        </div>
        <div className={styles.tableBody}>
          {filteredTableData.map((item, index) => (
            <div key={index} className={styles.tableRow}>
              <div className={styles.tableCell}>
                {formatDate(item.date)}
              </div>
              <div className={styles.tableCell}>
                {formatPrice(item.price, 'INR')}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ProductDataChart;