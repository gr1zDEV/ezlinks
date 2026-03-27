package com.ezinnovations.ezlinks.model;

import java.util.List;

public record ConfiguredCommand(String name,
                                String permission,
                                List<String> aliases,
                                String soundKey,
                                List<MessageLine> lines) {
}
