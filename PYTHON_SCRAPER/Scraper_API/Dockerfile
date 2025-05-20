FROM python:3.11-slim

WORKDIR /app

# Install system dependencies for OpenCV and Playwright
RUN apt-get update && apt-get install -y \
    build-essential \
    wget \
    curl \
    gnupg \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements first for better layer caching
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Install Playwright browsers
RUN playwright install --with-deps chromium

# Copy application code
COPY . .

# Expose the FastAPI port
EXPOSE 8000


# Command to run the application
CMD ["uvicorn", "src.main:app", "--host", "0.0.0.0", "--port", "8000", "--workers", "4", "--proxy-headers"]