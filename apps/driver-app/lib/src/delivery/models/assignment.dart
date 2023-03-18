import 'package:vanoma_driver/src/delivery/models/package.dart';

class Assignment {
  final String assignmentId;
  final Package package;

  Assignment({required this.assignmentId, required this.package});

  factory Assignment.fromJson(Map<String, dynamic> json) {
    return Assignment(
      assignmentId: json['assignmentId'],
      package: Package.fromJson(json['package']),
    );
  }
}
