package com.nifilili.core.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {

    private String to;
    private String subject;
    private String body;

    @Builder.Default
    private boolean html = false;
}
