import os
import time
from playwright.sync_api import sync_playwright
from dotenv import load_dotenv
import asyncio
from crawl4ai import AsyncWebCrawler
from bs4 import BeautifulSoup
load_dotenv()

# this also not currently used 
def capture_screenshot_and_pdf(url, screenshot_path="screenshot.png", pdf_path="page.pdf"):

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)  # Run in headless mode
        page = browser.new_page()

        # Open the page
        # Wait until network requests settle
        page.goto(url, wait_until="networkidle", timeout=120000)
        time.sleep(5)

        # Take a screenshot
        page.screenshot(path=screenshot_path, full_page=True)
        print(f"[✔] Screenshot saved at: {screenshot_path}")

        # Save as PDF
        page.pdf(path=pdf_path, format="A4", page_ranges="1")
        print(f"[✔] PDF saved at: {pdf_path}")

        browser.close()

#this is currently used for price scraping
def capture_screenshot_and_pdf2(url, screenshot_path="screenshot.png", pdf_path="page.pdf"):
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)  # Headless mode
        page = browser.new_page()

        # Open the page and wait for network to be idle
        page.goto(url, wait_until="networkidle", timeout=120000)
        time.sleep(5)

        # Remove pop-ups, overlays, and cookie banners
        page.evaluate("""
            const selectors = [
                'div[role="dialog"]',    // Common pop-ups
                'div[aria-modal="true"]', // Modals
                'iframe',                // Embedded pop-ups
                '.popup', '.overlay',     // Common class names
                '#cookie-banner',         // Cookie banners
                '.modal', '.ad',          // Ads
                'body > div[style*="position: fixed"]' // Fixed elements
            ];
            selectors.forEach(selector => {
                document.querySelectorAll(selector).forEach(el => el.remove());
            });
        """)

        # Take a screenshot
        page.screenshot(path=screenshot_path, full_page=True)
        print(f"[✔] Screenshot saved at: {screenshot_path}")

        # Save as PDF
        # page.pdf(path=pdf_path, format="A4", page_ranges="1")
        # print(f"[✔] PDF saved at: {pdf_path}")

        browser.close()

        return screenshot_path
    

#this still not works properly, have to work on it
def capture_screenshot_with_crawl4ai(url, screenshot_path="screenshot.png"):
    """Capture screenshots using crawl4ai which has better bot detection bypass"""
    
    async def _capture_screenshot():
        async with AsyncWebCrawler() as crawler:
            result = await crawler.arun(
                url=url,
                bypass_cache=True,
                page_timeout=120000,
                delay_before_return_html=5,  # Give page time to fully load
                screenshot_full_page=True,
                screenshot_path=screenshot_path
            )
            
            if result.success :
                print(f"[OK] Screenshot saved at: {screenshot_path}")
                return screenshot_path
            else:
                error_msg = result.error if hasattr(result, 'error') else "Unknown error"
                print(f"[ERROR] Failed to capture screenshot: {error_msg}")
                return None
    
    # Run the async function in a synchronous context
    return asyncio.run(_capture_screenshot())


# this using playwright m which not working for some sites
def extract_clean_html_old(url):
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page()

        # Add encoding specification
        page.set_extra_http_headers({
            'Accept-Charset': 'utf-8',
            'Content-Type': 'text/html; charset=utf-8'
        })

        page.goto(url, wait_until="domcontentloaded", timeout=120000)

        # Replace special characters in logging
        print("[OK] HTML Content extracted")  # Removed checkmark symbol

        clean_html = page.content()
        with open("demo.html", "w", encoding='UTF-8') as f:
            f.write(clean_html)
        browser.close()
        # Force UTF-8
        return clean_html.encode('utf-8', 'replace').decode('utf-8')


def extract_clean_html(url):
    """Extract clean HTML using crawl4ai instead of playwright"""
    
    async def _extract_with_crawler():
        async with AsyncWebCrawler() as crawler:
            result = await crawler.arun(
                url=url,
                bypass_cache=True,
                page_timeout=120000,
                delay_before_return_html=2,
            )
            
            if result.success:
                print("[OK] HTML Content extracted")
                clean_html = result.html
                with open("demo.html", "w", encoding='UTF-8') as f:
                    f.write(clean_html)
                # Force UTF-8
                return clean_html.encode('utf-8', 'replace').decode('utf-8')
            else:
                print(f"[ERROR] Failed to extract HTML: {result.error}")
                return None
    
    # Run the async function in a synchronous context to maintain compatibility
    return asyncio.run(_extract_with_crawler())
