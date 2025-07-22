from fastapi import FastAPI, HTTPException, Depends, Security
from fastapi.security import APIKeyHeader
from pydantic import BaseModel
from src.scraper import Scraper  # Import your Scraper class
import asyncio
# Add at the top of your file
import sys
import io
from src.custom_logger import get_logger
import os
import src.helper_functions as HF
import src.appConstants as constants
from dotenv import load_dotenv
# At the top of your file
from fastapi.middleware.cors import CORSMiddleware


load_dotenv()

logger = get_logger(__name__, log_file="api_call.log")
logger.info("This is an info message from from FastAPI calls class")

if sys.platform == "win32":
    asyncio.set_event_loop_policy(asyncio.WindowsProactorEventLoopPolicy())
    # Set UTF-8 encoding for Windows
    sys.stdout = io.TextIOWrapper(
        sys.stdout.buffer, encoding='utf-8', errors='replace')
    sys.stderr = io.TextIOWrapper(
        sys.stderr.buffer, encoding='utf-8', errors='replace')

# Add API key security setup
API_KEY_NAME = "Scraper-API"
api_key_header = APIKeyHeader(name=API_KEY_NAME, auto_error=False)


async def get_api_key(api_key: str = Security(api_key_header)):
    # Get valid API key from environment
    valid_api_key = os.getenv("SCRAPER_API_KEY")  # Set this in .env file

    if not api_key:
        raise HTTPException(
            status_code=401,
            detail="API key required"
        )

    if api_key != valid_api_key:
        raise HTTPException(
            status_code=401,
            detail="Invalid API key"
        )

    return api_key

app = FastAPI(
    title="Product Scraper API",
    description="API for scraping product data from e-commerce websites",
    version="1.0.0"
)

# After app = FastAPI(...)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Restrict this in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class ScrapeRequest(BaseModel):
    url: str


class Scraper_DeliveryDetails(BaseModel):
    url: str
    pincode: str

@app.get("/health", tags=["Health"])
async def health_check():
    return {"status": "healthy"}

@app.post("/scrape", tags=["Scraping"], dependencies=[Depends(get_api_key)])
async def scrape_product(request: ScrapeRequest):
    logger.info(f"Normal scrape api is invoked of url :: {request.url}")
    """
    Scrape product data from provided URL

    - **url**: Valid product URL from supported e-commerce sites
    """
    try:
        # Validate URL format
        if not request.url.startswith(('http://', 'https://')):
            raise HTTPException(status_code=400, detail="Invalid URL format")

        scraper = Scraper(request.url)
        result = await asyncio.wait_for(scraper.scrape(), timeout=120)

        if not result:
            raise HTTPException(
                status_code=404, detail="Product data not found")

        return {
            "status": "success",
            "data": result,
            "error": None
        }

    except asyncio.TimeoutError:
        logger.exception("Scraping timeout occurred")  # Logs full traceback
        raise HTTPException(status_code=504, detail="Scraping timeout")
    except Exception as e:
        logger.exception("Unexpected error occurred: %s",
                         str(e))  # Logs full traceback
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/scrape_price", tags=['Scraping Price'], dependencies=[Depends(get_api_key)])
async def scrape_product_price(request: ScrapeRequest):
    logger.info(f"Scraping Price api is invoked of url :: {request.url}")
    try:
        # Validate URL format
        if not request.url.startswith(('http://', 'https://')):
            raise HTTPException(status_code=400, detail="Invalid URL format")

        scraper = Scraper(request.url)
        # result = await asyncio.wait_for(scraper.scrape(), timeout=30)
        result = await asyncio.wait_for(scraper.extract_otherbrands_from_image(), timeout=180)

        if not result:
            raise HTTPException(
                status_code=404, detail="Product data not found")

        return {
            "status": "success",
            "data": result,
            "error": None
        }

    except asyncio.TimeoutError:
        logger.exception("Scraping timeout occurred")  # Logs full traceback
        raise HTTPException(status_code=504, detail="Scraping timeout")
    except Exception as e:
        logger.exception("Unexpected error occurred: %s",
                         str(e))  # Logs full traceback
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/fetch_pincodeTracking", tags=['Pincode Tracker'], dependencies=[Depends(get_api_key)])
async def scrape_product_price(request: Scraper_DeliveryDetails):
    logger.info(f"Scraping Price api is invoked of url :: {request.url}")
    try:
        # Validate URL format
        if not request.url.startswith(('http://', 'https://')):
            raise HTTPException(status_code=400, detail="Invalid URL format")

        brandName = HF.extract_website_name(request.url)
        if brandName not in constants.PINCODE_TRACKER_BRANDS:
            raise HTTPException(status_code=400, detail="Unsupported website")
        
        scraper = Scraper(request.url, request.pincode)
        result = await scraper.get_delivery_date()

        if not result:
            raise HTTPException(
                status_code=404, detail="Product data not found")

        return {
            "status": "success",
            "data": result,
        }

    except asyncio.TimeoutError:
        logger.exception("Scraping timeout occurred")  # Logs full traceback
        raise HTTPException(status_code=504, detail="Scraping timeout")
    except Exception as e:
        logger.exception("Unexpected error occurred: %s",
                         str(e))  # Logs full traceback
        raise HTTPException(status_code=500, detail=str(e))




if __name__ == "__main__":
    print("Starting FastAPI server...")
    import uvicorn
    uvicorn.run(
    "src.main:app", 
    host="0.0.0.0", 
    port=8000,
    workers=4,
    proxy_headers=True,
    access_log=True
)
