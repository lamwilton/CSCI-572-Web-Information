import networkx as nx


if __name__ == '__main__':
    edgelist = "edgelist.txt"
    directory = "/home/osboxes/solr-7.7.3/crawl_data/"
    output = "external_pageRankFile.txt"

    G = nx.read_edgelist(edgelist, create_using=nx.DiGraph())
    pr = nx.pagerank(G, alpha=0.85, personalization=None, max_iter=30,
                     tol=1e-06, nstart=None, weight='weight', dangling=None)
    with open(output, "w+") as file:
        for key, value in pr.items():
            file.write(directory + key + "=" + str(value) + "\n")
