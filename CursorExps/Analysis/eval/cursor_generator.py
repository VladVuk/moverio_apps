#!/usr/bin/env python

import numpy as np
import cv2

size = 48
width = 6
img = np.zeros((size, size, 4), dtype=np.uint8)

for i in range(0,size):
	for j in range(0,size):
		if i < size/2 + width/2 and i >= size/2 - width/2:
			img[i,j,1] = 255
			img[i,j,3] = 255
		elif j < size/2 + width/2 and j >= size/2 - width/2:
			img[i,j,1] = 255
			img[i,j,3] = 255


cv2.imwrite("cursor.png", img)

