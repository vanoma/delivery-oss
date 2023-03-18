package com.vanoma.api.order.external;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class EmailParams {
    private String subject;
    private String content;
    private List<String> recipients;
}
