package com.tesla.assignment.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class ApiController {
    private final List<String> errorList = new ArrayList<>();
    @PostMapping(value = "/temp")
    public ResponseEntity<Map<String, Object>> postData(@RequestBody(required = false) Map<String, Object> payload){

        // If the payload is null,empty, doesn't contain data parameter then throw the error
        if (payload == null || payload.isEmpty() || payload.get("data") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "bad request"));
        }

        try{
            String payloadString = (String) payload.get("data");
            if ( payloadString == null || payloadString.isEmpty()  || payloadString.trim().isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "bad request"));
            }
            String[] chunk= payloadString.split(":");
            //If the length of the chunk is not 4 or the third element doesn't contain 'Temperature' keyword then we need to throw an error
            if (chunk.length!=4 || !chunk[2].contains("'Temperature'")){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "bad request"));
            }
            int deviceId = 0;
            long epoch = 0;
            double temperature = 0.0;
            // Handling the format of deviceId, epoch, and temperature
            try{
                deviceId = Integer.parseInt(chunk[0]);
                epoch = Long.parseLong(chunk[1]);
                temperature =  (chunk[3].contains("."))?Double.parseDouble(chunk[3]):0.0;

            }
            catch (NumberFormatException e) {
                errorList.add(payloadString);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "bad request"));

            }
            if (deviceId==0||epoch==0||temperature==0.0){
                errorList.add(payloadString);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "bad request"));
            }
            //Check for the over temperature
            if (temperature>= 90){
                String formattedTime = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(epoch);
                return ResponseEntity.ok().body(Map.of(
                        "overtemp", true,
                        "device_id", deviceId,
                        "formatted_time",formattedTime
                ));
            }
            else {
                return ResponseEntity.ok().body(Map.of("overtemp", "false"));
            }
        }
        catch (HttpMessageNotReadableException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "bad request"));
        }

    }

    @GetMapping("/errors")
    public ResponseEntity<Map<String, Object>> getErrors(){
        List<String> errors = new ArrayList<>(errorList);
        return ResponseEntity.ok(Map.of("errors", errors));
    }

    @DeleteMapping("/errors")
    public ResponseEntity<Map<String, Object>> formatErrors() {
        errorList.clear();
        return ResponseEntity.ok(Map.of("message", "cleared error buffer"));
    }
}
