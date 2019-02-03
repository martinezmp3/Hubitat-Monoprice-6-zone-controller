#!/usr/bin/python
import socket, threading
import os, time, re, json, argparse, serial
#Serial comunication

ser = serial.Serial()
ser.port = "/dev/ttyUSB0" ################## SET ME!!!  If you use a USB dongle, plug it in and type dmesg to see what it uses.  ###########
ser.baudrate = 9600
ser.bytesize = serial.EIGHTBITS #number of bits per bytes
ser.parity = serial.PARITY_NONE #set parity check: no parity
ser.stopbits = serial.STOPBITS_ONE #number of stop bits
ser.timeout = 0.2              #timeout block read
ser.xonxoff = False     #disable software flow control
ser.rtscts = False     #disable hardware (RTS/CTS) flow control
ser.dsrdtr = False       #disable hardware (DSR/DTR) flow control
ser.writeTimeout = 2     #timeout for write

#End Serial communication

try:
    ser.open()
except Exception, e:
    print "Error opening serial port: " + str(e)
    exit()

if ser.isOpen():
	try:
		ser.flushInput()
		ser.flushOutput()
		ser.write("?11\r")
		time.sleep(0.2)
		numOfLines = 0
		response = ser.read(200)
		print response




        except Exception, e1:
            print "Error communicating...: " + str(e1)


ser.close()
