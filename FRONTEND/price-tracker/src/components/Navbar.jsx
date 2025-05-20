import { useState, useEffect, useRef } from "react";
import { Link, useLocation ,useNavigate} from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import {
  Menu,
  X,
  ChevronDown,
  LogOut,
  User,
  Settings,
  BarChart2,
} from "lucide-react";
import styles from "./Navbar.module.css";
import {logout} from "../apicalls/authAPIs"; // Adjust the import path as necessary
import { getErrorMessage } from "../services/handleErrorMssg"; // Adjust the import path as necessary
import { useAuth } from "../context/AuthContext";
import buzzlyn from "../assets/buzzlyn.svg";

export function Navbar() {
  const { isAuthenticated, user, logout: logoutContext } = useAuth();
  const navigate = useNavigate();

  const userData = user || {
    firstName: "John Doe",
  email: "john.doe@example.com",
  avatarUrl: "/placeholder.svg?height=40&width=40",
  };


  const [isOpen, setIsOpen] = useState(false);
  const [isScrolled, setIsScrolled] = useState(false);
  const location = useLocation();
  const pathname = location.pathname;
  const dropdownRef = useRef(null);
  const mobileMenuRef = useRef(null);
  const [isProfileOpen, setIsProfileOpen] = useState(false);

  // Detect scroll for navbar styling
  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 10);
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  // Close mobile menu when route changes
  useEffect(() => {
    setIsOpen(false);
  }, [pathname]);

  // Close dropdown when clicking outside for dekstop
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsProfileOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  // Add a new handler for the mobile menu
  useEffect(() => {
    const handleClickOutsideMobile = (event) => {
      if (
        mobileMenuRef.current &&
        !mobileMenuRef.current.contains(event.target)
      ) {
        setIsOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutsideMobile);
    return () => {
      document.removeEventListener("mousedown", handleClickOutsideMobile);
    };
  }, []);

  // Navigation items based on authentication status
  const navItems = isAuthenticated
    ? [
        { name: "Track", path: "/track" },
        { name: "Dashboard", path: "/dashboard" },
      ]
    : [
        // { name: "Track", path: "/track" },
        // { name: "About", path: "/about" },
        // { name: "Contact", path: "/contact" },
      ];

  const handleLogout =async () => {
   
    const resposne=await logout();  
    console.log("Logout response:", resposne); // Debugging line
    if (resposne.success === false) {
      const error = getErrorMessage(resposne.error, "Logout failed");
      console.error(error); // Log the error message
      return;
    }
    logoutContext();
    navigate('/');

  };

  return (
    <header className={`${styles.header} ${isScrolled ? styles.scrolled : ""}`}>
      <div className={styles.container}>
        <div className={styles.leftSection}>
          <Link to="/" className={styles.logo}>
            <img src={buzzlyn} alt="Logo" className={styles.logoImage} />
            <span className={styles.logoText}>buzzlyn</span>
          </Link>

          {/* Desktop Navigation */}
        </div>

        <div className={styles.rightSection}>
          <nav className={styles.desktopNav}>
            {navItems.map((item) => (
              <Link
                key={item.name}
                to={item.path}
                className={`${styles.navLink} ${
                  pathname === item.path ? styles.active : ""
                }`}
              >
                {item.name}
              </Link>
            ))}
          </nav>
          {/* Desktop Auth Actions */}
          <div className={styles.desktopAuthActions}>
            {isAuthenticated? (
              <div className={styles.profileDropdown} ref={dropdownRef}>
                <div
                  className={styles.profileTrigger}
                  onClick={() => setIsProfileOpen(!isProfileOpen)}
                  onMouseEnter={() => setIsProfileOpen(true)}
                >
                  <div className={styles.avatar}>
                    <img
                       src={userData.avatarUrl || "/placeholder.svg"}
                       alt={userData.firstName}
                       className={styles.avatarImage}
                    />
                  </div>
                  <span className={styles.profileName}>{userData.firstName}</span>
                </div>

                <AnimatePresence>
                  {isProfileOpen && (
                    <motion.div
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0, y: 10 }}
                      transition={{ duration: 0.2 }}
                      className={styles.dropdownMenu}
                      onMouseLeave={() => setIsProfileOpen(false)}
                    >
                      <div className={styles.userInfo}>
                        <div className={styles.avatar}>
                          <img
                           src={userData.avatarUrl || "/placeholder.svg"}
                           alt={userData.firstName}
                            className={styles.avatarImage}
                          />
                        </div>
                        <div className={styles.userDetails}>
                          <p className={styles.userName}>{userData.firstName}</p>
                          <p className={styles.userEmail}>{userData.email}</p>
                        </div>
                      </div>
                      <div className={styles.dropdownLinks}>
                        <Link to="/profile" className={styles.dropdownLink}>
                          <User className={styles.dropdownIcon} />
                          Your Dashboard
                        </Link>
                        {/* <Link to="/settings" className={styles.dropdownLink}>
                          <Settings className={styles.dropdownIcon} />
                          Settings
                        </Link>
                        <Link to="/statistics" className={styles.dropdownLink}>
                          <BarChart2 className={styles.dropdownIcon} />
                          Statistics
                        </Link> */}
                      </div>
                      <div className={styles.dropdownFooter}>
                        <button
                          onClick={handleLogout}
                          className={styles.logoutButton}
                        >
                          <LogOut className={styles.dropdownIcon} />
                          Logout
                        </button>
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
            ) : (
              <>
                <Link to="/auth" className={styles.signInButton}>
                  Sign In
                </Link>
                <Link to="/auth" className={styles.getStartedButton}>
                  Get Started
                </Link>
              </>
            )}
          </div>

          {/* Mobile Menu Button */}
          {isAuthenticated ? (
            <div className={styles.mobileMenuHeader}>
              <div className={styles.avatar}>
                <img
                  src={userData.avatarUrl || "/placeholder.svg"}
                  alt={userData.firstName}
                  className={styles.avatarImage}
                />
              </div>
              <button
                className={styles.menuButton}
                onClick={() => setIsOpen(!isOpen)}
                aria-label="Toggle menu"
              >
                <Menu size={24} />
              </button>
            </div>
          ) : (
            <Link to="/auth" className={styles.mobileSignInButton}>
              Sign In
            </Link>
          )}
        </div>
      </div>

      {/* Mobile Menu */}
      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0, x: "100%" }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: "100%" }}
            transition={{ duration: 0.3, ease: "easeInOut" }}
            className={styles.mobileMenuContainer}
            // ref={mobileMenuRef}
          >
            <button
              className={styles.closeButton}
              onClick={() => setIsOpen(false)}
            >
              <X size={24} />
            </button>

            {/* Actual mobile menu */}
            <div className={styles.mobileMenu} ref={mobileMenuRef}>
              <div className={styles.mobileMenuContent}>
                {isAuthenticated && (
                  <div className={styles.mobileUserInfo}>
                    <div className={styles.avatar}>
                      <img
                        src={userData.avatarUrl || "/placeholder.svg"}
                        alt={userData.firstName}
                        className={styles.avatarImage}
                      />
                    </div>
                    <div className={styles.userDetails}>
                      <p className={styles.userName}>{userData.firstName}</p>
                      <p className={styles.userEmail}>{userData.email}</p>
                    </div>
                  </div>
                )}

                <nav className={styles.mobileNavLinks}>
                  {navItems.map((item) => (
                    <Link
                      key={item.name}
                      to={item.path}
                      className={`${styles.mobileNavLink} ${
                        pathname === item.path ? styles.active : ""
                      }`}
                      onClick={() => setIsOpen(false)}
                    >
                      {item.name}
                    </Link>
                  ))}
                </nav>

                {isAuthenticated ? (
                  <div className={styles.mobileUserLinks}>
                    {/* <Link
                      to="/profile"
                      className={styles.mobileUserLink}
                      onClick={() => setIsOpen(false)}
                    >
                      <User className={styles.mobileLinkIcon} />
                      Profile
                    </Link>
                    <Link
                      to="/settings"
                      className={styles.mobileUserLink}
                      onClick={() => setIsOpen(false)}
                    >
                      <Settings className={styles.mobileLinkIcon} />
                      Settings
                    </Link>
                    <Link
                      to="/statistics"
                      className={styles.mobileUserLink}
                      onClick={() => setIsOpen(false)}
                    >
                      <BarChart2 className={styles.mobileLinkIcon} />
                      Statistics
                    </Link> */}
                    <button
                      onClick={handleLogout}
                      className={styles.mobileLogoutButton}
                    >
                      <LogOut className={styles.mobileLinkIcon} />
                      Logout
                    </button>
                  </div>
                ) : (
                  <div className={styles.mobileAuthButtons}>
                    <Link
                      to="/auth"
                      className={styles.mobileSignInButton}
                      onClick={() => setIsOpen(false)}
                    >
                      Sign In
                    </Link>
                    <Link
                      to="/auth"
                      className={styles.mobileGetStartedButton}
                      onClick={() => setIsOpen(false)}
                    >
                      Get Started
                    </Link>
                  </div>
                )}
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </header>
  );
}

export default Navbar;
