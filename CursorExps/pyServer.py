#!/usr/bin/env python

import socket
import cv2
import numpy as np

TCP_IP = '10.161.159.176'
TCP_PORT = 18944
BUFFER_SIZE = 1024  # Normally 1024, but we want fast response

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind((TCP_IP, TCP_PORT))
s.listen(1)

cv2.namedWindow("img")

conn, addr = s.accept()
print 'Connection address:', addr
while 1:
    data = conn.recv(BUFFER_SIZE)
    if not data: break
    print "received data:", data    

    conn.send(data)  # echo

    l = list()
    l = data.split(';')
    print l
    if len(l) != 3:
    	continue
    img = np.zeros([1280, 720, 3], dtype=np.uint8)
    cv2.circle(img, (int(l[0]), int(l[1])), 3, (255,0,0), -1)
    cv2.imshow("img", img)
    cv2.waitKey(1)

conn.close()
