/*
 * 
 * MonoPrice 6 Zone Amp Controller
 * 
 */
metadata {
	definition (name: "MonoPrice 6 Zone Amp Controller", namespace: "jorge.martinez", author: "Jorge Martinez") 	{
		capability "Polling"
		capability "Telnet"
		capability "Initialize"
		capability "Actuator"
        	capability "Switch"
        	capability "Sensor"
		capability "AudioVolume"
		attribute "mediaSource", "NUMBER"
		attribute "level" , "NUMBER"
		attribute "balance" , "NUMBER"
		attribute "bass" , "NUMBER"
		attribute "treble" , "NUMBER"
		command "poll"
		command "forcePoll"
		command "sendMsg" , ["STRING"]
		command "CloseTelnet"
		command "Source1"
		command "Source2"
		command "Source3"
		command "Source4"
		command "Source5"
		command "Source6"
//		command "setLevel"  ,["NUMBER"]
		command "nextTrack"
		command "previousTrack"
	}
	preferences {
		section("Device Settings:") 
		{
			input "IP", "String", title:"IP of Amp Controller", description: "IP", required: true, displayDuringSetup: true
			input "port", "NUMBER", title:"port of Amp Controller", description: "port", required: true, displayDuringSetup: true
			input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
			input name: "NumberAmps", type: "enum", description: "", title: "Number Amps", options: [[1:"1"],[2:"2"],[3:"3"]], defaultValue: 1
			input name: "Zone", type: "enum", description: "", title: "Zone", options: [[11:"Zone 1"],[12:"Zone 2"],[13:"Zone 3"],[14:"Zone 4"],[15:"Zone 5"],[16:"Zone 6"]], defaultValue: 11
		    	input name: "Percent",type: "enum", description: "", title: "Percent to dec/enc", options: [[1:1],[2:2],[3:3],[5:5],[10:10],[15:15]], defaultValue: 3
			input name: "MaxVolumen", type: "NUMBER", description: "", title: "Max volumen allow", defaultValue: 38
		}
	}
}
def CloseTelnet(){
telnetClose() 
}
def installed() {
	log.info('MonoPrice 6 Zone Amp Controller: installed()')
	initialize()
}
def updated(){
	log.info('MonoPrice 6 Zone Amp Controller: updated()')
	initialize()
	unschedule()
	runEvery1Minute(pollSchedule)
}
def pollSchedule(){
    forcePoll()
}
def initialize(){
	log.info('MonoPrice 6 Zone Amp Controller: initialize()')
	telnetClose() 
	telnetConnect([termChars:[13]], settings.IP, settings.port as int, '', '')
}
def forcePoll(){
	log.debug "Polling"
	sendMsg("get${settings.Zone}")
}
def poll(){
    if(now() - state.lastPoll > (60000))
        forcePoll()
    else
        log.debug "poll called before interval threshold was reached"
}
def sendMsg(String msg){
	log.info("Sending telnet msg: " + msg)
	return new hubitat.device.HubAction(msg, hubitat.device.Protocol.TELNET)
}
private parse(String msg) {
//	log.debug("Parse: " + msg.substring(3,5))
//	log.debug("#>"+settings.Zone)
	if (msg.length()>5)
	{
		if(msg.substring(3,5) == settings.Zone)
		{
			log.debug("got mach: "+ msg)
			def power = msg.substring(7,9)
			if (power.toInteger()){
				state.switch = "on"
				sendEvent(name: "switch", value: "on", isStateChange: true)
			}
			if (!power.toInteger()){
				state.switch = "off"
        			sendEvent(name: "switch", value: "off", isStateChange: true)
			}
			def mute = msg.substring(9,11)
			if (mute.toInteger()){
				state.mute = "muted"
				sendEvent(name: "mute", value: "muted", isStateChange: true)
			}
			if (!mute.toInteger()){
				state.mute = "unmuted"
				sendEvent(name: "mute", value: "unmuted", isStateChange: true)
			}
			def vol = msg.substring(13,15)
			if (state.volume != vol){
				state.volume = vol.toInteger()
				sendEvent(name: "volume", value: state.volume.toInteger(), isStateChange: true)
			}
			def source = msg.substring(21,23)
			if (state.mediaSource != source){
				state.mediaSource = source.toInteger()
				sendEvent(name: "mediaSource", value: state.mediaSource.toInteger(), isStateChange: true)
			}
			def Balance = msg.substring(19,21)
			if (state.balance != Balance){
				state.balance = Balance.toInteger()
				sendEvent(name: "balance", value: state.balance.toInteger(), isStateChange: true)
			}
			def Bass = msg.substring(17,19)
			if (state.bass != Bass){
				state.bass = Bass.toInteger()
				sendEvent(name: "bass", value: state.bass.toInteger(), isStateChange: true)
			}
			def Treble = msg.substring(15,17)
			if (state.treble != Treble){
				state.treble = Treble.toInteger()
				sendEvent(name: "treble", value: state.treble.toInteger(), isStateChange: true)
			}
			
		}
		else
			log.debug("not intrested" + msg)

	}
}
def telnetStatus(String status){
	log.warn "telnetStatus: error: " + status
	if (status != "receive error: Stream is closed")
	{
		log.error "Connection was dropped."
		initialize()
	} 
}
def nextTrack(){
	if(state.mediaSource<6)
	{
		def newmediaSource = state.mediaSource+1
		if (logEnable) {
			log.debug "next Source"
			log.debug state.mediaSource
			}
			try {
			sendEvent(name: "mediaSource", value: (newmediaSource), isStateChange: true)
			state.mediaSource = newmediaSource
			sendMsg ("Zone${settings.Zone}ch=0${state.mediaSource}")
  			} 
			catch (Exception e) {
        		log.warn "next Source failed: ${e.message}"
    			}
	}
}
def previousTrack(){
	if(state.mediaSource>1)
	{
		def newmediaSource = state.mediaSource-1
		if (logEnable) {
			log.debug "previous Source"
			log.debug state.mediaSource
			}
			try {
			sendEvent(name: "mediaSource", value: (newmediaSource), isStateChange: true)
			state.mediaSource = newmediaSource
			sendMsg ("Zone${settings.Zone}ch=0${state.mediaSource}")
  			} 
			catch (Exception e) {
        		log.warn "previous Source failed: ${e.message}"
    			}
	}
}
def setLevel (Value){
    if (logEnable) log.debug Value
    try {
	    if (Value>settings.MaxVolumen)
	    	Value = settings.MaxVolumen
	    if (Value<0)
	    	Value = 0
	    state.volume = Value
	    sendEvent(name: "volume", value: state.volume, isStateChange: true)
	    sendMsg ("Zone${settings.Zone}vo=${state.volume}")
  } catch (Exception e) {
        log.warn "Call to off failed: ${e.message}"
    }
}
def on() {
    if (logEnable) log.debug "trunning on"
	try {
		state.switch = "on"
        	sendEvent(name: "switch", value: "on", isStateChange: true)
		sendMsg ("Zone${settings.Zone}pr=01")
        }
	 catch (Exception e) {
        log.warn "Call to on failed: ${e.message}"
    }
}
def off() {
    if (logEnable) log.debug "trunning off"
    try {
	 	state.switch = "off"
        	sendEvent(name: "switch", value: "off", isStateChange: true)
		sendMsg ("Zone${settings.Zone}pr=00")
        }
	catch (Exception e) {
        log.warn "Call to off failed: ${e.message}"
    }
}
def volumeUp() {
	if (state.volume<100){
		if (logEnable) {
			log.debug "Volumen UP ${state.volume}"
			}
    		try {
	    		def newvolume = (state.volume.toInteger() + settings.Percent.toInteger()).toInteger()
	    		log.debug newvolume
			if (newvolume.toInteger()>settings.MaxVolumen.toInteger()) //ovewrite volume if > that max allow
				newvolume = settings.MaxVolumen.toInteger()
	    		state.volume = newvolume
			sendEvent(name: "volume", value: state.volume.toInteger(), isStateChange: true)
			sendMsg ("Zone${settings.Zone}vo=${state.volume}")
  		} catch (Exception e) {
        		log.warn "Call to off failed: ${e.message}"
    			}
	}
}
def volumeDown() {
	if (state.volume>0){
		if (logEnable) log.debug "Volumen DOWN ${state.volume}"
    		try {
	    		def newvolume = ((state.volume as long) - (settings.Percent as long))
	    		log.debug newvolume
			if (newvolume <0)
				newvolume = 0
	    		state.volume = newvolume
			sendEvent(name: "volume", value: state.volume, isStateChange: true)
	    		sendMsg ("Zone${settings.Zone}vo=${state.volume}")
  		} catch (Exception e) {
        		log.warn "Call to off failed: ${e.message}"
    			}
	}
}
def setVolume(Volume) {
    if (logEnable) log.debug Volume
    try {
	    if (Volume>settings.MaxVolumen)
	    	Volume = settings.MaxVolumen
	    if (Volume<0)
	    	Volume = 0
	    state.volume = Volume
	    sendEvent(name: "volume", value: state.volume, isStateChange: true)
	    sendMsg ("Zone${settings.Zone}vo=${state.volume}")
  } catch (Exception e) {
        log.warn "Call to off failed: ${e.message}"
    }
}
def mute() {
    if (logEnable) log.debug "mute"
    try {
	    	state.mute = "muted"
		sendEvent(name: "mute", value: "muted", isStateChange: true)
	    	sendMsg ("Zone${settings.Zone}mu=01")
  } catch (Exception e) {
        log.warn "Call to off failed: ${e.message}"
    }
}
def unmute() {
    if (logEnable) log.debug "unmute"
    try {
	    	state.mute = "unmuted"
		sendEvent(name: "mute", value: "unmuted", isStateChange: true)
	    	sendMsg ("Zone${settings.Zone}mu=00")
  } catch (Exception e) {
        log.warn "Call to off failed: ${e.message}"
    }
}
def Source1(){
    if (logEnable) log.debug "Source1"
    try {
		state.mediaSource = 1
		sendEvent(name: "mediaSource", value: 1, isStateChange: true)
		sendMsg ("Zone${settings.Zone}ch=0${state.mediaSource}")
    } catch (Exception e) {
        log.warn "Call to off failed: ${e.message}"
    }
}
def Source2(){
    if (logEnable) log.debug "Source2"
    try {
		state.mediaSource = 2
		sendEvent(name: "mediaSource", value: 2, isStateChange: true)
	    	sendMsg ("Zone${settings.Zone}ch=0${state.mediaSource}")
  } catch (Exception e) {
        log.warn "Call to off failed: ${e.message}"
    }
}
def Source3(){
    if (logEnable) log.debug "Source3"
    try {	    
		state.mediaSource = 3
		sendEvent(name: "mediaSource", value: 3, isStateChange: true)
		sendMsg ("Zone${settings.Zone}ch=0${state.mediaSource}") 
  } catch (Exception e) {
        log.warn "Call to off failed: ${e.message}"
    }
}
def Source4(){
    if (logEnable) log.debug "Source4"
    try {
		state.mediaSource = 4
		sendEvent(name: "mediaSource", value: 4, isStateChange: true)
	    	sendMsg ("Zone${settings.Zone}ch=0${state.mediaSource}")
  } catch (Exception e) {
        log.warn "Call to off failed: ${e.message}"
    }
}
def Source5(){
    if (logEnable) log.debug "Source5"
    try {	    
		state.mediaSource = 5
		sendEvent(name: "mediaSource", value: 5, isStateChange: true)
	    	sendMsg ("Zone${settings.Zone}ch=0${state.mediaSource}")
  } catch (Exception e) {
        log.warn "Call to off failed: ${e.message}"
    }
}
def Source6(){
    if (logEnable) log.debug "Source6"
    try {
		state.mediaSource = 6
		sendEvent(name: "mediaSource", value: 6, isStateChange: true)
		sendMsg ("Zone${settings.Zone}ch=0${state.mediaSource}")
  } catch (Exception e) {
        log.warn "Call to off failed: ${e.message}"
    }
}
