import asyncio
import re
from src.custom_logger import get_logger
import sys
import os
import src.helper_functions as HF
from crawl4ai.async_configs import BrowserConfig
from crawl4ai import AsyncWebCrawler
from time import sleep
from bs4 import BeautifulSoup
import src.playwright_demo as PD
import src.gemini_scraper as GS
import json
import src.image_utils as image_utils
from playwright.async_api import async_playwright
from src.helper_functions import extract_website_name

logger = get_logger(__name__, log_file="script1.log")
logger.info("This is an info message from from scraper class")


class Scraper:
    def __init__(self, product_url: str, pincode: str = None):
        self.product_url = product_url
        self.pincode = pincode
        logger.info("Initiated crawling for: %s", self.product_url)

    async def buy_available_option_amazon(self, url):
        try:
            proxy_config = {
            "server": os.environ.get("WEBSHARE_SERVER"),
            "username": os.environ.get("WEBSHARE_USERNAME"),
            "password": os.environ.get("WEBSHARE_PASSWORD")
            }
            browser_config = BrowserConfig(proxy_config=proxy_config,extra_args=["--no-sandbox"])
            async with async_playwright() as p:
                browser = await p.chromium.launch(proxy=proxy_config,headless=True, args=["--no-sandbox"])
                page = await browser.new_page()

                # Add encoding specification
                await page.set_extra_http_headers({
                    'Accept-Charset': 'utf-8',
                    'Content-Type': 'text/html; charset=utf-8'
                })

                await page.goto(url, wait_until="domcontentloaded", timeout=120000)
                await page.wait_for_selector("div#aod-offer", timeout=10000)

                clean_html = await page.content()
                await browser.close()
                clean_html = clean_html.encode(
                    'utf-8', 'replace').decode('utf-8')
            if clean_html:
                soup = BeautifulSoup(clean_html, 'html.parser')
                price_details_section = soup.find("div", id="aod-offer-list")

                if not price_details_section:
                    print("No available price options found.")
                    return

                all_available_price_options = price_details_section.find_all(
                    "div", id="aod-offer")

                best_price_details = {}  # Store best price option

                for each_price_option in all_available_price_options:
                    price_details = {}

                    # Find the delivery section
                    other_details_section = each_price_option.find(
                        "div", class_="a-fixed-left-grid-col a-col-right")
                    description_element = other_details_section.find(
                        "div", id="mir-layout-DELIVERY_BLOCK-slot-PRIMARY_DELIVERY_MESSAGE_LARGE") if other_details_section else None

                    # Check stock availability
                    error_span = description_element.find(
                        "span", class_="a-color-error") if description_element else None
                    if error_span:
                        print("❌ This seller's product is unavailable.")
                        price_details["stock"] = "Unavailable"
                        continue
                    else:
                        print("✅ This seller's product is available.")
                        price_details["stock"] = "Available"

                    # Extract Price
                    price_section = each_price_option.find(
                        "div", id="aod-offer-price")
                    price_section = price_section.find(
                        "div", class_="a-fixed-left-grid-col a-col-left") if price_section else None
                    price_element = price_section.find(
                        "span", class_="a-price aok-align-center centralizedApexPricePriceToPayMargin") if price_section else None
                    only_price_element = price_element.find(
                        "span", class_="a-offscreen") if price_element else None
                    price_str = only_price_element.get_text(
                        strip=True) if only_price_element else None
                    price = int(price_str.replace("₹", "").replace(
                        ",", "").split(".")[0]) if price_str else None

                    print("💰 Price:", price)

                    # Extract MRP
                    mrp_element = price_section.find(
                        "div", class_="a-section a-spacing-small aok-align-center centralizedApexBasisPriceCSS") if price_section else None
                    only_mrp_element = mrp_element.find(
                        "span", class_="a-offscreen") if mrp_element else None
                    mrp_str = only_mrp_element.get_text(
                        strip=True) if only_mrp_element else None
                    mrp = int(mrp_str.replace("₹", "").replace(
                        ",", "").split(".")[0]) if mrp_str else None

                    print("🏷️ MRP:", mrp)

                    price_details["price"] = price
                    price_details["mrp"] = mrp

                    # Store the best (lowest) price
                    if not best_price_details or (price and best_price_details.get("price", float('inf')) > price):
                        best_price_details = price_details

                # Print the best available price
                if best_price_details:
                    print("🔥 Best Available Price:", best_price_details)
                    return best_price_details
                else:
                    print("No available products found.")
            else:
                logger.error("Unable to extract the html from link")

        except Exception as e:
            logger.exception("Error occurred while processing Amazon prices.")

    async def extract_amazon_product(self):
        url = self.product_url
        if sys.platform.startswith('win'):
            sys.stdout.reconfigure(encoding='utf-8')
        product_details = {}
        product_details["brand"] = HF.extract_website_name(url)
        product_details["valid_product_page"] = True
        product_details["physical_product"] = True
        product_details["currency"] = "INR"
        proxy_config = {
            "server": os.environ.get("WEBSHARE_SERVER"),
            "username": os.environ.get("WEBSHARE_USERNAME"),
            "password": os.environ.get("WEBSHARE_PASSWORD")
        }
        browser_config = BrowserConfig(proxy_config=proxy_config,extra_args=["--no-sandbox"])
        try:
            async with AsyncWebCrawler(browser_config=browser_config) as crawler:  # config=browser_config
                result = await crawler.arun(url=url,
                                            bypass_cache=True,
                                            page_timeout=10000,
                                            delay_before_return_html=2,
                                            )

                if result.success:
                    soup = BeautifulSoup(result.html, 'html.parser')
                    title = soup.find('span', id='productTitle')
                    if title:
                        product_details['title'] = title.get_text().strip()
                    else:
                        product_details['title'] = "N/A"
                        logger.error(
                            f'Product title using #productTitle not found on {url}')
                        return product_details

                    price_details = soup.findAll(
                        'div', id='corePriceDisplay_desktop_feature_div')
                    # For books if price_details above not worked then this should work
                    price_detailsI = soup.select_one(
                        '#adbl_bb_price_1 > span > span:nth-child(2) > span.a-price-whole')
                    price_sectionII = soup.find("div", id="corePrice_desktop")
                    priceSectionIII = soup.select_one(
                        "span.a-price.a-text-price.a-size-medium")
                    if price_details:
                        exctual_price = price_details[0].find(
                            'span', class_='a-price aok-align-center reinventPricePriceToPayMargin priceToPay')
                        mrp = price_details[0].find(
                            'span', class_='a-size-small a-color-secondary aok-align-center basisPrice')
                        discount = price_details[0].find(
                            'span', class_='a-size-large a-color-price savingPriceOverride aok-align-center reinventPriceSavingsPercentageMargin savingsPercentage')
                        if exctual_price:
                            exctual_price = exctual_price.find(
                                'span', class_='a-price-whole')
                            product_details['price'] = int(
                                exctual_price.get_text().strip().replace(",", "").replace(".", ""))
                        else:
                            product_details['price'] = "N/A"
                            logger.warning(
                                f"Product price not found using exctual_price var on {url}")
                        if mrp:
                            mrp = mrp.find('span', class_='a-offscreen')
                            product_details['mrp'] = int(
                                mrp.get_text().strip().replace(",", "")[1:])
                        else:
                            product_details['mrp'] = "N/A"
                            logger.warning(
                                f"Product mrp not found using mrp var on {url}")
                        if discount:
                            product_details['discount'] = discount.get_text(
                            ).strip()
                        else:
                            product_details['discount'] = "N/A"
                            logger.warning(
                                f"Product discount not found using discount var on {url}")
                    elif price_detailsI:
                        price = price_detailsI.get_text().strip()
                        product_details['price'] = price
                    elif price_sectionII:
                        mrp_element = price_sectionII.select_one(
                            "td.a-span12.a-color-secondary.a-size-base"
                        )
                        mrp_element = mrp_element.find(
                            "span", class_="a-offscreen") if mrp_element else None
                        mrp = mrp_element.get_text(
                            strip=True) if mrp_element else "N/A"

                        # Extract Price
                        price_element = price_sectionII.select_one(
                            "span.a-price.a-text-price.a-size-medium.apexPriceToPay"
                        )
                        price_element = price_element.find(
                            "span", class_="a-offscreen") if price_element else None
                        price = price_element.get_text(
                            strip=True) if price_element else "N/A"

                        product_details['price'] = (
                            int(price.replace(",", "").split("₹")[1])
                            if price != "N/A" and "₹" in price else price
                        )
                        product_details['mrp'] = (int(
                            mrp.replace(",", "").split("₹")[
                                1]) if mrp != "N/A" and "₹" in mrp else mrp
                        )
                    elif priceSectionIII:
                        print("inside fourth price details")
                        price = priceSectionIII.find(
                            "span", class_="a-offscreen")
                        price = int(float(price.get_text(
                            strip=True).replace(",", "").split("₹")[1]))
                        price_details['price'] = price
                    else:
                        product_details['price'] = "N/A"
                        product_details['mrp'] = "N/A"
                        product_details['discount'] = "N/A"
                        logger.warning(
                            f"Price Details section not using price_details and price_detailsI found on {url} ")

                    # availabilityElement = soup.find('div', id="availability")
                    # if availabilityElement:
                    #     product_details["stock"] = availabilityElement.get_text(
                    #         strip=True)

                    buy_now_btn = soup.find("input", id="buy-now-button")
                    if buy_now_btn:
                        product_details["stock"] = "in_stock"
                    else:
                        unavailability = soup.find(
                            "div", class_="a-section a-spacing-small a-text-center")
                        if unavailability:
                            product_details['stock'] = "out_stock"
                        else:
                            product_details["stock"] = "out_stock"

                    buy_available_options = soup.find(
                        "span", id="buybox-see-all-buying-choices")
                    # Here it means we have to check the other sellers by clicking on See All Buying Options
                    if product_details['price'] == "N/A" and buy_available_options:
                        options_link_tag = buy_available_options.find("a")
                        link = options_link_tag['href'] if options_link_tag else None
                        website_name = extract_website_name(url)
                        link = "https://www."+website_name+link
                        available_buy_option_result = await self.buy_available_option_amazon(link)
                        if available_buy_option_result:
                            print(available_buy_option_result)
                            price = available_buy_option_result.get(
                                "price", "N/A")
                            mrp = available_buy_option_result.get("mrp", "N/A")
                            product_details['price'] = price
                            product_details['mrp'] = mrp
                            product_details['stock'] = "in_stock"

                    specs = {}
                    spec_table = soup.find(
                        'table', class_='a-normal a-spacing-micro')
                    if spec_table:
                        specs_rows = spec_table.findAll('tr')
                        for row in specs_rows:
                            key_elem = row.find('td', class_='a-span3')
                            value_elem = row.find('td', class_='a-span9')

                            if key_elem and value_elem:
                                key = key_elem.get_text(strip=True)
                                value = value_elem.get_text(strip=True)
                                specs[key] = value
                                specs[key] = value
                            else:
                                logger.warning(
                                    f"Product specifications micro key and value var not found on {url}")
                        product_details['specs'] = specs
                    else:
                        logger.warning(
                            f"Product specifications micro not found using .a-normal a-spacing-micro on {url}")
                        product_details["specs"] = "N/A"

                    ratings = soup.select_one(
                        '#acrPopover > span.a-declarative > a > span')
                    reviews = soup.find('span', id='acrCustomerReviewText')
                    if ratings:
                        product_details['ratings'] = ratings.get_text(
                            strip=True)
                    else:
                        product_details['ratings'] = "N/A"
                        logger.warning(
                            f"Rating start of selector :#acrPopover > span.a-declarative > a > span not found on {url}")

                    if reviews:
                        product_details['reviews'] = reviews.get_text(
                            strip=True).replace('ratings', '').replace(",", "")
                    else:
                        product_details['reviews'] = "N/A"
                        logger.warning(
                            f'Rating Count of id :acrCustomerReviewText not found on {url}')

                    logger.info(f"extracting product image")
                    image = soup.find('img', id='landingImage')
                    if image:
                        image_url = image['src']
                        product_details['image'] = image_url
                    else:
                        logger.warning(
                            f"Image not found using #landingImage on {url}")
                        product_details['image'] = "N/A"

                    details_table = soup.find(
                        'table', {'id': 'productDetails_techSpec_section_1'})
                    extracted_technical_data = {}
                    if details_table:
                        target_labels = [
                            "Model Name", "Model", "Item model number", "Style Name", "Model Number"]
                        if details_table:
                            rows = details_table.find_all('tr')
                        for row in rows:
                            header = row.find('th')
                            if header:
                                label = header.text.strip()
                                if label in target_labels:
                                    value_element = row.find('td')
                                    value = value_element.text.strip().replace(
                                        '\u200e', '')if value_element else None
                                    extracted_technical_data[label] = value
                    product_details["technical_details"] = extracted_technical_data

                    # 1st method: From Product Details Table
                    asin_element = soup.find_all('tr')
                    for tr in asin_element:
                        if 'ASIN' in tr.text:
                            asin_value = tr.find('td')
                            if asin_value:
                                product_details["ASIN"] = asin_value.text.strip()
                            break
                    else:
                        # 2nd method: From the bullet points list
                       for li in soup.find_all('li'):
                            text = li.get_text(separator=" ", strip=True)  # Gets full clean text with spacing
                            if 'ASIN' in text:
                                # Try to extract exactly 10-character ASIN using regex
                                import re
                                match = re.search(r'\b([A-Z0-9]{10})\b', text)
                                if match:
                                    asin_text = match.group(1)
                                    print("ASIN found:", asin_text)
                                    product_details["ASIN"] = asin_text
                                    break

                else:
                    logger.warning(
                        f"Error while getting product details: {result.error} on url {url}")
            return product_details
        except Exception as e:
            logger.error(
                f"Error at try/catch on product details: {str(e)} on url {url}")
            return product_details

    async def get_delivery_date_amazon(self):
        url = self.product_url
        pincode = self.pincode
        product_details = {}
        try:
            proxy_config = {
            "server": os.environ.get("WEBSHARE_SERVER"),
            "username": os.environ.get("WEBSHARE_USERNAME"),
            "password": os.environ.get("WEBSHARE_PASSWORD")
        }
            async with async_playwright() as p:
                browser = await p.chromium.launch(proxy=proxy_config,headless=True, slow_mo=100, args=["--no-sandbox"])
                context = await browser.new_context(
                    viewport={"width": 1920, "height": 1080},
                    user_agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
                )
                page = await context.new_page()
                await page.goto(url)
                logger.info(f"crawling: {url}")

                # Locate button
                checkBtn = page.locator('#contextualIngressPtLabel')
                try:
                    await checkBtn.wait_for(state="visible", timeout=10000)
                    await checkBtn.hover()
                    await page.wait_for_timeout(500)
                    await checkBtn.click()
                except Exception as e:
                    logger.warning(f"Button not found on {url}: {e}")
                    return product_details

                # Enter Pincode
                inputPincode = page.locator('#GLUXZipUpdateInput')
                try:
                    await inputPincode.wait_for(state="visible", timeout=5000)
                    await inputPincode.fill(pincode)
                except Exception as e:
                    logger.warning(f"Pincode input not found on {url}: {e}")
                    return product_details

                # Click Submit
                submitBtn = page.locator(
                    "input.a-button-input[aria-labelledby='GLUXZipUpdate-announce']")
                try:
                    await submitBtn.wait_for(state="visible", timeout=5000)
                    await page.wait_for_timeout(100)
                    await submitBtn.click()
                except Exception as e:
                    logger.warning(f"Submit button not found on {url}: {e}")
                    return product_details

                # Wait for Updated Delivery Info
                updatedDat = page.locator(
                    "div#contextualIngressPtLabel_deliveryShortLine")
                try:
                    while True:
                        content = await updatedDat.text_content()
                        if content and pincode in content:
                            break
                        await page.wait_for_timeout(500)

                    if await updatedDat.is_visible():
                        product_details['delivery_location'] = (await updatedDat.text_content()).strip()

                        html_content = await page.content()
                        soup = BeautifulSoup(html_content, 'html.parser')

                        delivery_block = soup.find(
                            'div', id='mir-layout-DELIVERY_BLOCK-slot-PRIMARY_DELIVERY_MESSAGE_LARGE')
                        if delivery_block:
                            delivery_fee = delivery_block.find('span').get(
                                'data-csa-c-delivery-price', 'N/A')
                            delivery_date = delivery_block.find(
                                'span', class_='a-text-bold').get_text(strip=True)

                            product_details["delivery_fee"] = delivery_fee
                            product_details["delivery_date"] = delivery_date
                        else:
                            logger.warning(
                                f"Delivery block not found on {url}")
                            product_details["delivery_fee"] = "N/A"
                            product_details["delivery_date"] = "N/A"
                    else:
                        logger.warning(
                            f"Updated delivery info not found on {url}")
                        product_details['delivery_location'] = "N/A"

                except Exception as e:
                    logger.warning(
                        f"Error retrieving updated delivery info on {url}: {e}")
                    product_details['delivery_location'] = "N/A"

                await browser.close()
                return product_details

        except Exception:
            logger.exception("Error occurred")

    async def extract_flipkart_product(self):
        url = self.product_url
        product_details = {}
        try:
            if sys.platform.startswith('win'):
                sys.stdout.reconfigure(encoding='utf-8')

            logger.info("Starting web crawling...")
            proxy_config = {
            "server": os.environ.get("WEBSHARE_SERVER"),
            "username": os.environ.get("WEBSHARE_USERNAME"),
            "password": os.environ.get("WEBSHARE_PASSWORD")
            }
            browser_config = BrowserConfig(proxy_config=proxy_config,extra_args=["--no-sandbox"])
            async with AsyncWebCrawler(browser_config=browser_config) as crawler:
                result = await crawler.arun(
                    url=url,
                    page_timeout=10000,
                    delay_before_return_html=2,
                    bypass_cache=True,
                )

            if not result.success:
                logger.warning(f"Unable to crawl {url}")
                return product_details

                # raise HTTPException(
                #    status_code=404, detail="Failed to retrieve page content")
            soup = BeautifulSoup(result.html, 'html.parser')

            product_details = {}
            product_details["brand"] = HF.extract_website_name(url)
            product_details["valid_product_page"] = True
            product_details["physical_product"] = True
            product_details["currency"] = "INR"
            title = soup.find('span', class_='VU-ZEz')
            title_brand = soup.find('span', class_='mEh187')
            if title:
                product_details["title"] = title.get_text(
                    strip=True).replace('\xa0', ' ')
                if title_brand:
                    title_brand = title_brand.get_text(
                        strip=True).replace('\xa0', ' ')
                    product_details['title'] = title_brand + \
                        " "+product_details['title']
            else:
                product_details["title"] = "N/A"
                logger.error(
                    f"Product title not found using .VU-ZEz on {url}: ")
                logger.warning(f"Or May be url is not a valid one")
                return product_details

            price = soup.find('div', class_='Nx9bqj CxhGGd yKS4la')
            mrp = soup.find('div', class_='yRaY8j A6+E6v yKS4la')
            discount = soup.select_one('div.UkUFwK.WW8yVX.yKS4la > span')
            if not price:
                price = soup.find('div', class_='Nx9bqj CxhGGd')
                mrp = soup.find('div', class_='yRaY8j A6+E6v')
                discount = soup.select_one('div.UkUFwK.WW8yVX > span')
            if price:
                product_price = price.get_text().strip()[1:]
                product_price = int(product_price.replace(",", ""))
                product_details["price"] = product_price
                if mrp:
                    product_details["mrp"] = int(
                        mrp.get_text().strip()[1:].replace(",", ""))
                else:
                    product_details["mrp"] = "N/A"
                    logger.warning(f"Product mrp not found on {url}")
                if discount:
                    product_details["discount"] = discount.get_text().strip()
                else:
                    product_details["discount"] = "N/A"
                    logger.warning(f"Product discount not found on {url}")
            else:
                product_details["price"] = "N/A"
                product_details["mrp"] = "N/A"
                product_details["discount"] = "N/A"
                logger.warning(f"Product price details not found on {url}")

            buy_now_btn = soup.find(
                "button", class_="QqFHMw vslbG+ _3Yl67G _7Pd1Fp")
            
            # check if product is out of stock for pincode only ?
            check_is_pincode_available=soup.find("div", class_="nyRpc8")
            pincode_out_of_stock_error=False
            if check_is_pincode_available:
                pincode_text = check_is_pincode_available.get_text(strip=True)
                logger.info(f"pincode_text: {pincode_text}")
                if "out of stock" in pincode_text:
                   pincode_out_of_stock_error = True
    
            if buy_now_btn:
                if "disabled" in buy_now_btn.attrs and not pincode_out_of_stock_error:
                    product_details["stock"] = "out_stock"
                    print("❌ Product is out of stock! 'Buy Now' is disabled.")
                else:
                    product_details["stock"] = "in_stock"
                    print("✅ Product is available! 'Buy Now' is enabled.")
            else:
                product_details["stock"] = "out_stock"
                logger.info(
                    "Buy now button not found, means product is not available")

            # stock = soup.find("div", class_="Z8JjpR")
            # if stock:
            #     product_details["stock"] = stock.get_text(strip=True)

            ratings = soup.find('div', class_='XQDdHH')
            reviews = soup.find('span', class_='Wphh3N')

            if ratings:
                product_details["ratings"] = ratings.get_text().strip()
            else:
                product_details["ratings"] = "N/A"
                logger.warning(
                    f"Rating start class : XQDdHH not found on {url} ")
            if reviews:
                product_details["reviews"] = reviews.get_text().strip()
                product_details["reviews"] = product_details["reviews"].split(' Ratings')[
                    0].replace(",", "")
            else:
                logger.warning(
                    f"Rating count class :Wphh3N not found on {url}")
                product_details["reviews"] = "N/A"

            image = soup.find('img', class_='DByuf4 IZexXJ jLEJ7H')
            if not image:
                image = soup.find('img', class_='_53J4C- utBuJY')

            if image:
                image_url = image['src']
                product_details["image"] = image_url
                print(f"image_url: {image_url}")
                # response = requests.get(image_url)
                # image_name = f"{product_details['title'][:50]}.png"
                # image_name = sanitize_filename(image_name)
                # image_path = os.path.join(os.getcwd(), image_name)
                # with open(image_path, 'wb') as f:
                #     f.write(response.content)
            else:
                logger.warning(
                    f"Product image using : .DByuf4 IZexXJ jLEJ7H or ._53J4C- utBuJY not found on {url}")

        # extracting product specs from page
            general_sections = soup.find_all(
                'div', class_='GNDEQ-', string=None)
            target_labels = ["Model Name", "Model",
                             "Item model number", "Style Name", "Model Number"]
            if general_sections:
                for section in general_sections:
                    header = section.find('div', class_='_4BJ2V+')
                    if header:
                        header = header.get_text(strip=True)
                        print(f"header value: {header}")
                        if header == "General":
                            # Filter for the specific section
                            print("header is General")
                            # Locate the table
                            table = section.find('table', class_='_0ZhAN9')
                            if table:
                                rows = table.find_all(
                                    'tr', class_='WJdYP6 row')  # Extract rows
                                if rows:
                                    key_value_pairs = {}
                                    extracted_technical_data = {}
                                    for row in rows:
                                        key_cell = row.find(
                                            'td', class_='+fFi1w col col-3-12')  # Locate key
                                        value_cell = row.find(
                                            'td', class_='Izz52n col col-9-12')  # Locate value

                                        if key_cell and value_cell:
                                            key = key_cell.get_text(strip=True)
                                            value = value_cell.get_text(
                                                strip=True)
                                            key_value_pairs[key] = value
                                            if key in target_labels:
                                                extracted_technical_data[key] = value
                                        else:
                                            logger.warning(
                                                f"Product Specifiaction table key and value using .+fFi1w col col-3-12 and .Izz52n col col-9-12 not found on {url} ")

                                    product_details[f"specs"] = key_value_pairs
                                    product_details["technical_details"] = extracted_technical_data
                                    break
                                else:
                                    logger.warning(
                                        f"Product General Specifiaction rows using .WJdYP6 row not found on {url}")
                                    product_details["specs"] = "N/A"
                            else:
                                logger.warning(
                                    f"Product General Specifications table using ._0ZhAN9 not found in {url}")
                                product_details["specs"] = "N/A"
                        else:
                            logger.info(
                                "General box is not there in header,extractring other details")
                            table = section.find('table', class_='_0ZhAN9')
                            if table:
                                rows = table.find_all(
                                    'tr', class_='WJdYP6 row')  # Extract rows
                                if rows:
                                    key_value_pairs = {}
                                    extracted_technical_data = {}
                                    for row in rows:
                                        key_cell = row.find(
                                            'td', class_='+fFi1w col col-3-12')  # Locate key
                                        value_cell = row.find(
                                            'td', class_='Izz52n col col-9-12')  # Locate value

                                        if key_cell and value_cell:
                                            key = key_cell.get_text(strip=True)
                                            value = value_cell.get_text(
                                                strip=True)
                                            key_value_pairs[key] = value
                                            if key in target_labels:
                                                extracted_technical_data[key] = value
                                    else:
                                        logger.warning(
                                            f"Product Specifiaction table key and value using .+fFi1w col col-3-12 and .Izz52n col col-9-12 not found on {url} ")
                                    product_details[f"specs:{header}"] = key_value_pairs
                                    product_details["technical_details"] = extracted_technical_data
                                else:
                                    logger.warning(
                                        f"Product  Specifiaction rows using .WJdYP6 row not found on {url}")
                                    # product_details["specs"]="N/A"
                            else:
                                logger.warning(
                                    f"Product Specifications table using ._0ZhAN9 not found in {url}")
                                # product_details["specs"]="N/A"
                    else:
                        logger.warning(
                            f"Product Sepecification Header using : ._4BJ2V not found on {url}")
                        logger.info(f"Extracting the specification available ")
                        table = section.find('table', class_='_0ZhAN9')
                        if table:
                            rows = table.find_all(
                                'tr', class_='WJdYP6 row')  # Extract rows
                            if rows:
                                key_value_pairs = {}
                                extracted_technical_data = {}
                                for row in rows:
                                    key_cell = row.find(
                                        'td', class_='+fFi1w col col-3-12')  # Locate key
                                    value_cell = row.find(
                                        'td', class_='Izz52n col col-9-12')  # Locate value

                                    if key_cell and value_cell:
                                        key = key_cell.get_text(strip=True)
                                        value = value_cell.get_text(strip=True)
                                        key_value_pairs[key] = value
                                        if key in target_labels:
                                            extracted_technical_data[key] = value
                                    else:
                                        logger.warning(
                                            f"Product Specifiaction table key and value using .+fFi1w col col-3-12 and .Izz52n col col-9-12 not found on {url} ")
                                product_details[f"specs"] = key_value_pairs
                                product_details["technical_details"] = extracted_technical_data
                            else:
                                logger.warning(
                                    f"Product  Specifiaction rows using .WJdYP6 row not found on {url}")
                                product_details["specs"] = "N/A"
                        else:
                            logger.warning(
                                f"Product Specifications table using ._0ZhAN9 not found in {url}")
                            product_details["specs"] = "N/A"
            else:
                product_details["specs"] = "N/A"
                logger.warning(
                    f"Product Specification general_sections using : GNDEQ- not found on url{url}")
            logger.info(f"Finised crawling for {url}")
            return product_details

        except Exception as e:
            logger.error(f"An error occurred in crawling: {str(e)} on {url}")
            return product_details

    async def get_delivery_date_flipkart(self):
        url = self.product_url
        pincode = self.pincode
        sys.stdout.reconfigure(encoding='utf-8')
        delivery_details = {}

        try:
            proxy_config = {
            "server": os.environ.get("WEBSHARE_SERVER"),
            "username": os.environ.get("WEBSHARE_USERNAME"),
            "password": os.environ.get("WEBSHARE_PASSWORD")
            }
       
            async with async_playwright() as p:
                browser = await p.chromium.launch(proxy=proxy_config,headless=True, args=["--no-sandbox"])
                page = await browser.new_page()

                # Open the URL
                await page.goto(url)
                logger.info(f"Navigating to the URL {url}")

                # Input the pincode
                input_element = page.locator(".AFOXgu")
                if await input_element.is_visible():
                    await input_element.fill(pincode)
                else:
                    logger.warning(
                        f"Delivery Input element: .AFOXgu not visible on {url}")
                    return 'N/A'

                # Click the check button
                check_btn = page.locator(".i40dM4")
                if await check_btn.is_visible():
                    await check_btn.click()
                else:
                    logger.warning(
                        f"Delivery Check button: .i40dM4 not visible on {url}")
                    return 'N/A'

                # Wait for the page to load and capture its source
                await page.wait_for_timeout(2000)  # Adjust timeout as needed
                page_source = await page.content()
                soup = BeautifulSoup(page_source, "html.parser")

                # Extract delivery date
                delivery_date = soup.find("span", class_="Y8v7Fl")
                if delivery_date:
                    delivery_details["delivery_date"] = delivery_date.text

                    # Check for free delivery
                    is_free = soup.find("span", class_="hcf08j")
                    if is_free:
                        delivery_details["delivery_fees"] = is_free.text
                    else:
                        delivery_rate = soup.find(
                            "span", class_="Xksjzr oeP9rn")
                        if delivery_rate:
                            delivery_details["delivery_fees"] = delivery_rate.text
                        else:
                            logger.warning(
                                f"Delivery rate element not found on {url}")
                            delivery_details["delivery_fees"] = "N/A"
                else:
                    logger.warning(f"Delivery date element not found on {url}")
                    delivery_details["delivery_date"] = "N/A"

                    # Handle pincode errors
                    pincode_error = soup.find("div", class_="nyRpc8")
                    if pincode_error:
                        delivery_details["pin_codeError"] = pincode_error.text
                        logger.error(f"Pincode error: {pincode_error.text}")
                    else:
                        logger.warning(
                            f"Pincode error element not found on {url}")

                await browser.close()
                logger.info("Browser closed.")
                return delivery_details

        except Exception as e:
            logger.exception(f"An error occurred on {url}: {str(e)}")
            return 'N/A'

    async def extract_chroma_product(self):
        url = self.product_url
        product_details = {}
        logger.info(f"Crawling url : {url}")
        if sys.platform.startswith('win'):
            sys.stdout.reconfigure(encoding='utf-8')

        proxy_config = {
            "server": os.environ.get("WEBSHARE_SERVER"),
            "username": os.environ.get("WEBSHARE_USERNAME"),
            "password": os.environ.get("WEBSHARE_PASSWORD")
        }
        browser_config = BrowserConfig(proxy_config=proxy_config,extra_args=["--no-sandbox"])
        try:

            async with AsyncWebCrawler(browser_config=browser_config
            ) as crawler:
                result = await crawler.arun(url=url,
                                            bypass_cache=True,
                                            # page_timeout=20000,
                                            delay_before_return_html=2,
                                            # wait_for="css:.bank-offer-swiper"
                                            )
                if result.success:
                    product_details["brand"] = HF.extract_website_name(url)
                    product_details["valid_product_page"] = True
                    product_details["physical_product"] = True
                    product_details["currency"] = "INR"
                    html_content = result.html
                    soup = BeautifulSoup(html_content, 'html.parser')
                    title = soup.find('h1', class_='pd-title pd-title-normal')
                    if title:
                        product_details["title"] = title.get_text().strip()
                    else:
                        product_details["title"] = "N/A"
                        logger.error(
                            f'Product title using class pd-title pd-title-normal not found')
                        return product_details

                    rating_section = soup.select_one(".cp-rating")
                    if rating_section:
                        # Extract star rating
                        star_rating_elem = rating_section.select_one(
                            "span > span")
                        star_rating = star_rating_elem.get_text(
                            strip=True) if star_rating_elem else None
                        product_details["ratings"] = star_rating

                        review_text_elem = soup.select_one(
                            ".pr-review.review-text")
                        review_text = review_text_elem.get_text(
                            strip=True) if review_text_elem else None
                        match = re.search(
                            r'(\d+)\s*Ratings\s*&\s*(\d+)\s*Reviews', review_text)
                        if match:
                            total_ratings = match.group(1)
                            total_reviews = match.group(2)
                        else:
                            total_ratings = total_reviews = None
                        product_details["reviews"] = total_ratings

                    price_elem = soup.find("span", id="pdp-product-price")
                    price = price_elem.get_text(
                        strip=True) if price_elem else None
                    if price:
                        price = int(price.replace(
                            ",", "").split("₹")[1].split(".")[0])
                        product_details["price"] = price

                    mrp_elem = soup.find("span", id="old-price")
                    mrp_ = mrp_elem.get_text(strip=True) if mrp_elem else None
                    if mrp_:
                        mrp = int(mrp_.replace(",", "").split(
                            "₹")[1].split(".")[0])
                        product_details["mrp"] = mrp

                    buy_now_btn = soup.select("button.buyNowBtn")
                    disabled_buy_now_btn = soup.select("button.disableBuyNow")
                    if buy_now_btn:
                        product_details["stock"] = "in_stock"
                    else:
                        product_details["stock"] = "out_stock"
                    # here \30 means 0 so it means 0prod_img it's kind of css selector thing
                    image_elem = soup.select_one(r"#\30 prod_img")
                    img = image_elem['src'] if image_elem else None
                    product_details["image"] = img

                    bank_offers = []
                    offers_section = soup.find(
                        "div", class_="bank-offer-swiper")
                    if offers_section:
                        print("inside offer section")
                        for offer in offers_section.select(".swiper-slide"):
                            bank_name_elem = offer.select_one(
                                ".bank-name-text")
                            bank_offer_elem = offer.select_one(
                                ".bank-offers-text-pdp-carousel")
                            bank_icon_elem = offer.select_one(
                                ".bank-offer-img-div-carousel img")
                            temp_data = {}
                            if bank_name_elem and bank_offer_elem:
                                bank_name = bank_name_elem.get_text(strip=True)
                                bank_offer = bank_offer_elem.get_text(
                                    strip=True)
                                temp_data["bank_name"] = bank_name
                                temp_data["bank_offer"] = bank_offer
                            if bank_icon_elem:
                                bank_icon = bank_icon_elem["src"]
                                temp_data["bank_icon"] = bank_icon
                            bank_offers.append(temp_data)
                    product_details["offers"] = bank_offers

                    manufacturing_details = {}
                    specs_table = soup.find(
                        "div", id="specification_container")
                    manufacturer_details_sections = specs_table.find_all(
                        'ul', class_='cp-specification-info')
                    for manufacturer_details_section in manufacturer_details_sections:
                        if manufacturer_details_section.find('h3', class_='title', string='Manufacturer Details'):

                            # Find all specification info within the Manufacturer Details section
                            spec_infos = manufacturer_details_section.find_all(
                                'ul', class_='cp-specification-spec-info')

                            for spec_info in spec_infos:
                                title_element = spec_info.find(
                                    'li', class_='cp-specification-spec-title')
                                details_element = spec_info.find(
                                    'li', class_='cp-specification-spec-details')

                                if title_element and details_element:
                                    title = title_element.text.strip()
                                    details = details_element.text.strip()

                                    manufacturing_details[title] = details
                    product_details["technical_details"] = manufacturing_details

        except Exception as e:
            logger.exception("Unexpected error occur")
        finally:
            return product_details

    async def extract_otherbrand_products(self):
        """Proper async handling for sync Playwright+Gemini operations"""
        try:
            # 1. Get clean HTML using Playwright (sync in executor)
            loop = asyncio.get_running_loop()
            clean_html = await loop.run_in_executor(
                None,
                lambda: PD.extract_clean_html(self.product_url)
            )

            # 2. Process HTML with Gemini (sync in executor)
            result = await loop.run_in_executor(
                None,
                lambda: GS.extract_from_html(
                    clean_html)  # Modified GS function
            )
            result["brand"] = HF.extract_website_name(self.product_url)
            return result

        except Exception as e:
            logger.error(f"Other brand extraction failed: {str(e)}")
            return {}

    async def extract_otherbrands_from_image(self):
        brand_name = HF.extract_website_name(self.product_url)
        try:
            if "amazon" in brand_name.lower():
                return await self.extract_amazon_product()
            elif "flipkart" in brand_name.lower():
                return await self.extract_flipkart_product()
            elif "croma" in brand_name.lower():
                return await self.extract_chroma_product()
            else:
                loop = asyncio.get_running_loop()
                image_path = await loop.run_in_executor(
                    None,
                    lambda: PD.capture_screenshot_and_pdf2(self.product_url)
                )

                cropped_img = await loop.run_in_executor(
                    None,
                    lambda: image_utils.crop_image(
                        image_path, "cropped_img.jpg")
                )

                result = await loop.run_in_executor(
                    None,
                    lambda: GS.extract_from_image("cropped_img.jpg")
                )
                if result and not result.get("price"):
                    logger.info(
                        "Seems price is not fetched, trying again by increase the height now to 1600px")
                    cropped_img = await loop.run_in_executor(
                        None,
                        lambda: image_utils.crop_image(
                            image_path, "cropped_img.jpg", width=1276, height=1600)
                    )
                    result = await loop.run_in_executor(
                        None,
                        lambda: GS.extract_from_image("cropped_img.jpg")
                    )

                result["brand"] = HF.extract_website_name(self.product_url)
                return result
        except Exception as e:
            logger.error(
                "Exception occured  ::", str(e))

    async def scrape(self):
        brand_name = HF.extract_website_name(self.product_url)
        try:
            if "amazon" in brand_name.lower():
                return await self.extract_amazon_product()
            elif "flipkart" in brand_name.lower():
                return await self.extract_flipkart_product()
            elif "croma" in brand_name.lower():
                return await self.extract_chroma_product()
            else:
                # Handle sync function in async context
                return await self.extract_otherbrand_products()
        except Exception as e:
            logger.error(f"Scraping failed: {str(e)}")
            return {}
        
    async def get_delivery_date(self):
        brand_name = HF.extract_website_name(self.product_url)
        try:
            if "amazon" in brand_name.lower():
                return await self.get_delivery_date_amazon()
            elif "flipkart" in brand_name.lower():
                return await self.get_delivery_date_flipkart()
            else:
                logger.warning(
                    f"Delivery date not supported for {brand_name}")
                return {}
        except Exception as e:
            logger.error(f"Delivery date retrieval failed: {str(e)}")
            return {}


if __name__ == "__main__":
    url = "https://www.flipkart.com/samsung-galaxy-s23-5g-cream-256-gb/p/itm745d4b532623e?pid=MOBGMFFXURCVYANE&lid=LSTMOBGMFFXURCVYANE4SRNTK&marketplace=FLIPKART&q=s23+5g&store=tyy%2F4io&srno=s_1_2&otracker=AS_QueryStore_OrganicAutoSuggest_2_4_na_na_na&otracker1=AS_QueryStore_OrganicAutoSuggest_2_4_na_na_na&fm=organic&iid=961e2df6-e6d5-4478-81d2-b7a44f32cf9e.MOBGMFFXURCVYANE.SEARCH&ppt=browse&ppn=browse&ssid=e0xucloqv40000001745438495827&qH=bec108eabfca89fd"
    obj = Scraper(url, "846001")
    # result = asyncio.run(obj.scrape())
    result = asyncio.run(obj.get_delivery_date())
    print(result)
