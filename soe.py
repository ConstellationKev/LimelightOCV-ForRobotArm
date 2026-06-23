# from math import isqrt

# primes_list = []

# # gets primes up to but not including this number
# n = 100

# # edge cases
# if n<=2:
#     print([])
# primes = [True] * n
# primes[0] = False
# primes[1] = False

# # found all primes
# for i in range(2, isqrt(n)):
#     if primes[i]:
#         for j in range(i*i, n, i):
#             primes[j] = False

# # printing primes
# for num in range(len(primes)):
#     if primes[num]:
#         primes_list.append(num)

# print(primes_list)

arr = [12, 1, 10, 4, 5, 7, 8, 6, 9, 3, 11, 2]

def binary_search(ls, low, high, target):
    while (high>=low):
        mid = (low+high)//2
        if (ls[mid]==target):
            return mid
        elif (ls[mid]<target):
            low = mid+1
        else:
            high = mid-1
    return -1

def bubble():
    sorted = False
    while not sorted:
        sorted = True
        for i in range(len(arr)-1):
            if arr[i] > arr[i+1]:
                temp = arr[i] # swap with temp
                arr[i] = arr[i+1]
                arr[i+1] = temp
                sorted = False

def select():
    curr_index = 0
    while curr_index<=len(arr)-2:
        index = curr_index+1
        min = index
        while index<len(arr):
            if arr[index] < arr[min]:
                min = index
            index+=1
        if arr[min] < arr[curr_index]:
                arr[min], arr[curr_index] = arr[curr_index], arr[min] # swap with no temp
        curr_index+=1

def insertion():
    curr_index = 1
    while (curr_index<len(arr)):
        if (arr[curr_index]<arr[curr_index-1]):
            insertdex = curr_index-1
            while insertdex>0 and arr[curr_index]<arr[insertdex-1]:
                insertdex-=1
            arr.insert(insertdex, arr[curr_index])
            arr.pop(curr_index+1)
        curr_index+=1
            

# bubble()
# select()
insertion()
print(arr)