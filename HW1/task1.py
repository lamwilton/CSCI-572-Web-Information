from bs4 import BeautifulSoup
import time
import requests
from random import randint
from html.parser import HTMLParser
import json

USER_AGENT = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36'}


class SearchEngine:
    @staticmethod
    def search(query, sleep=True):
        if sleep:  # Prevents loading too many pages too soon
            time.sleep(randint(10, 100))
        temp_url = '+'.join(query.split())  # for adding + between words for the query
        url = 'https://www.duckduckgo.com/html/?q=' + temp_url
        soup = BeautifulSoup(requests.get(url, headers=USER_AGENT).text, "html.parser")
        new_results = SearchEngine.scrape_search_result(soup)
        return new_results

    @staticmethod
    def scrape_search_result(soup):
        raw_results = soup.find_all("a", attrs={'class': 'result__a'})
        results = []
        # implement a check to get only 10 results and also check that URLs must not be duplicated
        for result in raw_results:
            link: str = result.get('href')
            # Lower case and remove slash only during comparison
            #link = link.lower().rstrip("/")
            if link not in results:
                results.append(link)
            if len(results) >= 10:
                return results
        return results


#############Driver code############
#SearchEngine.search("who discovered x-rays in 1885", False)
####################################

if __name__ == '__main__':
    with open("100QueriesSet4.txt", "r") as file:
        queries_set = file.read().splitlines()
    result = {}
    for query in queries_set:
        result[query] = SearchEngine.search(query)
    result_json = json.dumps(result, indent=2, separators=(',', ': '))
    with open("hw1old.json", "w+") as file:
        file.write(result_json)
