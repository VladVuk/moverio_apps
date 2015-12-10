import numpy as np
import cv2
import transformations


np.set_printoptions(suppress=True)

T1 = np.array([[0.95015115, -0.31156227, 0.009452152, -163.45354],
			[0.19861476, 0.62816185, 0.75230217, 44.031586],
			[-0.24059308, -0.7128754, 0.65875655, -468.28262],
			[0.0, 0.0, 0.0, 1.0]])
T2 = np.array([[0.95254594, -0.30351982, 0.023039132, -59.98667],
			[0.19971481, 0.6804334, 0.70506877, 11.276007],
			[-0.22963263, -0.6670301, 0.7087656, -407.66983],
			[0.0, 0.0, 0.0, 1.0]])
singlePoint = np.array([[0.0],[0.0],[0.0],[1.0]])


def visualize(CalibMatList, T):
	l = len(CalibMatList)
	for i in range(-50, 50, 1):
		point = np.array([[i],[0.0],[0.0],[1.0]])
		img = np.zeros([540, 960, 3], dtype=np.uint8)
		for CalibMat in CalibMatList:
			sp = CalibMat.dot(T.dot(point))
			x = sp[0]/sp[2]
			y = sp[1]/sp[2]
			cv2.circle(img, (x, y), 2, (255,255,255), -1)
		cv2.imshow("Vis", img)
		cv2.waitKey(0)




def computeCameraParamsQR(CalibMat):
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


def readMatrix(filename):
	fp = open(filename)
	lines = fp.readlines()
	mat = list()
	for i in range(0,3):
		data = lines[i].strip().split()
		row = list()
		for d in data:
			row.append(float(d))
		mat.append(row)
	return np.array(mat)


LEX = list()
LIN = list()
REX = list()
RIN = list()
LEFT = list()
RIGHT = list()
for i in range(5):
	right = readMatrix("R"+str(i+1)+".txt")
	rin, rex = computeCameraParamsQR(right)
	REX.append(rex)
	RIN.append(rin)
	print right
	RIGHT.append(right)
	# temp = rin.dot(rex[0:3,0:4].dot(T1.dot(singlePoint))).T
	# print temp/temp[0,2]
	# print rin

	left = readMatrix("L"+str(i+1)+".txt")
	lin, lex = computeCameraParamsQR(left)
	# temp = lin.dot(lex[0:3,0:4].dot(T1.dot(singlePoint))).T
	# print temp/temp[0,2]
	LEX.append(lex)
	LIN.append(lin)
	LEFT.append(left)

visualize(RIGHT, T2)
