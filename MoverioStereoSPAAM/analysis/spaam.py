import numpy as np
import math
import cv2
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





def left2Right(lex, rex):
	return lex.dot(np.linalg.inv(rex))





def showRotationAfterSwap(CalibMat):
	intrinsic, extrinsic = computeCameraParamsQR(CalibMat)
	temp = CalibMat.copy()
	temp[[1,0],:] = temp[[0,1],:]
	intrinsic2, extrinsic2 = computeCameraParamsQR(temp)
	print "Rotation of original matrix"
	print extrinsic[0:3,0:3]
	print "Rotation if swaped"
	print extrinsic2[0:3,0:3]





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




class SPAAM:
	def __init__(self, filename):
		self.screenPointList = np.zeros([0,3])
		self.spacePointList = np.zeros([0,4])

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
		self.spaamRead = np.array(mat)

		for j in range(3, numLines):
			data = lines[j].strip().split()
			screenPoint = np.array([[float(data[0]), float(data[1]), float(data[2])]])
			spacePoint = np.array([[float(data[3]),float(data[4]),float(data[5]),float(data[6])]])
			self.screenPointList = np.append(self.screenPointList, screenPoint, axis=0)
			self.spacePointList = np.append(self.spacePointList, spacePoint, axis=0)

		self.screenPointList = self.screenPointList.T
		self.spacePointList = self.spacePointList.T

		self.G = self.spaamCal()
		self.intrinsic, self.extrinsic = computeCameraParamsQR(self.G)




	def spaamCal(self):
		if self.screenPointList.shape[1] != self.spacePointList.shape[1]:
			print "Length not equal, spaam fails"
			return None

		l = self.screenPointList.shape[1]
		TScreen = np.zeros([3,3])
		TScreen[:,2] = np.mean(self.screenPointList, axis=1).T
		temp = np.std(self.screenPointList, axis=1)
		# temp = np.sqrt(np.multiply(np.apply_along_axis(np.linalg.norm, 1, screenPointList),\
		# 							np.apply_along_axis(np.linalg.norm, 1, screenPointList))\
		# 							- l*np.multiply(TScreen[:,2], TScreen[:,2]))
		TScreen[0,0] = temp[0]
		TScreen[1,1] = temp[1]
		TScreen_INV = np.linalg.inv(TScreen)
		TSpace = np.zeros([4,4])
		TSpace[:,3] = np.mean(self.spacePointList, axis=1).T
		temp = np.std(self.spacePointList, axis=1)
		# temp = np.sqrt(np.multiply(np.apply_along_axis(np.linalg.norm, 1, spacePointList),\
		# 							np.apply_along_axis(np.linalg.norm, 1, spacePointList))\
		# 						- l*np.multiply(TSpace[:,3], TSpace[:,3]))
		TSpace[0,0] = temp[0]
		TSpace[1,1] = temp[1]
		TSpace[2,2] = temp[2]
		TSpace_INV = np.linalg.inv(TSpace)

		NScreenPoint = TScreen_INV.dot(self.screenPointList)
		NSpacePoint = TSpace_INV.dot(self.spacePointList)
		
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

		self.GCore = GCore
		self.BHat = np.diag(s).dot(v)
		self.TScreen = TScreen
		self.TScreen_INV = TScreen_INV
		self.TSpace = TSpace
		self.TSpace_INV = TSpace_INV

		return G



	def GCoreFromP(self, p):
		'''
		self.level == 1:
			p = [[fx], [fy], [ox], [oy], [qx], [qy], [qz], [qw], [tx], [ty], [tz]]
		self.level == 2:
			p = [[f], [ox], [oy], [qx], [qy], [qz], [qw], [tx], [ty], [tz]]
		self.level == 3:
			p = [[fx], [fy], [ox], [oy], [qx], [qy], [qz], [qw], [tx], [ty], [tz]]
		'''
		if self.level == 1:
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
			GCore = self.TScreen_INV.dot(intrinsic.dot(extrinsic.dot(self.TSpace)))
			GCore = GCore / np.linalg.norm(GCore)
			return GCore.reshape(12,1)

		elif self.level == 2:
			intrinsic = np.zeros([3,4], dtype=np.float64)
			intrinsic[0,0] = p[0,0]
			intrinsic[1,1] = p[0,0]
			intrinsic[0,2] = p[1,0]
			intrinsic[1,2] = p[2,0]
			intrinsic[2,2] = 1.0
			extrinsic = transformations.quaternion_matrix([p[3,0],p[4,0],p[5,0],p[6,0]])
			extrinsic[0,3] = p[7,0]
			extrinsic[1,3] = p[8,0]
			extrinsic[2,3] = p[9,0]			
			GCore = self.TScreen_INV.dot(intrinsic.dot(extrinsic.dot(self.TSpace)))
			GCore = GCore / np.linalg.norm(GCore)
			return GCore.reshape(12,1)



	def getInitialP(self):
		initialQ = transformations.quaternion_from_matrix(self.extrinsic)
		if self.level == 1:
			return np.array([[self.intrinsic[0,0]], [self.intrinsic[1,1]],
							 [self.intrinsic[0,2]], [self.intrinsic[1,2]],
							 [initialQ[0]], [initialQ[1]], [initialQ[2]], [initialQ[3]],
							 [self.extrinsic[0,3]], [self.extrinsic[1,3]], [self.extrinsic[2,3]]])
		elif self.level == 2:
			return np.array([[(self.intrinsic[0,0] + self.intrinsic[1,1])/2],
							 [self.intrinsic[0,2]], [self.intrinsic[1,2]],
							 [initialQ[0]], [initialQ[1]], [initialQ[2]], [initialQ[3]],
							 [self.extrinsic[0,3]], [self.extrinsic[1,3]], [self.extrinsic[2,3]]])





	def normalizedP(self, p):
		'''
		Normalize state vector p so that the quaternion term is always unit
		p is a column matrix
		qIndex is the start index of quaternion term (x)
		'''
		qIndex = 0
		if self.level == 1:
			qIndex = 4
		elif self.level == 2:
			qIndex = 3
		temp = p[qIndex:qIndex+4].copy()
		temp = temp / np.linalg.norm(temp)
		p[qIndex:qIndex+4] = temp
		return p


	def Jacobian(self, p, GCore):
		'''
		Compute the Jacobian relating GCore (stacked) and state vector p
		'''
		l = p.shape[0]
		J = np.zeros([12,l], dtype=np.float64)
		for i in range(l):
			dp = np.zeros([l,1], dtype=np.float64)
			if p[i,0] < 0.00000001:
				increment = 0.0001
			else:
				increment = p[i,0]/1000.0
			dp[i,0] += increment
			dGCore = self.GCoreFromP(p+dp)
			J[:,i:i+1] = (dGCore - GCore) / increment
		return J




	def approximateGradientAlgebraic(self, level=1, printout=False):
		'''
		Using Gradient method to solve constrained camera estimation
		Cost function is the algebraic error
		'''
		self.level = level
		
		p = self.getInitialP()

		stepSize = 0.00001
		numIteration = 100

		for i in range(numIteration):
			GCore = self.GCoreFromP(p)
			J = self.Jacobian(p, GCore)
			error = -self.BHat.dot(GCore)
			if printout:
				print np.linalg.norm(error)
			Jac = self.BHat.dot(J)
			p = p + stepSize * Jac.T.dot(error)
			p = self.normalizedP(p)


		GCore = self.GCoreFromP(p).reshape(3,4)
		return self.TScreen.dot(GCore.dot(self.TSpace_INV))





	def getTotalGeometricError(self, CalibMat):
		if self.screenPointList.shape[1] != self.spacePointList.shape[1]:
			print "Length not equal, spaam fails"
			return -1

		l = self.screenPointList.shape[1]
		totalDist = 0.0
		projPointList = CalibMat.dot(self.spacePointList)
		for i in range(l):
			px = projPointList[0,i]/projPointList[2,i]
			py = projPointList[1,i]/projPointList[2,i]
			x = self.screenPointList[0,i]
			y = self.screenPointList[1,i]
			totalDist += (px-x)**2 + (py-y)**2
		return totalDist





left = SPAAM("LTotal.txt")
left1 = SPAAM("L1.txt")
left2 = SPAAM("L2.txt")
left3 = SPAAM("L3.txt")
left4 = SPAAM("L4.txt")
left5 = SPAAM("L5.txt")

leftApproxi1 = left.approximateGradientAlgebraic(level=1)
intrinsic1, extrinsic1 = computeCameraParamsQR(leftApproxi1)
print intrinsic1
print extrinsic1
leftApproxi2 = left.approximateGradientAlgebraic(level=2)
intrinsic2, extrinsic2 = computeCameraParamsQR(leftApproxi2)
print intrinsic2
print extrinsic2
visualize([left1.G, left2.G, left3.G, left4.G, left5.G, left.G, leftApproxi1, leftApproxi2], T1,
		  [(0,0,255), (0,0,255), (0,0,255), (0,0,255), (0,0,255), (255,255,255), (0,255,255), (255,255,0)])


