import json

if __name__ == '__main__':
    with open("hw1.json", "r") as file:
        my_result = json.loads(file.read())
    with open("hw1_format.json", "w+") as file:
        file.write(json.dumps(my_result, indent=2, separators=(',', ': ')))