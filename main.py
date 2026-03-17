import time  # thats gunna be used to measure the time it takes to run the code
import csv  # cuz csv files are freaky, and im aint doing allat myself
from names import names
from surnames import surnames


# thats the alorythm. cool.
def levenshteinDistance(str1, str2):
    m, n = len(str1), len(str2)
    dp = [[0] * (n + 1) for _ in range(m + 1)]  # cool 2D matrix to store distances
    for i in range(m + 1):
        dp[i][0] = i
    for j in range(n + 1):
        dp[0][j] = j
    for i in range(1, m + 1):
        for j in range(1, n + 1):
            if str1[i - 1] == str2[j - 1]:
                dp[i][j] = dp[i - 1][j - 1]
            else:
                dp[i][j] = 1 + min(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
    return dp[m][n]

def find_closest(word, candidates):
    min_dist = float("inf")
    best = word
    for candidate in candidates:
        dist = levenshteinDistance(word, candidate)
        if dist < min_dist:
            min_dist = dist
            best = candidate
    return best, min_dist

def compare_with_levenshtein(full_name: list, names_to_compare: list, surnames_to_compare: list):
    the_name, the_surname = full_name
    # Early exit jeśli oba imiona i nazwiska są w listach
    if the_name in names_to_compare and the_surname in surnames_to_compare:
        return the_name, the_surname

    # Try both orders if both are not found
    options = [
        (the_name, the_surname),
        (the_surname, the_name)
    ]
    best_result = None
    best_total_dist = float("inf")

    for name_candidate, surname_candidate in options:
        name_match, name_dist = find_closest(name_candidate, names_to_compare) if name_candidate not in names_to_compare else (name_candidate, 0)
        surname_match, surname_dist = find_closest(surname_candidate, surnames_to_compare) if surname_candidate not in surnames_to_compare else (surname_candidate, 0)
        total_dist = name_dist + surname_dist
        if total_dist < best_total_dist:
            best_total_dist = total_dist
            best_result = (name_match, surname_match)

    return best_result

# Czeba usunąć prefixy jak "dla Kuby Sawulskiego"
def remove_prefix(full_name: list):
    prefixes = {"dla", "do", "od", "na", "u", "z", "ze", "za", "po", "w", "we", "o", "przy"}
    while full_name[0].lower() in prefixes:
        full_name = full_name[1:]
    return full_name

def remove_particles(full_name: list):
    if len(full_name) == 2:
        return full_name[0], full_name[1]
    particle_idx = None

    for idx, part in enumerate(full_name[1:], 1):
        if part.lower() in surname_particles:
            particle_idx = idx
            break
    if particle_idx is not None:
        the_name, the_surname = full_name[0], " ".join(full_name[particle_idx+1:])
    else:
        the_name, the_surname = full_name[0], " ".join(full_name[1:])
    return the_name, the_surname
def clear_name(full_name: list):
    return remove_particles(remove_prefix(full_name))
    

# this is in case someone does like Vladmir von Levenshtein
surname_particles = {
    "von",
    "van",
    "de",
    "da",
    "del",
    "di",
    "la",
    "le",
    "du",
    "dos",
    "das",
    "der",
    "den",
    "dla",
    "ter",
    "ten",
    "el",
    "al",
    "bin",
    "ibn",
    "ap",
    "af",
    "mac",
    "mc",
    "fitz",
    "st.",
    "saint",
}

# for matching_name in matching_names:
# print("Levenshtein Distance:", distance)


import csv

start_time = time.time()

# Compute Levenshtein distances for names and surnames in names.csv and save to results.csv if fromFile is True
with open("names.csv", newline='', encoding='utf-8') as file, \
        open("results.csv", mode='w', newline='', encoding='utf-8') as results_file:
    reader = csv.reader(file, delimiter=';', quotechar='"')
    writer = csv.writer(results_file, delimiter=';', quotechar='"', quoting=csv.QUOTE_ALL)
    for row in reader:
        name, surname =  compare_with_levenshtein(clear_name(row[0].split()), names, surnames)
        writer.writerow([f'{name} {surname}', row[1]])

final_time = time.time()
print("~Time it took: ", round(final_time - start_time, 5))