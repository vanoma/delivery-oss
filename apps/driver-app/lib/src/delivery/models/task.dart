import 'package:vanoma_driver/src/delivery/models/address.dart';
import 'package:vanoma_driver/src/delivery/models/contact.dart';
import 'package:vanoma_driver/src/delivery/models/package.dart';

// ignore_for_file: constant_identifier_names
enum TaskType { PICK_UP, DROP_OFF }

class Task {
  final String taskId;
  final TaskType type;
  final Address address;
  final Contact contact;
  final String? note;
  final Contact otherParty;
  bool isCompleted;

  Task({
    required this.taskId,
    required this.type,
    required this.address,
    required this.contact,
    required this.note,
    required this.otherParty,
    this.isCompleted = false,
  });

  factory Task.fromJson(Map<String, dynamic> json) {
    TaskType type = toTaskType(json['type']);
    Package package = Package.fromJson(json['package']);
    return Task(
      taskId: json['taskId'],
      type: type,
      address:
          type == TaskType.PICK_UP ? package.fromAddress : package.toAddress,
      contact:
          type == TaskType.PICK_UP ? package.fromContact : package.toContact,
      note: type == TaskType.PICK_UP ? package.fromNote : package.toNote,
      otherParty:
          type == TaskType.PICK_UP ? package.toContact : package.fromContact,
      isCompleted: json['completedAt'] != null,
    );
  }

  static TaskType toTaskType(String value) {
    return TaskType.values
        .firstWhere((e) => e.toString() == 'TaskType.' + value);
  }
}
