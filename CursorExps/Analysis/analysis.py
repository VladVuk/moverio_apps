#!/usr/bin/env python

import numpy as np
import math
import sys


filenames = sys.argv[1:]

avgNum = 18




'''
Initializing fixed point list
'''
off = 160
fixedPointList = [(off,off), (320, off), (640-off,off), (off,480-off), (320,480-off),
					(640-off,480-off), (240,240), (400,240), (off,off), (320,off),
					(640-off,off), (off,480-off), (320,480-off), (640-off,480-off), (240,240),
					(400,240), (off,240), (640-off,240), (320,off), (320,480-off)]




def normalizedX(X):
	return (X-320) * 1.012 + 320

def normalizedY(Y):
	return (Y-320) * 1.00 + 320



print filenames


for filename in filenames:
	'''
	Determine the target file category
	'''
	category = 0
	if 'n' in filename or 'N' in filename:
		category = 2
	elif 'o' in filename or 'O' in filename:
		category = 1
	else:
		print "input file invalid"
		sys.exit()

	f = open(filename)


	'''
	Generating targetList
	'''
	targetList = list()
	if category == 1:
		targetList = list(fixedPointList)
	elif category == 2:
		for line in f:
			words = line.split()
			if words[0] == '$':
				targetList.append((float(words[1]), float(words[2])))

	# print len(targetList)

	targetCountMax = len(targetList)
	f = open(filename)


	'''
	Compute the distances
	'''
	totalDistances = list()
	targetCount = 0
	currentDistances = list()
	X = targetList[targetCount][0]
	Y = targetList[targetCount][1]
	for line in f:
		words = line.split()
		if words[0] == '*':
			x = float(words[1])
			y = float(words[2])
			currentDistances.append((x-normalizedX(X), y-normalizedY(Y)))
		elif words[0] == '$':
			targetCount += 1
			if targetCount == targetCountMax:
				break
			else:
				X = targetList[targetCount][0]
				Y = targetList[targetCount][1]
				totalDistances.append(list(currentDistances))
				currentDistances = list()



	'''
	Calc
	'''
	array = list()
	for d in totalDistances:
		# print len(d)
		avgNumCurrent = min(len(d), avgNum)
		if avgNumCurrent == 1:
			continue
		avgNumCurrent = 1
		# xavg = np.mean(d[-avgNumCurrent:][0])
		# yavg = np.mean(d[-avgNumCurrent:][1])
		xavg = d[-1][0]
		yavg = d[-1][1]
		array.append(math.sqrt(xavg**2 + yavg**2))


	array = np.array(array)
	print np.mean(array), np.std(array)

