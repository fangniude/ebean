package io.ebeaninternal.server.type;

import io.ebeanservice.docstore.api.mapping.DocPropertyType;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.EMPTY_LIST;

/**
 * H2 database support for DB ARRAY.
 */
@SuppressWarnings("rawtypes")
class ScalarTypeArrayListH2 extends ScalarTypeArrayList {

  static PlatformArrayTypeFactory factory() {
    return new ScalarTypeArrayListH2.Factory();
  }

  static class Factory implements PlatformArrayTypeFactory {

    private final Map<String, ScalarTypeArrayListH2> cache = new HashMap<>();

    /**
     * Return the ScalarType to use based on the List's generic parameter type.
     */
    @Override
    public ScalarType<?> typeFor(Type valueType, boolean nullable) {
      synchronized (this) {
        String key = valueType + ":" + nullable;
        if (valueType.equals(UUID.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArrayListH2(nullable, "uuid", DocPropertyType.UUID, ArrayElementConverter.UUID));
        }
        if (valueType.equals(Long.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArrayListH2(nullable, "bigint", DocPropertyType.LONG, ArrayElementConverter.LONG));
        }
        if (valueType.equals(Integer.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArrayListH2(nullable, "integer", DocPropertyType.INTEGER, ArrayElementConverter.INTEGER));
        }
        if (valueType.equals(Double.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArrayListH2(nullable, "float", DocPropertyType.DOUBLE, ArrayElementConverter.DOUBLE));
        }
        if (valueType.equals(String.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArrayListH2(nullable, "varchar", DocPropertyType.TEXT, ArrayElementConverter.STRING));
        }
        throw new IllegalArgumentException("Type [" + valueType + "] not supported for @DbArray mapping");
      }
    }

    @Override
    public ScalarType<?> typeForEnum(ScalarType<?> scalarType, boolean nullable) {
      return new ScalarTypeArrayListH2(nullable, "varchar", DocPropertyType.TEXT, new ArrayElementConverter.EnumConverter(scalarType));
    }
  }

  private ScalarTypeArrayListH2(boolean nullable, String arrayType, DocPropertyType docPropertyType, ArrayElementConverter converter) {
    super(nullable, arrayType, docPropertyType, converter);
  }

  @Override
  public void bind(DataBind bind, List value) throws SQLException {
    if (value == null) {
      bindNull(bind);
    } else {
      bind.setObject(toArray(value));
    }
  }

  @Override
  protected void bindNull(DataBind bind) throws SQLException {
    if (nullable) {
      bind.setNull(Types.ARRAY);
    } else {
      bind.setObject(toArray(EMPTY_LIST));
    }
  }
}
