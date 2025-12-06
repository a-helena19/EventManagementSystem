package everoutproject.Event.application.services;

import everoutproject.Event.application.exceptions.PaymentFailedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class PaymentService {

    private final RestTemplate restTemplate;
    private static final String PAYMENT_SERVICE_URL = "http://localhost:8081/api/payment";

    public PaymentService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void processPayment(BigDecimal depositAmount, String paymentMethod) throws PaymentFailedException {
        try {
            // Erstelle Payment Request
            Map<String, Object> paymentRequest = Map.of("price", depositAmount);

            // Sende Request an Payment-Service
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                    PAYMENT_SERVICE_URL,
                    paymentRequest,
                    (Class<Map<String, Object>>)(Class<?>)Map.class
            );

            // Prüfe Response
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                String status = (String) body.get("status");

                if (!"success".equals(status)) {
                    String message = (String) body.getOrDefault("message", "Payment failed");
                    throw new PaymentFailedException(message);
                }
            } else {
                throw new PaymentFailedException("Payment service returned unexpected response");
            }

        } catch (ResourceAccessException e) {
            // Connection-Fehler (z.B. Payment-Service offline)
            throw new PaymentFailedException("Payment service temporarily unavailable. Please try again later.", e);
        } catch (Exception e) {
            if (e instanceof PaymentFailedException) {
                throw e;
            }
            // Andere Fehler vom Payment-Service (z.B. 400 BAD_REQUEST)
            throw new PaymentFailedException("Payment failed. Please try again with a different payment method.");
        }
    }
}

