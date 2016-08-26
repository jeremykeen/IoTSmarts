/**
 * Particle (Spark) Core / Photon / Electron Remote Pool Temp and pH Logger
 * Work in progress to capture pool temp and pH level from photon
 * Get it here: https://github.com/jkeenest/IoTSmarts/
 *
 * Modified from Nic Jansma's code:
 * Author: Nic Jansma
 *
 * Licensed under the MIT license
 *
 * Available at: https://github.com/nicjansma/smart-things/
 *
 * Device type for a Particle (Spark) Core/Photon/Electron temperature/humidity/heat index sensor:
 *   https://github.com/nicjansma/dht-logger/
 */

preferences {
    input name: "channelID", type: "text", title: "ThingSpeak Channel ID", required: true
//    input name: "token", type: "password", title: "Access Token", required: true
    input name: "TempVar", type: "text", title: "ThingSpeak Temp Field", required: true, defaultValue: "1"
    input name: "pHVar", type: "text", title: "ThingSpeak pH Field", required: true, defaultValue: "2"
    input name: "BattVar", type: "text", title: "ThingSpeak Battery Field", required: true, defaultValue: "4"
}

metadata {
    definition (name: "ThingSpeak Pool Data Feed", namespace: "jkeenest", author: "Jeremy Keen") {
        capability "Polling"
        capability "Sensor"
        capability "Refresh"
        capability "Temperature Measurement"
        capability "pH Measurement"

        attribute "temperature", "number"
        attribute "pH", "number"
    }

    tiles(scale: 2) {
        valueTile("temperature", "device.temperature", width: 2, height: 2) {
            state("temperature", label:'${currentValue}°', unit:"F",
                backgroundColors:[
                    [value: 62, color: "#153591"],
                    [value: 66, color: "#1e9cbb"],
                    [value: 70, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 78, color: "#f1d801"],
                    [value: 82, color: "#d04e00"],
                    [value: 86, color: "#bc2323"]
                ]
            )
        }

        valueTile("ph", "device.ph", width: 2, height: 2) {
            state( "pH", label:'${currentValue}',
            	backgroundColors:[
                	[value: 7.1, color: "#153591"],
                    [value: 7.2, color: "#1e9cbb"],
                    [value: 7.3, color: "#90d2a7"],
                    [value: 7.5, color: "#44b621"],
                    [value: 7.7, color: "#f1d801"],
                    [value: 7.8, color: "#d04e00"],
                    [value: 8.0, color: "#bc2323"]
				]
			)
        }

        valueTile("battery", "device.battery", width: 2, height: 2) {
            state "default", label:'${currentValue}%'
        }

        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main("temperature")
        details(["temperature", "ph", "battery", "refresh"])
    }
}

// handle commands
def poll() {
    log.debug "Executing 'poll'"

    getAll()
}

def refresh() {
    log.debug "Executing 'refresh'"

    getAll()
}

def getAll() {
    getTemperature()
    getPH()
    getBattery()
}

def parse(String description) {
    def pair = description.split(":")

    createEvent(name: pair[0].trim(), value: pair[1].trim())
}

private getTemperature() {
    def closure = { response ->
        log.debug "Temperature request was successful, $response.data"

        sendEvent(name: "temperature", value: response.data.result)
    }
    httpGet("https://api.thingspeak.com/channels/${channelID}/fields/${TempVar}/last", closure)
}

private getPH() {
    def closure = { response ->
        log.debug "pH request was successful, $response.data"

        sendEvent(name: "ph", value: response.data.result)
    }

    httpGet("https://api.thingspeak.com/channels/${channelID}/fields/${pHVar}/last", closure)
}

private getBattery() {
    def closure = { response ->
        log.debug "Battery Level request was successful, $response.data"

        sendEvent(name: "battery", value: response.data.result)
    }

    httpGet("https://api.thingspeak.com/channels/${channelID}/fields/${BattVar}/last", closure)
}