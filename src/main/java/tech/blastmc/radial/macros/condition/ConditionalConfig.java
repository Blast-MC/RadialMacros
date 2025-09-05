package tech.blastmc.radial.macros.condition;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConditionalConfig {

    private ConditionalityRule type;
    private String value;

}
