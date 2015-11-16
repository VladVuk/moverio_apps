import numpy as np
import cv2
import sys
import OpenGL.GL as gl
import OpenGL.GLU as glu
import OpenGL.GLUT as glut


def nptolist(m):
	l = []
	for i in [0,1,2,3]:
		for j in [0,1,2,3]:
			l.append(m[j,i])
	return l

def printTranspose(m):
	for i in [0,1,2,3]:
		for j in [0,1,2,3]:
			print str(m[j,i])+"f,",
	print



point = np.array([[0.0],[0.0],[0.0],[1.0]])
G = [[2.252878021669723, -0.10538776507100311, -0.5965160927643689, 103.41611586300415],\
   [-0.07101499886683971, -2.2859606682614597, -0.18670062856834455, 23.575374227550363],\
   [-0.0001641127538991904, -0.0002442042492526862, -0.0008260820327195535, 0.000027671832411202413]]
G = np.array(G)
G[:,3] = G[:,3] / 1000.0
camMat, rotMat, transVec, rotXMat, rotYMat, rotZMat, euler = cv2.decomposeProjectionMatrix(G)

CAM = np.zeros([3,4], dtype=np.float32)
CAM[0:3, 0:3] = camMat
CAM_norm = np.zeros([3,4], dtype=np.float32)
CAM_norm[0:3, 0:3] = camMat / camMat[2,2]
MOV = np.zeros([4,4], dtype=np.float32)
MOV[0:3, 0:3] = rotMat
MOV[0:3, 3] = np.linalg.inv(camMat).dot(G[0:3, 3])
MOV[3,3] = 1.0

# At right center
# Trans = np.array([[0.95254594, -0.30351982, 0.023039132, -59.98667],
# 	[0.19971481, 0.6804334, 0.70506877, 11.276007],
# 	[-0.22963263, -0.6670301, 0.7087656, -407.66983],
# 	[0.0, 0.0, 0.0, 1.0]])
Trans = np.array([[0.95015115, -0.31156227, 0.009452152, -163.45354],
				[0.19861476, 0.62816185, 0.75230217, 44.031586],
				[-0.24059308, -0.7128754, 0.65875655, -468.28262],
				[0.0, 0.0, 0.0, 1.0]])
Trans[0:3,3] = Trans[0:3,3] / 1000.0


screen = CAM_norm.dot(MOV.dot(Trans.dot(point)))
print screen / screen[2]


f = 10.0
n = 0.0
r = 640
b = 480
ortho = [[2.0/r, 0.0, 0.0, -1],
		[0.0, -2.0/b, 0.0, 1],
		[0.0, 0.0, -2.0/(f-n), -(f+n)/(f-n)],
		[0.0, 0.0, 0.0, 1.0]]
ortho = np.array(ortho)
tproj = np.zeros([4,4], dtype=np.float32)
tproj[0:2,0:3] = CAM_norm[0:2,0:3]
tproj[0,2] = tproj[0,2]
tproj[1,2] = tproj[1,2]
tproj[2,2] = -(n+f)
tproj[2,3] = -n*f
tproj[3,2] = 1.0



proj = ortho.dot(tproj)

screen2 = proj.dot(MOV.dot(Trans.dot(point)))
print screen2 / screen2[3]

printTranspose(proj)
printTranspose(MOV)



def display():
	gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT)
	gl.glViewport(0, 0, 640, 480)
	gl.glMatrixMode(gl.GL_PROJECTION)
	# gl.glOrtho(0, r, b, 0, n, f)
	# gl.glMultMatrixf(nptolist(tproj),0)
	gl.glLoadMatrixf(nptolist(proj), 0)
	gl.glEnable(gl.GL_CULL_FACE)
	gl.glShadeModel(gl.GL_SMOOTH)
	gl.glEnable(gl.GL_DEPTH_TEST)
	gl.glFrontFace(gl.GL_CW)
	gl.glMatrixMode(gl.GL_MODELVIEW)
	gl.glLoadMatrixf(nptolist(MOV.dot(Trans)), 0)
	gl.glRotatef(180.0, 1.0, 0.0, 0)
	glut.glutSolidCube(0.02)
	glut.glutSwapBuffers()



def reshape(width,height):
	gl.glViewport(0, 0, width, height)

def keyboard( key, x, y ):
	if key == '\033':
		sys.exit( )

glut.glutInit()
glut.glutInitDisplayMode(glut.GLUT_DOUBLE | glut.GLUT_RGBA)
glut.glutCreateWindow('Hello world!')
glut.glutReshapeWindow(640,480)
glut.glutReshapeFunc(reshape)
glut.glutDisplayFunc(display)
glut.glutKeyboardFunc(keyboard)
glut.glutMainLoop()


# myTransVec = np.linalg.inv(camMat).dot(G[0:3, 3])
# # print myTransVec

# coefCAM = 1.0 / camMat[2,2]
# CAM = np.zeros([3,4], dtype=np.float32)
# CAM[0:3, 0:3] = camMat * coefCAM
# MOV = np.zeros([4,4], dtype=np.float32)
# MOV[0:3, 0:3] = rotMat
# MOV[0:3, 3] = myTransVec
# MOV[3,3] = 1.0
# print CAM
# print MOV
# print CAM.dot(MOV)


# f = 2.0
# n = 0.2
# l = 0.0
# r = 320.0
# b = 240.0
# t = 0.0
# m1 = np.zeros([4,3], dtype=np.float32)
# m1[0,0] = 1.0
# m1[1,1] = 1.0
# m1[2,2] = -f-n
# m1[3,2] = 1.0
# m2 = np.zeros([4,4], dtype=np.float32)
# m2[2,3] = f*n
# m3 = [[2.0/(r-l), 0.0, 0.0, -(r+l)/(r-l)],
#   [0.0, 2.0/(t-b), 0.0, -(t+b)/(t-b)],
#   [0.0, 0.0, 2.0/(f-n), -(f+n)/(f-n)],
#   [0.0, 0.0, 0.0, 1.0]]
# m3 = np.array(m3)
# newMat = (m1.dot(G) + m2).dot(m3)
# print newMat

