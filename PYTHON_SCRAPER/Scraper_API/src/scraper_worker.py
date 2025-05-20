import sys
import json
from src.scraper import Scraper

if __name__ == "__main__":
    product_url = sys.argv[1]  # Get URL from command-line argument
    scraper = Scraper(product_url)

    import asyncio
    # Run async function in standalone script
    result = asyncio.run(scraper.scrape2())

    print(json.dumps(result))  # Output as JSON
