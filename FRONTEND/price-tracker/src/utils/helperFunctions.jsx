import {pincodeBasedTrackingBrands} from "./constants";

const truncateText = (text, maxLength = 50) => {
    return text && text.length > maxLength 
      ? text.substring(0, maxLength) + "..." 
      : text;
  };


  function getCurrencySymbol(currencyCode) {
    const symbols = {
      USD: "$",
      EUR: "€",
      GBP: "£",
      INR: "₹",
      JPY: "¥",
      CNY: "¥",
      AUD: "A$",
      CAD: "C$",
      CHF: "CHF",
      KRW: "₩",
      RUB: "₽",
      BRL: "R$",
      ZAR: "R",
      // Add more if needed
    };
  
    return symbols[currencyCode.toUpperCase()] || currencyCode;
  }

  const formatPrice = (
    price,
    currencyCode = "USD",
    locale = "en-US"
  ) => {
    try {
      return new Intl.NumberFormat(locale, {
        style: "currency",
        currency: currencyCode,
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      }).format(price);
    } catch (error) {
      console.error(
        `Error formatting price with currency ${currencyCode}:`,
        error
      );
      // Fallback to basic formatting if there's an error
      return `${currencyCode} ${Number.parseFloat(price).toFixed(2)}`;
    }
  };

  const isPincodeTrackingAvailable = (brand) => {
    return pincodeBasedTrackingBrands.some((supportedBrands) => supportedBrands=== brand);
  }

export {
    truncateText,
    getCurrencySymbol,
    formatPrice,
    isPincodeTrackingAvailable
}