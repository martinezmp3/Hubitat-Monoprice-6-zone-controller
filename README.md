# Hubitat
Hubitat Drivers
Driver for Hubitat Elevation Hub
//****************//
//***DEPRECATED***//
//****************//
Pease use the universal driver work with Linux base computer/TCP-Serial adapert (@jrperson working on a micro controler option wiht added functionalities) https://github.com/martinezmp3/Hubitat-Monoprice-6-zone-controller-TCP-IP-Serial 

(this is a work in progress any feed back will be really appreciate)

What you need:
-	A Hubitat Hub
-	A Raspberry Pi (with Ethernet connectivity and in the same network of your hub)
-	A USB to Serial cable
- ser2net.py by Pavel Revak https://github.com/pavelrevak/ser2tcp

Instalation instructions
-	Install Raspbian OS any version (https://www.raspberrypi.org/documentation/installation/installing-images/)
-	If you want headless “no keyboard, mouse or screen” (https://www.raspberrypi.org/documentation/configuration/wireless/headless.md)
-	Connect USB to Serial from Raspberry Pi to the amp and power on your Raspberry pi
-	Get access to your console via ssh I use putty but any will work
-	Install python pip “sudo apt-get install python-pip”
-	Install pyserial “pip install pyserial”
- cd /home/pi/
- wget https://raw.githubusercontent.com/pavelrevak/ser2tcp/master/ser2tcp.py
- chmod +x /home/pi/ser2tcp.py
- sudo nano /lib/systemd/system/ser2tcp.service
- copy and paste:
-
#-------------------------------------------------------------------------------------
[Unit]
Description=sevice for serial to tcp or telnet server, ser2net python. by Pavel Revak
After=multi-user.target

[Service]
Type=simple
ExecStart=/usr/bin/python3 /home/pi/ser2tcp.py /dev/ttyUSB0 9600 NONE -l 0:23
Restart=on-abort

[Install]
WantedBy=multi-user.target
#-------------------------------------------------------------------------------------
- sudo chmod 644 /lib/systemd/system/ser2tcp.service
- sudo systemctl daemon-reload
- sudo systemctl enable ser2tcp.service
- sudo systemctl start ser2tcp.service
-	Open up the MonoPrice-6-Zone-Amp-Controller.groovy and copy Ctrl+a and Ctrl+c (https://raw.githubusercontent.com/martinezmp3/Hubitat-Monoprice-6-zone-controller-TCP-IP-Serial/master/Parent-MonoPrice-6-Zone-Amp-Controller.groovy)
-	In your hub web page, select the "Drivers Code" section and then click the "+ New Driver" button
-	Click in the editor window. Then PASTE all of the code you copied in the previous step.
-	Click the SAVE button in the editor window
- Open up the MonoPrice-6-Zone-Amp-Controller.groovy and copy Ctrl+a and Ctrl+c (https://raw.githubusercontent.com/martinezmp3/Hubitat-Monoprice-6-zone-controller-TCP-IP-Serial/master/Child-MonoPrice-6-Zone-Amp-Controller.groovy)
- Go back to your hub web page, select the "Drivers Code" section and then click the "+ New Driver" button
-	Click in the editor window. Then PASTE all of the code you copied in the previous step.
-	Click the SAVE button in the editor window
-	In the hubitat web page, select the "Devices" section, and then click the "Add Virtual Device" button in the top right corner.
-	In the window that appears, please fill in the "Device Name", "Device Label"
-	In the type filed look for Parent MonoPrice 6 Zone Amp Controller
-	Click save Device
-	In the Preferences section ip = ip of the Raspberry Pi, port = port in the scrip (unless you changed should be 23), Number of amp is if you have more than one amp (not implemented yet)
-	Click save 
-	If everything went well you should all the zones will show up as childs
(this is a work in progress any feed back will be really appreciate)
