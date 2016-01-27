#!/usr/bin/env python
'''
Project: MoverioSPAAM
File: spaam_gl_simulator.py
Author: Long Qian
Date: 2015-11-16

This script is used for testing AR display based on OpenGL and
the <CalibrationMatrix> computed from SPAAM calibration.
Because of ARToolkit convention, the actual <CalibrationMatrix> and
<TransformationMatrix> matrix's unit is mm!
Simply tune the parameter <InMeter> to change configuration.

The script also includes other utilities for testing and evaluation.
'''

import numpy as np
import cv2
import sys
import OpenGL.GL as gl
import OpenGL.GLU as glu
import OpenGL.GLUT as glut

np.set_printoptions(suppress=True)

# If InMeter is true, the unit for everything is meter.
# Otherwise, millimeter.
InMeter = True

# The position of the cube in world coordinates.
CubeCenter = np.array([[0.0],[0.0],[0.0],[1.0]])
CubeLength = 20.0


# A sample calibration CalibrationMatrix
# CalibrationMatrix = np.array([[2.252878021669723, -0.10538776507100311, -0.5965160927643689, 103.41611586300415],\
# 						   [-0.07101499886683971, -2.2859606682614597, -0.18670062856834455, 23.575374227550363],\
# 						   [-0.0001641127538991904, -0.0002442042492526862, -0.0008260820327195535, 0.000027671832411202413]])

# Another possible calibration matrix
# CalibrationMatrix = np.array([[-2.7159485335934357, -0.0014938360164294191, 0.7440388536997122, -115.47160933857211],
# 							[-1.8082579312160302E-5, 2.6440062702707126, 0.23924594027458754, -31.169444938835202],
# 							[7.888564506953308E-6, 1.0805111431645914E-5, 0.0010603382586340745, 0.006774942350076085]])
CalibrationMatrix = np.array([[2.286139238334763, 0.0070415398909735895, -0.604805223461732, 105.14202332183234],
							[-0.05621397423572271, -2.232857213864352, -0.1760644170151926, 25.838689031762023],
							[-2.2861000742183536E-4, -1.1084245778509513E-5, -7.927780889265074E-4, 0.014188381718983789]])
# CalibrationMatrix = np.array([[1.6380400513461664, -5.286675477061316E-4, -0.42126167214557564, 83.65883817583584],\
# 							[0.020200868919747414, -1.5139893596735088, -0.2747110787710624, 31.84094083501643],\
# 							[4.4314148028949554E-5, 3.426760716105004E-6, -5.868797627342585E-4, 0.020811645815770607]])
# CalibrationMatrix = np.array([[   1.16524563,    0.00278049,   -0.31905683,  131.49488093],
#  [  -0.01455041,   -1.12576543,   -0.2071979,    12.96216352],
#  [   0.00001206,   -0.00004513,   -0.00045472,   -0.00223643]])
# CalibrationMatrix = np.array([[ -1.19551985,  -0.01441908,   0.31620645, -55.14054087],
#  [  0.01836658,   1.12986884,   0.20908707, -17.42113775],
#  [  0.00005829,  -0.00003967,   0.00043723,  -0.00149776]])

# A sample transformation matrix of the AR tag
TransformationMatrix = np.array([[0.95015115, -0.31156227, 0.009452152, -163.45354],
								[0.19861476, 0.62816185, 0.75230217, 44.031586],
								[-0.24059308, -0.7128754, 0.65875655, -468.28262],
								[0.0, 0.0, 0.0, 1.0]])
# Another possible transformation Matrix
# TransformationMatrix = np.array([[0.95254594, -0.30351982, 0.023039132, -59.98667],
# 								[0.19971481, 0.6804334, 0.70506877, 11.276007],
# 								[-0.22963263, -0.6670301, 0.7087656, -407.66983],
# 								[0.0, 0.0, 0.0, 1.0]])


# Projection Matrix to be multiplied after glOrtho
ProjectionMatrix = np.zeros([4,4], dtype=np.float32)


if InMeter:
	TransformationMatrix[0:3,3] = TransformationMatrix[0:3,3] / 1000.0
	CalibrationMatrix[:,3] = CalibrationMatrix[:,3] / 1000.0
	CubeLength /= 1000.0


# Window and viewing geometry
height = 480
width = 640
zNear = 0.1
zFar = 1000.0





def computeProjectionMatrix(CalibMat):
	'''
	Projection matrix is pushed into OpenGL stack after an 
	orthogonal viewing matrix.
	<ProjMat> depends on calibration matrix, near and far clipping
	plan which helps OpenGL z-buffer to work.
	'''
	sign = 1
	if CalibMat[0,3] < 0 and CalibMat[1,3] < 0:
		sign = -1
		print "Inverse sign"
	LeftMat = np.zeros([4,3], dtype=np.float32)
	LeftMat[0,0] = 1.0
	LeftMat[1,1] = 1.0
	LeftMat[2,2] = -(zFar+zNear)
	LeftMat[3,2] = 1.0
	RightMat = np.zeros([4,4], dtype=np.float32)
	RightMat[2,3] = zFar*zNear
	if InMeter:
		RightMat[2,3] /= 1000.0
	ProjMat = LeftMat.dot(CalibMat * sign) + RightMat
	return ProjMat





def computeCameraParams(CalibMat):
	'''
	We can think of our eye as a pinhole camera, then we can
	determine the instrinsic and extrinsic parameter of our 
	eye (virtual camera).
	OpenCV function is called to compute them.
	Here, the <instrinsicMat> is not normalized, simply multiply 
	by k to get normalized instrinsic parameters
	'''
	instrinsicMat, rotMat, transVec, rotXMat, rotYMat, rotZMat, euler = cv2.decomposeProjectionMatrix(CalibMat)
	k = 1.0 / instrinsicMat[2,2]
	transVec_norm = transVec / transVec[3]
	extrinsicMat = np.zeros([4,4], dtype=np.float32)
	extrinsicMat[0:3, 0:3] = rotMat
	extrinsicMat[0:3, 3] = -rotMat.dot(transVec_norm[0:3]).T
	extrinsicMat[3,3] = 1.0
	return instrinsicMat, extrinsicMat, k





def computeGlOrthof(w, h, near, far):
	'''
	This function calculates the orthogonal viewing matrix produced
	by OpenGL, especially for simulating cameras, where top-left 
	is (0, 0), and z-axis pointing outward.
	Identical to gl.glOrtho(0, w, h, 0, near, far).
	'''
	orthoMat = np.array([[2.0/w, 0.0, 0.0, -1],
					[0.0, -2.0/h, 0.0, 1],
					[0.0, 0.0, -2.0/(far-near), -(far+near)/(far-near)],
					[0.0, 0.0, 0.0, 1.0]])
	return orthoMat





def computeScreenCoordinates(p, TransMat, ProjMat, OrthoMat, CalibMat):
	'''
	p should be homogeneous 4 by 1 numpy vector.
	Calculate the coordinate of the point on the screen.
	'''
	print "By OpenGL >>>"
	ps1 = OrthoMat.dot(ProjMat.dot(TransMat.dot(p)))
	ps1 = ps1 / ps1[3]
	if ps1[2] < -1.0 or ps1[2] > 1.0:
		print "Point will not be displayed on screen."
	else:
		print "Point normalized device coordinate is:", ps1.T
	ps2 = CalibMat.dot(TransMat.dot(p))
	ps2 = ps2 / ps2[2]
	print "By SPAAM >>>"
	print "Point screen coordinate is:", ps2[0], ps2[1]






def nptolist(m):
	'''
	Used to create a list of float numbers to be pushed to OpenGL.
	Follows OpenGL sequencial convention: column first, row second.
	'''
	l = []
	for i in [0,1,2,3]:
		for j in [0,1,2,3]:
			l.append(m[j,i])
	return l






def printTranspose(m):
	'''
	Used for debugging, printing the transposed matrix so that
	it can be directly copied to OpenGLRenderer.java
	'''
	for i in [0,1,2,3]:
		for j in [0,1,2,3]:
			print str(m[j,i])+"f,",
	print





def display():
	gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT)
	gl.glViewport(0, 0, width, height)
	gl.glMatrixMode(gl.GL_PROJECTION)
	gl.glLoadIdentity()
	gl.glOrtho(0, width, height, 0, zNear, zFar)
	gl.glMultMatrixf(nptolist(ProjectionMatrix), 0)
	gl.glEnable(gl.GL_CULL_FACE)
	gl.glShadeModel(gl.GL_SMOOTH)
	gl.glEnable(gl.GL_DEPTH_TEST)
	gl.glFrontFace(gl.GL_CW)
	gl.glMatrixMode(gl.GL_MODELVIEW)
	gl.glLoadMatrixf(nptolist(TransformationMatrix), 0)
	glut.glutSolidCube(CubeLength)
	glut.glutSwapBuffers()



def reshape(w,h):
	gl.glViewport(0, 0, w, h)


def keyboard( key, x, y ):
	if key == '\033':
		sys.exit( )




ProjectionMatrix = computeProjectionMatrix(CalibrationMatrix)
printTranspose(ProjectionMatrix)
printTranspose(TransformationMatrix)
glut.glutInit()
glut.glutInitDisplayMode(glut.GLUT_DOUBLE | glut.GLUT_RGBA)
glut.glutCreateWindow('SPAAM OpenGL Simulator')
glut.glutReshapeWindow(width, height)
glut.glutReshapeFunc(reshape)
glut.glutDisplayFunc(display)
glut.glutKeyboardFunc(keyboard)
glut.glutMainLoop()




