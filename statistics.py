a_star = open("Astar_final_final.txt", "r")
b_track = open("BackTrack_final_final.txt", "r")

def get_mean(arr):
    sum = 0
    for value, status in arr:
        sum += value
    return sum / len(arr) / 1_000_000

def variance(arr): 
    mean = get_mean(arr)
    squares = 0
    for value, status in arr:
        squares += (value - mean) ** 2
    return squares / (len(arr) - 1) / 10 ** 12


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

a_star_data = list(map(lambda x: (int(x.split()[0]), x.split()[1]), a_star.readlines()))

b_track_data = list(map(lambda x: (int(x.split()[0]), x.split()[1]), b_track.readlines()))
# print(*zip(a_star_data, b_track_data), sep="\n")

print("Mean for A*:", get_mean(a_star_data))
print("Mean for Back tracking:", get_mean(b_track_data))

print("Mode for A*:", mode(a_star_data))
print("Mode for Back tracking:", mode(b_track_data))

print("Variance for A*:", variance(a_star_data))
print("Variance for Back tracking:", variance(b_track_data))

loses_A = amount_loses(a_star_data)
loses_BTrack = amount_loses(b_track_data)
wins_A = amount_wins(a_star_data)
wins_BTrack = amount_wins(b_track_data)
print("Loses for A*:", loses_A)
print("Loses for Back tracking:", loses_BTrack)
print("Wins for A*:", wins_A)
print("Wins for Back tracking:", wins_BTrack)
print("Loses / Wins A*:", loses_A / wins_A)
print("Loses / Wins BackTrack:", loses_BTrack / wins_BTrack)
