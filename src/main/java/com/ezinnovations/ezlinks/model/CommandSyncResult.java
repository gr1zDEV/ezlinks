package com.ezinnovations.ezlinks.model;

public record CommandSyncResult(boolean attempted,
                                boolean succeeded,
                                String detail) {
}
