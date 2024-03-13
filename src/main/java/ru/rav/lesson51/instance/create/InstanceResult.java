package ru.rav.lesson51.instance.create;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstanceResult {
    private String message;
    private InstanceData data;

    public InstanceResult() {
        data = new InstanceData();
    }
}
