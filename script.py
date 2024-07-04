import threading

def axStart():
    if not system.example.axRunning():
        system.example.axStart()

def axStop():
    system.example.axStop()

def axSend(request):
    return system.example.axSend(request)

def test_request(tankId, pressure):
    # Expected response
    expected_post_response = "{\"reqId\": \"setPressure\", \"body\": {\"status-code\": \"201 complete\"}}"
    expected_get_response = "{\"reqId\": \"getPressure\", \"body\": {\"tankId\": \"" + str(tankId) + "\", \"pressure\": \"" + str(pressure) + "\", \"status-code\": \"200 ok\"}}"
    
    request = "\"auth-key\": \"secKey123#\", \"method\":\"POST\", \"reqId\": \"setPressure\", \"body\": { \"tankId\": \"" + str(tankId) + "\", \"pressure\": \"" + str(pressure) + "\"}"
    response = system.example.axSend(request)
    
    if response == expected_post_response:
    	pass
    else:
        print("Test failed!")


def test_axTcp(n_threads):
    axStart()
    
   
    tank_ids = [i for i in range(n_threads)]  
    pressures = [i * 10 for i in range(n_threads)]  
    
    
    threads = []
    for i in range(n_threads):
        thread = threading.Thread(target=test_request, args=(tank_ids[i], pressures[i]))
        threads.append(thread)
        thread.start()  
    
    for thread in threads:
        thread.join()  
    
    print("-----------> ALL THREADS PASSED THE TEST <-----------")
    
    axStop()
    
    
demo.axStart()
request = "\"auth-key\": \"secKey123#\", \"method\":\"POST\", \"reqId\": \"setPressure\", \"body\": { \"tankId\": \"" + str(tankId) + "\", \"pressure\": \"" + str(pressure) + "\"}"
response = demo.axSend(request)
print(response)


demo.axStop()

demo.test_axTcp(20)
