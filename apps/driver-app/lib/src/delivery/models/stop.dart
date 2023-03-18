import 'package:vanoma_driver/src/delivery/models/address.dart';
import 'package:vanoma_driver/src/delivery/models/task.dart';

class Stop {
  final String stopId;
  final Address address;
  int ranking;
  final List<Task> tasks;
  bool hasDeparted;
  bool hasArrived;
  bool completedAllTasks;

  Stop({
    required this.stopId,
    required this.address,
    required this.ranking,
    required this.tasks,
    this.hasDeparted = false,
    this.hasArrived = false,
    this.completedAllTasks = false,
  });

  factory Stop.fromJson(Map<String, dynamic> json, int index) {
    List<Task> tasks = (json['currentTasks'] as List<dynamic>)
        .map((e) => Task.fromJson(e as Map<String, dynamic>))
        .toList();

    return Stop(
      stopId: json['stopId'],
      address: tasks[0].address,
      ranking: index,
      hasDeparted: json['departedAt'] != null,
      hasArrived: json['arrivedAt'] != null,
      completedAllTasks: json['completedAt'] != null,
      tasks: tasks,
    );
  }
}
