package tech.blastmc.radial.macros;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class RadialGroup {

    private String name;
    private int keyCode;
    private List<RadialOption> options;

    public RadialGroup clone() {
        return new RadialGroup(name, keyCode, new ArrayList<>(options));
    }

}
