from bs4 import BeautifulSoup
import requests
import time
import json
from random import randint
from html.parser import HTMLParser
from urllib.parse import unquote

USER_AGENT = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36'}

class SearchEngine:

    @staticmethod
    def search(query, sleepTime = 10):
        time.sleep(sleepTime)
        temp_url = '+'.join(query.split())
        url = 'http://www.search.yahoo.com/search?p=' + temp_url + '&n=30'
        output_text = requests.get(url, headers=USER_AGENT).text
        soup = BeautifulSoup(output_text, "html.parser")
        new_results = SearchEngine.scrape_search_result(soup)
        return new_results

    @staticmethod
    def scrape_search_result(soup):
        raw_results = soup.find_all("a", attrs={"class": "ac-algo fz-l ac-21th lh-24"})
        results = set()
        for res in raw_results:
            if len(results) == 10:
                break
            link = res.get('href')
            if "RU=" in link:
                link = link.split("RU=")[1]
                link = link.split("/")[0]
            finalLink = unquote(link)
            results.add(finalLink)
        return list(results)

def removeProtocolFromUrl(url):
    finalUrl = url.replace("https://www.", "")\
        .replace("http://www.", "")\
        .replace("https://", "")\
        .replace("http://", "")\
        .rstrip("/")
    return finalUrl.lower()

def removeProtocolFromList(urlList):
    response = []
    for link in urlList:
        response.append(removeProtocolFromUrl(link))
    return response

if __name__ == "__main__":
    queries, googleDict, yahooDict = [], {}, {}
    overlappingResultsList, percentOverlapList, spearmanCoefficientList = [], [], []

    with open("Google_Result.json", "r") as googleFile:
        googleJson = googleFile.read()
        googleMap = json.loads(googleJson)
        for key, val in googleMap.items():
            googleResults = []
            for searchResult in val:
                googleResults.append(removeProtocolFromUrl(searchResult))
            googleDict[key] = googleResults

    with open("100QueriesSet.txt", "r") as qFile:
        for line in qFile:
            query = line.strip().rstrip(" ?")
            queries.append(query)

    for query in queries:
        sleep = randint(5, 10)
        yahooResults = SearchEngine.search(query, sleep)
        googleResults = googleDict.get(query, list)
        yahooDict[query] = yahooResults
        yahooResults = removeProtocolFromList(yahooResults)
        both = set(googleResults).intersection(yahooResults)
        googleIndexes = [googleResults.index(x) + 1 for x in both]
        yahooIndexes = [yahooResults.index(x) + 1 for x in both]
        print(query, " - ", sleep, " - ", len(yahooResults), " - ", len(both))
        overlapCount = len(both)
        overlapPercentage = overlapCount * 10
        if overlapCount == 0:
            spearmanCoefficient = 0.0
        elif overlapCount == 1:
            if googleIndexes[0] == yahooIndexes[0]:
                spearmanCoefficient = 1.0
            else:
                spearmanCoefficient = 0.0
        else:
            differenceInLists = []
            for index, val in enumerate(googleIndexes):
                differenceInLists.append((val - yahooIndexes[index])**2)
            spearmanCoefficient = 1 - ((6 * sum(differenceInLists)) / (overlapCount * ((overlapCount**2) - 1)))
        print(googleIndexes, yahooIndexes, overlapCount, overlapPercentage, spearmanCoefficient)

        overlappingResultsList.append(overlapCount)
        percentOverlapList.append(overlapPercentage)
        spearmanCoefficientList.append(spearmanCoefficient)

    with open("Yahoo_Result.json", "w") as yahooJsonFile:
        json.dump(yahooDict, yahooJsonFile, indent=2)

    with open("output.csv", "w") as outputFile:
        outputFile.write("Queries, Number of Overlapping Results, Percent Overlap, Spearman Coefficient\n")
        for index, query in enumerate(queries):
            outputFile.write("Query " + str(index + 1) + ", " + str(overlappingResultsList[index]) + ", " + str(percentOverlapList[index]) + ", " + str(spearmanCoefficientList[index]) + "\n")
        totalLength = len(queries)

        outputFile.write("Averages, " + str(sum(overlappingResultsList) / totalLength) + ", " + str(sum(percentOverlapList) / totalLength) + ", " + str(sum(spearmanCoefficientList) / totalLength))
    print("Averages, " + str(sum(overlappingResultsList) / totalLength) + ", " + str(sum(percentOverlapList) / totalLength) + ", " + str(sum(spearmanCoefficientList) / totalLength))