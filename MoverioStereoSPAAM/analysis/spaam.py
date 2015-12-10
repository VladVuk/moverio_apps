import numpy as np
import cv2



singlePoint = np.array([[0.0],[0.0],[0.0],[1.0]])
T1 = np.array([[0.95015115, -0.31156227, 0.009452152, -163.45354],
			[0.19861476, 0.62816185, 0.75230217, 44.031586],
			[-0.24059308, -0.7128754, 0.65875655, -468.28262],
			[0.0, 0.0, 0.0, 1.0]])
T2 = np.array([[0.95254594, -0.30351982, 0.023039132, -59.98667],
			[0.19971481, 0.6804334, 0.70506877, 11.276007],
			[-0.22963263, -0.6670301, 0.7087656, -407.66983],
			[0.0, 0.0, 0.0, 1.0]])
np.set_printoptions(suppress=True)


def readMatrix():
	leftScreenPoint = np.zeros([0,3])
	leftSpacePoint = np.zeros([0,4])
	rightSpacePoint = np.zeros([0,4])
	rightScreenPoint = np.zeros([0,3])
	leftSPAAMRead = list()
	rightSPAAMRead = list()

	for i in range(1,6,1):
		filename = "L" + str(i) + ".txt"
		fp = open(filename)
		print "Reading " + filename
		lines = fp.readlines()
		numLines = len(lines)
		print "Number of lines in " + filename + ": " + str(numLines)
		
		mat = list()
		for j in range(0,3):
			data = lines[j].strip().split()
			row = list()
			for d in data:
				row.append(float(d))
			mat.append(row)
		leftSPAAMRead.append(np.array(mat))

		for j in range(3, numLines):
			data = lines[j].strip().split()
			screenPoint = np.array([[float(data[0]), float(data[1]), float(data[2])]])
			spacePoint = np.array([[float(data[3]),float(data[4]),float(data[5]),float(data[6])]])
			leftScreenPoint = np.append(leftScreenPoint, screenPoint, axis=0)
			leftSpacePoint = np.append(leftSpacePoint, spacePoint, axis=0)

	for i in range(1,6,1):
		filename = "R" + str(i) + ".txt"
		fp = open(filename)
		print "Reading " + filename
		lines = fp.readlines()
		numLines = len(lines)
		print "Number of lines in " + filename + ": " + str(numLines)
		
		mat = list()
		for j in range(0,3):
			data = lines[j].strip().split()
			row = list()
			for d in data:
				row.append(float(d))
			mat.append(row)
		rightSPAAMRead.append(np.array(mat))

		for j in range(3, numLines):
			data = lines[j].strip().split()
			screenPoint = np.array([[float(data[0]), float(data[1]), float(data[2])]])
			spacePoint = np.array([[float(data[3]),float(data[4]),float(data[5]),float(data[6])]])
			rightScreenPoint = np.append(rightScreenPoint, screenPoint, axis=0)
			rightSpacePoint = np.append(rightSpacePoint, spacePoint, axis=0)

	return leftScreenPoint.T, leftSpacePoint.T, rightScreenPoint.T, rightSpacePoint.T, leftSPAAMRead, rightSPAAMRead





def spaamCal1(screenPointList, spacePointList):
	if screenPointList.shape[1] != spacePointList.shape[1]:
		print "Length not equal, spaam fails"
		return

	l = screenPointList.shape[1]
	TScreen = np.zeros([3,3])
	TScreen[:,2] = np.mean(screenPointList, axis=1).T
	temp = np.std(screenPointList, axis=1)
	TScreen[0,0] = temp[0]
	TScreen[1,1] = temp[1]
	TScreen_INV = np.linalg.inv(TScreen)
	TSpace = np.zeros([4,4])
	TSpace[:,3] = np.mean(spacePointList, axis=1).T
	temp = np.std(spacePointList, axis=1)
	TSpace[0,0] = temp[0]
	TSpace[1,1] = temp[1]
	TSpace[2,2] = temp[2]
	TSpace_INV = np.linalg.inv(TSpace)

	NScreenPoint = TScreen_INV.dot(screenPointList)
	NSpacePoint = TSpace_INV.dot(spacePointList)
	
	B = np.zeros([2*l, 12])
	for i in range(l):
		xi = NSpacePoint[0,i]
		yi = NSpacePoint[1,i]
		zi = NSpacePoint[2,i]
		px = NScreenPoint[0,i]
		py = NScreenPoint[1,i]
		B[2*i, 0] = xi
		B[2*i, 1] = yi
		B[2*i, 2] = zi
		B[2*i, 3] = 1.0
		B[2*i, 8] = -px*xi
		B[2*i, 9] = -px*yi
		B[2*i, 10] = -px*zi
		B[2*i, 11] = -px
		B[2*i+1, 4] = xi
		B[2*i+1, 5] = yi
		B[2*i+1, 6] = zi
		B[2*i+1, 7] = 1.0
		B[2*i+1, 8] = -py*xi
		B[2*i+1, 9] = -py*yi
		B[2*i+1, 10] = -py*zi
		B[2*i+1, 11] = -py

	u, s, v = np.linalg.svd(B)
	GCore = v[11,:].reshape(3,4)
	G = TScreen.dot(GCore.dot(TSpace_INV))
	print "Condition number: ", s[0] / s[11]
	return G



def spaamCal2(screenPointList, spacePointList):
	'''
	This SPAAM calculation is identical to the one running on the goggle,
	although it is not perfectly optimized.
	spaamCal1 yeilds less error.
	'''
	if screenPointList.shape[1] != spacePointList.shape[1]:
		print "Length not equal, spaam fails"
		return

	l = screenPointList.shape[1]
	TScreen = np.zeros([3,3])
	TScreen[:,2] = np.mean(screenPointList, axis=1).T
	temp = np.sqrt(np.multiply(np.apply_along_axis(np.linalg.norm, 1, screenPointList),\
								np.apply_along_axis(np.linalg.norm, 1, screenPointList))\
								- l*np.multiply(TScreen[:,2], TScreen[:,2]))
	TScreen[0,0] = temp[0]
	TScreen[1,1] = temp[1]
	TScreen_INV = np.linalg.inv(TScreen)
	TSpace = np.zeros([4,4])
	TSpace[:,3] = np.mean(spacePointList, axis=1).T
	temp = np.sqrt(np.multiply(np.apply_along_axis(np.linalg.norm, 1, spacePointList),\
								np.apply_along_axis(np.linalg.norm, 1, spacePointList))\
								- l*np.multiply(TSpace[:,3], TSpace[:,3]))
	TSpace[0,0] = temp[0]
	TSpace[1,1] = temp[1]
	TSpace[2,2] = temp[2]
	TSpace_INV = np.linalg.inv(TSpace)

	NScreenPoint = TScreen_INV.dot(screenPointList)
	NSpacePoint = TSpace_INV.dot(spacePointList)
	
	B = np.zeros([2*l, 12])
	for i in range(l):
		xi = NSpacePoint[0,i]
		yi = NSpacePoint[1,i]
		zi = NSpacePoint[2,i]
		px = NScreenPoint[0,i]
		py = NScreenPoint[1,i]
		B[2*i, 0] = xi
		B[2*i, 1] = yi
		B[2*i, 2] = zi
		B[2*i, 3] = 1.0
		B[2*i, 8] = -px*xi
		B[2*i, 9] = -px*yi
		B[2*i, 10] = -px*zi
		B[2*i, 11] = -px
		B[2*i+1, 4] = xi
		B[2*i+1, 5] = yi
		B[2*i+1, 6] = zi
		B[2*i+1, 7] = 1.0
		B[2*i+1, 8] = -py*xi
		B[2*i+1, 9] = -py*yi
		B[2*i+1, 10] = -py*zi
		B[2*i+1, 11] = -py

	u, s, v = np.linalg.svd(B)
	GCore = v[11,:].reshape(3,4)
	print "Condition number: ", s[0] / s[11]
	G = TScreen.dot(GCore.dot(TSpace_INV))
	return G
	





def visualize(CalibMatList, T, colorList):
	l = len(CalibMatList)
	for i in range(-50, 50, 1):
		point = np.array([[i],[0.0],[0.0],[1.0]])
		img = np.zeros([540, 960, 3], dtype=np.uint8)
		for j in range(l):
			sp = CalibMatList[j].dot(T.dot(point))
			x = sp[0]/sp[2]
			y = sp[1]/sp[2]
			cv2.circle(img, (x, y), 2, colorList[j], -1)
		cv2.imshow("Vis", img)
		cv2.waitKey(0)







def getGDistance(screenPointList, spacePointList, G):
	if screenPointList.shape[1] != spacePointList.shape[1]:
		print "Length not equal, spaam fails"
		return
	l = screenPointList.shape[1]
	totalDist = 0.0
	projPointList = G.dot(spacePointList)
	for i in range(l):
		px = projPointList[0,i]/projPointList[2,i]
		py = projPointList[1,i]/projPointList[2,i]
		x = screenPointList[0,i]
		y = screenPointList[1,i]
		totalDist += (px-x)**2 + (py-y)**2
	return totalDist



def computeCameraParamsQR(CalibMat):
	'''
	Internal matrix is normalized (I[2,2] = 1.0)
	External matrix is the transformation from eye to camera
	'''
	val, R, Q, Qx, Qy, Qz = cv2.RQDecomp3x3(CalibMat[0:3,0:3])
	if R[2,2] < 0:
		R[:,2] = -R[:,2]
	extrinsic = np.zeros([4,4], dtype=float)
	extrinsic[0:3,:] = np.linalg.inv(R).dot(CalibMat)
	R = R/R[2,2]
	temp = R.dot(extrinsic[0:3,0:4].dot(T1.dot(singlePoint))).T
	if temp[0,2] < 0:
		extrinsic[3,3] = -1.0
		extrinsic = -extrinsic
	else:
		extrinsic[3,3] = 1.0
	return R, extrinsic


def left2Right(lex, rex):
	return lex.dot(np.linalg.inv(rex))


def getR3(CalibMat):
	r = CalibMat[2,0:3]
	return r / np.linalg.norm(r)


def findCommonRotation(left, right):
	pass


def showRotationAfterSwap(CalibMat):
	intrinsic, extrinsic = computeCameraParamsQR(CalibMat)
	temp = CalibMat.copy()
	temp[[1,0],:] = temp[[0,1],:]
	intrinsic2, extrinsic2 = computeCameraParamsQR(temp)
	print "Rotation of original matrix"
	print extrinsic[0:3,0:3]
	print "Rotation if swaped"
	print extrinsic2[0:3,0:3]


def printTranspose(m):
	'''
	Used for debugging, printing the transposed matrix so that
	it can be directly copied to OpenGLRenderer.java
	'''
	for i in [0,1,2]:
		for j in [0,1,2,3]:
			print str(m[i,j])+",",
	print


leftScreenPoint, leftSpacePoint, rightScreenPoint, rightSpacePoint, leftSPAAMRead, rightSPAAMRead = readMatrix()

leftGTotal = spaamCal2(leftScreenPoint, leftSpacePoint)
rightGTotal = spaamCal2(rightScreenPoint, rightSpacePoint)
leftSPAAMRead.append(leftGTotal)
rightSPAAMRead.append(rightGTotal)



for i in range(len(leftSPAAMRead)):
	lin, lex = computeCameraParamsQR(leftSPAAMRead[i])
	rin, rex = computeCameraParamsQR(rightSPAAMRead[i])
	print leftSPAAMRead[i]
	print rightSPAAMRead[i]

# for i in range(0,5):
# 	spaamCal1(leftScreenPoint[:,20*i:20*i+20], leftSpacePoint[:,20*i:20*i+20])
# 	spaamCal1(rightScreenPoint[:,20*i:20*i+20], rightSpacePoint[:,20*i:20*i+20])


G = np.linalg.inv(leftGTotal[0:3,0:3]).dot(rightGTotal[0:3,0:3])


'''
Ideas:
1. Instead of decomposing, we should use optimization to get a rotation, that minimize 'zero' terms in projection matrix
2. Even idea 1 can be applied to original SPAAM to get a better G matrix that is more meaningful.
3. Gradually relax parameters, gradually decrease number of unknown in stereo system. (But how to solve for matrix is really a problem)
'''

'''
1. Weisserman
2. Project two screen
3. Look at two matrices
'''


# print "====================="
# print lex
# print rex
# print "====================="
# print lin
# print rin
