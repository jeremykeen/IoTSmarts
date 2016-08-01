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
    input name: "deviceId", type: "text", title: "Device ID", required: true
    input name: "token", type: "password", title: "Access Token", required: true
    input name: "particleTemperatureVar", type: "text", title: "Particle Temperature Variable", required: true, defaultValue: "temperature"
    input name: "particlepHVar", type: "text", title: "Spark pH Variable", required: true, defaultValue: "ph"
    input name: "particleBatteryLevelVar", type: "text", title: "Spark Heat Index Variable", required: true, defaultValue: "batteryLevel"
}

metadata {
    definition (name: "Particle Remote Temperature and Humidity Logger", namespace: "jkeenest", author: "Jeremy Keen") {
        capability "Polling"
        capability "Sensor"
        capability "Refresh"
        capability "Temperature Measurement"

        attribute "temperature", "number"
    }

    tiles(scale: 2) {
        valueTile("temperature", "device.temperature", width: 2, height: 2) {
            state("temperature", label:'${currentValue}°', unit:"F",
                backgroundColors:[
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
                ]
            )
        }

        valueTile("heatIndex", "device.pH", width: 2, height: 2) {
            state "heatIndex", label:'${currentValue}'
        }

        valueTile("humidity", "device.batteryLevel", width: 2, height: 2) {
            state "default", label:'${currentValue}%'
        }

        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main "temperature"
        details(["temperature", "pH", "batteryLevel", "refresh"])
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

    httpGet("https://api.particle.io/v1/devices/${deviceId}/${particleTemperatureVar}?access_token=${token}", closure)
}

private getPH() {
    def closure = { response ->
        log.debug "pH request was successful, $response.data"

        sendEvent(name: "ph", value: response.data.result)
    }

    httpGet("https://api.particle.io/v1/devices/${deviceId}/${particlepHVar}?access_token=${token}", closure)
}

private getBattery() {
    def closure = { response ->
        log.debug "Battery Level request was successful, $response.data"

        sendEvent(name: "batteryLevel", value: response.data.result)
    }

    httpGet("https://api.particle.io/v1/devices/${deviceId}/${particleBatteryLevelVar}?access_token=${token}", closure)
}
