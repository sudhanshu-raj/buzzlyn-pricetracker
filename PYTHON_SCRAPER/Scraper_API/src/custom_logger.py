import logging
import sys


def get_logger(name: str, log_file: str = "application.log", log_level=logging.INFO) -> logging.Logger:
    """
    Configures and returns a logger with console and file handlers.

    Args:
        name (str): The name of the logger.
        log_file (str): Path to the log file. Defaults to "application.log".
        log_level (int): Logging level (e.g., logging.INFO). Defaults to logging.INFO.

    Returns:
        logging.Logger: Configured logger.
    """
    # Clear existing handlers for root logger (to prevent interference)
    logging.getLogger().handlers.clear()

    # Create a custom logger
    logger = logging.getLogger(name)
    logger.setLevel(log_level)

    # Ensure no duplicate handlers
    if not logger.hasHandlers():
        # Create console handler
        console_handler = logging.StreamHandler(sys.stdout)
        console_handler.setLevel(log_level)

        # Create file handler
        file_handler = logging.FileHandler(
            log_file, mode="a", encoding="utf-8")
        file_handler.setLevel(log_level)

        # Create formatter
        formatter = logging.Formatter(
            '%(asctime)s > %(levelname)s > %(name)s > %(message)s'
        )

        # Add formatter to handlers
        console_handler.setFormatter(formatter)
        file_handler.setFormatter(formatter)

        # Add handlers to logger
        logger.addHandler(console_handler)
        logger.addHandler(file_handler)

    return logger
