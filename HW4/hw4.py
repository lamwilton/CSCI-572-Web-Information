from urllib.request import *

connection = urlopen('http://localhost:8983/solr/myexample/select?q=cheese&wt=python')
response = eval(connection.read())

print(response['response']['numFound'], "documents found.")

# Print the name of each document.

for document in response['response']['docs']:
    print("  Name =", document['id'])