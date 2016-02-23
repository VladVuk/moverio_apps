#!/usr/bin/env python

import pygame
import os
import socket

TCP_IP = '10.189.175.110'
TCP_PORT = 18944
BUFFER_SIZE = 1024

X_OFFSET = 0
Y_OFFSET = 0



SOCKET = True
if SOCKET:
	s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	s.connect((TCP_IP, TCP_PORT))

pygame.init()

screen = pygame.display.set_mode((1280, 960))
pygame.mouse.set_visible(False)
done = False
i = 0
counter = 0
counting = False
seq = 0

while not done:
	if seq > 10000000:
		seq = 0

	if counting:
		counter += 1
	if counter > 50:
		counting = False
		counter = 0
		i = i + 1

	for event in pygame.event.get():
		if event.type == pygame.QUIT:
			done = True
		elif event.type == pygame.MOUSEBUTTONDOWN and event.button == 1:
			if SOCKET:
				s.send(str(event.pos[0]-X_OFFSET) + ";" + str(event.pos[1]-Y_OFFSET) + ";1;" + str(seq) + ";\n")
			seq += 1
			counting = True
		elif event.type == pygame.MOUSEBUTTONDOWN and event.button == 3:
			if SOCKET:
				s.send(str(event.pos[0]-X_OFFSET) + ";" + str(event.pos[1]-Y_OFFSET) + ";2;" + str(seq) + ";\n")
			seq += 1
			i = i - 1
		elif event.type == pygame.MOUSEMOTION:
			if SOCKET:
				s.send(str(event.pos[0]-X_OFFSET) + ";" + str(event.pos[1]-Y_OFFSET) + ";-1;" + str(seq) + ";\n")
			seq += 1

	if i < 0:
		i = 0
	elif i >= 20:
		if SOCKET:
			s.close()
			SOCKET = False
	else:
		screen.fill((128, 128, 128))

		image = pygame.image.load("pics/slide_" + str(i) + ".bmp")
		screen.blit(image, (X_OFFSET,Y_OFFSET))
		pygame.display.flip()




if SOCKET:
	s.close()
