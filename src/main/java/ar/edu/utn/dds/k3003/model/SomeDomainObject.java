package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.exceptions.SomeDomainException;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SomeDomainObject {
  private String anAttribute;
  private Long otherAttribute;

  public SomeDomainObject sum(SomeDomainObject other) {
    if (Objects.isNull(other.getAnAttribute())) {
      throw new SomeDomainException("anAttribute is null", other);
    }
    return new SomeDomainObject(
        anAttribute + other.getAnAttribute(), otherAttribute + other.getOtherAttribute());
  }
}
