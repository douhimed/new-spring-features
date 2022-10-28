package org.adex.services;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@SpringBootApplication
public class ServicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicesApplication.class, args);
    }

}

record Message(String message) {
}

@RestController
class MessageController {

	private final ObservationRegistry observationRegistry;

	public MessageController(ObservationRegistry observationRegistry) {
		this.observationRegistry = observationRegistry;
	}

	@GetMapping("/message/{value}")
    public Message sendMessage(@PathVariable String value) {
        if (Objects.isNull(value) || value.trim().length() < 3)
            throw new IllegalArgumentException("The message  is not valid");

		return Observation
				.createNotStarted("message.value", this.observationRegistry)
				.observe(() -> new Message("Hey " + value + "!"));
    }

}

@ControllerAdvice
class MessageExceptionHandlerController {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail onException(HttpServletRequest request, IllegalArgumentException ex) {
        request.getAttributeNames()
                .asIterator()
                .forEachRemaining(attribute -> System.out.println("AttributeName : " + attribute));
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(404), ex.getMessage());
    }
}