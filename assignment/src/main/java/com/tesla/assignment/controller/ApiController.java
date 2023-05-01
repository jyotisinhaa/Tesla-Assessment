package com.tesla.assignment.controller;
import com.tesla.assignment.entity.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class ApiController {
    private final List<String> errorList = new ArrayList<>();
    @PostMapping(value = "/temp")
    public ResponseEntity<Map<String, Object>> postData(@RequestBody Data payload){
        int deviceId = 0;
        long epoch = 0;
        double temperature = 0;
        String payloadString = payload.getData();
        //If the data is empty then return the error
        if (payloadString.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "bad request"));
        }
        String[] chunk= payloadString.split(":");

        try{
            deviceId = Integer.parseInt(chunk[0]);
        }
        catch (NumberFormatException e) {
            errorList.add(chunk[0]);
        }
        try {
            epoch = Long.parseLong(chunk[1]);
        }catch(NumberFormatException e){
            errorList.add(chunk[1]);
        }
        try{
            if(chunk[3].contains(".")){
                temperature = Double.parseDouble(chunk[3]);
            } else{
                errorList.add(chunk[3]);
            }
        }
        catch(NumberFormatException e){
            errorList.add(chunk[3]);
        }

        //If the length of the chunk is not 4 or the third element doesn't contain 'Temperature' keyword then we need to throw an error
        if (chunk.length!=4 || !chunk[2].contains("Temperature") ||deviceId==0||epoch==0||temperature==0.0){
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
        else{
            return ResponseEntity.ok().body(Map.of("overtemp", "false"));
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
