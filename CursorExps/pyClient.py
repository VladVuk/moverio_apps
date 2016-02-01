#!/usr/bin/env python

import socket


TCP_IP = '10.189.175.110'
TCP_PORT = 18944
BUFFER_SIZE = 1024

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((TCP_IP, TCP_PORT))

MESSAGE = "Hello World"
flag = True
while flag:
	MESSAGE = raw_input("TCP/IP <<< ")
	if MESSAGE == "-1":
		flag = False
	else:
		MESSAGE = MESSAGE + "\n"
		s.send(MESSAGE)

s.close()
