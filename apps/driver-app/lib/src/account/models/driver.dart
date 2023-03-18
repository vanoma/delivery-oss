enum DriverStatus { ACTIVE, PENDING, INACTIVE }
enum DriverStatusColor { AVAILABLE, UNAVAILABLE }

extension ParseToString on DriverStatus {
  String toShortString() {
    return toString().split('.').last;
  }
}

class Driver {
  final String driverId;
  final String firstName;
  final String lastName;
  final String phoneNumber;
  final String secondPhoneNumber;
  final DriverStatus status;
  bool isAvailable;
  int assignmentCount;

  Driver({
    required this.driverId,
    required this.firstName,
    required this.lastName,
    required this.phoneNumber,
    required this.secondPhoneNumber,
    required this.status,
    required this.isAvailable,
    required this.assignmentCount,
  });

  factory Driver.fromJson(Map<String, dynamic> json) {
    return Driver(
      driverId: json['driverId'],
      firstName: json['firstName'],
      lastName: json['lastName'],
      phoneNumber: json['phoneNumber'],
      secondPhoneNumber: json['secondPhoneNumber'],
      status: Driver.toDriverStatus(json['status']),
      isAvailable: json['isAvailable'],
      assignmentCount: json['assignmentCount'],
    );
  }
  Map<String, dynamic> toJson() => {
        'driverId': driverId,
        'firstName': firstName,
        'lastName': lastName,
        'phoneNumber': phoneNumber,
        'secondPhoneNumber': secondPhoneNumber,
        'status': status.toShortString(),
        'isAvailable': isAvailable,
        'assignmentCount': assignmentCount,
      };

  static DriverStatus toDriverStatus(String value) {
    return DriverStatus.values
        .firstWhere((e) => e.toString() == 'DriverStatus.' + value);
  }
}
