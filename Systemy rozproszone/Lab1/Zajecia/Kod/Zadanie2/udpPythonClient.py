import socket

serverIP = "127.0.0.1"
serverPort = 9008
msg = "żółta gęś"

print("Python UDP client!")
client = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
client.sendto(bytes(msg, 'utf-8'), (serverIP, serverPort))

buff = []
buff, address = client.recvfrom(2048)
print("received: " + str(buff, "utf-8") + " from: " + str(address[0]) + ":" + str(address[1]))