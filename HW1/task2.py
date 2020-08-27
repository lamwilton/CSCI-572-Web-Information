import json


def fix_similar_url(url):
    """
    Fix for the metrics similar for URLs
    :param url: url input
    :return: Fixed url
    """
    url_new = url.replace("://www.", "://")
    url_new = url_new.replace("http://", "https://")
    url_new = url_new.lower().rstrip("/")
    return url_new


def spearman(google_list, my_list, overlap_list):
    """
    Compute Spearman's coefficient rho
    :param google_list:
    :param my_list:
    :param overlap_list:
    :return: rho
    """
    sum_d_i_square = 0
    n = len(overlap_list)
    for item in overlap_list:
        try:
            google_rank = google_list.index(item)
            my_rank = my_list.index(item)
        except ValueError:
            print("Overlap list is not right. Item does not overlap!")
            break
        sum_d_i_square += (google_rank - my_rank) ** 2

    # Calculate rho according to FAQ #6 value of rho
    if n == 0:
        rho = 0
    elif n == 1:
        rho = 1 if google_rank == my_rank else 0
    else:
        rho = 1 - (6 * sum_d_i_square) / (n * (n ** 2 - 1))
    return rho


def compute(query):
    """
    Compute all required results for the query
    :param query:
    :return:
    """
    google_list_pre = google_result[query]
    my_list_pre = my_result[query]
    google_list = []
    for url in google_list_pre:
        google_list.append(fix_similar_url(url))
    my_list = []
    for url in my_list_pre:
        my_list.append(fix_similar_url(url))

    overlap_list = list(set(google_list).intersection(set(my_list)))
    num_overlap = len(overlap_list)
    num_total = max(len(my_list), len(google_list))
    pct_overlap = 0 if num_total == 0 else num_overlap / num_total * 100

    rho = spearman(google_list, my_list, overlap_list)
    return num_overlap, pct_overlap, rho


if __name__ == '__main__':
    with open("Google_Result4.json", "r") as file:
        google_result = json.loads(file.read())
    with open("hw1.json", "r") as file:
        my_result = json.loads(file.read())

    num_overlap_list, pct_overlap_list, rho_list = [], [], []  # Save results for final average

    with open("hw1.csv", "a+") as output_file:
        output_file.write("Queries, Number of Overlapping Results, Percent Overlap, Spearman Coefficient\n")

        for query in google_result.keys():
            assert query in my_result.keys()  # Make sure both dicts has the query
            num_overlap, pct_overlap, rho = compute(query)
            output_file.write(", ".join([query, str(num_overlap), str(pct_overlap), str(rho)]) + "\n")

            num_overlap_list.append(num_overlap)
            pct_overlap_list.append(pct_overlap)
            rho_list.append(rho)

        # Compute and write Averages
        num_overlap_avg = sum(num_overlap_list) / len(num_overlap_list)
        pct_overlap_avg = sum(pct_overlap_list) / len(pct_overlap_list)
        rho_avg = sum(rho_list) / len(rho_list)
        output_file.write(", ".join(["Averages", str(num_overlap_avg), str(pct_overlap_avg), str(rho_avg)]))

    print("Successfully output to hw1.csv")
