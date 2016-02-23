#!/usr/bin/env python

import pygame
import os
import socket

TCP_IP = '10.189.175.110'
TCP_PORT = 18943
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
i = -1
counter = 0
counting = False
seq = 0

x = 0
y = 0

cursor_image = pygame.image.load("cursor.png")
hl = cursor_image.get_width()/2


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
				s.send(str(event.pos[0]-hl) + ";" + str(event.pos[1]-hl) + ";5;" + str(seq) + ";\n")
			seq += 1
			counting = True
		elif event.type == pygame.MOUSEBUTTONDOWN and event.button == 3:
			if SOCKET:
				s.send(str(event.pos[0]-hl) + ";" + str(event.pos[1]-hl) + ";6;" + str(seq) + ";\n")
			seq += 1
			i = i - 1
		elif event.type == pygame.MOUSEMOTION:
			x = event.pos[0] - hl
			y = event.pos[1] - hl

	if i < -1:
		i = 0
	elif i >= 16:
		done = True
	else:
		screen.fill((128, 128, 128))
		if i == -1:
			image = pygame.image.load("slide_0.bmp")
		else:
			image = pygame.image.load("slide_" + str(i/4) + ".bmp")
		screen.blit(image, (X_OFFSET,Y_OFFSET))
		screen.blit(cursor_image, (x, y))
		pygame.display.flip()

if SOCKET:
	s.close()
