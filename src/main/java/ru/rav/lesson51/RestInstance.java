package ru.rav.lesson51;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.rav.lesson51.instance.create.InstanceCreate;
import ru.rav.lesson51.instance.create.InstanceResult;
import ru.rav.lesson51.instance.create.InstanceService;

@RestController
public class RestInstance {
    private final InstanceService instanceService;

    @Autowired
    public RestInstance(InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    @RequestMapping(value = "/corporate-settlement-instance/create", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Object> handleInstance(@RequestBody @Valid InstanceCreate instanceCreate, BindingResult result) {
        InstanceResult instanceResult = new InstanceResult();
        int iError;

        if (result.hasErrors()) {
            for( ObjectError er : result.getAllErrors()) {
                //throw new ResponseStatusException( 400, er.getDefaultMessage(), null);
                instanceResult.setMessage( er.getDefaultMessage());
                return ResponseEntity.status( 400).body( instanceResult);
            }
        }

        iError = instanceService.setProduct( instanceCreate, instanceResult);

        if (iError != 200) {
            //throw new ResponseStatusException( iError, instanceService.getMessage(), null);
            instanceResult.setMessage(instanceService.getMessage());
            return ResponseEntity.status(iError).body(instanceResult);
        }

        return ResponseEntity.ok( instanceResult);
    }
}
