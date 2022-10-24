data = open("res.txt", 'r')

a1 = open("A1.txt", "w")
a2 = open("A2.txt", "w")
b1 = open("B1.txt", "w")
b2 = open("B2.txt", "w")

for line in data.readlines():
    t, time, res = line.split()
    if(t == "A1"):
        a1.write(f"{time} {res}\n")
    if(t == "A2"):
        a2.write(f"{time} {res}\n")
    if(t == "B1"):
        b1.write(f"{time} {res}\n")
    if(t == "B2"):
        b2.write(f"{time} {res}\n")


