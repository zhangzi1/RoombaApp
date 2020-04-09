import socket
import _thread


def receiver(num, client_socket, client_addr):
    while 1:
        recv_data = client_socket.recv(1024)
        if recv_data == b"":
            print("Thread", num, "finished")
            break
        data = recv_data.decode()
        print("Connection", num, client_addr, data)
        if data == "auto" or data == "manu":
            client_socket.send(("39%" + "\n").encode())


def UDPtransponder():
    UDP_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    UDP_socket.bind(("", 8864))
    print("Transponder is on. ")
    while 1:
        data, (ip, port) = UDP_socket.recvfrom(1024)  # 一次接收1024字节
        if data.decode() == "Are you Roomba?":
            print("UDP broadcast from:" + ip)
            UDP_socket.sendto("I am Roomba.".encode(), (ip, 8865))


_thread.start_new_thread(UDPtransponder, ())
TCP_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
TCP_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
TCP_socket.bind(("", 8866))
TCP_socket.listen(10)
num = 0
print("Server is on. ")
while 1:
    num += 1
    client_socket, client_addr = TCP_socket.accept()
    print("Connection", num, "for", client_addr)
    _thread.start_new_thread(receiver, (num, client_socket, client_addr,))
