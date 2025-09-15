package web.common.util;

import com.github.f4b6a3.ulid.UlidCreator;

public final class Ulids {
  private Ulids() {}
  public static String newUlid() {
    return UlidCreator.getUlid().toString();
  }
  // Backward compatibility for existing code/tests
  public static String generate() {
    return newUlid();
  }
}
