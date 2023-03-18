class TimeUtils {
  static DateTime getUtcNow() {
    return DateTime.now().toUtc();
  }

  static String getIsoFormat() {
    return getUtcNow().toIso8601String();
  }
}
