import { useState, useEffect, useRef } from "react"
import styles from "./SearchBar.module.css"

const SearchBar = ({ onSearch, products }) => {
  const [searchTerm, setSearchTerm] = useState("")
  const [isExpanded, setIsExpanded] = useState(false)
  const [searchHistory, setSearchHistory] = useState([])
  const [showHistory, setShowHistory] = useState(false)
  const [searchResults, setSearchResults] = useState([])
  const searchRef = useRef(null)
  const inputRef = useRef(null)

  // Add these new state variables after the existing state declarations
  const [suggestions, setSuggestions] = useState([])
  const [showSuggestions, setShowSuggestions] = useState(false)
  const [highlightedIndex, setHighlightedIndex] = useState(-1)

  // Load search history from localStorage on component mount
  useEffect(() => {
    const savedHistory = localStorage.getItem("searchHistory")
    if (savedHistory) {
      setSearchHistory(JSON.parse(savedHistory))
    }
  }, [])

  // Save search history to localStorage whenever it changes
  useEffect(() => {
    localStorage.setItem("searchHistory", JSON.stringify(searchHistory))
  }, [searchHistory])

  // Handle click outside to close dropdown
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (searchRef.current && !searchRef.current.contains(event.target)) {
        setShowHistory(false)
        setShowSuggestions(false)
      }
    }

    document.addEventListener("mousedown", handleClickOutside)
    return () => {
      document.removeEventListener("mousedown", handleClickOutside)
    }
  }, [])

  // Add this new useEffect after the existing useEffect hooks
  useEffect(() => {
    if (searchTerm.trim().length > 0) {
      generateSuggestions(searchTerm)
      setShowSuggestions(true)
      setShowHistory(false)
    } else {
      setSuggestions([])
      setShowSuggestions(false)
    }
  }, [searchTerm])

  const handleSearch = (term) => {
    const searchValue = term || searchTerm

    if (!searchValue.trim()) {
      onSearch(products)
      return
    }

    // Add to search history if it's a new search
    if (searchValue.trim() && !searchHistory.includes(searchValue.trim())) {
      const newHistory = [searchValue.trim(), ...searchHistory.slice(0, 9)]
      setSearchHistory(newHistory)
    }

    // Filter products based on multiple criteria
    const filteredProducts = products.filter((product) => {
      const searchLower = searchValue.toLowerCase()

      // Search in product name
      if (product.name && product.name.toLowerCase().includes(searchLower)) {
        return true
      }

      // Search in store
      if (product.store && product.store.toLowerCase().includes(searchLower)) {
        return true
      }

      // Search in price
      if (
        (product.currentPrice && product.currentPrice.toString().includes(searchLower)) ||
        (product.originalPrice && product.originalPrice.toString().includes(searchLower))
      ) {
        return true
      }

      // Search in target values
      if (product.target && typeof product.target === "object") {
        const targetEntries = Object.entries(product.target)
        for (const [targetName, isCompleted] of targetEntries) {
          if (targetName.toLowerCase().includes(searchLower)) {
            return true
          }
        }
      }

      // Search in stock status
      if (
        (product.stock_status && "in stock".includes(searchLower)) ||
        (!product.stock_status && "out of stock".includes(searchLower))
      ) {
        return true
      }

      // Search in tracking status
      if (
        (product.running_status && "running".includes(searchLower)) ||
        (!product.running_status && "stopped".includes(searchLower))
      ) {
        return true
      }

      return false
    })

    onSearch(filteredProducts)
    setSearchResults(filteredProducts)
    setShowHistory(false)
    setShowSuggestions(false)
  }

  const handleInputChange = (e) => {
    setSearchTerm(e.target.value)
    if (e.target.value === "") {
      onSearch(products)
    }
  }

  // Modify the handleKeyDown function to navigate suggestions with arrow keys
  const handleKeyDown = (e) => {
    if (e.key === "Enter") {
      if (highlightedIndex >= 0 && highlightedIndex < suggestions.length) {
        handleSuggestionClick(suggestions[highlightedIndex])
      } else {
        handleSearch()
      }
    } else if (e.key === "ArrowDown") {
      e.preventDefault()
      if (showSuggestions) {
        setHighlightedIndex((prev) => (prev < suggestions.length - 1 ? prev + 1 : prev))
      } else if (showHistory) {
        setHighlightedIndex((prev) => (prev < searchHistory.length - 1 ? prev + 1 : prev))
      }
    } else if (e.key === "ArrowUp") {
      e.preventDefault()
      setHighlightedIndex((prev) => (prev > 0 ? prev - 1 : 0))
    } else if (e.key === "Escape") {
      setShowSuggestions(false)
      setShowHistory(false)
      setHighlightedIndex(-1)
    }
  }

  const handleHistoryItemClick = (item) => {
    setSearchTerm(item)
    handleSearch(item)
    setShowHistory(false)
    setShowSuggestions(false)
  }

  const clearHistory = (e) => {
    e.stopPropagation()
    setSearchHistory([])
    localStorage.removeItem("searchHistory")
  }

  // Modify the handleFocus function to reset highlighted index
  const handleFocus = () => {
    setIsExpanded(true)
    setHighlightedIndex(-1)
    if (searchTerm.trim() === "") {
      setShowHistory(true)
      setShowSuggestions(false)
    } else {
      setShowSuggestions(true)
      setShowHistory(false)
    }
  }

  // Add this function to handle input blur
  const handleBlur = () => {
    // Use setTimeout to allow click events to process before hiding
    setTimeout(() => {
      setIsExpanded(false)
      setShowSuggestions(false)
      setShowHistory(false)
    }, 200)
  }

  const clearSearch = () => {
    setSearchTerm("")
    onSearch(products)
    if (inputRef.current) {
      inputRef.current.focus()
    }
  }

  // Add this new function after the existing functions
  const generateSuggestions = (term) => {
    if (!term.trim() || !products || products.length === 0) {
      setSuggestions([])
      return
    }

    const termLower = term.toLowerCase()

    // Get unique product names, stores, and categories for suggestions
    const productNameSuggestions = []
    const storeSuggestions = []
    const categorySuggestions = []
    const priceSuggestions = []
    const statusSuggestions = []

    // Track what we've already suggested to avoid duplicates
    const suggestedNames = new Set()
    const suggestedStores = new Set()
    const suggestedCategories = new Set()

    products.forEach((product) => {
      // Product name suggestions
      if (product.name && product.name.toLowerCase().includes(termLower)) {
        const name = product.name
        if (!suggestedNames.has(name)) {
          productNameSuggestions.push({
            type: "product",
            value: name,
            display: highlightMatch(name, term),
          })
          suggestedNames.add(name)
        }
      }

      // Store suggestions
      if (product.store && product.store.toLowerCase().includes(termLower)) {
        const store = product.store
        if (!suggestedStores.has(store)) {
          storeSuggestions.push({
            type: "store",
            value: store,
            display: highlightMatch(store, term),
          })
          suggestedStores.add(store)
        }
      }

      // Category suggestions (if available)
      if (product.category && product.category.toLowerCase().includes(termLower)) {
        const category = product.category
        if (!suggestedCategories.has(category)) {
          categorySuggestions.push({
            type: "category",
            value: category,
            display: highlightMatch(category, term),
          })
          suggestedCategories.add(category)
        }
      }

      // Price range suggestions
      if (product.currentPrice && product.currentPrice.toString().includes(termLower)) {
        const priceRange = `Price under ${Math.ceil(product.currentPrice / 100) * 100}`
        priceSuggestions.push({
          type: "price",
          value: priceRange,
          display: priceRange,
        })
      }

      // Status suggestions
      if ("in stock".includes(termLower) && product.stock_status) {
        statusSuggestions.push({
          type: "status",
          value: "In Stock",
          display: "In Stock",
        })
      } else if ("out of stock".includes(termLower) && !product.stock_status) {
        statusSuggestions.push({
          type: "status",
          value: "Out of Stock",
          display: "Out of Stock",
        })
      }
    })

    // Combine all suggestions, prioritizing exact matches and limiting total suggestions
    const allSuggestions = [
      ...productNameSuggestions.slice(0, 3),
      ...storeSuggestions.slice(0, 2),
      ...categorySuggestions.slice(0, 2),
      ...priceSuggestions.slice(0, 1),
      ...statusSuggestions.slice(0, 1),
    ].slice(0, 8) // Limit to 8 total suggestions

    // Add a "View all results for..." suggestion if we have results
    if (allSuggestions.length > 0) {
      allSuggestions.push({
        type: "viewAll",
        value: term,
        display: `View all results for "${term}"`,
      })
    }

    setSuggestions(allSuggestions)
  }

  // Add this helper function to highlight matching text
  const highlightMatch = (text, query) => {
    if (!query.trim()) return text

    const regex = new RegExp(`(${query.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")})`, "gi")
    const parts = text.split(regex)

    return (
      <>
        {parts.map((part, i) =>
          regex.test(part) ? (
            <strong key={i} className={styles.highlight}>
              {part}
            </strong>
          ) : (
            part
          ),
        )}
      </>
    )
  }

  // Add this function to handle suggestion selection
  const handleSuggestionClick = (suggestion) => {
    if (suggestion.type === "viewAll") {
      handleSearch(suggestion.value)
    } else {
      setSearchTerm(suggestion.value)
      handleSearch(suggestion.value)
    }
    setShowSuggestions(false)
  }

  return (
    <div className={styles.searchContainer} ref={searchRef}>
      <div className={`${styles.searchBar} ${isExpanded ? styles.expanded : ""}`}>
        <div className={styles.searchIcon}>
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
        </div>
        {/* Update the input element to include the onBlur handler */}
        <input
          ref={inputRef}
          type="text"
          placeholder="Search products, stores, price..."
          value={searchTerm}
          onChange={handleInputChange}
          onKeyDown={handleKeyDown}
          onFocus={handleFocus}
          onBlur={handleBlur}
          className={styles.searchInput}
        />
        {searchTerm && (
          <button className={styles.clearButton} onClick={clearSearch}>
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        )}
        <button className={styles.searchButton} onClick={() => handleSearch()}>
          Search
        </button>
      </div>

      {showHistory && searchHistory.length > 0 && (
        <div className={styles.historyDropdown}>
          <div className={styles.historyHeader}>
            <span>Recent Searches</span>
            <button className={styles.clearHistoryBtn} onClick={clearHistory}>
              Clear All
            </button>
          </div>
          <ul className={styles.historyList}>
            {searchHistory.map((item, index) => (
              <li key={index} className={styles.historyItem} onClick={() => handleHistoryItemClick(item)}>
                <div className={styles.historyItemContent}>
                  <svg
                    className={styles.historyIcon}
                    xmlns="http://www.w3.org/2000/svg"
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <circle cx="12" cy="12" r="10" />
                    <polyline points="12 6 12 12 16 14" />
                  </svg>
                  <span>{item}</span>
                </div>
              </li>
            ))}
          </ul>
        </div>
      )}

      {/* Add the suggestions dropdown after the history dropdown */}
      {showSuggestions && suggestions.length > 0 && (
        <div className={styles.suggestionsDropdown}>
          <ul className={styles.suggestionsList}>
            {suggestions.map((suggestion, index) => (
              <li
                key={`${suggestion.type}-${index}`}
                className={`${styles.suggestionItem} ${highlightedIndex === index ? styles.highlighted : ""}`}
                onClick={() => handleSuggestionClick(suggestion)}
              >
                {suggestion.type === "product" && (
                  <svg
                    className={styles.suggestionIcon}
                    xmlns="http://www.w3.org/2000/svg"
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
                    <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
                  </svg>
                )}
                {suggestion.type === "store" && (
                  <svg
                    className={styles.suggestionIcon}
                    xmlns="http://www.w3.org/2000/svg"
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                    <polyline points="9 22 9 12 15 12 15 22"></polyline>
                  </svg>
                )}
                {suggestion.type === "category" && (
                  <svg
                    className={styles.suggestionIcon}
                    xmlns="http://www.w3.org/2000/svg"
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
                  </svg>
                )}
                {suggestion.type === "price" && (
                  <svg
                    className={styles.suggestionIcon}
                    xmlns="http://www.w3.org/2000/svg"
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <line x1="12" y1="1" x2="12" y2="23"></line>
                    <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
                  </svg>
                )}
                {suggestion.type === "status" && (
                  <svg
                    className={styles.suggestionIcon}
                    xmlns="http://www.w3.org/2000/svg"
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <circle cx="12" cy="12" r="10"></circle>
                    <polyline points="12 6 12 12 16 14"></polyline>
                  </svg>
                )}
                {suggestion.type === "viewAll" && (
                  <svg
                    className={styles.suggestionIcon}
                    xmlns="http://www.w3.org/2000/svg"
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <circle cx="11" cy="11" r="8"></circle>
                    <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                  </svg>
                )}
                <span className={styles.suggestionText}>{suggestion.display}</span>
              </li>
            ))}
          </ul>
        </div>
      )}

      {searchTerm && searchResults.length === 0 && !showHistory && !showSuggestions && (
        <div className={styles.noResults}>
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            className={styles.noResultsIcon}
          >
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          <p>No results found for "{searchTerm}"</p>
        </div>
      )}
    </div>
  )
}

export default SearchBar
