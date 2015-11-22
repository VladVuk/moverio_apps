import cv2
import numpy as np

width = 960
height = 492

leftImg = np.zeros([height, width, 3], np.uint8)
rightImg = np.zeros([height, width, 3], np.uint8)
totalImg = np.zeros([height, width, 3], np.uint8)
leftImg.fill(220)
rightImg.fill(220)
totalImg.fill(220)

squareWidth = 400
squareHeight = 200
startx = width/2 - squareWidth/2
endx = width/2 + squareWidth/2
starty = height/2 - squareHeight/2
endy = height/2 + squareHeight/2

for i in range(startx, endx+1):
	for j in range(starty, endy+1):
		leftImg[j,i-50,0] = 127
		rightImg[j,i+50,1] = 127
		totalImg[j,i-50,0] = 127
		totalImg[j,i+50,1] = 127


def drawBorder(img):
	shape = img.shape
	print shape
	rects = [(0,0,80,10), (0,0,10,80), (shape[0],0,shape[0]-80,10), (shape[0],0,shape[0]-10,80), \
			(0,shape[1],10,shape[1]-80), (0,shape[1],80,shape[1]-10),\
			(shape[0],shape[1],shape[0]-10,shape[1]-80), (shape[0],shape[1],shape[0]-80,shape[1]-10)]
	for r in rects:
		x1 = (r[0]+r[2]-abs(r[0]-r[2]))/2
		x2 = (r[0]+r[2]+abs(r[0]-r[2]))/2
		y1 = (r[1]+r[3]-abs(r[1]-r[3]))/2
		y2 = (r[1]+r[3]+abs(r[1]-r[3]))/2
		img[x1:x2, y1:y2, 0] = 0
		img[x1:x2, y1:y2, 1] = 0
		img[x1:x2, y1:y2, 2] = 0

drawBorder(leftImg)
drawBorder(rightImg)
drawBorder(totalImg)

separator = np.zeros([height, 150, 3], np.uint8)
separator.fill(255)


resizeImg = cv2.resize(np.concatenate((leftImg, rightImg), axis=1), (width, height))
sbsImg = np.concatenate((leftImg, separator, rightImg), axis=1)

leftImg = cv2.resize(leftImg, (0,0), fx=0.25, fy=0.25)
rightImg = cv2.resize(rightImg, (0,0), fx=0.25, fy=0.25)
resizeImg = cv2.resize(resizeImg, (0,0), fx=0.25, fy=0.25)
totalImg = cv2.resize(totalImg, (0,0), fx=0.25, fy=0.25)
sbsImg = cv2.resize(sbsImg, (0,0), fx=0.25, fy=0.25)

cv2.imwrite("resize.jpg", resizeImg)
cv2.imwrite("sbs.jpg", sbsImg)
cv2.imwrite("left.jpg", leftImg)
cv2.imwrite("right.jpg", rightImg)
cv2.imwrite("total.jpg", totalImg)


