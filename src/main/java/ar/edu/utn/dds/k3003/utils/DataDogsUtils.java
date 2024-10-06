package ar.edu.utn.dds.k3003.utils;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmHeapPressureMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.datadog.DatadogConfig;
import io.micrometer.datadog.DatadogMeterRegistry;
import lombok.Getter;

import java.time.Duration;

public class DataDogsUtils {
    @Getter
    private final StepMeterRegistry registro;

    public DataDogsUtils(String appTag) {
        // crea un registro para nuestras métricas basadas en DD
        var config = new DatadogConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            public String apiKey() {
                return System.getenv("DDAPI");
            }

            @Override
            public String uri() {
                return "https://api.us3.datadoghq.com";
            }

            @Override
            public String get(String k) {
                return null; // accept the rest of the defaults
            }
        };
        registro = new DatadogMeterRegistry(config, Clock.SYSTEM);
        registro.config().commonTags("Heladeras", appTag);
        initInfraMonitoring();
    }

    private void initInfraMonitoring() {
        // agregamos a nuestro reigstro de métricas to do lo relacionado a infra/tech
        // de la instancia y JVM
        try (var jvmGcMetrics = new JvmGcMetrics(); var jvmHeapPressureMetrics = new JvmHeapPressureMetrics()) {
            jvmGcMetrics.bindTo(registro);
            jvmHeapPressureMetrics.bindTo(registro);
        }
        new JvmMemoryMetrics().bindTo(registro);
        new ProcessorMetrics().bindTo(registro);
        new FileDescriptorMetrics().bindTo(registro);
    }

}