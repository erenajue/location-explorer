package ga.elirey.locationexplorer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Not enough points to process request")
public class InsufficientRequiredPointsException extends RuntimeException {

}
