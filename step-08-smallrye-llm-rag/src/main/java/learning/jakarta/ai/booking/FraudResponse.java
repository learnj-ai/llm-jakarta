package learning.jakarta.ai.booking;

import lombok.Data;

import java.util.List;

@Data
public class FraudResponse {
    private String customerName;
    private String customerSurname;
    private boolean fraudDetected;
    private List<String> bookingIds;
}