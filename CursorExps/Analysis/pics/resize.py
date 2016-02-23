import numpy as np
import cv2
import sys

i = 0
for name in sys.argv[1:]:
	img = cv2.imread(name)
	dst = cv2.resize(img,None,fx=2, fy=2, interpolation = cv2.INTER_CUBIC)
	cv2.imwrite("slide_"+str(i)+".bmp", dst)
	i += 1
