package com.biengual.core.domain.document.content.script;

import org.springframework.data.annotation.TypeAlias;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@TypeAlias(value = "YoutubeScript")
public class YoutubeScript implements ListeningScript {
    private double startTimeInSecond;
    private double durationInSecond;
    private String enScript;
    private String koScript;

    public static ListeningScript of(double startTimeInSecond, double durationInSecond, String enScript, String koScript) {
        return YoutubeScript.builder()
            .startTimeInSecond(startTimeInSecond)
            .durationInSecond(durationInSecond)
            .enScript(enScript)
            .koScript(koScript)
            .build();
    }

    @Override
    public String getEnScript() {
        return this.enScript;
    }

    @Override
    public String getKoScript(){
        return this.koScript;
    }
}
