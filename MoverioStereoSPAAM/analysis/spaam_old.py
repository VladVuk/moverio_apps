import numpy as np
import cv2
import math
import transformations


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



def normalizedG(G):
	'''
	Normalize G so that G[2,0]**2 + G[2,1]**2 + G[2,2]**2 = 1
	'''
	c = math.sqrt(G[2,0] ** 2 + G[2,1] ** 2 + G[2,2] ** 2)
	return G / c



def GCoreFromP(p, TScreen_INV, TSpace):
	'''
	p = [[fx], [fy], [ox], [oy], [qx], [qy], [qz], [qw], [tx], [ty], [tz]]
	'''
	intrinsic = np.zeros([3,4], dtype=np.float64)
	intrinsic[0,0] = p[0,0]
	intrinsic[1,1] = p[1,0]
	intrinsic[0,2] = p[2,0]
	intrinsic[1,2] = p[3,0]
	intrinsic[2,2] = 1.0

	extrinsic = transformations.quaternion_matrix([p[4,0],p[5,0],p[6,0],p[7,0]])
	extrinsic[0,3] = p[8,0]
	extrinsic[1,3] = p[9,0]
	extrinsic[2,3] = p[10,0]

	
	GCore = TScreen_INV.dot(intrinsic.dot(extrinsic.dot(TSpace)))
	GCore = GCore / np.linalg.norm(GCore)
	return GCore.reshape(12,1)



def Jacobian(p, GCore, TScreen_INV, TSpace):
	J = np.zeros([12,11], dtype=np.float64)
	for i in range(11):
		dp = np.zeros([11,1], dtype=np.float64)
		if p[i,0] < 0.00000001:
			increment = 0.0001
		else:
			increment = p[i,0]/1000.0
		dp[i,0] += increment
		dGCore = GCoreFromP(p+dp, TScreen_INV, TSpace)
		J[:,i:i+1] = (dGCore - GCore) / increment
	return J






def approximateGradientAlgebraic(screenPointList, spacePointList):
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
	BHat = np.diag(s).dot(v)
	GCore = v[11,:].reshape(3,4)
	G = TScreen.dot(GCore.dot(TSpace_INV))
	intr, extr = computeCameraParamsQR(G)


	initialQ = transformations.quaternion_from_matrix(extr)
	p = np.array([[intr[0,0]], [intr[1,1]], [intr[0,2]], [intr[1,2]],
				[initialQ[0]], [initialQ[1]], [initialQ[2]], [initialQ[3]],
				[extr[0,3]], [extr[1,3]], [extr[2,3]]])


	alpha = 0.00001
	for i in range(100):
		GCore = GCoreFromP(p, TScreen_INV, TSpace)
		J = Jacobian(p, GCore, TScreen_INV, TSpace)
		error = -BHat.dot(GCore)
		print np.linalg.norm(error)
		Jac = BHat.dot(J)
		p = p + alpha * Jac.T.dot(error)


	GCore = GCoreFromP(p, TScreen_INV, TSpace).reshape(3,4)
	return TScreen.dot(GCore.dot(TSpace_INV))





def getReprojectionError(screenPointList, spacePointList, G):
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


def showRotationAfterSwap(CalibMat):
	intrinsic, extrinsic = computeCameraParamsQR(CalibMat)
	temp = CalibMat.copy()
	temp[[1,0],:] = temp[[0,1],:]
	intrinsic2, extrinsic2 = computeCameraParamsQR(temp)
	print "Rotation of original matrix"
	print extrinsic[0:3,0:3]
	print "Rotation if swaped"
	print extrinsic2[0:3,0:3]




leftScreenPoint, leftSpacePoint, rightScreenPoint, rightSpacePoint, leftSPAAMRead, rightSPAAMRead = readMatrix()

leftGTotal = spaamCal2(leftScreenPoint, leftSpacePoint)
rightGTotal = spaamCal2(rightScreenPoint, rightSpacePoint)
leftSPAAMRead.append(leftGTotal)
rightSPAAMRead.append(rightGTotal)


leftGNormalized = normalizedG(leftGTotal)


print getReprojectionError(leftScreenPoint, leftSpacePoint, leftGTotal)
leftGTotalApp = approximateGradientAlgebraic(leftScreenPoint, leftSpacePoint)
print getReprojectionError(leftScreenPoint, leftSpacePoint, leftGTotalApp)

intrinsic, extrinsic = computeCameraParamsQR(leftGTotalApp)
print intrinsic
print extrinsic





# print "====================="
# print lex
# print rex
# print "====================="
# print lin
# print rin
