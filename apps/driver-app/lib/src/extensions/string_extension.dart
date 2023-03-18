extension StringExtension on String {
  String capitalize() {
    return '${this[0].toUpperCase()}${substring(1)}';
  }

  String initials() {
    String initials = '';
    for (int i = 0; i < split(' ').length; i++) {
      if (i < 2) {
        initials += split(' ')[i][0].toUpperCase();
      }
    }
    return initials;
  }
}
