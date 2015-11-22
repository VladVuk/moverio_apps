#!/usr/bin/env python

import numpy as np
import sys
import OpenGL.GL as gl
import OpenGL.GLU as glu
import OpenGL.GLUT as glut

zNear = 0.1
zFar = 1000.0


TransformationMatrix = np.array([[0.95015115, -0.31156227, 0.009452152, -163.45354],
								[0.19861476, 0.62816185, 0.75230217, 44.031586],
								[-0.24059308, -0.7128754, 0.65875655, -468.28262],
								[0.0, 0.0, 0.0, 1.0]])
ProjectionMatrix = np.array(
[[  2.28613924e+00,   7.04153989e-03,  -6.04805223e-01,   1.05142023e+02],
 [ -5.62139742e-02,  -2.23285721e+00,  -1.76064417e-01,   2.58386890e+01],
 [  2.28632863e-01,   1.10853539e-02,   7.92857347e-01,   8.58101998e+01],
 [ -2.28610007e-04,  -1.10842458e-05,  -7.92778089e-04,   1.41883817e-02]])




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


def mainDisplay():
	gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT)
	glut.glutSwapBuffers()


def view1Display():
	gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT)
	gl.glViewport(0, 0, 400, 400)
	gl.glMatrixMode(gl.GL_PROJECTION)
	gl.glLoadIdentity()
	gl.glOrtho(0, 400, 400, 0, zNear, zFar)
	gl.glMultMatrixf(nptolist(ProjectionMatrix), 0)
	gl.glMatrixMode(gl.GL_MODELVIEW)
	gl.glLoadMatrixf(nptolist(TransformationMatrix), 0)
	glut.glutSolidCube(20.0)
	glut.glutSwapBuffers()


def view2Display():
	gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT)
	gl.glViewport(0, 0, 400, 400)
	gl.glMatrixMode(gl.GL_PROJECTION)
	gl.glLoadIdentity()
	gl.glOrtho(0, 400, 400, 0, zNear, zFar)
	gl.glMultMatrixf(nptolist(ProjectionMatrix), 0)
	gl.glMatrixMode(gl.GL_MODELVIEW)
	gl.glLoadMatrixf(nptolist(TransformationMatrix), 0)
	glut.glutSolidCube(20.0)
	glut.glutSwapBuffers()



def reshape(w,h):
	gl.glViewport(0, 0, w, h)


def keyboard( key, x, y ):
	if key == '\033':
		sys.exit( )



glut.glutInit()
glut.glutInitDisplayMode(glut.GLUT_DOUBLE | glut.GLUT_RGBA | glut.GLUT_DEPTH)
glut.glutInitWindowSize(800, 400)

window = glut.glutCreateWindow('Subwindow Example')
glut.glutReshapeFunc(reshape)
glut.glutDisplayFunc(mainDisplay)
glut.glutKeyboardFunc(keyboard)

view1 = glut.glutCreateSubWindow(window, 0, 0, 400, 400)
glut.glutDisplayFunc(view1Display)
glut.glutKeyboardFunc(keyboard)

view2 = glut.glutCreateSubWindow(window, 400, 0, 400, 400)
glut.glutDisplayFunc(view2Display)
glut.glutKeyboardFunc(keyboard)



glut.glutMainLoop()
