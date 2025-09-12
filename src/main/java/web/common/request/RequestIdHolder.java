package web.common.request;

public final class RequestIdHolder {
  private static final InheritableThreadLocal<String> TL = new InheritableThreadLocal<>();

  private RequestIdHolder() {}

  public static void set(String id) {
    TL.set(id);
  }

  public static String get() {
    return TL.get();
  }

  public static void clear() {
    TL.remove();
  }

  public static String getOrCreate() {
    String id = TL.get();
    if (id == null) {
      id = web.common.util.Ulids.newUlid();
      TL.set(id);
    }
    return id;
  }
}
