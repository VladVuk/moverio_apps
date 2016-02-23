#!/usr/bin/env python

import numpy as np
import cv2
import random

height = 480
width = 640

bgColor = 200

'''
img is 473 h * 473 w size
'''
img = cv2.imread("Hiro.bmp", cv2.CV_LOAD_IMAGE_GRAYSCALE)
xc = yc = 236
cv2.line(img, (xc-15, yc), (xc+15, yc), (0,0,0), 8)
cv2.line(img, (xc, yc-15), (xc, yc+15), (0,0,0), 8)


magic = [17,23,4,10,11,24,5,6,12,18,1,7,13,19,25,8,14,20,21,2]
# magic = [10, 30, 50, 20,10, 30, 50, 20,10, 30, 50, 20,10, 30, 50, 20,10, 30, 50, 20]



'''
Initializing fixed point list
'''
off = 160
fixedPointList = [(off,off), (320, off), (640-off,off), (off,480-off), (320,480-off),
					(640-off,480-off), (240,240), (400,240), (off,off), (320,off),
					(640-off,off), (off,480-off), (320,480-off), (640-off,480-off), (240,240),
					(400,240), (off,240), (640-off,240), (320,off), (320,480-off)]

# fixedPointList = [(off,off), (off,off), (off,off), (off,off), 
# 					(640-off,480-off), (640-off,480-off), (640-off,480-off), (640-off,480-off),
# 					(640-off,off), (640-off,off), (640-off,off), (640-off,off), 
# 					(off,480-off), (off,480-off), (off,480-off), (off,480-off), 
# 					(320,240), (320,240), (320,240), (320,240)]


def placeMarker(x, y, size):
	pts1 = np.float32([[xc,yc],[xc-1,yc],[xc,yc-1]])
	pts2 = np.float32([[x,y],[x-size,y],[x,y-size]])
 	# M = cv2.getPerspectiveTransform(pts1,pts2)
	# dst = cv2.warpPerspective(img, M, (width, height), borderValue=(255,255,255))
 	M = cv2.getAffineTransform(pts1,pts2)
	dst = cv2.warpAffine(img, M, (width, height), borderValue=(255,255,255))
	return dst


def changeBackground(src):
	return np.where( src>240, 160, src)


def slideShow(save):
	for i in range(0,20):
		size = random.uniform(0.5, 0.65)
		dst = placeMarker(fixedPointList[i][0], fixedPointList[i][1], 30.0/(magic[i]*3+50))
		dst = changeBackground(dst)
		dst = cv2.resize(dst,None,fx=2, fy=2, interpolation = cv2.INTER_CUBIC)
		if save:
			cv2.imwrite("slides/slide_"+str(i)+".bmp", dst)
		cv2.imshow("slide", dst)
		c = cv2.waitKey(0)
		print c
		if c == 113:
			break



slideShow(save = True)


