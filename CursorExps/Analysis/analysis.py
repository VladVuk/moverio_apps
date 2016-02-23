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


transformation = np.zeros([4,4])
transformation[3,3] = 1.0

def pushMatrix(floats, i):
	if i == 0:
		transformation[0,0] = floats[0]
		transformation[0,1] = floats[1]
		transformation[0,2] = floats[2]
		transformation[0,3] = floats[3]
		transformation[1,0] = floats[4]
		transformation[1,1] = floats[5]
		transformation[1,2] = floats[6]
		transformation[1,3] = floats[7]
		transformation[2,0] = floats[8]
		transformation[2,1] = floats[9]
		transformation[2,2] = floats[10]
		transformation[2,3] = floats[11]
	else:
		transformation[i-1,0] = floats[0]
		transformation[i-1,1] = floats[1]
		transformation[i-1,2] = floats[2]
		transformation[i-1,3] = floats[3]


projection = np.array([[2.3174095153808594, 0.0, -0.19314861297607422, 0.0],
	[0.0, 3.1142921447753906, 0.0605013370513916, 0.0],
	[0.0, 0.0, -1.0020020008087158, -20.02001953125],
	[0.0, 0.0, -1.0, 0.0]])
singlePoint = np.array([[0.0],[0.0],[0.0],[1.0]])
def getTrackedPos():
	temp = projection.dot(transformation.dot(singlePoint))
	temp = temp / temp[3,0]
	return 640.0 * (temp[0,0] + 1.0) / 2.0, 480.0 * (-temp[1,0] + 1.0) / 2.0




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
			elif words[0] == '%':
				targetList = targetList[:-1]

	print len(targetList)

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
		if words[0] == "*1":
			results = [float(i) for i in words[1:]]
			pushMatrix(results, 1)
		elif words[0] == "*2":
			results = [float(i) for i in words[1:]]
			pushMatrix(results, 2)
		elif words[0] == "*3":
			results = [float(i) for i in words[1:]]
			pushMatrix(results, 3)
			x, y = getTrackedPos()
			# print x-normalizedX(X), y-normalizedY(Y)
			currentDistances.append((x-normalizedX(X), y-normalizedY(Y)))
		elif words[0] == "*":
			results = [float(i) for i in words[1:]]
			pushMatrix(results, 0)
			x, y = getTrackedPos()
			currentDistances.append((x-normalizedX(X), y-normalizedY(Y)))
		elif words[0] == '$':
			targetCount += 1
			# raw_input()
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
		avgNumCurrent = min(len(d), avgNum)
		if avgNumCurrent == 1:
			continue
		avgNumCurrent = 1
		# xavg = np.mean(d[-avgNumCurrent:][0])
		# yavg = np.mean(d[-avgNumCurrent:][1])
		xavg = d[-1][0]
		yavg = d[-1][1]
		if abs(xavg) > 400 or abs(yavg) > 400:
			continue
		array.append(math.sqrt(xavg**2 + yavg**2))


	array = np.array(array)
	print np.mean(array), np.std(array)

