import socket

serverIP = "127.0.0.1"
serverPort = 9008
msg = (69).to_bytes(4, byteorder="little")

print("Python UDP client!")
client = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
client.sendto(msg, (serverIP, serverPort))

buff = []
buff, address = client.recvfrom(2048)
received_number = int.from_bytes(buff, byteorder='little')
print("received: " + str(received_number) + " from: " + str(address[0]) + ":" + str(address[1]))