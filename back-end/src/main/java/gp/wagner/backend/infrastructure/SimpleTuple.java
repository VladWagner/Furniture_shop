package gp.wagner.backend.infrastructure;

import lombok.Getter;
import lombok.Setter;

//Попытка собственной реализации словаря
@Getter
@Setter
public class SimpleTuple<V1, V2> {

    private V1 value1;

    private V2 value2;

    public SimpleTuple(V1 value1, V2 value2) {
        this.value1 = value1;
        this.value2 = value2;
    }
}
