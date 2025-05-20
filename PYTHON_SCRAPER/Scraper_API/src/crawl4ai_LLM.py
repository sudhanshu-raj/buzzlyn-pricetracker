import os
import asyncio
import json
from pydantic import BaseModel, Field
from typing import List
from crawl4ai import AsyncWebCrawler, BrowserConfig, CrawlerRunConfig, CacheMode
from crawl4ai.extraction_strategy import LLMExtractionStrategy
import time
from dotenv import load_dotenv
load_dotenv()
class Product(BaseModel):
    name: str=Field(description="Name or Title of product")
    price: str=Field(description="Price of product")
    mrp:str =Field(description="Mrp of product if possible")
    ratings:str =Field(description="ratings of that product")
    reviews:str =Field(description="reviews on that product")
   # specifications:dict =Field(description="Product specifications in dictionary format if possible")
    imageUrl:str =Field(description="Real image URL of product image")


    

async def main(url):
    # 1. Define the LLM extraction strategy
    llm_strategy = LLMExtractionStrategy(
        provider= "gemini/gemini-2.0-flash",            # e.g. "ollama/llama2"
       api_token=os.environ.get("GEMINI_API_KEY"),
    #    provider="ollama/llama3:latest",  # Changed to local Ollama model
    # api_token=None,  # No API token needed for local Ollama
    # base_url="http://localhost:11434",  
        schema=Product.model_json_schema(),            # Or use model_json_schema()
        extraction_type="schema",
        instruction="given the html content product page, extract all the product details, details need to extract is price, mrp, title, reviews,ratings, and image url  all data in json",
         chunk_token_threshold=2000,  # Increased chunk size to reduce requests
    overlap_rate=0.2,  # Add some overlap for better context
    apply_chunking=True,
    input_format="markdown",
    extra_args={
        "temperature": 0.0,
        "max_tokens": 800,
        "request_timeout": 30,  # Add timeout
        "num_retries": 3,  # Add automatic retries
    }
    
    )
    


    # 2. Build the crawler config
    crawl_config = CrawlerRunConfig(
        extraction_strategy=llm_strategy,
        cache_mode=CacheMode.BYPASS,
        remove_overlay_elements=True,
        exclude_external_links=True,
        wait_until="networkidle",
        page_timeout=90000
    )

    # 3. Create a browser config if needed
    proxy_config = {
    "server": os.environ.get("ZENROWS_SERVER"),
    "username": os.environ.get("ZENROWS_USERNAME"),
    "password": os.environ.get("ZENROWS_PASSWORD")
}   
    browser_cfg = BrowserConfig(proxy_config=proxy_config)
    start_time=time.time()
    async with AsyncWebCrawler() as crawler: #config=browser_cfg
        # 4. Let's say we want to crawl a single page
        result = await crawler.arun(
            url=url,
            config=crawl_config
        )

        if result.success:
            # 5. The extracted content is presumably JSON
            data = json.loads(result.extracted_content)
            print("Extracted items:", data)

            # 6. Show usage stats
            llm_strategy.show_usage()  # prints token usage
        else:
            print("Error:", result.error_message)
        end_time=time.time()
        print(f"Finished in {end_time-start_time}")

if __name__ == "__main__":
    URL="https://zozila.com/buy/microsoft-xbox-wireless-controller-astral-purple-new/"
    asyncio.run(main(URL))