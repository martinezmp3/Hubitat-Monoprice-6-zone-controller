#!/usr/bin/python
import socket, threading
import os, serial, time, re, json
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
HOST = ''
PORT = 10022
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind((HOST, PORT))
s.listen(4)
pill2kill = threading.Event()
clients = [] #list of clients connected
lock = threading.Lock()
count = 0
class chatServer(threading.Thread):
    def __init__(self, (socket,address)):
        threading.Thread.__init__(self)
        self.socket = socket
        self.address= address
    def run(self):
        lock.acquire()
        clients.append(self)
        lock.release()
        print '%s:%s connected.' % self.address
        while True:
            data = self.socket.recv(1024)
            if data.startswith("Zone"):
		zone = data[4:6]
		command = data[6:8]
		value = int(data[9:11])
		print ('%02d' %value)
#		print "<{}{}{}".format(zone, command, value)
		try:
			ser.open()
			if ser.is_open:
				print "serial port open"
		                ser.flushInput()
        	        	ser.flushOutput()
				ser.write("<{}{}{}\r".format(zone, command, ('%02d' %value)))
				time.sleep(0.2)
	               		response = ser.read(200)
 	              		print response
				ser.close()
		except Exception, e1:
       			print "Error communicating...: " + str(e1)
		self.socket.send("Done\n\r")
            if not data:
                break
	    if data.startswith("get"):
		zone = data[3:5]
                print "?{}\r".format(zone)
                try:
                        ser.open()
                        if ser.is_open:
                                print "serial port open"
                                ser.flushInput()
                                ser.flushOutput()
                                ser.write("?{}\r".format(zone))
                                time.sleep(0.2)
                                response = ser.read(200)
                                print response
                                ser.close()
				self.socket.send(response+"\n\r")
                except Exception, e1:
                        print "Error communicating...: " + str(e1)

#        self.socket.send("message\n\r")
#            for c in clients:
#		print data
#                c.socket.send(data)
        self.socket.close()
        print '%s:%s disconnected.' % self.address
        lock.acquire()
        clients.remove(self)
        lock.release()
def poll (stop_event):
	global count
	response = ""
	for i in range(35):
		time.sleep(1)
		count = count+1
		print count
		if i == 10 or i == 20 or i == 30:
	                try:
        	                ser.open()
                	        if ser.is_open:
					print "serial port open"
                   	                ser.flushInput()
                       	                ser.flushOutput()
                                        ser.write("?10\r")
                                	time.sleep(0.2)
                                	response = ser.read(200)
                               		print response
                                	ser.close()
#                                	self.socket.send(response+"\n\r")
 	                except Exception, e1:
               			         print "Error communicating...: " + str(e1)

			print "sending message"
#			j = 0
			for c in clients:
#				j = j+1
#				response = str(j)+"test"+"\n\r"
				print response
				c.socket.send(response)






t = threading.Thread(target=poll,args=(pill2kill,))
#client.publish(log_mqtt_topic,"thread start",0,0)
t.setDaemon(True)
t.start()
#print t.isDaemon()
#        client.publish(log_mqtt_topic,"thread is a Daemon "+str(t.isDaemon()),0,0)
if t.isAlive():
	print "is Alive"

#while True:
#    time.sleep(1)
#    cout = cont+1
#    print cont
while True: # wait for socket to connect
    # send socket to chatserver and start monitoring
    chatServer(s.accept()).start()
