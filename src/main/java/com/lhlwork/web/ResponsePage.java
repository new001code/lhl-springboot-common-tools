package com.lhlwork.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponsePage implements Serializable {
    @Serial
    private static final long serialVersionUID = 134534534534898912L;
    private Long total;
    private List<?> data;

    private Object footer;
}
