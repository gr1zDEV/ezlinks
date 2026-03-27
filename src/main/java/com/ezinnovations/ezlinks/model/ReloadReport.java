package com.ezinnovations.ezlinks.model;

public record ReloadReport(CommandBindingReport bindingReport,
                           CommandSyncResult syncResult) {
}
