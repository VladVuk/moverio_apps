import cv2
import numpy as np

img = cv2.imread("box.jpg")
coef = 0.7
for i in range(img.shape[0]):
	for j in range(img.shape[1]):
		img[i,j,0] = img[i,j,0] * coef
		img[i,j,1] = img[i,j,1] * coef
		img[i,j,2] = img[i,j,2] * coef

cv2.imwrite("box_dark.jpg", img)
