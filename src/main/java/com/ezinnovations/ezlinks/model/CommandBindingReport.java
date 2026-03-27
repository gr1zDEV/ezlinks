package com.ezinnovations.ezlinks.model;

import java.util.List;

public record CommandBindingReport(int dynamicCommandCount,
                                   int boundLabelCount,
                                   List<String> conflicts) {
}
