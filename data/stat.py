from math import sqrt
from statistics import median
import sys

a_star = open(sys.argv[1], "r")

def get_mean(arr):
    sum = 0
    for value, status in arr:
        sum += value
    return sum / len(arr) / 1_000_000

def standart_deviation(arr): 
    mean = get_mean(arr)
    squares = 0
    for value, status in arr:
        squares += (value - mean) ** 2
    return sqrt(squares / (len(arr) - 1) / 10 ** 12)


def mode(arr):
    d = {}
    for value, status in arr:
        devided_value = value // 10000
        if(devided_value not in d):
            d[devided_value] = 1
        else:
            d[devided_value] += 1
    return sorted(d.items(), key=lambda x: -x[1])[0][0] / 100

def amount_loses(arr): 
    res = 0

    for value, status in arr:
        if status == "L":
            res += 1;
    return res

def amount_wins(arr): 
    res = 0

    for value, status in arr:
        if status == "W":
            res += 1;
    return res


def median_value(arr): 
    data = []
    for value, status in arr:
        data.append(value)

    return median(data)



a_star_data = list(map(lambda x: (int(x.split()[0]), x.split()[1]), a_star.readlines()))

# print(*zip(a_star_data, b_track_data), sep="\n")

print("Mean:", get_mean(a_star_data))

print("Mode:", mode(a_star_data))

print("Median: ", median_value(a_star_data) / 10**6) 

print("Standart deviation:", standart_deviation(a_star_data))

loses_A = amount_loses(a_star_data)
wins_A = amount_wins(a_star_data)
print("Loses: ", loses_A)
print("Wins: ", wins_A)
print("Loses %: ", loses_A / len(a_star_data) * 100)
print("Wins %: ", wins_A / len(a_star_data) * 100)


