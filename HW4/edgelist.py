from bs4 import BeautifulSoup
import os


if __name__ == '__main__':
    mapFile = "URLtoHTML_latimes_news.csv"
    directory = "latimes"
    outputFile = "edgelist.csv"
    fileUrlDict = dict()
    urlFileDict = dict()
    edges = set()

    with open(mapFile, encoding="UTF-8") as file:
        file.readline()
        for line in file:
            lineSplit = line.strip("\n").split(",")
            fileUrlDict[lineSplit[0]] = lineSplit[1]
            urlFileDict[lineSplit[1]] = lineSplit[0]

    for file in os.listdir(directory):
        filename = os.path.join(directory, file)
        with open(filename, encoding="UTF-8") as file2:
            soup = BeautifulSoup(file2, features="html.parser")

        for link in soup.findAll('a', href=True):
            link2 = link.get('href')
            if link2 in urlFileDict:
                edges.add(os.path.basename(filename) + " " + urlFileDict[link2])

    with open(outputFile, "w+") as outfile:
        for edge in edges:
            outfile.write(edge)
            outfile.write("\n")
