package com.nextune.feed.dto;

import java.util.UUID;

public record InteracaoResponse(
        UUID postId,
        boolean curtido,
        boolean salvo,
        boolean pulou,
        int segundosOuvidos) {}
