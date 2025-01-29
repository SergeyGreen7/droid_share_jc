package org.example.project.data

import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class TcpP2pClientServer {

    private var client: Socket? = null
    private var server: ServerSocket? = null

    fun shutdown() {
        try {
            server?.close()
            println("Server socket is closed")
        } catch (e: Exception) {
            println("Could not close the server socket, $e")
        }
        try {
            client?.close()
            println("Client socket is closed")
        } catch (e: Exception) {
            println("Could not close the client socket, $e")
        }

        println("TcpP2pConnector, shutdown(), isSocketCreated() = ${isSocketCreated()}")
    }

    fun isSocketNotCreated(): Boolean {
        return !isSocketCreated()
    }

    fun isSocketCreated(): Boolean {
        println( "TcpP2pConnector, isSocketCreated():")
        println( "    server == null = ${server == null}")
        if (server != null) {
            println( "    server!!.isBound = ${server!!.isBound}")
            println( "    !server!!.isClosed = ${!server!!.isClosed}")
        }
        println( "    client == null = ${client == null}")
        if (client != null) {
            println( "    client!!.isConnected = ${client!!.isConnected}")
            println( "    !client!!.isClosed = ${!client!!.isClosed}")
        }

        return isServerOpened() || isClientConnected()
    }

    fun getInputStream(): InputStream {
        return client!!.getInputStream()
    }

    fun getOutputStream(): OutputStream {
        return client!!.getOutputStream()
    }

    fun isServerOpened(): Boolean {
        return server != null && server!!.isBound && !server!!.isClosed
    }

    fun isClientConnected(): Boolean {
        return client != null && client!!.isConnected && !client!!.isClosed
    }

    fun createClient(address: InetAddress, port: Int, timeout: Int) {
        try {
            client = Socket()
            client?.connect(InetSocketAddress(address, port), timeout)
            println( "createClient, client = $client")
            println( "Client: connection to server is done")
        } catch (e: Exception) {
            println( "Exception during connection to the server: $e")
        }
    }

    fun createServer(port: Int) {
        if (!isServerOpened()) {
            server = ServerSocket(port)
            println( "Server: socket created")
        } else {
            println( "Server: Socket is already opened")
        }
        try {
            println( "Start server.accept()...")
            client = server?.accept()
            println( "Server: accept done")
            println( "createServer(), client = $client")
        } catch (e: Exception) {
            println( "TcpP2pConnector, createServer, exception happened: $e")
        }
    }
}