"use client"

import { createContext, useContext, useEffect, useState } from "react"

const ThemeContext = createContext(null)

// export function ThemeProvider({ children }) {
//   const [theme, setTheme] = useState(() => {
//     // Check for saved theme in localStorage
//     const savedTheme = localStorage.getItem("theme")

//     // Check for system preference if no saved theme
//     if (!savedTheme) {
//       return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light"
//     }

//     return savedTheme
//   })

//   useEffect(() => {
//     // Update localStorage when theme changes
//     localStorage.setItem("theme", theme)

//     // Update document class for CSS variables
//     if (theme === "dark") {
//       document.documentElement.classList.add("dark")
//     } else {
//       document.documentElement.classList.remove("dark")
//     }
//   }, [theme])

//   const toggleTheme = () => {
//     setTheme((prevTheme) => (prevTheme === "light" ? "dark" : "light"))
//   }

//   return <ThemeContext.Provider value={{ theme, toggleTheme }}>{children}</ThemeContext.Provider>
// }

export function ThemeProvider({ children }) {
  // Instead of checking localStorage or system preference, always use dark mode
  const [theme] = useState("dark");
  
  useEffect(() => {
    // Always add dark class to document
    document.documentElement.classList.add("dark");
    
    // Persist this setting (optional)
    localStorage.setItem("theme", "dark");
  }, []);

  // Return a dummy toggle function that does nothing
  const toggleTheme = () => {
    // No-op function (does nothing)
    console.log("Theme toggling is disabled - app is dark mode only");
  };

  return <ThemeContext.Provider value={{ theme, toggleTheme }}>{children}</ThemeContext.Provider>;
}


export function useTheme() {
  const context = useContext(ThemeContext)
  if (context === null) {
    throw new Error("useTheme must be used within a ThemeProvider")
  }
  return context
}

